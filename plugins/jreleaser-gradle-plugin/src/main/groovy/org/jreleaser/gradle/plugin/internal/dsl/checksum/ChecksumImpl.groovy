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
package org.jreleaser.gradle.plugin.internal.dsl.checksum

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.checksum.Checksum
import org.jreleaser.util.Algorithm

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
    final Property<Boolean> artifacts
    final Property<Boolean> files
    final ListProperty<Algorithm> algorithms

    @Inject
    ChecksumImpl(ObjectFactory objects) {
        name = objects.property(String).convention(Providers.<String> notDefined())
        individual = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        artifacts = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        files = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        algorithms = objects.listProperty(Algorithm).convention(Providers.<List<Algorithm>> notDefined())
    }

    @Internal
    boolean isSet() {
        return name.present ||
            individual.present ||
            artifacts.present ||
            files.present
    }

    @Override
    void algorithm(String algorithm) {
        if (isNotBlank(algorithm)) {
            algorithms.add(Algorithm.of(algorithm.trim()))
        }
    }

    org.jreleaser.model.internal.checksum.Checksum toModel() {
        org.jreleaser.model.internal.checksum.Checksum checksum = new org.jreleaser.model.internal.checksum.Checksum()
        if (name.present) checksum.name = name.get()
        if (individual.present) checksum.individual = individual.get()
        if (artifacts.present) checksum.artifacts = artifacts.get()
        if (files.present) checksum.files = files.get()
        checksum.algorithms = (Set<Algorithm>) algorithms.getOrElse([])
        checksum
    }
}
