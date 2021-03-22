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
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.file.Directory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.jreleaser.gradle.plugin.dsl.Artifact
import org.jreleaser.gradle.plugin.dsl.Brew
import org.jreleaser.gradle.plugin.dsl.Chocolatey
import org.jreleaser.gradle.plugin.dsl.Distribution
import org.jreleaser.gradle.plugin.dsl.Jbang
import org.jreleaser.gradle.plugin.dsl.Scoop
import org.jreleaser.gradle.plugin.dsl.Snap
import org.jreleaser.model.Distribution.DistributionType

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
    final Property<String> groupId
    final Property<String> artifactId
    final Property<String> mainClass
    final Property<DistributionType> distributionType
    final ListProperty<String> tags
    final MapProperty<String, Object> extraProperties
    final BrewImpl brew
    final ChocolateyImpl chocolatey
    final JbangImpl jbang
    final ScoopImpl scoop
    final SnapImpl snap

    final NamedDomainObjectContainer<ArtifactImpl> artifacts
    private final Property<String> myName
    private final PackagersImpl packagers

    @Inject
    DistributionImpl(ObjectFactory objects, Provider<Directory> distributionsDirProvider, PackagersImpl packagers) {
        this.packagers = packagers
        executable = objects.property(String).convention(Providers.notDefined())
        groupId = objects.property(String).convention(Providers.notDefined())
        artifactId = objects.property(String).convention(Providers.notDefined())
        mainClass = objects.property(String).convention(Providers.notDefined())
        myName = objects.property(String).convention(Providers.notDefined())
        distributionType = objects.property(DistributionType).convention(DistributionType.BINARY)
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

        brew = objects.newInstance(BrewImpl, objects, distributionsDirProvider)
        brew.distributionName.set(myName)
        chocolatey = objects.newInstance(ChocolateyImpl, objects, distributionsDirProvider)
        chocolatey.distributionName.set(myName)
        jbang = objects.newInstance(JbangImpl, objects, distributionsDirProvider)
        jbang.distributionName.set(myName)
        scoop = objects.newInstance(ScoopImpl, objects, distributionsDirProvider)
        scoop.distributionName.set(myName)
        snap = objects.newInstance(SnapImpl, objects, distributionsDirProvider)
        snap.distributionName.set(myName)
    }

    void setName(String name) {
        this.name = name
        this.myName.set(name)
    }

    @Override
    void setDistributionType(String distributionType) {
        this.distributionType.set(DistributionType.valueOf(distributionType.toUpperCase()))
    }

    @Override
    void addTag(String tag) {
        if (isNotBlank(tag)) {
            tags.add(tag.trim())
        }
    }

    @Override
    void artifact(Action<? super Artifact> action) {
        ArtifactImpl artifact = artifacts.maybeCreate("artifact-${artifacts.size()}".toString())
        action.execute(artifact)
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

    org.jreleaser.model.Distribution toModel() {
        org.jreleaser.model.Distribution distribution = new org.jreleaser.model.Distribution()
        distribution.name = name
        if (executable.present) distribution.executable = executable.get()
        if (groupId.present) distribution.groupId = groupId.get()
        if (artifactId.present) distribution.artifactId = artifactId.get()
        if (mainClass.present) distribution.mainClass = mainClass.get()
        distribution.type = distributionType.get()
        for (ArtifactImpl artifact : artifacts) {
            distribution.artifacts.add(artifact.toModel())
        }
        distribution.tags = (List<String>) tags.getOrElse([])
        if (extraProperties.present) distribution.extraProperties.putAll(extraProperties.get())
        if (brew.isSet()) distribution.brew = brew.toModel()
        if (chocolatey.isSet()) distribution.chocolatey = chocolatey.toModel()
        if (jbang.isSet()) distribution.jbang = jbang.toModel()
        if (scoop.isSet()) distribution.scoop = scoop.toModel()
        if (snap.isSet()) distribution.snap = snap.toModel()
        distribution
    }
}
