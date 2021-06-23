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
import org.jreleaser.gradle.plugin.dsl.Mattermost

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.4.0
 */
@CompileStatic
class MattermostImpl extends AbstractAnnouncer implements Mattermost {
    final Property<String> webhook
    final Property<String> message
    final RegularFileProperty messageTemplate

    @Inject
    MattermostImpl(ObjectFactory objects) {
        super(objects)
        webhook = objects.property(String).convention(Providers.notDefined())
        message = objects.property(String).convention(Providers.notDefined())
        messageTemplate = objects.fileProperty().convention(Providers.notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            webhook.present ||
            message.present ||
            messageTemplate.present
    }

    org.jreleaser.model.Mattermost toModel() {
        org.jreleaser.model.Mattermost mattermost = new org.jreleaser.model.Mattermost()
        fillProperties(mattermost)
        if (webhook.present) mattermost.webhook = webhook.get()
        if (message.present) mattermost.message = message.get()
        if (messageTemplate.present) {
            mattermost.messageTemplate = messageTemplate.asFile.get().absolutePath
        }
        mattermost
    }
}
