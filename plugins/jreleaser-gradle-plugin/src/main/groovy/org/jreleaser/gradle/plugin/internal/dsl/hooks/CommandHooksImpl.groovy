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


import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.common.Matrix
import org.jreleaser.gradle.plugin.dsl.hooks.CommandHook
import org.jreleaser.gradle.plugin.dsl.hooks.CommandHooks
import org.jreleaser.gradle.plugin.dsl.hooks.NamedCommandHooks
import org.jreleaser.gradle.plugin.internal.dsl.common.MatrixImpl
import org.jreleaser.model.Active

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.2.0
 */
@CompileStatic
class CommandHooksImpl implements CommandHooks {
    final Property<Active> active

    final NamedDomainObjectContainer<NamedCommandHooks> groups
    final NamedDomainObjectContainer<CommandHookImpl> before
    final NamedDomainObjectContainer<CommandHookImpl> success
    final NamedDomainObjectContainer<CommandHookImpl> failure
    final Property<String> condition
    final MapProperty<String, String> environment
    final Property<Boolean> applyDefaultMatrix
    final MatrixImpl matrix

    @Inject
    CommandHooksImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        condition = objects.property(String).convention(Providers.<String> notDefined())
        environment = objects.mapProperty(String, String).convention(Providers.notDefined())
        applyDefaultMatrix = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        matrix = objects.newInstance(MatrixImpl, objects)

        groups = objects.domainObjectContainer(NamedCommandHooks, new NamedDomainObjectFactory<NamedCommandHooks>() {
            @Override
            NamedCommandHooksImpl create(String name) {
                NamedCommandHooksImpl hook = objects.newInstance(NamedCommandHooksImpl, objects)
                hook.name = name
                hook
            }
        })

        before = objects.domainObjectContainer(CommandHookImpl, new NamedDomainObjectFactory<CommandHookImpl>() {
            @Override
            CommandHookImpl create(String name) {
                CommandHookImpl hook = objects.newInstance(CommandHookImpl, objects)
                hook.name = name
                hook
            }
        })

        success = objects.domainObjectContainer(CommandHookImpl, new NamedDomainObjectFactory<CommandHookImpl>() {
            @Override
            CommandHookImpl create(String name) {
                CommandHookImpl hook = objects.newInstance(CommandHookImpl, objects)
                hook.name = name
                hook
            }
        })

        failure = objects.domainObjectContainer(CommandHookImpl, new NamedDomainObjectFactory<CommandHookImpl>() {
            @Override
            CommandHookImpl create(String name) {
                CommandHookImpl hook = objects.newInstance(CommandHookImpl, objects)
                hook.name = name
                hook
            }
        })
    }

    boolean isSet() {
        !groups.empty ||
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
    void group(Action<? super NamedDomainObjectContainer<NamedCommandHooks>> action) {
        action.execute(groups)
    }

    @Override
    void before(Action<? super CommandHook> action) {
        action.execute(before.maybeCreate("before-${before.size()}".toString()))
    }

    @Override
    void success(Action<? super CommandHook> action) {
        action.execute(success.maybeCreate("success-${success.size()}".toString()))
    }

    @Override
    void failure(Action<? super CommandHook> action) {
        action.execute(failure.maybeCreate("failure-${failure.size()}".toString()))
    }

    @Override
    void matrix(Action<? super Matrix> action) {
        action.execute(matrix)
    }

    org.jreleaser.model.internal.hooks.CommandHooks toModel() {
        org.jreleaser.model.internal.hooks.CommandHooks commandHooks = new org.jreleaser.model.internal.hooks.CommandHooks()
        if (active.present) commandHooks.active = active.get()

        groups.each { commandHooks.addGroup(((NamedCommandHooksImpl) it).toModel()) }
        before.forEach { CommandHookImpl hook -> commandHooks.addBefore(hook.toModel()) }
        success.forEach { CommandHookImpl hook -> commandHooks.addSuccess(hook.toModel()) }
        failure.forEach { CommandHookImpl hook -> commandHooks.addFailure(hook.toModel()) }
        if (condition.present) commandHooks.condition = condition.get()
        if (environment.present) commandHooks.environment.putAll(environment.get())
        if (applyDefaultMatrix.present) commandHooks.applyDefaultMatrix = applyDefaultMatrix.get()
        if (matrix.isSet()) commandHooks.setMatrix(matrix.toModel())

        commandHooks
    }
}
