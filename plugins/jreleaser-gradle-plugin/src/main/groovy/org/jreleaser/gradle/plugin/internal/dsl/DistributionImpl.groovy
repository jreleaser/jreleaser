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
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.AppImage
import org.jreleaser.gradle.plugin.dsl.Artifact
import org.jreleaser.gradle.plugin.dsl.Asdf
import org.jreleaser.gradle.plugin.dsl.Brew
import org.jreleaser.gradle.plugin.dsl.Chocolatey
import org.jreleaser.gradle.plugin.dsl.Distribution
import org.jreleaser.gradle.plugin.dsl.Docker
import org.jreleaser.gradle.plugin.dsl.Gofish
import org.jreleaser.gradle.plugin.dsl.Java
import org.jreleaser.gradle.plugin.dsl.Jbang
import org.jreleaser.gradle.plugin.dsl.Macports
import org.jreleaser.gradle.plugin.dsl.Platform
import org.jreleaser.gradle.plugin.dsl.Scoop
import org.jreleaser.gradle.plugin.dsl.Sdkman
import org.jreleaser.gradle.plugin.dsl.Snap
import org.jreleaser.gradle.plugin.dsl.Spec
import org.jreleaser.model.Active
import org.jreleaser.model.Distribution.DistributionType
import org.jreleaser.model.Stereotype
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
    final Property<String> groupId
    final Property<String> artifactId
    final Property<Active> active
    final Property<Stereotype> stereotype
    final Property<DistributionType> distributionType
    final ListProperty<String> tags
    final MapProperty<String, Object> extraProperties
    final ExecutableImpl executable
    final JavaImpl java
    final PlatformImpl platform
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

    final NamedDomainObjectContainer<ArtifactImpl> artifacts

    @Inject
    DistributionImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.notDefined())
        stereotype = objects.property(Stereotype).convention(Providers.notDefined())
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

        executable = objects.newInstance(ExecutableImpl, objects)
        java = objects.newInstance(JavaImpl, objects)
        platform = objects.newInstance(PlatformImpl, objects)
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
    void setDistributionType(String str) {
        if (isNotBlank(str)) {
            this.distributionType.set(DistributionType.of(str.trim()))
        }
    }

    @Override
    void setStereotype(String str) {
        if (isNotBlank(str)) {
            stereotype.set(Stereotype.of(str.trim()))
        }
    }

    @Override
    void tag(String tag) {
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
    void platform(Action<? super Platform> action) {
        action.execute(platform)
    }

    @Override
    void executable(Action<? super Executable> action) {
        action.execute(executable)
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
    void platform(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Platform) Closure<Void> action) {
        ConfigureUtil.configure(action, platform)
    }

    @Override
    void executable(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Executable) Closure<Void> action) {
        ConfigureUtil.configure(action, executable)
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

    org.jreleaser.model.Distribution toModel() {
        org.jreleaser.model.Distribution distribution = new org.jreleaser.model.Distribution()
        distribution.name = name
        if (active.present) distribution.active = active.get()
        if (stereotype.present) distribution.stereotype = stereotype.get()
        if (executable.isSet()) distribution.executable = executable.toModel()
        distribution.type = distributionType.get()
        distribution.java = java.toModel()
        distribution.platform = platform.toModel()
        for (ArtifactImpl artifact : artifacts) {
            distribution.addArtifact(artifact.toModel())
        }
        distribution.tags = (List<String>) tags.getOrElse([])
        if (extraProperties.present) distribution.extraProperties.putAll(extraProperties.get())
        if (appImage.isSet()) distribution.appImage = appImage.toModel()
        if (asdf.isSet()) distribution.asdf = asdf.toModel()
        if (brew.isSet()) distribution.brew = brew.toModel()
        if (chocolatey.isSet()) distribution.chocolatey = chocolatey.toModel()
        if (docker.isSet()) distribution.docker = docker.toModel()
        if (gofish.isSet()) distribution.gofish = gofish.toModel()
        if (jbang.isSet()) distribution.jbang = jbang.toModel()
        if (macports.isSet()) distribution.macports = macports.toModel()
        if (scoop.isSet()) distribution.scoop = scoop.toModel()
        if (sdkman.isSet()) distribution.sdkman = sdkman.toModel()
        if (snap.isSet()) distribution.snap = snap.toModel()
        if (spec.isSet()) distribution.spec = spec.toModel()
        distribution
    }

    @CompileStatic
    static class ExecutableImpl implements Executable {
        final Property<String> name
        final Property<String> unixExtension
        final Property<String> windowsExtension

        @Inject
        ExecutableImpl(ObjectFactory objects) {
            name = objects.property(String).convention(Providers.notDefined())
            unixExtension = objects.property(String).convention(Providers.notDefined())
            windowsExtension = objects.property(String).convention(Providers.notDefined())
        }

        @Internal
        boolean isSet() {
            name.present ||
                unixExtension.present ||
                windowsExtension.present
        }

        org.jreleaser.model.Distribution.Executable toModel() {
            org.jreleaser.model.Distribution.Executable executable = new org.jreleaser.model.Distribution.Executable()
            if (name.present) executable.name = name.get()
            if (unixExtension.present) executable.unixExtension = unixExtension.get()
            if (windowsExtension.present) executable.windowsExtension = windowsExtension.get()
            executable
        }
    }
}
