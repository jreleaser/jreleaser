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
package org.jreleaser.gradle.plugin.internal.dsl.common

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.jreleaser.gradle.plugin.dsl.common.FileSet
import org.jreleaser.model.Active

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
class FileSetImpl implements FileSet {
    String name
    final Property<Active> active
    final Property<String> input
    final Property<String> output
    final Property<String> platform
    final Property<Boolean> failOnMissingInput
    final SetProperty<String> includes
    final SetProperty<String> excludes
    final MapProperty<String, Object> extraProperties

    @Inject
    FileSetImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        input = objects.property(String).convention(Providers.<String> notDefined())
        output = objects.property(String).convention(Providers.<String> notDefined())
        platform = objects.property(String).convention(Providers.<String> notDefined())
        failOnMissingInput = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        includes = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        excludes = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
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

    org.jreleaser.model.internal.common.FileSet toModel() {
        org.jreleaser.model.internal.common.FileSet fileSet = new org.jreleaser.model.internal.common.FileSet()
        if (active.present) fileSet.active = active.get()
        if (input.present) fileSet.input = input.get()
        if (output.present) fileSet.output = output.get()
        if (platform.present) fileSet.platform = platform.get()
        if (failOnMissingInput.present) fileSet.failOnMissingInput = failOnMissingInput.get()
        fileSet.includes = (Set<String>) includes.getOrElse([] as Set<String>)
        fileSet.includes = (Set<String>) includes.getOrElse([] as Set<String>)
        if (extraProperties.present) fileSet.extraProperties.putAll(extraProperties.get())
        fileSet
    }
}
