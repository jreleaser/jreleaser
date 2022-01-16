/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.bundle.RB;
import org.jreleaser.util.Constants;
import org.jreleaser.util.SemVer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.CollectionUtils.safePut;
import static org.jreleaser.util.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Distribution extends Packagers implements ExtraProperties, Activatable {
    public static final EnumSet<DistributionType> JAVA_DISTRIBUTION_TYPES = EnumSet.of(
        DistributionType.JAVA_BINARY,
        DistributionType.JLINK,
        DistributionType.SINGLE_JAR
    );

    private final List<String> tags = new ArrayList<>();
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final Set<Artifact> artifacts = new LinkedHashSet<>();
    private final Java java = new Java();
    private final Platform platform = new Platform();
    private Active active;
    @JsonIgnore
    private boolean enabled;
    private String name;
    private DistributionType type = DistributionType.JAVA_BINARY;
    private String executable;
    private String executableExtension;

    void setAll(Distribution distribution) {
        super.setAll(distribution);
        this.active = distribution.active;
        this.enabled = distribution.enabled;
        this.name = distribution.name;
        this.type = distribution.type;
        this.executable = distribution.executable;
        this.executableExtension = distribution.executableExtension;
        setPlatform(distribution.platform);
        setJava(distribution.java);
        setTags(distribution.tags);
        setExtraProperties(distribution.extraProperties);
        setArtifacts(distribution.artifacts);
    }

    public Map<String, Object> props() {
        Map<String, Object> props = new LinkedHashMap<>();
        applyTemplates(props, getResolvedExtraProperties());
        props.put(Constants.KEY_DISTRIBUTION_NAME, name);
        props.put(Constants.KEY_DISTRIBUTION_EXECUTABLE, executable);
        props.put(Constants.KEY_DISTRIBUTION_EXECUTABLE_EXTENSION, executableExtension);
        props.put(Constants.KEY_DISTRIBUTION_TAGS_BY_SPACE, String.join(" ", tags));
        props.put(Constants.KEY_DISTRIBUTION_TAGS_BY_COMMA, String.join(",", tags));
        props.putAll(java.getResolvedExtraProperties());
        safePut(Constants.KEY_DISTRIBUTION_JAVA_GROUP_ID, java.getGroupId(), props, true);
        safePut(Constants.KEY_DISTRIBUTION_JAVA_ARTIFACT_ID, java.getArtifactId(), props, true);
        safePut(Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS, java.getMainClass(), props, true);
        if (isNotBlank(java.getVersion())) {
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION, java.getVersion());
            SemVer jv = SemVer.of(java.getVersion());
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MAJOR, jv.getMajor(), props, true);
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MINOR, jv.getMinor(), props, true);
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_PATCH, jv.getPatch(), props, true);
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_TAG, jv.getTag(), props, true);
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_BUILD, jv.getBuild(), props, true);
        } else {
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MAJOR, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MINOR, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_PATCH, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_TAG, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_BUILD, "");
        }
        return props;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

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

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform.setAll(platform);
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
        this.type = DistributionType.of(type);
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

    public String getExecutableExtension() {
        return executableExtension;
    }

    public void setExecutableExtension(String executableExtension) {
        this.executableExtension = executableExtension;
    }

    public Set<Artifact> getArtifacts() {
        return Artifact.sortArtifacts(artifacts);
    }

    public void setArtifacts(Set<Artifact> artifacts) {
        this.artifacts.clear();
        this.artifacts.addAll(artifacts);
    }

    public void addArtifacts(Set<Artifact> artifacts) {
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
        this.extraProperties.clear();
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public void addExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.putAll(extraProperties);
    }

    public void mergeExtraProperties(Map<String, Object> extraProperties) {
        extraProperties.forEach((k, v) -> {
            if (!this.extraProperties.containsKey(k)) {
                this.extraProperties.put(k, v);
            }
        });
    }

    // --== TOOLs ==--

    public <T extends Tool> T findTool(String name) {
        if (isBlank(name)) {
            throw new JReleaserException(RB.$("ERROR_tool_name_not_blank"));
        }

        return resolveTool(name);
    }

    public <T extends Tool> T getTool(String name) {
        T tool = findTool(name);
        if (null != tool) {
            return tool;
        }
        throw new JReleaserException(RB.$("ERROR_tool_not_configured", name));
    }

    private <T extends Tool> T resolveTool(String name) {
        switch (name.toLowerCase().trim()) {
            case Brew.NAME:
                return (T) getBrew();
            case Chocolatey.NAME:
                return (T) getChocolatey();
            case Docker.NAME:
                return (T) getDocker();
            case Gofish.NAME:
                return (T) getGofish();
            case Jbang.NAME:
                return (T) getJbang();
            case Macports.NAME:
                return (T) getMacports();
            case Scoop.NAME:
                return (T) getScoop();
            case Sdkman.NAME:
                return (T) getSdkman();
            case Snap.NAME:
                return (T) getSnap();
            case Spec.NAME:
                return (T) getSpec();
            default:
                throw new JReleaserException(RB.$("ERROR_unsupported_packager", name));
        }
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("active", active);
        props.put("type", type);
        props.put("executable", executable);
        props.put("executableExtension", executableExtension);
        if (full || platform.isSet()) props.put("platform", platform.asMap(full));

        Map<String, Map<String, Object>> mappedArtifacts = new LinkedHashMap<>();
        int i = 0;
        for (Artifact artifact : getArtifacts()) {
            mappedArtifacts.put("artifact " + (i++), artifact.asMap(full));
        }
        props.put("artifacts", mappedArtifacts);

        props.put("tags", tags);
        props.put("extraProperties", getResolvedExtraProperties());
        if (java.isEnabled()) {
            props.put("java", java.asMap(full));
        }
        props.putAll(super.asMap(full));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(name, props);
        return map;
    }

    public static Set<String> supportedPackagers() {
        Set<String> set = new LinkedHashSet<>();
        set.add(Brew.NAME);
        set.add(Chocolatey.NAME);
        set.add(Docker.NAME);
        set.add(Gofish.NAME);
        set.add(Jbang.NAME);
        set.add(Macports.NAME);
        set.add(Scoop.NAME);
        set.add(Sdkman.NAME);
        set.add(Snap.NAME);
        set.add(Spec.NAME);
        return Collections.unmodifiableSet(set);
    }

    public enum DistributionType {
        BINARY("binary"),
        JAVA_BINARY("java"),
        JLINK("jlink"),
        SINGLE_JAR("uberjar"),
        NATIVE_IMAGE("graal"),
        NATIVE_PACKAGE("jpackage");

        private String alias;

        DistributionType(String alias) {
            this.alias = alias.toUpperCase();
        }

        public static DistributionType of(String str) {
            if (isBlank(str)) return null;

            String value = str.replaceAll(" ", "_")
                .replaceAll("-", "_")
                .toUpperCase().trim();

            // try alias
            for (DistributionType type : DistributionType.values()) {
                if (type.alias.equals(value)) {
                    return type;
                }
            }

            return DistributionType.valueOf(value);
        }
    }
}
