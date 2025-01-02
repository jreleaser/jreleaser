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
package org.jreleaser.cli;

import org.jreleaser.model.JReleaserException;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "init")
public class Init extends AbstractLoggingCommand<Main> {
    @CommandLine.Option(names = {"-o", "--overwrite"})
    boolean overwrite;

    @CommandLine.Option(names = {"-f", "--format"})
    String format;

    @CommandLine.ParentCommand
    Main parent;

    private Path outputDirectory;

    @Override
    protected Main parent() {
        return parent;
    }

    @Override
    protected void execute() {
        try {
            outputDirectory = null != basedir ? basedir : Paths.get(".").normalize();
            initLogger();
            org.jreleaser.engine.init.Init.execute(logger, format, overwrite, outputDirectory);
        } catch (IllegalStateException e) {
            throw new JReleaserException($("ERROR_unexpected_error"), e);
        } finally {
            if (null != logger) logger.close();
        }
    }

    @Override
    protected Path getOutputDirectory() {
        return outputDirectory;
    }
}
