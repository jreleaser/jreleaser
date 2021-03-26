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
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Distribution extends Packagers implements ExtraProperties, EnabledProvider {
    public static final EnumSet<DistributionType> JAVA_DISTRIBUTION_TYPES = EnumSet.of(
        DistributionType.JAVA_BINARY,
        DistributionType.JLINK
    );

    private final List<String> tags = new ArrayList<>();
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final List<Artifact> artifacts = new ArrayList<>();
    private final Java java = new Java();
    private Boolean enabled;
    private String name;
    private DistributionType type = DistributionType.JAVA_BINARY;
    private String executable;

    void setAll(Distribution distribution) {
        super.setAll(distribution);
        this.enabled = distribution.enabled;
        this.name = distribution.name;
        this.type = distribution.type;
        this.executable = distribution.executable;
        this.java.setAll(distribution.java);
        setTags(distribution.tags);
        setExtraProperties(distribution.extraProperties);
        setArtifacts(distribution.artifacts);
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
    public String getPrefix() {
        return "distribution";
    }

    public DistributionType getType() {
        return type;
    }

    public void setType(DistributionType type) {
        this.type = type;
    }

    public void setType(String type) {
        this.type = DistributionType.valueOf(type.replaceAll(" ", "_")
            .replaceAll("-", "_")
            .toUpperCase());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts.clear();
        this.artifacts.addAll(artifacts);
    }

    public void addArtifacts(List<Artifact> artifacts) {
        this.artifacts.addAll(artifacts);
    }

    public void addArtifact(Artifact artifact) {
        if (null != artifact) {
            this.artifacts.add(artifact);
        }
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }

    public void addTags(List<String> tags) {
        this.tags.addAll(tags);
    }

    public void addTag(String tag) {
        if (isNotBlank(tag)) {
            this.tags.add(tag.trim());
        }
    }

    public void removeTag(String tag) {
        if (isNotBlank(tag)) {
            this.tags.remove(tag.trim());
        }
    }

    public Java getJava() {
        return java;
    }

    public void setJava(Java java) {
        this.java.setAll(java);
    }

    @Override
    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    @Override
    public void setExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.putAll(extraProperties);
    }

    // --== TOOLs ==--

    @Override
    public void addExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.putAll(extraProperties);
    }

    public <T extends Tool> T findTool(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("Tool name must not be blank");
        }

        return resolveTool(name);
    }

    public <T extends Tool> T getTool(String name) {
        T tool = findTool(name);
        if (null != tool) {
            return tool;
        }
        throw new IllegalArgumentException("Tool '" + name + "' has not been configured");
    }

    private <T extends Tool> T resolveTool(String name) {
        switch (name.toLowerCase().trim()) {
            case Brew.NAME:
                return (T) getBrew();
            case Chocolatey.NAME:
                return (T) getChocolatey();
            case Jbang.NAME:
                return (T) getJbang();
            case Scoop.NAME:
                return (T) getScoop();
            case Snap.NAME:
                return (T) getSnap();
            default:
                throw new IllegalArgumentException("Unsupported tool '" + name + "'");
        }
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("type", type);
        props.put("executable", executable);
        props.put("artifacts", artifacts.stream()
            .map(Artifact::asMap)
            .collect(Collectors.toList()));
        props.put("tags", tags);
        props.put("extraProperties", getResolvedExtraProperties());
        if (java.isEnabled()) {
            props.put("java", java.asMap());
        }
        props.putAll(super.asMap());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(name, props);
        return map;
    }

    public static Set<String> supportedTools() {
        Set<String> set = new LinkedHashSet<>();
        set.add(Brew.NAME);
        set.add(Chocolatey.NAME);
        set.add(Jbang.NAME);
        set.add(Scoop.NAME);
        set.add(Snap.NAME);
        return Collections.unmodifiableSet(set);
    }

    public enum DistributionType {
        JAVA_BINARY,
        JLINK,
        // NATIVE_IMAGE,
    }
}
