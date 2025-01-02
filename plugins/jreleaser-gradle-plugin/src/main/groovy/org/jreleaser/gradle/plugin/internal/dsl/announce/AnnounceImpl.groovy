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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.announce.Announce
import org.jreleaser.gradle.plugin.dsl.announce.ArticleAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.BlueskyAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.DiscordAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.DiscourseAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.DiscussionsAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.GitterAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.GoogleChatAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.HttpAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.LinkedinAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.MastodonAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.MattermostAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.OpenCollectiveAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.SdkmanAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.SlackAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.SmtpAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.TeamsAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.TelegramAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.TwitterAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.WebhookAnnouncer
import org.jreleaser.gradle.plugin.dsl.announce.ZulipAnnouncer
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class AnnounceImpl implements Announce {
    final Property<Active> active
    final ArticleAnnouncerImpl article
    final BlueskyAnnouncerImpl bluesky
    final DiscordAnnouncerImpl discord
    final DiscourseAnnouncerImpl discourse
    final DiscussionsAnnouncerImpl discussions
    final GitterAnnouncerImpl gitter
    final GoogleChatAnnouncerImpl googleChat
    final LinkedinAnnouncerImpl linkedin
    final SmtpAnnouncerImpl smtp
    final MastodonAnnouncerImpl mastodon
    final MattermostAnnouncerImpl mattermost
    final OpenCollectiveAnnouncerImpl openCollective
    final SdkmanAnnouncerImpl sdkman
    final SlackAnnouncerImpl slack
    final TeamsAnnouncerImpl teams
    final TelegramAnnouncerImpl telegram
    final TwitterAnnouncerImpl twitter
    final ZulipAnnouncerImpl zulip
    final NamedDomainObjectContainer<HttpAnnouncer> http
    final NamedDomainObjectContainer<WebhookAnnouncer> webhooks

    @Inject
    AnnounceImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        article = objects.newInstance(ArticleAnnouncerImpl, objects)
        bluesky = objects.newInstance(BlueskyAnnouncerImpl, objects)
        discord = objects.newInstance(DiscordAnnouncerImpl, objects)
        discourse = objects.newInstance(DiscourseAnnouncerImpl, objects)
        discussions = objects.newInstance(DiscussionsAnnouncerImpl, objects)
        gitter = objects.newInstance(GitterAnnouncerImpl, objects)
        googleChat = objects.newInstance(GoogleChatAnnouncerImpl, objects)
        linkedin = objects.newInstance(LinkedinAnnouncerImpl, objects)
        smtp = objects.newInstance(SmtpAnnouncerImpl, objects)
        mastodon = objects.newInstance(MastodonAnnouncerImpl, objects)
        openCollective = objects.newInstance(OpenCollectiveAnnouncerImpl, objects)
        mattermost = objects.newInstance(MattermostAnnouncerImpl, objects)
        sdkman = objects.newInstance(SdkmanAnnouncerImpl, objects)
        slack = objects.newInstance(SlackAnnouncerImpl, objects)
        teams = objects.newInstance(TeamsAnnouncerImpl, objects)
        telegram = objects.newInstance(TelegramAnnouncerImpl, objects)
        twitter = objects.newInstance(TwitterAnnouncerImpl, objects)
        zulip = objects.newInstance(ZulipAnnouncerImpl, objects)

        http = objects.domainObjectContainer(HttpAnnouncer, new NamedDomainObjectFactory<HttpAnnouncer>() {
            @Override
            HttpAnnouncer create(String name) {
                HttpAnnouncerImpl http = objects.newInstance(HttpAnnouncerImpl, objects)
                http.name = name
                return http
            }
        })

        webhooks = objects.domainObjectContainer(WebhookAnnouncer, new NamedDomainObjectFactory<WebhookAnnouncer>() {
            @Override
            WebhookAnnouncer create(String name) {
                WebhookAnnouncerImpl webhook = objects.newInstance(WebhookAnnouncerImpl, objects)
                webhook.name = name
                return webhook
            }
        })
    }

    @Deprecated
    SmtpAnnouncer getMail() {
        smtp
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void article(Action<? super ArticleAnnouncer> action) {
        action.execute(article)
    }

    @Override
    void bluesky(Action<? super BlueskyAnnouncer> action) {
        action.execute(bluesky)
    }

    @Override
    void discord(Action<? super DiscordAnnouncer> action) {
        action.execute(discord)
    }

    @Override
    void discourse(Action<? super DiscourseAnnouncer> action) {
        action.execute(discourse)
    }

    @Override
    void discussions(Action<? super DiscussionsAnnouncer> action) {
        action.execute(discussions)
    }

    @Override
    void gitter(Action<? super GitterAnnouncer> action) {
        action.execute(gitter)
    }

    @Override
    void googleChat(Action<? super GoogleChatAnnouncer> action) {
        action.execute(googleChat)
    }

    @Override
    void linkedin(Action<? super LinkedinAnnouncer> action) {
        action.execute(linkedin)
    }

    @Override
    void http(Action<? super NamedDomainObjectContainer<HttpAnnouncer>> action) {
        action.execute(http)
    }

    @Override
    void mail(Action<? super SmtpAnnouncer> action) {
        action.execute(smtp)
    }

    @Override
    void smtp(Action<? super SmtpAnnouncer> action) {
        action.execute(smtp)
    }

    @Override
    void mastodon(Action<? super MastodonAnnouncer> action) {
        action.execute(mastodon)
    }

    @Override
    void mattermost(Action<? super MattermostAnnouncer> action) {
        action.execute(mattermost)
    }

    @Override
    void openCollective(Action<? super OpenCollectiveAnnouncer> action) {
        action.execute(openCollective)
    }

    @Override
    void sdkman(Action<? super SdkmanAnnouncer> action) {
        action.execute(sdkman)
    }

    @Override
    void slack(Action<? super SlackAnnouncer> action) {
        action.execute(slack)
    }

    @Override
    void teams(Action<? super TeamsAnnouncer> action) {
        action.execute(teams)
    }

    @Override
    void telegram(Action<? super TelegramAnnouncer> action) {
        action.execute(telegram)
    }

    @Override
    void twitter(Action<? super TwitterAnnouncer> action) {
        action.execute(twitter)
    }

    @Override
    void zulip(Action<? super ZulipAnnouncer> action) {
        action.execute(zulip)
    }

    @Override
    void webhooks(Action<? super NamedDomainObjectContainer<WebhookAnnouncer>> action) {
        action.execute(webhooks)
    }

    @Override
    @CompileDynamic
    void article(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ArticleAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, article)
    }

    @Override
    @CompileDynamic
    void bluesky(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = BlueskyAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, bluesky)
    }

    @Override
    @CompileDynamic
    void discord(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DiscordAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, discord)
    }

    @Override
    @CompileDynamic
    void discourse(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DiscourseAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, discourse)
    }

    @Override
    @CompileDynamic
    void discussions(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DiscussionsAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, discussions)
    }

    @Override
    @CompileDynamic
    void gitter(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GitterAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, gitter)
    }

    @Override
    @CompileDynamic
    void googleChat(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GoogleChatAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, googleChat)
    }

    @Override
    @CompileDynamic
    void linkedin(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = LinkedinAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, linkedin)
    }

    @Override
    @CompileDynamic
    void http(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, http)
    }

    @Override
    @CompileDynamic
    void mail(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SmtpAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, smtp)
    }

    @Override
    @CompileDynamic
    void smtp(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SmtpAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, smtp)
    }

    @Override
    @CompileDynamic
    void mastodon(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = MastodonAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, mastodon)
    }

    @Override
    @CompileDynamic
    void mattermost(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = MattermostAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, mattermost)
    }

    @Override
    @CompileDynamic
    void openCollective(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = OpenCollectiveAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, openCollective)
    }

    @Override
    @CompileDynamic
    void sdkman(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SdkmanAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, sdkman)
    }

    @Override
    @CompileDynamic
    void slack(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SlackAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, slack)
    }

    @Override
    @CompileDynamic
    void teams(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = TeamsAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, teams)
    }

    @Override
    @CompileDynamic
    void telegram(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = TelegramAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, telegram)
    }

    @Override
    @CompileDynamic
    void twitter(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = TwitterAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, twitter)
    }

    @Override
    @CompileDynamic
    void zulip(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ZulipAnnouncer) Closure<Void> action) {
        ConfigureUtil.configure(action, zulip)
    }

    @Override
    @CompileDynamic
    void webhooks(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, webhooks)
    }

    org.jreleaser.model.internal.announce.Announce toModel() {
        org.jreleaser.model.internal.announce.Announce announce = new org.jreleaser.model.internal.announce.Announce()
        if (active.present) announce.active = active.get()
        if (article.isSet()) announce.article = article.toModel()
        if (bluesky.isSet()) announce.bluesky = bluesky.toModel()
        if (discord.isSet()) announce.discord = discord.toModel()
        if (discourse.isSet()) announce.discourse = discourse.toModel()
        if (discussions.isSet()) announce.discussions = discussions.toModel()
        if (gitter.isSet()) announce.gitter = gitter.toModel()
        if (googleChat.isSet()) announce.googleChat = googleChat.toModel()
        if (linkedin.isSet()) announce.linkedin = linkedin.toModel()
        if (smtp.isSet()) announce.smtp = smtp.toModel()
        if (mastodon.isSet()) announce.mastodon = mastodon.toModel()
        if (openCollective.isSet()) announce.openCollective = openCollective.toModel()
        if (mattermost.isSet()) announce.mattermost = mattermost.toModel()
        if (sdkman.isSet()) announce.sdkman = sdkman.toModel()
        if (slack.isSet()) announce.slack = slack.toModel()
        if (teams.isSet()) announce.teams = teams.toModel()
        if (telegram.isSet()) announce.telegram = telegram.toModel()
        if (twitter.isSet()) announce.twitter = twitter.toModel()
        if (zulip.isSet()) announce.zulip = zulip.toModel()

        http.toList().each { http ->
            announce.addHttpAnnouncer(((HttpAnnouncerImpl) http).toModel())
        }

        webhooks.toList().each { webhook ->
            announce.addWebhook(((WebhookAnnouncerImpl) webhook).toModel())
        }

        announce
    }
}
