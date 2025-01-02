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
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.Domain;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Packagers<S extends Packagers<S>> extends AbstractActivatable<S> implements Domain {
    private static final long serialVersionUID = 6799849630017240270L;

    protected final AppImagePackager appImage = new AppImagePackager();
    protected final AsdfPackager asdf = new AsdfPackager();
    protected final BrewPackager brew = new BrewPackager();
    protected final ChocolateyPackager chocolatey = new ChocolateyPackager();
    protected final DockerPackager docker = new DockerPackager();
    protected final FlatpakPackager flatpak = new FlatpakPackager();
    protected final GofishPackager gofish = new GofishPackager();
    protected final JbangPackager jbang = new JbangPackager();
    protected final JibPackager jib = new JibPackager();
    protected final MacportsPackager macports = new MacportsPackager();
    protected final ScoopPackager scoop = new ScoopPackager();
    protected final SdkmanPackager sdkman = new SdkmanPackager();
    protected final SnapPackager snap = new SnapPackager();
    protected final SpecPackager spec = new SpecPackager();
    protected final WingetPackager winget = new WingetPackager();

    @JsonIgnore
    private final org.jreleaser.model.api.packagers.Packagers immutable = new org.jreleaser.model.api.packagers.Packagers() {
        private static final long serialVersionUID = -613241828881201495L;

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
            return unmodifiableMap(Packagers.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.packagers.Packagers asImmutable() {
        return immutable;
    }

    public boolean hasEnabledPackagers() {
        return appImage.isEnabled() ||
            asdf.isEnabled() ||
            brew.isEnabled() ||
            chocolatey.isEnabled() ||
            docker.isEnabled() ||
            flatpak.isEnabled() ||
            gofish.isEnabled() ||
            jbang.isEnabled() ||
            jib.isEnabled() ||
            macports.isEnabled() ||
            scoop.isEnabled() ||
            sdkman.isEnabled() ||
            snap.isEnabled() ||
            spec.isEnabled() ||
            winget.isEnabled();
    }

    @Override
    public void merge(S source) {
        super.merge(source);
        setAppImage(source.appImage);
        setAsdf(source.asdf);
        setBrew(source.brew);
        setChocolatey(source.chocolatey);
        setDocker(source.docker);
        setFlatpak(source.flatpak);
        setGofish(source.gofish);
        setJbang(source.jbang);
        setJib(source.jib);
        setMacports(source.macports);
        setScoop(source.scoop);
        setSdkman(source.sdkman);
        setSnap(source.snap);
        setSpec(source.spec);
        setWinget(source.winget);
    }

    public AppImagePackager getAppImage() {
        return appImage;
    }

    public void setAppImage(AppImagePackager appImage) {
        this.appImage.merge(appImage);
    }

    public AsdfPackager getAsdf() {
        return asdf;
    }

    public void setAsdf(AsdfPackager asdf) {
        this.asdf.merge(asdf);
    }

    public BrewPackager getBrew() {
        return brew;
    }

    public void setBrew(BrewPackager brew) {
        this.brew.merge(brew);
    }

    public ChocolateyPackager getChocolatey() {
        return chocolatey;
    }

    public void setChocolatey(ChocolateyPackager chocolatey) {
        this.chocolatey.merge(chocolatey);
    }

    public DockerPackager getDocker() {
        return docker;
    }

    public void setDocker(DockerPackager docker) {
        this.docker.merge(docker);
    }

    public GofishPackager getGofish() {
        return gofish;
    }

    public void setGofish(GofishPackager gofish) {
        this.gofish.merge(gofish);
    }

    public FlatpakPackager getFlatpak() {
        return flatpak;
    }

    public void setFlatpak(FlatpakPackager flatpak) {
        this.flatpak.merge(flatpak);
    }

    public JbangPackager getJbang() {
        return jbang;
    }

    public void setJbang(JbangPackager jbang) {
        this.jbang.merge(jbang);
    }

    public JibPackager getJib() {
        return jib;
    }

    public void setJib(JibPackager jib) {
        this.jib.merge(jib);
    }

    public MacportsPackager getMacports() {
        return macports;
    }

    public void setMacports(MacportsPackager macports) {
        this.macports.merge(macports);
    }

    public ScoopPackager getScoop() {
        return scoop;
    }

    public void setScoop(ScoopPackager scoop) {
        this.scoop.merge(scoop);
    }

    public SdkmanPackager getSdkman() {
        return sdkman;
    }

    public void setSdkman(SdkmanPackager sdkman) {
        this.sdkman.merge(sdkman);
    }

    public SnapPackager getSnap() {
        return snap;
    }

    public void setSnap(SnapPackager snap) {
        this.snap.merge(snap);
    }

    public SpecPackager getSpec() {
        return spec;
    }

    public void setSpec(SpecPackager spec) {
        this.spec.merge(spec);
    }

    public WingetPackager getWinget() {
        return winget;
    }

    public void setWinget(WingetPackager winget) {
        this.winget.merge(winget);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.putAll(appImage.asMap(full));
        map.putAll(asdf.asMap(full));
        map.putAll(brew.asMap(full));
        map.putAll(chocolatey.asMap(full));
        map.putAll(docker.asMap(full));
        map.putAll(flatpak.asMap(full));
        map.putAll(gofish.asMap(full));
        map.putAll(jbang.asMap(full));
        map.putAll(jib.asMap(full));
        map.putAll(macports.asMap(full));
        map.putAll(scoop.asMap(full));
        map.putAll(sdkman.asMap(full));
        map.putAll(snap.asMap(full));
        map.putAll(spec.asMap(full));
        map.putAll(winget.asMap(full));
        return map;
    }
}
