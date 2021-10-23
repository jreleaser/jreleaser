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
package org.jreleaser.maven.plugin;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Announce implements EnabledAware {
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
    private final Map<String, Webhook> webhooks = new LinkedHashMap<>();
    private final Zulip zulip = new Zulip();
    private Boolean enabled;

    void setAll(Announce announce) {
        this.enabled = announce.enabled;
        setArticle(announce.article);
        setDiscord(announce.discord);
        setDiscussions(announce.discussions);
        setGitter(announce.gitter);
        setGoogleChat(announce.googleChat);
        setMail(announce.mail);
        setMastodon(announce.mastodon);
        setSdkman(announce.sdkman);
        setSlack(announce.slack);
        setTeams(announce.teams);
        setTelegram(announce.telegram);
        setTwitter(announce.twitter);
        setWebhooks(announce.webhooks);
        setZulip(announce.zulip);
    }

    @Override
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabledSet() {
        return enabled != null;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article.setAll(article);
    }

    public Discord getDiscord() {
        return discord;
    }

    public void setDiscord(Discord discord) {
        this.discord.setAll(discord);
    }

    public Discussions getDiscussions() {
        return discussions;
    }

    public void setDiscussions(Discussions discussions) {
        this.discussions.setAll(discussions);
    }

    public Gitter getGitter() {
        return gitter;
    }

    public void setGitter(Gitter gitter) {
        this.gitter.setAll(gitter);
    }

    public GoogleChat getGoogleChat() {
        return googleChat;
    }

    public void setGoogleChat(GoogleChat googleChat) {
        this.googleChat.setAll(googleChat);
    }

    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail.setAll(mail);
    }

    public Mastodon getMastodon() {
        return mastodon;
    }

    public void setMastodon(Mastodon mastodon) {
        this.mastodon.setAll(mastodon);
    }

    public Mattermost getMattermost() {
        return mattermost;
    }

    public void setMattermost(Mattermost mattermost) {
        this.mattermost.setAll(mattermost);
    }

    public SdkmanAnnouncer getSdkman() {
        return sdkman;
    }

    public void setSdkman(SdkmanAnnouncer sdkman) {
        this.sdkman.setAll(sdkman);
    }

    public Slack getSlack() {
        return slack;
    }

    public void setSlack(Slack slack) {
        this.slack.setAll(slack);
    }

    public Teams getTeams() {
        return teams;
    }

    public void setTeams(Teams teams) {
        this.teams.setAll(teams);
    }

    public Telegram getTelegram() {
        return telegram;
    }

    public void setTelegram(Telegram telegram) {
        this.telegram.setAll(telegram);
    }

    public Twitter getTwitter() {
        return twitter;
    }

    public void setTwitter(Twitter twitter) {
        this.twitter.setAll(twitter);
    }

    public Map<String, Webhook> getWebhooks() {
        return webhooks;
    }

    public void setWebhooks(Map<String, Webhook> webhooks) {
        this.webhooks.clear();
        this.webhooks.putAll(webhooks);
    }

    public Zulip getZulip() {
        return zulip;
    }

    public void setZulip(Zulip zulip) {
        this.zulip.setAll(zulip);
    }
}
