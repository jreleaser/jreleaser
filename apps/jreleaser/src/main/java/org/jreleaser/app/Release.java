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
package org.jreleaser.app;

import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.releaser.ReleaseException;
import org.jreleaser.model.releaser.Releaser;
import org.jreleaser.releaser.Releasers;
import org.jreleaser.tools.Checksums;
import picocli.CommandLine;

import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "release",
    description = "Create or update a release")
public class Release extends AbstractModelCommand {
    @CommandLine.Option(names = {"-y", "--dryrun"},
        description = "Skips remote operations.")
    boolean dryRun;

    @Override
    protected void consumeModel(JReleaserModel jreleaserModel) {
        Checksums.collectAndWriteChecksums(logger, jreleaserModel, getChecksumsDirectory());

        try {
            Releaser releaser = Releasers.findReleaser(logger, jreleaserModel)
                .configureWith(actualBasedir, jreleaserModel)
                .addReleaseAsset(getChecksumsDirectory().resolve("checksums.txt"))
                .build();
            releaser.release(dryRun);
        } catch (ReleaseException e) {
            throw new JReleaserException("Unexpected error when creating release " + actualConfigFile.toAbsolutePath(), e);
        }
    }

    protected Path getOutputDirectory() {
        return actualBasedir.resolve("out");
    }

    protected Path getChecksumsDirectory() {
        return getOutputDirectory()
            .resolve("jreleaser")
            .resolve("checksums");
    }
}
