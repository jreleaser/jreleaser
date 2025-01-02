/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
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
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.release.Changelog;
import org.jreleaser.model.internal.util.VersionUtils;
import org.jreleaser.model.spi.release.User;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.util.CollectionUtils;
import org.jreleaser.util.StringUtils;
import org.jreleaser.version.Version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static java.lang.System.lineSeparator;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Constants.KEY_CATEGORIZE_SCOPES;
import static org.jreleaser.model.Constants.KEY_CHANGELOG_CHANGES;
import static org.jreleaser.model.Constants.KEY_CHANGELOG_CONTRIBUTORS;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.mustache.MustacheUtils.passThrough;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.sdk.git.ChangelogProvider.extractIssues;
import static org.jreleaser.sdk.git.ChangelogProvider.storeIssues;
import static org.jreleaser.sdk.git.GitSdk.extractTagName;
import static org.jreleaser.util.ComparatorUtils.lessThan;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.isTrue;
import static org.jreleaser.util.StringUtils.normalizeRegexPattern;
import static org.jreleaser.util.StringUtils.stripMargin;
import static org.jreleaser.util.StringUtils.toSafeRegexPattern;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ChangelogGenerator {
    private static final String UNCATEGORIZED = "<<UNCATEGORIZED>>";
    private static final String REGEX_PREFIX = "regex:";

    protected String createChangelog(JReleaserContext context) throws IOException {
        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();
        Changelog changelog = releaser.getChangelog();

        String separator = lineSeparator();
        if (org.jreleaser.model.api.release.GitlabReleaser.TYPE.equals(releaser.getServiceName())) {
            separator += lineSeparator();
        }
        String commitSeparator = separator;

        try {
            Git git = GitSdk.of(context).open();
            context.getLogger().debug(RB.$("changelog.generator.resolve.commits"));
            Iterable<RevCommit> commits = resolveCommits(git, context);

            Comparator<RevCommit> revCommitComparator = Comparator.comparing(RevCommit::getCommitTime).reversed();
            if (changelog.getSort() == org.jreleaser.model.Changelog.Sort.ASC) {
                revCommitComparator = Comparator.comparing(RevCommit::getCommitTime);
            }
            context.getLogger().debug(RB.$("changelog.generator.sort.commits", changelog.getSort()));

            // collect
            List<RevCommit> commitList = StreamSupport.stream(commits.spliterator(), false)
                .filter(c -> !changelog.isSkipMergeCommits() || c.getParentCount() <= 1)
                .collect(toList());

            if (context.getModel().getRelease().getReleaser().getIssues().isEnabled()) {
                // extract issues
                String rawContent = commitList.stream()
                    .map(RevCommit::getFullMessage)
                    .collect(joining(lineSeparator()));

                context.getLogger().info(RB.$("issues.generator.extract"));
                Set<Integer> issues = extractIssues(context, rawContent);
                storeIssues(context, issues);
            }

            if (changelog.resolveFormatted(context.getModel().getProject())) {
                return formatChangelog(context, changelog, commitList, revCommitComparator, commitSeparator);
            }

            String commitsUrl = releaser.getResolvedCommitUrl(context.getModel());

            return "## Changelog" +
                lineSeparator() +
                lineSeparator() +
                commitList.stream()
                    .sorted(revCommitComparator)
                    .map(commit -> formatCommit(commit, commitsUrl, changelog, commitSeparator))
                    .collect(joining(commitSeparator));
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

    public Tags resolveTags(Git git, JReleaserContext context) throws GitAPIException {
        GitSdk gitSdk = GitSdk.of(context);
        if (gitSdk.isShallow()) {
            context.getLogger().warn(RB.$("changelog.shallow.warning"));
        }

        List<Ref> tags = git.tagList().call();

        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();
        String effectiveTagName = releaser.getEffectiveTagName(context.getModel());
        String tagName = releaser.getTagName();
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
        String previousTagName = releaser.getResolvedPreviousTagName(context.getModel());
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
                } else {
                    previousTag = tags.stream()
                        .filter(ref -> extractTagName(ref).matches(tagPattern))
                        .filter(ref -> !defaultVersion.equals(version(context, ref, versionPattern, true)))
                        .filter(ref -> lessThan(version(context, ref, versionPattern, true), currentVersion))
                        .findFirst();


                    if (previousTag.isPresent()) {
                        RevCommit earlyAccessCommit = gitSdk.resolveSingleCommit(git, tag.get());
                        RevCommit previousTagCommit = gitSdk.resolveSingleCommit(git, previousTag.get());

                        if (previousTagCommit.getCommitTime() > earlyAccessCommit.getCommitTime()) {
                            tag = previousTag;
                        }
                    }
                }

                if (tag.isPresent()) {
                    context.getLogger().debug(RB.$("changelog.generator.tag.found"), extractTagName(tag.get()));
                    context.getModel().getRelease().getReleaser().setPreviousTagName(extractTagName(tag.get()));
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
                context.getModel().getRelease().getReleaser().setPreviousTagName(extractTagName(tag.get()));
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
            context.getModel().getRelease().getReleaser().setPreviousTagName(extractTagName(previousTag.get()));
            return Tags.of(tag.get(), previousTag.get());
        }

        return Tags.current(tag.get());
    }

    protected Iterable<RevCommit> resolveCommits(Git git, JReleaserContext context) throws GitAPIException, IOException {
        Tags tags = resolveTags(git, context);
        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();

        ObjectId head = git.getRepository().resolve(Constants.HEAD);

        // tag: early-access
        if (context.getModel().getProject().isSnapshot()) {
            Project.Snapshot snapshot = context.getModel().getProject().getSnapshot();
            String effectiveLabel = snapshot.getEffectiveLabel();
            if (effectiveLabel.equals(releaser.getEffectiveTagName(context.getModel()))) {
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
        return null != peeled.getPeeledObjectId() ? peeled.getPeeledObjectId() : peeled.getObjectId();
    }

    protected String formatChangelog(JReleaserContext context,
                                     Changelog changelog,
                                     List<RevCommit> commits,
                                     Comparator<RevCommit> revCommitComparator,
                                     String lineSeparator) {
        Set<Contributor> contributors = new TreeSet<>();
        Map<String, List<Commit>> categories = new LinkedHashMap<>();

        commits.stream()
            .sorted(revCommitComparator)
            .map(rc -> "conventional-commits".equals(changelog.getPreset()) ? ConventionalCommit.of(rc) : Commit.of(rc))
            .map(c -> c.extractIssues(context))
            .peek(c -> {
                applyLabels(c, changelog.getLabelers());

                if (!changelog.getContributors().isEnabled()) return;

                if (!changelog.getHide().containsContributor(c.author.name) &&
                    !changelog.getHide().containsContributor(c.author.email)) {
                    contributors.add(new Contributor(c.author));
                }
                c.committers.stream()
                    .filter(author -> !changelog.getHide().containsContributor(author.name))
                    .filter(author -> !changelog.getHide().containsContributor(author.email))
                    .forEach(author -> contributors.add(new Contributor(author)));
            })
            .filter(c -> checkLabels(c, changelog))
            .forEach(commit -> categories
                .computeIfAbsent(categorize(commit, changelog), k -> new ArrayList<>())
                .add(commit));

        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();
        String commitsUrl = releaser.getResolvedCommitUrl(context.getModel());
        String issueTracker = releaser.getResolvedIssueTrackerUrl(context.getModel(), true);

        TemplateContext props = context.fullProps();
        props.setAll(changelog.resolvedExtraProperties());
        StringBuilder changes = new StringBuilder();
        for (Changelog.Category category : changelog.getCategories()) {
            String categoryKey = category.getKey();
            if (!categories.containsKey(categoryKey) || changelog.getHide().containsCategory(categoryKey)) continue;

            props.set("categoryTitle", category.getTitle());
            changes.append(applyTemplate(changelog.getCategoryTitleFormat(), props))
                .append(lineSeparator);

            final String categoryFormat = resolveCommitFormat(changelog, category);

            if (isConventionalCommits(changelog) && isCategorizeScopes(changelog)) {
                Map<String, List<Commit>> scopes = categories.get(categoryKey).stream()
                    .collect(groupingBy(commit -> {
                        if (commit instanceof ConventionalCommit) {
                            ConventionalCommit cc = (ConventionalCommit) commit;
                            return isNotBlank(cc.ccScope) ? cc.ccScope : UNCATEGORIZED;
                        }
                        return UNCATEGORIZED;
                    }));

                scopes.keySet().stream().sorted()
                    .filter(scope -> !UNCATEGORIZED.equals(scope))
                    .forEach(scope -> changes.append("**")
                        .append(scope)
                        .append("**")
                        .append(lineSeparator)
                        .append(scopes.get(scope).stream()
                            .map(c -> {
                                ((ConventionalCommit) c).ccScope = ""; // clear scope
                                return resolveTemplate(categoryFormat, c.asContext(changelog.isLinks(), commitsUrl, issueTracker));
                            })
                            .collect(joining(lineSeparator)))
                        .append(lineSeparator)
                        .append(lineSeparator()));

                if (scopes.containsKey(UNCATEGORIZED)) {
                    // add unscoped header only if there are more than one uncategorized commits
                    if (scopes.size() > 1) changes.append("**unscoped**");
                    changes.append(lineSeparator).append(scopes.get(UNCATEGORIZED).stream()
                            .map(c -> resolveTemplate(categoryFormat, c.asContext(changelog.isLinks(), commitsUrl, issueTracker)))
                            .collect(joining(lineSeparator)))
                        .append(lineSeparator)
                        .append(lineSeparator());
                }
            } else {
                changes.append(categories.get(categoryKey).stream()
                        .map(c -> resolveTemplate(categoryFormat, c.asContext(changelog.isLinks(), commitsUrl, issueTracker)))
                        .collect(joining(lineSeparator)))
                    .append(lineSeparator)
                    .append(lineSeparator());
            }
        }

        if (!changelog.getHide().isUncategorized() && categories.containsKey(UNCATEGORIZED)) {
            if (changes.length() > 0) {
                changes.append("---")
                    .append(lineSeparator);
            }

            changes.append(categories.get(UNCATEGORIZED).stream()
                    .map(c -> resolveTemplate(changelog.getFormat(), c.asContext(changelog.isLinks(), commitsUrl, issueTracker)))
                    .collect(joining(lineSeparator)))
                .append(lineSeparator)
                .append(lineSeparator());
        }

        StringBuilder formattedContributors = new StringBuilder();
        if (changelog.getContributors().isEnabled() && !contributors.isEmpty()) {
            formattedContributors.append(applyTemplate(changelog.getContributorsTitleFormat(), props))
                .append(lineSeparator)
                .append("We'd like to thank the following people for their contributions:")
                .append(lineSeparator)
                .append(formatContributors(context, changelog, contributors, lineSeparator))
                .append(lineSeparator);
        }

        props.set(KEY_CHANGELOG_CHANGES, passThrough(changes.toString()));
        props.set(KEY_CHANGELOG_CONTRIBUTORS, passThrough(formattedContributors.toString()));
        context.getChangelog().setFormattedChanges(changes.toString());
        context.getChangelog().setFormattedContributors(formattedContributors.toString());

        return applyReplacers(context, changelog, stripMargin(applyTemplate(changelog.getResolvedContentTemplate(context), props)));
    }

    private boolean isConventionalCommits(Changelog changelog) {
        return isNotBlank(changelog.getPreset()) &&
            "conventional-commits".equals(changelog.getPreset().toLowerCase(Locale.ENGLISH).trim());
    }

    private boolean isCategorizeScopes(Changelog changelog) {
        return isTrue(changelog.getExtraProperties().get(KEY_CATEGORIZE_SCOPES));
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
                if (!context.isDryrun() && isNotBlank(format) && (format.contains("AsLink") || format.contains("Username"))) {
                    context.getReleaser().findUser(contributor.email, contributor.name)
                        .ifPresent(contributor::setUser);
                }
            })
            .collect(groupingBy(Contributor::getName));

        String contributorFormat = isNotBlank(format) ? format : "{{contributorName}}";

        grouped.keySet().stream().sorted().forEach(name -> {
            List<Contributor> cs = grouped.get(name);
            Optional<Contributor> contributor = cs.stream()
                .filter(c -> null != c.getUser())
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
        TemplateContext props = context.getModel().props();
        context.getModel().getRelease().getReleaser().fillProps(props, context.getModel());
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
            String label = labeler.getLabel();

            String title = labeler.getTitle();
            if (isNotBlank(title)) {
                if (title.startsWith(REGEX_PREFIX)) {
                    String regex = title.substring(REGEX_PREFIX.length());
                    if (commit.title.matches(normalizeRegexPattern(regex))) {
                        commit.labels.add(label);
                    }
                } else {
                    if (matches(commit.title, title)) {
                        commit.labels.add(label);
                    }
                }
            }

            String body = labeler.getBody();
            if (isNotBlank(body)) {
                if (body.startsWith(REGEX_PREFIX)) {
                    String regex = body.substring(REGEX_PREFIX.length());
                    if (commit.body.matches(normalizeRegexPattern(regex))) {
                        commit.labels.add(label);
                    }
                } else {
                    if (matches(commit.body, body)) {
                        commit.labels.add(label);
                    }
                }
            }

            String contributor = labeler.getContributor();
            if (isNotBlank(contributor)) {
                if (contributor.startsWith(REGEX_PREFIX)) {
                    String regex = contributor.substring(REGEX_PREFIX.length());
                    if (commit.author.name.matches(normalizeRegexPattern(regex)) ||
                        commit.author.email.matches(normalizeRegexPattern(regex))) {
                        commit.labels.add(label);
                    }
                    for (Author committer : commit.committers) {
                        if (committer.name.matches(normalizeRegexPattern(regex)) ||
                            committer.email.matches(normalizeRegexPattern(regex))) {
                            commit.labels.add(label);
                        }
                    }
                } else {
                    if (matches(commit.author.name, contributor) ||
                        matches(commit.author.email, contributor)) {
                        commit.labels.add(label);
                    }
                    for (Author committer : commit.committers) {
                        if (matches(committer.name, contributor) ||
                            matches(committer.email, contributor)) {
                            commit.labels.add(label);
                        }
                    }
                }
            }
        }
    }

    private boolean matches(String haystack, String needle) {
        return haystack.contains(needle) || haystack.matches(toSafeRegexPattern(needle));
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
        if (!context.getModel().getRelease().getReleaser().getChangelog().isEnabled()) {
            return "";
        }

        return new ChangelogGenerator().createChangelog(context);
    }

    public static class Tags {
        private final Ref current;
        private final Ref previous;

        private Tags(Ref current, Ref previous) {
            this.current = current;
            this.previous = previous;
        }

        public Optional<Ref> getCurrent() {
            return Optional.ofNullable(current);
        }

        public Optional<Ref> getPrevious() {
            return Optional.ofNullable(previous);
        }

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
    }

    protected static class Commit {
        private static final Pattern CO_AUTHORED_BY_PATTERN = Pattern.compile("^[Cc]o-authored-by:\\s+(.*)\\s+<(.*)>.*$");
        private final Set<String> labels = new LinkedHashSet<>();
        private final Set<Author> committers = new LinkedHashSet<>();
        private final Set<Integer> issues = new TreeSet<>();
        private final String fullHash;
        private final String shortHash;
        private final String title;
        private final Author author;
        protected String body;

        protected Commit(RevCommit rc) {
            fullHash = rc.getId().name();
            shortHash = rc.getId().abbreviate(7).name();
            body = rc.getFullMessage().trim();
            String[] lines = split(body);
            if (lines.length > 0) {
                title = lines[0].trim();
            } else {
                title = "";
            }
            author = new Author(rc.getAuthorIdent().getName(), rc.getAuthorIdent().getEmailAddress());
            addContributor(rc.getCommitterIdent().getName(), rc.getCommitterIdent().getEmailAddress());
            for (String line : lines) {
                Matcher m = CO_AUTHORED_BY_PATTERN.matcher(line);
                if (m.matches()) {
                    addContributor(m.group(1), m.group(2));
                }
            }
        }

        TemplateContext asContext(boolean links, String commitsUrl, String issueTrackerUrl) {
            TemplateContext context = new TemplateContext();
            if (links) {
                context.set("commitShortHash", passThrough("[" + shortHash + "](" + commitsUrl + "/" + shortHash + ")"));
            } else {
                context.set("commitShortHash", shortHash);
            }
            context.set("commitsUrl", commitsUrl);
            context.set("commitFullHash", fullHash);
            context.set("commitTitle", passThrough(title));
            context.set("commitAuthor", passThrough(author.name));
            context.set("commitBody", passThrough(body));
            context.set("commitHasIssues", !issues.isEmpty());
            context.set("commitIssues", issues.stream().map(i -> {
                String issue = links ? passThrough("[#" + i + "](" + issueTrackerUrl + i + ")") : "#" + i;
                return singletonMap("issue", issue);
            }).collect(toList()));
            return context;
        }

        public Set<Integer> getIssues() {
            return unmodifiableSet(issues);
        }

        private void addContributor(String name, String email) {
            if (isNotBlank(name) && isNotBlank(email)) {
                committers.add(new Author(name, email));
            }
        }

        public Commit extractIssues(JReleaserContext context) {
            issues.addAll(ChangelogProvider.extractIssues(context, body));
            return this;
        }

        static Commit of(RevCommit rc) {
            return new Commit(rc);
        }

        protected static String[] split(String str) {
            // Any Unicode linebreak sequence
            return str.split("\\R");
        }
    }

    static class ConventionalCommit extends Commit {
        private static final Pattern FIRST_LINE_PATTERN =
            Pattern.compile("^(?<type>\\w+)(?:\\((?<scope>[^)\\n]+)\\))?(?<bang>!)?: (?<description>.*$)");
        private static final Pattern BREAKING_CHANGE_PATTERN = Pattern.compile("^BREAKING[ \\-]CHANGE:\\s+(?<content>[\\w\\W]+)", Pattern.MULTILINE);
        private static final Pattern TRAILER_PATTERN = Pattern.compile("(?<token>^\\w+(?:-\\w+)*)(?:: | #)(?<value>.*$)");

        private final List<Trailer> trailers = new ArrayList<>();
        private boolean isConventional = true;
        private boolean ccIsBreakingChange;
        private String ccType = "";
        private String ccScope = "";
        private String ccDescription = "";
        private String ccBody = "";
        private String ccBreakingChangeContent = "";

        private ConventionalCommit(RevCommit rc) {
            super(rc);
            List<String> lines = new ArrayList<>(Arrays.asList(split(body)));
            Matcher matcherFirstLine = FIRST_LINE_PATTERN.matcher(lines.get(0).trim());
            if (matcherFirstLine.matches()) {
                lines.remove(0); // consumed first line
                if (null != matcherFirstLine.group("bang") && !matcherFirstLine.group("bang").isEmpty()) {
                    ccIsBreakingChange = true;
                }
                ccType = matcherFirstLine.group("type");
                ccScope = null == matcherFirstLine.group("scope") ? "" : matcherFirstLine.group("scope");
                ccDescription = matcherFirstLine.group("description");
            } else {
                isConventional = false;
                return;
            }

            // drop any empty lines at the beginning
            while (!lines.isEmpty() && isBlank(lines.get(0))) {
                lines.remove(0);
            }

            // try to match trailers from the end
            while (!lines.isEmpty()) {
                Matcher matcherTrailer = TRAILER_PATTERN.matcher(lines.get(lines.size() - 1).trim());
                if (matcherTrailer.matches()) {
                    String token = matcherTrailer.group("token");
                    if ("BREAKING-CHANGE".equals(token)) break;
                    trailers.add(new Trailer(token, matcherTrailer.group("value")));
                    lines.remove(lines.size() - 1); // consume last line
                } else {
                    break;
                }
            }

            // drop any empty lines at the end
            while (!lines.isEmpty() && isBlank(lines.get(lines.size() - 1))) {
                lines.remove(lines.size() - 1);
            }

            Matcher matcherBC = BREAKING_CHANGE_PATTERN.matcher(String.join("\n", lines));
            if (matcherBC.find()) {
                ccIsBreakingChange = true;
                ccBreakingChangeContent = matcherBC.group("content");
                // consume the breaking change
                OptionalInt match = IntStream.range(0, lines.size())
                    .filter(i -> BREAKING_CHANGE_PATTERN.matcher(lines.get(i).trim()).find())
                    .findFirst();
                if (match.isPresent() && lines.size() > match.getAsInt()) {
                    lines.subList(match.getAsInt(), lines.size()).clear();
                }
            }

            // the rest is the body
            ccBody = String.join("\n", lines);
        }

        @Override
        TemplateContext asContext(boolean links, String commitsUrl, String issueTrackerUrl) {
            TemplateContext context = super.asContext(links, commitsUrl, issueTrackerUrl);
            context.set("commitIsConventional", isConventional);
            context.set("conventionalCommitBreakingChangeContent", passThrough(ccBreakingChangeContent));
            context.set("conventionalCommitIsBreakingChange", ccIsBreakingChange);
            context.set("conventionalCommitType", ccType);
            context.set("conventionalCommitScope", ccScope);
            context.set("conventionalCommitDescription", passThrough(ccDescription));
            context.set("conventionalCommitBody", passThrough(ccBody));
            context.set("conventionalCommitTrailers", unmodifiableList(trailers));
            return context;
        }

        public List<Trailer> getTrailers() {
            return trailers;
        }

        public static Commit of(RevCommit rc) {
            ConventionalCommit c = new ConventionalCommit(rc);
            if (c.isConventional) {
                return c;
            } else {
                // not ideal to reparse the commit, but that way we return a Commit instead of a ConventionalCommit
                return Commit.of(rc);
            }
        }

        static class Trailer {
            private final String token;
            private final String value;

            public Trailer(String token, String value) {
                this.token = token;
                this.value = value;
            }

            public String getToken() {
                return token;
            }

            public String getValue() {
                return value;
            }

            @Override
            public String toString() {
                return passThrough(token + ": " + value);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Trailer)) return false;
                Trailer trailer = (Trailer) o;
                return token.equals(trailer.token) && value.equals(trailer.value);
            }

            @Override
            public int hashCode() {
                return Objects.hash(token, value);
            }
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
            if (null == o || getClass() != o.getClass()) return false;
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

        TemplateContext asContext() {
            TemplateContext context = new TemplateContext();
            context.set("contributorName", passThrough(name));
            context.set("contributorNameAsLink", passThrough(name));
            context.set("contributorUsername", "");
            context.set("contributorUsernameAsLink", "");
            if (null != user) {
                context.set("contributorNameAsLink", passThrough(user.asLink(name)));
                context.set("contributorUsername", passThrough(user.getUsername()));
                context.set("contributorUsernameAsLink", passThrough(user.asLink("@" + user.getUsername())));
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
            if (null == o || getClass() != o.getClass()) return false;
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
