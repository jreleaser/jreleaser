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
package org.jreleaser.cli;

import org.jreleaser.config.JReleaserConfigParser;
import org.jreleaser.engine.context.ContextCreator;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.model.internal.environment.Environment;
import org.jreleaser.util.Env;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.StringUtils;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
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
public abstract class AbstractModelCommand<C extends IO> extends AbstractLoggingCommand<C> {
    @CommandLine.Option(names = {"-c", "--config-file"}, paramLabel = "<file>")
    Path configFile;

    @CommandLine.Option(names = {"-grs", "--git-root-search"})
    Boolean gitRootSearch;

    @CommandLine.Option(names = {"--strict"})
    Boolean strict;

    @CommandLine.Option(names = {"-P", "--set-property"},
        paramLabel = "<key=value>")
    String[] properties;

    Path actualConfigFile;
    Path actualBasedir;

    @Override
    protected void collectCandidateDeprecatedArgs(Set<AbstractCommand.DeprecatedArg> args) {
        super.collectCandidateDeprecatedArgs(args);
        args.add(new DeprecatedArg("-grs", "--git-root-search", "1.5.0"));
    }

    @Override
    protected void execute() {
        resolveConfigFile();
        resolveBasedir();
        initLogger();
        PlatformUtils.resolveCurrentPlatform(logger);
        logger.info("JReleaser {}", JReleaserVersion.getPlainVersion());
        JReleaserVersion.banner(logger.getTracer());
        logger.info($("TEXT_config_file"), actualConfigFile);
        logger.increaseIndent();
        logger.info($("TEXT_basedir_set"), actualBasedir.toAbsolutePath());
        logger.info($("TEXT_outputdir_set"), getOutputDirectory().toAbsolutePath());
        logger.decreaseIndent();
        doExecute(createContext());
    }

    private void resolveConfigFile() {
        if (null != configFile) {
            actualConfigFile = configFile.normalize();
        } else {
            Path directory = Paths.get(".").normalize();
            Optional<Path> file = resolveConfigFileAt(directory);
            if (!file.isPresent() && null != basedir) {
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
            spec.commandLine().usage(parent().getOut());
            throw new HaltExecutionException();
        }
    }

    private Optional<Path> resolveConfigFileAt(Path directory) {
        ServiceLoader<JReleaserConfigParser> parsers = ServiceLoader.load(JReleaserConfigParser.class,
            JReleaserConfigParser.class.getClassLoader());

        for (JReleaserConfigParser parser : parsers) {
            Path file = directory.resolve("jreleaser." + parser.getPreferredFileExtension()).normalize();
            if (Files.exists(file)) {
                return Optional.of(file);
            }
        }

        return Optional.empty();
    }

    private void resolveBasedir() {
        String resolvedBasedir = Env.resolve(org.jreleaser.model.api.JReleaserContext.BASEDIR, null != basedir ? basedir.toString() : "");
        actualBasedir = (isNotBlank(resolvedBasedir) ? Paths.get(resolvedBasedir) : actualConfigFile.toAbsolutePath().getParent()).normalize();
        if (!Files.exists(actualBasedir)) {
            spec.commandLine().getErr()
                .println(spec.commandLine().getColorScheme().errorText(
                    $("ERROR_missing_required_option", "--basedir=<basedir>")));
            spec.commandLine().usage(parent().getOut());
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
            getCommand(),
            model,
            actualBasedir,
            getOutputDirectory(),
            resolveBoolean(org.jreleaser.model.api.JReleaserContext.DRY_RUN, dryrun()),
            resolveBoolean(org.jreleaser.model.api.JReleaserContext.GIT_ROOT_SEARCH, gitRootSearch()),
            resolveBoolean(org.jreleaser.model.api.JReleaserContext.STRICT, strict()),
            collectSelectedPlatforms(),
            collectRejectedPlatforms());
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

    protected JReleaserContext.Configurer resolveConfigurer(Path configFile) {
        switch (StringUtils.getFilenameExtension(configFile.getFileName().toString())) {
            case "yml":
            case "yaml":
                return JReleaserContext.Configurer.CLI_YAML;
            case "toml":
                return JReleaserContext.Configurer.CLI_TOML;
            case "json":
                return JReleaserContext.Configurer.CLI_JSON;
            default:
                // should not happen!
                throw new IllegalArgumentException($("ERROR_invalid_config_format", configFile.getFileName()));
        }
    }

    @Override
    protected Path getOutputDirectory() {
        return resolveOutputDirectory(actualBasedir, outputdir, "out");
    }

    protected Boolean dryrun() {
        return false;
    }

    protected Boolean strict() {
        return strict;
    }

    protected Boolean gitRootSearch() {
        return gitRootSearch;
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

    protected Mode getMode() {
        return Mode.FULL;
    }

    protected abstract JReleaserCommand getCommand();

    protected List<String> collectSelectedPlatforms() {
        return Collections.emptyList();
    }

    protected List<String> collectRejectedPlatforms() {
        return Collections.emptyList();
    }

    protected Properties collectProperties() {
        Properties props = new Properties();

        if (null != properties && properties.length > 0) {
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
