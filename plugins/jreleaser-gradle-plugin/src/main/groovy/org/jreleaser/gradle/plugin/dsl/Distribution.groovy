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
import org.jreleaser.model.Distribution.DistributionType
import org.jreleaser.model.Stereotype

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface Distribution extends Activatable, ExtraProperties, Packagers {
    Property<DistributionType> getDistributionType()

    Property<Stereotype> getStereotype()

    ListProperty<String> getTags()

    void setDistributionType(String str)

    void tag(String tag)

    Java getJava()

    Platform getPlatform()

    Executable getExecutable()

    void setStereotype(String str)

    void artifact(Action<? super Artifact> action)

    void java(Action<? super Java> action)

    void platform(Action<? super Platform> action)

    void executable(Action<? super Executable> action)

    void artifact(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action)

    void java(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Java) Closure<Void> action)

    void platform(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Platform) Closure<Void> action)

    void executable(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Executable) Closure<Void> action)

    interface Executable {
        Property<String> getName()

        Property<String> getUnixExtension()

        Property<String> getWindowsExtension()
    }
}