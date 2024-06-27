/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 The JReleaser authors.
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
package org.jreleaser.gradle.plugin.internal.dsl.servers

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.servers.HttpServer
import org.jreleaser.model.Http

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 * @author Andres Almiray
 * @author Jan Wloka
 * @since 1.20.0
 */
@CompileStatic
class HttpServerImpl extends AbstractServer implements HttpServer {
    final Property<Http.Authorization> authorization
    final MapProperty<String, String> headers

    @Inject
    HttpServerImpl(ObjectFactory objects) {
        super(objects)
        authorization = objects.property(Http.Authorization).convention(Providers.<Http.Authorization> notDefined())
        headers = objects.mapProperty(String, String).convention(Providers.notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            authorization.present ||
            headers.present
    }

    @Override
    void header(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            headers.put(key.trim(), value.trim())
        }
    }

    @Override
    void setAuthorization(String authorization) {
        this.authorization.set(Http.Authorization.of(authorization))
    }

    org.jreleaser.model.internal.servers.HttpServer toModel() {
        org.jreleaser.model.internal.servers.HttpServer server = new org.jreleaser.model.internal.servers.HttpServer()
        server.name = name
        fillProperties(server)
        if (authorization.present) server.authorization = authorization.get()
        if (headers.present) server.headers.putAll(headers.get())
        server
    }
}
