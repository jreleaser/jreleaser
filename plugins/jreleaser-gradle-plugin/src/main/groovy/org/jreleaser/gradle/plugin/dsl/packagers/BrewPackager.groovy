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
package org.jreleaser.gradle.plugin.dsl.packagers

import groovy.transform.CompileStatic
import org.gradle.api.Action
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
interface BrewPackager extends RepositoryPackager {
    Property<String> getFormulaName()

    Property<String> getDownloadStrategy()

    SetProperty<String> getRequireRelative()

    Property<Boolean> getMultiPlatform()

    ListProperty<String> getLivecheck()

    MapProperty<String, String> getDependencies()

    void dependency(String key, String value)

    void dependency(String key)

    Tap getRepository()

    @Deprecated
    void repository(Action<? super Tap> action)

    @Deprecated
    void repository(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Tap) Closure<Void> action)

    @Deprecated
    Tap getRepoTap()

    @Deprecated
    void repoTap(Action<? super Tap> action)

    @Deprecated
    void repoTap(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Tap) Closure<Void> action)

    Cask getCask()

    void cask(Action<? super Cask> action)

    void cask(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Cask) Closure<Void> action)

    @CompileStatic
    interface Cask {
        Property<String> getName()

        Property<String> getDisplayName()

        Property<String> getPkgName()

        Property<String> getAppName()

        Property<String> getAppcast()

        Property<Boolean> getEnabled()

        MapProperty<String, List<String>> getUninstall()

        MapProperty<String, List<String>> getZap()
    }
}