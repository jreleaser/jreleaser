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
package org.jreleaser.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.bundle.RB;
import org.jreleaser.util.Env;
import org.jreleaser.util.JReleaserException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.JReleaserOutput.nag;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Announce extends AbstractModelObject<Announce> implements Domain, Activatable {
    private final Article article = new Article();
    private final Discord discord = new Discord();
    private final Discussions discussions = new Discussions();
    private final Gitter gitter = new Gitter();
    private final GoogleChat googleChat = new GoogleChat();
    private final Mail mail = new Mail();
    private final Mastodon mastodon = new Mastodon();
    private final Mattermost mattermost = new Mattermost();
    private final SdkmanAnnouncer sdkman = new SdkmanAnnouncer();
    private final Slack slack = new Slack();
    private final Teams teams = new Teams();
    private final Telegram telegram = new Telegram();
    private final Twitter twitter = new Twitter();
    private final Webhooks webhooks = new Webhooks();
    private final Zulip zulip = new Zulip();

    private Active active;
    @JsonIgnore
    private boolean enabled = true;

    @Override
    public void freeze() {
        super.freeze();
        article.freeze();
        discord.freeze();
        discussions.freeze();
        gitter.freeze();
        googleChat.freeze();
        mail.freeze();
        mastodon.freeze();
        mattermost.freeze();
        sdkman.freeze();
        slack.freeze();
        teams.freeze();
        telegram.freeze();
        twitter.freeze();
        webhooks.freeze();
        zulip.freeze();
    }

    @Override
    public void merge(Announce announce) {
        freezeCheck();
        this.active = merge(this.active, announce.active);
        this.enabled = merge(this.enabled, announce.enabled);
        setArticle(announce.article);
        setDiscord(announce.discord);
        setDiscussions(announce.discussions);
        setGitter(announce.gitter);
        setGoogleChat(announce.googleChat);
        setMail(announce.mail);
        setMastodon(announce.mastodon);
        setMattermost(announce.mattermost);
        setSdkman(announce.sdkman);
        setSlack(announce.slack);
        setTeams(announce.teams);
        setTelegram(announce.telegram);
        setTwitter(announce.twitter);
        setZulip(announce.zulip);
        setConfiguredWebhooks(announce.webhooks);
    }

    @Override
    public boolean isEnabled() {
        return enabled && active != null;
    }

    @Deprecated
    public void setEnabled(Boolean enabled) {
        nag("announce.enabled is deprecated since 1.1.0 and will be removed in 2.0.0");
        freezeCheck();
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

    @Override
    public Active getActive() {
        return active;
    }

    @Override
    public void setActive(Active active) {
        freezeCheck();
        this.active = active;
    }

    @Override
    public void setActive(String str) {
        setActive(Active.of(str));
    }

    @Override
    public boolean isActiveSet() {
        return active != null;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article.merge(article);
    }

    public Discord getDiscord() {
        return discord;
    }

    public void setDiscord(Discord discord) {
        this.discord.merge(discord);
    }

    public Discussions getDiscussions() {
        return discussions;
    }

    public void setDiscussions(Discussions discussions) {
        this.discussions.merge(discussions);
    }

    public Gitter getGitter() {
        return gitter;
    }

    public void setGitter(Gitter gitter) {
        this.gitter.merge(gitter);
    }

    public GoogleChat getGoogleChat() {
        return googleChat;
    }

    public void setGoogleChat(GoogleChat googleChat) {
        this.googleChat.merge(googleChat);
    }

    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail.merge(mail);
    }

    public Mastodon getMastodon() {
        return mastodon;
    }

    public void setMastodon(Mastodon mastodon) {
        this.mastodon.merge(mastodon);
    }

    public Mattermost getMattermost() {
        return mattermost;
    }

    public void setMattermost(Mattermost mattermost) {
        this.mattermost.merge(mattermost);
    }

    public SdkmanAnnouncer getSdkman() {
        return sdkman;
    }

    public void setSdkman(SdkmanAnnouncer sdkman) {
        this.sdkman.merge(sdkman);
    }

    public Slack getSlack() {
        return slack;
    }

    public void setSlack(Slack slack) {
        this.slack.merge(slack);
    }

    public Teams getTeams() {
        return teams;
    }

    public void setTeams(Teams teams) {
        this.teams.merge(teams);
    }

    public Telegram getTelegram() {
        return telegram;
    }

    public void setTelegram(Telegram telegram) {
        this.telegram.merge(telegram);
    }

    public Twitter getTwitter() {
        return twitter;
    }

    public void setTwitter(Twitter twitter) {
        this.twitter.merge(twitter);
    }

    public Webhooks getConfiguredWebhooks() {
        return this.webhooks;
    }

    void setConfiguredWebhooks(Webhooks webhooks) {
        this.webhooks.merge(webhooks);
    }

    public Map<String, Webhook> getWebhooks() {
        return this.webhooks.getWebhooks();
    }

    public void setWebhooks(Map<String, Webhook> webhooks) {
        this.webhooks.setWebhooks(webhooks);
    }

    public void addWebhook(Webhook webhook) {
        this.webhooks.addWebhook(webhook);
    }

    public Zulip getZulip() {
        return zulip;
    }

    public void setZulip(Zulip zulip) {
        this.zulip.merge(zulip);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", active);
        map.putAll(article.asMap(full));
        map.putAll(discord.asMap(full));
        map.putAll(discussions.asMap(full));
        map.putAll(gitter.asMap(full));
        map.putAll(googleChat.asMap(full));
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
            case Article.NAME:
                return (A) getArticle();
            case Discord.NAME:
                return (A) getDiscord();
            case Discussions.NAME:
                return (A) getDiscussions();
            case Gitter.NAME:
                return (A) getGitter();
            case GoogleChat.NAME:
                return (A) getGoogleChat();
            case Mail.NAME:
                return (A) getMail();
            case Mastodon.NAME:
                return (A) getMastodon();
            case Mattermost.NAME:
                return (A) getMattermost();
            case SdkmanAnnouncer.NAME:
                return (A) getSdkman();
            case Slack.NAME:
                return (A) getSlack();
            case Teams.NAME:
                return (A) getTeams();
            case Telegram.NAME:
                return (A) getTelegram();
            case Twitter.NAME:
                return (A) getTwitter();
            case Webhooks.NAME:
                return (A) getConfiguredWebhooks();
            case Zulip.NAME:
                return (A) getZulip();
            default:
                throw new JReleaserException(RB.$("ERROR_unsupported_announcer", name));
        }
    }

    public static Set<String> supportedAnnouncers() {
        Set<String> set = new LinkedHashSet<>();
        set.add(Article.NAME);
        set.add(Discord.NAME);
        set.add(Discussions.NAME);
        set.add(Gitter.NAME);
        set.add(GoogleChat.NAME);
        set.add(Mail.NAME);
        set.add(Mastodon.NAME);
        set.add(Mattermost.NAME);
        set.add(SdkmanAnnouncer.NAME);
        set.add(Slack.NAME);
        set.add(Teams.NAME);
        set.add(Telegram.NAME);
        set.add(Twitter.NAME);
        set.add(Webhooks.NAME);
        set.add(Zulip.NAME);
        return Collections.unmodifiableSet(set);
    }
}
