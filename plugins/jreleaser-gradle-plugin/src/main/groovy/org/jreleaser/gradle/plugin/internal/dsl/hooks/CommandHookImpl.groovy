/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.hooks.CommandHook

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.2.0
 */
@CompileStatic
class CommandHookImpl extends AbstractHook implements CommandHook {
    String name
    final Property<String> cmd
    final SetProperty<String> platforms
    final FilterImpl filter

    @Inject
    CommandHookImpl(ObjectFactory objects) {
        super(objects)
        cmd = objects.property(String).convention(Providers.<String> notDefined())
        platforms = objects.setProperty(String).convention(Providers.<List<String>> notDefined())
        filter = objects.newInstance(FilterImpl, objects)
    }

    @Internal
    boolean isSet() {
        super.isSet() ||
            continueOnError.present ||
            cmd.present ||
            platforms.present ||
            filter.isSet()
    }

    @Override
    void platform(String platform) {
        if (isNotBlank(platform)) {
            platforms.add(platform.trim())
        }
    }

    org.jreleaser.model.internal.hooks.CommandHook toModel() {
        org.jreleaser.model.internal.hooks.CommandHook hook = new org.jreleaser.model.internal.hooks.CommandHook()
        fillHookProperties(hook)
        if (cmd.present) hook.cmd = cmd.get()
        hook.platforms = (Set<String>) platforms.getOrElse([] as Set<String>)
        hook.filter = filter.toModel()
        hook
    }
}
