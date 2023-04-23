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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.hooks.Hook
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
abstract class AbstractHook implements Hook {
    final Property<Active> active
    final Property<Boolean> continueOnError
    final Property<Boolean> verbose
    final SetProperty<String> platforms
    final MapProperty<String, String> environment

    @Inject
    AbstractHook(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        continueOnError = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        verbose = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        platforms = objects.setProperty(String).convention(Providers.<List<String>> notDefined())
        environment = objects.mapProperty(String, String).convention(Providers.notDefined())
    }

    @Internal
    boolean isSet() {
        active.present ||
            platforms.present
    }

    @Override
    void environment(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            environment.put(key.trim(), value.trim())
        }
    }

    @Override
    void filter(Action<? super Filter> action) {
        action.execute(filter)
    }

    @Override
    @CompileDynamic
    void filter(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Filter) Closure<Void> action) {
        ConfigureUtil.configure(action, filter)
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void platform(String platform) {
        if (isNotBlank(platform)) {
            platforms.add(platform.trim())
        }
    }

    protected <T extends org.jreleaser.model.internal.hooks.Hook> void fillHookProperties(T hook) {
        if (active.present) hook.active = active.get()
        if (continueOnError.present) hook.continueOnError = continueOnError.get()
        if (verbose.present) hook.verbose = verbose.get()
        if (environment.present) hook.environment.putAll(environment.get())
        hook.platforms = (Set<String>) platforms.getOrElse([] as Set<String>)
    }

    static class FilterImpl implements Filter {
        final SetProperty<String> includes
        final SetProperty<String> excludes

        @Inject
        FilterImpl(ObjectFactory objects) {
            includes = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
            excludes = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        }

        @Internal
        boolean isSet() {
            (includes.present && !includes.get().isEmpty()) ||
                (excludes.present && !excludes.get().isEmpty())
        }

        void include(String str) {
            if (isNotBlank(str)) {
                includes.add(str.trim())
            }
        }

        void exclude(String str) {
            if (isNotBlank(str)) {
                excludes.add(str.trim())
            }
        }

        org.jreleaser.model.internal.hooks.Hook.Filter toModel() {
            org.jreleaser.model.internal.hooks.Hook.Filter filter = new org.jreleaser.model.internal.hooks.Hook.Filter()
            filter.includes = (Set<String>) includes.getOrElse([] as Set<String>)
            filter.includes = (Set<String>) includes.getOrElse([] as Set<String>)
            filter
        }
    }
}
