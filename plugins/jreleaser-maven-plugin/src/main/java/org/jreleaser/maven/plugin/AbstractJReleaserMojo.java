/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.maven.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jreleaser.engine.context.ContextCreator;
import org.jreleaser.maven.plugin.internal.JReleaserLoggerAdapter;
import org.jreleaser.maven.plugin.internal.JReleaserModelConfigurer;
import org.jreleaser.maven.plugin.internal.JReleaserModelConverter;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.util.JReleaserLogger;
import org.jreleaser.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractJReleaserMojo extends AbstractMojo {
    /**
     * The project whose model will be checked.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter
    protected Jreleaser jreleaser;

    @Parameter(property = "jreleaser.output.directory", defaultValue = "${project.build.directory}/jreleaser")
    protected File outputDirectory;

    @Parameter(property = "jreleaser.config.file")
    protected File configFile;

    /**
     * Skips remote operations.
     */
    @Parameter(property = "jreleaser.dryrun")
    protected boolean dryrun;

    /**
     * Searches for the Git root.
     */
    @Parameter(property = "jreleaser.git.root.search")
    protected boolean gitRootSearch;

    @Parameter(defaultValue = "${session}", required = true)
    private MavenSession session;

    @Parameter(defaultValue = "${maven.multiModuleProjectDirectory}")
    private String multiModuleProjectDirectory;

    protected JReleaserLogger getLogger() throws MojoExecutionException {
        return new JReleaserLoggerAdapter(createTracer(), getLog());
    }

    protected PrintWriter createTracer() throws MojoExecutionException {
        try {
            java.nio.file.Files.createDirectories(outputDirectory.toPath());
            return new PrintWriter(new FileOutputStream(
                outputDirectory.toPath().resolve("trace.log").toFile()));
        } catch (IOException e) {
            throw new MojoExecutionException("Could not initialize trace file", e);
        }
    }

    protected JReleaserModel convertModel() {
        JReleaserModel jreleaserModel = JReleaserModelConverter.convert(jreleaser != null ? jreleaser : new Jreleaser());
        return JReleaserModelConfigurer.configure(jreleaserModel, project, session);
    }

    protected JReleaserModel readModel(JReleaserLogger logger) {
        JReleaserModel jreleaserModel = ContextCreator.resolveModel(logger, configFile.toPath());
        return JReleaserModelConfigurer.configure(jreleaserModel, project, session);
    }

    protected JReleaserContext createContext() throws MojoExecutionException {
        try {
            JReleaserLogger logger = getLogger();
            Path basedir = resolveBasedir();

            logger.info("JReleaser {}", JReleaserVersion.getPlainVersion());
            JReleaserVersion.banner(logger.getTracer(), false);
            if (null != configFile) {
                logger.info("Configuring with {}", configFile.getAbsolutePath());
            }
            logger.increaseIndent();
            logger.info("- basedir set to {}", basedir.toAbsolutePath());
            logger.decreaseIndent();

            return ContextCreator.create(
                logger,
                resolveConfigurer(configFile),
                getMode(),
                null == configFile ? convertModel() : readModel(logger),
                basedir,
                outputDirectory.toPath(),
                dryrun,
                gitRootSearch,
                collectSelectedPlatforms());
        } catch (JReleaserException e) {
            throw new MojoExecutionException("JReleaser for project " + project.getArtifactId() + " has not been properly configured.", e);
        }
    }

    protected JReleaserContext.Configurer resolveConfigurer(File configFile) {
        if (null == configFile) return JReleaserContext.Configurer.MAVEN;

        switch (StringUtils.getFilenameExtension(configFile.getName())) {
            case "yml":
            case "yaml":
                return JReleaserContext.Configurer.CLI_YAML;
            case "toml":
                return JReleaserContext.Configurer.CLI_TOML;
            case "json":
                return JReleaserContext.Configurer.CLI_JSON;
        }
        // should not happen!
        throw new IllegalArgumentException("Invalid configuration format: " + configFile.getName());
    }

    protected JReleaserContext.Mode getMode() {
        return JReleaserContext.Mode.FULL;
    }

    private Path resolveBasedir() {
        if (isNotBlank(multiModuleProjectDirectory)) {
            return Paths.get(multiModuleProjectDirectory.trim());
        } else if (isNotBlank(session.getExecutionRootDirectory())) {
            return Paths.get(session.getExecutionRootDirectory().trim());
        }
        return project.getBasedir().toPath();
    }

    protected List<String> collectSelectedPlatforms() {
        return Collections.emptyList();
    }
}
