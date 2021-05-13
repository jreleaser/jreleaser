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
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.jreleaser.gradle.plugin.JReleaserExtension
import org.jreleaser.gradle.plugin.dsl.Announce
import org.jreleaser.gradle.plugin.dsl.Assemble
import org.jreleaser.gradle.plugin.dsl.Environment
import org.jreleaser.gradle.plugin.dsl.Files
import org.jreleaser.gradle.plugin.dsl.Packagers
import org.jreleaser.gradle.plugin.dsl.Project
import org.jreleaser.gradle.plugin.dsl.Release
import org.jreleaser.gradle.plugin.dsl.Signing
import org.jreleaser.gradle.plugin.dsl.Upload
import org.jreleaser.gradle.plugin.internal.dsl.AnnounceImpl
import org.jreleaser.gradle.plugin.internal.dsl.AssembleImpl
import org.jreleaser.gradle.plugin.internal.dsl.DistributionImpl
import org.jreleaser.gradle.plugin.internal.dsl.EnvironmentImpl
import org.jreleaser.gradle.plugin.internal.dsl.FilesImpl
import org.jreleaser.gradle.plugin.internal.dsl.PackagersImpl
import org.jreleaser.gradle.plugin.internal.dsl.ProjectImpl
import org.jreleaser.gradle.plugin.internal.dsl.ReleaseImpl
import org.jreleaser.gradle.plugin.internal.dsl.SigningImpl
import org.jreleaser.gradle.plugin.internal.dsl.UploadImpl
import org.jreleaser.model.Distribution
import org.jreleaser.model.JReleaserModel
import org.kordamp.gradle.util.ConfigureUtil

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
    final EnvironmentImpl environment
    final ProjectImpl project
    final ReleaseImpl release
    final UploadImpl upload
    final PackagersImpl packagers
    final AnnounceImpl announce
    final AssembleImpl assemble
    final SigningImpl signing
    final FilesImpl files
    final NamedDomainObjectContainer<DistributionImpl> distributions

    @Inject
    JReleaserExtensionImpl(ObjectFactory objects,
                           Provider<String> nameProvider,
                           Provider<String> descriptionProvider,
                           Provider<String> versionProvider) {
        enabled = objects.property(Boolean).convention(true)
        dryrun = objects.property(Boolean).convention(false)
        environment = objects.newInstance(EnvironmentImpl, objects)
        project = objects.newInstance(ProjectImpl, objects, nameProvider, descriptionProvider, versionProvider)
        release = objects.newInstance(ReleaseImpl, objects)
        upload = objects.newInstance(UploadImpl, objects)
        packagers = objects.newInstance(PackagersImpl, objects)
        announce = objects.newInstance(AnnounceImpl, objects)
        assemble = objects.newInstance(AssembleImpl, objects)
        signing = objects.newInstance(SigningImpl, objects)
        files = objects.newInstance(FilesImpl, objects)

        distributions = objects.domainObjectContainer(DistributionImpl, new NamedDomainObjectFactory<DistributionImpl>() {
            @Override
            DistributionImpl create(String name) {
                DistributionImpl distribution = objects.newInstance(DistributionImpl, objects)
                distribution.name = name
                return distribution
            }
        })
    }

    @Override
    void environment(Action<? super Environment> action) {
        action.execute(environment)
    }

    @Override
    void project(Action<? super Project> action) {
        action.execute(project)
    }

    @Override
    void files(Action<? super Files> action) {
        action.execute(files)
    }

    @Override
    void release(Action<? super Release> action) {
        action.execute(release)
    }

    @Override
    void upload(Action<? super Upload> action) {
        action.execute(upload)
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
    void assemble(Action<? super Assemble> action) {
        action.execute(assemble)
    }

    @Override
    void signing(Action<? super Signing> action) {
        action.execute(signing)
    }

    @Override
    void environment(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Environment) Closure<Void> action) {
        ConfigureUtil.configure(action, environment)
    }

    @Override
    void project(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Project) Closure<Void> action) {
        ConfigureUtil.configure(action, project)
    }

    @Override
    void files(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Files) Closure<Void> action) {
        ConfigureUtil.configure(action, files)
    }

    @Override
    void release(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Release) Closure<Void> action) {
        ConfigureUtil.configure(action, release)
    }

    @Override
    void upload(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upload) Closure<Void> action) {
        ConfigureUtil.configure(action, upload)
    }

    @Override
    void packagers(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Packagers) Closure<Void> action) {
        ConfigureUtil.configure(action, packagers)
    }

    @Override
    void announce(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Announce) Closure<Void> action) {
        ConfigureUtil.configure(action, announce)
    }

    @Override
    void assemble(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Assemble) Closure<Void> action) {
        ConfigureUtil.configure(action, assemble)
    }

    @Override
    void signing(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Signing) Closure<Void> action) {
        ConfigureUtil.configure(action, signing)
    }

    @CompileDynamic
    JReleaserModel toModel(org.gradle.api.Project gradleProject) {
        JReleaserModel jreleaser = new JReleaserModel()
        jreleaser.environment = environment.toModel(gradleProject)
        jreleaser.project = project.toModel()
        jreleaser.release = release.toModel()
        jreleaser.upload = upload.toModel()
        jreleaser.packagers = packagers.toModel()
        jreleaser.announce = announce.toModel()
        jreleaser.assemble = assemble.toModel()
        jreleaser.signing = signing.toModel()
        jreleaser.files = files.toModel()
        jreleaser.distributions = (distributions.toList().stream()
            .collect(Collectors.toMap(
                { DistributionImpl d -> d.name },
                { DistributionImpl d -> d.toModel() })) as Map<String, Distribution>)
        jreleaser
    }
}
