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
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Tool;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.util.Constants;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class DistributionProcessor {
    private final JReleaserContext context;
    private final String distributionName;
    private final String toolName;
    private final Path outputDirectory;

    private DistributionProcessor(JReleaserContext context,
                                  String distributionName,
                                  String toolName) {
        this.context = context;
        this.distributionName = distributionName;
        this.toolName = toolName;
        this.outputDirectory = context.getOutputDirectory()
            .resolve(distributionName)
            .resolve(toolName);
    }

    public String getDistributionName() {
        return distributionName;
    }

    public String getToolName() {
        return toolName;
    }

    public boolean prepareDistribution() throws ToolProcessingException {
        Distribution distribution = context.getModel().findDistribution(distributionName);
        Tool tool = distribution.getTool(toolName);
        if (!tool.isEnabled()) {
            context.getLogger().debug("Skipping for {} distribution", distributionName);
            return false;
        }
        context.getLogger().info("Preparing {} distribution", distributionName);

        context.getLogger().debug("Reading checksums for {} distribution", distributionName);
        for (int i = 0; i < distribution.getArtifacts().size(); i++) {
            Artifact artifact = distribution.getArtifacts().get(i);
            Checksums.readHash(context, distributionName, artifact);
        }

        return ToolProcessors.findProcessor(context, tool)
            .prepareDistribution(distribution, initProps());
    }

    public boolean packageDistribution() throws ToolProcessingException {
        Distribution distribution = context.getModel().findDistribution(distributionName);
        Tool tool = distribution.getTool(toolName);
        if (!tool.isEnabled()) {
            context.getLogger().debug("Skipping for {} distribution", distributionName);
            return false;
        }
        context.getLogger().info("Packaging {} distribution", distributionName);

        return ToolProcessors.findProcessor(context, tool)
            .packageDistribution(distribution, initProps());
    }

    public boolean uploadDistribution() throws ToolProcessingException {
        Distribution distribution = context.getModel().findDistribution(distributionName);
        Tool tool = distribution.getTool(toolName);
        if (!tool.isEnabled()) {
            context.getLogger().debug("Skipping for {} distribution", distributionName);
            return false;
        }
        if (context.getModel().getProject().isSnapshot() && !tool.isSnapshotAllowed()) {
            context.getLogger().info("Skipping for {} distribution (snapshots not allowed)", distributionName);
            return false;
        }
        context.getLogger().info("Uploading {} distribution", distributionName);

        return ToolProcessors.findProcessor(context, tool)
            .uploadDistribution(distribution, initProps());
    }

    private Map<String, Object> initProps() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put(Constants.KEY_OUTPUT_DIRECTORY, outputDirectory);
        props.put(Constants.KEY_PREPARE_DIRECTORY, outputDirectory.resolve("prepare"));
        props.put(Constants.KEY_PACKAGE_DIRECTORY, outputDirectory.resolve("package"));
        props.put(Constants.KEY_DISTRIBUTION_PREPARE_DIRECTORY, context.getBasedir().relativize(outputDirectory.resolve("prepare")));
        props.put(Constants.KEY_DISTRIBUTION_PACKAGE_DIRECTORY, context.getBasedir().relativize(outputDirectory.resolve("package")));
        props.put(Constants.KEY_CHECKSUM_DIRECTORY, context.getChecksumsDirectory());
        return props;
    }

    public static DistributionProcessorBuilder builder() {
        return new DistributionProcessorBuilder();
    }

    public static class DistributionProcessorBuilder {
        private JReleaserContext context;
        private String distributionName;
        private String toolName;

        public DistributionProcessorBuilder context(JReleaserContext context) {
            this.context = requireNonNull(context, "'context' must not be null");
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

        public DistributionProcessor build() {
            requireNonNull(context, "'context' must not be null");
            requireNonBlank(distributionName, "'distributionName' must not be blank");
            requireNonBlank(toolName, "'toolName' must not be blank");
            return new DistributionProcessor(context, distributionName, toolName);
        }
    }
}
