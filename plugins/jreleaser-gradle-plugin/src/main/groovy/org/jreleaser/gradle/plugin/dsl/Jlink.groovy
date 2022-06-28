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
package org.jreleaser.gradle.plugin.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

/**
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
interface Jlink extends JavaAssembler {
    Property<String> getImageName()

    Property<String> getImageNameTransform()

    Property<Boolean> getCopyJars()

    SetProperty<String> getModuleNames()

    SetProperty<String> getAdditionalModuleNames()

    ListProperty<String> getArgs()

    void arg(String arg)

    void jdeps(Action<? super Jdeps> action)

    void jdk(Action<? super Artifact> action)

    void targetJdk(Action<? super Artifact> action)

    void jdeps(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Jdeps) Closure<Void> action)

    void jdk(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action)

    void targetJdk(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action)

    @CompileStatic
    interface Jdeps {
        Property<Boolean> getEnabled()

        Property<String> getMultiRelease()

        Property<Boolean> getIgnoreMissingDeps()

        Property<Boolean> getUseWildcardInPath()

        SetProperty<String> getTargets()
    }
}