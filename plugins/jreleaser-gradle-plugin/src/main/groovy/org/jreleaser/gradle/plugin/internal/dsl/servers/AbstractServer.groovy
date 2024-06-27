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
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.servers.Server

import javax.inject.Inject

/**
 * @author Andres Almiray
 * @author Jan Wloka
 * @since 1.20.0
 */
@CompileStatic
abstract class AbstractServer implements Server {
    String name
    final Property<String> host
    final Property<Integer> port
    final Property<String> username
    final Property<String> password
    final Property<Integer> connectTimeout
    final Property<Integer> readTimeout

    @Inject
    AbstractServer(ObjectFactory objects) {
        host = objects.property(String).convention(Providers.<String> notDefined())
        port = objects.property(Integer).convention(Providers.<Integer> notDefined())
        username = objects.property(String).convention(Providers.<String> notDefined())
        password = objects.property(String).convention(Providers.<String> notDefined())
        connectTimeout = objects.property(Integer).convention(Providers.<Integer> notDefined())
        readTimeout = objects.property(Integer).convention(Providers.<Integer> notDefined())
    }

    @Internal
    boolean isSet() {
        host.present ||
            port.present ||
            username.present ||
            password.present ||
            connectTimeout.present ||
            readTimeout.present
    }

    protected <A extends org.jreleaser.model.internal.servers.Server<?>> void fillProperties(A server) {
        if (host.present) server.host = host.get()
        if (port.present) server.port = port.get()
        if (username.present) server.username = username.get()
        if (password.present) server.password = password.get()
        if (connectTimeout.present) server.connectTimeout = connectTimeout.get()
        if (readTimeout.present) server.readTimeout = readTimeout.get()
    }
}
