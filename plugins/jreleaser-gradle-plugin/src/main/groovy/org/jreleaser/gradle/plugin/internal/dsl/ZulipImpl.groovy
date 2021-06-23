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
import org.jreleaser.gradle.plugin.dsl.Zulip

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ZulipImpl extends AbstractAnnouncer implements Zulip {
    final Property<String> account
    final Property<String> apiKey
    final Property<String> apiHost
    final Property<String> channel
    final Property<String> subject
    final Property<String> message
    final RegularFileProperty messageTemplate

    @Inject
    ZulipImpl(ObjectFactory objects) {
        super(objects)
        account = objects.property(String).convention(Providers.notDefined())
        apiKey = objects.property(String).convention(Providers.notDefined())
        apiHost = objects.property(String).convention(Providers.notDefined())
        channel = objects.property(String).convention(Providers.notDefined())
        subject = objects.property(String).convention(Providers.notDefined())
        message = objects.property(String).convention(Providers.notDefined())
        messageTemplate = objects.fileProperty().convention(Providers.notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            account.present ||
            apiKey.present ||
            apiHost.present ||
            channel.present ||
            subject.present ||
            message.present ||
            messageTemplate.present
    }

    org.jreleaser.model.Zulip toModel() {
        org.jreleaser.model.Zulip zulip = new org.jreleaser.model.Zulip()
        fillProperties(zulip)
        if (account.present) zulip.account = account.get()
        if (apiKey.present) zulip.apiKey = apiKey.get()
        if (apiHost.present) zulip.apiHost = apiHost.get()
        if (channel.present) zulip.channel = channel.get()
        if (subject.present) zulip.subject = subject.get()
        if (message.present) zulip.message = message.get()
        if (messageTemplate.present) {
            zulip.messageTemplate = messageTemplate.asFile.get().absolutePath
        }
        zulip
    }
}
