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
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.download.FtpDownloader

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 1.1.0
 */
@CompileStatic
class FtpDownloaderImpl extends AbstractDownloader implements FtpDownloader {
    String name
    final Property<String> username
    final Property<String> password
    final Property<String> host
    final Property<Integer> port

    @Inject
    FtpDownloaderImpl(ObjectFactory objects) {
        super(objects)
        username = objects.property(String).convention(Providers.<String> notDefined())
        password = objects.property(String).convention(Providers.<String> notDefined())
        host = objects.property(String).convention(Providers.<String> notDefined())
        port = objects.property(Integer).convention(Providers.<Integer> notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            username.present ||
            password.present ||
            host.present ||
            port.present
    }

    org.jreleaser.model.internal.download.FtpDownloader toModel() {
        org.jreleaser.model.internal.download.FtpDownloader downloader = new org.jreleaser.model.internal.download.FtpDownloader()
        downloader.name = name
        fillProperties(downloader)
        downloader.username = username.orNull
        downloader.password = password.orNull
        downloader.host = host.orNull
        if (port.present) downloader.port = port.get()
        downloader
    }
}
