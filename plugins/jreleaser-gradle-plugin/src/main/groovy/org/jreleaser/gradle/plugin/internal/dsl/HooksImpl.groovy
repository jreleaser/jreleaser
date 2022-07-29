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
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.CommandHooks
import org.jreleaser.gradle.plugin.dsl.Hooks
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

    @Inject
    HooksImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.notDefined())
        command = objects.newInstance(CommandHooksImpl, objects)
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void command(Action<? super CommandHooks> action) {
        action.execute(command)
    }

    @Override
    void command(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommandHooks) Closure<Void> action) {
        ConfigureUtil.configure(action, command)
    }

    org.jreleaser.model.Hooks toModel() {
        org.jreleaser.model.Hooks hooks = new org.jreleaser.model.Hooks()
        if (active.present) hooks.active = active.get()
        hooks.command = command.toModel()
        hooks
    }
}
