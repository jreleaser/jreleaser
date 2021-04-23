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
package org.jreleaser.model;

import org.jreleaser.config.JReleaserConfigLoader;
import org.jreleaser.config.JReleaserConfigParser;
import org.jreleaser.util.Env;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Environment implements Domain {
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private String variables;
    private Properties props;

    void setAll(Environment environment) {
        this.variables = environment.variables;
        setProperties(environment.properties);
    }

    public String getVariable(String key) {
        return props.getProperty(Env.prefix(key));
    }

    public boolean isSet() {
        return isNotBlank(variables) ||
            !properties.isEmpty();
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties.putAll(properties);
    }

    @Override
    public final Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("variables", variables);
        map.put("properties", properties);

        return map;
    }

    public void initProps(JReleaserContext context) {
        if (null == props) {
            props = new Properties();

            if (isNotBlank(variables)) {
                loadProperties(context, context.getBasedir().resolve(variables.trim()));
                return;
            }

            String home = System.getenv("JRELEASER_USER_HOME");
            if (isBlank(home)) {
                home = System.getProperty("user.home");
            }

            Path configDirectory = Paths.get(home).resolve(".jreleaser");
            loadProperties(context, resolveConfigFileAt(configDirectory)
                .orElse(configDirectory.resolve("config.properties")));
        }
    }

    private void loadProperties(JReleaserContext context, Path file) {
        context.getLogger().info("Loading properties from {}", file.toAbsolutePath());
        if (Files.exists(file)) {
            try {
                if (file.getFileName().toString().endsWith(".properties")) {
                    props.load(new FileInputStream(file.toFile()));
                } else {
                    props.putAll(JReleaserConfigLoader.loadProperties(file));
                }
            } catch (IOException e) {
                context.getLogger().debug("Could not load properties from {}", file.toAbsolutePath(), e);
            }
        } else {
            context.getLogger().warn("Properties source {} does not exist", file.toAbsolutePath());
        }
    }

    private Optional<Path> resolveConfigFileAt(Path directory) {
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
