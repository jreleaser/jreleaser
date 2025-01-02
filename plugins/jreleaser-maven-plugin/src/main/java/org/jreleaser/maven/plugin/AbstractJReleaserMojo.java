/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
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
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jreleaser.engine.context.ContextCreator;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.maven.plugin.internal.JReleaserLoggerAdapter;
import org.jreleaser.maven.plugin.internal.JReleaserModelConfigurer;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.util.Env;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.JReleaserOutput.JRELEASER_QUIET;
import static org.jreleaser.util.IoUtils.newPrintWriter;
import static org.jreleaser.util.StringUtils.isBlank;
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
    protected JReleaserModel jreleaser;

    @Parameter(property = "jreleaser.output.directory", defaultValue = "${project.build.directory}/jreleaser")
    protected File outputDirectory;

    @Parameter(property = "jreleaser.config.file")
    protected File configFile;

    /**
     * Skips remote operations.
     */
    @Parameter(property = "jreleaser.dry.run")
    protected Boolean dryrun;

    /**
     * Searches for the Git root.
     */
    @Parameter(property = "jreleaser.git.root.search")
    protected Boolean gitRootSearch;

    /**
     * Enable strict mode.
     */
    @Parameter(property = "jreleaser.strict")
    protected Boolean strict;

    @Parameter(defaultValue = "${session}", required = true)
    private MavenSession session;

    @Parameter(defaultValue = "${maven.multiModuleProjectDirectory}")
    private String multiModuleProjectDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Banner.display(project, getLog());
        if (isSkip()) {
            getLog().info("Execution has been explicitly skipped.");
            return;
        }

        doExecute();
    }

    protected abstract boolean isSkip();

    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

    protected boolean isQuiet() {
        return getLog().isErrorEnabled() &&
            !getLog().isWarnEnabled() &&
            !getLog().isInfoEnabled() &&
            !getLog().isDebugEnabled();
    }

    protected JReleaserLogger getLogger() throws MojoExecutionException {
        return new JReleaserLoggerAdapter(createTracer(), getLog());
    }

    protected PrintWriter createTracer() throws MojoExecutionException {
        try {
            java.nio.file.Files.createDirectories(outputDirectory.toPath());
            return newPrintWriter(new FileOutputStream(
                outputDirectory.toPath().resolve("trace.log").toFile()));
        } catch (IOException e) {
            throw new MojoExecutionException("Could not initialize trace file", e);
        }
    }

    protected JReleaserModel convertModel() {
        JReleaserModel jreleaserModel = null != jreleaser ? jreleaser : new JReleaserModel();
        return JReleaserModelConfigurer.configure(jreleaserModel, project, session);
    }

    protected JReleaserModel readModel(JReleaserLogger logger) {
        JReleaserModel jreleaserModel = ContextCreator.resolveModel(logger, configFile.toPath());
        return JReleaserModelConfigurer.configure(jreleaserModel, project, session);
    }

    protected JReleaserContext createContext() throws MojoExecutionException {
        try {
            if (isQuiet()) {
                System.setProperty(JRELEASER_QUIET, "true");
            }

            JReleaserLogger logger = getLogger();
            PlatformUtils.resolveCurrentPlatform(logger);
            Path basedir = resolveBasedir();

            logger.info("JReleaser {}", JReleaserVersion.getPlainVersion());
            JReleaserVersion.banner(logger.getTracer());
            if (null != configFile) {
                logger.info("Configuring with {}", configFile.getAbsolutePath());
            }
            logger.increaseIndent();
            logger.info("- basedir set to {}", basedir.toAbsolutePath());
            logger.info("- outputdir set to {}", outputDirectory.toPath().toAbsolutePath());
            logger.decreaseIndent();

            return ContextCreator.create(
                logger,
                resolveConfigurer(configFile),
                getMode(),
                getCommand(),
                null == configFile ? convertModel() : readModel(logger),
                basedir,
                outputDirectory.toPath(),
                resolveBoolean(org.jreleaser.model.api.JReleaserContext.DRY_RUN, dryrun),
                resolveBoolean(org.jreleaser.model.api.JReleaserContext.GIT_ROOT_SEARCH, gitRootSearch),
                resolveBoolean(org.jreleaser.model.api.JReleaserContext.STRICT, strict),
                collectSelectedPlatforms(),
                collectRejectedPlatforms());
        } catch (JReleaserException e) {
            throw new MojoExecutionException("JReleaser for project " + project.getArtifactId() + " has not been properly configured.", e);
        }
    }

    protected boolean resolveBoolean(String key, Boolean value) {
        if (null != value) return value;
        String resolvedValue = Env.resolve(key, "");
        return isNotBlank(resolvedValue) && Boolean.parseBoolean(resolvedValue);
    }

    protected List<String> resolveCollection(String key, List<String> values) {
        if (!values.isEmpty()) return values;
        String resolvedValue = Env.resolve(key, "");
        if (isBlank(resolvedValue)) return Collections.emptyList();
        return Arrays.stream(resolvedValue.trim().split(","))
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .collect(toList());
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
            default:
                // should not happen!
                throw new IllegalArgumentException("Invalid configuration format: " + configFile.getName());
        }
    }

    protected Mode getMode() {
        return Mode.FULL;
    }

    protected abstract JReleaserCommand getCommand();

    private Path resolveBasedir() {
        String resolvedBasedir = Env.resolve(org.jreleaser.model.api.JReleaserContext.BASEDIR, "");
        if (isNotBlank(resolvedBasedir)) {
            return Paths.get(resolvedBasedir.trim());
        } else if (isNotBlank(multiModuleProjectDirectory)) {
            return Paths.get(multiModuleProjectDirectory.trim());
        } else if (isNotBlank(session.getExecutionRootDirectory())) {
            return Paths.get(session.getExecutionRootDirectory().trim());
        }
        return project.getBasedir().toPath();
    }

    protected List<String> collectSelectedPlatforms() {
        return Collections.emptyList();
    }

    protected List<String> collectRejectedPlatforms() {
        return Collections.emptyList();
    }

    protected List<String> collectEntries(String[] input) {
        return collectEntries(input, false);
    }

    protected List<String> collectEntries(String[] input, boolean lowerCase) {
        List<String> list = new ArrayList<>();
        if (null != input && input.length > 0) {
            for (String s : input) {
                if (isNotBlank(s)) {
                    if (!s.contains("-") && lowerCase) {
                        s = StringUtils.getHyphenatedName(s);
                    }
                    list.add(lowerCase ? s.toLowerCase(Locale.ENGLISH) : s);
                }
            }
        }
        return list;
    }
}
