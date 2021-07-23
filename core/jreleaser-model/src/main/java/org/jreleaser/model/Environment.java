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
    private VariablesSource variablesSource;
    private String variables;
    private Properties props;
    private Path propertiesFile;

    void setAll(Environment environment) {
        this.variablesSource = environment.variablesSource;
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

    public VariablesSource getVariablesSource() {
        return variablesSource;
    }

    public void setVariablesSource(VariablesSource variablesSource) {
        this.variablesSource = variablesSource;
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

    public Path getPropertiesFile() {
        return propertiesFile;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
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

            if (null != variablesSource) {
                props.putAll(variablesSource.getVariables());
            }
        }
    }

    private void loadProperties(JReleaserContext context, Path file) {
        propertiesFile = file;
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

    public interface VariablesSource {
        Map<String, String> getVariables();
    }

    public static abstract class AbstractVariablesSource implements VariablesSource {
        @Override
        public Map<String, String> getVariables() {
            Map<String, String> variables = doGetVariables();
            Map<String, String> map = new LinkedHashMap<>();

            variables.forEach((key, value) -> {
                if (key.startsWith("jreleaser.")) {
                    map.put(key.replace(".", "_").toUpperCase(), value);
                }
            });

            variables.forEach((key, value) -> {
                if (key.startsWith("JRELEASER_")) {
                    map.put(key, value);
                }
            });

            return map;
        }

        protected abstract Map<String, String> doGetVariables();
    }

    public static class PropertiesVariablesSource extends AbstractVariablesSource {
        private final Properties properties;

        public PropertiesVariablesSource(Properties properties) {
            this.properties = properties;
        }

        @Override
        protected Map<String, String> doGetVariables() {
            Map<String, String> map = new LinkedHashMap<>();
            properties.forEach((k, v) -> map.put(String.valueOf(k), String.valueOf(v)));
            return map;
        }
    }

    public static class MapVariablesSource extends AbstractVariablesSource {
        private final Map<String, ?> properties;

        public MapVariablesSource(Map<String, ?> properties) {
            this.properties = properties;
        }

        @Override
        protected Map<String, String> doGetVariables() {
            Map<String, String> map = new LinkedHashMap<>();
            properties.forEach((k, v) -> map.put(k, String.valueOf(v)));
            return map;
        }
    }
}
