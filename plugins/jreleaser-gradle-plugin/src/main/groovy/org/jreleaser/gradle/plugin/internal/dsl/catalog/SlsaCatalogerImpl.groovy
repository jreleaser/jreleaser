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
package org.jreleaser.gradle.plugin.internal.dsl.catalog

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.catalog.SlsaCataloger

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.7.0
 */
@CompileStatic
class SlsaCatalogerImpl extends AbstractCataloger implements SlsaCataloger {
    final Property<String> attestationName
    final Property<Boolean> artifacts
    final Property<Boolean> files
    final Property<Boolean> deployables
    final SetProperty<String> includes
    final SetProperty<String> excludes

    @Inject
    SlsaCatalogerImpl(ObjectFactory objects) {
        super(objects)
        attestationName = objects.property(String).convention(Providers.<String> notDefined())
        artifacts = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        files = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        deployables = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        includes = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        excludes = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
    }

    @Internal
    boolean isSet() {
        return super.isSet() ||
            attestationName.present ||
            artifacts.present ||
            files.present ||
            deployables.present ||
            includes.present ||
            excludes.present
    }

    void include(String str) {
        if (isNotBlank(str)) {
            includes.add(str.trim())
        }
    }

    void exclude(String str) {
        if (isNotBlank(str)) {
            excludes.add(str.trim())
        }
    }

    org.jreleaser.model.internal.catalog.SlsaCataloger toModel() {
        org.jreleaser.model.internal.catalog.SlsaCataloger cataloger = new org.jreleaser.model.internal.catalog.SlsaCataloger()
        fillProperties(cataloger)
        if (attestationName.present) cataloger.attestationName = attestationName.get()
        if (artifacts.present) cataloger.artifacts = artifacts.get()
        if (files.present) cataloger.files = files.get()
        if (deployables.present) cataloger.deployables = deployables.get()
        cataloger.includes = (Set<String>) includes.getOrElse([] as Set<String>)
        cataloger.includes = (Set<String>) includes.getOrElse([] as Set<String>)
        cataloger
    }
}
