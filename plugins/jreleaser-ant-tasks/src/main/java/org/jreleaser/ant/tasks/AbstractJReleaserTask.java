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
package org.jreleaser.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jreleaser.ant.tasks.internal.JReleaserLoggerAdapter;
import org.jreleaser.config.JReleaserConfigLoader;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.JReleaserModelValidator;
import org.jreleaser.util.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractJReleaserTask extends Task {
    protected File configFile;
    protected File outputDirectory;
    protected boolean dryrun;
    protected boolean skip;

    protected Logger logger;
    protected Path actualConfigFile;
    protected Path actualBasedir;

    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }

    public void setDryrun(boolean dryrun) {
        this.dryrun = dryrun;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    protected Logger getLogger() {
        if (null == logger) {
            logger = new JReleaserLoggerAdapter(getProject());
        }
        return logger;
    }

    @Override
    public void execute() throws BuildException {
        Banner.display(getLogger());
        if (skip) return;

        resolveConfigFile();
        resolveBasedir();
        logger.info("basedir set to {}", actualBasedir.toAbsolutePath());
        logger.info("dryrun set to {}", dryrun);
        consumeModel(resolveModel());
    }

    protected void resolveConfigFile() {
        actualConfigFile = null != configFile ? configFile.toPath() : getProject().getBaseDir().toPath().normalize().resolve(".jreleaser.yml");
        if (!Files.exists(actualConfigFile)) {
            throw new IllegalStateException("Missing required property: configFile");
        }
    }

    private void resolveBasedir() {
        actualBasedir = actualConfigFile.toAbsolutePath().getParent();
    }

    protected abstract void consumeModel(JReleaserModel jreleaserModel);

    private JReleaserModel resolveModel() {
        try {
            getLogger().info("Reading configuration");
            JReleaserModel jreleaserModel = JReleaserConfigLoader.loadConfig(actualConfigFile);
            getLogger().info("Validating configuration");
            List<String> errors = JReleaserModelValidator.validate(logger, actualBasedir, jreleaserModel);
            if (!errors.isEmpty()) {
                getLogger().error("== JReleaser ==");
                errors.forEach(logger::error);
                throw new JReleaserException("JReleaser with " + actualConfigFile.toAbsolutePath() + " has not been properly configured.");
            }

            return jreleaserModel;
        } catch (IllegalArgumentException e) {
            throw new JReleaserException("Unexpected error when parsing configuration from " + actualConfigFile.toAbsolutePath(), e);
        }
    }

    protected Path getOutputDirectory() {
        return actualBasedir.resolve("out").resolve("jreleaser");
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    protected JReleaserContext createContext(JReleaserModel jreleaserModel) {
        return new JReleaserContext(
            logger,
            jreleaserModel,
            actualBasedir,
            getOutputDirectory(),
            dryrun);
    }
}
