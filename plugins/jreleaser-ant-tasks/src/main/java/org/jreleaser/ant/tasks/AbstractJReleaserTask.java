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
import org.jreleaser.config.JReleaserConfigParser;
import org.jreleaser.context.ContextCreator;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.JReleaserLogger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractJReleaserTask extends Task {
    protected File configFile;
    protected boolean dryrun;
    protected boolean skip;

    protected JReleaserLogger logger;
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

    protected JReleaserLogger getLogger() {
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
        logger.info("Configuring with {}", actualConfigFile);
        logger.info(" - basedir set to {}", actualBasedir.toAbsolutePath());
        logger.info(" - dryrun set to {}", dryrun);
        doExecute(createContext());
    }

    private void resolveConfigFile() {
        if (null != configFile) {
            actualConfigFile = configFile.toPath();
        } else {
            ServiceLoader<JReleaserConfigParser> parsers = ServiceLoader.load(JReleaserConfigParser.class,
                JReleaserConfigParser.class.getClassLoader());

            for (JReleaserConfigParser parser : parsers) {
                Path file = Paths.get(".").normalize()
                    .resolve("jreleaser." + parser.getPreferredFileExtension());
                if (Files.exists(file)) {
                    actualConfigFile = file;
                    break;
                }
            }
        }

        if (null == actualConfigFile || !Files.exists(actualConfigFile)) {
            throw new BuildException("Missing required option 'configFile' " +
                "or local file named .jreleaser[" +
                String.join("|", getSupportedConfigFormats()) + "]");
        }
    }

    private void resolveBasedir() {
        actualBasedir = actualConfigFile.toAbsolutePath().getParent();
    }

    protected abstract void doExecute(JReleaserContext context);

    protected Path getOutputDirectory() {
        return actualBasedir.resolve("out").resolve("jreleaser");
    }

    protected JReleaserContext createContext() {
        return ContextCreator.create(
            logger,
            actualConfigFile,
            actualBasedir,
            getOutputDirectory(),
            dryrun);
    }

    private Set<String> getSupportedConfigFormats() {
        Set<String> extensions = new LinkedHashSet<>();

        ServiceLoader<JReleaserConfigParser> parsers = ServiceLoader.load(JReleaserConfigParser.class,
            JReleaserConfigParser.class.getClassLoader());

        for (JReleaserConfigParser parser : parsers) {
            extensions.add("." + parser.getPreferredFileExtension());
        }

        return extensions;
    }
}
