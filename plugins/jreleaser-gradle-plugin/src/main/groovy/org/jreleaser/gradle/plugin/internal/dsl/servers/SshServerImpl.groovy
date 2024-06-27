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
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.servers.SshServer

import javax.inject.Inject

/**
 * @author Andres Almiray
 * @author Jan Wloka
 * @since 1.20.0
 */
@CompileStatic
class SshServerImpl extends AbstractServer implements SshServer {
    final RegularFileProperty knownHostsFile
    final Property<String> publicKey
    final Property<String> privateKey
    final Property<String> passphrase
    final Property<String> fingerprint

    @Inject
    SshServerImpl(ObjectFactory objects) {
        super(objects)
        knownHostsFile = objects.fileProperty().convention(Providers.notDefined())
        publicKey = objects.property(String).convention(Providers.<String> notDefined())
        privateKey = objects.property(String).convention(Providers.<String> notDefined())
        passphrase = objects.property(String).convention(Providers.<String> notDefined())
        fingerprint = objects.property(String).convention(Providers.<String> notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            knownHostsFile.present ||
            publicKey.present ||
            privateKey.present ||
            passphrase.present ||
            fingerprint.present
    }

    org.jreleaser.model.internal.servers.SshServer toModel() {
        org.jreleaser.model.internal.servers.SshServer server = new org.jreleaser.model.internal.servers.SshServer()
        server.name = name
        fillProperties(server)
        if (knownHostsFile.present) {
            server.knownHostsFile = knownHostsFile.asFile.get().absolutePath
        }
        server.publicKey = publicKey.orNull
        server.privateKey = privateKey.orNull
        server.passphrase = passphrase.orNull
        server.fingerprint = fingerprint.orNull
        server
    }
}
