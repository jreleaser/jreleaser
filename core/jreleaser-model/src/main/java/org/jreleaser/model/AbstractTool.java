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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractTool implements Tool {
    protected final String name;
    protected final Map<String, Object> extraProperties = new LinkedHashMap<>();
    protected boolean enabled;
    protected Active active;
    protected String templateDirectory;

    protected AbstractTool(String name) {
        this.name = name;
    }

    void setAll(AbstractTool tool) {
        this.active = tool.active;
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
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void disable() {
        active = Active.NEVER;
        enabled = false;
    }

    public boolean resolveEnabled(Project project) {
        if (null == active) {
            active = Active.NEVER;
        }
        enabled = active.check(project);
        return enabled;
    }

    public boolean resolveEnabled(Project project, Distribution distribution) {
        if (null == active) {
            active = Active.NEVER;
        }
        enabled = active.check(project);
        if (!supportsDistribution(distribution)) {
            enabled = false;
        }
        return enabled;
    }

    @Override
    public Active getActive() {
        return active;
    }

    @Override
    public void setActive(Active active) {
        this.active = active;
    }

    @Override
    public void setActive(String str) {
        this.active = Active.of(str);
    }

    @Override
    public boolean isActiveSet() {
        return active != null;
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
    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    @Override
    public void setExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.clear();
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public void addExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("active", active);
        props.put("templateDirectory", templateDirectory);
        asMap(full, props);
        props.put("extraProperties", getResolvedExtraProperties());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(getName(), props);
        return map;
    }

    protected abstract void asMap(boolean full, Map<String, Object> props);

    @Override
    public boolean supportsDistribution(Distribution distribution) {
        return true;
    }
}
