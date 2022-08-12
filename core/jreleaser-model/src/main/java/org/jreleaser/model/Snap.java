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

import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Distribution.DistributionType.BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_PACKAGE;
import static org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR;
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
public class Snap extends AbstractRepositoryPackager<Snap> {
    public static final String TYPE = "snap";
    public static final String SKIP_SNAP = "skipSnap";

    private static final Map<Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();

    static {
        Set<String> extensions = setOf(
            TAR_BZ2.extension(),
            TAR_GZ.extension(),
            TAR_XZ.extension(),
            TBZ2.extension(),
            TGZ.extension(),
            TXZ.extension(),
            TAR.extension());

        SUPPORTED.put(BINARY, extensions);
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(NATIVE_IMAGE, extensions);
        SUPPORTED.put(NATIVE_PACKAGE, setOf(DEB.extension(), RPM.extension()));
        SUPPORTED.put(SINGLE_JAR, setOf(JAR.extension()));
    }

    private final Set<String> localPlugs = new LinkedHashSet<>();
    private final Set<String> localSlots = new LinkedHashSet<>();
    private final List<Plug> plugs = new ArrayList<>();
    private final List<Slot> slots = new ArrayList<>();
    private final List<Architecture> architectures = new ArrayList<>();
    private final SnapTap snap = new SnapTap();

    private String packageName;
    private String base = "core20";
    private String grade = "stable";
    private String confinement = "strict";
    private String exportedLogin;
    private Boolean remoteBuild;

    public Snap() {
        super(TYPE);
    }

    @Override
    public void freeze() {
        super.freeze();
        plugs.forEach(Plug::freeze);
        slots.forEach(Slot::freeze);
        architectures.forEach(Architecture::freeze);
        snap.freeze();
    }

    @Override
    public void merge(Snap snap) {
        freezeCheck();
        super.merge(snap);
        this.packageName = merge(this.packageName, snap.packageName);
        this.base = merge(this.base, snap.base);
        this.grade = merge(this.grade, snap.grade);
        this.confinement = merge(this.confinement, snap.confinement);
        this.exportedLogin = merge(this.exportedLogin, snap.exportedLogin);
        this.remoteBuild = merge(this.remoteBuild, snap.remoteBuild);
        setLocalPlugs(merge(this.localPlugs, snap.localPlugs));
        setLocalSlots(merge(this.localSlots, snap.localSlots));
        setPlugs(merge(this.plugs, snap.plugs));
        setSlots(merge(this.slots, snap.slots));
        setArchitectures(merge(this.architectures, snap.architectures));
        setSnap(snap.snap);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        freezeCheck();
        this.packageName = packageName;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        freezeCheck();
        this.base = base;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        freezeCheck();
        this.grade = grade;
    }

    public String getConfinement() {
        return confinement;
    }

    public void setConfinement(String confinement) {
        freezeCheck();
        this.confinement = confinement;
    }

    public Set<String> getLocalPlugs() {
        return freezeWrap(localPlugs);
    }

    public void setLocalPlugs(Set<String> localPlugs) {
        freezeCheck();
        this.localPlugs.clear();
        this.localPlugs.addAll(localPlugs);
    }

    public Set<String> getLocalSlots() {
        return freezeWrap(localSlots);
    }

    public void setLocalSlots(Set<String> localSlots) {
        freezeCheck();
        this.localSlots.clear();
        this.localSlots.addAll(localSlots);
    }

    public List<Plug> getPlugs() {
        return freezeWrap(plugs);
    }

    public void setPlugs(List<Plug> plugs) {
        freezeCheck();
        this.plugs.clear();
        this.plugs.addAll(plugs);
    }

    public List<Slot> getSlots() {
        return freezeWrap(slots);
    }

    public void setSlots(List<Slot> slots) {
        freezeCheck();
        this.slots.clear();
        this.slots.addAll(slots);
    }

    public List<Architecture> getArchitectures() {
        return freezeWrap(architectures);
    }

    public void setArchitectures(List<Architecture> architectures) {
        freezeCheck();
        this.architectures.clear();
        this.architectures.addAll(architectures);
    }

    public void addArchitecture(List<Architecture> architectures) {
        freezeCheck();
        this.architectures.addAll(architectures);
    }

    public void addArchitecture(Architecture architecture) {
        freezeCheck();
        if (null != architecture) {
            this.architectures.add(architecture);
        }
    }

    public String getExportedLogin() {
        return exportedLogin;
    }

    public void setExportedLogin(String exportedLogin) {
        freezeCheck();
        this.exportedLogin = exportedLogin;
    }

    public boolean isRemoteBuild() {
        return remoteBuild != null && remoteBuild;
    }

