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
package org.jreleaser.gradle.plugin.internal

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.jreleaser.engine.context.ContextCreator
import org.jreleaser.gradle.plugin.JReleaserExtension
import org.jreleaser.gradle.plugin.dsl.Announce
import org.jreleaser.gradle.plugin.dsl.Assemble
import org.jreleaser.gradle.plugin.dsl.Checksum
import org.jreleaser.gradle.plugin.dsl.Distribution
import org.jreleaser.gradle.plugin.dsl.Download
import org.jreleaser.gradle.plugin.dsl.Environment
import org.jreleaser.gradle.plugin.dsl.Files
import org.jreleaser.gradle.plugin.dsl.Hooks
import org.jreleaser.gradle.plugin.dsl.Packagers
import org.jreleaser.gradle.plugin.dsl.Platform
import org.jreleaser.gradle.plugin.dsl.Project
import org.jreleaser.gradle.plugin.dsl.Release
import org.jreleaser.gradle.plugin.dsl.Signing
import org.jreleaser.gradle.plugin.dsl.Upload
import org.jreleaser.gradle.plugin.internal.dsl.AnnounceImpl
import org.jreleaser.gradle.plugin.internal.dsl.AssembleImpl
import org.jreleaser.gradle.plugin.internal.dsl.ChecksumImpl
import org.jreleaser.gradle.plugin.internal.dsl.DistributionImpl
import org.jreleaser.gradle.plugin.internal.dsl.DownloadImpl
import org.jreleaser.gradle.plugin.internal.dsl.EnvironmentImpl
import org.jreleaser.gradle.plugin.internal.dsl.FilesImpl
import org.jreleaser.gradle.plugin.internal.dsl.HooksImpl
import org.jreleaser.gradle.plugin.internal.dsl.PackagersImpl
import org.jreleaser.gradle.plugin.internal.dsl.PlatformImpl
import org.jreleaser.gradle.plugin.internal.dsl.ProjectImpl
import org.jreleaser.gradle.plugin.internal.dsl.ReleaseImpl
import org.jreleaser.gradle.plugin.internal.dsl.SigningImpl
import org.jreleaser.gradle.plugin.internal.dsl.UploadImpl
import org.jreleaser.model.JReleaserModel
import org.jreleaser.util.JReleaserLogger
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isBlank
import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class JReleaserExtensionImpl implements JReleaserExtension {
    final RegularFileProperty configFile
    final Property<Boolean> enabled
    final Property<Boolean> dryrun
    final Property<Boolean> gitRootSearch
    final EnvironmentImpl environment
    final HooksImpl hooks
    final ProjectImpl project
    final PlatformImpl platform
    final ReleaseImpl release
    final UploadImpl upload
    final DownloadImpl download
    final PackagersImpl packagers
    final AnnounceImpl announce
    final AssembleImpl assemble
    final ChecksumImpl checksum
    final SigningImpl signing
    final FilesImpl files
    final NamedDomainObjectContainer<Distribution> distributions

    private final ProjectLayout layout

    @Inject
    JReleaserExtensionImpl(ObjectFactory objects,
                           ProjectLayout layout,
                           Provider<String> nameProvider,
                           Provider<String> descriptionProvider,
                           Provider<String> versionProvider) {
        this.layout = layout
        configFile = objects.fileProperty()
        enabled = objects.property(Boolean).convention(true)
        dryrun = objects.property(Boolean).convention(false)
        gitRootSearch = objects.property(Boolean).convention(false)
        environment = objects.newInstance(EnvironmentImpl, objects)
        hooks = objects.newInstance(HooksImpl, objects)
        project = objects.newInstance(ProjectImpl, objects, nameProvider, descriptionProvider, versionProvider)
        platform = objects.newInstance(PlatformImpl, objects)
        release = objects.newInstance(ReleaseImpl, objects)
        upload = objects.newInstance(UploadImpl, objects)
        download = objects.newInstance(DownloadImpl, objects)
        packagers = objects.newInstance(PackagersImpl, objects)
        announce = objects.newInstance(AnnounceImpl, objects)
        assemble = objects.newInstance(AssembleImpl, objects)
        checksum = objects.newInstance(ChecksumImpl, objects)
        signing = objects.newInstance(SigningImpl, objects)
        files = objects.newInstance(FilesImpl, objects)

        distributions = objects.domainObjectContainer(Distribution, new NamedDomainObjectFactory<Distribution>() {
            @Override
            Distribution create(String name) {
                DistributionImpl distribution = objects.newInstance(DistributionImpl, objects)
                distribution.name = name
                return distribution
            }
        })
    }

    void setConfigFile(String path) {
        if (isNotBlank(path)) {
            this.configFile.set(layout.projectDirectory.file(path.trim()))
        }
    }

    @Override
    void environment(Action<? super Environment> action) {
        action.execute(environment)
    }

    @Override
    void hooks(Action<? super Hooks> action) {
        action.execute(hooks)
    }

    @Override
    void project(Action<? super Project> action) {
        action.execute(project)
    }

    @Override
    void platform(Action<? super Platform> action) {
        action.execute(platform)
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
    void download(Action<? super Download> action) {
        action.execute(download)
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
    void checksum(Action<? super Checksum> action) {
        action.execute(checksum)
    }

    @Override
    void distributions(Action<? super NamedDomainObjectContainer<Distribution>> action) {
        action.execute(distributions)
    }

    @Override
    void environment(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Environment) Closure<Void> action) {
        ConfigureUtil.configure(action, environment)
    }

    @Override
    void hooks(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Hooks) Closure<Void> action) {
        ConfigureUtil.configure(action, hooks)
    }

    @Override
    void project(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Project) Closure<Void> action) {
        ConfigureUtil.configure(action, project)
    }

    @Override
    void platform(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Platform) Closure<Void> action) {
        ConfigureUtil.configure(action, platform)
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
    void download(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Download) Closure<Void> action) {
        ConfigureUtil.configure(action, download)
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

    @Override
    void checksum(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Checksum) Closure<Void> action) {
        ConfigureUtil.configure(action, checksum)
    }

    @Override
    void distributions(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, distributions)
    }

    @CompileDynamic
    JReleaserModel toModel(org.gradle.api.Project gradleProject, JReleaserLogger logger) {
        if (configFile.present) {
            JReleaserModel jreleaser = ContextCreator.resolveModel(logger, configFile.asFile.get().toPath())
            if (isBlank(jreleaser.project.name)) jreleaser.project.name = project.name.orNull
            if (isBlank(jreleaser.project.version)) jreleaser.project.version = project.version.orNull
            if (isBlank(jreleaser.project.description)) jreleaser.project.description = project.description.orNull
            jreleaser.environment.propertiesSource = new org.jreleaser.model.Environment.MapPropertiesSource(
                filterProperties(project.properties))
            return jreleaser
        }

        JReleaserModel jreleaser = new JReleaserModel()
        jreleaser.environment = environment.toModel(gradleProject)
        jreleaser.hooks = hooks.toModel()
        jreleaser.project = project.toModel()
        jreleaser.platform = platform.toModel()
        jreleaser.release = release.toModel()
        jreleaser.upload = upload.toModel()
        jreleaser.download = download.toModel()
        jreleaser.packagers = packagers.toModel()
        jreleaser.announce = announce.toModel()
        jreleaser.assemble = assemble.toModel()
        jreleaser.signing = signing.toModel()
        jreleaser.checksum = checksum.toModel()
        jreleaser.files = files.toModel()
        distributions.each { jreleaser.addDistribution(((DistributionImpl) it).toModel()) }
        jreleaser
    }

    private Map<String, ?> filterProperties(Map<String, ?> inputs) {
        Map<String, ?> outputs = [:]

        inputs.each { key, value ->
            if (key.startsWith('systemProp') || key.startsWith('VISITED_org_kordamp_gradle')) return

            def val = value
            if (value instanceof Provider) {
                Provider provider = (Provider) value
                val = provider.present ? provider.get() : null
            }

            if (value instanceof CharSequence ||
                value instanceof Number ||
                value instanceof Boolean ||
                value instanceof File) {
                outputs.put(key, val)
            }
        }

        outputs
    }
}
