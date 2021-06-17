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
package org.jreleaser.cli;

import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.templates.TemplateGenerationException;
import org.jreleaser.templates.TemplateGenerator;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "template",
    mixinStandardHelpOptions = true,
    description = "Generate a tool/announcer template.")
public class Template extends AbstractCommand {
    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    Composite composite;

    static class Composite {
        @CommandLine.ArgGroup(exclusive = false, multiplicity = "0..1", order = 1,
            heading = "Announcer templates%n")
        Announcers announcers;

        @CommandLine.ArgGroup(exclusive = false, multiplicity = "0..1", order = 2,
            heading = "Tool templates%n")
        Tools tools;

        String announcerName() {
            return announcers != null ? announcers.announcerName : null;
        }

        String toolName() {
            return tools != null ? tools.toolName : null;
        }

        String distributionName() {
            return tools != null ? tools.distributionName : null;
        }

        Distribution.DistributionType distributionType() {
            return tools != null ? tools.distributionType : null;
        }
    }

    static class Announcers {
        @CommandLine.Option(names = {"-an", "--announcer-name"},
            description = "The name of the announcer.",
            required = true)
        String announcerName;
    }

    static class Tools {
        @CommandLine.Option(names = {"-dn", "--distribution-name"},
            description = "The name of the distribution.",
            required = true)
        String distributionName;

        @CommandLine.Option(names = {"-tn", "--tool-name"},
            description = "The name of the tool.",
            required = true)
        String toolName;

        @CommandLine.Option(names = {"-dt", "--distribution-type"},
            description = "The type of the distribution.\nDefaults to JAVA_BINARY.",
            required = true,
            defaultValue = "JAVA_BINARY")
        Distribution.DistributionType distributionType;
    }

    @CommandLine.Option(names = {"-o", "--overwrite"},
        description = "Overwrite existing files.")
    boolean overwrite;

    @CommandLine.Option(names = {"-s", "--snapshot"},
        description = "Use snapshot templates.")
    boolean snapshot;

    @CommandLine.ParentCommand
    Main parent;

    @Override
    protected Main parent() {
        return parent;
    }

    protected void execute() {
        try {
            basedir = null != basedir ? basedir : Paths.get(".").normalize();

            initLogger();

            Path outputDirectory = basedir
                .resolve("src")
                .resolve("jreleaser");

            Path output = TemplateGenerator.builder()
                .logger(logger)
                .distributionName(composite.distributionName())
                .distributionType(composite.distributionType())
                .toolName(composite.toolName())
                .announcerName(composite.announcerName())
                .outputDirectory(outputDirectory)
                .overwrite(overwrite)
                .snapshot(snapshot)
                .build()
                .generate();

            if (null != output && !quiet) {
                logger.info("Template generated at {}", output.toAbsolutePath());
            }
        } catch (TemplateGenerationException e) {
            throw new JReleaserException("Unexpected error", e);
        }
    }

    @Override
    protected Path getOutputDirectory() {
        if (null != outputdir) {
            return outputdir.resolve("jreleaser");
        }
        return basedir.resolve("out").resolve("jreleaser");
    }
}
