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
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.upload.ForgejoUploader

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 1.18.0
 */
@CompileStatic
class ForgejoUploaderImpl extends AbstractUploader implements ForgejoUploader {
    String name
    final Property<String> host
    final Property<String> owner
    final Property<String> token
    final Property<String> packageName
    final Property<String> packageVersion

    @Inject
    ForgejoUploaderImpl(ObjectFactory objects) {
        super(objects)
        host = objects.property(String).convention(Providers.<String> notDefined())
        owner = objects.property(String).convention(Providers.<String> notDefined())
        token = objects.property(String).convention(Providers.<String> notDefined())
        packageName = objects.property(String).convention(Providers.<String> notDefined())
        packageVersion = objects.property(String).convention(Providers.<String> notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            host.present ||
            owner.present ||
            token.present ||
            packageName.present ||
            packageVersion.present
    }

    org.jreleaser.model.internal.upload.ForgejoUploader toModel() {
        org.jreleaser.model.internal.upload.ForgejoUploader uploader = new org.jreleaser.model.internal.upload.ForgejoUploader()
        uploader.name = name
        fillProperties(uploader)
        if (host.present) uploader.host = host.get()
        if (owner.present) uploader.owner = owner.get()
        if (token.present) uploader.token = token.get()
        if (packageName.present) uploader.packageName = packageName.get()
        if (packageVersion.present) uploader.packageVersion = packageVersion.get()
        uploader
    }
}
