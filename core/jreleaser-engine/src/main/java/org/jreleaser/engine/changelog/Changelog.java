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
package org.jreleaser.engine.changelog;

import org.jreleaser.bundle.RB;
import org.jreleaser.engine.release.Releasers;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.JReleaserException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Changelog {
    public static String createChangelog(JReleaserContext context) {
        try {
            return Releasers.releaserFor(context).generateReleaseNotes();
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_changelog"), e);
        }
    }

    public static String readChangelog(JReleaserContext context) {
        Path changelogFile = context.getOutputDirectory()
            .resolve("release")
            .resolve("CHANGELOG.md");

        if (Files.exists(changelogFile)) {
            try {
                return new String(Files.readAllBytes(changelogFile)).trim();
            } catch (IOException e) {
                context.getLogger().warn(RB.$("ERROR_cannot_read_changelog"),
                    context.relativizeToBasedir(changelogFile));
            }
        }

        return "";
    }
}
