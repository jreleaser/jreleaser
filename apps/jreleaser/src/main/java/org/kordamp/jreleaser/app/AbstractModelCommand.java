/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Andres Almiray.
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
package org.kordamp.jreleaser.app;

import org.kordamp.jreleaser.app.internal.JReleaserLoggerAdapter;
import org.kordamp.jreleaser.config.JReleaserConfigLoader;
import org.kordamp.jreleaser.model.JReleaserModel;
import org.kordamp.jreleaser.model.JReleaserModelValidator;
import org.kordamp.jreleaser.util.Logger;
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
            JReleaserModel jreleaserModel = JReleaserConfigLoader.loadConfig(actualConfigFile);
            List<String> errors = JReleaserModelValidator.validate(logger, actualBasedir, jreleaserModel);
            if (!errors.isEmpty()) {
                Logger logger = new JReleaserLoggerAdapter(parent.out);
                logger.error("== JReleaser ==");
                errors.forEach(logger::error);
                throw new JReleaserException("JReleaser with " + actualConfigFile.toAbsolutePath() + " has not been properly configured.");
            }

            return jreleaserModel;
        } catch (IllegalArgumentException e) {
            throw new JReleaserException("Unexpected error when parsing configuration from " + actualConfigFile.toAbsolutePath(), e);
        }
    }
}
