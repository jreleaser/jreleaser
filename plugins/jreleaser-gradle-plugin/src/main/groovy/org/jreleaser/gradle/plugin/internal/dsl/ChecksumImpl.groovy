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
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Checksum
import org.jreleaser.model.Active

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.4.0
 */
@CompileStatic
class ChecksumImpl implements Checksum {
    final Property<String> name
    final Property<Boolean> individual

    @Inject
    ChecksumImpl(ObjectFactory objects) {
        name = objects.property(String).convention(Providers.notDefined())
        individual = objects.property(Boolean).convention(Providers.notDefined())
    }

    @Internal
    boolean isSet() {
        return name.present ||
            individual.present
    }

    org.jreleaser.model.Checksum toModel() {
        org.jreleaser.model.Checksum checksum = new org.jreleaser.model.Checksum()
        if (name.present) checksum.name = name.get()
        if (individual.present) checksum.individual = individual.get()
        checksum
    }
}
