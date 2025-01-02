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
package org.jreleaser.gradle.plugin.internal.dsl.upload

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.upload.Uploader
import org.jreleaser.model.Active

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@CompileStatic
abstract class AbstractUploader implements Uploader {
    final Property<Active> active
    final Property<Integer> connectTimeout
    final Property<Integer> readTimeout
    final Property<Boolean> artifacts
    final Property<Boolean> files
    final Property<Boolean> signatures
    final Property<Boolean> checksums
    final Property<Boolean> catalogs
    final MapProperty<String, Object> extraProperties

    @Inject
    AbstractUploader(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        connectTimeout = objects.property(Integer).convention(Providers.<Integer> notDefined())
        readTimeout = objects.property(Integer).convention(Providers.<Integer> notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
        artifacts = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        files = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        signatures = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        checksums = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        catalogs = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
    }

    @Internal
    boolean isSet() {
        active.present ||
            connectTimeout.present ||
            readTimeout.present ||
            extraProperties.present ||
            artifacts.present ||
            files.present ||
            checksums.present ||
            catalogs.present ||
            signatures.present
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    protected <U extends org.jreleaser.model.internal.upload.Uploader> void fillProperties(U uploader) {
        if (active.present) uploader.active = active.get()
        if (connectTimeout.present) uploader.connectTimeout = connectTimeout.get()
        if (readTimeout.present) uploader.readTimeout = readTimeout.get()
        if (extraProperties.present) uploader.extraProperties.putAll(extraProperties.get())
        if (artifacts.present) uploader.artifacts = artifacts.get()
        if (files.present) uploader.files = files.get()
        if (signatures.present) uploader.signatures = signatures.get()
        if (checksums.present) uploader.checksums = checksums.get()
        if (catalogs.present) uploader.catalogs = catalogs.get()
    }
}
