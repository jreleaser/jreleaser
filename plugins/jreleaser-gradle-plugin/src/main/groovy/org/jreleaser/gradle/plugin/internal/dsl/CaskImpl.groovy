/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Cask
import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.5.0
 */
@CompileStatic
class CaskImpl implements Cask {
    final Property<String> name
    final Property<String> displayName
    final Property<String> pkgName
    final Property<String> appName
    final MapProperty<String,List<String>> uninstall
    final MapProperty<String,List<String>> zap

    @Inject
    CaskImpl(ObjectFactory objects) {
        displayName = objects.property(String).convention(Providers.notDefined())
        name = objects.property(String).convention(Providers.notDefined())
        pkgName = objects.property(String).convention(Providers.notDefined())
        appName = objects.property(String).convention(Providers.notDefined())
        uninstall = (objects.mapProperty(String,List).convention(Providers.notDefined()) as MapProperty<String, List<String>>)
        zap = (objects.mapProperty(String,List).convention(Providers.notDefined()) as MapProperty<String, List<String>>)
    }

    @Internal
    boolean isSet() {
        displayName.present ||
            name.present ||
            pkgName.present ||
            appName.present ||
            uninstall.present ||
            zap.present
    }

    org.jreleaser.model.Cask toModel() {
        org.jreleaser.model.Cask cask = new org.jreleaser.model.Cask()
        if (displayName.present) cask.displayName = displayName.get()
        if (name.present) cask.name = name.get()
        if (pkgName.present) cask.pkgName = pkgName.get()
        if (appName.present) cask.appName = appName.get()
        if (uninstall.present) cask.uninstall = uninstall.get()
        if (zap.present) cask.zap = zap.get()
        cask
    }
}
