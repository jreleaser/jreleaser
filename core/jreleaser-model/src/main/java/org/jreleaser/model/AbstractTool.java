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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractTool implements Tool {
    protected final String name;
    protected final Map<String, String> extraProperties = new LinkedHashMap<>();
    protected Boolean enabled;
    protected String templateDirectory;

    protected AbstractTool(String name) {
        this.name = name;
    }

    void setAll(AbstractTool tool) {
        this.enabled = tool.enabled;
        this.templateDirectory = tool.templateDirectory;
        setExtraProperties(tool.extraProperties);
    }

    @Override
    public Set<String> getSupportedExtensions() {
        Set<String> set = new LinkedHashSet<>();
        set.add(".zip");
        return set;
    }

    @Override
    public String getPrefix() {
        return getName();
    }

    @Override
    public Boolean isEnabled() {
        return enabled != null && enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabledSet() {
        return enabled != null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTemplateDirectory() {
        return templateDirectory;
    }

    @Override
    public void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory = templateDirectory;
    }

    @Override
    public Map<String, String> getExtraProperties() {
        return extraProperties;
    }

    @Override
    public void setExtraProperties(Map<String, String> extraProperties) {
        this.extraProperties.clear();
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public void addExtraProperties(Map<String, String> extraProperties) {
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public final Map<String, Object> asMap() {
        if (!isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("templateDirectory", templateDirectory);
        asMap(props);
        props.put("extraProperties", getResolvedExtraProperties());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(getName(), props);
        return map;
    }

    protected abstract void asMap(Map<String, Object> props);

    @Override
    public boolean isSnapshotAllowed() {
        return false;
    }

    @Override
    public boolean supportsDistribution(Distribution distribution) {
        return true;
    }
}
