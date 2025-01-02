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
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.hooks.ScriptHook

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.6.0
 */
@CompileStatic
class ScriptHookImpl extends AbstractHook implements ScriptHook {
    String name
    final Property<String> run
    final Property<org.jreleaser.model.api.hooks.ScriptHook.Shell> shell
    final FilterImpl filter

    @Inject
    ScriptHookImpl(ObjectFactory objects) {
        super(objects)
        run = objects.property(String).convention(Providers.<String> notDefined())
        shell = objects.property(org.jreleaser.model.api.hooks.ScriptHook.Shell).convention(Providers.<org.jreleaser.model.api.hooks.ScriptHook.Shell> notDefined())
        filter = objects.newInstance(FilterImpl, objects)
    }

    @Internal
    boolean isSet() {
        super.isSet() ||
            run.present ||
            shell.present ||
            filter.isSet()
    }

    @Override
    void setShell(String str) {
        if (isNotBlank(str)) {
            shell.set(org.jreleaser.model.api.hooks.ScriptHook.Shell.of(str.trim()))
        }
    }

    org.jreleaser.model.internal.hooks.ScriptHook toModel() {
        org.jreleaser.model.internal.hooks.ScriptHook hook = new org.jreleaser.model.internal.hooks.ScriptHook()
        fillHookProperties(hook)
        if (run.present) hook.run = run.get()
        if (shell.present) hook.shell = shell.get()
        hook.filter = filter.toModel()
        hook
    }
}
