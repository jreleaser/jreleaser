/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.hooks.JbangHook

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.20.0
 */
@CompileStatic
class JbangHookImpl extends AbstractHook implements JbangHook {
    String name
    final Property<String> version
    final Property<String> script
    final ListProperty<String> args
    final ListProperty<String> jbangArgs
    final ListProperty<String> trusts
    final FilterImpl filter

    @Inject
    JbangHookImpl(ObjectFactory objects) {
        super(objects)
        version = objects.property(String).convention(Providers.<String> notDefined())
        script = objects.property(String).convention(Providers.<String> notDefined())
        args = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        jbangArgs = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        trusts = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        filter = objects.newInstance(FilterImpl, objects)
    }

    @Internal
    boolean isSet() {
        super.isSet() ||
            version.present ||
            script.present ||
            args.present ||
            jbangArgs.present ||
            trusts.present ||
            filter.isSet()
    }

    @Override
    void arg(String arg) {
        if (isNotBlank(arg)) {
            args.add(arg.trim())
        }
    }

    @Override
    void jbangArg(String jbangArg) {
        if (isNotBlank(jbangArg)) {
            jbangArgs.add(jbangArg.trim())
        }
    }

    @Override
    void trust(String trust) {
        if (isNotBlank(trust)) {
            trusts.add(trust.trim())
        }
    }

    org.jreleaser.model.internal.hooks.JbangHook toModel() {
        org.jreleaser.model.internal.hooks.JbangHook hook = new org.jreleaser.model.internal.hooks.JbangHook()
        fillHookProperties(hook)
        if (version.present) hook.version = version.get()
        if (script.present) hook.script = script.get()
        hook.args = (List<String>) args.getOrElse([])
        hook.jbangArgs = (List<String>) jbangArgs.getOrElse([])
        hook.trusts = (List<String>) trusts.getOrElse([])
        hook.filter = filter.toModel()
        hook
    }
}
