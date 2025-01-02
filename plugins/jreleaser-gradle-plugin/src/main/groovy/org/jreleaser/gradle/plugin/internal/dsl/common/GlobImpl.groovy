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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.common.Glob
import org.jreleaser.model.Active

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class GlobImpl implements Glob {
    String name
    final Property<Active> active
    final Property<String> pattern
    final Property<String> platform
    final DirectoryProperty directory
    final MapProperty<String, Object> extraProperties

    @Inject
    GlobImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        pattern = objects.property(String).convention(Providers.<String> notDefined())
        platform = objects.property(String).convention(Providers.<String> notDefined())
        directory = objects.directoryProperty().convention(Providers.notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void setDirectory(String path) {
        this.directory.set(new File(path))
    }

    org.jreleaser.model.internal.common.Glob toModel() {
        org.jreleaser.model.internal.common.Glob glob = new org.jreleaser.model.internal.common.Glob()
        if (active.present) glob.active = active.get()
        if (pattern.present) glob.pattern = pattern.get()
        if (platform.present) glob.platform = platform.get()
        if (directory.present) {
            glob.directory = directory.asFile.get().absolutePath
        }
        if (extraProperties.present) glob.extraProperties.putAll(extraProperties.get())
        glob
    }
}
