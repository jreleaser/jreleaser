/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.announce.BlueskyAnnouncer

/**
 *
 * @author BEJUG
 * @since 1.7.0
 */
//TODO BEJUG
class BlueskyAnnouncerImpl extends AbstractAnnouncer implements BlueskyAnnouncer {
    BlueskyAnnouncerImpl(ObjectFactory objects) {
        super(objects)
    }

    @Override
    Property<String> getHost() {
        return null
    }

    @Override
    Property<String> getScreenName() {
        return null
    }

    @Override
    Property<String> getPassword() {
        return null
    }

    @Override
    Property<String> getStatus() {
        return null
    }

    @Override
    Property<String> getStatusTemplate() {
        return null
    }

    @Override
    ListProperty<String> getStatuses() {
        return null
    }

    @Override
    void status(String message) {

    }

    org.jreleaser.model.internal.announce.BlueskyAnnouncer toModel() {
        org.jreleaser.model.internal.announce.BlueskyAnnouncer announcer = new org.jreleaser.model.internal.announce.BlueskyAnnouncer()
        fillProperties(announcer)
        //TODO BEJUG
        announcer
    }
}
