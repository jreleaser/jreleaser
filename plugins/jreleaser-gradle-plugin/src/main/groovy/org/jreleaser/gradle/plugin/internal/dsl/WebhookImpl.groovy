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
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Webhook

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.5.0
 */
@CompileStatic
class WebhookImpl extends AbstractAnnouncer implements Webhook {
    String name
    final Property<String> webhook
    final Property<String> message
    final Property<String> messageProperty
    final RegularFileProperty messageTemplate

    @Inject
    WebhookImpl(ObjectFactory objects) {
        super(objects)
        webhook = objects.property(String).convention(Providers.notDefined())
        message = objects.property(String).convention(Providers.notDefined())
        messageProperty = objects.property(String).convention(Providers.notDefined())
        messageTemplate = objects.fileProperty().convention(Providers.notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            webhook.present ||
            message.present ||
            messageProperty.present ||
            messageTemplate.present
    }

    org.jreleaser.model.Webhook toModel() {
        org.jreleaser.model.Webhook w = new org.jreleaser.model.Webhook()
        fillProperties(w)
        w.name = name
        if (webhook.present) w.webhook = webhook.get()
        if (message.present) w.message = message.get()
        if (messageProperty.present) w.messageProperty = messageProperty.get()
        if (messageTemplate.present) {
            w.messageTemplate = messageTemplate.asFile.get().absolutePath
        }
        w
    }
}
