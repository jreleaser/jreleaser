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
package org.jreleaser.templates;

import org.apache.commons.io.IOUtils;
import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.JReleaserException;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Objects.requireNonNull;
import static org.jreleaser.model.internal.JReleaserSupport.supportedAnnouncers;
import static org.jreleaser.model.internal.JReleaserSupport.supportedAssemblers;
import static org.jreleaser.model.internal.JReleaserSupport.supportedPackagers;
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
    private final org.jreleaser.model.Distribution.DistributionType distributionType;
    private final String packagerName;
    private final String announcerName;
    private final String assemblerType;
    private final String assemblerName;
    private final Path outputDirectory;
    private final boolean overwrite;
    private final boolean snapshot;

    private TemplateGenerator(JReleaserLogger logger,
                              String distributionName,
                              org.jreleaser.model.Distribution.DistributionType distributionType,
                              String assemblerType,
                              String assemblerName,
                              String packagerName,
                              String announcerName,
                              Path outputDirectory,
                              boolean overwrite,
                              boolean snapshot) {
        this.logger = logger;
        this.distributionName = distributionName;
        this.distributionType = distributionType;
        this.assemblerType = assemblerType;
        this.assemblerName = assemblerName;
        this.packagerName = packagerName;
        this.announcerName = announcerName;
        String directory = "distributions";
        if (isNotBlank(announcerName)) directory = "templates";
        if (isNotBlank(assemblerType)) directory = "assemblers";
        this.outputDirectory = outputDirectory.resolve(directory);
        this.overwrite = overwrite;
        this.snapshot = snapshot;
    }

    public Path generate() throws TemplateGenerationException {
        if (isNotBlank(announcerName)) {
            return generateAnnouncer();
        } else if (isNotBlank(assemblerName) && isNotBlank(assemblerType)) {
            return generateAssembler();
        }
        return generatePackager();
    }

    private Path generateAnnouncer() throws TemplateGenerationException {
        if (!supportedAnnouncers().contains(announcerName)) {
            logger.error(RB.$("templates.announcer.not.supported"), announcerName);
            return null;
        }

        logger.info(RB.$("templates.create.directory"), outputDirectory.toAbsolutePath());
        try {
            Files.createDirectories(outputDirectory);
        } catch (IOException e) {
            throw fail(e);
        }

        TemplateResource value = null;

        try {
            value = TemplateUtils.resolveTemplate(logger, "announcers/" + announcerName + ".tpl");
        } catch (JReleaserException e) {
            if (e.getMessage().contains("classpath")) {
                throw new JReleaserException(RB.$("templates.announcer.no.template", announcerName));
            } else {
                throw e;
            }
        }

        Path outputFile = outputDirectory.resolve(announcerName + ".tpl");
        logger.info(RB.$("templates.writing.file"), outputFile.toAbsolutePath());

        try (Writer fileWriter = Files.newBufferedWriter(outputFile, overwrite ? CREATE : CREATE_NEW, WRITE, TRUNCATE_EXISTING);
             BufferedWriter decoratedWriter = new VersionDecoratingWriter(fileWriter)) {
            IOUtils.copy(value.getReader(), decoratedWriter);
        } catch (FileAlreadyExistsException e) {
            logger.error(RB.$("templates.file_exists.error"), outputFile.toAbsolutePath());
            return null;
        } catch (Exception e) {
            throw fail(e);
        }

        return outputFile;
    }

    private Path generateAssembler() throws TemplateGenerationException {
        if (!supportedAssemblers().contains(assemblerType)) {
            logger.error(RB.$("templates.assembler.not.supported"), assemblerType);
            return null;
        }

        Path output = outputDirectory.resolve(assemblerName)
            .resolve(assemblerType).normalize();

        logger.info(RB.$("templates.create.directory"), output.toAbsolutePath());
        try {
            Files.createDirectories(output);
        } catch (IOException e) {
            throw fail(e);
        }

        Map<String, TemplateResource> templates = TemplateUtils.resolveTemplates(logger, assemblerType, assemblerType, false);
        return generateTemplates(templates, output);
    }

    private Path generatePackager() throws TemplateGenerationException {
        if (!supportedPackagers().contains(packagerName)) {
            logger.error(RB.$("ERROR_packager_not_supported"), packagerName);
            return null;
        }

        Path output = outputDirectory.resolve(distributionName)
            .resolve(packagerName + (snapshot ? "-snapshot" : "")).normalize();

        logger.info(RB.$("templates.create.directory"), output.toAbsolutePath());
        try {
            Files.createDirectories(output);
        } catch (IOException e) {
            throw fail(e);
        }

        Map<String, TemplateResource> templates = TemplateUtils.resolveTemplates(logger, distributionType.name(), packagerName, snapshot);
        return generateTemplates(templates, output);
    }

    private Path generateTemplates(Map<String, TemplateResource> templates, Path output) throws TemplateGenerationException {
        for (Map.Entry<String, TemplateResource> template : templates.entrySet()) {
            Path outputFile = output.resolve(template.getKey());
            logger.info(RB.$("templates.writing.file"), outputFile.toAbsolutePath());

            try {
                Files.createDirectories(outputFile.getParent());
            } catch (IOException e) {
                throw fail(e);
            }

            TemplateResource value = template.getValue();

            if (value.isReader()) {
                try (Writer fileWriter = Files.newBufferedWriter(outputFile, overwrite ? CREATE : CREATE_NEW, WRITE, TRUNCATE_EXISTING);
                     BufferedWriter decoratedWriter = new VersionDecoratingWriter(fileWriter)) {
                    IOUtils.copy(value.getReader(), decoratedWriter);
                } catch (FileAlreadyExistsException e) {
                    logger.error(RB.$("templates.file_exists.error"), outputFile.toAbsolutePath());
                    return null;
                } catch (Exception e) {
                    throw fail(e);
                }
            } else {
                try (OutputStream outputStream = new FileOutputStream(outputFile.toFile())) {
                    IOUtils.copy(value.getInputStream(), outputStream);
                } catch (FileAlreadyExistsException e) {
                    logger.error(RB.$("templates.file_exists.error"), outputFile.toAbsolutePath());
                    return null;
                } catch (Exception e) {
                    throw fail(e);
                }
            }
        }

        return output;
    }

    private TemplateGenerationException fail(Exception e) throws TemplateGenerationException {
        throw new TemplateGenerationException(RB.$("ERROR_unexpected_template_fail",
            distributionType, distributionName, packagerName), e);
    }

    public static TemplateGeneratorBuilder builder() {
        return new TemplateGeneratorBuilder();
    }

    public static class TemplateGeneratorBuilder {
        private JReleaserLogger logger;
        private String distributionName;
        private org.jreleaser.model.Distribution.DistributionType distributionType = org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
        private String packagerName;
        private String announcerName;
        private String assemblerName;
        private String assemblerType;
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

        public TemplateGeneratorBuilder distributionType(org.jreleaser.model.Distribution.DistributionType distributionType) {
            this.distributionType = distributionType;
            return this;
        }

        public TemplateGeneratorBuilder packagerName(String packagerName) {
            this.packagerName = packagerName;
            return this;
        }

        public TemplateGeneratorBuilder announcerName(String announcerName) {
            this.announcerName = announcerName;
            return this;
        }

        public TemplateGeneratorBuilder assemblerName(String assemblerName) {
            this.assemblerName = assemblerName;
            return this;
        }

        public TemplateGeneratorBuilder assemblerType(String assemblerType) {
            this.assemblerType = assemblerType;
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
                if (isNotBlank(assemblerType)) {
                    requireNonBlank(assemblerName, "'assemblerName' must not be blank");
                } else if (isNotBlank(assemblerName)) {
                    requireNonBlank(assemblerType, "'assemblerType' must not be blank");
                } else {
                    requireNonBlank(distributionName, "'distributionName' must not be blank");
                    requireNonNull(distributionType, "'distributionType' must not be null");
                    requireNonBlank(packagerName, "'packagerName' must not be blank");
                }
            }
            requireNonNull(outputDirectory, "'outputDirectory' must not be null");
            return new TemplateGenerator(logger, distributionName, distributionType,
                assemblerType, assemblerName, packagerName, announcerName,
                outputDirectory, overwrite, snapshot);
        }
    }
}
