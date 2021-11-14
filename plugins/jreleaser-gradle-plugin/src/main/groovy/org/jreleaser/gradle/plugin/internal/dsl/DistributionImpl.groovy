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
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.Artifact
import org.jreleaser.gradle.plugin.dsl.Brew
import org.jreleaser.gradle.plugin.dsl.Chocolatey
import org.jreleaser.gradle.plugin.dsl.Distribution
import org.jreleaser.gradle.plugin.dsl.Docker
import org.jreleaser.gradle.plugin.dsl.Java
import org.jreleaser.gradle.plugin.dsl.Jbang
import org.jreleaser.gradle.plugin.dsl.Scoop
import org.jreleaser.gradle.plugin.dsl.Sdkman
import org.jreleaser.gradle.plugin.dsl.Snap
import org.jreleaser.model.Active
import org.jreleaser.model.Distribution.DistributionType
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class DistributionImpl implements Distribution {
    String name
    final Property<String> executable
    final Property<String> executableExtension
    final Property<String> groupId
    final Property<String> artifactId
    final Property<Active> active
    final Property<DistributionType> distributionType
    final ListProperty<String> tags
    final MapProperty<String, Object> extraProperties
    final BrewImpl brew
    final ChocolateyImpl chocolatey
    final DockerImpl docker
    final JbangImpl jbang
    final ScoopImpl scoop
    final SdkmanImpl sdkman
    final SnapImpl snap
    final JavaImpl java

    final NamedDomainObjectContainer<ArtifactImpl> artifacts

    @Inject
    DistributionImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.notDefined())
        executable = objects.property(String).convention(Providers.notDefined())
        executableExtension = objects.property(String).convention(Providers.notDefined())
        groupId = objects.property(String).convention(Providers.notDefined())
        artifactId = objects.property(String).convention(Providers.notDefined())
        distributionType = objects.property(DistributionType).convention(DistributionType.JAVA_BINARY)
        tags = objects.listProperty(String).convention(Providers.notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())

        artifacts = objects.domainObjectContainer(ArtifactImpl, new NamedDomainObjectFactory<ArtifactImpl>() {
            @Override
            ArtifactImpl create(String name) {
                ArtifactImpl artifact = objects.newInstance(ArtifactImpl, objects)
                artifact.name = name
                artifact
            }
        })

        java = objects.newInstance(JavaImpl, objects)

        brew = objects.newInstance(BrewImpl, objects)
        chocolatey = objects.newInstance(ChocolateyImpl, objects)
        docker = objects.newInstance(DockerImpl, objects)
        jbang = objects.newInstance(JbangImpl, objects)
        scoop = objects.newInstance(ScoopImpl, objects)
        sdkman = objects.newInstance(SdkmanImpl, objects)
        snap = objects.newInstance(SnapImpl, objects)
    }

    @Override
    void setDistributionType(String distributionType) {
        this.distributionType.set(DistributionType.of(distributionType))
    }

    @Override
    void addTag(String tag) {
        if (isNotBlank(tag)) {
            tags.add(tag.trim())
        }
    }

    @Override
    void artifact(Action<? super Artifact> action) {
        action.execute(artifacts.maybeCreate("artifact-${artifacts.size()}".toString()))
    }

    @Override
    void java(Action<? super Java> action) {
        action.execute(java)
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
    void sdkman(Action<? super Sdkman> action) {
        action.execute(sdkman)
    }

    @Override
    void snap(Action<? super Snap> action) {
        action.execute(snap)
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void artifact(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, artifacts.maybeCreate("artifact-${artifacts.size()}".toString()))
    }

    @Override
    void java(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Java) Closure<Void> action) {
        ConfigureUtil.configure(action, java)
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
    void jbang(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Jbang) Closure<Void> action) {
        ConfigureUtil.configure(action, jbang)
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

    org.jreleaser.model.Distribution toModel() {
        org.jreleaser.model.Distribution distribution = new org.jreleaser.model.Distribution()
        distribution.name = name
        if (active.present) distribution.active = active.get()
        if (executable.present) distribution.executable = executable.get()
        if (executableExtension.present) distribution.executableExtension = executableExtension.get()
        distribution.type = distributionType.get()
        distribution.java = java.toModel()
        for (ArtifactImpl artifact : artifacts) {
            distribution.addArtifact(artifact.toModel())
        }
        distribution.tags = (List<String>) tags.getOrElse([])
        if (extraProperties.present) distribution.extraProperties.putAll(extraProperties.get())
        if (brew.isSet()) distribution.brew = brew.toModel()
        if (chocolatey.isSet()) distribution.chocolatey = chocolatey.toModel()
        if (docker.isSet()) distribution.docker = docker.toModel()
        if (jbang.isSet()) distribution.jbang = jbang.toModel()
        if (scoop.isSet()) distribution.scoop = scoop.toModel()
        if (sdkman.isSet()) distribution.sdkman = sdkman.toModel()
        if (snap.isSet()) distribution.snap = snap.toModel()
        distribution
    }
}
