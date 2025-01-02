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
package org.jreleaser.engine.environment;

import org.jreleaser.bundle.RB;
import org.jreleaser.config.JReleaserConfigLoader;
import org.jreleaser.config.JReleaserConfigParser;
import org.jreleaser.logging.JReleaserLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;

import static java.nio.file.Files.newInputStream;
import static org.jreleaser.model.Constants.DEFAULT_GIT_REMOTE;
import static org.jreleaser.model.Constants.JRELEASER_USER_HOME;
import static org.jreleaser.model.Constants.XDG_CONFIG_HOME;
import static org.jreleaser.util.Env.JRELEASER_ENV_PREFIX;
import static org.jreleaser.util.Env.envKey;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class Environment {
    private Environment() {
        // noop
    }

    public static void display(JReleaserLogger logger, Path basedir) {
        Path configDirectory = null;

        String home = System.getenv(XDG_CONFIG_HOME);
        if (isNotBlank(home) && Files.exists(Paths.get(home).resolve("jreleaser"))) {
            configDirectory = Paths.get(home).resolve("jreleaser");
        }

        if (null == configDirectory) {
            home = System.getenv(JRELEASER_USER_HOME);
            if (isBlank(home)) {
                home = System.getProperty("user.home") + File.separator + ".jreleaser";
            }
            configDirectory = Paths.get(home);
        }

        loadVariables(logger, resolveConfigFileAt(configDirectory)
            .orElse(configDirectory.resolve("config.properties")));

        Path envFilePath = basedir.resolve(".env");
        if (Files.exists(envFilePath)) {
            loadVariables(logger, envFilePath);
        }

        Set<String> vars = new TreeSet<>();
        System.getenv().forEach((k, v) -> {
            if (k.startsWith(JRELEASER_ENV_PREFIX)) vars.add(k);
        });
        if (System.getenv().containsKey(envKey(DEFAULT_GIT_REMOTE))) {
            vars.add(envKey(DEFAULT_GIT_REMOTE));
        }

        if (!vars.isEmpty()) {
            logger.info(RB.$("environment.variables.env"));
            vars.forEach(message -> logger.info("  " + message));
        }
    }

    private static void loadVariables(JReleaserLogger logger, Path file) {
        Set<String> vars = new TreeSet<>();

        Properties p = new Properties();
        logger.info(RB.$("environment.load.variables"), file.toAbsolutePath());
        if (Files.exists(file)) {
            try {
                if (file.getFileName().toString().endsWith(".properties") ||
                    file.getFileName().toString().equals(".env")) {
                    try (InputStream in = newInputStream(file)) {
                        p.load(in);
                    }
                } else {
                    p.putAll(JReleaserConfigLoader.loadProperties(file));
                }
            } catch (IOException e) {
                logger.debug(RB.$("environment.variables.load.error"), file.toAbsolutePath(), e);
            }
        } else {
            logger.warn(RB.$("environment.variables.source.missing"), file.toAbsolutePath());
        }

        p.stringPropertyNames().stream()
            .filter(k -> k.startsWith(JRELEASER_ENV_PREFIX)).
            forEach(vars::add);

        if (!vars.isEmpty()) {
            logger.info(RB.$("environment.variables.file", file.getFileName().toString()));
            vars.forEach(message -> logger.info("  " + message));
        }
    }

    private static Optional<Path> resolveConfigFileAt(Path directory) {
        ServiceLoader<JReleaserConfigParser> parsers = ServiceLoader.load(JReleaserConfigParser.class,
            JReleaserConfigParser.class.getClassLoader());

        for (JReleaserConfigParser parser : parsers) {
            Path file = directory.resolve("config." + parser.getPreferredFileExtension());
            if (Files.exists(file)) {
                return Optional.of(file);
            }
        }

        return Optional.empty();
    }
}
