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

import org.jreleaser.bundle.RB;
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

import static org.jreleaser.util.StringUtils.getPropertyNameForLowerCaseHyphenSeparatedName;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Environment implements Domain {
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private PropertiesSource propertiesSource;
    private String variables;
    private Properties vars;
    private Path propertiesFile;

    void setAll(Environment environment) {
        this.variables = environment.variables;
        setProperties(environment.properties);
        setPropertiesSource(environment.propertiesSource);
    }

    public String getVariable(String key) {
        return vars.getProperty(Env.prefix(key));
    }

    public boolean isSet() {
        return isNotBlank(variables) ||
            !properties.isEmpty();
    }

    public PropertiesSource getPropertiesSource() {
        return propertiesSource;
    }

    public void setPropertiesSource(PropertiesSource propertiesSource) {
        this.propertiesSource = propertiesSource;
        if (null != this.propertiesSource) {
            properties.putAll(propertiesSource.getProperties());
        }
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
        if (null == vars) {
            vars = new Properties();

            String home = System.getenv("JRELEASER_USER_HOME");
            if (isBlank(home)) {
                home = System.getProperty("user.home");
            }

            Path configDirectory = Paths.get(home).resolve(".jreleaser");
            loadVariables(context, resolveConfigFileAt(configDirectory)
                .orElse(configDirectory.resolve("config.properties")));

            if (isNotBlank(variables)) {
                loadVariables(context, context.getBasedir().resolve(variables.trim()));
            }

            if (null != propertiesSource) {
                properties.putAll(propertiesSource.getProperties());
            }
        }
    }

    private void loadVariables(JReleaserContext context, Path file) {
        propertiesFile = file;
        context.getLogger().info(RB.$("environment.load.variables"), file.toAbsolutePath());
        if (Files.exists(file)) {
            try {
                if (file.getFileName().toString().endsWith(".properties")) {
                    vars.load(new FileInputStream(file.toFile()));
                } else {
                    vars.putAll(JReleaserConfigLoader.loadProperties(file));
                }
            } catch (IOException e) {
                context.getLogger().debug(RB.$("environment.variables.load.error"), file.toAbsolutePath(), e);
            }
        } else {
            context.getLogger().warn(RB.$("environment.variables.source.missing"), file.toAbsolutePath());
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

    public interface PropertiesSource {
        Map<String, String> getProperties();
    }

    public static abstract class AbstractPropertiesSource implements PropertiesSource {
        @Override
        public Map<String, String> getProperties() {
            Map<String, String> props = doGetProperties();
            Map<String, String> map = new LinkedHashMap<>();

            props.forEach((key, value) -> {
                if (key.startsWith("JRELEASER_")) return;
                String k = key.replace(".", "-");
                k = getPropertyNameForLowerCaseHyphenSeparatedName(k);
                map.put(k, value);
            });

            return map;
        }

        protected abstract Map<String, String> doGetProperties();
    }

    public static class PropertiesPropertiesSource extends AbstractPropertiesSource {
        private final Properties properties;

        public PropertiesPropertiesSource(Properties properties) {
            this.properties = properties;
        }

        @Override
        protected Map<String, String> doGetProperties() {
            Map<String, String> map = new LinkedHashMap<>();
            properties.forEach((k, v) -> map.put(String.valueOf(k), String.valueOf(v)));
            return map;
        }
    }

    public static class MapPropertiesSource extends AbstractPropertiesSource {
        private final Map<String, ?> properties;

        public MapPropertiesSource(Map<String, ?> properties) {
            this.properties = properties;
        }

        @Override
        protected Map<String, String> doGetProperties() {
            Map<String, String> map = new LinkedHashMap<>();
            properties.forEach((k, v) -> map.put(k, String.valueOf(v)));
            return map;
        }
    }
}
