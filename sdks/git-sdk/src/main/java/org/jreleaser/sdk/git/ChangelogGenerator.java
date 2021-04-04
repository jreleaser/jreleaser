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
import org.jreleaser.model.Github;
import org.jreleaser.model.JReleaserContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.jreleaser.sdk.git.GitSdk.extractTagName;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ChangelogGenerator {
    public static String generate(JReleaserContext context) throws IOException {
        if (!context.getModel().getRelease().getGitService().getChangelog().isEnabled()) {
            return "";
        }

        return createChangelog(context);
    }

    private static String createChangelog(JReleaserContext context) throws IOException {
        GitService gitService = context.getModel().getRelease().getGitService();
        Changelog changelog = gitService.getChangelog();
        String commitsUrl = gitService.getResolvedCommitUrl(context.getModel().getProject());

        String separator = System.lineSeparator();
        if (!Github.NAME.equals(gitService.getServiceName())) {
            separator += System.lineSeparator();
        }
        String commitSeparator = separator;

        try {
            Git git = GitSdk.of(context).open();
            context.getLogger().debug("Resolving commits");
            Iterable<RevCommit> commits = resolveCommits(git, context);

            Comparator<RevCommit> revCommitComparator = Comparator.comparing(RevCommit::getCommitTime).reversed();
            if (changelog.getSort() == Changelog.Sort.ASC) {
                revCommitComparator = Comparator.comparing(RevCommit::getCommitTime);
            }
            context.getLogger().debug("Sorting commits {}", changelog.getSort());

            return "## Changelog" +
                System.lineSeparator() +
                System.lineSeparator() +
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
        String[] input = commit.getFullMessage().trim().split(System.lineSeparator());

        List<String> lines = new ArrayList<>();
        for (int i = 0; i < input.length; i++) {
            if (i == 0) {
                if (changelog.isLinks()) {
                    lines.add("[" + abbreviation + "](" + commitsUrl + "/" + commitHash + ") " + input[i].trim());
                } else {
                    lines.add(abbreviation + " " + input[i].trim());
                }
            } else if (isNotBlank(input[i])) {
                lines.add("         " + input[i].trim());
            }
        }

        return String.join(commitSeparator, lines);
    }

    private static Iterable<RevCommit> resolveCommits(Git git, JReleaserContext context) throws GitAPIException, IOException {
        List<Ref> tags = git.tagList().call();
        tags.sort(new GitSdk.TagComparator().reversed());

        GitService gitService = context.getModel().getRelease().getGitService();
        String effectiveTagName = gitService.getEffectiveTagName(context.getModel().getProject());
        String tagName = gitService.getConfiguredTagName();
        String tagPattern = tagName.replaceAll("\\{\\{.*}}", "\\.\\*");

        context.getLogger().debug("Looking for tags that match '{}', excluding '{}'", tagPattern, effectiveTagName);

        Optional<Ref> tag = tags.stream()
            .filter(ref -> !extractTagName(ref).equals(effectiveTagName))
            .filter(ref -> extractTagName(ref).matches(tagPattern))
            .findFirst();

        ObjectId head = git.getRepository().resolve(Constants.HEAD);
        if (tag.isPresent()) {
            context.getLogger().debug("Found tag {}", extractTagName(tag.get()));
            Ref peeled = git.getRepository().getRefDatabase().peel(tag.get());
            ObjectId fromRef = peeled.getPeeledObjectId() != null ? peeled.getPeeledObjectId() : peeled.getObjectId();
            return git.log().addRange(fromRef, head).call();
        }

        return git.log().add(head).call();
    }
}
