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
package org.jreleaser.gradle.plugin.internal.dsl.download

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.download.HttpDownloader
import org.jreleaser.model.Http

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
    final Property<String> username
    final Property<String> password
    final Property<Http.Authorization> authorization
    final MapProperty<String, String> headers

    @Inject
    HttpDownloaderImpl(ObjectFactory objects) {
        super(objects)
        username = objects.property(String).convention(Providers.<String> notDefined())
        password = objects.property(String).convention(Providers.<String> notDefined())
        authorization = objects.property(Http.Authorization).convention(Providers.<Http.Authorization> notDefined())
        headers = objects.mapProperty(String, String).convention(Providers.notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            username.present ||
            password.present ||
            authorization.present ||
            headers.present
    }

    @Override
    void setHeader(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            headers.put(key.trim(), value.trim())
        }
    }

    @Override
    void setAuthorization(String authorization) {
        this.authorization.set(Http.Authorization.of(authorization))
    }

    org.jreleaser.model.internal.download.HttpDownloader toModel() {
        org.jreleaser.model.internal.download.HttpDownloader downloader = new org.jreleaser.model.internal.download.HttpDownloader()
        downloader.name = name
        fillProperties(downloader)
        if (username.present) downloader.username = username.get()
        if (password.present) downloader.password = password.get()
        if (authorization.present) downloader.authorization = authorization.get()
        if (headers.present) downloader.headers.putAll(headers.get())
        downloader
    }
}
