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
package org.jreleaser.maven.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Snap extends AbstractRepositoryPackager {
    private final Set<String> localPlugs = new LinkedHashSet<>();
    private final Set<String> localSlots = new LinkedHashSet<>();
    private final List<Plug> plugs = new ArrayList<>();
    private final List<Slot> slots = new ArrayList<>();
    private final List<Architecture> architectures = new ArrayList<>();
    private final Tap snap = new Tap();
    private String packageName;
    private String base;
    private String grade;
    private String confinement;
    private File exportedLogin;
    private Boolean remoteBuild;

    void setAll(Snap snap) {
        super.setAll(snap);
        this.packageName = snap.packageName;
        this.base = snap.base;
        this.grade = snap.grade;
        this.confinement = snap.confinement;
        this.exportedLogin = snap.exportedLogin;
        this.remoteBuild = snap.remoteBuild;
        setLocalPlugs(localPlugs);
        setLocalSlots(localSlots);
        setPlugs(plugs);
        setSlots(slots);
        setSnap(snap.snap);
        setArchitectures(snap.architectures);
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

    public File getExportedLogin() {
        return exportedLogin;
    }

    public void setExportedLogin(File exportedLogin) {
        this.exportedLogin = exportedLogin;
    }

    public Boolean getRemoteBuild() {
        return remoteBuild;
    }

    public boolean isRemoteBuild() {
        return remoteBuild != null && remoteBuild;
    }

    public void setRemoteBuild(Boolean remoteBuild) {
        this.remoteBuild = remoteBuild;
    }

    public Tap getSnap() {
        return snap;
    }

    public void setSnap(Tap snap) {
        this.snap.setAll(snap);
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            isNotBlank(packageName) ||
            isNotBlank(base) ||
            isNotBlank(grade) ||
            isNotBlank(confinement) ||
            null != exportedLogin ||
            null != remoteBuild ||
            !localPlugs.isEmpty() ||
            !plugs.isEmpty() ||
            !slots.isEmpty() ||
            snap.isSet();
    }

    public static class Slot {
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
    }

    public static class Plug {
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
    }

    public static class Architecture {
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

        public List<String> getRunOn() {
            return runOn;
        }

        public void setRunOn(List<String> runOn) {
            this.runOn.clear();
            this.runOn.addAll(runOn);
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
    }
}
