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
import org.jreleaser.model.Mail

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface SmtpAnnouncer extends Announcer {
    Property<Mail.Transport> getTransport()

    Property<Mail.MimeType> getMimeType()

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

    void setMessageTemplate(String messageTemplate)

    MapProperty<String, String> getProperties()
}