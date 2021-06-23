/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
import org.jreleaser.gradle.plugin.dsl.Http
import org.jreleaser.model.Uploader

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@CompileStatic
class HttpImpl extends AbstractUploader implements Http {
    String name
    final Property<String> target
    final Property<String> username
    final Property<String> password
    final Property<Uploader.Method> method
    final Property<Uploader.Authorization> authorization
    final MapProperty<String, String> headers

    @Inject
    HttpImpl(ObjectFactory objects) {
        super(objects)
        target = objects.property(String).convention(Providers.notDefined())
        username = objects.property(String).convention(Providers.notDefined())
        password = objects.property(String).convention(Providers.notDefined())
        method = objects.property(Uploader.Method).convention(Providers.notDefined())
        authorization = objects.property(Uploader.Authorization).convention(Providers.notDefined())
        headers = objects.mapProperty(String, String).convention(Providers.notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            target.present ||
            username.present ||
            password.present ||
            method.present ||
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
        this.authorization.set(Uploader.Authorization.of(authorization))
    }

    @Override
    void setMethod(String method) {
        this.method.set(Uploader.Method.of(method))
    }

    org.jreleaser.model.HttpUploader toModel() {
        org.jreleaser.model.HttpUploader http = new org.jreleaser.model.HttpUploader()
        http.name = name
        fillProperties(http)
        if (target.present) http.target = target.get()
        if (username.present) http.username = username.get()
        if (password.present) http.password = password.get()
        if (method.present) http.method = method.get()
        if (authorization.present) http.authorization = authorization.get()
        if (headers.present) http.headers.putAll(headers.get())
        http
    }
}
