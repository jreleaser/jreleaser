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
package org.jreleaser.app;

import org.jreleaser.app.internal.ColorizedJReleaserLoggerAdapter;
import org.jreleaser.config.JReleaserConfigLoader;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.JReleaserModelValidator;
import org.jreleaser.util.Logger;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command
public abstract class AbstractModelCommand extends AbstractCommand {
    @CommandLine.Option(names = {"--config-file"},
        description = "The config file")
    Path configFile;

    @CommandLine.ParentCommand
    Main parent;

    Path actualConfigFile;
    Path actualBasedir;

    @Override
    protected Main parent() {
        return parent;
    }

    protected void execute() {
        resolveConfigFile();
        resolveBasedir();
        logger.info("basedir set to {}", actualBasedir.toAbsolutePath());
        logger.info("dryrun set to {}", dryrun());
        consumeModel(resolveModel());
    }

    protected void resolveConfigFile() {
        actualConfigFile = null != configFile ? configFile : Paths.get(".").normalize().resolve(".jreleaser.yml");
        if (!Files.exists(actualConfigFile)) {
            spec.commandLine().getErr()
                .println(spec.commandLine().getColorScheme().errorText("Missing required option: '--config-file=<configFile>'"));
            spec.commandLine().usage(parent.out);
            throw new HaltExecutionException();
        }
    }

    private void resolveBasedir() {
        actualBasedir = null != basedir ? basedir : actualConfigFile.toAbsolutePath().getParent();
        if (!Files.exists(actualBasedir)) {
            spec.commandLine().getErr()
                .println(spec.commandLine().getColorScheme().errorText("Missing required option: '--basedir=<basedir>'"));
            spec.commandLine().usage(parent.out);
            throw new HaltExecutionException();
        }
    }

    protected abstract void consumeModel(JReleaserModel jreleaserModel);

    private JReleaserModel resolveModel() {
        try {
            logger.info("Reading configuration");
            JReleaserModel jreleaserModel = JReleaserConfigLoader.loadConfig(actualConfigFile);
            logger.info("Validating configuration");
            List<String> errors = JReleaserModelValidator.validate(logger, actualBasedir, jreleaserModel);
            if (!errors.isEmpty()) {
                Logger logger = new ColorizedJReleaserLoggerAdapter(parent.out);
                logger.error("== JReleaser ==");
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

    protected JReleaserContext createContext(JReleaserModel jreleaserModel) {
        return new JReleaserContext(
            logger,
            jreleaserModel,
            actualBasedir,
            getOutputDirectory(),
            dryrun());
    }

    protected boolean dryrun() {
        return false;
    }
}
