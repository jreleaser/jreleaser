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
import org.jreleaser.gradle.plugin.dsl.Telegram

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
class TelegramImpl extends AbstractAnnouncer implements Telegram {
    final Property<String> token
    final Property<String> chatId
    final Property<String> message
    final RegularFileProperty messageTemplate

    @Inject
    TelegramImpl(ObjectFactory objects) {
        super(objects)
        token = objects.property(String).convention(Providers.notDefined())
        chatId = objects.property(String).convention(Providers.notDefined())
        message = objects.property(String).convention(Providers.notDefined())
        messageTemplate = objects.fileProperty().convention(Providers.notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            token.present ||
            chatId.present ||
            message.present ||
            messageTemplate.present
    }

    org.jreleaser.model.Telegram toModel() {
        org.jreleaser.model.Telegram telegram = new org.jreleaser.model.Telegram()
        fillProperties(telegram)
        if (token.present) telegram.token = token.get()
        if (chatId.present) telegram.chatId = token.get()
        if (message.present) telegram.message = message.get()
        if (messageTemplate.present) {
            telegram.messageTemplate = messageTemplate.asFile.get().absolutePath
        }
        telegram
    }
}
