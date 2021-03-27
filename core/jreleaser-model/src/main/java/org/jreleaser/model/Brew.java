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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Brew extends AbstractTool {
    public static final String NAME = "brew";
    private final List<Dependency> dependencies = new ArrayList<>();
    private HomebrewTap tap = new HomebrewTap();

    public Brew() {
        super(NAME);
    }

    void setAll(Brew brew) {
        super.setAll(brew);
        this.tap.setAll(brew.tap);
        setDependenciesAsList(brew.dependencies);
    }

    public HomebrewTap getTap() {
        return tap;
    }

    public void setTap(HomebrewTap tap) {
        this.tap = tap;
    }

    public void setDependencies(Map<String, String> dependencies) {
        if (null == dependencies || dependencies.isEmpty()) {
            return;
        }
        this.dependencies.clear();
        dependencies.forEach(this::addDependency);
    }

    public List<Dependency> getDependenciesAsList() {
        return dependencies;
    }

    public void setDependenciesAsList(List<Dependency> dependencies) {
        if (null == dependencies || dependencies.isEmpty()) {
            return;
        }
        this.dependencies.clear();
        this.dependencies.addAll(dependencies);
    }

    public void addDependencies(Map<String, String> dependencies) {
        if (null == dependencies || dependencies.isEmpty()) {
            return;
        }
        dependencies.forEach(this::addDependency);
    }

    public void addDependency(String key, String value) {
        dependencies.add(new Dependency(key, value));
    }

    public void addDependency(String key) {
        dependencies.add(new Dependency(key));
    }

    @Override
    protected void asMap(Map<String, Object> props) {
        props.put("tap", tap.asMap());
        props.put("dependencies", dependencies);
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return tap;
    }

    public static class Dependency {
        private final String key;
        private final String value;

        private Dependency(String key) {
            this(key, null);
        }

        private Dependency(String key, String value) {
            this.key = key;
            this.value = isBlank(value) || "null".equalsIgnoreCase(value) ? null : value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            StringBuilder formatted = new StringBuilder();
            if (key.startsWith(":")) {
                formatted.append(key);
            } else {
                formatted.append("\"")
                    .append(key)
                    .append("\"");
            }
            if (isNotBlank(value)) {
                formatted.append(" => \"")
                    .append(value)
                    .append("\"");
            }
            return formatted.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Dependency that = (Dependency) o;
            return key.equals(that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }
}
