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
package org.jreleaser.model.internal.packagers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Distribution.DistributionType.BINARY;
import static org.jreleaser.model.Distribution.DistributionType.FLAT_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_PACKAGE;
import static org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR;
import static org.jreleaser.model.JReleaserOutput.nag;
import static org.jreleaser.model.api.packagers.SnapPackager.SKIP_SNAP;
import static org.jreleaser.model.api.packagers.SnapPackager.TYPE;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.DEB;
import static org.jreleaser.util.FileType.JAR;
import static org.jreleaser.util.FileType.RPM;
import static org.jreleaser.util.FileType.TAR;
import static org.jreleaser.util.FileType.TAR_BZ2;
import static org.jreleaser.util.FileType.TAR_GZ;
import static org.jreleaser.util.FileType.TAR_XZ;
import static org.jreleaser.util.FileType.TBZ2;
import static org.jreleaser.util.FileType.TGZ;
import static org.jreleaser.util.FileType.TXZ;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isFalse;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class SnapPackager extends AbstractRepositoryPackager<org.jreleaser.model.api.packagers.SnapPackager, SnapPackager> {
    private static final Map<org.jreleaser.model.Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();
    private static final long serialVersionUID = 478495088345166846L;

    static {
        Set<String> extensions = setOf(
            TAR_BZ2.extension(),
            TAR_GZ.extension(),
            TAR_XZ.extension(),
            TBZ2.extension(),
            TGZ.extension(),
            TXZ.extension(),
            TAR.extension());

        SUPPORTED.put(NATIVE_IMAGE, extensions);
        SUPPORTED.put(BINARY, extensions);
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(NATIVE_PACKAGE, setOf(DEB.extension(), RPM.extension()));
        SUPPORTED.put(SINGLE_JAR, setOf(JAR.extension()));
        SUPPORTED.put(FLAT_BINARY, emptySet());
    }

    private final Set<String> localPlugs = new LinkedHashSet<>();
    private final Set<String> localSlots = new LinkedHashSet<>();
    private final List<Plug> plugs = new ArrayList<>();
    private final List<Slot> slots = new ArrayList<>();
    private final List<Architecture> architectures = new ArrayList<>();
    private final SnapRepository repository = new SnapRepository();

    private String packageName;
    private String base;
    private String grade;
    private String confinement;
    private String exportedLogin;
    private Boolean remoteBuild;

    @JsonIgnore
    private final org.jreleaser.model.api.packagers.SnapPackager immutable = new org.jreleaser.model.api.packagers.SnapPackager() {
        private static final long serialVersionUID = 7513685922871763049L;

        private List<? extends org.jreleaser.model.api.packagers.SnapPackager.Architecture> architectures;
        private List<? extends org.jreleaser.model.api.packagers.SnapPackager.Slot> slots;
        private List<? extends org.jreleaser.model.api.packagers.SnapPackager.Plug> plugs;

        @Override
        public String getPackageName() {
            return packageName;
        }

        @Override
        public String getBase() {
            return base;
        }

        @Override
        public String getGrade() {
            return grade;
        }

        @Override
        public String getConfinement() {
            return confinement;
        }

        @Override
        public Set<String> getLocalPlugs() {
            return unmodifiableSet(localPlugs);
        }

        @Override
        public Set<String> getLocalSlots() {
            return unmodifiableSet(localSlots);
        }

        @Override
        public List<? extends org.jreleaser.model.api.packagers.SnapPackager.Plug> getPlugs() {
            if (null == plugs) {
                plugs = SnapPackager.this.plugs.stream()
                    .map(SnapPackager.Plug::asImmutable)
                    .collect(toList());
            }
            return plugs;
        }

        @Override
        public List<? extends org.jreleaser.model.api.packagers.SnapPackager.Slot> getSlots() {
            if (null == slots) {
                slots = SnapPackager.this.slots.stream()
                    .map(SnapPackager.Slot::asImmutable)
                    .collect(toList());
            }
            return slots;
        }

        @Override
        public List<? extends org.jreleaser.model.api.packagers.SnapPackager.Architecture> getArchitectures() {
            if (null == architectures) {
                architectures = SnapPackager.this.architectures.stream()
                    .map(SnapPackager.Architecture::asImmutable)
                    .collect(toList());
            }
            return architectures;
        }

        @Override
        public String getExportedLogin() {
            return exportedLogin;
        }

        @Override
        public boolean isRemoteBuild() {
            return SnapPackager.this.isRemoteBuild();
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getRepository() {
            return repository.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getSnap() {
            return getRepository();
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getPackagerRepository() {
            return getRepository();
        }

        @Override
        public org.jreleaser.model.api.common.CommitAuthor getCommitAuthor() {
            return SnapPackager.this.getCommitAuthor().asImmutable();
        }

        @Override
        public String getTemplateDirectory() {
            return SnapPackager.this.getTemplateDirectory();
        }

        @Override
        public List<String> getSkipTemplates() {
            return unmodifiableList(SnapPackager.this.getSkipTemplates());
        }

        @Override
        public String getType() {
            return SnapPackager.this.getType();
        }

        @Override
        public String getDownloadUrl() {
            return SnapPackager.this.getDownloadUrl();
        }

        @Override
        public boolean supportsPlatform(String platform) {
            return SnapPackager.this.supportsPlatform(platform);
        }

        @Override
        public boolean supportsDistribution(Distribution.DistributionType distributionType) {
            return SnapPackager.this.supportsDistribution(distributionType);
        }

        @Override
        public Set<String> getSupportedFileExtensions(Distribution.DistributionType distributionType) {
            return SnapPackager.this.getSupportedFileExtensions(distributionType);
        }

        @Override
        public Set<Stereotype> getSupportedStereotypes() {
            return SnapPackager.this.getSupportedStereotypes();
        }

        @Override
        public boolean isSnapshotSupported() {
            return SnapPackager.this.isSnapshotSupported();
        }

        @Override
        public boolean isContinueOnError() {
            return SnapPackager.this.isContinueOnError();
        }

        @Override
        public Active getActive() {
            return SnapPackager.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return SnapPackager.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(SnapPackager.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return SnapPackager.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(SnapPackager.this.getExtraProperties());
        }
    };

    public SnapPackager() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.packagers.SnapPackager asImmutable() {
        return immutable;
    }

    @Override
    public void merge(SnapPackager source) {
        super.merge(source);
        this.packageName = merge(this.packageName, source.packageName);
        this.base = merge(this.base, source.base);
        this.grade = merge(this.grade, source.grade);
        this.confinement = merge(this.confinement, source.confinement);
        this.exportedLogin = merge(this.exportedLogin, source.exportedLogin);
        this.remoteBuild = merge(this.remoteBuild, source.remoteBuild);
        setLocalPlugs(merge(this.localPlugs, source.localPlugs));
        setLocalSlots(merge(this.localSlots, source.localSlots));
        setPlugs(merge(this.plugs, source.plugs));
        setSlots(merge(this.slots, source.slots));
        setArchitectures(merge(this.architectures, source.architectures));
        setRepository(source.repository);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getConfinement() {
        return confinement;
    }

    public void setConfinement(String confinement) {
        this.confinement = confinement;
    }

    public Set<String> getLocalPlugs() {
        return localPlugs;
    }

    public void setLocalPlugs(Set<String> localPlugs) {
        this.localPlugs.clear();
        this.localPlugs.addAll(localPlugs);
    }

    public Set<String> getLocalSlots() {
        return localSlots;
    }

    public void setLocalSlots(Set<String> localSlots) {
        this.localSlots.clear();
        this.localSlots.addAll(localSlots);
    }

    public List<Plug> getPlugs() {
        return plugs;
    }

    public void setPlugs(List<Plug> plugs) {
        this.plugs.clear();
        this.plugs.addAll(plugs);
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public void setSlots(List<Slot> slots) {
        this.slots.clear();
        this.slots.addAll(slots);
    }

    public List<Architecture> getArchitectures() {
        return architectures;
    }

    public void setArchitectures(List<Architecture> architectures) {
        this.architectures.clear();
        this.architectures.addAll(architectures);
    }

    public void addArchitecture(List<Architecture> architectures) {
        this.architectures.addAll(architectures);
    }

    public void addArchitecture(Architecture architecture) {
        if (null != architecture) {
            this.architectures.add(architecture);
        }
    }

    public String getExportedLogin() {
        return exportedLogin;
    }

    public void setExportedLogin(String exportedLogin) {
        this.exportedLogin = exportedLogin;
    }

    public boolean isRemoteBuild() {
        return null != remoteBuild && remoteBuild;
    }

    public void setRemoteBuild(Boolean remoteBuild) {
        this.remoteBuild = remoteBuild;
    }

    public boolean isRemoteBuildSet() {
        return null != remoteBuild;
    }

    public SnapRepository getRepository() {
        return repository;
    }

    public void setRepository(SnapRepository repository) {
        this.repository.merge(repository);
    }

    @Deprecated
    public SnapRepository getSnap() {
        return getRepository();
    }

    @Deprecated
    public void setSnap(SnapRepository repository) {
        nag("snap.snap is deprecated since 1.8.0 and will be removed in 2.0.0. Use snap.repository instead");
        setRepository(repository);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("packageName", packageName);
        props.put("base", base);
        props.put("grade", grade);
        props.put("confinement", confinement);
        props.put("exportedLogin", exportedLogin);
        props.put("remoteBuild", isRemoteBuild());
        props.put("repository", repository.asMap(full));
        props.put("localPlugs", localPlugs);
        props.put("localSlots", localSlots);

        Map<String, Map<String, Object>> mapped = new LinkedHashMap<>();
        for (int i = 0; i < plugs.size(); i++) {
            mapped.put("plug " + i, plugs.get(i).asMap(full));
        }
        props.put("plugs", mapped);

        mapped = new LinkedHashMap<>();
        for (int i = 0; i < slots.size(); i++) {
            mapped.put("slot " + i, slots.get(i).asMap(full));
        }
        props.put("slots", mapped);

        mapped = new LinkedHashMap<>();
        for (int i = 0; i < architectures.size(); i++) {
            mapped.put("architecture " + i, architectures.get(i).asMap(full));
        }
        props.put("architectures", mapped);
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return getRepository();
    }

    public PackagerRepository getPackagerRepository() {
        return getRepository();
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) || PlatformUtils.isUnix(platform);
    }

    @Override
    public boolean supportsDistribution(org.jreleaser.model.Distribution.DistributionType distributionType) {
        return SUPPORTED.containsKey(distributionType);
    }

    @Override
    public Set<String> getSupportedFileExtensions(org.jreleaser.model.Distribution.DistributionType distributionType) {
        return unmodifiableSet(SUPPORTED.getOrDefault(distributionType, emptySet()));
    }

    @Override
    protected boolean isNotSkipped(Artifact artifact) {
        return isFalse(artifact.getExtraProperties().get(SKIP_SNAP));
    }

    public static final class Attribute {
        public final String key;
        public final String value;

        public Attribute(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    public static final class Slot extends AbstractModelObject<Slot> implements Domain {
        private static final long serialVersionUID = 8422045649925759163L;

        private final Map<String, String> attributes = new LinkedHashMap<>();
        private final List<String> reads = new ArrayList<>();
        private final List<String> writes = new ArrayList<>();
        private String name;

        @JsonIgnore
        private final org.jreleaser.model.api.packagers.SnapPackager.Slot immutable = new org.jreleaser.model.api.packagers.SnapPackager.Slot() {
            private static final long serialVersionUID = -3518924698578544847L;

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Map<String, String> getAttributes() {
                return unmodifiableMap(attributes);
            }

            @Override
            public List<String> getReads() {
                return unmodifiableList(reads);
            }

            @Override
            public List<String> getWrites() {
                return unmodifiableList(writes);
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Slot.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.packagers.SnapPackager.Slot asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Slot source) {
            this.name = merge(this.name, source.name);
            setAttributes(merge(this.attributes, source.attributes));
            setReads(merge(this.reads, source.reads));
            setWrites(merge(this.writes, source.writes));
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, String> attributes) {
            this.attributes.clear();
            this.attributes.putAll(attributes);
        }

        public Collection<Attribute> getAttrs() {
            return attributes.entrySet().stream()
                .map(e -> new Attribute(e.getKey(), e.getValue()))
                .collect(toList());
        }

        public List<String> getReads() {
            return reads;
        }

        public void setReads(List<String> reads) {
            this.reads.clear();
            this.reads.addAll(reads);
        }

        public List<String> getWrites() {
            return writes;
        }

        public void setWrites(List<String> writes) {
            this.writes.clear();
            this.writes.addAll(writes);
        }

        public boolean getHasReads() {
            return !reads.isEmpty();
        }

        public boolean getHasWrites() {
            return !writes.isEmpty();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(name, attributes);
            map.put("read", reads);
            map.put("write", writes);
            return map;
        }

        public static Slot copyOf(Slot other) {
            Slot copy = new Slot();
            copy.setName(other.getName());
            copy.setAttributes(other.getAttributes());
            copy.setReads(other.getReads());
            copy.setWrites(other.getWrites());
            return copy;
        }
    }

    public static final class Plug extends AbstractModelObject<Plug> implements Domain {
        private static final long serialVersionUID = 8041902336260999261L;

        private final Map<String, String> attributes = new LinkedHashMap<>();
        private final List<String> reads = new ArrayList<>();
        private final List<String> writes = new ArrayList<>();
        private String name;

        @JsonIgnore
        private final org.jreleaser.model.api.packagers.SnapPackager.Plug immutable = new org.jreleaser.model.api.packagers.SnapPackager.Plug() {
            private static final long serialVersionUID = -5689359361910963388L;

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Map<String, String> getAttributes() {
                return unmodifiableMap(attributes);
            }

            @Override
            public List<String> getReads() {
                return unmodifiableList(reads);
            }

            @Override
            public List<String> getWrites() {
                return unmodifiableList(writes);
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Plug.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.packagers.SnapPackager.Plug asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Plug source) {
            this.name = merge(this.name, source.name);
            setAttributes(merge(this.attributes, source.attributes));
            setReads(merge(this.reads, source.reads));
            setWrites(merge(this.writes, source.writes));
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, String> attributes) {
            this.attributes.clear();
            this.attributes.putAll(attributes);
        }

        public Collection<Attribute> getAttrs() {
            return attributes.entrySet().stream()
                .map(e -> new Attribute(e.getKey(), e.getValue()))
                .collect(toList());
        }

        public List<String> getReads() {
            return reads;
        }

        public void setReads(List<String> reads) {
            this.reads.clear();
            this.reads.addAll(reads);
        }

        public List<String> getWrites() {
            return writes;
        }

        public void setWrites(List<String> writes) {
            this.writes.clear();
            this.writes.addAll(writes);
        }

        public boolean getHasReads() {
            return !reads.isEmpty();
        }

        public boolean getHasWrites() {
            return !writes.isEmpty();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(name, attributes);
            map.put("reads", reads);
            map.put("writes", writes);
            return map;
        }

        public static Plug copyOf(Plug other) {
            Plug copy = new Plug();
            copy.setName(other.getName());
            copy.setAttributes(other.getAttributes());
            copy.setReads(other.getReads());
            copy.setWrites(other.getWrites());
            return copy;
        }
    }

    public static final class SnapRepository extends PackagerRepository {
        private static final long serialVersionUID = 4117738159449060256L;

        public SnapRepository() {
            super("snap", "snap");
        }
    }

    public static final class Architecture extends AbstractModelObject<Architecture> implements Domain {
        private static final long serialVersionUID = 1878739013053454056L;

        private final List<String> buildOn = new ArrayList<>();
        private final List<String> runOn = new ArrayList<>();
        private Boolean ignoreError;

        @JsonIgnore
        private final org.jreleaser.model.api.packagers.SnapPackager.Architecture immutable = new org.jreleaser.model.api.packagers.SnapPackager.Architecture() {
            private static final long serialVersionUID = 7707062117835809382L;

            @Override
            public List<String> getBuildOn() {
                return unmodifiableList(buildOn);
            }

            @Override
            public List<String> getRunOn() {
                return unmodifiableList(runOn);
            }

            @Override
            public boolean isIgnoreError() {
                return Architecture.this.isIgnoreError();
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Architecture.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.packagers.SnapPackager.Architecture asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Architecture source) {
            this.ignoreError = merge(this.ignoreError, source.ignoreError);
            setBuildOn(merge(this.buildOn, source.buildOn));
            setRunOn(merge(this.runOn, source.runOn));
        }

        public List<String> getBuildOn() {
            return buildOn;
        }

        public void setBuildOn(List<String> buildOn) {
            this.buildOn.clear();
            this.buildOn.addAll(buildOn);
        }

        public List<String> getRunOn() {
            return runOn;
        }

        public void setRunOn(List<String> runOn) {
            this.runOn.clear();
            this.runOn.addAll(runOn);
        }

        public boolean hasBuildOn() {
            return !buildOn.isEmpty();
        }

        public boolean hasRunOn() {
            return !runOn.isEmpty();
        }

        public boolean isIgnoreError() {
            return null != ignoreError && ignoreError;
        }

        public void setIgnoreError(Boolean ignoreError) {
            this.ignoreError = ignoreError;
        }

        public boolean isIgnoreErrorSet() {
            return null != ignoreError;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("buildOn", buildOn);
            map.put("runOn", runOn);
            map.put("ignoreError", isIgnoreError());
            return map;
        }
    }
}
