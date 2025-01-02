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
package org.jreleaser.gradle.plugin.dsl.hooks

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.common.Activatable
import org.jreleaser.gradle.plugin.dsl.common.Matrix

/**
 *
 * @author Andres Almiray
 * @since 1.6.0
 */
@CompileStatic
interface ScriptHooks extends Activatable {
    Property<String> getCondition()

    MapProperty<String, String> getEnvironment()

    Property<Boolean> getApplyDefaultMatrix()

    void environment(String key, String value)

    void before(Action<? super ScriptHook> action)

    void success(Action<? super ScriptHook> action)

    void failure(Action<? super ScriptHook> action)

    void matrix(Action<? super Matrix> action)

    void before(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ScriptHook) Closure<Void> action)

    void success(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ScriptHook) Closure<Void> action)

    void failure(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ScriptHook) Closure<Void> action)

    void matrix(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Matrix) Closure<Void> action)
}