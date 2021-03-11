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
package org.jreleaser.tools;

import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Tool;
import org.jreleaser.util.Constants;
import org.jreleaser.util.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class DistributionProcessor {
    private final Logger logger;
    private final JReleaserModel model;
    private final String distributionName;
    private final String toolName;
    private final Path checksumDirectory;
    private final Path outputDirectory;

    private DistributionProcessor(Logger logger,
                                  JReleaserModel model,
                                  String distributionName,
                                  String toolName,
                                  Path checksumDirectory,
                                  Path outputDirectory) {
        this.logger = logger;
        this.model = model;
        this.distributionName = distributionName;
        this.toolName = toolName;
        this.checksumDirectory = checksumDirectory;
        this.outputDirectory = outputDirectory;
    }

    public JReleaserModel getModel() {
        return model;
    }

    public String getDistributionName() {
        return distributionName;
    }

    public String getToolName() {
        return toolName;
    }

    public Path getChecksumDirectory() {
        return checksumDirectory;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public boolean prepareDistribution() throws ToolProcessingException {
        Distribution distribution = model.findDistribution(distributionName);
        Tool tool = distribution.getTool(toolName);
        if (!tool.isEnabled()) {
            logger.warn("Skipping {} tool for {} distribution", toolName, distributionName);
            return false;
        }

        logger.info("Preparing {} distribution with tool {}", distributionName, toolName);

        Map<String, Object> context = new LinkedHashMap<>();
        context.put(Constants.KEY_CHECKSUM_DIRECTORY, checksumDirectory);
        context.put(Constants.KEY_OUTPUT_DIRECTORY, outputDirectory);
        context.put(Constants.KEY_PREPARE_DIRECTORY, outputDirectory.resolve("prepare"));

        logger.debug("Reading checksums for {} distribution", distributionName);
        for (int i = 0; i < distribution.getArtifacts().size(); i++) {
            Artifact artifact = distribution.getArtifacts().get(i);
            Checksums.readHash(logger, distributionName, artifact, checksumDirectory);
        }

        ToolProcessor<?> toolProcessor = ToolProcessors.findProcessor(logger, model, tool);
        return toolProcessor.prepareDistribution(distribution, context);
    }

    public boolean packageDistribution() throws ToolProcessingException {
        Distribution distribution = model.findDistribution(distributionName);
        Tool tool = distribution.getTool(toolName);
        if (!tool.isEnabled()) {
            logger.warn("Skipping {} tool for {} distribution", toolName, distributionName);
            return false;
        }

        logger.info("Packaging {} distribution with tool {}", distributionName, toolName);

        Map<String, Object> context = new LinkedHashMap<>();
        context.put(Constants.KEY_OUTPUT_DIRECTORY, outputDirectory);
        context.put(Constants.KEY_PREPARE_DIRECTORY, outputDirectory.resolve("prepare"));
        context.put(Constants.KEY_PACKAGE_DIRECTORY, outputDirectory.resolve("package"));

        ToolProcessor<?> toolProcessor = ToolProcessors.findProcessor(logger, model, tool);
        return toolProcessor.packageDistribution(distribution, context);
    }

    private void readHash(Artifact artifact, Path checksumDirectory) throws ToolProcessingException {
        Path artifactPath = Paths.get(artifact.getPath());
        Path checksumPath = checksumDirectory.resolve(distributionName).resolve(artifactPath.getFileName() + ".sha256");

        if (!artifactPath.toFile().exists()) {
            throw new ToolProcessingException("Artifact does not exist. " + artifactPath.toAbsolutePath());
        }

        if (!checksumPath.toFile().exists()) {
            logger.info("Artifact checksum does not exist. " + checksumPath.toAbsolutePath());
            Checksums.calculateHash(logger, artifactPath, checksumPath);
        } else if (artifactPath.toFile().lastModified() > checksumPath.toFile().lastModified()) {
            logger.info("Artifact {} is newer than {}", artifactPath.toAbsolutePath(), checksumPath.toAbsolutePath());
            Checksums.calculateHash(logger, artifactPath, checksumPath);
        }

        try {
            logger.debug("Reading checksum for {} from {}", artifactPath.toAbsolutePath(), checksumPath.toAbsolutePath());
            artifact.setHash(new String(Files.readAllBytes(checksumPath)));
        } catch (IOException e) {
            throw new ToolProcessingException("Unexpected error when reading hash from " + checksumPath.toAbsolutePath(), e);
        }
    }

    public static DistributionProcessorBuilder builder() {
        return new DistributionProcessorBuilder();
    }

    public static class DistributionProcessorBuilder {
        private Logger logger;
        private JReleaserModel model;
        private String distributionName;
        private String toolName;
        private Path checksumDirectory;
        private Path outputDirectory;

        public DistributionProcessorBuilder logger(Logger logger) {
            this.logger = requireNonNull(logger, "'logger' must not be null");
            return this;
        }

        public DistributionProcessorBuilder model(JReleaserModel model) {
            this.model = requireNonNull(model, "'model' must not be null");
            return this;
        }

        public DistributionProcessorBuilder distributionName(String distributionName) {
            this.distributionName = requireNonBlank(distributionName, "'distributionName' must not be blank");
            return this;
        }

        public DistributionProcessorBuilder toolName(String toolName) {
            this.toolName = requireNonBlank(toolName, "'toolName' must not be blank");
            return this;
        }

        public DistributionProcessorBuilder checksumDirectory(Path checksumDirectory) {
            this.checksumDirectory = requireNonNull(checksumDirectory, "'checksumDirectory' must not be null");
            return this;
        }

        public DistributionProcessorBuilder outputDirectory(Path outputDirectory) {
            this.outputDirectory = requireNonNull(outputDirectory, "'outputDirectory' must not be null");
            return this;
        }

        public DistributionProcessor build() {
            requireNonNull(logger, "'logger' must not be null");
            requireNonNull(model, "'model' must not be null");
            requireNonBlank(distributionName, "'distributionName' must not be blank");
            requireNonBlank(toolName, "'toolName' must not be blank");
            requireNonNull(checksumDirectory, "'checksumDirectory' must not be null");
            requireNonNull(outputDirectory, "'outputDirectory' must not be null");
            return new DistributionProcessor(logger, model, distributionName, toolName, checksumDirectory, outputDirectory);
        }
    }
}
