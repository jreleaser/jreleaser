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
package org.jreleaser.gradle.plugin.internal

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.file.Directory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.jreleaser.gradle.plugin.JReleaserExtension
import org.jreleaser.gradle.plugin.dsl.Announce
import org.jreleaser.gradle.plugin.dsl.Artifact
import org.jreleaser.gradle.plugin.dsl.Packagers
import org.jreleaser.gradle.plugin.dsl.Project
import org.jreleaser.gradle.plugin.dsl.Release
import org.jreleaser.gradle.plugin.dsl.Sign
import org.jreleaser.gradle.plugin.internal.dsl.AnnounceImpl
import org.jreleaser.gradle.plugin.internal.dsl.ArtifactImpl
import org.jreleaser.gradle.plugin.internal.dsl.DistributionImpl
import org.jreleaser.gradle.plugin.internal.dsl.PackagersImpl
import org.jreleaser.gradle.plugin.internal.dsl.ProjectImpl
import org.jreleaser.gradle.plugin.internal.dsl.ReleaseImpl
import org.jreleaser.gradle.plugin.internal.dsl.SignImpl
import org.jreleaser.model.Distribution
import org.jreleaser.model.JReleaserModel

import javax.inject.Inject
import java.util.stream.Collectors

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class JReleaserExtensionImpl implements JReleaserExtension {
    final Property<Boolean> enabled
    final Property<Boolean> dryrun
    final ProjectImpl project
    final ReleaseImpl release
    final PackagersImpl packagers
    final AnnounceImpl announce
    final SignImpl sign
    final NamedDomainObjectContainer<ArtifactImpl> artifacts
    final NamedDomainObjectContainer<DistributionImpl> distributions

    @Inject
    JReleaserExtensionImpl(ObjectFactory objects,
                           Provider<String> nameProvider,
                           Provider<String> descriptionProvider,
                           Provider<String> versionProvider,
                           Provider<Directory> distributionsDirProvider) {
        enabled = objects.property(Boolean).convention(true)
        dryrun = objects.property(Boolean).convention(false)
        project = objects.newInstance(ProjectImpl, objects, nameProvider, descriptionProvider, versionProvider)
        release = objects.newInstance(ReleaseImpl, objects)
        packagers = objects.newInstance(PackagersImpl, objects)
        announce = objects.newInstance(AnnounceImpl, objects)
        sign = objects.newInstance(SignImpl, objects)
        artifacts = objects.domainObjectContainer(ArtifactImpl, new NamedDomainObjectFactory<ArtifactImpl>() {
            @Override
            ArtifactImpl create(String name) {
                ArtifactImpl artifact = objects.newInstance(ArtifactImpl, objects)
                artifact.name = name
                artifact
            }
        })
        distributions = objects.domainObjectContainer(DistributionImpl, new NamedDomainObjectFactory<DistributionImpl>() {
            @Override
            DistributionImpl create(String name) {
                DistributionImpl distribution = objects.newInstance(DistributionImpl, objects, distributionsDirProvider, packagers)
                distribution.name = name
                return distribution
            }
        })
    }

    @Override
    void project(Action<? super Project> action) {
        action.execute(project)
    }

    @Override
    void artifact(Action<? super Artifact> action) {
        ArtifactImpl artifact = artifacts.maybeCreate("artifact-${artifacts.size()}".toString())
        action.execute(artifact)
    }

    @Override
    void release(Action<? super Release> action) {
        action.execute(release)
    }

    @Override
    void packagers(Action<? super Packagers> action) {
        action.execute(packagers)
    }

    @Override
    void announce(Action<? super Announce> action) {
        action.execute(announce)
    }

    @Override
    void sign(Action<? super Sign> action) {
        action.execute(sign)
    }

    @CompileDynamic
    JReleaserModel toModel() {
        JReleaserModel jreleaser = new JReleaserModel()
        jreleaser.project = project.toModel()
        jreleaser.release = release.toModel()
        jreleaser.packagers = packagers.toModel()
        jreleaser.announce = announce.toModel()
        jreleaser.sign = sign.toModel()
        for (ArtifactImpl artifact : artifacts) {
            jreleaser.artifacts.add(artifact.toModel())
        }
        jreleaser.distributions = (distributions.toList().stream()
            .collect(Collectors.toMap(
                { DistributionImpl d -> d.name },
                { DistributionImpl d -> d.toModel() })) as Map<String, Distribution>)
        jreleaser
    }
}
