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
package org.jreleaser.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Announce implements Domain, EnabledAware {
    private final Discord discord = new Discord();
    private final Discussions discussions = new Discussions();
    private final Gitter gitter = new Gitter();
    private final Mail mail = new Mail();
    private final Mastodon mastodon = new Mastodon();
    private final Mattermost mattermost = new Mattermost();
    private final Sdkman sdkman = new Sdkman();
    private final Slack slack = new Slack();
    private final Teams teams = new Teams();
    private final Twitter twitter = new Twitter();
    private final Zulip zulip = new Zulip();
    private final GoogleChat googleChat = new GoogleChat();
    private Boolean enabled;

    void setAll(Announce announce) {
        this.enabled = announce.enabled;
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
        setTwitter(announce.twitter);
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

    public void setGoogleChat(GoogleChat googleChat){
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

    public Sdkman getSdkman() {
        return sdkman;
    }

    public void setSdkman(Sdkman sdkman) {
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

    public Twitter getTwitter() {
        return twitter;
    }

    public void setTwitter(Twitter twitter) {
        this.twitter.setAll(twitter);
    }

    public Zulip getZulip() {
        return zulip;
    }

    public void setZulip(Zulip zulip) {
        this.zulip.setAll(zulip);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.putAll(discord.asMap(full));
        map.putAll(discussions.asMap(full));
        map.putAll(gitter.asMap(full));
        map.putAll(mail.asMap(full));
        map.putAll(mastodon.asMap(full));
        map.putAll(mattermost.asMap(full));
        map.putAll(sdkman.asMap(full));
        map.putAll(slack.asMap(full));
        map.putAll(teams.asMap(full));
        map.putAll(twitter.asMap(full));
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
        throw new JReleaserException("Announcer '" + name + "' has not been configured");
    }

    private <A extends Announcer> A resolveAnnouncer(String name) {
        switch (name.toLowerCase().trim()) {
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
            case Sdkman.NAME:
                return (A) getSdkman();
            case Slack.NAME:
                return (A) getSlack();
            case Teams.NAME:
                return (A) getTeams();
            case Twitter.NAME:
                return (A) getTwitter();
            case Zulip.NAME:
                return (A) getZulip();
            default:
                throw new JReleaserException("Unsupported announcer '" + name + "'");
        }
    }

    public static Set<String> supportedAnnouncers() {
        Set<String> set = new LinkedHashSet<>();
        set.add(Discord.NAME);
        set.add(Discussions.NAME);
        set.add(Gitter.NAME);
        set.add(Mail.NAME);
        set.add(Mastodon.NAME);
        set.add(Mattermost.NAME);
        set.add(Sdkman.NAME);
        set.add(Slack.NAME);
        set.add(Teams.NAME);
        set.add(Twitter.NAME);
        set.add(Zulip.NAME);
        return Collections.unmodifiableSet(set);
    }
}
