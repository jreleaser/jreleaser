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
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface Snap extends RepositoryTool {
    Property<String> getPackageName()

    Property<String> getBase()

    Property<String> getGrade()

    Property<String> getConfinement()

    RegularFileProperty getExportedLogin()

    Property<Boolean> getRemoteBuild()

    SetProperty<String> getLocalPlugs()

    SetProperty<String> getLocalSlots()

    NamedDomainObjectContainer<Plug> getPlugs()

    NamedDomainObjectContainer<Slot> getSlots()

    void addLocalPlug(String plug)

    void addLocalSlot(String slot)

    Tap getSnap()

    void snap(Action<? super Tap> action)

    void snap(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Tap) Closure<Void> action)

    @CompileStatic
    interface Slot {
        MapProperty<String, String> getAttributes()

        void addAttribute(String key, String value)

        ListProperty<String> getReads()

        void addRead(String read)

        ListProperty<String> getWrites()

        void addWrite(String write)
    }

    @CompileStatic
    interface Plug {
        MapProperty<String, String> getAttributes()

        void addAttribute(String key, String value)
    }
}