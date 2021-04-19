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

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Announce implements Domain, EnabledAware {
    private final Discussions discussions = new Discussions();
    private final Gitter gitter = new Gitter();
    private final Mail mail = new Mail();
    private final Sdkman sdkman = new Sdkman();
    private final Slack slack = new Slack();
    private final Twitter twitter = new Twitter();
    private final Zulip zulip = new Zulip();
    private Boolean enabled;

    void setAll(Announce announce) {
        this.enabled = announce.enabled;
        setDiscussions(announce.discussions);
        setGitter(announce.gitter);
        setMail(announce.mail);
        setSdkman(announce.sdkman);
        setSlack(announce.slack);
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

    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail.setAll(mail);
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
        map.putAll(discussions.asMap(full));
        map.putAll(gitter.asMap(full));
        map.putAll(mail.asMap(full));
        map.putAll(sdkman.asMap(full));
        map.putAll(slack.asMap(full));
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
            case Discussions.NAME:
                return (A) getDiscussions();
            case Gitter.NAME:
                return (A) getGitter();
            case Mail.NAME:
                return (A) getMail();
            case Sdkman.NAME:
                return (A) getSdkman();
            case Slack.NAME:
                return (A) getSlack();
            case Twitter.NAME:
                return (A) getTwitter();
            case Zulip.NAME:
                return (A) getZulip();
            default:
                throw new JReleaserException("Unsupported announcer '" + name + "'");
        }
    }
}
