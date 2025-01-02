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
import org.jreleaser.gradle.plugin.dsl.announce.BlueskyAnnouncer

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Simon Verhoeven
 * @author Tom Cools
 * @since 1.7.0
 */
@CompileStatic
class BlueskyAnnouncerImpl extends AbstractAnnouncer implements BlueskyAnnouncer {
    final Property<String> host
    final Property<String> handle
    final Property<String> password
    final Property<String> status
    final Property<String> statusTemplate
    final ListProperty<String> statuses

    @Inject
    BlueskyAnnouncerImpl(ObjectFactory objects) {
        super(objects)
        host = objects.property(String).convention(Providers.<String> notDefined())
        handle = objects.property(String).convention(Providers.<String> notDefined())
        password = objects.property(String).convention(Providers.<String> notDefined())
        status = objects.property(String).convention(Providers.<String> notDefined())
        statusTemplate = objects.property(String).convention(Providers.<String> notDefined())
        statuses = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
    }

    @Override
    void status(String message) {
        if (isNotBlank(message)) {
            statuses.add(message.trim())
        }
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            host.present ||
            handle.present ||
            password.present ||
            status.present ||
            statusTemplate.present ||
            statuses.present
    }

    org.jreleaser.model.internal.announce.BlueskyAnnouncer toModel() {
        org.jreleaser.model.internal.announce.BlueskyAnnouncer announcer = new org.jreleaser.model.internal.announce.BlueskyAnnouncer()
        fillProperties(announcer)

        if (host.present) announcer.host = host.get()
        if (handle.present) announcer.handle = handle.get()
        if (password.present) announcer.password = password.get()
        if (status.present) announcer.status = status.get()
        if (statusTemplate.present) announcer.statusTemplate = statusTemplate.get()
        announcer.statuses = (List<String>) statuses.getOrElse([])
        announcer
    }
}
