/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
import org.jreleaser.gradle.plugin.dsl.Tool
import org.jreleaser.model.Active

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class AbstractTool implements Tool {
    final Property<Active> active
    final Property<Boolean> continueOnError
    final MapProperty<String, Object> extraProperties

    @Inject
    AbstractTool(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.notDefined())
        continueOnError = objects.property(Boolean).convention(Providers.notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
    }

    @Internal
    boolean isSet() {
        active.present ||
            continueOnError.present||
            extraProperties.present
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    protected <T extends org.jreleaser.model.Tool> void fillToolProperties(T tool) {
        if (active.present) tool.active = active.get()
        if (continueOnError.present) tool.continueOnError = continueOnError.get()
        if (extraProperties.present) tool.extraProperties.putAll(extraProperties.get())
    }
}
