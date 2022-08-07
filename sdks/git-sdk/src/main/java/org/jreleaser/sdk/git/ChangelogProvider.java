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
import org.jreleaser.model.JReleaserContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ChangelogProvider {
    public static String getChangelog(JReleaserContext context) throws IOException {
        Changelog changelog = context.getModel().getRelease().getGitService().getChangelog();

        if (!changelog.isEnabled()) {
            context.getLogger().info(RB.$("changelog.disabled"));
            return "";
        }

        return storeChangelog(context, resolveChangelog(context));
    }

    public static String storeChangelog(JReleaserContext context, String content) throws IOException {
        Path changelogFile = context.getOutputDirectory()
            .resolve("release")
            .resolve("CHANGELOG.md");

        context.getLogger().info(RB.$("changelog.generator.generate"), context.getBasedir().relativize(changelogFile));
        context.getLogger().debug(content);

        Files.createDirectories(changelogFile.getParent());
        Files.write(changelogFile, content.getBytes(), CREATE, WRITE, TRUNCATE_EXISTING);

        return content;
    }

    private static String resolveChangelog(JReleaserContext context) throws IOException {
        Changelog changelog = context.getModel().getRelease().getGitService().getChangelog();
        String externalChangelog = changelog.getExternal();

        if (isNotBlank(externalChangelog)) {
            Path externalChangelogPath = context.getBasedir().resolve(Paths.get(externalChangelog));
            File externalChangelogFile = externalChangelogPath.toFile();
            if (!externalChangelogFile.exists()) {
                throw new IllegalStateException(RB.$("ERROR_changelog_not_exist", context.getBasedir().relativize(externalChangelogPath)));
            }

            context.getLogger().info(RB.$("changelog.generator.read"), context.getBasedir().relativize(externalChangelogPath));
            return new String(Files.readAllBytes(externalChangelogPath));
        }

        return ChangelogGenerator.generate(context);
    }
}
