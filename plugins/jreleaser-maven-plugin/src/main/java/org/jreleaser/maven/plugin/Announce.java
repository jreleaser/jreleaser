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
public class Announce {
    private final Sdkman sdkman = new Sdkman();
    private final Twitter twitter = new Twitter();
    private final Zulip zulip = new Zulip();

    void setAll(Announce announce) {
        setSdkman(announce.sdkman);
        setTwitter(announce.twitter);
        setZulip(announce.zulip);
    }

    public Sdkman getSdkman() {
        return sdkman;
    }

    public void setSdkman(Sdkman sdkman) {
        this.sdkman.setAll(sdkman);
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
