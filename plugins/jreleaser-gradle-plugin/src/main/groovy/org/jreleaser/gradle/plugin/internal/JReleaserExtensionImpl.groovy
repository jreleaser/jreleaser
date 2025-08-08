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
import org.jreleaser.gradle.plugin.dsl.announce.Announce
import org.jreleaser.gradle.plugin.dsl.assemble.Assemble
import org.jreleaser.gradle.plugin.dsl.catalog.Catalog
import org.jreleaser.gradle.plugin.dsl.checksum.Checksum
import org.jreleaser.gradle.plugin.dsl.common.Matrix
import org.jreleaser.gradle.plugin.dsl.deploy.Deploy
import org.jreleaser.gradle.plugin.dsl.distributions.Distribution
import org.jreleaser.gradle.plugin.dsl.download.Download
import org.jreleaser.gradle.plugin.dsl.environment.Environment
import org.jreleaser.gradle.plugin.dsl.extensions.Extension
import org.jreleaser.gradle.plugin.dsl.files.Files
import org.jreleaser.gradle.plugin.dsl.hooks.Hooks
import org.jreleaser.gradle.plugin.dsl.packagers.Packagers
import org.jreleaser.gradle.plugin.dsl.platform.Platform
import org.jreleaser.gradle.plugin.dsl.project.Project
import org.jreleaser.gradle.plugin.dsl.release.Release
import org.jreleaser.gradle.plugin.dsl.servers.Servers
import org.jreleaser.gradle.plugin.dsl.signing.Signing
import org.jreleaser.gradle.plugin.dsl.upload.Upload
import org.jreleaser.gradle.plugin.internal.dsl.announce.AnnounceImpl
import org.jreleaser.gradle.plugin.internal.dsl.assemble.AssembleImpl
import org.jreleaser.gradle.plugin.internal.dsl.catalog.CatalogImpl
import org.jreleaser.gradle.plugin.internal.dsl.checksum.ChecksumImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.MatrixImpl
import org.jreleaser.gradle.plugin.internal.dsl.deploy.DeployImpl
import org.jreleaser.gradle.plugin.internal.dsl.distributions.DistributionImpl
import org.jreleaser.gradle.plugin.internal.dsl.download.DownloadImpl
import org.jreleaser.gradle.plugin.internal.dsl.environment.EnvironmentImpl
import org.jreleaser.gradle.plugin.internal.dsl.extensions.ExtensionImpl
import org.jreleaser.gradle.plugin.internal.dsl.files.FilesImpl
import org.jreleaser.gradle.plugin.internal.dsl.hooks.HooksImpl
import org.jreleaser.gradle.plugin.internal.dsl.packagers.PackagersImpl
import org.jreleaser.gradle.plugin.internal.dsl.platform.PlatformImpl
import org.jreleaser.gradle.plugin.internal.dsl.project.ProjectImpl
import org.jreleaser.gradle.plugin.internal.dsl.release.ReleaseImpl
import org.jreleaser.gradle.plugin.internal.dsl.servers.ServersImpl
import org.jreleaser.gradle.plugin.internal.dsl.signing.SigningImpl
import org.jreleaser.gradle.plugin.internal.dsl.upload.UploadImpl
import org.jreleaser.logging.JReleaserLogger
import org.jreleaser.model.internal.JReleaserModel
import org.jreleaser.util.Env

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
    final RegularFileProperty settingsFile
    final Property<Boolean> enabled
    final Property<Boolean> yolo
    final Property<Boolean> dryrun
    final Property<Boolean> gitRootSearch
    final Property<Boolean> strict
    final Property<Boolean> dependsOnAssemble
    final EnvironmentImpl environment
    final MatrixImpl matrix
    final HooksImpl hooks
    final ProjectImpl project
    final PlatformImpl platform
    final ReleaseImpl release
    final DeployImpl deploy
    final CatalogImpl catalog
    final UploadImpl upload
    final DownloadImpl download
    final PackagersImpl packagers
    final AnnounceImpl announce
    final AssembleImpl assemble
    final ChecksumImpl checksum
    final ServersImpl servers
    final SigningImpl signing
    final FilesImpl files
    final NamedDomainObjectContainer<Distribution> distributions
    private final NamedDomainObjectContainer<Extension> extensions

    private final ProjectLayout layout

    @Inject
    JReleaserExtensionImpl(ObjectFactory objects,
                           ProjectLayout layout,
                           Provider<String> nameProvider,
                           Provider<String> descriptionProvider,
                           Provider<String> versionProvider) {
        this.layout = layout
        configFile = objects.fileProperty()
        settingsFile = objects.fileProperty()
        enabled = objects.property(Boolean).convention(true)
        yolo = objects.property(Boolean).convention(resolveBoolean(org.jreleaser.model.api.JReleaserContext.YOLO))
        dryrun = objects.property(Boolean).convention(resolveBoolean(org.jreleaser.model.api.JReleaserContext.DRY_RUN))
        strict = objects.property(Boolean).convention(resolveBoolean(org.jreleaser.model.api.JReleaserContext.STRICT))
        gitRootSearch = objects.property(Boolean).convention(resolveBoolean(org.jreleaser.model.api.JReleaserContext.GIT_ROOT_SEARCH))
        dependsOnAssemble = objects.property(Boolean).convention(true)
        environment = objects.newInstance(EnvironmentImpl, objects)
        matrix = objects.newInstance(MatrixImpl, objects)
        hooks = objects.newInstance(HooksImpl, objects)
        project = objects.newInstance(ProjectImpl, objects, nameProvider, descriptionProvider, versionProvider)
        platform = objects.newInstance(PlatformImpl, objects)
        release = objects.newInstance(ReleaseImpl, objects)
        deploy = objects.newInstance(DeployImpl, objects)
        catalog = objects.newInstance(CatalogImpl, objects)
        upload = objects.newInstance(UploadImpl, objects)
        download = objects.newInstance(DownloadImpl, objects)
        packagers = objects.newInstance(PackagersImpl, objects)
        announce = objects.newInstance(AnnounceImpl, objects)
        assemble = objects.newInstance(AssembleImpl, objects)
        checksum = objects.newInstance(ChecksumImpl, objects)
        servers = objects.newInstance(ServersImpl, objects)
        signing = objects.newInstance(SigningImpl, objects)
        files = objects.newInstance(FilesImpl, objects)

        distributions = objects.domainObjectContainer(Distribution, new NamedDomainObjectFactory<Distribution>() {
            @Override
            Distribution create(String name) {
                DistributionImpl distribution = objects.newInstance(DistributionImpl, objects)
                distribution.name = name
                distribution
            }
        })

        extensions = objects.domainObjectContainer(Extension, new NamedDomainObjectFactory<Extension>() {
            @Override
            Extension create(String name) {
                ExtensionImpl extension = objects.newInstance(ExtensionImpl, objects)
                extension.name = name
                extension
            }
        })
    }

    void setConfigFile(String path) {
        if (isNotBlank(path)) {
            this.configFile.set(layout.projectDirectory.file(path.trim()))
        }
    }

    void setSettingsFile(String path) {
        if (isNotBlank(path)) {
            this.settingsFile.set(layout.projectDirectory.file(path.trim()))
        }
    }

    @Override
    void environment(Action<? super Environment> action) {
        action.execute(environment)
    }

    @Override
    void matrix(Action<? super Matrix> action) {
        action.execute(matrix)
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
    void deploy(Action<? super Deploy> action) {
        action.execute(deploy)
    }

    @Override
    void catalog(Action<? super Catalog> action) {
        action.execute(catalog)
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
    void servers(Action<? super Servers> action) {
        action.execute(servers)
    }

    @Override
    void distributions(Action<? super NamedDomainObjectContainer<Distribution>> action) {
        action.execute(distributions)
    }

    @Override
    void extensions(Action<? super NamedDomainObjectContainer<Extension>> action) {
        action.execute(extensions)
    }

    @CompileDynamic
    JReleaserModel toModel(org.gradle.api.Project gradleProject, JReleaserLogger logger) {
        if (configFile.present) {
            JReleaserModel jreleaser = ContextCreator.resolveModel(logger, configFile.asFile.get().toPath())
            if (isBlank(jreleaser.project.name)) jreleaser.project.name = gradleProject.name
            if (isBlank(jreleaser.project.version)) jreleaser.project.version = gradleProject.version
            if (isBlank(jreleaser.project.description)) jreleaser.project.description = gradleProject.description
            jreleaser.environment.propertiesSource = new org.jreleaser.model.internal.environment.Environment.MapPropertiesSource(
                filterProperties(gradleProject.properties))
            return jreleaser
        }

        JReleaserModel jreleaser = new JReleaserModel()
        jreleaser.environment = environment.toModel(gradleProject)
        jreleaser.setMatrix(matrix.toModel())
        jreleaser.hooks = hooks.toModel()
        jreleaser.project = project.toModel()
        jreleaser.platform = platform.toModel()
        jreleaser.release = release.toModel()
        jreleaser.deploy = deploy.toModel()
        jreleaser.catalog = catalog.toModel()
        jreleaser.upload = upload.toModel()
        jreleaser.download = download.toModel()
        jreleaser.packagers = packagers.toModel()
        jreleaser.announce = announce.toModel()
        jreleaser.assemble = assemble.toModel()
        jreleaser.signing = signing.toModel()
        jreleaser.checksum = checksum.toModel()
        jreleaser.servers = servers.toModel()
        jreleaser.files = files.toModel()
        distributions.each { jreleaser.addDistribution(((DistributionImpl) it).toModel()) }
        extensions.each { jreleaser.addExtension(((ExtensionImpl) it).toModel()) }
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

    private boolean resolveBoolean(String key) {
        String resolvedValue = Env.resolve(key, '')
        return isNotBlank(resolvedValue) && Boolean.parseBoolean(resolvedValue)
    }
}
