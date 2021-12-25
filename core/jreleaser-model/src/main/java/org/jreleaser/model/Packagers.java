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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Packagers implements Domain {
    private final Brew brew = new Brew();
    private final Chocolatey chocolatey = new Chocolatey();
    private final Docker docker = new Docker();
    private final Gofish gofish = new Gofish();
    private final Jbang jbang = new Jbang();
    private final Macports macports = new Macports();
    private final Scoop scoop = new Scoop();
    private final Sdkman sdkman = new Sdkman();
    private final Snap snap = new Snap();
    private final Spec spec = new Spec();

    public boolean hasEnabledPackagers() {
        return brew.isEnabled() ||
            chocolatey.isEnabled() ||
            docker.isEnabled() ||
            gofish.isEnabled() ||
            jbang.isEnabled() ||
            macports.isEnabled() ||
            scoop.isEnabled() ||
            sdkman.isEnabled() ||
            snap.isEnabled()||
            spec.isEnabled();
    }

    void setAll(Packagers packagers) {
        setBrew(packagers.brew);
        setChocolatey(packagers.chocolatey);
        setDocker(packagers.docker);
        setGofish(packagers.gofish);
        setJbang(packagers.jbang);
        setMacports(packagers.macports);
        setScoop(packagers.scoop);
        setSdkman(packagers.sdkman);
        setSnap(packagers.snap);
        setSpec(packagers.spec);
    }

    public Brew getBrew() {
        return brew;
    }

    public void setBrew(Brew brew) {
        this.brew.setAll(brew);
    }

    public Chocolatey getChocolatey() {
        return chocolatey;
    }

    public void setChocolatey(Chocolatey chocolatey) {
        this.chocolatey.setAll(chocolatey);
    }

    public Docker getDocker() {
        return docker;
    }

    public void setDocker(Docker docker) {
        this.docker.setAll(docker);
    }

    public Gofish getGofish() {
        return gofish;
    }

    public void setGofish(Gofish gofish) {
        this.gofish.setAll(gofish);
    }

    public Jbang getJbang() {
        return jbang;
    }

    public void setJbang(Jbang jbang) {
        this.jbang.setAll(jbang);
    }

    public Macports getMacports() {
        return macports;
    }

    public void setMacports(Macports macports) {
        this.macports.setAll(macports);
    }

    public Scoop getScoop() {
        return scoop;
    }

    public void setScoop(Scoop scoop) {
        this.scoop.setAll(scoop);
    }

    public Sdkman getSdkman() {
        return sdkman;
    }

    public void setSdkman(Sdkman sdkman) {
        this.sdkman.setAll(sdkman);
    }

    public Snap getSnap() {
        return snap;
    }

    public void setSnap(Snap snap) {
        this.snap.setAll(snap);
    }

    public Spec getSpec() {
        return spec;
    }

    public void setSpec(Spec spec) {
        this.spec.setAll(spec);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.putAll(brew.asMap(full));
        map.putAll(chocolatey.asMap(full));
        map.putAll(docker.asMap(full));
        map.putAll(gofish.asMap(full));
        map.putAll(jbang.asMap(full));
        map.putAll(macports.asMap(full));
        map.putAll(scoop.asMap(full));
        map.putAll(sdkman.asMap(full));
        map.putAll(snap.asMap(full));
        map.putAll(spec.asMap(full));
        return map;
    }
}
