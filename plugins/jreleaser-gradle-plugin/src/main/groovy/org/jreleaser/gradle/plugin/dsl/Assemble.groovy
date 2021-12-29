/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.gradle.plugin.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property

/**
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
interface Assemble {
    Property<Boolean> getEnabled()

    NamedDomainObjectContainer<Archive> getArchive()

    NamedDomainObjectContainer<Jlink> getJlink()

    NamedDomainObjectContainer<Jpackage> getJpackage()

    NamedDomainObjectContainer<NativeImage> getNativeImage()

    void archive(Action<? super NamedDomainObjectContainer<Archive>> action)

    void jlink(Action<? super NamedDomainObjectContainer<Jlink>> action)

    void jpackage(Action<? super NamedDomainObjectContainer<Jpackage>> action)

    void nativeImage(Action<? super NamedDomainObjectContainer<NativeImage>> action)

    void archive(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void jlink(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void jpackage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void nativeImage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)
}