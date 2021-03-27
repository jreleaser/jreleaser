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
package org.jreleaser.templates;

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
    private final Path outputDirectory;
    private final boolean overwrite;
    private final boolean snapshot;

    private TemplateGenerator(JReleaserLogger logger,
                              String distributionName,
                              Distribution.DistributionType distributionType,
                              String toolName,
                              Path outputDirectory,
                              boolean overwrite,
                              boolean snapshot) {
        this.logger = logger;
        this.distributionName = distributionName;
        this.distributionType = distributionType;
        this.toolName = toolName;
        this.outputDirectory = outputDirectory;
        this.overwrite = overwrite;
        this.snapshot = snapshot;
    }

    public String getDistributionName() {
        return distributionName;
    }

    public Distribution.DistributionType getDistributionType() {
        return distributionType;
    }

    public String getToolName() {
        return toolName;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public Path generate() throws TemplateGenerationException {
        if (!Distribution.supportedTools().contains(toolName)) {
            logger.error("Tool {} is not supported", toolName);
            return null;
        }

        Path output = outputDirectory.resolve(distributionName)
            .resolve(toolName + (snapshot ? "-snapshot" : "")).normalize();

        logger.info("Creating output directory {}", output.toAbsolutePath());
        try {
            Files.createDirectories(output);
        } catch (IOException e) {
            throw fail(e);
        }

        Map<String, Reader> templates = TemplateUtils.resolveTemplates(logger, distributionType, toolName, snapshot);
        for (Map.Entry<String, Reader> template : templates.entrySet()) {
            Path outputFile = output.resolve(template.getKey());
            logger.info("Writing file " + outputFile.toAbsolutePath());
            try (Writer writer = Files.newBufferedWriter(outputFile, (overwrite ? CREATE : CREATE_NEW), WRITE, TRUNCATE_EXISTING);
                 Scanner scanner = new Scanner(template.getValue())) {
                while (scanner.hasNextLine()) {
                    writer.write(scanner.nextLine() + System.lineSeparator());
                }
            } catch (FileAlreadyExistsException e) {
                logger.error("File {} already exists and overwrite was set to false.", outputFile.toAbsolutePath());
                return null;
            } catch (Exception e) {
                throw fail(e);
            }
        }

        return output;
    }

    private TemplateGenerationException fail(Exception e) throws TemplateGenerationException {
        throw new TemplateGenerationException("Unexpected error when generating template. " +
            "distributionType=" + distributionType +
            ", distributionName=" + distributionName +
            ", toolName=" + toolName, e);
    }

    public static TemplateGeneratorBuilder builder() {
        return new TemplateGeneratorBuilder();
    }

    public static class TemplateGeneratorBuilder {
        private JReleaserLogger logger;
        private String distributionName;
        private Distribution.DistributionType distributionType = Distribution.DistributionType.JAVA_BINARY;
        private String toolName;
        private Path outputDirectory;
        private boolean overwrite;
        private boolean snapshot;

        public TemplateGeneratorBuilder logger(JReleaserLogger logger) {
            this.logger = requireNonNull(logger, "'logger' must not be null");
            return this;
        }

        public TemplateGeneratorBuilder distributionName(String distributionName) {
            this.distributionName = requireNonBlank(distributionName, "'distributionName' must not be blank");
            return this;
        }

        public TemplateGeneratorBuilder distributionType(Distribution.DistributionType distributionType) {
            this.distributionType = requireNonNull(distributionType, "'distributionType' must not be null");
            return this;
        }

        public TemplateGeneratorBuilder toolName(String toolName) {
            this.toolName = requireNonBlank(toolName, "'toolName' must not be blank");
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
            requireNonBlank(distributionName, "'distributionName' must not be blank");
            requireNonNull(distributionType, "'distributionType' must not be null");
            requireNonBlank(toolName, "'toolName' must not be blank");
            requireNonNull(outputDirectory, "'outputDirectory' must not be null");
            return new TemplateGenerator(logger, distributionName, distributionType, toolName, outputDirectory, overwrite, snapshot);
        }
    }
}
