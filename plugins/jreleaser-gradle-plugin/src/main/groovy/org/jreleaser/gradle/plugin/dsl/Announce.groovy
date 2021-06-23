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
package org.jreleaser.gradle.plugin.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.provider.Property

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface Announce {
    Property<Boolean> getEnabled()

    Discord getDiscord()

    Discussions getDiscussions()

    Gitter getGitter()

    GoogleChat getGoogleChat()

    Mail getMail()

    Mastodon getMastodon()

    Mattermost getMattermost()

    Sdkman getSdkman()

    Slack getSlack()

    Teams getTeams()

    Twitter getTwitter()

    Zulip getZulip()

    void discord(Action<? super Discord> action)

    void discussions(Action<? super Discussions> action)

    void gitter(Action<? super Gitter> action)

    void googleChat(Action<? super GoogleChat> action)

    void mail(Action<? super Mail> action)

    void mastodon(Action<? super Mastodon> action)

    void mattermost(Action<? super Mattermost> action)

    void sdkman(Action<? super Sdkman> action)

    void slack(Action<? super Slack> action)

    void teams(Action<? super Teams> action)

    void twitter(Action<? super Twitter> action)

    void zulip(Action<? super Zulip> action)

    void discord(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Discord) Closure<Void> action)

    void discussions(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Discussions) Closure<Void> action)

    void gitter(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Gitter) Closure<Void> action)

    void googleChat(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GoogleChat) Closure<Void> action)

    void mail(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Mail) Closure<Void> action)

    void mastodon(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Mastodon) Closure<Void> action)

    void mattermost(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Mattermost) Closure<Void> action)

    void sdkman(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Sdkman) Closure<Void> action)

    void slack(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Slack) Closure<Void> action)

    void teams(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Teams) Closure<Void> action)

    void twitter(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Twitter) Closure<Void> action)

    void zulip(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Zulip) Closure<Void> action)
}