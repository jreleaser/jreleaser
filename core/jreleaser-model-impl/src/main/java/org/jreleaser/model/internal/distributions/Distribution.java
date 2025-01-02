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
package org.jreleaser.model.internal.distributions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.Executable;
import org.jreleaser.model.internal.common.ExtraProperties;
import org.jreleaser.model.internal.common.Java;
import org.jreleaser.model.internal.packagers.Packager;
import org.jreleaser.model.internal.packagers.Packagers;
import org.jreleaser.model.internal.platform.Platform;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.version.SemanticVersion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toSet;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_EXECUTABLE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_EXECUTABLE_EXTENSION_UNIX;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_EXECUTABLE_EXTENSION_WINDOWS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_EXECUTABLE_NAME;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_EXECUTABLE_UNIX;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_EXECUTABLE_WINDOWS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_ARTIFACT_ID;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_GROUP_ID;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_VERSION;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_VERSION_BUILD;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_VERSION_MAJOR;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_VERSION_MINOR;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_VERSION_PATCH;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_VERSION_TAG;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_NAME;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_STEREOTYPE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_TAGS_BY_COMMA;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_TAGS_BY_SPACE;
import static org.jreleaser.model.JReleaserOutput.nag;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class Distribution extends Packagers<Distribution> implements Domain, Activatable, ExtraProperties {
    private static final long serialVersionUID = 8423362983187267131L;

    private final List<String> tags = new ArrayList<>();
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final Set<Artifact> artifacts = new LinkedHashSet<>();
    private final Java java = new Java();
    private final Platform platform = new Platform();
    private final Executable executable = new Executable();

    private String name;
    private org.jreleaser.model.Distribution.DistributionType type = org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
    private Stereotype stereotype;

    @JsonIgnore
    private final org.jreleaser.model.api.distributions.Distribution immutable = new org.jreleaser.model.api.distributions.Distribution() {
        private static final long serialVersionUID = 1918136121866210647L;

        private Set<? extends org.jreleaser.model.api.common.Artifact> artifacts;

        @Override
        public org.jreleaser.model.api.platform.Platform getPlatform() {
            return platform.asImmutable();
        }

        @Override
        public org.jreleaser.model.Distribution.DistributionType getType() {
            return type;
        }

        @Override
        public Stereotype getStereotype() {
            return stereotype;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public org.jreleaser.model.api.common.Executable getExecutable() {
            return executable.asImmutable();
        }

        @Override
        public Set<? extends org.jreleaser.model.api.common.Artifact> getArtifacts() {
            if (null == artifacts) {
                artifacts = Distribution.this.artifacts.stream()
                    .map(Artifact::asImmutable)
                    .collect(toSet());
            }
            return artifacts;
        }

        @Override
        public List<String> getTags() {
            return unmodifiableList(tags);
        }

        @Override
        public org.jreleaser.model.api.common.Java getJava() {
            return java.asImmutable();
        }

        @Override
        public Active getActive() {
            return Distribution.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return Distribution.this.isEnabled();
        }

        @Override
        public String getPrefix() {
            return Distribution.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }

        @Override
        public org.jreleaser.model.api.packagers.AppImagePackager getAppImage() {
            return appImage.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.AsdfPackager getAsdf() {
            return asdf.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.BrewPackager getBrew() {
            return brew.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.ChocolateyPackager getChocolatey() {
            return chocolatey.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.DockerPackager getDocker() {
            return docker.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.GofishPackager getGofish() {
            return gofish.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.FlatpakPackager getFlatpak() {
            return flatpak.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.JbangPackager getJbang() {
            return jbang.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.JibPackager getJib() {
            return jib.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.MacportsPackager getMacports() {
            return macports.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.ScoopPackager getScoop() {
            return scoop.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.SdkmanPackager getSdkman() {
            return sdkman.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.SnapPackager getSnap() {
            return snap.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.SpecPackager getSpec() {
            return spec.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.WingetPackager getWinget() {
            return winget.asImmutable();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return Collections.unmodifiableMap(Distribution.this.asMap(full));
        }
    };

    @Override
    public org.jreleaser.model.api.distributions.Distribution asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Distribution source) {
        super.merge(source);
        this.name = merge(this.name, source.name);
        this.type = merge(this.type, source.type);
        this.stereotype = merge(this.stereotype, source.stereotype);
        setExecutable(source.executable);
        setPlatform(source.platform);
        setJava(source.java);
        setTags(merge(this.tags, source.tags));
        setExtraProperties(merge(this.extraProperties, source.extraProperties));
        setArtifacts(merge(this.artifacts, source.artifacts));
    }

    public TemplateContext props() {
        TemplateContext props = new TemplateContext();
        applyTemplates(props, resolvedExtraProperties());
        props.set(KEY_DISTRIBUTION_NAME, name);
        props.set(KEY_DISTRIBUTION_STEREOTYPE, getStereotype());
        props.set(KEY_DISTRIBUTION_EXECUTABLE, executable.getName());
        props.set(KEY_DISTRIBUTION_EXECUTABLE_NAME, executable.getName());
        props.set(KEY_DISTRIBUTION_EXECUTABLE_UNIX, executable.resolveExecutable("linux"));
        props.set(KEY_DISTRIBUTION_EXECUTABLE_WINDOWS, executable.resolveExecutable("windows"));
        props.set(KEY_DISTRIBUTION_EXECUTABLE_EXTENSION_UNIX, executable.resolveUnixExtension(), "");
        props.set(KEY_DISTRIBUTION_EXECUTABLE_EXTENSION_WINDOWS, executable.resolveWindowsExtension(), "");
        props.set(KEY_DISTRIBUTION_TAGS_BY_SPACE, String.join(" ", tags));
        props.set(KEY_DISTRIBUTION_TAGS_BY_COMMA, String.join(",", tags));
        props.setAll(java.resolvedExtraProperties());
        props.set(KEY_DISTRIBUTION_JAVA_GROUP_ID, java.getGroupId(), "");
        props.set(KEY_DISTRIBUTION_JAVA_ARTIFACT_ID, java.getArtifactId(), "");
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_CLASS, java.getMainClass(), "");
        if (isNotBlank(java.getVersion())) {
            props.set(KEY_DISTRIBUTION_JAVA_VERSION, java.getVersion());
            SemanticVersion jv = SemanticVersion.of(java.getVersion());
            props.set(KEY_DISTRIBUTION_JAVA_VERSION_MAJOR, jv.getMajor(), "");
            props.set(KEY_DISTRIBUTION_JAVA_VERSION_MINOR, jv.getMinor(), "");
            props.set(KEY_DISTRIBUTION_JAVA_VERSION_PATCH, jv.getPatch(), "");
            props.set(KEY_DISTRIBUTION_JAVA_VERSION_TAG, jv.getTag(), "");
            props.set(KEY_DISTRIBUTION_JAVA_VERSION_BUILD, jv.getBuild(), "");
        } else {
            props.set(KEY_DISTRIBUTION_JAVA_VERSION, "");
            props.set(KEY_DISTRIBUTION_JAVA_VERSION_MAJOR, "");
            props.set(KEY_DISTRIBUTION_JAVA_VERSION_MINOR, "");
            props.set(KEY_DISTRIBUTION_JAVA_VERSION_PATCH, "");
            props.set(KEY_DISTRIBUTION_JAVA_VERSION_TAG, "");
            props.set(KEY_DISTRIBUTION_JAVA_VERSION_BUILD, "");
        }
        return props;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform.merge(platform);
    }

    @Override
    public String prefix() {
        return "distribution";
    }

    public org.jreleaser.model.Distribution.DistributionType getType() {
        return type;
    }

    public void setType(org.jreleaser.model.Distribution.DistributionType type) {
        if (type == org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE) {
            nag("NATIVE_IMAGE is deprecated since 1.4.0 and will be removed in 2.0.0. Use BINARY instead");
            this.type = org.jreleaser.model.Distribution.DistributionType.BINARY;
        } else {
            this.type = type;
        }
    }

    public void setType(String type) {
        this.type = org.jreleaser.model.Distribution.DistributionType.of(type);
    }

    public Stereotype getStereotype() {
        return stereotype;
    }

    public void setStereotype(Stereotype stereotype) {
        this.stereotype = stereotype;
    }

    public void setStereotype(String str) {
        setStereotype(Stereotype.of(str));
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

    public <T extends Packager<?>> T findPackager(String name) {
        if (isBlank(name)) {
            throw new JReleaserException(RB.$("ERROR_packager_name_not_blank"));
        }

        return resolvePackager(name);
    }

    private <T extends Packager<?>> T resolvePackager(String name) {
        switch (name.toLowerCase(Locale.ENGLISH).trim()) {
            case org.jreleaser.model.api.packagers.AppImagePackager.TYPE:
                return (T) getAppImage();
            case org.jreleaser.model.api.packagers.AsdfPackager.TYPE:
                return (T) getAsdf();
            case org.jreleaser.model.api.packagers.BrewPackager.TYPE:
                return (T) getBrew();
            case org.jreleaser.model.api.packagers.ChocolateyPackager.TYPE:
                return (T) getChocolatey();
            case org.jreleaser.model.api.packagers.DockerConfiguration.TYPE:
                return (T) getDocker();
            case org.jreleaser.model.api.packagers.FlatpakPackager.TYPE:
                return (T) getFlatpak();
            case org.jreleaser.model.api.packagers.GofishPackager.TYPE:
                return (T) getGofish();
            case org.jreleaser.model.api.packagers.JbangPackager.TYPE:
                return (T) getJbang();
            case org.jreleaser.model.api.packagers.JibConfiguration.TYPE:
                return (T) getJib();
            case org.jreleaser.model.api.packagers.MacportsPackager.TYPE:
                return (T) getMacports();
            case org.jreleaser.model.api.packagers.ScoopPackager.TYPE:
                return (T) getScoop();
            case org.jreleaser.model.api.packagers.SdkmanPackager.TYPE:
                return (T) getSdkman();
            case org.jreleaser.model.api.packagers.SnapPackager.TYPE:
                return (T) getSnap();
            case org.jreleaser.model.api.packagers.SpecPackager.TYPE:
                return (T) getSpec();
            case org.jreleaser.model.api.packagers.WingetPackager.TYPE:
                return (T) getWinget();
            default:
                throw new JReleaserException(RB.$("ERROR_unsupported_packager", name));
        }
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("active", getActive());
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
        props.put("stereotype", stereotype);
        props.put("extraProperties", getExtraProperties());
        if (java.isEnabled()) {
            props.put("java", java.asMap(full));
        }
        props.putAll(super.asMap(full));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(name, props);
        return map;
    }
}
