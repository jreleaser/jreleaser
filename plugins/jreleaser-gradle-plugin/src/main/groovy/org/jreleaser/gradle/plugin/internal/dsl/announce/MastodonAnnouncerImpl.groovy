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
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.announce.MastodonAnnouncer

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.4.0
 */
@CompileStatic
class MastodonAnnouncerImpl extends AbstractAnnouncer implements MastodonAnnouncer {
    final Property<String> host
    final Property<String> accessToken
    final Property<String> status
    final Property<String> statusTemplate
    final ListProperty<String> statuses

    @Inject
    MastodonAnnouncerImpl(ObjectFactory objects) {
        super(objects)
        host = objects.property(String).convention(Providers.<String> notDefined())
        accessToken = objects.property(String).convention(Providers.<String> notDefined())
        status = objects.property(String).convention(Providers.<String> notDefined())
        statusTemplate = objects.property(String).convention(Providers.<String> notDefined())
        statuses = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            host.present ||
            accessToken.present ||
            status.present ||
            statusTemplate.present ||
            statuses.present
    }

    @Override
    void status(String message) {
        if (isNotBlank(message)) {
            statuses.add(message.trim())
        }
    }

    org.jreleaser.model.internal.announce.MastodonAnnouncer toModel() {
        org.jreleaser.model.internal.announce.MastodonAnnouncer announcer = new org.jreleaser.model.internal.announce.MastodonAnnouncer()
        fillProperties(announcer)
        if (host.present) announcer.host = host.get()
        if (accessToken.present) announcer.accessToken = accessToken.get()
        if (status.present) announcer.status = status.get()
        if (statusTemplate.present) announcer.statusTemplate = statusTemplate.get()
        announcer.statuses = (List<String>) statuses.getOrElse([])
        announcer
    }
}
