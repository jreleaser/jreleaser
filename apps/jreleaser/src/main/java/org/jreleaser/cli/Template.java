/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
@CommandLine.Command(name = "template")
public class Template extends AbstractCommand {
    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    Composite composite;

    static class Composite {
        @CommandLine.ArgGroup(exclusive = false, multiplicity = "0..1", order = 1,
            headingKey = "announcer.header")
        Announcers announcers;

        @CommandLine.ArgGroup(exclusive = false, multiplicity = "0..1", order = 2,
            headingKey = "tool.header")
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
            descriptionKey = "announcer.name",
            required = true)
        String announcerName;
    }

    static class Tools {
        @CommandLine.Option(names = {"-dn", "--distribution-name"},
            required = true)
        String distributionName;

        @CommandLine.Option(names = {"-tn", "--tool-name"},
            required = true)
        String toolName;

        @CommandLine.Option(names = {"-dt", "--distribution-type"},
            required = true,
            defaultValue = "JAVA_BINARY")
        Distribution.DistributionType distributionType;
    }

    @CommandLine.Option(names = {"-o", "--overwrite"})
    boolean overwrite;

    @CommandLine.Option(names = {"-s", "--snapshot"})
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
                logger.info($("jreleaser.template.TEXT_success"), output.toAbsolutePath());
            }
        } catch (TemplateGenerationException e) {
            throw new JReleaserException($("ERROR_unexpected_error"), e);
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
