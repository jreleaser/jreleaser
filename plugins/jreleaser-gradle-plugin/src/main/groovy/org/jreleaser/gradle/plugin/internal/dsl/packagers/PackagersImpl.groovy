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
package org.jreleaser.gradle.plugin.internal.dsl.packagers

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.jreleaser.gradle.plugin.dsl.packagers.AppImagePackager
import org.jreleaser.gradle.plugin.dsl.packagers.AsdfPackager
import org.jreleaser.gradle.plugin.dsl.packagers.BrewPackager
import org.jreleaser.gradle.plugin.dsl.packagers.ChocolateyPackager
import org.jreleaser.gradle.plugin.dsl.packagers.DockerPackager
import org.jreleaser.gradle.plugin.dsl.packagers.FlatpakPackager
import org.jreleaser.gradle.plugin.dsl.packagers.GofishPackager
import org.jreleaser.gradle.plugin.dsl.packagers.JbangPackager
import org.jreleaser.gradle.plugin.dsl.packagers.JibPackager
import org.jreleaser.gradle.plugin.dsl.packagers.MacportsPackager
import org.jreleaser.gradle.plugin.dsl.packagers.Packagers
import org.jreleaser.gradle.plugin.dsl.packagers.ScoopPackager
import org.jreleaser.gradle.plugin.dsl.packagers.SdkmanPackager
import org.jreleaser.gradle.plugin.dsl.packagers.SnapPackager
import org.jreleaser.gradle.plugin.dsl.packagers.SpecPackager
import org.jreleaser.gradle.plugin.dsl.packagers.WingetPackager
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class PackagersImpl implements Packagers {
    final AppImagePackagerImpl appImage
    final AsdfPackagerImpl asdf
    final BrewPackagerImpl brew
    final ChocolateyPackagerImpl chocolatey
    final DockerPackagerImpl docker
    final FlatpakPackagerImpl flatpak
    final GofishPackagerImpl gofish
    final JbangPackagerImpl jbang
    final JibPackagerImpl jib
    final MacportsPackagerImpl macports
    final ScoopPackagerImpl scoop
    final SdkmanPackagerImpl sdkman
    final SnapPackagerImpl snap
    final SpecPackagerImpl spec
    final WingetPackagerImpl winget

    @Inject
    PackagersImpl(ObjectFactory objects) {
        appImage = objects.newInstance(AppImagePackagerImpl, objects)
        asdf = objects.newInstance(AsdfPackagerImpl, objects)
        brew = objects.newInstance(BrewPackagerImpl, objects)
        chocolatey = objects.newInstance(ChocolateyPackagerImpl, objects)
        docker = objects.newInstance(DockerPackagerImpl, objects)
        flatpak = objects.newInstance(FlatpakPackagerImpl, objects)
        gofish = objects.newInstance(GofishPackagerImpl, objects)
        jbang = objects.newInstance(JbangPackagerImpl, objects)
        jib = objects.newInstance(JibPackagerImpl, objects)
        macports = objects.newInstance(MacportsPackagerImpl, objects)
        scoop = objects.newInstance(ScoopPackagerImpl, objects)
        sdkman = objects.newInstance(SdkmanPackagerImpl, objects)
        snap = objects.newInstance(SnapPackagerImpl, objects)
        spec = objects.newInstance(SpecPackagerImpl, objects)
        winget = objects.newInstance(WingetPackagerImpl, objects)
    }

    @Override
    void appImage(Action<? super AppImagePackager> action) {
        action.execute(appImage)
    }

    @Override
    void asdf(Action<? super AsdfPackager> action) {
        action.execute(asdf)
    }

    @Override
    void brew(Action<? super BrewPackager> action) {
        action.execute(brew)
    }

    @Override
    void chocolatey(Action<? super ChocolateyPackager> action) {
        action.execute(chocolatey)
    }

    @Override
    void docker(Action<? super DockerPackager> action) {
        action.execute(docker)
    }

    @Override
    void flatpak(Action<? super FlatpakPackager> action) {
        action.execute(flatpak)
    }

    @Override
    void gofish(Action<? super GofishPackager> action) {
        action.execute(gofish)
    }

    @Override
    void jbang(Action<? super JbangPackager> action) {
        action.execute(jbang)
    }

    @Override
    void jib(Action<? super JibPackager> action) {
        action.execute(jib)
    }

    @Override
    void macports(Action<? super MacportsPackager> action) {
        action.execute(macports)
    }

    @Override
    void scoop(Action<? super ScoopPackager> action) {
        action.execute(scoop)
    }

    @Override
    void sdkman(Action<? super SdkmanPackager> action) {
        action.execute(sdkman)
    }

    @Override
    void snap(Action<? super SnapPackager> action) {
        action.execute(snap)
    }

    @Override
    void spec(Action<? super SpecPackager> action) {
        action.execute(spec)
    }

    @Override
    void winget(Action<? super WingetPackager> action) {
        action.execute(winget)
    }

    @Override
    @CompileDynamic
    void appImage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = AppImagePackager) Closure<Void> action) {
        ConfigureUtil.configure(action, appImage)
    }

    @Override
    @CompileDynamic
    void asdf(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = AsdfPackager) Closure<Void> action) {
        ConfigureUtil.configure(action, asdf)
    }

    @Override
    @CompileDynamic
    void brew(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = BrewPackager) Closure<Void> action) {
        ConfigureUtil.configure(action, brew)
    }

    @Override
    @CompileDynamic
    void chocolatey(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ChocolateyPackager) Closure<Void> action) {
        ConfigureUtil.configure(action, chocolatey)
    }

    @Override
    @CompileDynamic
    void docker(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DockerPackager) Closure<Void> action) {
        ConfigureUtil.configure(action, docker)
    }

    @Override
    @CompileDynamic
    void flatpak(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = FlatpakPackager) Closure<Void> action) {
        ConfigureUtil.configure(action, flatpak)
    }

    @Override
    @CompileDynamic
    void gofish(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GofishPackager) Closure<Void> action) {
        ConfigureUtil.configure(action, gofish)
    }

    @Override
    @CompileDynamic
    void jbang(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = JbangPackager) Closure<Void> action) {
        ConfigureUtil.configure(action, jbang)
    }

    @Override
    @CompileDynamic
    void jib(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = JibPackager) Closure<Void> action) {
        ConfigureUtil.configure(action, jib)
    }

    @Override
    @CompileDynamic
    void macports(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = MacportsPackager) Closure<Void> action) {
        ConfigureUtil.configure(action, macports)
    }

    @Override
    @CompileDynamic
    void scoop(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ScoopPackager) Closure<Void> action) {
        ConfigureUtil.configure(action, scoop)
    }

    @Override
    @CompileDynamic
    void sdkman(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SdkmanPackager) Closure<Void> action) {
        ConfigureUtil.configure(action, sdkman)
    }

    @Override
    @CompileDynamic
    void snap(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SnapPackager) Closure<Void> action) {
        ConfigureUtil.configure(action, snap)
    }

    @Override
    @CompileDynamic
    void spec(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SpecPackager) Closure<Void> action) {
        ConfigureUtil.configure(action, spec)
    }

    @Override
    @CompileDynamic
    void winget(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = WingetPackager) Closure<Void> action) {
        ConfigureUtil.configure(action, winget)
    }

    org.jreleaser.model.internal.packagers.Packagers toModel() {
        org.jreleaser.model.internal.packagers.Packagers packagers = new org.jreleaser.model.internal.packagers.Packagers()
        if (appImage.isSet()) packagers.appImage = appImage.toModel()
        if (asdf.isSet()) packagers.asdf = asdf.toModel()
        if (brew.isSet()) packagers.brew = brew.toModel()
        if (chocolatey.isSet()) packagers.chocolatey = chocolatey.toModel()
        if (docker.isSet()) packagers.docker = docker.toModel()
        if (flatpak.isSet()) packagers.flatpak = flatpak.toModel()
        if (gofish.isSet()) packagers.gofish = gofish.toModel()
        if (jbang.isSet()) packagers.jbang = jbang.toModel()
        if (jib.isSet()) packagers.jib = jib.toModel()
        if (macports.isSet()) packagers.macports = macports.toModel()
        if (scoop.isSet()) packagers.scoop = scoop.toModel()
        if (sdkman.isSet()) packagers.sdkman = sdkman.toModel()
        if (snap.isSet()) packagers.snap = snap.toModel()
        if (spec.isSet()) packagers.spec = spec.toModel()
        if (winget.isSet()) packagers.winget = winget.toModel()
        packagers
    }
}
