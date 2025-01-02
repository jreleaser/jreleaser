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
package org.jreleaser.gradle.plugin.internal.dsl.distributions

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.common.Artifact
import org.jreleaser.gradle.plugin.dsl.common.Executable
import org.jreleaser.gradle.plugin.dsl.common.Java
import org.jreleaser.gradle.plugin.dsl.distributions.Distribution
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
import org.jreleaser.gradle.plugin.dsl.packagers.ScoopPackager
import org.jreleaser.gradle.plugin.dsl.packagers.SdkmanPackager
import org.jreleaser.gradle.plugin.dsl.packagers.SnapPackager
import org.jreleaser.gradle.plugin.dsl.packagers.SpecPackager
import org.jreleaser.gradle.plugin.dsl.packagers.WingetPackager
import org.jreleaser.gradle.plugin.dsl.platform.Platform
import org.jreleaser.gradle.plugin.internal.dsl.common.ArtifactImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.ExecutableImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.JavaImpl
import org.jreleaser.gradle.plugin.internal.dsl.packagers.AppImagePackagerImpl
import org.jreleaser.gradle.plugin.internal.dsl.packagers.AsdfPackagerImpl
import org.jreleaser.gradle.plugin.internal.dsl.packagers.BrewPackagerImpl
import org.jreleaser.gradle.plugin.internal.dsl.packagers.ChocolateyPackagerImpl
import org.jreleaser.gradle.plugin.internal.dsl.packagers.DockerPackagerImpl
import org.jreleaser.gradle.plugin.internal.dsl.packagers.FlatpakPackagerImpl
import org.jreleaser.gradle.plugin.internal.dsl.packagers.GofishPackagerImpl
import org.jreleaser.gradle.plugin.internal.dsl.packagers.JbangPackagerImpl
import org.jreleaser.gradle.plugin.internal.dsl.packagers.JibPackagerImpl
import org.jreleaser.gradle.plugin.internal.dsl.packagers.MacportsPackagerImpl
import org.jreleaser.gradle.plugin.internal.dsl.packagers.ScoopPackagerImpl
import org.jreleaser.gradle.plugin.internal.dsl.packagers.SdkmanPackagerImpl
import org.jreleaser.gradle.plugin.internal.dsl.packagers.SnapPackagerImpl
import org.jreleaser.gradle.plugin.internal.dsl.packagers.SpecPackagerImpl
import org.jreleaser.gradle.plugin.internal.dsl.packagers.WingetPackagerImpl
import org.jreleaser.gradle.plugin.internal.dsl.platform.PlatformImpl
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

    private final NamedDomainObjectContainer<ArtifactImpl> artifacts

    @Inject
    DistributionImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        stereotype = objects.property(Stereotype).convention(Providers.<Stereotype> notDefined())
        groupId = objects.property(String).convention(Providers.<String> notDefined())
        artifactId = objects.property(String).convention(Providers.<String> notDefined())
        distributionType = objects.property(DistributionType).convention(DistributionType.JAVA_BINARY)
        tags = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
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
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    @CompileDynamic
    void artifact(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, artifacts.maybeCreate("artifact-${artifacts.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void java(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Java) Closure<Void> action) {
        ConfigureUtil.configure(action, java)
    }

    @Override
    @CompileDynamic
    void platform(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Platform) Closure<Void> action) {
        ConfigureUtil.configure(action, platform)
    }

    @Override
    @CompileDynamic
    void executable(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Executable) Closure<Void> action) {
        ConfigureUtil.configure(action, executable)
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

    org.jreleaser.model.internal.distributions.Distribution toModel() {
        org.jreleaser.model.internal.distributions.Distribution distribution = new org.jreleaser.model.internal.distributions.Distribution()
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
        if (flatpak.isSet()) distribution.flatpak = flatpak.toModel()
        if (gofish.isSet()) distribution.gofish = gofish.toModel()
        if (jbang.isSet()) distribution.jbang = jbang.toModel()
        if (jib.isSet()) distribution.jib = jib.toModel()
        if (macports.isSet()) distribution.macports = macports.toModel()
        if (scoop.isSet()) distribution.scoop = scoop.toModel()
        if (sdkman.isSet()) distribution.sdkman = sdkman.toModel()
        if (snap.isSet()) distribution.snap = snap.toModel()
        if (spec.isSet()) distribution.spec = spec.toModel()
        if (winget.isSet()) distribution.winget = winget.toModel()
        distribution
    }
}
