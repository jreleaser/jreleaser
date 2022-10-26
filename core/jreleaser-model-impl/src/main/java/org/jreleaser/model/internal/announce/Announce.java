/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.model.internal.announce;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.util.Env;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.JReleaserOutput.nag;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class Announce extends AbstractModelObject<Announce> implements Domain, Activatable {
    private final ArticleAnnouncer article = new ArticleAnnouncer();
    private final DiscordAnnouncer discord = new DiscordAnnouncer();
    private final DiscourseAnnouncer discourse = new DiscourseAnnouncer();
    private final DiscussionsAnnouncer discussions = new DiscussionsAnnouncer();
    private final GitterAnnouncer gitter = new GitterAnnouncer();
    private final GoogleChatAnnouncer googleChat = new GoogleChatAnnouncer();
    private final HttpAnnouncers http = new HttpAnnouncers();
    private final SmtpAnnouncer mail = new SmtpAnnouncer();
    private final MastodonAnnouncer mastodon = new MastodonAnnouncer();
    private final MattermostAnnouncer mattermost = new MattermostAnnouncer();
    private final SdkmanAnnouncer sdkman = new SdkmanAnnouncer();
    private final SlackAnnouncer slack = new SlackAnnouncer();
    private final TeamsAnnouncer teams = new TeamsAnnouncer();
    private final TelegramAnnouncer telegram = new TelegramAnnouncer();
    private final TwitterAnnouncer twitter = new TwitterAnnouncer();
    private final WebhooksAnnouncer webhooks = new WebhooksAnnouncer();
    private final ZulipAnnouncer zulip = new ZulipAnnouncer();

    private Active active;
    @JsonIgnore
    private boolean enabled = true;

    private final org.jreleaser.model.api.announce.Announce immutable = new org.jreleaser.model.api.announce.Announce() {
        @Override
        public org.jreleaser.model.api.announce.ArticleAnnouncer getArticle() {
            return article.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.announce.DiscordAnnouncer getDiscord() {
            return discord.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.announce.DiscourseAnnouncer getDiscourse() {
            return discourse.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.announce.DiscussionsAnnouncer getDiscussions() {
            return discussions.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.announce.GitterAnnouncer getGitter() {
            return gitter.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.announce.GoogleChatAnnouncer getGoogleChat() {
            return googleChat.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.announce.SmtpAnnouncer getMail() {
            return mail.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.announce.MastodonAnnouncer getMastodon() {
            return mastodon.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.announce.MattermostAnnouncer getMattermost() {
            return mattermost.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.announce.SdkmanAnnouncer getSdkman() {
            return sdkman.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.announce.SlackAnnouncer getSlack() {
            return slack.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.announce.TeamsAnnouncer getTeams() {
            return teams.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.announce.TelegramAnnouncer getTelegram() {
            return telegram.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.announce.TwitterAnnouncer getTwitter() {
            return twitter.asImmutable();
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.announce.HttpAnnouncer> getHttp() {
            return http.asImmutable().getHttpAnnouncers();
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.announce.WebhookAnnouncer> getWebhooks() {
            return webhooks.asImmutable().getWebhooks();
        }

        @Override
        public org.jreleaser.model.api.announce.ZulipAnnouncer getZulip() {
            return zulip.asImmutable();
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return Announce.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Announce.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.announce.Announce asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Announce source) {
        this.active = merge(this.active, source.active);
        this.enabled = merge(this.enabled, source.enabled);
        setArticle(source.article);
        setDiscord(source.discord);
        setDiscourse(source.discourse);
        setDiscussions(source.discussions);
        setGitter(source.gitter);
        setGoogleChat(source.googleChat);
        setConfiguredHttp(source.http);
        setMail(source.mail);
        setMastodon(source.mastodon);
        setMattermost(source.mattermost);
        setSdkman(source.sdkman);
        setSlack(source.slack);
        setTeams(source.teams);
        setTelegram(source.telegram);
        setTwitter(source.twitter);
        setZulip(source.zulip);
        setConfiguredWebhooks(source.webhooks);
    }

    @Override
    public boolean isEnabled() {
        return enabled && active != null;
    }

    @Deprecated
    public void setEnabled(Boolean enabled) {
        nag("announce.enabled is deprecated since 1.1.0 and will be removed in 2.0.0");
        if (null != enabled) {
            this.active = enabled ? Active.ALWAYS : Active.NEVER;
        }
    }

    public void disable() {
        active = Active.NEVER;
        enabled = false;
    }

    public boolean resolveEnabled(Project project) {
        if (null == active) {
            setActive(Env.resolveOrDefault("announce.active", "", "ALWAYS"));
        }
        enabled = active.check(project);
        return enabled;
    }

    public Active getActive() {
        return active;
    }

    public void setActive(Active active) {
        this.active = active;
    }

    public void setActive(String str) {
        setActive(Active.of(str));
    }

    public boolean isActiveSet() {
        return active != null;
    }

    public ArticleAnnouncer getArticle() {
        return article;
    }

    public void setArticle(ArticleAnnouncer article) {
        this.article.merge(article);
    }

    public DiscordAnnouncer getDiscord() {
        return discord;
    }

    public void setDiscord(DiscordAnnouncer discord) {
        this.discord.merge(discord);
    }

    public DiscourseAnnouncer getDiscourse() {
        return discourse;
    }

    public void setDiscourse(DiscourseAnnouncer discourse) {
        this.discourse.merge(discourse);
    }

    public DiscussionsAnnouncer getDiscussions() {
        return discussions;
    }

    public void setDiscussions(DiscussionsAnnouncer discussions) {
        this.discussions.merge(discussions);
    }

    public GitterAnnouncer getGitter() {
        return gitter;
    }

    public void setGitter(GitterAnnouncer gitter) {
        this.gitter.merge(gitter);
    }

    public GoogleChatAnnouncer getGoogleChat() {
        return googleChat;
    }

    public void setGoogleChat(GoogleChatAnnouncer googleChat) {
        this.googleChat.merge(googleChat);
    }

    public SmtpAnnouncer getMail() {
        return mail;
    }

    public void setMail(SmtpAnnouncer mail) {
        this.mail.merge(mail);
    }

    public MastodonAnnouncer getMastodon() {
        return mastodon;
    }

    public void setMastodon(MastodonAnnouncer mastodon) {
        this.mastodon.merge(mastodon);
    }

    public MattermostAnnouncer getMattermost() {
        return mattermost;
    }

    public void setMattermost(MattermostAnnouncer mattermost) {
        this.mattermost.merge(mattermost);
    }

    public SdkmanAnnouncer getSdkman() {
        return sdkman;
    }

    public void setSdkman(SdkmanAnnouncer sdkman) {
        this.sdkman.merge(sdkman);
    }

    public SlackAnnouncer getSlack() {
        return slack;
    }

    public void setSlack(SlackAnnouncer slack) {
        this.slack.merge(slack);
    }

    public TeamsAnnouncer getTeams() {
        return teams;
    }

    public void setTeams(TeamsAnnouncer teams) {
        this.teams.merge(teams);
    }

    public TelegramAnnouncer getTelegram() {
        return telegram;
    }

    public void setTelegram(TelegramAnnouncer telegram) {
        this.telegram.merge(telegram);
    }

    public TwitterAnnouncer getTwitter() {
        return twitter;
    }

    public void setTwitter(TwitterAnnouncer twitter) {
        this.twitter.merge(twitter);
    }

    public HttpAnnouncers getConfiguredHttp() {
        return this.http;
    }

    void setConfiguredHttp(HttpAnnouncers https) {
        this.http.merge(https);
    }

    public Map<String, HttpAnnouncer> getHttp() {
        return this.http.getHttpAnnouncers();
    }

    public void setHttp(Map<String, HttpAnnouncer> https) {
        this.http.setHttpAnnouncers(https);
    }

    public void addHttpAnnouncer(HttpAnnouncer http) {
        this.http.addHttpAnnouncer(http);
    }

    public WebhooksAnnouncer getConfiguredWebhooks() {
        return this.webhooks;
    }

    void setConfiguredWebhooks(WebhooksAnnouncer webhooks) {
        this.webhooks.merge(webhooks);
    }

    public Map<String, WebhookAnnouncer> getWebhooks() {
        return this.webhooks.getWebhooks();
    }

    public void setWebhooks(Map<String, WebhookAnnouncer> webhooks) {
        this.webhooks.setWebhooks(webhooks);
    }

    public void addWebhook(WebhookAnnouncer webhook) {
        this.webhooks.addWebhook(webhook);
    }

    public ZulipAnnouncer getZulip() {
        return zulip;
    }

    public void setZulip(ZulipAnnouncer zulip) {
        this.zulip.merge(zulip);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", active);
        map.putAll(article.asMap(full));
        map.putAll(discord.asMap(full));
        map.putAll(discourse.asMap(full));
        map.putAll(discussions.asMap(full));
        map.putAll(gitter.asMap(full));
        map.putAll(googleChat.asMap(full));
        map.putAll(http.asMap(full));
        map.putAll(mail.asMap(full));
        map.putAll(mastodon.asMap(full));
        map.putAll(mattermost.asMap(full));
        map.putAll(sdkman.asMap(full));
        map.putAll(slack.asMap(full));
        map.putAll(teams.asMap(full));
        map.putAll(telegram.asMap(full));
        map.putAll(twitter.asMap(full));
        map.putAll(webhooks.asMap(full));
        map.putAll(zulip.asMap(full));
        return map;
    }

    public <A extends Announcer> A findAnnouncer(String name) {
        if (isBlank(name)) {
            throw new JReleaserException("Announcer name must not be blank");
        }

        return resolveAnnouncer(name);
    }

    public <A extends Announcer> A getAnnouncer(String name) {
        A announcer = findAnnouncer(name);
        if (null != announcer) {
            return announcer;
        }
        throw new JReleaserException(RB.$("ERROR_announcer_not_configured", name));
    }

    private <A extends Announcer> A resolveAnnouncer(String name) {
        switch (name.toLowerCase(Locale.ENGLISH).trim()) {
            case org.jreleaser.model.api.announce.ArticleAnnouncer.TYPE:
                return (A) getArticle();
            case org.jreleaser.model.api.announce.DiscordAnnouncer.TYPE:
                return (A) getDiscord();
            case org.jreleaser.model.api.announce.DiscourseAnnouncer.TYPE:
                return (A) getDiscourse();
            case org.jreleaser.model.api.announce.DiscussionsAnnouncer.TYPE:
                return (A) getDiscussions();
            case org.jreleaser.model.api.announce.GitterAnnouncer.TYPE:
                return (A) getGitter();
            case org.jreleaser.model.api.announce.GoogleChatAnnouncer.TYPE:
                return (A) getGoogleChat();
            case org.jreleaser.model.api.announce.HttpAnnouncers.TYPE:
                return (A) getConfiguredHttp();
            case org.jreleaser.model.api.announce.SmtpAnnouncer.TYPE:
                return (A) getMail();
            case org.jreleaser.model.api.announce.MastodonAnnouncer.TYPE:
                return (A) getMastodon();
            case org.jreleaser.model.api.announce.MattermostAnnouncer.TYPE:
                return (A) getMattermost();
            case org.jreleaser.model.api.announce.SdkmanAnnouncer.TYPE:
                return (A) getSdkman();
            case org.jreleaser.model.api.announce.SlackAnnouncer.TYPE:
                return (A) getSlack();
            case org.jreleaser.model.api.announce.TeamsAnnouncer.TYPE:
                return (A) getTeams();
            case org.jreleaser.model.api.announce.TelegramAnnouncer.TYPE:
                return (A) getTelegram();
            case org.jreleaser.model.api.announce.TwitterAnnouncer.TYPE:
                return (A) getTwitter();
            case org.jreleaser.model.api.announce.WebhooksAnnouncer.TYPE:
                return (A) getConfiguredWebhooks();
            case org.jreleaser.model.api.announce.ZulipAnnouncer.TYPE:
                return (A) getZulip();
            default:
                throw new JReleaserException(RB.$("ERROR_unsupported_announcer", name));
        }
    }
}
