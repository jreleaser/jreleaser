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
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.jreleaser.gradle.plugin.dsl.AppImage
import org.jreleaser.gradle.plugin.dsl.Asdf
import org.jreleaser.gradle.plugin.dsl.Brew
import org.jreleaser.gradle.plugin.dsl.Chocolatey
import org.jreleaser.gradle.plugin.dsl.Docker
import org.jreleaser.gradle.plugin.dsl.Gofish
import org.jreleaser.gradle.plugin.dsl.Jbang
import org.jreleaser.gradle.plugin.dsl.Macports
import org.jreleaser.gradle.plugin.dsl.Packagers
import org.jreleaser.gradle.plugin.dsl.Scoop
import org.jreleaser.gradle.plugin.dsl.Sdkman
import org.jreleaser.gradle.plugin.dsl.Snap
import org.jreleaser.gradle.plugin.dsl.Spec
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class PackagersImpl implements Packagers {
    final AppImageImpl appImage
    final AsdfImpl asdf
    final BrewImpl brew
    final ChocolateyImpl chocolatey
    final DockerImpl docker
    final GofishImpl gofish
    final JbangImpl jbang
    final MacportsImpl macports
    final ScoopImpl scoop
    final SdkmanImpl sdkman
    final SnapImpl snap
    final SpecImpl spec

    @Inject
    PackagersImpl(ObjectFactory objects) {
        appImage = objects.newInstance(AppImageImpl, objects)
        asdf = objects.newInstance(AsdfImpl, objects)
        brew = objects.newInstance(BrewImpl, objects)
        chocolatey = objects.newInstance(ChocolateyImpl, objects)
        docker = objects.newInstance(DockerImpl, objects)
        gofish = objects.newInstance(GofishImpl, objects)
        jbang = objects.newInstance(JbangImpl, objects)
        macports = objects.newInstance(MacportsImpl, objects)
        scoop = objects.newInstance(ScoopImpl, objects)
        sdkman = objects.newInstance(SdkmanImpl, objects)
        snap = objects.newInstance(SnapImpl, objects)
        spec = objects.newInstance(SpecImpl, objects)
    }

    @Override
    void appImage(Action<? super AppImage> action) {
        action.execute(appImage)
    }

    @Override
    void asdf(Action<? super Asdf> action) {
        action.execute(asdf)
    }

    @Override
    void brew(Action<? super Brew> action) {
        action.execute(brew)
    }

    @Override
    void chocolatey(Action<? super Chocolatey> action) {
        action.execute(chocolatey)
    }

    @Override
    void docker(Action<? super Docker> action) {
        action.execute(docker)
    }

    @Override
    void gofish(Action<? super Gofish> action) {
        action.execute(gofish)
    }

    @Override
    void jbang(Action<? super Jbang> action) {
        action.execute(jbang)
    }

    @Override
    void macports(Action<? super Macports> action) {
        action.execute(macports)
    }

    @Override
    void scoop(Action<? super Scoop> action) {
        action.execute(scoop)
    }

    @Override
    void sdkman(Action<? super Sdkman> action) {
        action.execute(sdkman)
    }

    @Override
    void snap(Action<? super Snap> action) {
        action.execute(snap)
    }

    @Override
    void spec(Action<? super Spec> action) {
        action.execute(spec)
    }

    @Override
    void appImage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = AppImage) Closure<Void> action) {
        ConfigureUtil.configure(action, appImage)
    }

    @Override
    void asdf(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Asdf) Closure<Void> action) {
        ConfigureUtil.configure(action, asdf)
    }

    @Override
    void brew(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Brew) Closure<Void> action) {
        ConfigureUtil.configure(action, brew)
    }

    @Override
    void chocolatey(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Chocolatey) Closure<Void> action) {
        ConfigureUtil.configure(action, chocolatey)
    }

    @Override
    void docker(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Docker) Closure<Void> action) {
        ConfigureUtil.configure(action, docker)
    }

    @Override
    void gofish(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Gofish) Closure<Void> action) {
        ConfigureUtil.configure(action, gofish)
    }

    @Override
    void jbang(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Jbang) Closure<Void> action) {
        ConfigureUtil.configure(action, jbang)
    }

    @Override
    void macports(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Macports) Closure<Void> action) {
        ConfigureUtil.configure(action, macports)
    }

    @Override
    void scoop(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Scoop) Closure<Void> action) {
        ConfigureUtil.configure(action, scoop)
    }

    @Override
    void sdkman(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Sdkman) Closure<Void> action) {
        ConfigureUtil.configure(action, sdkman)
    }

    @Override
    void snap(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Snap) Closure<Void> action) {
        ConfigureUtil.configure(action, snap)
    }

    @Override
    void spec(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Spec) Closure<Void> action) {
        ConfigureUtil.configure(action, spec)
    }

    org.jreleaser.model.Packagers toModel() {
        org.jreleaser.model.Packagers packagers = new org.jreleaser.model.Packagers()
        if (appImage.isSet()) packagers.appImage = appImage.toModel()
        if (asdf.isSet()) packagers.asdf = asdf.toModel()
        if (brew.isSet()) packagers.brew = brew.toModel()
        if (chocolatey.isSet()) packagers.chocolatey = chocolatey.toModel()
        if (docker.isSet()) packagers.docker = docker.toModel()
        if (gofish.isSet()) packagers.gofish = gofish.toModel()
        if (jbang.isSet()) packagers.jbang = jbang.toModel()
        if (macports.isSet()) packagers.macports = macports.toModel()
        if (scoop.isSet()) packagers.scoop = scoop.toModel()
        if (sdkman.isSet()) packagers.sdkman = sdkman.toModel()
        if (snap.isSet()) packagers.snap = snap.toModel()
        if (spec.isSet()) packagers.spec = spec.toModel()
        packagers
    }
}
