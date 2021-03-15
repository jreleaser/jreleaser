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

import org.jreleaser.model.Changelog;
import org.jreleaser.model.JReleaserContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ChangelogProvider {
    public static String getChangelog(JReleaserContext context, String commitsUrl, Changelog changelog) throws IOException {
        if (!changelog.isEnabled()) {
            return "";
        }

        String externalChangelog = changelog.getExternal();

        if (isNotBlank(externalChangelog)) {
            Path externalChangelogPath = context.getBasedir().resolve(Paths.get(externalChangelog));
            File externalChangelogFile = externalChangelogPath.toFile();
            if (!externalChangelogFile.exists()) {
                throw new IllegalStateException("Changelog " + context.getBasedir().relativize(externalChangelogPath) + " does not exist");
            }

            context.getLogger().info("Reading changelog from {}",context.getBasedir().relativize(externalChangelogPath));
            return new String(Files.readAllBytes(externalChangelogPath));
        }

        return ChangelogGenerator.generate(context, commitsUrl, changelog);
    }
}
