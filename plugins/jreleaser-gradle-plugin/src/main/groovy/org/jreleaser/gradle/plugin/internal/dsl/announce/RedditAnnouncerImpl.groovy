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
import org.jreleaser.gradle.plugin.dsl.announce.RedditAnnouncer

import javax.inject.Inject

/**
 *
 * @author Usman Shaikh
 * @since 1.21.0
 */
@CompileStatic
class RedditAnnouncerImpl extends AbstractAnnouncer implements RedditAnnouncer {
    final Property<String> clientId
    final Property<String> clientSecret
    final Property<String> username
    final Property<String> password
    final Property<String> subreddit
    final Property<String> title
    final Property<String> text
    final Property<String> textTemplate
    final Property<String> url
    final Property<String> submissionType

    @Inject
    RedditAnnouncerImpl(ObjectFactory objects) {
        super(objects)
        clientId = objects.property(String).convention(Providers.<String> notDefined())
        clientSecret = objects.property(String).convention(Providers.<String> notDefined())
        username = objects.property(String).convention(Providers.<String> notDefined())
        password = objects.property(String).convention(Providers.<String> notDefined())
        subreddit = objects.property(String).convention(Providers.<String> notDefined())
        title = objects.property(String).convention(Providers.<String> notDefined())
        text = objects.property(String).convention(Providers.<String> notDefined())
        textTemplate = objects.property(String).convention(Providers.<String> notDefined())
        url = objects.property(String).convention(Providers.<String> notDefined())
        submissionType = objects.property(String).convention(Providers.<String> notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            clientId.present ||
            clientSecret.present ||
            username.present ||
            password.present ||
            subreddit.present ||
            title.present ||
            text.present ||
            textTemplate.present ||
            url.present ||
            submissionType.present
    }

    org.jreleaser.model.internal.announce.RedditAnnouncer toModel() {
        org.jreleaser.model.internal.announce.RedditAnnouncer announcer = new org.jreleaser.model.internal.announce.RedditAnnouncer()
        fillProperties(announcer)

        if (clientId.present) announcer.clientId = clientId.get()
        if (clientSecret.present) announcer.clientSecret = clientSecret.get()
        if (username.present) announcer.username = username.get()
        if (password.present) announcer.password = password.get()
        if (subreddit.present) announcer.subreddit = subreddit.get()
        if (title.present) announcer.title = title.get()
        if (text.present) announcer.text = text.get()
        if (textTemplate.present) announcer.textTemplate = textTemplate.get()
        if (url.present) announcer.url = url.get()
        if (submissionType.present) announcer.submissionType = org.jreleaser.model.api.announce.RedditAnnouncer.SubmissionType.of(submissionType.get())
        announcer
    }
}