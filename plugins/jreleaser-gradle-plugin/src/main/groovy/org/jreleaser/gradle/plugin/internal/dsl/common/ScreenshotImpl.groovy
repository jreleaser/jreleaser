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
import org.jreleaser.gradle.plugin.dsl.common.Screenshot

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.2.0
 */
@CompileStatic
class ScreenshotImpl implements Screenshot {
    String name
    final Property<org.jreleaser.model.Screenshot.Type> screenshotType
    final Property<Boolean> primary
    final Property<String> url
    final Property<String> caption
    final Property<Integer> width
    final Property<Integer> height
    final MapProperty<String, Object> extraProperties

    @Inject
    ScreenshotImpl(ObjectFactory objects) {
        screenshotType = objects.property(org.jreleaser.model.Screenshot.Type)
            .convention(org.jreleaser.model.Screenshot.Type.SOURCE)
        primary = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        url = objects.property(String).convention(Providers.<String> notDefined())
        caption = objects.property(String).convention(Providers.<String> notDefined())
        width = objects.property(Integer).convention(Providers.<Integer> notDefined())
        height = objects.property(Integer).convention(Providers.<Integer> notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
    }

    @Override
    void setScreenshotType(String str) {
        if (isNotBlank(str)) {
            this.screenshotType.set(org.jreleaser.model.Screenshot.Type.of(str.trim()))
        }
    }

    org.jreleaser.model.internal.common.Screenshot toModel() {
        org.jreleaser.model.internal.common.Screenshot screenshot = new org.jreleaser.model.internal.common.Screenshot()
        screenshot.type = screenshotType.get()
        if (primary.present) screenshot.primary = primary.get()
        if (url.present) screenshot.url = url.get()
        if (caption.present) screenshot.caption = caption.get()
        if (width.present) screenshot.width = width.get()
        if (height.present) screenshot.height = height.get()
        if (extraProperties.present) screenshot.extraProperties.putAll(extraProperties.get())
        screenshot
    }
}
