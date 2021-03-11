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

import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.templates.TemplateGenerationException;
import org.jreleaser.templates.TemplateGenerator;
import picocli.CommandLine;

import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "template",
    description = "Generates a tool template")
public class Template extends AbstractCommand {
    @CommandLine.Option(names = {"--distribution-name"},
        description = "The name of the distribution",
        required = true)
    String distributionName;

    @CommandLine.Option(names = {"--tool-name"},
        description = "The name of the tool",
        required = true)
    String toolName;

    @CommandLine.Option(names = {"--distribution-type"},
        description = "The type of the distribution",
        required = true,
        defaultValue = "BINARY")
    Distribution.DistributionType distributionType;

    @CommandLine.Option(names = {"--overwrite"},
        description = "Overwrite existing files")
    boolean overwrite;

    @CommandLine.ParentCommand
    Main parent;

    @Override
    protected Main parent() {
        return parent;
    }

    protected void execute() {
        try {
            if (null == basedir) {
                spec.commandLine().getErr()
                    .println(spec.commandLine().getColorScheme().errorText("Missing required option: '--basedir=<basedir>'"));
                spec.commandLine().usage(parent.out);
                throw new HaltExecutionException();
            }

            Path outputDirectory = basedir
                .resolve("src")
                .resolve("distributions");

            boolean result = TemplateGenerator.builder()
                .logger(logger)
                .distributionName(distributionName)
                .distributionType(distributionType)
                .toolName(toolName)
                .outputDirectory(outputDirectory)
                .overwrite(overwrite)
                .build()
                .generate();

            if (result && !quiet) {
                parent.out.println("Template generated at " +
                    outputDirectory.resolve(distributionName).resolve(toolName)
                        .normalize().toAbsolutePath());
            }
        } catch (TemplateGenerationException e) {
            throw new JReleaserException("Unexpected error", e);
        }
    }
}
