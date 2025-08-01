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
import org.jreleaser.gradle.plugin.dsl.hooks.JbangHook
import org.jreleaser.gradle.plugin.dsl.hooks.NamedJbangHooks
import org.jreleaser.gradle.plugin.internal.dsl.common.MatrixImpl
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.20.0
 */
@CompileStatic
class NamedJbangHooksImpl implements NamedJbangHooks {
    String name
    final Property<Active> active

    final NamedDomainObjectContainer<JbangHookImpl> before
    final NamedDomainObjectContainer<JbangHookImpl> success
    final NamedDomainObjectContainer<JbangHookImpl> failure
    final Property<String> version
    final Property<String> script
    final Property<String> condition
    final MapProperty<String, String> environment
    final Property<Boolean> applyDefaultMatrix
    final MatrixImpl matrix

    @Inject
    NamedJbangHooksImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        version = objects.property(String).convention(Providers.<String> notDefined())
        script = objects.property(String).convention(Providers.<String> notDefined())
        condition = objects.property(String).convention(Providers.<String> notDefined())
        environment = objects.mapProperty(String, String).convention(Providers.notDefined())
        applyDefaultMatrix = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        matrix = objects.newInstance(MatrixImpl, objects)

        before = objects.domainObjectContainer(JbangHookImpl, new NamedDomainObjectFactory<JbangHookImpl>() {
            @Override
            JbangHookImpl create(String name) {
                JbangHookImpl hook = objects.newInstance(JbangHookImpl, objects)
                hook.name = name
                hook
            }
        })

        success = objects.domainObjectContainer(JbangHookImpl, new NamedDomainObjectFactory<JbangHookImpl>() {
            @Override
            JbangHookImpl create(String name) {
                JbangHookImpl hook = objects.newInstance(JbangHookImpl, objects)
                hook.name = name
                hook
            }
        })

        failure = objects.domainObjectContainer(JbangHookImpl, new NamedDomainObjectFactory<JbangHookImpl>() {
            @Override
            JbangHookImpl create(String name) {
                JbangHookImpl hook = objects.newInstance(JbangHookImpl, objects)
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
    void before(Action<? super JbangHook> action) {
        action.execute(before.maybeCreate("before-${before.size()}".toString()))
    }

    @Override
    void success(Action<? super JbangHook> action) {
        action.execute(success.maybeCreate("success-${success.size()}".toString()))
    }

    @Override
    void failure(Action<? super JbangHook> action) {
        action.execute(failure.maybeCreate("failure-${failure.size()}".toString()))
    }

    @Override
    void matrix(Action<? super Matrix> action) {
        action.execute(matrix)
    }

    @Override
    @CompileDynamic
    void before(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = JbangHook) Closure<Void> action) {
        ConfigureUtil.configure(action, before.maybeCreate("before-${before.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void success(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = JbangHook) Closure<Void> action) {
        ConfigureUtil.configure(action, success.maybeCreate("success-${success.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void failure(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = JbangHook) Closure<Void> action) {
        ConfigureUtil.configure(action, failure.maybeCreate("failure-${failure.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void matrix(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Matrix) Closure<Void> action) {
        ConfigureUtil.configure(action, matrix)
    }

    org.jreleaser.model.internal.hooks.NamedJbangHooks toModel() {
        org.jreleaser.model.internal.hooks.NamedJbangHooks jbangHooks = new org.jreleaser.model.internal.hooks.NamedJbangHooks()
        jbangHooks.name = name
        if (active.present) jbangHooks.active = active.get()

        before.forEach { JbangHookImpl hook -> jbangHooks.addBefore(hook.toModel()) }
        success.forEach { JbangHookImpl hook -> jbangHooks.addSuccess(hook.toModel()) }
        failure.forEach { JbangHookImpl hook -> jbangHooks.addFailure(hook.toModel()) }
        if (version.present) jbangHooks.version = version.get()
        if (script.present) jbangHooks.script = script.get()
        if (condition.present) jbangHooks.condition = condition.get()
        if (environment.present) jbangHooks.environment.putAll(environment.get())
        if (applyDefaultMatrix.present) jbangHooks.applyDefaultMatrix = applyDefaultMatrix.get()
        if (matrix.isSet()) jbangHooks.setMatrix(matrix.toModel())

        jbangHooks
    }
}
