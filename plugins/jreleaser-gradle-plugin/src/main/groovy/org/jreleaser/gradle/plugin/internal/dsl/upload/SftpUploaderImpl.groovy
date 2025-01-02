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
package org.jreleaser.gradle.plugin.internal.dsl.upload

import groovy.transform.CompileStatic
import org.gradle.api.model.ObjectFactory
import org.jreleaser.gradle.plugin.dsl.upload.SftpUploader

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 1.1.0
 */
@CompileStatic
class SftpUploaderImpl extends AbstractSshUploader implements SftpUploader {
    String name

    @Inject
    SftpUploaderImpl(ObjectFactory objects) {
        super(objects)
    }

    org.jreleaser.model.internal.upload.SftpUploader toModel() {
        org.jreleaser.model.internal.upload.SftpUploader uploader = new org.jreleaser.model.internal.upload.SftpUploader()
        uploader.name = name
        fillProperties(uploader)
        uploader
    }
}
