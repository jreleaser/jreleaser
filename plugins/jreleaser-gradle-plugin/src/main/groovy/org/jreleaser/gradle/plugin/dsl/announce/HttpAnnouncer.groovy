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
package org.jreleaser.gradle.plugin.dsl.announce

import groovy.transform.CompileStatic
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.jreleaser.model.Http

/**
 *
 * @author Andres Almiray
 * @since 1.3.0
 */
@CompileStatic
interface HttpAnnouncer extends Announcer {
    Property<String> getServerRef()

    Property<String> getUrl()

    Property<String> getUsername()

    Property<String> getPassword()

    Property<Http.Method> getMethod()

    Property<Http.Authorization> getAuthorization()

    Property<String> getBearerKeyword()

    MapProperty<String, String> getHeaders()

    void setHeader(String key, String value)

    void setAuthorization(String authorization)

    void setMethod(String method)

    Property<String> getPayload()

    RegularFileProperty getPayloadTemplate()

    void setPayloadTemplate(String payloadTemplate)
}