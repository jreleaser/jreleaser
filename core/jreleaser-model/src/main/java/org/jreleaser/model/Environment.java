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

import org.jreleaser.util.Env;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Environment implements Domain {
    private String variables;
    private Properties props;

    public boolean isEmpty() {
        return isBlank(variables);
    }

    void setAll(Environment environment) {
        this.variables = environment.variables;
    }

    public String getVariable(String key) {
        return props.getProperty(Env.prefix(key));
    }

    public void initProps(JReleaserContext context) {
        if (null == props) {
            props = new Properties();

            String home = System.getenv("JRELEASER_USER_HOME");
            if (isBlank(home)) {
                home = System.getProperty("user.home");
            }

            Path defaultPath = Paths.get(home).resolve(".jreleaser").resolve("config.properties");
            Path path = isNotBlank(variables) ? context.getBasedir().resolve(variables.trim()) : defaultPath;
            if (!Files.exists(path)) path = defaultPath;
            context.getLogger().debug("Properties path: {}", path.toAbsolutePath());

            if (Files.exists(path)) {
                try {
                    context.getLogger().info("Loading properties from {}", path.toAbsolutePath());
                    props.load(new FileInputStream(path.toFile()));
                } catch (IOException e) {
                    context.getLogger().debug("Could not load properties from {}", path.toAbsolutePath(), e);
                }
            }
        }
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

    @Override
    public final Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("variables", variables);

        return map;
    }
}
