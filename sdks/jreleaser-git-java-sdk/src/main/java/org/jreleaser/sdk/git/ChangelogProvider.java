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

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.release.Changelog;
import org.jreleaser.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.stream.Collectors.joining;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class ChangelogProvider {
    public static final String ISSUES_FILE = "issues.txt";
    public static final String CHANGELOG_FILE = "CHANGELOG.md";

    private ChangelogProvider() {
        // noop
    }

    public static String getChangelog(JReleaserContext context) throws IOException {
        Changelog changelog = context.getModel().getRelease().getReleaser().getChangelog();

        if (!changelog.isEnabled()) {
            context.getLogger().info(RB.$("changelog.disabled"));
            return "";
        }

        String content = resolveChangelog(context);
        return storeChangelog(context, content);
    }

    public static String storeChangelog(JReleaserContext context, String content) throws IOException {
        Path changelogFile = getReleaseFilePath(context, CHANGELOG_FILE);

        context.getLogger().info(RB.$("changelog.generator.store"), context.getBasedir().relativize(changelogFile));
        context.getLogger().debug(content);

        Files.createDirectories(changelogFile.getParent());
        Files.write(changelogFile, content.getBytes(UTF_8), CREATE, WRITE, TRUNCATE_EXISTING);

        return content;
    }

    public static List<String> getIssues(JReleaserContext context) throws IOException {
        Path issuesFile = getReleaseFilePath(context, ISSUES_FILE);

        if (Files.exists(issuesFile)) {
            return Files.readAllLines(issuesFile);
        }

        return Collections.emptyList();
    }

    private static String resolveChangelog(JReleaserContext context) throws IOException {
        Path issuesFile = getReleaseFilePath(context, ISSUES_FILE);

        // clean up first
        if (Files.exists(issuesFile)) Files.delete(issuesFile);

        Changelog changelog = context.getModel().getRelease().getReleaser().getChangelog();
        String externalChangelog = changelog.getExternal();

        if (isNotBlank(externalChangelog)) {
            Path externalChangelogPath = context.getBasedir().resolve(Paths.get(externalChangelog));
            File externalChangelogFile = externalChangelogPath.toFile();
            if (!externalChangelogFile.exists()) {
                throw new IllegalStateException(RB.$("ERROR_changelog_not_exist", context.getBasedir().relativize(externalChangelogPath)));
            }

            context.getLogger().info(RB.$("changelog.generator.read"), context.getBasedir().relativize(externalChangelogPath));
            String content = new String(Files.readAllBytes(externalChangelogPath), UTF_8);

            if (context.getModel().getRelease().getReleaser().getIssues().isEnabled()) {
                context.getLogger().info(RB.$("issues.generator.extract"));
                Set<Integer> issues = extractIssues(context, content);
                storeIssues(context, issues);
            }

            return content;
        }

        context.getLogger().info(RB.$("changelog.generator.generate"));
        return ChangelogGenerator.generate(context);
    }

    public static Set<Integer> extractIssues(JReleaserContext context, String content) {
        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();
        String issueTracker = releaser.getResolvedIssueTrackerUrl(context.getModel(), true);

        String p1 = StringUtils.escapeRegexChars(issueTracker);
        String p2 = StringUtils.escapeRegexChars(releaser.getCanonicalRepoName());
        String p3 = StringUtils.escapeRegexChars(releaser.getName());
        String regex = "(?:" + p2 + "|(?<!/)" + p3 + ")#(?<repo>\\d+)|[^a-zA-Z0-9]#(?<hash>\\d+)";
        regex += isNotBlank(p1) ? "|" + p1 + "(?<tracker>\\d+)" : "";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        Set<Integer> issues = new TreeSet<>();
        while (matcher.find()) {
            if (isNotBlank(matcher.group("repo"))) issues.add(Integer.valueOf(matcher.group("repo")));
            if (isNotBlank(matcher.group("hash"))) issues.add(Integer.valueOf(matcher.group("hash")));
            if (isNotBlank(p1) && isNotBlank(matcher.group("tracker")))
                issues.add(Integer.valueOf(matcher.group("tracker")));
        }

        return issues;
    }

    public static void storeIssues(JReleaserContext context, Set<Integer> issues) throws IOException {
        if (issues.isEmpty()) return;

        Path issuesFile = getReleaseFilePath(context, ISSUES_FILE);
        String content = issues.stream().map(String::valueOf).collect(joining(lineSeparator()));
        context.getLogger().info(RB.$("issues.generator.store"), context.getBasedir().relativize(issuesFile));
        context.getLogger().debug(content);

        Files.createDirectories(issuesFile.getParent());
        Files.write(issuesFile, content.getBytes(UTF_8), CREATE, WRITE, TRUNCATE_EXISTING);
    }

    public static Path getReleaseFilePath(JReleaserContext context, String fileName) {
        return context.getOutputDirectory()
            .resolve("release")
            .resolve(fileName);
    }
}
