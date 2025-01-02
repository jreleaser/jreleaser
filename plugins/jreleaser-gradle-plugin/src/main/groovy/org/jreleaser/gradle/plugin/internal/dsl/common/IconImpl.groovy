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
import org.jreleaser.gradle.plugin.dsl.common.Icon

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 1.2.0
 */
@CompileStatic
class IconImpl implements Icon {
    String name
    final Property<String> url
    final Property<Integer> width
    final Property<Integer> height
    final Property<Boolean> primary
    final MapProperty<String, Object> extraProperties

    @Inject
    IconImpl(ObjectFactory objects) {
        url = objects.property(String).convention(Providers.<String> notDefined())
        width = objects.property(Integer).convention(Providers.<Integer> notDefined())
        height = objects.property(Integer).convention(Providers.<Integer> notDefined())
        primary = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
    }

    org.jreleaser.model.internal.common.Icon toModel() {
        org.jreleaser.model.internal.common.Icon icon = new org.jreleaser.model.internal.common.Icon()
        if (url.present) icon.url = url.get()
        if (width.present) icon.width = width.get()
        if (height.present) icon.height = height.get()
        if (primary.present) icon.primary = primary.get()
        if (extraProperties.present) icon.extraProperties.putAll(extraProperties.get())
        icon
    }
}
