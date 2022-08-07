/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jreleaser.sdk.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Changelog;
import org.jreleaser.model.GitService;
import org.jreleaser.model.Gitlab;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.releaser.spi.User;
import org.jreleaser.model.util.VersionUtils;
import org.jreleaser.util.CollectionUtils;
import org.jreleaser.util.StringUtils;
import org.jreleaser.util.Version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.groupingBy;
import static org.jreleaser.sdk.git.GitSdk.extractTagName;
import static org.jreleaser.util.ComparatorUtils.lessThan;
import static org.jreleaser.util.Constants.KEY_CHANGELOG_CHANGES;
import static org.jreleaser.util.Constants.KEY_CHANGELOG_CONTRIBUTORS;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.MustacheUtils.passThrough;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.normalizeRegexPattern;
import static org.jreleaser.util.StringUtils.stripMargin;
import static org.jreleaser.util.StringUtils.toSafeRegexPattern;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ChangelogGenerator {
    private static final String UNCATEGORIZED = "<<UNCATEGORIZED>>";
    private static final String REGEX_PREFIX = "regex:";

    public ChangelogGenerator() {

    }

    protected String createChangelog(JReleaserContext context) throws IOException {
        GitService gitService = context.getModel().getRelease().getGitService();
        Changelog changelog = gitService.getChangelog();

        String separator = lineSeparator();
        if (Gitlab.NAME.equals(gitService.getServiceName())) {
            separator += lineSeparator();
        }
        String commitSeparator = separator;

        try {
            Git git = GitSdk.of(context).open();
            context.getLogger().debug(RB.$("changelog.generator.resolve.commits"));
            Iterable<RevCommit> commits = resolveCommits(git, context);

            Comparator<RevCommit> revCommitComparator = Comparator.comparing(RevCommit::getCommitTime).reversed();
            if (changelog.getSort() == Changelog.Sort.ASC) {
                revCommitComparator = Comparator.comparing(RevCommit::getCommitTime);
            }
            context.getLogger().debug(RB.$("changelog.generator.sort.commits"), changelog.getSort());

            if (changelog.resolveFormatted(context.getModel().getProject())) {
                return formatChangelog(context, changelog, commits, revCommitComparator, commitSeparator);
            }

            String commitsUrl = gitService.getResolvedCommitUrl(context.getModel());

            return "## Changelog" +
                lineSeparator() +
                lineSeparator() +
                StreamSupport.stream(commits.spliterator(), false)
                    .filter(c -> !changelog.isSkipMergeCommits() || c.getParentCount() <= 1)
                    .sorted(revCommitComparator)
                    .map(commit -> formatCommit(commit, commitsUrl, changelog, commitSeparator))
                    .collect(Collectors.joining(commitSeparator));
        } catch (GitAPIException e) {
            throw new IOException(e);
        }
    }

    protected String formatCommit(RevCommit commit, String commitsUrl, Changelog changelog, String commitSeparator) {
        String commitHash = commit.getId().name();
        String abbreviation = commit.getId().abbreviate(7).name();
        String[] input = commit.getFullMessage().trim().split(lineSeparator());

        List<String> lines = new ArrayList<>();

        if (changelog.isLinks()) {
            lines.add("[" + abbreviation + "](" + commitsUrl + "/" + commitHash + ") " + input[0].trim());
        } else {
            lines.add(abbreviation + " " + input[0].trim());
        }

        return String.join(commitSeparator, lines);
    }

    private Version version(JReleaserContext context, Ref tag, Pattern versionPattern) {
        return version(context, tag, versionPattern, false);
    }

    private Version version(JReleaserContext context, Ref tag, Pattern versionPattern, boolean strict) {
        return VersionUtils.version(context, extractTagName(tag), versionPattern, strict);
    }

    private Version defaultVersion(JReleaserContext context) {
        return VersionUtils.defaultVersion(context);
    }

    public static class Tags {
        private final Optional<Ref> current;
        private final Optional<Ref> previous;

        private static Tags empty() {
            return new Tags(null, null);
        }

        private static Tags current(Ref tag) {
            return new Tags(tag, null);
        }

        private static Tags previous(Ref tag) {
            return new Tags(null, tag);
        }

        private static Tags of(Ref tag1, Ref tag2) {
            return new Tags(tag1, tag2);
        }

        private Tags(Ref current, Ref previous) {
            this.current = Optional.ofNullable(current);
            this.previous = Optional.ofNullable(previous);
        }

        public Optional<Ref> getCurrent() {
            return current;
        }

        public Optional<Ref> getPrevious() {
            return previous;
        }
    }

    public Tags resolveTags(Git git, JReleaserContext context) throws GitAPIException {
        List<Ref> tags = git.tagList().call();

        GitService gitService = context.getModel().getRelease().getGitService();
        String effectiveTagName = gitService.getEffectiveTagName(context.getModel());
        String tagName = gitService.getConfiguredTagName();
        String tagPattern = tagName.replaceAll("\\{\\{.*}}", "\\.\\*");

        Pattern versionPattern = VersionUtils.resolveVersionPattern(context);
        VersionUtils.clearUnparseableTags();

        tags.sort((tag1, tag2) -> {
            Version v1 = version(context, tag1, versionPattern);
            Version v2 = version(context, tag2, versionPattern);
            return v2.compareTo(v1);
        });

        context.getLogger().debug(RB.$("changelog.generator.lookup.tag"), effectiveTagName);
        Optional<Ref> tag = tags.stream()
            .filter(ref -> extractTagName(ref).equals(effectiveTagName))
            .findFirst();

        Optional<Ref> previousTag = Optional.empty();
        String previousTagName = gitService.getConfiguredPreviousTagName();
        if (isNotBlank(previousTagName)) {
            context.getLogger().debug(RB.$("changelog.generator.lookup.previous.tag"), previousTagName);
            previousTag = tags.stream()
                .filter(ref -> extractTagName(ref).equals(previousTagName))
                .findFirst();
        }

        Version currentVersion = context.getModel().getProject().version();
        Version defaultVersion = defaultVersion(context);

        // tag: early-access
        if (context.getModel().getProject().isSnapshot()) {
            Project.Snapshot snapshot = context.getModel().getProject().getSnapshot();
            String effectiveLabel = snapshot.getEffectiveLabel();
            if (effectiveLabel.equals(effectiveTagName)) {
                if (snapshot.isFullChangelog()) {
                    tag = Optional.empty();
                }

                if (!tag.isPresent()) {
                    if (previousTag.isPresent()) {
                        tag = previousTag;
                    }

                    if (!tag.isPresent()) {
                        context.getLogger().debug(RB.$("changelog.generator.lookup.matching.tag"), tagPattern, effectiveTagName);

                        tag = tags.stream()
                            .filter(ref -> !extractTagName(ref).equals(effectiveTagName))
                            .filter(ref -> versionPattern.matcher(extractTagName(ref)).matches())
                            .filter(ref -> currentVersion.equalsSpec(version(context, ref, versionPattern, true)))
                            .filter(ref -> !defaultVersion.equals(version(context, ref, versionPattern, true)))
                            .findFirst();
                    }
                }

                if (tag.isPresent()) {
                    context.getLogger().debug(RB.$("changelog.generator.tag.found"), extractTagName(tag.get()));
                    return Tags.previous(tag.get());
                }

                return Tags.empty();
            }
        }

        // tag: latest
        if (!tag.isPresent()) {
            if (previousTag.isPresent()) {
                tag = previousTag;
            }

            if (!tag.isPresent()) {
                context.getLogger().debug(RB.$("changelog.generator.lookup.matching.tag"), tagPattern, effectiveTagName);
                tag = tags.stream()
                    .filter(ref -> !extractTagName(ref).equals(effectiveTagName))
                    .filter(ref -> versionPattern.matcher(extractTagName(ref)).matches())
                    .filter(ref -> currentVersion.equalsSpec(version(context, ref, versionPattern, true)))
                    .filter(ref -> !defaultVersion.equals(version(context, ref, versionPattern, true)))
                    .findFirst();
            }

            if (tag.isPresent()) {
                context.getLogger().debug(RB.$("changelog.generator.tag.found"), extractTagName(tag.get()));
                return Tags.previous(tag.get());
            }

            return Tags.empty();
        }

        // tag: somewhere in the middle
        if (!previousTag.isPresent()) {
            context.getLogger().debug(RB.$("changelog.generator.lookup.before.tag"), effectiveTagName, tagPattern);
            previousTag = tags.stream()
                .filter(ref -> extractTagName(ref).matches(tagPattern))
                .filter(ref -> !defaultVersion.equals(version(context, ref, versionPattern, true)))
                .filter(ref -> lessThan(version(context, ref, versionPattern, true), currentVersion))
                .findFirst();
        }

        if (previousTag.isPresent()) {
            context.getLogger().debug(RB.$("changelog.generator.tag.found"), extractTagName(previousTag.get()));
            return Tags.of(tag.get(), previousTag.get());
        }

        return Tags.current(tag.get());
    }

    protected Iterable<RevCommit> resolveCommits(Git git, JReleaserContext context) throws GitAPIException, IOException {
        Tags tags = resolveTags(git, context);
        GitService gitService = context.getModel().getRelease().getGitService();

        ObjectId head = git.getRepository().resolve(Constants.HEAD);

        // tag: early-access
        if (context.getModel().getProject().isSnapshot()) {
            Project.Snapshot snapshot = context.getModel().getProject().getSnapshot();
            String effectiveLabel = snapshot.getEffectiveLabel();
            if (effectiveLabel.equals(gitService.getEffectiveTagName(context.getModel()))) {
                if (tags.getPrevious().isPresent()) {
                    Ref fromRef = tags.getPrevious().get();
                    return git.log().addRange(getObjectId(git, fromRef), head).call();
                } else {
                    return git.log().add(head).call();
                }
            }
        }

        // tag: latest
        if (!tags.getCurrent().isPresent()) {
            if (tags.getPrevious().isPresent()) {
                Ref fromRef = tags.getPrevious().get();
                return git.log().addRange(getObjectId(git, fromRef), head).call();
            } else {
                return git.log().add(head).call();
            }
        }

        // tag: somewhere in the middle
        if (tags.getPrevious().isPresent()) {
            ObjectId fromRef = getObjectId(git, tags.getPrevious().get());
            ObjectId toRef = getObjectId(git, tags.getCurrent().get());
            return git.log().addRange(fromRef, toRef).call();
        }

        ObjectId toRef = getObjectId(git, tags.getCurrent().get());
        return git.log().add(toRef).call();
    }

    private ObjectId getObjectId(Git git, Ref ref) throws IOException {
        Ref peeled = git.getRepository().getRefDatabase().peel(ref);
        return peeled.getPeeledObjectId() != null ? peeled.getPeeledObjectId() : peeled.getObjectId();
    }

    protected String formatChangelog(JReleaserContext context,
                                     Changelog changelog,
                                     Iterable<RevCommit> commits,
                                     Comparator<RevCommit> revCommitComparator,
                                     String lineSeparator) {
        Set<Contributor> contributors = new LinkedHashSet<>();
        Map<String, List<Commit>> categories = new LinkedHashMap<>();

        StreamSupport.stream(commits.spliterator(), false)
            .filter(c -> !changelog.isSkipMergeCommits() || c.getParentCount() <= 1)
            .sorted(revCommitComparator)
            .map(Commit::of)
            .peek(c -> {
                if (!changelog.getContributors().isEnabled()) return;

                if (!changelog.getHide().containsContributor(c.author.name)) {
                    contributors.add(new Contributor(c.author));
                }
                c.committers.stream()
                    .filter(author -> !changelog.getHide().containsContributor(author.name))
                    .forEach(author -> contributors.add(new Contributor(author)));
            })
            .peek(c -> applyLabels(c, changelog.getLabelers()))
            .filter(c -> checkLabels(c, changelog))
            .forEach(commit -> categories
                .computeIfAbsent(categorize(commit, changelog), k -> new ArrayList<>())
                .add(commit));


        GitService gitService = context.getModel().getRelease().getGitService();
        String commitsUrl = gitService.getResolvedCommitUrl(context.getModel());

        StringBuilder changes = new StringBuilder();
        for (Changelog.Category category : changelog.getCategories()) {
            String categoryKey = category.getKey();
            if (!categories.containsKey(categoryKey) || changelog.getHide().containsCategory(categoryKey)) continue;

            changes.append("## ")
                .append(category.getTitle())
                .append(lineSeparator);

            final String categoryFormat = resolveCommitFormat(changelog, category);

            changes.append(categories.get(categoryKey).stream()
                    .map(c -> resolveTemplate(categoryFormat, c.asContext(changelog.isLinks(), commitsUrl)))
                    .collect(Collectors.joining(lineSeparator)))
                .append(lineSeparator)
                .append(lineSeparator());
        }

        if (!changelog.getHide().isUncategorized() && categories.containsKey(UNCATEGORIZED)) {
            if (changes.length() > 0) {
                changes.append("---")
                    .append(lineSeparator);
            }

            changes.append(categories.get(UNCATEGORIZED).stream()
                    .map(c -> resolveTemplate(changelog.getFormat(), c.asContext(changelog.isLinks(), commitsUrl)))
                    .collect(Collectors.joining(lineSeparator)))
                .append(lineSeparator)
                .append(lineSeparator());
        }

        StringBuilder formattedContributors = new StringBuilder();
        if (changelog.getContributors().isEnabled() && !contributors.isEmpty()) {
            formattedContributors.append("## Contributors")
                .append(lineSeparator)
                .append("We'd like to thank the following people for their contributions:")
                .append(lineSeparator)
                .append(formatContributors(context, changelog, contributors, lineSeparator))
                .append(lineSeparator);
        }

        Map<String, Object> props = context.fullProps();
        props.put(KEY_CHANGELOG_CHANGES, passThrough(changes.toString()));
        props.put(KEY_CHANGELOG_CONTRIBUTORS, passThrough(formattedContributors.toString()));

        return applyReplacers(context, changelog, stripMargin(applyTemplate(changelog.getResolvedContentTemplate(context), props)));
    }

    private String resolveCommitFormat(Changelog changelog, Changelog.Category category) {
        if (StringUtils.isNotBlank(category.getFormat())) {
            return category.getFormat();
        }
        return changelog.getFormat();
    }

    private String formatContributors(JReleaserContext context,
                                      Changelog changelog,
                                      Set<Contributor> contributors,
                                      String lineSeparator) {
        List<String> list = new ArrayList<>();
        String format = changelog.getContributors().getFormat();

        Map<String, List<Contributor>> grouped = contributors.stream()
            .peek(contributor -> {
                if (isNotBlank(format) && (format.contains("AsLink") || format.contains("Username"))) {
                    context.getReleaser().findUser(contributor.email, contributor.name)
                        .ifPresent(contributor::setUser);
                }
            })
            .collect(groupingBy(Contributor::getName));

        String contributorFormat = isNotBlank(format) ? format : "{{contributorName}}";

        grouped.forEach((name, cs) -> {
            Optional<Contributor> contributor = cs.stream()
                .filter(c -> c.getUser() != null)
                .findFirst();
            if (contributor.isPresent()) {
                list.add(resolveTemplate(contributorFormat, contributor.get().asContext()));
            } else {
                list.add(resolveTemplate(contributorFormat, cs.get(0).asContext()));
            }
        });

        String separator = contributorFormat.startsWith("-") || contributorFormat.startsWith("*") ? lineSeparator : ", ";
        return String.join(separator, list);
    }

    private String applyReplacers(JReleaserContext context, Changelog changelog, String text) {
        Map<String, Object> props = context.getModel().props();
        context.getModel().getRelease().getGitService().fillProps(props, context.getModel());
        for (Changelog.Replacer replacer : changelog.getReplacers()) {
            String search = resolveTemplate(replacer.getSearch(), props);
            String replace = resolveTemplate(replacer.getReplace(), props);
            text = text.replaceAll(search, replace);
        }

        return text;
    }

    protected String categorize(Commit commit, Changelog changelog) {
        if (!commit.labels.isEmpty()) {
            for (Changelog.Category category : changelog.getCategories()) {
                if (CollectionUtils.intersects(category.getLabels(), commit.labels)) {
                    return category.getKey();
                }
            }
        }

        return UNCATEGORIZED;
    }

    private void applyLabels(Commit commit, Set<Changelog.Labeler> labelers) {
        for (Changelog.Labeler labeler : labelers) {
            if (isNotBlank(labeler.getTitle())) {
                if (labeler.getTitle().startsWith(REGEX_PREFIX)) {
                    String regex = labeler.getTitle().substring(REGEX_PREFIX.length());
                    if (commit.title.matches(normalizeRegexPattern(regex))) {
                        commit.labels.add(labeler.getLabel());
                    }
                } else {
                    if (commit.title.contains(labeler.getTitle()) || commit.title.matches(toSafeRegexPattern(labeler.getTitle()))) {
                        commit.labels.add(labeler.getLabel());
                    }
                }
            }
            if (isNotBlank(labeler.getBody())) {
                if (labeler.getBody().startsWith(REGEX_PREFIX)) {
                    String regex = labeler.getBody().substring(REGEX_PREFIX.length());
                    if (commit.body.matches(normalizeRegexPattern(regex))) {
                        commit.labels.add(labeler.getLabel());
                    }
                } else {
                    if (commit.body.contains(labeler.getBody()) || commit.body.matches(toSafeRegexPattern(labeler.getBody()))) {
                        commit.labels.add(labeler.getLabel());
                    }
                }
            }
        }
    }

    protected boolean checkLabels(Commit commit, Changelog changelog) {
        if (!changelog.getIncludeLabels().isEmpty()) {
            return CollectionUtils.intersects(changelog.getIncludeLabels(), commit.labels);
        }

        if (!changelog.getExcludeLabels().isEmpty()) {
            return !CollectionUtils.intersects(changelog.getExcludeLabels(), commit.labels);
        }

        return true;
    }

    public static String generate(JReleaserContext context) throws IOException {
        if (!context.getModel().getRelease().getGitService().getChangelog().isEnabled()) {
            return "";
        }

        return new ChangelogGenerator().createChangelog(context);
    }

    protected static class Commit {
        private static final Pattern CO_AUTHORED_BY_PATTERN = Pattern.compile("^[Cc]o-authored-by:\\s+(.*)\\s+<(.*)>.*$");
        private final Set<String> labels = new LinkedHashSet<>();
        private final Set<Author> committers = new LinkedHashSet<>();
        private String fullHash;
        private String shortHash;
        private String title;
        private String body;
        private Author author;
        private int time;

        Map<String, Object> asContext(boolean links, String commitsUrl) {
            Map<String, Object> context = new LinkedHashMap<>();
            if (links) {
                context.put("commitShortHash", passThrough("[" + shortHash + "](" + commitsUrl + "/" + shortHash + ")"));
            } else {
                context.put("commitShortHash", shortHash);
            }
            context.put("commitsUrl", commitsUrl);
            context.put("commitFullHash", fullHash);
            context.put("commitTitle", passThrough(title));
            context.put("commitAuthor", passThrough(author.name));
            context.put("commitBody", passThrough(body));
            return context;
        }

        private void addContributor(String name, String email) {
            if (isNotBlank(name) && isNotBlank(email)) {
                committers.add(new Author(name, email));
            }
        }

        static Commit of(RevCommit rc) {
            Commit c = new Commit();
            c.fullHash = rc.getId().name();
            c.shortHash = rc.getId().abbreviate(7).name();
            c.body = rc.getFullMessage();
            String[] lines = split(c.body);
            c.title = lines[0];
            c.author = new Author(rc.getAuthorIdent().getName(), rc.getAuthorIdent().getEmailAddress());
            c.addContributor(rc.getCommitterIdent().getName(), rc.getCommitterIdent().getEmailAddress());
            c.time = rc.getCommitTime();
            for (String line : lines) {
                Matcher m = CO_AUTHORED_BY_PATTERN.matcher(line);
                if (m.matches()) {
                    c.addContributor(m.group(1), m.group(2));
                }
            }
            return c;
        }

        private static String[] split(String str) {
            // try Windows first
            String sep = "\r\n";
            if (str.contains(sep)) {
                return str.split(sep);
            }

            return str.split("\n");
        }
    }

    private static class Author implements Comparable<Author> {
        protected final String name;
        protected final String email;

        private Author(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        @Override
        public int compareTo(Author that) {
            return name.compareTo(that.name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Author that = (Author) o;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return name + " <" + email + ">";
        }
    }

    private static class Contributor implements Comparable<Contributor> {
        private final String name;
        private final String email;
        private User user;

        private Contributor(Author author) {
            this.name = author.name;
            this.email = author.email;
        }

        private Contributor(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        Map<String, Object> asContext() {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("contributorName", passThrough(name));
            context.put("contributorNameAsLink", passThrough(name));
            context.put("contributorUsername", "");
            context.put("contributorUsernameAsLink", "");
            if (user != null) {
                context.put("contributorNameAsLink", passThrough(user.asLink(name)));
                context.put("contributorUsername", passThrough(user.getUsername()));
                context.put("contributorUsernameAsLink", passThrough(user.asLink("@" + user.getUsername())));
            }
            return context;
        }

        @Override
        public int compareTo(Contributor that) {
            return name.compareTo(that.name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Contributor that = (Contributor) o;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return name + " <" + email + ">";
        }
    }
}
