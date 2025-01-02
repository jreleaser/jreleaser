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
import org.jreleaser.templates.TemplateGenerationException;
import org.jreleaser.templates.TemplateGenerator;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
@CommandLine.Command(name = "generate")
public class TemplateGenerate extends AbstractLoggingCommand<Template> {
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
            return null != announcers ? announcers.announcerName : null;
        }

        String assemblerType() {
            return null != assemblers ? assemblers.assemblerType : null;
        }

        String assemblerName() {
            return null != assemblers ? assemblers.assemblerName : null;
        }

        String packagerName() {
            return null != packagers ? packagers.packagerName : null;
        }

        String distributionName() {
            return null != packagers ? packagers.distributionName : null;
        }

        org.jreleaser.model.Distribution.DistributionType distributionType() {
            return null != packagers ? packagers.distributionType : null;
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
            paramLabel = "<type>",
            descriptionKey = "assembler.type",
            required = true)
        String assemblerType;

        @CommandLine.Option(names = {"-s", "--assembler-name"},
            paramLabel = "<name>",
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


    @Override
    protected void collectCandidateDeprecatedArgs(Set<AbstractCommand.DeprecatedArg> args) {
        super.collectCandidateDeprecatedArgs(args);
        args.add(new DeprecatedArg("-sn", "--snapshot", "1.5.0"));
        args.add(new DeprecatedArg("-d", "--distribution", "1.5.0"));
        args.add(new DeprecatedArg("-xd", "--exclude-distribution", "1.5.0"));
        args.add(new DeprecatedArg("-p", "--packager", "1.5.0"));
        args.add(new DeprecatedArg("-xp", "--exclude-packager", "1.5.0"));
        args.add(new DeprecatedArg("-a", "--announcer", "1.5.0"));
        args.add(new DeprecatedArg("-s", "--assembler", "1.5.0"));
        args.add(new DeprecatedArg("-st", "--assembler-type", "1.5.0"));
        args.add(new DeprecatedArg("-dt", "--distribution-type", "1.5.0"));
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
                logger.info($("jreleaser.template.generate.TEXT_success"), output.toAbsolutePath());
            }
        } catch (TemplateGenerationException e) {
            throw new JReleaserException($("ERROR_unexpected_error"), e);
        } finally {
            if (null != logger) logger.close();
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
