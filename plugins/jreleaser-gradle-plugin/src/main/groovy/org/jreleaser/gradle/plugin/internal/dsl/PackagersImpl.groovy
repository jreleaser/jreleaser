/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
import org.jreleaser.gradle.plugin.dsl.Brew
import org.jreleaser.gradle.plugin.dsl.Chocolatey
import org.jreleaser.gradle.plugin.dsl.Docker
import org.jreleaser.gradle.plugin.dsl.Jbang
import org.jreleaser.gradle.plugin.dsl.Packagers
import org.jreleaser.gradle.plugin.dsl.Scoop
import org.jreleaser.gradle.plugin.dsl.Snap

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class PackagersImpl implements Packagers {
    final BrewImpl brew
    final ChocolateyImpl chocolatey
    final DockerImpl docker
    final JbangImpl jbang
    final ScoopImpl scoop
    final SnapImpl snap

    @Inject
    PackagersImpl(ObjectFactory objects) {
        brew = objects.newInstance(BrewImpl, objects)
        chocolatey = objects.newInstance(ChocolateyImpl, objects)
        docker = objects.newInstance(DockerImpl, objects)
        jbang = objects.newInstance(JbangImpl, objects)
        scoop = objects.newInstance(ScoopImpl, objects)
        snap = objects.newInstance(SnapImpl, objects)
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
    void jbang(Action<? super Jbang> action) {
        action.execute(jbang)
    }

    @Override
    void scoop(Action<? super Scoop> action) {
        action.execute(scoop)
    }

    @Override
    void snap(Action<? super Snap> action) {
        action.execute(snap)
    }

    org.jreleaser.model.Packagers toModel() {
        org.jreleaser.model.Packagers packagers = new org.jreleaser.model.Packagers()
        if (brew.isSet()) packagers.brew = brew.toModel()
        if (chocolatey.isSet()) packagers.chocolatey = chocolatey.toModel()
        if (docker.isSet()) packagers.docker = docker.toModel()
        if (jbang.isSet()) packagers.jbang = jbang.toModel()
        if (scoop.isSet()) packagers.scoop = scoop.toModel()
        if (snap.isSet()) packagers.snap = snap.toModel()
        packagers
    }
}
