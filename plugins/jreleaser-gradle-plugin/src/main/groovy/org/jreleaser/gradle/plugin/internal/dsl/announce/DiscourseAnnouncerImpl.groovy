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
package org.jreleaser.gradle.plugin.internal.dsl.announce

import groovy.transform.CompileStatic
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.announce.DiscourseAnnouncer

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 1.3.0
 */
@CompileStatic
class DiscourseAnnouncerImpl extends AbstractAnnouncer implements DiscourseAnnouncer {
    final Property<String> host
    final Property<String> username
    final Property<String> apiKey
    final Property<String> categoryName
    final Property<String> title
    final Property<String> message
    final RegularFileProperty messageTemplate

    @Inject
    DiscourseAnnouncerImpl(ObjectFactory objects) {
        super(objects)
        host = objects.property(String).convention(Providers.<String> notDefined())
        username = objects.property(String).convention(Providers.<String> notDefined())
        apiKey = objects.property(String).convention(Providers.<String> notDefined())
        categoryName = objects.property(String).convention(Providers.<String> notDefined())
        title = objects.property(String).convention(Providers.<String> notDefined())
        message = objects.property(String).convention(Providers.<String> notDefined())
        messageTemplate = objects.fileProperty().convention(Providers.notDefined())
    }

    @Override
    void setMessageTemplate(String messageTemplate) {
        this.messageTemplate.set(new File(messageTemplate))
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            username.present ||
            apiKey.present ||
            host.present ||
            categoryName.present ||
            title.present ||
            message.present ||
            messageTemplate.present
    }

    org.jreleaser.model.internal.announce.DiscourseAnnouncer toModel() {
        org.jreleaser.model.internal.announce.DiscourseAnnouncer announcer = new org.jreleaser.model.internal.announce.DiscourseAnnouncer()
        fillProperties(announcer)
        if (host.present) announcer.host = host.get()
        if (username.present) announcer.username = username.get()
        if (apiKey.present) announcer.apiKey = apiKey.get()
        if (categoryName.present) announcer.categoryName = categoryName.get()
        if (title.present) announcer.title = title.get()
        if (message.present) announcer.message = message.get()
        if (messageTemplate.present) {
            announcer.messageTemplate = messageTemplate.asFile.get().absolutePath
        }
        announcer
    }
}
