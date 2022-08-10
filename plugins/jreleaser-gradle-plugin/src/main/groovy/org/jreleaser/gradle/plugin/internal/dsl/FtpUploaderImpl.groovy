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
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.FtpUploader

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 1.1.0
 */
@CompileStatic
class FtpUploaderImpl extends AbstractUploader implements FtpUploader {
    String name
    final Property<String> username
    final Property<String> password
    final Property<String> host
    final Property<Integer> port
    final Property<String> path
    final Property<String> downloadUrl

    @Inject
    FtpUploaderImpl(ObjectFactory objects) {
        super(objects)
        username = objects.property(String).convention(Providers.notDefined())
        password = objects.property(String).convention(Providers.notDefined())
        host = objects.property(String).convention(Providers.notDefined())
        port = objects.property(Integer).convention(Providers.notDefined())
        path = objects.property(String).convention(Providers.notDefined())
        downloadUrl = objects.property(String).convention(Providers.notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            username.present ||
            password.present ||
            host.present ||
            port.present ||
            path.present ||
            downloadUrl.present
    }

    org.jreleaser.model.FtpUploader toModel() {
        org.jreleaser.model.FtpUploader ftp = new org.jreleaser.model.FtpUploader()
        ftp.name = name
        fillProperties(ftp)
        ftp.username = username.orNull
        ftp.password = password.orNull
        ftp.host = host.orNull
        ftp.path = path.orNull
        ftp.downloadUrl = downloadUrl.orNull
        if (port.present) ftp.port = port.get()
        ftp
    }
}
