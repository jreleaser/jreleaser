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
import org.jreleaser.gradle.plugin.dsl.HttpDownloader

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.1.0
 */
@CompileStatic
class HttpDownloaderImpl extends AbstractDownloader implements HttpDownloader {
    String name
    final Property<String> input
    final Property<String> output
    final MapProperty<String, String> headers
    final UnpackImpl unpack

    @Inject
    HttpDownloaderImpl(ObjectFactory objects) {
        super(objects)
        input = objects.property(String).convention(Providers.notDefined())
        output = objects.property(String).convention(Providers.notDefined())
        headers = objects.mapProperty(String, String).convention(Providers.notDefined())
        unpack = objects.newInstance(UnpackImpl, objects)
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            input.present ||
            output.present ||
            headers.present ||
            unpack.isSet()
    }

    @Override
    void setHeader(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            headers.put(key.trim(), value.trim())
        }
    }

    org.jreleaser.model.HttpDownloader toModel() {
        org.jreleaser.model.HttpDownloader http = new org.jreleaser.model.HttpDownloader()
        http.name = name
        fillProperties(http)
        if (input.present) http.input = input.get()
        if (output.present) http.output = output.get()
        if (headers.present) http.headers.putAll(headers.get())
        if (unpack.isSet()) http.unpack = unpack.toModel()
        http
    }
}
