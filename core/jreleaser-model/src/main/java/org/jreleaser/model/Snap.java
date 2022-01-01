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

import org.jreleaser.util.FileType;
import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Snap extends AbstractRepositoryTool {
    public static final String NAME = "snap";
    public static final String SKIP_SNAP = "skipSnap";

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
        super(NAME);
    }

    @Override
    public Set<String> getSupportedExtensions() {
        Set<String> set = new LinkedHashSet<>();
        set.add(FileType.TAR_BZ2.extension());
        set.add(FileType.TAR_GZ.extension());
        set.add(FileType.TAR_XZ.extension());
        set.add(FileType.TBZ2.extension());
        set.add(FileType.TGZ.extension());
        set.add(FileType.TXZ.extension());
        set.add(FileType.TAR.extension());
        return set;
    }

    void setAll(Snap snap) {
        super.setAll(snap);
        this.packageName = snap.packageName;
        this.base = snap.base;
        this.grade = snap.grade;
        this.confinement = snap.confinement;
        this.exportedLogin = snap.exportedLogin;
        this.remoteBuild = snap.remoteBuild;
        setLocalPlugs(snap.localPlugs);
        setLocalSlots(snap.localSlots);
        setPlugs(snap.plugs);
        setSlots(snap.slots);
        setArchitectures(snap.architectures);
        setSnap(snap.snap);
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

    public void addLocalPlugs(Set<String> localPlugs) {
        this.localPlugs.addAll(localPlugs);
    }

    public void addLocalPlug(String localPlug) {
        if (isNotBlank(localPlug)) {
            this.localPlugs.add(localPlug.trim());
        }
    }

    public void removeLocalPlug(String localPlug) {
        if (isNotBlank(localPlug)) {
            this.localPlugs.remove(localPlug.trim());
        }
    }

    public Set<String> getLocalSlots() {
        return localSlots;
    }

    public void setLocalSlots(Set<String> localSlots) {
        this.localSlots.clear();
        this.localSlots.addAll(localSlots);
    }

    public void addLocalSlots(Set<String> localSlots) {
        this.localSlots.addAll(localSlots);
    }

    public void addLocalSlot(String localSlot) {
        if (isNotBlank(localSlot)) {
            this.localSlots.add(localSlot.trim());
        }
    }

    public void removeLocalSlot(String localSlot) {
        if (isNotBlank(localSlot)) {
            this.localSlots.remove(localSlot.trim());
        }
    }

    public List<Plug> getPlugs() {
        return plugs;
    }

    public void setPlugs(List<Plug> plugs) {
        this.plugs.clear();
        this.plugs.addAll(plugs);
    }

    public void addPlugs(List<Plug> plugs) {
        this.plugs.addAll(plugs);
    }

    public void addPlug(Plug plug) {
        if (null != plug) {
            this.plugs.add(plug);
        }
    }

    public void removePlug(Plug plug) {
        if (null != plug) {
            this.plugs.remove(plug);
        }
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public void setSlots(List<Slot> slots) {
        this.slots.clear();
        this.slots.addAll(slots);
    }

    public void addSlots(List<Slot> slots) {
        this.slots.addAll(slots);
    }

    public void addSlot(Slot slot) {
        if (null != slot) {
            this.slots.add(slot);
        }
    }

    public void removeSlot(Slot slot) {
        if (null != slot) {
            this.slots.remove(slot);
        }
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

    public void removeArchitecture(Architecture architecture) {
        if (null != architecture) {
            this.architectures.remove(architecture);
        }
    }

    public String getExportedLogin() {
        return exportedLogin;
    }

    public void setExportedLogin(String exportedLogin) {
        this.exportedLogin = exportedLogin;
    }

    public boolean isRemoteBuild() {
        return remoteBuild != null && remoteBuild;
    }

    public void setRemoteBuild(Boolean remoteBuild) {
        this.remoteBuild = remoteBuild;
    }

    public boolean isRemoteBuildSet() {
        return remoteBuild != null;
    }

    public SnapTap getSnap() {
        return snap;
    }

    public void setSnap(SnapTap snap) {
        this.snap.setAll(snap);
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

    public static class Slot implements Domain {
        private final Map<String, String> attributes = new LinkedHashMap<>();
        private final List<String> reads = new ArrayList<>();
        private final List<String> writes = new ArrayList<>();
        private String name;

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

        public void addAttributes(Map<String, String> attributes) {
            this.attributes.putAll(attributes);
        }

        public void addAttribute(String key, String value) {
            attributes.put(key, value);
        }

        public List<String> getReads() {
            return reads;
        }

        public void setReads(List<String> reads) {
            this.reads.clear();
            this.reads.addAll(reads);
        }

        public void addReads(List<String> read) {
            this.reads.addAll(read);
        }

        public void addRead(String read) {
            if (isNotBlank(read)) {
                this.reads.add(read.trim());
            }
        }

        public void removeRead(String read) {
            if (isNotBlank(read)) {
                this.reads.remove(read.trim());
            }
        }

        public List<String> getWrites() {
            return writes;
        }

        public void setWrites(List<String> writes) {
            this.writes.clear();
            this.writes.addAll(writes);
        }

        public void addWrites(List<String> write) {
            this.writes.addAll(write);
        }

        public void addWrite(String write) {
            if (isNotBlank(write)) {
                this.writes.add(write.trim());
            }
        }

        public void removeWrite(String write) {
            if (isNotBlank(write)) {
                this.writes.remove(write.trim());
            }
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

    public static class Plug implements Domain {
        private final Map<String, String> attributes = new LinkedHashMap<>();
        private String name;

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

        public void addAttributes(Map<String, String> attributes) {
            this.attributes.putAll(attributes);
        }

        public void addAttribute(String key, String value) {
            attributes.put(key, value);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(name, attributes);
            return map;
        }

        public static Plug copyOf(Plug other) {
            Plug copy = new Plug();
            copy.setName(other.getName());
            copy.setAttributes(other.getAttributes());
            return copy;
        }
    }

    public static class SnapTap extends AbstractRepositoryTap {
        public SnapTap() {
            super("snap", "snap");
        }
    }

    public static class Architecture implements Domain {
        private final List<String> buildOn = new ArrayList<>();
        private final List<String> runOn = new ArrayList<>();
        private Boolean ignoreError;

        public List<String> getBuildOn() {
            return buildOn;
        }

        public void setBuildOn(List<String> buildOn) {
            this.buildOn.clear();
            this.buildOn.addAll(buildOn);
        }

        public void addBuildOn(List<String> buildOn) {
            this.buildOn.addAll(buildOn);
        }

        public void addBuildOn(String str) {
            if (isNotBlank(str)) {
                this.buildOn.add(str.trim());
            }
        }

        public List<String> getRunOn() {
            return runOn;
        }

        public void setRunOn(List<String> runOn) {
            this.runOn.clear();
            this.runOn.addAll(runOn);
        }

        public void addRunOn(List<String> runOn) {
            this.runOn.addAll(runOn);
        }

        public void addRunOn(String str) {
            if (isNotBlank(str)) {
                this.runOn.add(str.trim());
            }
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
