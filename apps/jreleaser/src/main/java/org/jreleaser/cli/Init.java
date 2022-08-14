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
package org.jreleaser.cli;

import org.apache.commons.io.IOUtils;
import org.jreleaser.config.JReleaserConfigParser;
import org.jreleaser.templates.TemplateResource;
import org.jreleaser.templates.TemplateUtils;
import org.jreleaser.util.JReleaserException;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.Set;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "init")
public class Init extends AbstractLoggingCommand {
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

    protected void execute() {
        try {
            outputDirectory = null != basedir ? basedir : Paths.get(".").normalize();
            initLogger();

            if (!getSupportedConfigFormats().contains(format)) {
                spec.commandLine().getErr()
                    .println(spec.commandLine()
                        .getColorScheme()
                        .errorText($("jreleaser.init.ERROR_invalid_format",
                            String.join("|", getSupportedConfigFormats())))
                    );
                spec.commandLine().usage(parent.out);
                throw new HaltExecutionException();
            }

            Path outputFile = outputDirectory.resolve("jreleaser." + format);

            TemplateResource template = TemplateUtils.resolveTemplate(logger, "jreleaser." + format + ".tpl");

            String content = IOUtils.toString(template.getReader());
            LocalDate now = LocalDate.now();
            content = content.replaceAll("@year@", now.getYear() + "");

            logger.info($("jreleaser.init.TEXT_writing_file"), outputFile.toAbsolutePath());

            try {
                Files.write(outputFile, content.getBytes(), (overwrite ? CREATE : CREATE_NEW), WRITE, TRUNCATE_EXISTING);
            } catch (FileAlreadyExistsException e) {
                logger.error($("jreleaser.init.ERROR_file_exists"), outputFile.toAbsolutePath());
                return;
            }

            if (!quiet) {
                logger.info($("jreleaser.init.TEXT_success"), outputDirectory.toAbsolutePath());
            }
        } catch (IllegalStateException | IOException e) {
            throw new JReleaserException($("ERROR_unexpected_error"), e);
        }
    }

    private Set<String> getSupportedConfigFormats() {
        Set<String> extensions = new LinkedHashSet<>();

        ServiceLoader<JReleaserConfigParser> parsers = ServiceLoader.load(JReleaserConfigParser.class,
            JReleaserConfigParser.class.getClassLoader());

        for (JReleaserConfigParser parser : parsers) {
            extensions.add(parser.getPreferredFileExtension());
        }

        return extensions;
    }

    @Override
    protected Path getOutputDirectory() {
        return outputDirectory;
    }
}
