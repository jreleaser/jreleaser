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
package org.jreleaser.gradle.plugin.internal.dsl.platform

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.jreleaser.gradle.plugin.dsl.platform.Platform

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.10.0
 */
@CompileStatic
class PlatformImpl implements Platform {
    final MapProperty<String, String> replacements

    @Inject
    PlatformImpl(ObjectFactory objects) {
        replacements = objects.mapProperty(String, String).convention(Providers.notDefined())
    }

    boolean isSet() {
        replacements.present
    }

    org.jreleaser.model.internal.platform.Platform toModel() {
        org.jreleaser.model.internal.platform.Platform environment = new org.jreleaser.model.internal.platform.Platform()
        if (replacements.present) environment.properties.putAll(replacements.get())
        environment
    }
}
