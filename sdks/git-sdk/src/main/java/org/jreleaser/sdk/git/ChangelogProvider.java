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

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Changelog;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
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
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.stream.Collectors.joining;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ChangelogProvider {
    public static final String ISSUES_FILE = "issues.txt";
    public static final String CHANGELOG_FILE = "CHANGELOG.md";

    public static String getChangelog(JReleaserContext context) throws IOException {
        Changelog changelog = context.getModel().getRelease().getGitService().getChangelog();

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
        Files.write(changelogFile, content.getBytes(), CREATE, WRITE, TRUNCATE_EXISTING);

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

        Changelog changelog = context.getModel().getRelease().getGitService().getChangelog();
        String externalChangelog = changelog.getExternal();

        if (isNotBlank(externalChangelog)) {
            Path externalChangelogPath = context.getBasedir().resolve(Paths.get(externalChangelog));
            File externalChangelogFile = externalChangelogPath.toFile();
            if (!externalChangelogFile.exists()) {
                throw new IllegalStateException(RB.$("ERROR_changelog_not_exist", context.getBasedir().relativize(externalChangelogPath)));
            }

            context.getLogger().info(RB.$("changelog.generator.read"), context.getBasedir().relativize(externalChangelogPath));
            String content = new String(Files.readAllBytes(externalChangelogPath));

            if (context.getModel().getRelease().getGitService().getIssues().isEnabled()) {
                Set<Integer> issues = extractIssues(context, content);
                storeIssues(context, issues);
            }

            return content;
        }

        context.getLogger().info(RB.$("changelog.generator.generate"));
        return ChangelogGenerator.generate(context);
    }

    public static Set<Integer> extractIssues(JReleaserContext context, String content) {
        context.getLogger().info(RB.$("issues.generator.extract"));

        GitService service = context.getModel().getRelease().getGitService();
        String issueTracker = service.getResolvedIssueTrackerUrl(context.getModel());
        if (!issueTracker.endsWith("/")) {
            issueTracker += "/";
        }

        String p1 = StringUtils.escapeRegexChars(issueTracker);
        String p2 = StringUtils.escapeRegexChars(service.getCanonicalRepoName());
        Pattern pattern = Pattern.compile(".*" + p1 + "(\\d+)|.*" + p2 + "#(\\d+)|.*#(\\d+)" + ".*");
        Matcher matcher = pattern.matcher(content);
        Set<Integer> issues = new TreeSet<>();
        while (matcher.find()) {
            if (isNotBlank(matcher.group(1))) issues.add(Integer.valueOf(matcher.group(1)));
            if (isNotBlank(matcher.group(2))) issues.add(Integer.valueOf(matcher.group(2)));
            if (isNotBlank(matcher.group(3))) issues.add(Integer.valueOf(matcher.group(3)));
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
        Files.write(issuesFile, content.getBytes(), CREATE, WRITE, TRUNCATE_EXISTING);
    }

    public static Path getReleaseFilePath(JReleaserContext context, String fileName) {
        return context.getOutputDirectory()
            .resolve("release")
            .resolve(fileName);
    }
}
