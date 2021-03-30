/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
package org.jreleaser.gradle.plugin.dsl

import groovy.transform.CompileStatic
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.Announcer

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface Mail extends Announcer {
    Property<org.jreleaser.model.Mail.Transport> getTransport()

    Property<org.jreleaser.model.Mail.MimeType> getMimeType()

    void setTransport(String transport)

    void setMimeType(String mimeType)

    Property<Integer> getPort()

    Property<Boolean> getAuth()

    Property<String> getHost()

    Property<String> getUsername()

    Property<String> getPassword()

    Property<String> getFrom()

    Property<String> getTo()

    Property<String> getCc()

    Property<String> getBcc()

    Property<String> getSubject()

    Property<String> getMessage()

    RegularFileProperty getMessageTemplate()

    MapProperty<String, String> getProperties()
}