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
package org.jreleaser.model;

import org.jreleaser.util.Errors;
import org.jreleaser.util.JReleaserLogger;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserContext {
    private final JReleaserLogger logger;
    private final JReleaserModel model;
    private final Path basedir;
    private final Path outputDirectory;
    private final boolean dryrun;
    private final Mode mode;
    private final Errors errors = new Errors();

    private String distributionName;
    private String toolName;
    private String announcerName;
    private String assemblerName;
    private String changelog;

    public JReleaserContext(JReleaserLogger logger,
                            Mode mode,
                            JReleaserModel model,
                            Path basedir,
                            Path outputDirectory,
                            boolean dryrun) {
        this.logger = logger;
        this.mode = mode;
        this.model = model;
        this.basedir = basedir;
        this.outputDirectory = outputDirectory;
        this.dryrun = dryrun;
    }

    public Errors validateModel() {
        if (errors.hasErrors()) return errors;

        this.model.getEnvironment().initProps(this);

        logger.info("Validating configuration");

        if (mode == Mode.FULL) {
            adjustDistributions();
        }

        try {
            JReleaserModelValidator.validate(this, this.mode, errors);
        } catch (Exception e) {
            logger.trace(e);
            errors.configuration(e.toString());
        }

        if (errors.hasErrors()) {
            logger.error("== JReleaser ==");
            errors.logErrors(logger);
        }

        return errors;
    }

    private void adjustDistributions() {
        logger.debug("adjusting distributions with assemblies");

        // resolve assemblers
        try {
            JReleaserModelValidator.validate(this, Mode.ASSEMBLE, errors);
            JReleaserModelResolver.resolve(this, errors);
        } catch (Exception e) {
            logger.trace(e);
            errors.configuration(e.toString());
        }

        // match distributions
        for (Assembler assembler : model.getAssemble().findAllAssemblers()) {
            Distribution distribution = model.getDistributions().get(assembler.getName());
            if (null == distribution) {
                distribution = new Distribution();
                distribution.setType(assembler.getType());
                distribution.setName(assembler.getName());
                model.getDistributions().put(assembler.getName(), distribution);
            }
            distribution.setExecutable(assembler.getExecutable());
            distribution.setActive(assembler.getActive());
            distribution.setJava(assembler.getJava());
            distribution.setArtifacts(assembler.getOutputs());

            Map<String, String> extraProperties = new LinkedHashMap<>(distribution.getExtraProperties());
            extraProperties.putAll(assembler.getExtraProperties());
            distribution.setExtraProperties(extraProperties);
        }
    }

    public JReleaserLogger getLogger() {
        return logger;
    }

    public Mode getMode() {
        return mode;
    }

    public JReleaserModel getModel() {
        return model;
    }

    public Path getBasedir() {
        return basedir;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public Path getChecksumsDirectory() {
        return outputDirectory.resolve("checksums");
    }

    public Path getSignaturesDirectory() {
        return outputDirectory.resolve("signatures");
    }

    public boolean isDryrun() {
        return dryrun;
    }

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    public boolean hasDistributionName() {
        return isNotBlank(distributionName);
    }

    public boolean hasToolName() {
        return isNotBlank(toolName);
    }

    public boolean hasAnnouncerName() {
        return isNotBlank(announcerName);
    }

    public boolean hasAssemblerName() {
        return isNotBlank(assemblerName);
    }

    public String getDistributionName() {
        return distributionName;
    }

    public void setDistributionName(String distributionName) {
        this.distributionName = distributionName;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getAnnouncerName() {
        return announcerName;
    }

    public void setAnnouncerName(String announcerName) {
        this.announcerName = announcerName;
    }

    public String getAssemblerName() {
        return assemblerName;
    }

    public void setAssemblerName(String assemblerName) {
        this.assemblerName = assemblerName;
    }

    @Override
    public String toString() {
        return "JReleaserContext[" +
            "basedir=" + basedir.toAbsolutePath() +
            ", outputDirectory=" + outputDirectory.toAbsolutePath() +
            ", dryrun=" + dryrun +
            ", mode=" + mode +
            "]";
    }

    public enum Mode {
        ASSEMBLE,
        FULL
    }
}
