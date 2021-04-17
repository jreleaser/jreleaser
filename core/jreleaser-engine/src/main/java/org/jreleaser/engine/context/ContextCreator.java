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
package org.jreleaser.engine.context;

import org.jreleaser.config.JReleaserConfigLoader;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.JReleaserModelValidator;
import org.jreleaser.model.Project;
import org.jreleaser.util.Constants;
import org.jreleaser.util.JReleaserLogger;
import org.jreleaser.util.Version;

import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ContextCreator {
    public static JReleaserContext create(JReleaserLogger logger,
                                          JReleaserContext.Mode mode,
                                          Path configFile,
                                          Path basedir,
                                          Path outputDirectory,
                                          boolean dryrun) {
        return create(logger,
            mode,
            resolveModel(logger, configFile),
            basedir,
            outputDirectory,
            dryrun);
    }

    public static JReleaserContext create(JReleaserLogger logger,
                                          JReleaserContext.Mode mode,
                                          JReleaserModel model,
                                          Path basedir,
                                          Path outputDirectory,
                                          boolean dryrun) {
        JReleaserContext context = new JReleaserContext(
            logger,
            mode,
            model,
            basedir,
            outputDirectory,
            dryrun);

        ModelAutoConfigurer.autoConfigure(context);

        report(context);

        return context;
    }

    private static JReleaserModel resolveModel(JReleaserLogger logger, Path configFile) {
        try {
            logger.info("Reading configuration");
            return JReleaserConfigLoader.loadConfig(configFile);
        } catch (JReleaserException e) {
            logger.trace(e);
            throw e;
        } catch (Exception e) {
            logger.trace(e);
            throw new JReleaserException("Unexpected error when parsing configuration from " + configFile.toAbsolutePath(), e);
        }
    }

    private static void report(JReleaserContext context) {
        String version = context.getModel().getProject().getVersion();
        parseVersion(version, context.getModel().getProject());

        context.getLogger().info("Project version set to {}", version);
        context.getLogger().info("Release is{}snapshot", context.getModel().getProject().isSnapshot() ? " " : " not ");
        context.getLogger().info("Timestamp is {}", context.getModel().getTimestamp());
        context.getLogger().info("HEAD is at {}", context.getModel().getCommit().getShortHash());
    }

    private static void parseVersion(String version, Project project) {
        Version parsedVersion = Version.of(version);

        project.addExtraProperty(Constants.KEY_VERSION_MAJOR, parsedVersion.getMajor());
        if (parsedVersion.hasMinor()) project.addExtraProperty(Constants.KEY_VERSION_MINOR, parsedVersion.getMinor());
        if (parsedVersion.hasPatch()) project.addExtraProperty(Constants.KEY_VERSION_PATCH, parsedVersion.getPatch());
        if (parsedVersion.hasTag()) project.addExtraProperty(Constants.KEY_VERSION_TAG, parsedVersion.getTag());
        if (parsedVersion.hasBuild()) project.addExtraProperty(Constants.KEY_VERSION_BUILD, parsedVersion.getBuild());
    }
}
