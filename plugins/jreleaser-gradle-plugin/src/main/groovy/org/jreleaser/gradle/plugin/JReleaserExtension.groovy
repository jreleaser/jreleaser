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
package org.jreleaser.gradle.plugin

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.Announce
import org.jreleaser.gradle.plugin.dsl.Assemble
import org.jreleaser.gradle.plugin.dsl.Checksum
import org.jreleaser.gradle.plugin.dsl.Distribution
import org.jreleaser.gradle.plugin.dsl.Environment
import org.jreleaser.gradle.plugin.dsl.Files
import org.jreleaser.gradle.plugin.dsl.Packagers
import org.jreleaser.gradle.plugin.dsl.Project
import org.jreleaser.gradle.plugin.dsl.Release
import org.jreleaser.gradle.plugin.dsl.Signing
import org.jreleaser.gradle.plugin.dsl.Upload

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface JReleaserExtension {
    Property<Boolean> getEnabled()

    Property<Boolean> getDryrun()

    Environment getEnvironment()

    Project getProject()

    Release getRelease()

    Upload getUpload()

    Packagers getPackagers()

    Announce getAnnounce()

    Assemble getAssemble()

    Signing getSigning()

    Checksum getChecksum()

    NamedDomainObjectContainer<Distribution> getDistributions()

    void environment(Action<? super Environment> action)

    void project(Action<? super Project> action)

    void files(Action<? super Files> action)

    void release(Action<? super Release> action)

    void upload(Action<? super Upload> action)

    void packagers(Action<? super Packagers> action)

    void announce(Action<? super Announce> action)

    void assemble(Action<? super Assemble> action)

    void signing(Action<? super Signing> action)

    void checksum(Action<? super Checksum> action)

    void environment(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Environment) Closure<Void> action)

    void project(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Project) Closure<Void> action)

    void files(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Files) Closure<Void> action)

    void release(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Release) Closure<Void> action)

    void upload(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upload) Closure<Void> action)

    void packagers(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Packagers) Closure<Void> action)

    void announce(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Announce) Closure<Void> action)

    void assemble(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Assemble) Closure<Void> action)

    void signing(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Signing) Closure<Void> action)

    void checksum(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Checksum) Closure<Void> action)
}
