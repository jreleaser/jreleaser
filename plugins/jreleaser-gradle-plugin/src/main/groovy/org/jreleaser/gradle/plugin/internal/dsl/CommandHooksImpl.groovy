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
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.CommandHook
import org.jreleaser.gradle.plugin.dsl.CommandHooks
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
class CommandHooksImpl implements CommandHooks {
    final Property<Active> active

    final NamedDomainObjectContainer<CommandHookImpl> before
    final NamedDomainObjectContainer<CommandHookImpl> success
    final NamedDomainObjectContainer<CommandHookImpl> failure

    @Inject
    CommandHooksImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.notDefined())

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
    void before(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommandHook) Closure<Void> action) {
        ConfigureUtil.configure(action, before.maybeCreate("before-${before.size()}".toString()))
    }

    @Override
    void success(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommandHook) Closure<Void> action) {
        ConfigureUtil.configure(action, success.maybeCreate("success-${success.size()}".toString()))
    }

    @Override
    void failure(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommandHook) Closure<Void> action) {
        ConfigureUtil.configure(action, failure.maybeCreate("failure-${failure.size()}".toString()))
    }

    org.jreleaser.model.CommandHooks toModel() {
        org.jreleaser.model.CommandHooks commandHooks = new org.jreleaser.model.CommandHooks()
        if (active.present) commandHooks.active = active.get()

        before.forEach { hook -> commandHooks.addBefore(hook.toModel()) }
        success.forEach { hook -> commandHooks.addSuccess(hook.toModel()) }
        failure.forEach { hook -> commandHooks.addFailure(hook.toModel()) }

        commandHooks
    }
}
