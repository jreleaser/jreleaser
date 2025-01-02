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
import org.jreleaser.gradle.plugin.dsl.announce.WebhookAnnouncer

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.5.0
 */
@CompileStatic
class WebhookAnnouncerImpl extends AbstractAnnouncer implements WebhookAnnouncer {
    String name
    final Property<String> webhook
    final Property<String> message
    final Property<String> messageProperty
    final RegularFileProperty messageTemplate
    final Property<Boolean> structuredMessage

    @Inject
    WebhookAnnouncerImpl(ObjectFactory objects) {
        super(objects)
        webhook = objects.property(String).convention(Providers.<String> notDefined())
        message = objects.property(String).convention(Providers.<String> notDefined())
        messageProperty = objects.property(String).convention(Providers.<String> notDefined())
        messageTemplate = objects.fileProperty().convention(Providers.notDefined())
        structuredMessage = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
    }

    @Override
    void setMessageTemplate(String messageTemplate) {
        this.messageTemplate.set(new File(messageTemplate))
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            webhook.present ||
            message.present ||
            messageProperty.present ||
            messageTemplate.present ||
            structuredMessage.present
    }

    org.jreleaser.model.internal.announce.WebhookAnnouncer toModel() {
        org.jreleaser.model.internal.announce.WebhookAnnouncer announcer = new org.jreleaser.model.internal.announce.WebhookAnnouncer()
        fillProperties(announcer)
        announcer.name = name
        if (webhook.present) announcer.webhook = webhook.get()
        if (message.present) announcer.message = message.get()
        if (messageProperty.present) announcer.messageProperty = messageProperty.get()
        if (messageTemplate.present) {
            announcer.messageTemplate = messageTemplate.asFile.get().absolutePath
        }
        if (structuredMessage.present) announcer.structuredMessage = structuredMessage.get()
        announcer
    }
}
