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
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserVersion;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command
public abstract class AbstractModelCommand extends AbstractCommand {
    @CommandLine.Option(names = {"-c", "--config-file"},
        description = "The config file")
    Path configFile;

    @CommandLine.Option(names = {"-grs", "--git-root-search"},
        description = "Searches for the Git root.")
    boolean gitRootSearch;

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
        logger.info("Configuring with {}", actualConfigFile);
        logger.increaseIndent();
        logger.info("- basedir set to {}", actualBasedir.toAbsolutePath());
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
                    .errorText("Missing required option: '--config-file=<configFile>' " +
                        "or local file named jreleaser[" +
                        String.join("|", getSupportedConfigFormats()) + "]"));
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
                .println(spec.commandLine().getColorScheme().errorText("Missing required option: '--basedir=<basedir>'"));
            spec.commandLine().usage(parent.out);
            throw new HaltExecutionException();
        }
    }

    protected abstract void doExecute(JReleaserContext context);

    protected JReleaserContext createContext() {
        return ContextCreator.create(
            logger,
            getMode(),
            actualConfigFile,
            actualBasedir,
            getOutputDirectory(),
            dryrun(),
            gitRootSearch);
    }

    protected Path getOutputDirectory() {
        if (null != outputdir) {
            return actualBasedir.resolve(outputdir).resolve("jreleaser");
        }
        return actualBasedir.resolve("out").resolve("jreleaser");
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
}
