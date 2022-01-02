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
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.Registry

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class RegistryImpl implements Registry {
    final String name
    final Property<String> server
    final Property<String> repositoryName
    final Property<String> username
    final Property<String> password

    @Inject
    RegistryImpl(String name, ObjectFactory objects) {
        this.name = name
        server = objects.property(String).convention(Providers.notDefined())
        repositoryName = objects.property(String).convention(Providers.notDefined())
        username = objects.property(String).convention(Providers.notDefined())
        password = objects.property(String).convention(Providers.notDefined())
    }

    org.jreleaser.model.Registry toModel() {
        org.jreleaser.model.Registry registry = new org.jreleaser.model.Registry()
        registry.serverName = name
        if (server.present) registry.server = server.get()
        if (repositoryName.present) registry.repositoryName = repositoryName.get()
        if (username.present) registry.username = username.get()
        if (password.present) registry.password = password.get()
        registry
    }
}
