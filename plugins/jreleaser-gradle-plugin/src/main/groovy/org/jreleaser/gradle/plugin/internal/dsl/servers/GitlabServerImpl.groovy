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
import org.jreleaser.gradle.plugin.dsl.servers.GitlabServer

import javax.inject.Inject

/**
 * @author Andres Almiray
 * @author Jan Wloka
 * @since 1.20.0
 */
@CompileStatic
class GitlabServerImpl extends AbstractServer implements GitlabServer {
    final Property<String> projectIdentifier

    @Inject
    GitlabServerImpl(ObjectFactory objects) {
        super(objects)
        projectIdentifier = objects.property(String).convention(Providers.<String> notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            projectIdentifier.present
    }

    org.jreleaser.model.internal.servers.GitlabServer toModel() {
        org.jreleaser.model.internal.servers.GitlabServer server = new org.jreleaser.model.internal.servers.GitlabServer()
        server.name = name
        fillProperties(server)
        if (projectIdentifier.present) server.projectIdentifier = projectIdentifier.get()
        server
    }
}
