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
import org.jreleaser.model.Project;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.util.Constants;
import org.jreleaser.util.JReleaserLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ContextCreator {
    private static final Pattern FULL_SEMVER_PATTERN = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:[\\.\\-]((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");
    private static final Pattern MAJOR_MINOR_PATTERN = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:[\\.\\-]((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");
    private static final Pattern MAJOR_PATTERN = Pattern.compile("^(0|[1-9]\\d*)(?:[\\.\\-]((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

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

        try {
            if (!context.validateModel().isEmpty()) {
                throw new JReleaserException("JReleaser with " + configFile.toAbsolutePath() + " has not been properly configured.");
            }
        } catch (IllegalArgumentException e) {
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

        try {
            if (!context.validateModel().isEmpty()) {
                throw new JReleaserException("JReleaser has not been properly configured.");
            }
        } catch (IllegalArgumentException e) {
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
        String version = context.getModel().getProject().getResolvedVersion();
        parseVersion(version, context.getModel().getProject());

        context.getLogger().info("Project version set to {}", version);
        context.getLogger().info("Release is{}snapshot", context.getModel().getProject().isSnapshot() ? " " : " not ");
        context.getLogger().info("Timestamp is {}", context.getModel().getTimestamp());
        context.getLogger().info("HEAD is at {}", context.getModel().getCommit().getShortHash());
    }

    private static void parseVersion(String version, Project project) {
        Matcher m = FULL_SEMVER_PATTERN.matcher(version);

        if (m.matches()) {
            project.addExtraProperty(Constants.KEY_VERSION_MAJOR, m.group(1));
            project.addExtraProperty(Constants.KEY_VERSION_MINOR, m.group(2));
            project.addExtraProperty(Constants.KEY_VERSION_PATCH, m.group(3));
            project.addExtraProperty(Constants.KEY_VERSION_TAG, m.group(4));
            project.addExtraProperty(Constants.KEY_VERSION_BUILD, m.group(5));
            return;
        }

        m = MAJOR_MINOR_PATTERN.matcher(version);
        if (m.matches()) {
            project.addExtraProperty(Constants.KEY_VERSION_MAJOR, m.group(1));
            project.addExtraProperty(Constants.KEY_VERSION_MINOR, m.group(2));
            project.addExtraProperty(Constants.KEY_VERSION_TAG, m.group(3));
            project.addExtraProperty(Constants.KEY_VERSION_BUILD, m.group(4));
            return;
        }

        m = MAJOR_PATTERN.matcher(version);
        if (m.matches()) {
            project.addExtraProperty(Constants.KEY_VERSION_MAJOR, m.group(1));
            project.addExtraProperty(Constants.KEY_VERSION_TAG, m.group(2));
            project.addExtraProperty(Constants.KEY_VERSION_BUILD, m.group(3));
        }
    }
}
