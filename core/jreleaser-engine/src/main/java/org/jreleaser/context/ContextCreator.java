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
package org.jreleaser.context;

import org.jreleaser.config.JReleaserConfigLoader;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.util.JReleaserLogger;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ContextCreator {
    public static JReleaserContext create(JReleaserLogger logger,
                                          Path configFile,
                                          Path basedir,
                                          Path outputDirectory,
                                          boolean dryrun) {

        JReleaserContext context = new JReleaserContext(
            logger,
            resolveModel(logger, configFile),
            basedir,
            outputDirectory,
            dryrun);

        try {
            context.getModel().setCommit(GitSdk.head(basedir));
        } catch (IOException e) {
            throw new JReleaserException("Could not determine git HEAD", e);
        }

        if (!context.validateModel().isEmpty()) {
            throw new JReleaserException("JReleaser with " + configFile.toAbsolutePath() + " has not been properly configured.");
        }

        report(context);

        return context;
    }

    public static JReleaserContext create(JReleaserLogger logger,
                                          JReleaserModel model,
                                          Path basedir,
                                          Path outputDirectory,
                                          boolean dryrun) {

        JReleaserContext context = new JReleaserContext(
            logger,
            model,
            basedir,
            outputDirectory,
            dryrun);

        try {
            context.getModel().setCommit(GitSdk.head(basedir));
        } catch (IOException e) {
            throw new JReleaserException("Could not determine git HEAD", e);
        }

        if (!context.validateModel().isEmpty()) {
            throw new JReleaserException("JReleaser has not been properly configured.");
        }

        report(context);

        return context;
    }

    private static JReleaserModel resolveModel(JReleaserLogger logger, Path configFile) {
        try {
            logger.info("Reading configuration");
            return JReleaserConfigLoader.loadConfig(configFile);
        } catch (IllegalArgumentException e) {
            throw new JReleaserException("Unexpected error when parsing configuration from " + configFile.toAbsolutePath(), e);
        }
    }

    private static void report(JReleaserContext context) {
        context.getLogger().info("Project version set to {}", context.getModel().getProject().getResolvedVersion());
        context.getLogger().info("Release is{}snapshot", context.getModel().getProject().isSnapshot() ? " " : " not ");
        context.getLogger().info("Timestamp is {}", context.getModel().getTimestamp());
        context.getLogger().info("HEAD is at {}", context.getModel().getCommit().getShortHash());
    }
}
