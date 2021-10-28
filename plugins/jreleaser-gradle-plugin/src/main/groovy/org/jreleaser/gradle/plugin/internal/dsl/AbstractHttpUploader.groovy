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
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.HttpUploader

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
abstract class AbstractHttpUploader extends AbstractUploader implements HttpUploader {
    final Property<String> uploadUrl
    final Property<String> downloadUrl

    @Inject
    AbstractHttpUploader(ObjectFactory objects) {
        super(objects)
        uploadUrl = objects.property(String).convention(Providers.notDefined())
        downloadUrl = objects.property(String).convention(Providers.notDefined())
    }

    @Internal
    boolean isSet() {
        super.isSet() ||
            uploadUrl.present ||
            downloadUrl.present
    }

    protected <U extends org.jreleaser.model.HttpUploader> void fillProperties(U uploader) {
        super.fillProperties(uploader)
        if (uploadUrl.present) uploader.uploadUrl = uploadUrl.get()
        if (downloadUrl.present) uploader.downloadUrl = downloadUrl.get()
    }
}
