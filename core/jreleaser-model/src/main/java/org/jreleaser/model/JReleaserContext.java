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
package org.jreleaser.model;

import org.jreleaser.util.Logger;

import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserContext {
    private final Logger logger;
    private final JReleaserModel model;
    private final Path basedir;
    private final Path outputDirectory;
    private final boolean dryrun;

    public JReleaserContext(Logger logger, JReleaserModel model, Path basedir, Path outputDirectory, boolean dryrun) {
        this.logger = logger;
        this.model = model;
        this.basedir = basedir;
        this.outputDirectory = outputDirectory;
        this.dryrun = dryrun;
    }

    public Logger getLogger() {
        return logger;
    }

    public JReleaserModel getModel() {
        return model;
    }

    public Path getBasedir() {
        return basedir;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public Path getChecksumsDirectory() {
        return outputDirectory.resolve("checksums");
    }

    public Path getSignaturesDirectory() {
        return outputDirectory.resolve("signatures");
    }

    public boolean isDryrun() {
        return dryrun;
    }

    @Override
    public String toString() {
        return "JReleaserContext[" +
            "basedir=" + basedir.toAbsolutePath() +
            ", outputDirectory=" + outputDirectory.toAbsolutePath() +
            ", dryrun=" + dryrun +
            "]";
    }
}
