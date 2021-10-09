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
package org.jreleaser.templates;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Announce;
import org.jreleaser.model.Distribution;
import org.jreleaser.util.JReleaserLogger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class TemplateGenerator {
    private final JReleaserLogger logger;
    private final String distributionName;
    private final Distribution.DistributionType distributionType;
    private final String toolName;
    private final String announcerName;
    private final Path outputDirectory;
    private final boolean overwrite;
    private final boolean snapshot;

    private TemplateGenerator(JReleaserLogger logger,
                              String distributionName,
                              Distribution.DistributionType distributionType,
                              String toolName,
                              String announcerName,
                              Path outputDirectory,
                              boolean overwrite,
                              boolean snapshot) {
        this.logger = logger;
        this.distributionName = distributionName;
        this.distributionType = distributionType;
        this.toolName = toolName;
        this.announcerName = announcerName;
        this.outputDirectory = outputDirectory.resolve(isNotBlank(announcerName) ? "templates" : "distributions");
        this.overwrite = overwrite;
        this.snapshot = snapshot;
    }

    public Path generate() throws TemplateGenerationException {
        if (isNotBlank(announcerName)) {
            return generateAnnouncer();
        }
        return generateTool();
    }

    private Path generateAnnouncer() throws TemplateGenerationException {
        if (!Announce.supportedAnnouncers().contains(announcerName)) {
            logger.error(RB.$("templates.announcer.not.supported"), announcerName);
            return null;
        }

        logger.info(RB.$("templates.create.directory"), outputDirectory.toAbsolutePath());
        try {
            Files.createDirectories(outputDirectory);
        } catch (IOException e) {
            throw fail(e);
        }

        Reader reader = TemplateUtils.resolveTemplate(logger, announcerName);

        Path outputFile = outputDirectory.resolve(announcerName + ".tpl");
        logger.info(RB.$("templates.writing.file"), outputFile.toAbsolutePath());

        try (Writer writer = Files.newBufferedWriter(outputFile, (overwrite ? CREATE : CREATE_NEW), WRITE, TRUNCATE_EXISTING);
             Scanner scanner = new Scanner(reader)) {
            while (scanner.hasNextLine()) {
                writer.write(scanner.nextLine() + System.lineSeparator());
            }
        } catch (FileAlreadyExistsException e) {
            logger.error(RB.$("templates.file_exists.error"), outputFile.toAbsolutePath());
            return null;
        } catch (Exception e) {
            throw fail(e);
        }

        return outputFile;
    }

    private Path generateTool() throws TemplateGenerationException {
        if (!Distribution.supportedTools().contains(toolName)) {
            logger.error(RB.$("ERROR_tool_not_supported"), toolName);
            return null;
        }

        Path output = outputDirectory.resolve(distributionName)
            .resolve(toolName + (snapshot ? "-snapshot" : "")).normalize();

        logger.info(RB.$("templates.create.directory"), output.toAbsolutePath());
        try {
            Files.createDirectories(output);
        } catch (IOException e) {
            throw fail(e);
        }

        Map<String, Reader> templates = TemplateUtils.resolveTemplates(logger, distributionType.name(), toolName, snapshot);
        for (Map.Entry<String, Reader> template : templates.entrySet()) {
            Path outputFile = output.resolve(template.getKey());
            logger.info(RB.$("templates.writing.file"), outputFile.toAbsolutePath());

            try {
                Files.createDirectories(outputFile.getParent());
            } catch (IOException e) {
                throw fail(e);
            }

            try (Writer writer = Files.newBufferedWriter(outputFile, (overwrite ? CREATE : CREATE_NEW), WRITE, TRUNCATE_EXISTING);
                 Scanner scanner = new Scanner(template.getValue())) {
                while (scanner.hasNextLine()) {
                    writer.write(scanner.nextLine() + System.lineSeparator());
                }
            } catch (FileAlreadyExistsException e) {
                logger.error(RB.$("templates.file_exists.error"), outputFile.toAbsolutePath());
                return null;
            } catch (Exception e) {
                throw fail(e);
            }
        }

        return output;
    }

    private TemplateGenerationException fail(Exception e) throws TemplateGenerationException {
        throw new TemplateGenerationException(RB.$("ERROR_unexpected_template_fail",
            distributionType, distributionName, toolName), e);
    }

    public static TemplateGeneratorBuilder builder() {
        return new TemplateGeneratorBuilder();
    }

    public static class TemplateGeneratorBuilder {
        private JReleaserLogger logger;
        private String distributionName;
        private Distribution.DistributionType distributionType = Distribution.DistributionType.JAVA_BINARY;
        private String toolName;
        private String announcerName;
        private Path outputDirectory;
        private boolean overwrite;
        private boolean snapshot;

        public TemplateGeneratorBuilder logger(JReleaserLogger logger) {
            this.logger = requireNonNull(logger, "'logger' must not be null");
            return this;
        }

        public TemplateGeneratorBuilder distributionName(String distributionName) {
            this.distributionName = distributionName;
            return this;
        }

        public TemplateGeneratorBuilder distributionType(Distribution.DistributionType distributionType) {
            this.distributionType = distributionType;
            return this;
        }

        public TemplateGeneratorBuilder toolName(String toolName) {
            this.toolName = toolName;
            return this;
        }

        public TemplateGeneratorBuilder announcerName(String announcerName) {
            this.announcerName = announcerName;
            return this;
        }

        public TemplateGeneratorBuilder outputDirectory(Path outputDirectory) {
            this.outputDirectory = requireNonNull(outputDirectory, "'outputDirectory' must not be null");
            return this;
        }

        public TemplateGeneratorBuilder overwrite(boolean overwrite) {
            this.overwrite = overwrite;
            return this;
        }

        public TemplateGeneratorBuilder snapshot(boolean snapshot) {
            this.snapshot = snapshot;
            return this;
        }

        public TemplateGenerator build() {
            requireNonNull(logger, "'logger' must not be null");
            if (isBlank(announcerName)) {
                requireNonBlank(distributionName, "'distributionName' must not be blank");
                requireNonNull(distributionType, "'distributionType' must not be null");
                requireNonBlank(toolName, "'toolName' must not be blank");
            }
            requireNonNull(outputDirectory, "'outputDirectory' must not be null");
            return new TemplateGenerator(logger, distributionName, distributionType, toolName, announcerName, outputDirectory, overwrite, snapshot);
        }
    }
}