    public void setRemoteBuild(Boolean remoteBuild) {
        freezeCheck();
        this.remoteBuild = remoteBuild;
    }

    public boolean isRemoteBuildSet() {
        return remoteBuild != null;
    }

    public SnapTap getSnap() {
        return snap;
    }

    public void setSnap(SnapTap snap) {
        this.snap.merge(snap);
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
        props.put("snap", snap.asMap(full));
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
        return snap;
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) || PlatformUtils.isUnix(platform);
    }

    @Override
    public boolean supportsDistribution(Distribution distribution) {
        return SUPPORTED.containsKey(distribution.getType());
    }

    @Override
    public Set<String> getSupportedExtensions(Distribution distribution) {
        return Collections.unmodifiableSet(SUPPORTED.getOrDefault(distribution.getType(), Collections.emptySet()));
    }

    @Override
    protected boolean isNotSkipped(Artifact artifact) {
        return isFalse(artifact.getExtraProperties().get(SKIP_SNAP));
    }

    public static class Attribute {
        public final String key;
        public final String value;

        public Attribute(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    public static class Slot extends AbstractModelObject<Slot> implements Domain {
        private final Map<String, String> attributes = new LinkedHashMap<>();
        private final List<String> reads = new ArrayList<>();
        private final List<String> writes = new ArrayList<>();
        private String name;

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
            freezeCheck();
            this.name = name;
        }

        public Map<String, String> getAttributes() {
            return freezeWrap(attributes);
        }

        public void setAttributes(Map<String, String> attributes) {
            freezeCheck();
            this.attributes.clear();
            this.attributes.putAll(attributes);
        }

        public Collection<Attribute> getAttrs() {
            return attributes.entrySet().stream()
                .map(e -> new Attribute(e.getKey(), e.getValue()))
                .collect(toList());
        }

        public List<String> getReads() {
            return freezeWrap(reads);
        }

        public void setReads(List<String> reads) {
            freezeCheck();
            this.reads.clear();
            this.reads.addAll(reads);
        }

        public List<String> getWrites() {
            return freezeWrap(writes);
        }

        public void setWrites(List<String> writes) {
            freezeCheck();
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

    public static class Plug extends AbstractModelObject<Plug> implements Domain {
        private final Map<String, String> attributes = new LinkedHashMap<>();
        private final List<String> reads = new ArrayList<>();
        private final List<String> writes = new ArrayList<>();
        private String name;

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
            freezeCheck();
            this.name = name;
        }

        public Map<String, String> getAttributes() {
            return freezeWrap(attributes);
        }

        public void setAttributes(Map<String, String> attributes) {
            freezeCheck();
            this.attributes.clear();
            this.attributes.putAll(attributes);
        }

        public Collection<Attribute> getAttrs() {
            return attributes.entrySet().stream()
                .map(e -> new Attribute(e.getKey(), e.getValue()))
                .collect(toList());
        }

        public List<String> getReads() {
            return freezeWrap(reads);
        }

        public void setReads(List<String> reads) {
            freezeCheck();
            this.reads.clear();
            this.reads.addAll(reads);
        }

        public List<String> getWrites() {
            return freezeWrap(writes);
        }

        public void setWrites(List<String> writes) {
            freezeCheck();
            this.writes.clear();
            this.writes.addAll(writes);
        }

        public boolean getHasRead() {
            return !reads.isEmpty();
        }

        public boolean getHasWrite() {
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

    public static class SnapTap extends AbstractRepositoryTap<SnapTap> {
        public SnapTap() {
            super("snap", "snap");
        }
    }

    public static class Architecture extends AbstractModelObject<Architecture> implements Domain {
        private final List<String> buildOn = new ArrayList<>();
        private final List<String> runOn = new ArrayList<>();
        private Boolean ignoreError;

        @Override
        public void merge(Architecture source) {
            this.ignoreError = merge(this.ignoreError, source.ignoreError);
            setBuildOn(merge(this.buildOn, source.buildOn));
            setRunOn(merge(this.runOn, source.runOn));
        }

        public List<String> getBuildOn() {
            return freezeWrap(buildOn);
        }

        public void setBuildOn(List<String> buildOn) {
            freezeCheck();
            this.buildOn.clear();
            this.buildOn.addAll(buildOn);
        }

        public List<String> getRunOn() {
            return freezeWrap(runOn);
        }

        public void setRunOn(List<String> runOn) {
            freezeCheck();
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
            return ignoreError != null && ignoreError;
        }

        public void setIgnoreError(Boolean ignoreError) {
            freezeCheck();
            this.ignoreError = ignoreError;
        }

        public boolean isIgnoreErrorSet() {
            return ignoreError != null;
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
