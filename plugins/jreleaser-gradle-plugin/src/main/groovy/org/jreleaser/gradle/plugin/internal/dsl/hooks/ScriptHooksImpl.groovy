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
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.common.Matrix
import org.jreleaser.gradle.plugin.dsl.hooks.ScriptHook
import org.jreleaser.gradle.plugin.dsl.hooks.ScriptHooks
import org.jreleaser.gradle.plugin.internal.dsl.common.MatrixImpl
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.6.0
 */
@CompileStatic
class ScriptHooksImpl implements ScriptHooks {
    final Property<Active> active

    final NamedDomainObjectContainer<ScriptHookImpl> before
    final NamedDomainObjectContainer<ScriptHookImpl> success
    final NamedDomainObjectContainer<ScriptHookImpl> failure
    final Property<String> condition
    final MapProperty<String, String> environment
    final Property<Boolean> applyDefaultMatrix
    final MatrixImpl matrix

    @Inject
    ScriptHooksImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        condition = objects.property(String).convention(Providers.<String> notDefined())
        environment = objects.mapProperty(String, String).convention(Providers.notDefined())
        applyDefaultMatrix = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        matrix = objects.newInstance(MatrixImpl, objects)

        before = objects.domainObjectContainer(ScriptHookImpl, new NamedDomainObjectFactory<ScriptHookImpl>() {
            @Override
            ScriptHookImpl create(String name) {
                ScriptHookImpl hook = objects.newInstance(ScriptHookImpl, objects)
                hook.name = name
                hook
            }
        })

        success = objects.domainObjectContainer(ScriptHookImpl, new NamedDomainObjectFactory<ScriptHookImpl>() {
            @Override
            ScriptHookImpl create(String name) {
                ScriptHookImpl hook = objects.newInstance(ScriptHookImpl, objects)
                hook.name = name
                hook
            }
        })

        failure = objects.domainObjectContainer(ScriptHookImpl, new NamedDomainObjectFactory<ScriptHookImpl>() {
            @Override
            ScriptHookImpl create(String name) {
                ScriptHookImpl hook = objects.newInstance(ScriptHookImpl, objects)
                hook.name = name
                hook
            }
        })
    }

    boolean isSet() {
        !before.empty ||
            !success.empty ||
            !failure.empty
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
    void before(Action<? super ScriptHook> action) {
        action.execute(before.maybeCreate("before-${before.size()}".toString()))
    }

    @Override
    void success(Action<? super ScriptHook> action) {
        action.execute(success.maybeCreate("success-${success.size()}".toString()))
    }

    @Override
    void failure(Action<? super ScriptHook> action) {
        action.execute(failure.maybeCreate("failure-${failure.size()}".toString()))
    }

    @Override
    void matrix(Action<? super Matrix> action) {
        action.execute(matrix)
    }

    @Override
    @CompileDynamic
    void before(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ScriptHook) Closure<Void> action) {
        ConfigureUtil.configure(action, before.maybeCreate("before-${before.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void success(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ScriptHook) Closure<Void> action) {
        ConfigureUtil.configure(action, success.maybeCreate("success-${success.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void failure(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ScriptHook) Closure<Void> action) {
        ConfigureUtil.configure(action, failure.maybeCreate("failure-${failure.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void matrix(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Matrix) Closure<Void> action) {
        ConfigureUtil.configure(action, matrix)
    }

    org.jreleaser.model.internal.hooks.ScriptHooks toModel() {
        org.jreleaser.model.internal.hooks.ScriptHooks scriptHooks = new org.jreleaser.model.internal.hooks.ScriptHooks()
        if (active.present) scriptHooks.active = active.get()

        before.forEach { ScriptHookImpl hook -> scriptHooks.addBefore(hook.toModel()) }
        success.forEach { ScriptHookImpl hook -> scriptHooks.addSuccess(hook.toModel()) }
        failure.forEach { ScriptHookImpl hook -> scriptHooks.addFailure(hook.toModel()) }
        if (condition.present) scriptHooks.condition = condition.get()
        if (environment.present) scriptHooks.environment.putAll(environment.get())
        if (applyDefaultMatrix.present) scriptHooks.applyDefaultMatrix = applyDefaultMatrix.get()
        if (matrix.isSet()) scriptHooks.setMatrix(matrix.toModel())

        scriptHooks
    }
}
