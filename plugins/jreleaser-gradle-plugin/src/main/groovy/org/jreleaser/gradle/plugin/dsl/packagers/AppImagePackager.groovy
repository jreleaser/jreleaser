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
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.jreleaser.gradle.plugin.dsl.common.Icon
import org.jreleaser.gradle.plugin.dsl.common.Screenshot

/**
 *
 * @author Andres Almiray
 * @since 1.2.0
 */
@CompileStatic
interface AppImagePackager extends RepositoryPackager {
    Property<String> getComponentId()

    ListProperty<String> getCategories()

    Property<String> getDeveloperName()

    Property<Boolean> getRequiresTerminal()

    SetProperty<String> getSkipReleases()

    void category(String category)

    void skipRelease(String skipRelease)

    Tap getRepository()

    void repository(Action<? super Tap> action)

    void repository(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Tap) Closure<Void> action)

    void screenshot(Action<? super Screenshot> action)

    void screenshot(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Screenshot) Closure<Void> action)

    void icon(Action<? super Icon> action)

    void icon(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Icon) Closure<Void> action)
}