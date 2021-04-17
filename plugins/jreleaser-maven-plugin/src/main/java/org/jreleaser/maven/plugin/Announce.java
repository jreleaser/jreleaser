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
package org.jreleaser.maven.plugin;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Announce implements EnabledProvider {
    private final Discussions discussions = new Discussions();
    private final Mail mail = new Mail();
    private final Sdkman sdkman = new Sdkman();
    private final Slack slack = new Slack();
    private final Twitter twitter = new Twitter();
    private final Zulip zulip = new Zulip();
    private Boolean enabled;

    void setAll(Announce announce) {
        this.enabled = announce.enabled;
        setDiscussions(announce.discussions);
        setMail(announce.mail);
        setSdkman(announce.sdkman);
        setSlack(announce.slack);
        setTwitter(announce.twitter);
        setZulip(announce.zulip);
    }

    @Override
    public Boolean isEnabled() {
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
}
