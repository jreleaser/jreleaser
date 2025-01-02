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
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.announce.HttpAnnouncer
import org.jreleaser.model.Http

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.3.0
 */
@CompileStatic
class HttpAnnouncerImpl extends AbstractAnnouncer implements HttpAnnouncer {
    String name
    final Property<String> url
    final Property<String> username
    final Property<String> password
    final Property<Http.Method> method
    final Property<Http.Authorization> authorization
    final Property<String> bearerKeyword
    final MapProperty<String, String> headers
    final Property<String> payload
    final RegularFileProperty payloadTemplate

    @Inject
    HttpAnnouncerImpl(ObjectFactory objects) {
        super(objects)
        url = objects.property(String).convention(Providers.<String> notDefined())
        username = objects.property(String).convention(Providers.<String> notDefined())
        password = objects.property(String).convention(Providers.<String> notDefined())
        method = objects.property(Http.Method).convention(Providers.<Http.Method> notDefined())
        authorization = objects.property(Http.Authorization).convention(Providers.<Http.Authorization> notDefined())
        bearerKeyword = objects.property(String).convention(Providers.<String> notDefined())
        headers = objects.mapProperty(String, String).convention(Providers.notDefined())
        payload = objects.property(String).convention(Providers.<String> notDefined())
        payloadTemplate = objects.fileProperty().convention(Providers.notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            url.present ||
            username.present ||
            password.present ||
            method.present ||
            authorization.present ||
            bearerKeyword.present ||
            headers.present ||
            payload.present ||
            payloadTemplate.present
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

    @Override
    void setMethod(String method) {
        this.method.set(Http.Method.of(method))
    }

    @Override
    void setPayloadTemplate(String payloadTemplate) {
        this.payloadTemplate.set(new File(payloadTemplate))
    }

    org.jreleaser.model.internal.announce.HttpAnnouncer toModel() {
        org.jreleaser.model.internal.announce.HttpAnnouncer announcer = new org.jreleaser.model.internal.announce.HttpAnnouncer()
        announcer.name = name
        fillProperties(announcer)
        if (url.present) announcer.url = url.get()
        if (username.present) announcer.username = username.get()
        if (password.present) announcer.password = password.get()
        if (method.present) announcer.method = method.get()
        if (authorization.present) announcer.authorization = authorization.get()
        if (bearerKeyword.present) announcer.bearerKeyword = bearerKeyword.get()
        if (headers.present) announcer.headers.putAll(headers.get())
        if (payload.present) announcer.payload = payload.get()
        if (payloadTemplate.present) {
            announcer.payloadTemplate = payloadTemplate.asFile.get().absolutePath
        }
        announcer
    }
}
