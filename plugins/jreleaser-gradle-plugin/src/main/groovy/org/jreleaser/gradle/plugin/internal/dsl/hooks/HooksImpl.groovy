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
package org.jreleaser.gradle.plugin.internal.dsl.hooks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.common.Matrix
import org.jreleaser.gradle.plugin.dsl.hooks.CommandHooks
import org.jreleaser.gradle.plugin.dsl.hooks.Hooks
import org.jreleaser.gradle.plugin.dsl.hooks.ScriptHooks
import org.jreleaser.gradle.plugin.internal.dsl.common.MatrixImpl
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.2.0
 */
@CompileStatic
class HooksImpl implements Hooks {
    final Property<Active> active
    final CommandHooksImpl command
    final ScriptHooksImpl script
    final Property<String> condition
    final MapProperty<String, String> environment
    final Property<Boolean> applyDefaultMatrix
    final MatrixImpl matrix

    @Inject
    HooksImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        command = objects.newInstance(CommandHooksImpl, objects)
        script = objects.newInstance(ScriptHooksImpl, objects)
        condition = objects.property(String).convention(Providers.<String> notDefined())
        environment = objects.mapProperty(String, String).convention(Providers.notDefined())
        applyDefaultMatrix = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        matrix = objects.newInstance(MatrixImpl, objects)
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void environment(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            environment.put(key.trim(), value.trim())
        }
    }

    @Override
    void command(Action<? super CommandHooks> action) {
        action.execute(command)
    }

    @Override
    void script(Action<? super ScriptHooks> action) {
        action.execute(script)
    }

    @Override
    void matrix(Action<? super Matrix> action) {
        action.execute(matrix)
    }

    @Override
    @CompileDynamic
    void command(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommandHooks) Closure<Void> action) {
        ConfigureUtil.configure(action, command)
    }

    @Override
    @CompileDynamic
    void script(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ScriptHooks) Closure<Void> action) {
        ConfigureUtil.configure(action, script)
    }

    @Override
    @CompileDynamic
    void matrix(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Matrix) Closure<Void> action) {
        ConfigureUtil.configure(action, matrix)
    }

    org.jreleaser.model.internal.hooks.Hooks toModel() {
        org.jreleaser.model.internal.hooks.Hooks hooks = new org.jreleaser.model.internal.hooks.Hooks()
        if (active.present) hooks.active = active.get()
        if (condition.present) hooks.condition = condition.get()
        if (environment.present) hooks.environment.putAll(environment.get())
        if (applyDefaultMatrix.present) hooks.applyDefaultMatrix = applyDefaultMatrix.get()
        if (matrix.isSet()) hooks.setMatrix(matrix.toModel())
        hooks.command = command.toModel()
        hooks.script = script.toModel()
        hooks
    }
}
