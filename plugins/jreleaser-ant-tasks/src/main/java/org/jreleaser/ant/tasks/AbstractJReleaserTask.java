/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
import org.jreleaser.engine.context.ContextCreator;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.util.JReleaserLogger;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.util.FileUtils.resolveOutputDirectory;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractJReleaserTask extends Task {
    protected File basedir;
    protected File configFile;
    protected boolean dryrun;
    protected boolean gitRootSearch;
    protected boolean skip;
    protected Path outputDir;

    protected JReleaserLogger logger;
    protected Path actualConfigFile;
    protected Path actualBasedir;

    public void setBasedir(File basedir) {
        this.basedir = basedir;
    }

    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }

    public void setDryrun(boolean dryrun) {
        this.dryrun = dryrun;
    }

    public void setGitRootSearch(boolean gitRootSearch) {
        this.gitRootSearch = gitRootSearch;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public void setOutputDir(Path outputDir) {
        this.outputDir = outputDir;
    }

    @Override
    public void execute() throws BuildException {
        Banner.display(new PrintWriter(System.out, true));
        if (skip) return;

        resolveConfigFile();
        resolveBasedir();
        initLogger();
        PlatformUtils.resolveCurrentPlatform(logger);
        logger.info("JReleaser {}", JReleaserVersion.getPlainVersion());
        JReleaserVersion.banner(logger.getTracer());
        logger.info("Configuring with {}", actualConfigFile);
        logger.info(" - basedir set to {}", actualBasedir.toAbsolutePath());
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
                "or local file named jreleaser[" +
                String.join("|", getSupportedConfigFormats()) + "]");
        }
    }

    private void resolveBasedir() {
        actualBasedir = (null != basedir ? basedir.toPath() : actualConfigFile.toAbsolutePath().getParent()).normalize();
    }

    protected abstract void doExecute(JReleaserContext context);

    protected JReleaserLogger initLogger() {
        if (null == logger) {
            logger = new JReleaserLoggerAdapter(createTracer(), getProject());
        }
        return logger;
    }

    protected PrintWriter createTracer() {
        try {
            Files.createDirectories(getOutputDirectory());
            return new PrintWriter(new FileOutputStream(
                getOutputDirectory().resolve("trace.log").toFile()),
                true);
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize trace file", e);
        }
    }

    protected Path getOutputDirectory() {
        return resolveOutputDirectory(actualBasedir, outputDir, "build");
    }

    protected JReleaserContext createContext() {
        return ContextCreator.create(
            logger,
            resolveConfigurer(actualConfigFile),
            getMode(),
            actualConfigFile,
            actualBasedir,
            getOutputDirectory(),
            dryrun,
            gitRootSearch,
            collectSelectedPlatforms());
    }

    protected JReleaserContext.Configurer resolveConfigurer(Path configFile) {
        switch (StringUtils.getFilenameExtension(configFile.getFileName().toString())) {
            case "yml":
            case "yaml":
                return JReleaserContext.Configurer.CLI_YAML;
            case "toml":
                return JReleaserContext.Configurer.CLI_TOML;
            case "json":
                return JReleaserContext.Configurer.CLI_JSON;
        }
        // should not happen!
        throw new IllegalArgumentException("Invalid configuration format: " + configFile.getFileName());
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

    protected JReleaserContext.Mode getMode() {
        return JReleaserContext.Mode.FULL;
    }

    protected List<String> collectSelectedPlatforms() {
        return Collections.emptyList();
    }

    protected List<String> collectEntries(String[] input) {
        return collectEntries(input, false);
    }

    protected List<String> collectEntries(String[] input, boolean lowerCase) {
        List<String> list = new ArrayList<>();
        if (input != null && input.length > 0) {
            for (String s : input) {
                if (isNotBlank(s)) {
                    s = s.trim();
                    list.add(lowerCase ? s.toLowerCase(Locale.ENGLISH) : s);
                }
            }
        }
        return list;
    }

    protected Collection<String> expandAndCollect(String input) {
        if (isBlank(input)) return Collections.emptyList();

        if (input.contains(",")) {
            return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(toList());
        }

        return Collections.singletonList(input.trim());
    }
}
