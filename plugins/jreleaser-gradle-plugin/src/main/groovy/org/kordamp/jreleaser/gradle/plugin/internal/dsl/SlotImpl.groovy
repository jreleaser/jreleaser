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
package org.kordamp.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.kordamp.jreleaser.gradle.plugin.dsl.Slot

import javax.inject.Inject

import static org.kordamp.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class SlotImpl implements Slot {
    String name
    final MapProperty<String, String> attributes
    final ListProperty<String> reads
    final ListProperty<String> writes

    @Inject
    SlotImpl(ObjectFactory objects) {
        attributes = objects.mapProperty(String, String).convention([:])
        reads = objects.listProperty(String).convention(Providers.notDefined())
        writes = objects.listProperty(String).convention(Providers.notDefined())
    }

    void setName(String name) {
        this.name = name
    }

    @Override
    void addAttribute(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            attributes.put(key.trim(), value.trim())
        }
    }

    @Override
    void addRead(String read) {
        if (isNotBlank(read)) {
            reads.add(read.trim())
        }
    }

    @Override
    void addWrite(String write) {
        if (isNotBlank(write)) {
            writes.add(write.trim())
        }
    }

    org.kordamp.jreleaser.model.Slot toModel() {
        org.kordamp.jreleaser.model.Slot slot = new org.kordamp.jreleaser.model.Slot()
        slot.name = name
        slot.attributes.putAll(attributes.get())
        slot.reads = (List<String>) reads.getOrElse([])
        slot.writes = (List<String>) writes.getOrElse([])
        slot
    }
}
