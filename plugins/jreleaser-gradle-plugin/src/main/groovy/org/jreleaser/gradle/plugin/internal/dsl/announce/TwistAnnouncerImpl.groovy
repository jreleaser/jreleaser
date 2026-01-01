/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.announce.TwistAnnouncer

import javax.inject.Inject

/**
 *
 * @author Usman Shaikh
 * @since 1.23.0
 */
@CompileStatic
class TwistAnnouncerImpl extends AbstractAnnouncer implements TwistAnnouncer {
    final Property<String> accessToken
    final Property<String> channelId
    final Property<String> threadId
    final Property<String> title
    final Property<String> message
    final Property<String> messageTemplate

    @Inject
    TwistAnnouncerImpl(ObjectFactory objects) {
        super(objects)
        accessToken = objects.property(String).convention(Providers.<String> notDefined())
        channelId = objects.property(String).convention(Providers.<String> notDefined())
        threadId = objects.property(String).convention(Providers.<String> notDefined())
        title = objects.property(String).convention(Providers.<String> notDefined())
        message = objects.property(String).convention(Providers.<String> notDefined())
        messageTemplate = objects.property(String).convention(Providers.<String> notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            accessToken.present ||
            channelId.present ||
            threadId.present ||
            title.present ||
            message.present ||
            messageTemplate.present
    }

    org.jreleaser.model.internal.announce.TwistAnnouncer toModel() {
        org.jreleaser.model.internal.announce.TwistAnnouncer announcer = new org.jreleaser.model.internal.announce.TwistAnnouncer()
        fillProperties(announcer)

        if (accessToken.present) announcer.accessToken = accessToken.get()
        if (channelId.present) announcer.channelId = channelId.get()
        if (threadId.present) announcer.threadId = threadId.get()
        if (title.present) announcer.title = title.get()
        if (message.present) announcer.message = message.get()
        if (messageTemplate.present) announcer.messageTemplate = messageTemplate.get()
        announcer
    }
}
