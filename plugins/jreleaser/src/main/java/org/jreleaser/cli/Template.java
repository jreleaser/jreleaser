/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
import org.jreleaser.templates.TemplateGenerationException;
import org.jreleaser.templates.TemplateGenerator;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
@CommandLine.Command(name = "template")
public class Template extends AbstractLoggingCommand {
    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    Composite composite;

    static class Composite {
        @CommandLine.ArgGroup(exclusive = false, order = 1,
            headingKey = "announcer.header")
        Announcers announcers;

        @CommandLine.ArgGroup(exclusive = false, order = 1,
            headingKey = "assembler.header")
        Assemblers assemblers;

        @CommandLine.ArgGroup(exclusive = false, order = 2,
            headingKey = "packager.header")
        Packagers packagers;

        String announcerName() {
            return announcers != null ? announcers.announcerName : null;
        }

        String assemblerType() {
            return assemblers != null ? assemblers.assemblerType : null;
        }

        String assemblerName() {
            return assemblers != null ? assemblers.assemblerName : null;
        }

        String packagerName() {
            return packagers != null ? packagers.packagerName : null;
        }

        String distributionName() {
            return packagers != null ? packagers.distributionName : null;
        }

        org.jreleaser.model.Distribution.DistributionType distributionType() {
            return packagers != null ? packagers.distributionType : null;
        }
    }

    static class Announcers {
        @CommandLine.Option(names = {"-a", "--announcer"},
            paramLabel = "<announcer>",
            descriptionKey = "announcer.name",
            required = true)
        String announcerName;
    }

    static class Assemblers {
        @CommandLine.Option(names = {"-st", "--assembler-type"},
            paramLabel = "<assembler-type>",
            descriptionKey = "assembler.type",
            required = true)
        String assemblerType;

        @CommandLine.Option(names = {"-s", "--assembler-name"},
            paramLabel = "<assembler-name>",
            descriptionKey = "assembler.name",
            required = true)
        String assemblerName;
    }

    static class Packagers {
        @CommandLine.Option(names = {"-d", "--distribution"},
            paramLabel = "<distribution>",
            required = true)
        String distributionName;

        @CommandLine.Option(names = {"-p", "--packager"},
            paramLabel = "<packager>",
            required = true)
        String packagerName;

        @CommandLine.Option(names = {"-dt", "--distribution-type"},
            paramLabel = "<type>",
            required = true,
            defaultValue = "JAVA_BINARY")
        org.jreleaser.model.Distribution.DistributionType distributionType;
    }

    @CommandLine.Option(names = {"-o", "--overwrite"})
    boolean overwrite;

    @CommandLine.Option(names = {"-sn", "--snapshot"})
    boolean snapshot;

    @CommandLine.ParentCommand
    Main parent;

    @Override
    protected Main parent() {
        return parent;
    }

    @Override
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
                .packagerName(composite.packagerName())
                .announcerName(composite.announcerName())
                .assemblerType(composite.assemblerType())
                .assemblerName(composite.assemblerName())
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
        } finally {
            if (logger != null) logger.close();
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
