/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.Announce
import org.jreleaser.gradle.plugin.dsl.Discussions
import org.jreleaser.gradle.plugin.dsl.Mail
import org.jreleaser.gradle.plugin.dsl.Sdkman
import org.jreleaser.gradle.plugin.dsl.Twitter
import org.jreleaser.gradle.plugin.dsl.Zulip

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class AnnounceImpl implements Announce {
    final Property<Boolean> enabled
    final DiscussionsImpl discussions
    final MailImpl mail
    final SdkmanImpl sdkman
    final TwitterImpl twitter
    final ZulipImpl zulip

    @Inject
    AnnounceImpl(ObjectFactory objects) {
        enabled = objects.property(Boolean).convention(Providers.notDefined())
        discussions = objects.newInstance(DiscussionsImpl, objects)
        mail = objects.newInstance(MailImpl, objects)
        sdkman = objects.newInstance(SdkmanImpl, objects)
        twitter = objects.newInstance(TwitterImpl, objects)
        zulip = objects.newInstance(ZulipImpl, objects)
    }

    @Override
    void discussions(Action<? super Discussions> action) {
        action.execute(discussions)
    }

    @Override
    void mail(Action<? super Mail> action) {
        action.execute(mail)
    }

    @Override
    void sdkman(Action<? super Sdkman> action) {
        action.execute(sdkman)
    }

    @Override
    void twitter(Action<? super Twitter> action) {
        action.execute(twitter)
    }

    @Override
    void zulip(Action<? super Zulip> action) {
        action.execute(zulip)
    }

    org.jreleaser.model.Announce toModel() {
        org.jreleaser.model.Announce announce = new org.jreleaser.model.Announce()
        if (enabled.present) announce.enabled = enabled.get()
        if (discussions.isSet()) announce.discussions = discussions.toModel()
        if (mail.isSet()) announce.mail = mail.toModel()
        if (sdkman.isSet()) announce.sdkman = sdkman.toModel()
        if (twitter.isSet()) announce.twitter = twitter.toModel()
        if (zulip.isSet()) announce.zulip = zulip.toModel()
        announce
    }
}
