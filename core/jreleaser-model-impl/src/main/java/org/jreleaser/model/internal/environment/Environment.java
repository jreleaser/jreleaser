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
package org.jreleaser.model.internal.environment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.bundle.RB;
import org.jreleaser.config.JReleaserConfigLoader;
import org.jreleaser.config.JReleaserConfigParser;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.util.Env;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;

import static java.nio.file.Files.newInputStream;
import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.DEFAULT_GIT_REMOTE;
import static org.jreleaser.model.Constants.JRELEASER_USER_HOME;
import static org.jreleaser.model.Constants.XDG_CONFIG_HOME;
import static org.jreleaser.util.Env.JRELEASER_ENV_PREFIX;
import static org.jreleaser.util.Env.JRELEASER_SYS_PREFIX;
import static org.jreleaser.util.Env.envKey;
import static org.jreleaser.util.StringUtils.getPropertyNameForLowerCaseHyphenSeparatedName;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class Environment extends AbstractModelObject<Environment> implements Domain {
    private static final long serialVersionUID = 4554098923129885325L;

    private final Map<String, Object> properties = new LinkedHashMap<>();
    @JsonIgnore
    private final Map<String, Object> sourcedProperties = new LinkedHashMap<>();
    @JsonIgnore
    private PropertiesSource propertiesSource;
    private String variables;
    @JsonIgnore
    private Properties vars;
    @JsonIgnore
    private Path propertiesFile;

    @JsonIgnore
    private final org.jreleaser.model.api.environment.Environment immutable = new org.jreleaser.model.api.environment.Environment() {
        private static final long serialVersionUID = -7287090119869371299L;

        @Override
        public Properties getVars() {
            return vars;
        }

        @Override
        public String getVariables() {
            return variables;
        }

        @Override
        public Map<String, Object> getProperties() {
            return unmodifiableMap(properties);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Environment.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.environment.Environment asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Environment source) {
        this.variables = merge(this.variables, source.variables);
        setProperties(merge(this.properties, source.properties));
        setPropertiesSource(merge(this.propertiesSource, source.propertiesSource));
    }

    public String resolve(String key) {
        return env(key, Env.sys(key, ""));
    }

    public String resolve(String key, String value) {
        return env(key, Env.sys(key, value));
    }

    public String resolveOrDefault(String key, String value, String defaultValue) {
        String result = env(key, Env.sys(key, value));
        return isNotBlank(result) ? result : defaultValue;
    }

    private String env(String key, String value) {
        if (isNotBlank(value)) {
            return value;
        }
        return getVariable(envKey(key));
    }

    public Properties getVars() {
        return vars;
    }

    public String getVariable(String key) {
        return vars.getProperty(Env.envKey(key));
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
            sourcedProperties.putAll(propertiesSource.getProperties());
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

    public Map<String, Object> getSourcedProperties() {
        return sourcedProperties;
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

            loadVariables(context, resolveConfigFileAt(configDirectory)
                .orElse(configDirectory.resolve("config.properties")));

            if (isNotBlank(variables)) {
                loadVariables(context, context.getBasedir().resolve(variables.trim()));
            }

            Path envFilePath = context.getBasedir().resolve(".env");
            if (Files.exists(envFilePath)) {
                loadVariables(context, envFilePath);
            }

            // env vars
            Set<String> keyNames = new TreeSet<>();
            Properties envVars = new Properties();
            System.getenv().forEach((k, v) -> {
                if (k.startsWith(JRELEASER_ENV_PREFIX)) keyNames.add(k);
                if (k.startsWith(JRELEASER_ENV_PREFIX)) envVars.put(k, v);
            });
            if (System.getenv().containsKey(envKey(DEFAULT_GIT_REMOTE))) {
                keyNames.add(envKey(DEFAULT_GIT_REMOTE));
            }
            if (!keyNames.isEmpty()) {
                context.getLogger().debug(RB.$("environment.variables.env"));
                keyNames.forEach(message -> context.getLogger().debug("  " + message));
            }

            // system props
            keyNames.clear();
            System.getProperties().stringPropertyNames().forEach(k -> {
                if (k.startsWith(JRELEASER_SYS_PREFIX)) keyNames.add(k);
            });
            if (!keyNames.isEmpty()) {
                context.getLogger().debug(RB.$("environment.system.properties"));
                keyNames.forEach(message -> context.getLogger().debug("  " + message));
            }

            // merge keyNames
            Properties merged = new Properties();
            merged.putAll(envVars);
            merged.putAll(this.vars);
            this.vars.clear();
            this.vars.putAll(merged);

            if (null != propertiesSource) {
                sourcedProperties.putAll(propertiesSource.getProperties());
            }
        }
    }

    private void loadVariables(JReleaserContext context, Path file) {
        propertiesFile = file;
        context.getLogger().info(RB.$("environment.load.variables"), file.toAbsolutePath());
        if (Files.exists(file)) {
            try {
                Properties p = new Properties();
                if (file.getFileName().toString().endsWith(".properties") ||
                    file.getFileName().toString().equals(".env")) {
                    try (InputStream in = newInputStream(file)) {
                        p.load(in);
                    }
                } else {
                    p.putAll(JReleaserConfigLoader.loadProperties(file));
                }
                vars.putAll(p);

                Set<String> keyNames = new TreeSet<>();
                p.stringPropertyNames().stream()
                    .filter(k -> k.startsWith(JRELEASER_ENV_PREFIX)).
                    forEach(keyNames::add);

                if (!keyNames.isEmpty()) {
                    context.getLogger().debug(RB.$("environment.variables.file", file.getFileName().toString()));
                    keyNames.forEach(message -> context.getLogger().debug("  " + message));
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

    public boolean getBooleanProperty(String key) {
        boolean keyInProperties = properties.containsKey(key) && Boolean.parseBoolean(String.valueOf(properties.get(key)));
        boolean keyInSourcedProperties = sourcedProperties.containsKey(key) && Boolean.parseBoolean(String.valueOf(sourcedProperties.get(key)));
        return keyInProperties || keyInSourcedProperties;
    }

    public interface PropertiesSource extends Serializable {
        Map<String, String> getProperties();
    }

    public abstract static class AbstractPropertiesSource implements PropertiesSource {
        private static final long serialVersionUID = 9102569253517657171L;

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
        private static final long serialVersionUID = 7747477120107034027L;

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
        private static final long serialVersionUID = 6643212572356054605L;

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
