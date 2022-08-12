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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Packagers<S extends Packagers<S>> extends AbstractModelObject<S> implements Domain {
    protected final AppImage appimage = new AppImage();
    protected final Asdf asdf = new Asdf();
    protected final Brew brew = new Brew();
    protected final Chocolatey chocolatey = new Chocolatey();
    protected final Docker docker = new Docker();
    protected final Gofish gofish = new Gofish();
    protected final Jbang jbang = new Jbang();
    protected final Macports macports = new Macports();
    protected final Scoop scoop = new Scoop();
    protected final Sdkman sdkman = new Sdkman();
    protected final Snap snap = new Snap();
    protected final Spec spec = new Spec();

    public boolean hasEnabledPackagers() {
        return appimage.isEnabled() ||
            asdf.isEnabled() ||
            brew.isEnabled() ||
            chocolatey.isEnabled() ||
            docker.isEnabled() ||
            gofish.isEnabled() ||
            jbang.isEnabled() ||
            macports.isEnabled() ||
            scoop.isEnabled() ||
            sdkman.isEnabled() ||
            snap.isEnabled() ||
            spec.isEnabled();
    }

    @Override
    public void freeze() {
        super.freeze();
        appimage.freeze();
        asdf.freeze();
        brew.freeze();
        chocolatey.freeze();
        docker.freeze();
        gofish.freeze();
        jbang.freeze();
        macports.freeze();
        scoop.freeze();
        sdkman.freeze();
        snap.freeze();
        spec.freeze();
    }

    @Override
    public void merge(S packagers) {
        freezeCheck();
        setAppImage(packagers.appimage);
        setAsdf(packagers.asdf);
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

    public AppImage getAppImage() {
        return appimage;
    }

    public void setAppImage(AppImage appimage) {
        this.appimage.merge(appimage);
    }

    public Asdf getAsdf() {
        return asdf;
    }

    public void setAsdf(Asdf asdf) {
        this.asdf.merge(asdf);
    }

    public Brew getBrew() {
        return brew;
    }

    public void setBrew(Brew brew) {
        this.brew.merge(brew);
    }

    public Chocolatey getChocolatey() {
        return chocolatey;
    }

    public void setChocolatey(Chocolatey chocolatey) {
        this.chocolatey.merge(chocolatey);
    }

    public Docker getDocker() {
        return docker;
    }

    public void setDocker(Docker docker) {
        this.docker.merge(docker);
    }

    public Gofish getGofish() {
        return gofish;
    }

    public void setGofish(Gofish gofish) {
        this.gofish.merge(gofish);
    }

    public Jbang getJbang() {
        return jbang;
    }

    public void setJbang(Jbang jbang) {
        this.jbang.merge(jbang);
    }

    public Macports getMacports() {
        return macports;
    }

    public void setMacports(Macports macports) {
        this.macports.merge(macports);
    }

    public Scoop getScoop() {
        return scoop;
    }

    public void setScoop(Scoop scoop) {
        this.scoop.merge(scoop);
    }

    public Sdkman getSdkman() {
        return sdkman;
    }

    public void setSdkman(Sdkman sdkman) {
        this.sdkman.merge(sdkman);
    }

    public Snap getSnap() {
        return snap;
    }

    public void setSnap(Snap snap) {
        this.snap.merge(snap);
    }

    public Spec getSpec() {
        return spec;
    }

    public void setSpec(Spec spec) {
        this.spec.merge(spec);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.putAll(appimage.asMap(full));
        map.putAll(asdf.asMap(full));
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
