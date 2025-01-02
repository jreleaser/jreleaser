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
package org.jreleaser.gradle.plugin

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
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
import org.jreleaser.gradle.plugin.dsl.signing.Signing
import org.jreleaser.gradle.plugin.dsl.upload.Upload

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface JReleaserExtension {
    RegularFileProperty getConfigFile()

    void setConfigFile(String path)

    Property<Boolean> getEnabled()

    Property<Boolean> getDryrun()

    Property<Boolean> getGitRootSearch()

    Property<Boolean> getStrict()

    Property<Boolean> getDependsOnAssemble()

    Environment getEnvironment()

    Matrix getMatrix()

    Hooks getHooks()

    Project getProject()

    Platform getPlatform()

    Release getRelease()

    Deploy getDeploy()

    Catalog getCatalog()

    Upload getUpload()

    Download getDownload()

    Packagers getPackagers()

    Announce getAnnounce()

    Assemble getAssemble()

    Signing getSigning()

    Checksum getChecksum()

    // NamedDomainObjectContainer<Extension> getExtensions()

    NamedDomainObjectContainer<Distribution> getDistributions()

    void environment(Action<? super Environment> action)

    void matrix(Action<? super Matrix> action)

    void hooks(Action<? super Hooks> action)

    void project(Action<? super Project> action)

    void platform(Action<? super Platform> action)

    void files(Action<? super Files> action)

    void release(Action<? super Release> action)

    void deploy(Action<? super Deploy> action)

    void catalog(Action<? super Catalog> action)

    void upload(Action<? super Upload> action)

    void download(Action<? super Download> action)

    void packagers(Action<? super Packagers> action)

    void announce(Action<? super Announce> action)

    void assemble(Action<? super Assemble> action)

    void signing(Action<? super Signing> action)

    void checksum(Action<? super Checksum> action)

    void distributions(Action<? super NamedDomainObjectContainer<Distribution>> action)

    void extensions(Action<? super NamedDomainObjectContainer<Extension>> action)

    void environment(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Environment) Closure<Void> action)

    void matrix(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Matrix) Closure<Void> action)

    void hooks(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Hooks) Closure<Void> action)

    void project(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Project) Closure<Void> action)

    void platform(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Platform) Closure<Void> action)

    void files(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Files) Closure<Void> action)

    void release(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Release) Closure<Void> action)

    void deploy(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Deploy) Closure<Void> action)

    void catalog(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Catalog) Closure<Void> action)

    void upload(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upload) Closure<Void> action)

    void download(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Download) Closure<Void> action)

    void packagers(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Packagers) Closure<Void> action)

    void announce(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Announce) Closure<Void> action)

    void assemble(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Assemble) Closure<Void> action)

    void signing(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Signing) Closure<Void> action)

    void checksum(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Checksum) Closure<Void> action)

    void distributions(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void extensions(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)
}
