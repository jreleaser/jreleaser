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
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.common.JvmOptions

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.13.0
 */
@CompileStatic
class JvmOptionsImpl implements JvmOptions {
    final ListProperty<String> universal
    final ListProperty<String> unix
    final ListProperty<String> linux
    final ListProperty<String> osx
    final ListProperty<String> windows

    @Inject
    JvmOptionsImpl(ObjectFactory objects) {
        universal = objects.listProperty(String).convention([])
        unix = objects.listProperty(String).convention([])
        linux = objects.listProperty(String).convention([])
        osx = objects.listProperty(String).convention([])
        windows = objects.listProperty(String).convention([])
    }

    @Internal
    boolean isSet() {
        universal.present ||
            unix.present ||
            linux.present ||
            osx.present ||
            windows.present
    }

    void universal(String option) {
        if (isNotBlank(option)) {
            universal.add(option.trim())
        }
    }

    void unix(String option) {
        if (isNotBlank(option)) {
            unix.add(option.trim())
        }
    }

    void linux(String option) {
        if (isNotBlank(option)) {
            linux.add(option.trim())
        }
    }

    void osx(String option) {
        if (isNotBlank(option)) {
            osx.add(option.trim())
        }
    }

    void windows(String option) {
        if (isNotBlank(option)) {
            windows.add(option.trim())
        }
    }

    org.jreleaser.model.internal.common.JvmOptions toModel() {
        org.jreleaser.model.internal.common.JvmOptions jvmOptions = new org.jreleaser.model.internal.common.JvmOptions()
        jvmOptions.universal.addAll(universal.get())
        jvmOptions.unix.addAll(unix.get())
        jvmOptions.linux.addAll(linux.get())
        jvmOptions.osx.addAll(osx.get())
        jvmOptions.windows.addAll(windows.get())
        jvmOptions
    }
}
