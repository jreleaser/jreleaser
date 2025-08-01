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
package org.jreleaser.gradle.plugin.dsl.announce

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.jreleaser.gradle.plugin.dsl.common.Activatable

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface Announce extends Activatable {
    ArticleAnnouncer getArticle()

    BlueskyAnnouncer getBluesky()

    DiscordAnnouncer getDiscord()

    DiscourseAnnouncer getDiscourse()

    DiscussionsAnnouncer getDiscussions()

    GitterAnnouncer getGitter()

    GoogleChatAnnouncer getGoogleChat()

    LinkedinAnnouncer getLinkedin()

    @Deprecated
    SmtpAnnouncer getMail()

    SmtpAnnouncer getSmtp()

    MastodonAnnouncer getMastodon()

    MattermostAnnouncer getMattermost()

    OpenCollectiveAnnouncer getOpenCollective()

    SdkmanAnnouncer getSdkman()

    SlackAnnouncer getSlack()

    TeamsAnnouncer getTeams()

    TelegramAnnouncer getTelegram()

    TwitterAnnouncer getTwitter()

    ZulipAnnouncer getZulip()

    NamedDomainObjectContainer<HttpAnnouncer> getHttp()

    NamedDomainObjectContainer<WebhookAnnouncer> getWebhooks()

    void article(Action<? super ArticleAnnouncer> action)

    void bluesky(Action<? super BlueskyAnnouncer> action)

    void discord(Action<? super DiscordAnnouncer> action)

    void discourse(Action<? super DiscourseAnnouncer> action)

    void discussions(Action<? super DiscussionsAnnouncer> action)

    void gitter(Action<? super GitterAnnouncer> action)

    void googleChat(Action<? super GoogleChatAnnouncer> action)

    void linkedin(Action<? super LinkedinAnnouncer> action)

    void http(Action<? super NamedDomainObjectContainer<HttpAnnouncer>> action)

    void mail(Action<? super SmtpAnnouncer> action)

    void smtp(Action<? super SmtpAnnouncer> action)

    void mastodon(Action<? super MastodonAnnouncer> action)

    void mattermost(Action<? super MattermostAnnouncer> action)

    void openCollective(Action<? super OpenCollectiveAnnouncer> action)

    void sdkman(Action<? super SdkmanAnnouncer> action)

    void slack(Action<? super SlackAnnouncer> action)

    void teams(Action<? super TeamsAnnouncer> action)

    void telegram(Action<? super TelegramAnnouncer> action)

    void twitter(Action<? super TwitterAnnouncer> action)

    void zulip(Action<? super ZulipAnnouncer> action)

    void webhooks(Action<? super NamedDomainObjectContainer<WebhookAnnouncer>> action)
}