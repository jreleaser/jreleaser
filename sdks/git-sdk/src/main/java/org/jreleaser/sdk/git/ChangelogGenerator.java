/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
import org.jreleaser.util.CollectionUtils;
import org.jreleaser.util.Version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.lang.System.lineSeparator;
import static org.jreleaser.model.GitService.TAG_EARLY_ACCESS;
import static org.jreleaser.sdk.git.GitSdk.extractTagName;
import static org.jreleaser.util.Constants.KEY_CHANGELOG_CHANGES;
import static org.jreleaser.util.Constants.KEY_CHANGELOG_CONTRIBUTORS;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.MustacheUtils.passThrough;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.stripMargin;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ChangelogGenerator {
    private static final String UNCATEGORIZED = "<<UNCATEGORIZED>>";

    public static String generate(JReleaserContext context) throws IOException {
        if (!context.getModel().getRelease().getGitService().getChangelog().isEnabled()) {
            return "";
        }

        return createChangelog(context);
    }

    private static String createChangelog(JReleaserContext context) throws IOException {
        GitService gitService = context.getModel().getRelease().getGitService();
        Changelog changelog = gitService.getChangelog();
        String commitsUrl = gitService.getResolvedCommitUrl(context.getModel());

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
                return formatChangelog(context, changelog, commits, revCommitComparator, commitSeparator);
            }

            return "# Changelog" +
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

    private static Version versionOf(Ref tag, Pattern versionPattern) {
        Matcher matcher = versionPattern.matcher(extractTagName(tag));
        if (matcher.matches()) {
            return Version.of(matcher.group(1));
        }
        return Version.of("0.0.0");
    }

    private static Iterable<RevCommit> resolveCommits(Git git, JReleaserContext context) throws GitAPIException, IOException {
        List<Ref> tags = git.tagList().call();

        GitService gitService = context.getModel().getRelease().getGitService();
        String effectiveTagName = gitService.getEffectiveTagName(context.getModel());
        String tagName = gitService.getConfiguredTagName();
        String tagPattern = tagName.replaceAll("\\{\\{.*}}", "\\.\\*");
        Pattern versionPattern = Pattern.compile(tagName.replaceAll("\\{\\{.*}}", "\\(\\.\\*\\)"));

        tags.sort((tag1, tag2) -> {
            Version v1 = versionOf(tag1, versionPattern);
            Version v2 = versionOf(tag2, versionPattern);
            return v2.compareTo(v1);
        });

        Optional<Ref> tag = Optional.empty();
        if (TAG_EARLY_ACCESS.equals(effectiveTagName)) {
            context.getLogger().debug("looking for tags that match '{}'", effectiveTagName);
            tag = tags.stream()
                .filter(ref -> extractTagName(ref).equals(effectiveTagName))
                .findFirst();
        }

        if (!tag.isPresent()) {
            context.getLogger().debug("looking for tags that match '{}', excluding '{}'", tagPattern, effectiveTagName);

            tag = tags.stream()
                .filter(ref -> !extractTagName(ref).equals(effectiveTagName))
                .filter(ref -> extractTagName(ref).matches(tagPattern))
                .findFirst();
        }

        ObjectId head = git.getRepository().resolve(Constants.HEAD);

        if (tag.isPresent()) {
            context.getLogger().debug("found tag {}", extractTagName(tag.get()));
            Ref peeled = git.getRepository().getRefDatabase().peel(tag.get());
            ObjectId fromRef = peeled.getPeeledObjectId() != null ? peeled.getPeeledObjectId() : peeled.getObjectId();
            return git.log().addRange(fromRef, head).call();
        }

        return git.log().add(head).call();
    }

    private static String formatChangelog(JReleaserContext context, Changelog changelog,
                                          Iterable<RevCommit> commits,
                                          Comparator<RevCommit> revCommitComparator,
                                          String lineSeparator) {
        Set<String> contributorNames = new LinkedHashSet<>();
        Map<String, List<Commit>> categories = new LinkedHashMap<>();

        StreamSupport.stream(commits.spliterator(), false)
            .sorted(revCommitComparator)
            .map(Commit::of)
            .peek(c -> {
                contributorNames.add(c.author);
                if (isNotBlank(c.committer)) contributorNames.add(c.committer);
            })
            .peek(c -> applyLabels(c, changelog.getLabelers()))
            .filter(c -> checkLabels(c, changelog))
            .forEach(commit -> categories
                .computeIfAbsent(categorize(commit, changelog), k -> new ArrayList<>())
                .add(commit));

        StringBuilder changes = new StringBuilder();
        for (Changelog.Category category : changelog.getCategories()) {
            String categoryTitle = category.getTitle();
            if (!categories.containsKey(categoryTitle)) continue;

            changes.append("## ")
                .append(categoryTitle)
                .append(lineSeparator);

            changes.append(categories.get(categoryTitle).stream()
                .map(c -> applyTemplate(changelog.getChange(), c.asContext()))
                .collect(Collectors.joining(lineSeparator)))
                .append(lineSeparator)
                .append(lineSeparator());
        }

        if (categories.containsKey(UNCATEGORIZED)) {
            changes.append("---")
                .append(lineSeparator);

            changes.append(categories.get(UNCATEGORIZED).stream()
                .map(c -> applyTemplate(changelog.getChange(), c.asContext()))
                .collect(Collectors.joining(lineSeparator)))
                .append(lineSeparator)
                .append(lineSeparator());
        }

        StringBuilder contributors = new StringBuilder("## Contributors")
            .append(lineSeparator)
            .append(String.join(", ", contributorNames))
            .append(lineSeparator);

        Map<String, Object> props = context.props();
        props.put(KEY_CHANGELOG_CHANGES, passThrough(changes.toString()));
        props.put(KEY_CHANGELOG_CONTRIBUTORS, passThrough(contributors.toString()));

        return applyReplacers(changelog, stripMargin(applyTemplate(changelog.getResolvedContentTemplate(context), props)));
    }

    private static String applyReplacers(Changelog changelog, String text) {
        for (Changelog.Replacer replacer : changelog.getReplacers()) {
            text = text.replaceAll(replacer.getSearch(), replacer.getReplace());
        }

        return text;
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
                if (commit.title.contains(labeler.getTitle()) || commit.title.matches(labeler.getTitle())) {
                    commit.labels.add(labeler.getLabel());
                }
            }
            if (isNotBlank(labeler.getBody())) {
                if (commit.body.contains(labeler.getBody()) || commit.body.matches(labeler.getBody())) {
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
        private int time;

        Map<String, Object> asContext() {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("commitFullHash", fullHash);
            context.put("commitShortHash", shortHash);
            context.put("commitTitle", passThrough(title));
            context.put("commitAuthor", passThrough(author));
            return context;
        }

        static Commit of(RevCommit rc) {
            Commit c = new Commit();
            c.fullHash = rc.getId().name();
            c.shortHash = rc.getId().abbreviate(7).name();
            c.title = rc.getShortMessage();
            c.body = rc.getFullMessage();
            c.author = rc.getAuthorIdent().getName();
            c.committer = rc.getCommitterIdent().getName();
            c.time = rc.getCommitTime();
            return c;
        }
    }
}
