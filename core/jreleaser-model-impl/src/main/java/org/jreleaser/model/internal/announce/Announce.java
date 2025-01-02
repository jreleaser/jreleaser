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
package org.jreleaser.model.internal.announce;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.Domain;

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
public final class Announce extends AbstractActivatable<Announce> implements Domain {
    private static final long serialVersionUID = 8244852443096292765L;

    private final ArticleAnnouncer article = new ArticleAnnouncer();
    private final BlueskyAnnouncer bluesky = new BlueskyAnnouncer();
    private final DiscordAnnouncer discord = new DiscordAnnouncer();
    private final DiscourseAnnouncer discourse = new DiscourseAnnouncer();
    private final DiscussionsAnnouncer discussions = new DiscussionsAnnouncer();
    private final GitterAnnouncer gitter = new GitterAnnouncer();
    private final GoogleChatAnnouncer googleChat = new GoogleChatAnnouncer();
    private final LinkedinAnnouncer linkedin = new LinkedinAnnouncer();
    private final SmtpAnnouncer smtp = new SmtpAnnouncer();
    private final MastodonAnnouncer mastodon = new MastodonAnnouncer();
    private final MattermostAnnouncer mattermost = new MattermostAnnouncer();
    private final OpenCollectiveAnnouncer openCollective = new OpenCollectiveAnnouncer();
    private final SdkmanAnnouncer sdkman = new SdkmanAnnouncer();
    private final SlackAnnouncer slack = new SlackAnnouncer();
    private final TeamsAnnouncer teams = new TeamsAnnouncer();
    private final TelegramAnnouncer telegram = new TelegramAnnouncer();
    private final TwitterAnnouncer twitter = new TwitterAnnouncer();
    private final ZulipAnnouncer zulip = new ZulipAnnouncer();
    @JsonIgnore
    private final HttpAnnouncers httpAnnouncers = new HttpAnnouncers();
    @JsonIgnore
    private final WebhooksAnnouncer webhooksAnnouncer = new WebhooksAnnouncer();

