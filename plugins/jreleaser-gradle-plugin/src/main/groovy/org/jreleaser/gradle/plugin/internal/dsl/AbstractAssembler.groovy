/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Assembler
import org.jreleaser.gradle.plugin.dsl.Java
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class AbstractAssembler implements Assembler {
    final Property<Boolean> exported
    final Property<Active> active
    final Property<String> executable
    final MapProperty<String, Object> extraProperties
    final DirectoryProperty templateDirectory

    @Inject
    AbstractAssembler(ObjectFactory objects) {
        exported = objects.property(Boolean).convention(Providers.notDefined())
        active = objects.property(Active).convention(Providers.notDefined())
        executable = objects.property(String).convention(Providers.notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
        templateDirectory = objects.directoryProperty().convention(Providers.notDefined())
    }

    @Internal
    boolean isSet() {
        exported.present ||
            active.present ||
            executable.present ||
            extraProperties.present
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void java(Action<? super Java> action) {
        action.execute(java)
    }

    @Override
    void java(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Java) Closure<Void> action) {
        ConfigureUtil.configure(action, java)
    }

    protected <A extends org.jreleaser.model.Assembler> void fillProperties(A assembler) {
        assembler.exported = exported.getOrElse(true)
        if (active.present) assembler.active = active.get()
        if (executable.present) assembler.executable = executable.get()
        if (extraProperties.present) assembler.extraProperties.putAll(extraProperties.get())
        if (templateDirectory.present) {
            assembler.templateDirectory = templateDirectory.get().asFile.toPath().toString()
        }
    }
}
