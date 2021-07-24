/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
import org.jreleaser.model.Changelog;
import org.jreleaser.model.GitService;
import org.jreleaser.model.Gitlab;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.releaser.spi.Releaser;
import org.jreleaser.model.releaser.spi.User;
import org.jreleaser.util.CollectionUtils;
import org.jreleaser.util.JavaModuleVersion;
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
import static org.jreleaser.util.StringUtils.stripMargin;
import static org.jreleaser.util.StringUtils.toSafeRegexPattern;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ChangelogGenerator {
    private static final String UNCATEGORIZED = "<<UNCATEGORIZED>>";

    public static String generate(JReleaserContext context, Releaser releaser) throws IOException {
        if (!context.getModel().getRelease().getGitService().getChangelog().isEnabled()) {
            return "";
        }

        return createChangelog(context, releaser);
    }

    private static String createChangelog(JReleaserContext context, Releaser releaser) throws IOException {
        GitService gitService = context.getModel().getRelease().getGitService();
        Changelog changelog = gitService.getChangelog();

        String separator = lineSeparator();
        if (Gitlab.NAME.equals(gitService.getServiceName())) {
            separator += lineSeparator();
        }
        String commitSeparator = separator;

        try {
            Git git = GitSdk.of(context).open();
            context.getLogger().debug("resolving commits");
            Iterable<RevCommit> commits = resolveCommits(git, context);

            Comparator<RevCommit> revCommitComparator = Comparator.comparing(RevCommit::getCommitTime).reversed();
            if (changelog.getSort() == Changelog.Sort.ASC) {
                revCommitComparator = Comparator.comparing(RevCommit::getCommitTime);
            }
            context.getLogger().debug("sorting commits {}", changelog.getSort());

            if (changelog.resolveFormatted(context.getModel().getProject())) {
                return formatChangelog(context, releaser, changelog, commits, revCommitComparator, commitSeparator);
            }

            String commitsUrl = gitService.getResolvedCommitUrl(context.getModel());

            return "## Changelog" +
                lineSeparator() +
                lineSeparator() +
                StreamSupport.stream(commits.spliterator(), false)
                    .sorted(revCommitComparator)
                    .map(commit -> formatCommit(commit, commitsUrl, changelog, commitSeparator))
                    .collect(Collectors.joining(commitSeparator));
        } catch (GitAPIException e) {
            throw new IOException(e);
        }
    }

    private static String formatCommit(RevCommit commit, String commitsUrl, Changelog changelog, String commitSeparator) {
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

    private static Version semverOf(Ref tag, Pattern versionPattern) {
        Matcher matcher = versionPattern.matcher(extractTagName(tag));
        if (matcher.matches()) {
            return Version.of(matcher.group(1));
        }
        return Version.of("0.0.0");
    }

    private static JavaModuleVersion javaModuleVersionOf(Ref tag, Pattern versionPattern) {
        Matcher matcher = versionPattern.matcher(extractTagName(tag));
        if (matcher.matches()) {
            return JavaModuleVersion.of(matcher.group(1));
        }
        return JavaModuleVersion.of("0.0.0");
    }

    private static String versionOf(Ref tag, Pattern versionPattern) {
        Matcher matcher = versionPattern.matcher(extractTagName(tag));
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "0.0.0";
    }

    private static Comparable version(JReleaserContext context, Ref tag, Pattern versionPattern) {
        switch (context.getModel().getProject().getVersionPattern()) {
            case SEMVER:
                return semverOf(tag, versionPattern);
            case JAVA_MODULE:
                return javaModuleVersionOf(tag, versionPattern);
            default:
                return versionOf(tag, versionPattern);
        }
    }

    private static Iterable<RevCommit> resolveCommits(Git git, JReleaserContext context) throws GitAPIException, IOException {
        List<Ref> tags = git.tagList().call();

        GitService gitService = context.getModel().getRelease().getGitService();
        String effectiveTagName = gitService.getEffectiveTagName(context.getModel());
        String tagName = gitService.getConfiguredTagName();
        String tagPattern = tagName.replaceAll("\\{\\{.*}}", "\\.\\*");
        Pattern vp = Pattern.compile(tagName.replaceAll("\\{\\{.*}}", "\\(\\.\\*\\)"));
        if (!tagName.contains("{{")) {
            vp = Pattern.compile("(.*)");
        }
        Pattern versionPattern = vp;

        tags.sort((tag1, tag2) -> {
            Comparable v1 = version(context, tag1, versionPattern);
            Comparable v2 = version(context, tag2, versionPattern);
            return v2.compareTo(v1);
        });

        ObjectId head = git.getRepository().resolve(Constants.HEAD);

        context.getLogger().debug("looking for tag that matches '{}'", effectiveTagName);
        Optional<Ref> tag = tags.stream()
            .filter(ref -> extractTagName(ref).equals(effectiveTagName))
            .findFirst();

        // tag: early-access
        if (context.getModel().getProject().isSnapshot()) {
            String effectiveLabel = context.getModel().getProject().getSnapshot().getEffectiveLabel();
            if (effectiveLabel.equals(effectiveTagName)) {
                if (!tag.isPresent()) {
                    context.getLogger().debug("looking for tags that match '{}', excluding '{}'", tagPattern, effectiveTagName);

                    tag = tags.stream()
                        .filter(ref -> !extractTagName(ref).equals(effectiveTagName))
                        .filter(ref -> extractTagName(ref).matches(tagPattern))
                        .findFirst();
                }

                if (tag.isPresent()) {
                    context.getLogger().debug("found tag {}", extractTagName(tag.get()));
                    ObjectId fromRef = getObjectId(git, tag.get());
                    return git.log().addRange(fromRef, head).call();
                } else {
                    return git.log().add(head).call();
                }
            }
        }

        // tag: latest
        if (!tag.isPresent()) {
            context.getLogger().debug("looking for tags that match '{}', excluding '{}'", tagPattern, effectiveTagName);

            tag = tags.stream()
                .filter(ref -> !extractTagName(ref).equals(effectiveTagName))
                .filter(ref -> extractTagName(ref).matches(tagPattern))
                .findFirst();

            if (tag.isPresent()) {
                context.getLogger().debug("found tag {}", extractTagName(tag.get()));
                ObjectId fromRef = getObjectId(git, tag.get());
                return git.log().addRange(fromRef, head).call();
            }

            return git.log().add(head).call();
        }

        // tag: somewhere in the middle
        context.getLogger().debug("looking for a tag before '{}' that matches '{}'", effectiveTagName, tagPattern);

        Comparable currentVersion = version(context, tag.get(), versionPattern);

        Optional<Ref> previousTag = tags.stream()
            .filter(ref -> extractTagName(ref).matches(tagPattern))
            .filter(ref -> lessThan(version(context, ref, versionPattern), currentVersion))
            .findFirst();

        if (previousTag.isPresent()) {
            context.getLogger().debug("found tag {}", extractTagName(previousTag.get()));
            ObjectId fromRef = getObjectId(git, previousTag.get());
            ObjectId toRef = getObjectId(git, tag.get());
            return git.log().addRange(fromRef, toRef).call();
        }

        ObjectId toRef = getObjectId(git, tag.get());
        return git.log().add(toRef).call();
    }

    private static ObjectId getObjectId(Git git, Ref ref) throws IOException {
        Ref peeled = git.getRepository().getRefDatabase().peel(ref);
        return peeled.getPeeledObjectId() != null ? peeled.getPeeledObjectId() : peeled.getObjectId();
    }

    private static String formatChangelog(JReleaserContext context,
                                          Releaser releaser,
                                          Changelog changelog,
                                          Iterable<RevCommit> commits,
                                          Comparator<RevCommit> revCommitComparator,
                                          String lineSeparator) {
        Set<Contributor> contributors = new LinkedHashSet<>();
        Map<String, List<Commit>> categories = new LinkedHashMap<>();

        StreamSupport.stream(commits.spliterator(), false)
            .sorted(revCommitComparator)
            .map(Commit::of)
            .peek(c -> {
                if (!changelog.getContributors().isEnabled()) return;

                if (!changelog.getHide().containsContributor(c.author)) {
                    contributors.add(new Contributor(c.author, c.authorEmail));
                }
                if (isNotBlank(c.committer) && !changelog.getHide().containsContributor(c.committer)) {
                    contributors.add(new Contributor(c.committer, c.committerEmail));
                }
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
            String categoryTitle = category.getTitle();
            if (!categories.containsKey(categoryTitle) || changelog.getHide().containsCategory(categoryTitle)) continue;

            changes.append("## ")
                .append(categoryTitle)
                .append(lineSeparator);

            changes.append(categories.get(categoryTitle).stream()
                .map(c -> applyTemplate(changelog.getChange(), c.asContext(changelog.isLinks(), commitsUrl)))
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
                .map(c -> applyTemplate(changelog.getChange(), c.asContext(changelog.isLinks(), commitsUrl)))
                .collect(Collectors.joining(lineSeparator)))
                .append(lineSeparator)
                .append(lineSeparator());
        }

        StringBuilder formattedContributors = new StringBuilder();
        if (changelog.getContributors().isEnabled()) {
            formattedContributors.append("## Contributors")
                .append(lineSeparator)
                .append("We'd like to thank the following people for their contributions:")
                .append(lineSeparator)
                .append(formatContributors(context, releaser, changelog, contributors, lineSeparator))
                .append(lineSeparator);
        }

        Map<String, Object> props = context.props();
        props.put(KEY_CHANGELOG_CHANGES, passThrough(changes.toString()));
        props.put(KEY_CHANGELOG_CONTRIBUTORS, passThrough(formattedContributors.toString()));

        return applyReplacers(context, changelog, stripMargin(applyTemplate(changelog.getResolvedContentTemplate(context), props)));
    }

    private static String formatContributors(JReleaserContext context,
                                             Releaser releaser,
                                             Changelog changelog,
                                             Set<Contributor> contributors,
                                             String lineSeparator) {
        List<String> list = new ArrayList<>();
        String format = changelog.getContributors().getFormat();

        Map<String, List<Contributor>> grouped = contributors.stream()
            .peek(contributor -> {
                if (isNotBlank(format) && (format.contains("AsLink") || format.contains("Username"))) {
                    releaser.findUser(contributor.email, contributor.name)
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
                list.add(applyTemplate(contributorFormat, contributor.get().asContext()));
            } else {
                list.add(applyTemplate(contributorFormat, cs.get(0).asContext()));
            }
        });

        String separator = contributorFormat.startsWith("-") || contributorFormat.startsWith("*") ? lineSeparator : ", ";
        return String.join(separator, list);
    }

    private static String applyReplacers(JReleaserContext context, Changelog changelog, String text) {
        Map<String, Object> props = context.getModel().props();
        context.getModel().getRelease().getGitService().fillProps(props, context.getModel());
        for (Changelog.Replacer replacer : changelog.getReplacers()) {
            String search = maybeExpand(props, replacer.getSearch());
            String replace = maybeExpand(props, replacer.getReplace());
            text = text.replaceAll(search, replace);
        }

        return text;
    }

    private static String maybeExpand(Map<String, Object> props, String str) {
        if (str.contains("{{")) {
            return applyTemplate(str, props);
        }
        return str;
    }

    private static String categorize(Commit commit, Changelog changelog) {
        if (!commit.labels.isEmpty()) {
            for (Changelog.Category category : changelog.getCategories()) {
                if (CollectionUtils.intersects(category.getLabels(), commit.labels)) {
                    return category.getTitle();
                }
            }
        }

        return UNCATEGORIZED;
    }

    private static void applyLabels(Commit commit, Set<Changelog.Labeler> labelers) {
        for (Changelog.Labeler labeler : labelers) {
            if (isNotBlank(labeler.getTitle())) {
                if (commit.title.contains(labeler.getTitle()) || commit.title.matches(toSafeRegexPattern(labeler.getTitle()))) {
                    commit.labels.add(labeler.getLabel());
                }
            }
            if (isNotBlank(labeler.getBody())) {
                if (commit.body.contains(labeler.getBody()) || commit.body.matches(toSafeRegexPattern(labeler.getBody()))) {
                    commit.labels.add(labeler.getLabel());
                }
            }
        }
    }

    private static boolean checkLabels(Commit commit, Changelog changelog) {
        if (!changelog.getIncludeLabels().isEmpty()) {
            return CollectionUtils.intersects(changelog.getIncludeLabels(), commit.labels);
        }

        if (!changelog.getExcludeLabels().isEmpty()) {
            return !CollectionUtils.intersects(changelog.getExcludeLabels(), commit.labels);
        }

        return true;
    }

    private static class Commit {
        private final Set<String> labels = new LinkedHashSet<>();
        private String fullHash;
        private String shortHash;
        private String title;
        private String body;
        private String author;
        private String committer;
        private String authorEmail;
        private String committerEmail;
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
            context.put("commitAuthor", passThrough(author));
            return context;
        }

        static Commit of(RevCommit rc) {
            Commit c = new Commit();
            c.fullHash = rc.getId().name();
            c.shortHash = rc.getId().abbreviate(7).name();
            c.body = rc.getFullMessage();
            c.title = c.body.split(lineSeparator())[0];
            c.author = rc.getAuthorIdent().getName();
            c.committer = rc.getCommitterIdent().getName();
            c.authorEmail = rc.getAuthorIdent().getEmailAddress();
            c.committerEmail = rc.getCommitterIdent().getEmailAddress();
            c.time = rc.getCommitTime();
            return c;
        }
    }

    private static class Contributor implements Comparable<Contributor> {
        private final String name;
        private final String email;
        private User user;

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
            return email.compareTo(that.email);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Contributor that = (Contributor) o;
            return email.equals(that.email);
        }

        @Override
        public int hashCode() {
            return Objects.hash(email);
        }

        @Override
        public String toString() {
            return name + " <" + email + ">";
        }
    }
}
