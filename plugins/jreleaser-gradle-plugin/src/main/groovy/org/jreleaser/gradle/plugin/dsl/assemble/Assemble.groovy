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
package org.jreleaser.gradle.plugin.dsl.assemble

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.jreleaser.gradle.plugin.dsl.common.Activatable

/**
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
interface Assemble extends Activatable {
    NamedDomainObjectContainer<ArchiveAssembler> getArchive()

    NamedDomainObjectContainer<DebAssembler> getDeb()

    NamedDomainObjectContainer<JavaArchiveAssembler> getJavaArchive()

    NamedDomainObjectContainer<JlinkAssembler> getJlink()

    NamedDomainObjectContainer<JpackageAssembler> getJpackage()

    NamedDomainObjectContainer<NativeImageAssembler> getNativeImage()

    void archive(Action<? super NamedDomainObjectContainer<ArchiveAssembler>> action)

    void deb(Action<? super NamedDomainObjectContainer<DebAssembler>> action)

    void javaArchive(Action<? super NamedDomainObjectContainer<JavaArchiveAssembler>> action)

    void jlink(Action<? super NamedDomainObjectContainer<JlinkAssembler>> action)

    void jpackage(Action<? super NamedDomainObjectContainer<JpackageAssembler>> action)

    void nativeImage(Action<? super NamedDomainObjectContainer<NativeImageAssembler>> action)

    void archive(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void deb(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void javaArchive(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void jlink(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void jpackage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void nativeImage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)
}