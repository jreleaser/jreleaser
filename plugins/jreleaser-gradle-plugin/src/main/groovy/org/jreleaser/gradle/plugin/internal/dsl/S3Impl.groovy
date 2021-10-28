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
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.S3

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@CompileStatic
class S3Impl extends AbstractUploader implements S3 {
    String name
    final Property<String> region
    final Property<String> bucket
    final Property<String> accessKeyId
    final Property<String> secretKey
    final Property<String> sessionToken
    final Property<String> endpoint
    final Property<String> path
    final Property<String> downloadUrl
    final MapProperty<String, String> headers

    @Inject
    S3Impl(ObjectFactory objects) {
        super(objects)
        region = objects.property(String).convention(Providers.notDefined())
        bucket = objects.property(String).convention(Providers.notDefined())
        accessKeyId = objects.property(String).convention(Providers.notDefined())
        secretKey = objects.property(String).convention(Providers.notDefined())
        sessionToken = objects.property(String).convention(Providers.notDefined())
        endpoint = objects.property(String).convention(Providers.notDefined())
        path = objects.property(String).convention(Providers.notDefined())
        downloadUrl = objects.property(String).convention(Providers.notDefined())
        headers = objects.mapProperty(String, String).convention(Providers.notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            region.present ||
            bucket.present ||
            accessKeyId.present ||
            secretKey.present ||
            sessionToken.present ||
            endpoint.present ||
            path.present ||
            downloadUrl.present ||
            headers.present
    }

    @Override
    void setHeader(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            headers.put(key.trim(), value.trim())
        }
    }

    org.jreleaser.model.S3 toModel() {
        org.jreleaser.model.S3 s3 = new org.jreleaser.model.S3()
        s3.name = name
        fillProperties(s3)
        if (region.present) s3.region = region.get()
        if (bucket.present) s3.bucket = bucket.get()
        if (accessKeyId.present) s3.accessKeyId = accessKeyId.get()
        if (secretKey.present) s3.secretKey = secretKey.get()
        if (sessionToken.present) s3.sessionToken = sessionToken.get()
        if (endpoint.present) s3.endpoint = endpoint.get()
        if (path.present) s3.path = path.get()
        if (downloadUrl.present) s3.downloadUrl = downloadUrl.get()
        if (headers.present) s3.headers.putAll(headers.get())
        s3
    }
}
