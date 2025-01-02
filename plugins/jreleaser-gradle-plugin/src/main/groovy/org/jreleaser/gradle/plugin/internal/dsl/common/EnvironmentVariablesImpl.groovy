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
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.common.EnvironmentVariables

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.13.0
 */
@CompileStatic
class EnvironmentVariablesImpl implements EnvironmentVariables {
    final MapProperty<String, String> universal
    final MapProperty<String, String> unix
    final MapProperty<String, String> linux
    final MapProperty<String, String> osx
    final MapProperty<String, String> windows

    @Inject
    EnvironmentVariablesImpl(ObjectFactory objects) {
        universal = objects.mapProperty(String, String).convention([:])
        unix = objects.mapProperty(String, String).convention([:])
        linux = objects.mapProperty(String, String).convention([:])
        osx = objects.mapProperty(String, String).convention([:])
        windows = objects.mapProperty(String, String).convention([:])
    }

    @Internal
    boolean isSet() {
        universal.present ||
            unix.present ||
            linux.present ||
            osx.present ||
            windows.present
    }

    void universal(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            universal.put(key.trim(), value.trim())
        }
    }

    void unix(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            unix.put(key.trim(), value.trim())
        }
    }

    void linux(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            linux.put(key.trim(), value.trim())
        }
    }

    void osx(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            osx.put(key.trim(), value.trim())
        }
    }

    void windows(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            windows.put(key.trim(), value.trim())
        }
    }

    org.jreleaser.model.internal.common.EnvironmentVariables toModel() {
        org.jreleaser.model.internal.common.EnvironmentVariables environmentVariables = new org.jreleaser.model.internal.common.EnvironmentVariables()
        environmentVariables.universal.putAll(universal.get())
        environmentVariables.unix.putAll(unix.get())
        environmentVariables.linux.putAll(linux.get())
        environmentVariables.osx.putAll(osx.get())
        environmentVariables.windows.putAll(windows.get())
        environmentVariables
    }
}
