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

import org.jreleaser.util.Env;

import java.util.Map;

import static org.jreleaser.model.Sdkman.SDKMAN_CONSUMER_KEY;
import static org.jreleaser.model.Sdkman.SDKMAN_CONSUMER_TOKEN;
import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class SdkmanAnnouncer extends AbstractAnnouncer<SdkmanAnnouncer> {
    public static final String NAME = "sdkman";

    private String consumerKey;
    private String consumerToken;
    private String candidate;
    private String releaseNotesUrl;
    private String downloadUrl;
    private Sdkman.Command command;

    public SdkmanAnnouncer() {
        super(NAME);
    }

    @Override
    public void merge(SdkmanAnnouncer sdkman) {
        freezeCheck();
        super.merge(sdkman);
        this.consumerKey = merge(this.consumerKey, sdkman.consumerKey);
        this.consumerToken = merge(this.consumerToken, sdkman.consumerToken);
        this.candidate = merge(this.candidate, sdkman.candidate);
        this.releaseNotesUrl = merge(this.releaseNotesUrl, sdkman.releaseNotesUrl);
        this.downloadUrl = merge(this.downloadUrl, sdkman.downloadUrl);
        this.command = merge(this.command, sdkman.command);
    }

    @Override
    public boolean isSnapshotSupported() {
        return false;
    }

    public String getResolvedConsumerKey() {
        return Env.env(SDKMAN_CONSUMER_KEY, consumerKey);
    }

    public String getResolvedConsumerToken() {
        return Env.env(SDKMAN_CONSUMER_TOKEN, consumerToken);
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        freezeCheck();
        this.consumerKey = consumerKey;
    }

    public String getConsumerToken() {
        return consumerToken;
    }

    public void setConsumerToken(String consumerToken) {
        freezeCheck();
        this.consumerToken = consumerToken;
    }

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        freezeCheck();
        this.candidate = candidate;
    }

    public String getReleaseNotesUrl() {
        return releaseNotesUrl;
    }

    public void setReleaseNotesUrl(String releaseNotesUrl) {
        freezeCheck();
        this.releaseNotesUrl = releaseNotesUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        freezeCheck();
        this.downloadUrl = downloadUrl;
    }

    public Sdkman.Command getCommand() {
        return command;
    }

    public void setCommand(Sdkman.Command command) {
        freezeCheck();
        this.command = command;
    }

    public void setCommand(String str) {
        setCommand(Sdkman.Command.of(str));
    }

    public boolean isCommandSet() {
        return command != null;
    }

    public boolean isMajor() {
        return command == Sdkman.Command.MAJOR;
    }


    @Override
    protected void asMap(Map<String, Object> props, boolean full) {
        props.put("consumerKey", isNotBlank(getResolvedConsumerKey()) ? HIDE : UNSET);
        props.put("consumerToken", isNotBlank(getResolvedConsumerToken()) ? HIDE : UNSET);
        props.put("candidate", candidate);
        props.put("releaseNotesUrl", releaseNotesUrl);
        props.put("downloadUrl", downloadUrl);
        props.put("command", command);
    }
}
