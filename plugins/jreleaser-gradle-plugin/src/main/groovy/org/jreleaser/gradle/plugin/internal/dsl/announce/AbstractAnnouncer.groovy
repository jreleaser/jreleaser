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
package org.jreleaser.gradle.plugin.internal.dsl.announce

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.announce.Announcer
import org.jreleaser.model.Active

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class AbstractAnnouncer implements Announcer {
    final Property<Active> active
    final Property<Integer> connectTimeout
    final Property<Integer> readTimeout
    final MapProperty<String, Object> extraProperties

    @Inject
    AbstractAnnouncer(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        connectTimeout = objects.property(Integer).convention(Providers.<Integer> notDefined())
        readTimeout = objects.property(Integer).convention(Providers.<Integer> notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
    }

    @Internal
    boolean isSet() {
        active.present ||
            connectTimeout.present ||
            readTimeout.present ||
            extraProperties.present
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    protected <A extends org.jreleaser.model.internal.announce.Announcer> void fillProperties(A announcer) {
        if (active.present) announcer.active = active.get()
        if (connectTimeout.present) announcer.connectTimeout = connectTimeout.get()
        if (readTimeout.present) announcer.readTimeout = readTimeout.get()
        if (extraProperties.present) announcer.extraProperties.putAll(extraProperties.get())
    }
}
