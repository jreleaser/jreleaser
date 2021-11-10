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
package org.jreleaser.cli;

import org.jreleaser.config.JReleaserConfigParser;
import org.jreleaser.engine.context.ContextCreator;
import org.jreleaser.model.Environment;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.util.StringUtils;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

import static org.jreleaser.util.FileUtils.resolveOutputDirectory;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command
public abstract class AbstractModelCommand extends AbstractCommand {
    @CommandLine.Option(names = {"-c", "--config-file"})
    Path configFile;

    @CommandLine.Option(names = {"-grs", "--git-root-search"})
    boolean gitRootSearch;

    @CommandLine.Option(names = {"-p", "--set-property"},
        paramLabel = "<key=value>")
    String[] properties;

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
        initLogger();
        logger.info("JReleaser {}", JReleaserVersion.getPlainVersion());
        JReleaserVersion.banner(logger.getTracer(), false);
        logger.info($("TEXT_config_file"), actualConfigFile);
        logger.increaseIndent();
        logger.info($("TEXT_basedir_set"), actualBasedir.toAbsolutePath());
        logger.decreaseIndent();
        doExecute(createContext());
    }

    private void resolveConfigFile() {
        if (null != configFile) {
            actualConfigFile = configFile;
        } else {
            Path directory = Paths.get(".").normalize();
            Optional<Path> file = resolveConfigFileAt(directory);
            if (!file.isPresent() && basedir != null) {
                file = resolveConfigFileAt(basedir);
            }
            actualConfigFile = file.orElse(null);
        }

        if (null == actualConfigFile || !Files.exists(actualConfigFile)) {
            spec.commandLine().getErr()
                .println(spec.commandLine()
                    .getColorScheme()
                    .errorText($("ERROR_missing_config_file",
                        String.join("|", getSupportedConfigFormats())
                    )));
            spec.commandLine().usage(parent.out);
            throw new HaltExecutionException();
        }
    }

    private Optional<Path> resolveConfigFileAt(Path directory) {
        ServiceLoader<JReleaserConfigParser> parsers = ServiceLoader.load(JReleaserConfigParser.class,
            JReleaserConfigParser.class.getClassLoader());

        for (JReleaserConfigParser parser : parsers) {
            Path file = directory.resolve("jreleaser." + parser.getPreferredFileExtension());
            if (Files.exists(file)) {
                return Optional.of(file);
            }
        }

        return Optional.empty();
    }

    private void resolveBasedir() {
        actualBasedir = null != basedir ? basedir : actualConfigFile.toAbsolutePath().getParent();
        if (!Files.exists(actualBasedir)) {
            spec.commandLine().getErr()
                .println(spec.commandLine().getColorScheme().errorText(
                    $("ERROR_missing_required_option", "--basedir=<basedir>")));
            spec.commandLine().usage(parent.out);
            throw new HaltExecutionException();
        }
    }

    protected abstract void doExecute(JReleaserContext context);

    protected JReleaserContext createContext() {
        JReleaserModel model = ContextCreator.resolveModel(logger, actualConfigFile);
        Environment.PropertiesSource propertiesSource = new Environment.PropertiesPropertiesSource(collectProperties());
        model.getEnvironment().setPropertiesSource(propertiesSource);

        return ContextCreator.create(
            logger,
            resolveConfigurer(actualConfigFile),
            getMode(),
            model,
            actualBasedir,
            getOutputDirectory(),
            dryrun(),
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
        throw new IllegalArgumentException($("ERROR_invalid_config_format", configFile.getFileName()));
    }

    protected Path getOutputDirectory() {
        return resolveOutputDirectory(actualBasedir, outputdir, "out");
    }

    protected boolean dryrun() {
        return false;
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

    protected Properties collectProperties() {
        Properties props = new Properties();

        if (properties != null && properties.length > 0) {
            for (String property : properties) {
                if (property.contains("=")) {
                    int d = property.indexOf('=');
                    if (d == 0 || d == properties.length - 1) {
                        throw new IllegalArgumentException($("ERROR_invalid_property", property));
                    }
                    props.put(property.substring(0, d),
                        property.substring(d + 1));
                } else {
                    props.put(property, Boolean.TRUE);
                }
            }
        }

        return props;
    }
}