    @JsonIgnore
    private final org.jreleaser.model.api.announce.Announce immutable = new org.jreleaser.model.api.announce.Announce() {
        private static final long serialVersionUID = 2116386621001490270L;

        @Override
        public org.jreleaser.model.api.announce.ArticleAnnouncer getArticle() {
            return article.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.announce.BlueskyAnnouncer getBluesky() {
            return bluesky.asImmutable();
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
        public org.jreleaser.model.api.announce.LinkedinAnnouncer getLinkedInAnnouncer() {
            return linkedin.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.announce.SmtpAnnouncer getMail() {
            return smtp.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.announce.SmtpAnnouncer getSmtp() {
            return smtp.asImmutable();
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
        public org.jreleaser.model.api.announce.OpenCollectiveAnnouncer getOpenCollective() {
            return openCollective.asImmutable();
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
            return httpAnnouncers.asImmutable().getHttpAnnouncers();
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.announce.WebhookAnnouncer> getWebhooks() {
            return webhooksAnnouncer.asImmutable().getWebhooks();
        }

        @Override
        public org.jreleaser.model.api.announce.ZulipAnnouncer getZulip() {
            return zulip.asImmutable();
        }

        @Override
        public Active getActive() {
            return Announce.this.getActive();
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

    public Announce() {
        enabledSet(true);
    }

    public org.jreleaser.model.api.announce.Announce asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Announce source) {
        super.merge(source);
        setArticle(source.article);
        setBluesky(source.bluesky);
        setDiscord(source.discord);
        setDiscourse(source.discourse);
        setDiscussions(source.discussions);
        setGitter(source.gitter);
        setLinkedin(source.linkedin);
        setGoogleChat(source.googleChat);
        setSmtp(source.smtp);
        setMastodon(source.mastodon);
        setMattermost(source.mattermost);
        setOpenCollective(source.openCollective);
        setSdkman(source.sdkman);
        setSlack(source.slack);
        setTeams(source.teams);
        setTelegram(source.telegram);
        setTwitter(source.twitter);
        setZulip(source.zulip);
        setConfiguredHttp(source.httpAnnouncers);
        setConfiguredWebhooks(source.webhooksAnnouncer);
    }

    @Deprecated
    @JsonPropertyDescription("announce.enabled is deprecated since 1.1.0 and will be removed in 2.0.0")
    public void setEnabled(Boolean enabled) {
        nag("announce.enabled is deprecated since 1.1.0 and will be removed in 2.0.0");
        if (null != enabled) {
            setActive(enabled ? Active.ALWAYS : Active.NEVER);
        }
    }

    public ArticleAnnouncer getArticle() {
        return article;
    }

    public void setArticle(ArticleAnnouncer article) {
        this.article.merge(article);
    }

    public BlueskyAnnouncer getBluesky() {
        return bluesky;
    }

    public void setBluesky(BlueskyAnnouncer bluesky) {
        this.bluesky.merge(bluesky);
    }

    @JsonPropertyDescription("announce.discord is deprecated since 1.4.0 and will be removed in 2.0.0")
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

    @JsonPropertyDescription("announce.gitter is deprecated since 1.4.0 and will be removed in 2.0.0")
    public GitterAnnouncer getGitter() {
        return gitter;
    }

    public void setGitter(GitterAnnouncer gitter) {
        this.gitter.merge(gitter);
    }

    @JsonPropertyDescription("announce.googleChat is deprecated since 1.4.0 and will be removed in 2.0.0")
    public GoogleChatAnnouncer getGoogleChat() {
        return googleChat;
    }

    public void setGoogleChat(GoogleChatAnnouncer googleChat) {
        this.googleChat.merge(googleChat);
    }

    public LinkedinAnnouncer getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(LinkedinAnnouncer linkedin) {
        this.linkedin.merge(linkedin);
    }

    @Deprecated
    @JsonPropertyDescription("announce.mail is deprecated since 1.4.0 and will be removed in 2.0.0")
    public SmtpAnnouncer getMail() {
        return getSmtp();
    }

    @Deprecated
    @JsonPropertyDescription("announce.mail is deprecated since 1.4.0 and will be removed in 2.0.0")
    public void setMail(SmtpAnnouncer mail) {
        nag("announce.mail is deprecated since 1.4.0 and will be removed in 2.0.0");
        setSmtp(mail);
    }

    public SmtpAnnouncer getSmtp() {
        return smtp;
    }

    public void setSmtp(SmtpAnnouncer smtp) {
        this.smtp.merge(smtp);
    }

    public MastodonAnnouncer getMastodon() {
        return mastodon;
    }

    public void setMastodon(MastodonAnnouncer mastodon) {
        this.mastodon.merge(mastodon);
    }

    @JsonPropertyDescription("announce.mattermost is deprecated since 1.4.0 and will be removed in 2.0.0")
    public MattermostAnnouncer getMattermost() {
        return mattermost;
    }

    public void setMattermost(MattermostAnnouncer mattermost) {
        this.mattermost.merge(mattermost);
    }

    public OpenCollectiveAnnouncer getOpenCollective() {
        return openCollective;
    }

    public void setOpenCollective(OpenCollectiveAnnouncer openCollective) {
        this.openCollective.merge(openCollective);
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

    @JsonPropertyDescription("announce.teams is deprecated since 1.4.0 and will be removed in 2.0.0")
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

    @JsonIgnore
    public HttpAnnouncers getConfiguredHttp() {
        return this.httpAnnouncers;
    }

    void setConfiguredHttp(HttpAnnouncers https) {
        this.httpAnnouncers.merge(https);
    }

    public Map<String, HttpAnnouncer> getHttp() {
        return this.httpAnnouncers.getHttp();
    }

    public void setHttp(Map<String, HttpAnnouncer> https) {
        this.httpAnnouncers.setHttp(https);
    }

    public void addHttpAnnouncer(HttpAnnouncer http) {
        this.httpAnnouncers.addHttpAnnouncer(http);
    }

    @JsonIgnore
    public WebhooksAnnouncer getConfiguredWebhooks() {
        return this.webhooksAnnouncer;
    }

    void setConfiguredWebhooks(WebhooksAnnouncer webhooks) {
        this.webhooksAnnouncer.merge(webhooks);
    }

    public Map<String, WebhookAnnouncer> getWebhooks() {
        return this.webhooksAnnouncer.getWebhooks();
    }

    public void setWebhooks(Map<String, WebhookAnnouncer> webhooks) {
        this.webhooksAnnouncer.setWebhooks(webhooks);
    }

    public void addWebhook(WebhookAnnouncer webhook) {
        this.webhooksAnnouncer.addWebhook(webhook);
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
        map.put("active", getActive());
        map.putAll(article.asMap(full));
        map.putAll(bluesky.asMap(full));
        map.putAll(discord.asMap(full));
        map.putAll(discourse.asMap(full));
        map.putAll(discussions.asMap(full));
        map.putAll(gitter.asMap(full));
        map.putAll(googleChat.asMap(full));
        map.putAll(linkedin.asMap(full));
        map.putAll(httpAnnouncers.asMap(full));
        map.putAll(smtp.asMap(full));
        map.putAll(mastodon.asMap(full));
        map.putAll(mattermost.asMap(full));
        map.putAll(openCollective.asMap(full));
        map.putAll(sdkman.asMap(full));
        map.putAll(slack.asMap(full));
        map.putAll(teams.asMap(full));
        map.putAll(telegram.asMap(full));
        map.putAll(twitter.asMap(full));
        map.putAll(webhooksAnnouncer.asMap(full));
        map.putAll(zulip.asMap(full));
        return map;
    }

    public <A extends Announcer<?>> A findAnnouncer(String name) {
        if (isBlank(name)) {
            throw new JReleaserException("Announcer name must not be blank");
        }

        return resolveAnnouncer(name);
    }

    private <A extends Announcer<?>> A resolveAnnouncer(String name) {
        switch (name.toLowerCase(Locale.ENGLISH).trim()) {
            case org.jreleaser.model.api.announce.ArticleAnnouncer.TYPE:
                return (A) getArticle();
            case org.jreleaser.model.api.announce.BlueskyAnnouncer.TYPE:
                return (A) getBluesky();
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
            case org.jreleaser.model.api.announce.LinkedinAnnouncer.TYPE:
                return (A) getLinkedin();
            case org.jreleaser.model.api.announce.HttpAnnouncers.TYPE:
                return (A) getConfiguredHttp();
            case org.jreleaser.model.api.announce.SmtpAnnouncer.TYPE:
            case org.jreleaser.model.api.announce.SmtpAnnouncer.TYPE_LEGACY:
                return (A) getSmtp();
            case org.jreleaser.model.api.announce.MastodonAnnouncer.TYPE:
                return (A) getMastodon();
            case org.jreleaser.model.api.announce.MattermostAnnouncer.TYPE:
                return (A) getMattermost();
            case org.jreleaser.model.api.announce.OpenCollectiveAnnouncer.TYPE:
                return (A) getOpenCollective();
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
