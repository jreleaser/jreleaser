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
import org.jreleaser.gradle.plugin.dsl.Slack

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class SlackImpl extends AbstractAnnouncer implements Slack {
    final Property<String> token
    final Property<String> webhook
    final Property<String> channel
    final Property<String> message
    final RegularFileProperty messageTemplate

    @Inject
    SlackImpl(ObjectFactory objects) {
        super(objects)
        token = objects.property(String).convention(Providers.notDefined())
        webhook = objects.property(String).convention(Providers.notDefined())
        channel = objects.property(String).convention(Providers.notDefined())
        message = objects.property(String).convention(Providers.notDefined())
        messageTemplate = objects.fileProperty().convention(Providers.notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            token.present ||
            webhook.present ||
            channel.present ||
            message.present ||
            messageTemplate.present
    }

    org.jreleaser.model.Slack toModel() {
        org.jreleaser.model.Slack slack = new org.jreleaser.model.Slack()
        fillProperties(slack)
        if (token.present) slack.token = token.get()
        if (webhook.present) slack.webhook = webhook.get()
        if (channel.present) slack.channel = channel.get()
        if (message.present) slack.message = message.get()
        if (messageTemplate.present) {
            slack.messageTemplate = messageTemplate.asFile.get().absolutePath
        }
        slack
    }
}
