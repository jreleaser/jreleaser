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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Brew extends AbstractTool {
    public static final String NAME = "brew";

    private final Map<String, String> dependencies = new LinkedHashMap<>();

    public Brew() {
        super(NAME);
    }

    void setAll(Brew brew) {
        super.setAll(brew);
        setDependencies(brew.dependencies);
    }

    public Map<String, String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Map<String, String> dependencies) {
        this.dependencies.clear();
        this.dependencies.putAll(dependencies);
    }

    public void addDependencies(Map<String, String> dependencies) {
        this.dependencies.putAll(dependencies);
    }

    public void addDependency(String key, String value) {
        dependencies.put(key, value);
    }

    public void addDependency(String key) {
        dependencies.put(key, "");
    }

    @Override
    protected void asMap(Map<String, Object> props) {
        props.put("dependencies", dependencies);
    }
}
