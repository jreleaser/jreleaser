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
import org.jreleaser.util.JReleaserException;
import org.jreleaser.util.PlatformUtils;
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
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_EXECUTABLE;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_EXECUTABLE_EXTENSION_UNIX;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_EXECUTABLE_EXTENSION_WINDOWS;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_EXECUTABLE_NAME;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_EXECUTABLE_UNIX;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_EXECUTABLE_WINDOWS;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_JAVA_ARTIFACT_ID;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_JAVA_GROUP_ID;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_JAVA_VERSION;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_JAVA_VERSION_BUILD;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_JAVA_VERSION_MAJOR;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_JAVA_VERSION_MINOR;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_JAVA_VERSION_PATCH;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_JAVA_VERSION_TAG;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_NAME;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_TAGS_BY_COMMA;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_TAGS_BY_SPACE;
import static org.jreleaser.util.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Distribution extends Packagers<Distribution> implements ExtraProperties, Activatable {
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
    private final Executable executable = new Executable();

    private Active active;
    @JsonIgnore
    private boolean enabled;
    private String name;
    private DistributionType type = DistributionType.JAVA_BINARY;

    @Override
    public void merge(Distribution distribution) {
        super.merge(distribution);
        this.active = merge(this.active, distribution.active);
        this.enabled = merge(this.enabled, distribution.enabled);
        this.name = merge(this.name, distribution.name);
        this.type = merge(this.type, distribution.type);
        setExecutable(distribution.executable);
        setPlatform(distribution.platform);
        setJava(distribution.java);
        setTags(merge(this.tags, distribution.tags));
        setExtraProperties(merge(this.extraProperties, distribution.extraProperties));
        setArtifacts(merge(this.artifacts, distribution.artifacts));
    }

    public Map<String, Object> props() {
        Map<String, Object> props = new LinkedHashMap<>();
        applyTemplates(props, getResolvedExtraProperties());
        props.put(KEY_DISTRIBUTION_NAME, name);
        props.put(KEY_DISTRIBUTION_EXECUTABLE, executable.getName());
        props.put(KEY_DISTRIBUTION_EXECUTABLE_NAME, executable.getName());
        props.put(KEY_DISTRIBUTION_EXECUTABLE_UNIX, executable.resolveExecutable("linux"));
        props.put(KEY_DISTRIBUTION_EXECUTABLE_WINDOWS, executable.resolveExecutable("windows"));
        safePut(KEY_DISTRIBUTION_EXECUTABLE_EXTENSION_UNIX, executable.resolveUnixExtension(), props, true);
        safePut(KEY_DISTRIBUTION_EXECUTABLE_EXTENSION_WINDOWS, executable.resolveWindowsExtension(), props, true);
        props.put(KEY_DISTRIBUTION_TAGS_BY_SPACE, String.join(" ", tags));
        props.put(KEY_DISTRIBUTION_TAGS_BY_COMMA, String.join(",", tags));
        props.putAll(java.getResolvedExtraProperties());
        safePut(KEY_DISTRIBUTION_JAVA_GROUP_ID, java.getGroupId(), props, true);
        safePut(KEY_DISTRIBUTION_JAVA_ARTIFACT_ID, java.getArtifactId(), props, true);
        safePut(KEY_DISTRIBUTION_JAVA_MAIN_CLASS, java.getMainClass(), props, true);
        if (isNotBlank(java.getVersion())) {
            props.put(KEY_DISTRIBUTION_JAVA_VERSION, java.getVersion());
            SemVer jv = SemVer.of(java.getVersion());
            safePut(KEY_DISTRIBUTION_JAVA_VERSION_MAJOR, jv.getMajor(), props, true);
            safePut(KEY_DISTRIBUTION_JAVA_VERSION_MINOR, jv.getMinor(), props, true);
            safePut(KEY_DISTRIBUTION_JAVA_VERSION_PATCH, jv.getPatch(), props, true);
            safePut(KEY_DISTRIBUTION_JAVA_VERSION_TAG, jv.getTag(), props, true);
            safePut(KEY_DISTRIBUTION_JAVA_VERSION_BUILD, jv.getBuild(), props, true);
        } else {
            props.put(KEY_DISTRIBUTION_JAVA_VERSION, "");
            props.put(KEY_DISTRIBUTION_JAVA_VERSION_MAJOR, "");
            props.put(KEY_DISTRIBUTION_JAVA_VERSION_MINOR, "");
            props.put(KEY_DISTRIBUTION_JAVA_VERSION_PATCH, "");
            props.put(KEY_DISTRIBUTION_JAVA_VERSION_TAG, "");
            props.put(KEY_DISTRIBUTION_JAVA_VERSION_BUILD, "");
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
        this.platform.mergeValues(platform);
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

    public Executable getExecutable() {
        return executable;
    }

    public void setExecutable(Executable executable) {
        this.executable.merge(executable);
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
        this.java.merge(java);
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

    // --== PACKAGERS ==--

    public <T extends Packager> T findPackager(String name) {
        if (isBlank(name)) {
            throw new JReleaserException(RB.$("ERROR_packager_name_not_blank"));
        }

        return resolvePackager(name);
    }

    public <T extends Packager> T getPackager(String name) {
        T packager = findPackager(name);
        if (null != packager) {
            return packager;
        }
        throw new JReleaserException(RB.$("ERROR_packager_not_configured", name));
    }

    private <T extends Packager> T resolvePackager(String name) {
        switch (name.toLowerCase().trim()) {
            case Brew.TYPE:
                return (T) getBrew();
            case Chocolatey.TYPE:
                return (T) getChocolatey();
            case Docker.TYPE:
                return (T) getDocker();
            case Gofish.TYPE:
                return (T) getGofish();
            case Jbang.TYPE:
                return (T) getJbang();
            case Macports.TYPE:
                return (T) getMacports();
            case Scoop.TYPE:
                return (T) getScoop();
            case Sdkman.TYPE:
                return (T) getSdkman();
            case Snap.TYPE:
                return (T) getSnap();
            case Spec.TYPE:
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
        props.put("executable", executable.asMap(full));
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
        set.add(Brew.TYPE);
        set.add(Chocolatey.TYPE);
        set.add(Docker.TYPE);
        set.add(Gofish.TYPE);
        set.add(Jbang.TYPE);
        set.add(Macports.TYPE);
        set.add(Scoop.TYPE);
        set.add(Sdkman.TYPE);
        set.add(Snap.TYPE);
        set.add(Spec.TYPE);
        return Collections.unmodifiableSet(set);
    }

    public enum DistributionType {
        BINARY("binary"),
        JAVA_BINARY("java"),
        JLINK("jlink"),
        SINGLE_JAR("uberjar"),
        NATIVE_IMAGE("graal"),
        NATIVE_PACKAGE("jpackage");

        private final String alias;

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

    public static class Executable extends AbstractModelObject<Executable> implements Domain {
        private String name;
        private String unixExtension;
        private String windowsExtension = "bat";

        @Override
        public void merge(Distribution.Executable executable) {
            this.name = this.merge(this.name, executable.name);
            this.unixExtension = this.merge(this.unixExtension, executable.unixExtension);
            this.windowsExtension = this.merge(this.windowsExtension, executable.windowsExtension);
        }

        public String resolveExecutable(String platform) {
            if (PlatformUtils.isWindows(platform)) {
                return name + resolveWindowsExtension();
            }

            return name + resolveUnixExtension();
        }

        public String resolveUnixExtension() {
            return isNotBlank(unixExtension) ? "." + unixExtension : "";
        }

        public String resolveWindowsExtension() {
            return "." + windowsExtension;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUnixExtension() {
            return unixExtension;
        }

        public void setUnixExtension(String unixExtension) {
            this.unixExtension = unixExtension;
        }

        public String getWindowsExtension() {
            return windowsExtension;
        }

        public void setWindowsExtension(String windowsExtension) {
            this.windowsExtension = windowsExtension;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", name);
            map.put("unixExtension", unixExtension);
            map.put("windowsExtension", windowsExtension);
            return map;
        }
    }
}
