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

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class SdkmanAnnouncer extends AbstractAnnouncer {
    private String consumerKey;
    private String consumerToken;
    private String candidate;
    private String releaseNotesUrl;
    private boolean major = true;
    private Sdkman.Command command;

    void setAll(SdkmanAnnouncer sdkman) {
        super.setAll(sdkman);
        this.consumerKey = sdkman.consumerKey;
        this.consumerToken = sdkman.consumerToken;
        this.candidate = sdkman.candidate;
        this.releaseNotesUrl = sdkman.releaseNotesUrl;
        this.major = sdkman.major;
        this.command = sdkman.command;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerToken() {
        return consumerToken;
    }

    public void setConsumerToken(String consumerToken) {
        this.consumerToken = consumerToken;
    }

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }

    public String getReleaseNotesUrl() {
        return releaseNotesUrl;
    }

    public void setReleaseNotesUrl(String releaseNotesUrl) {
        this.releaseNotesUrl = releaseNotesUrl;
    }

    public boolean isMajor() {
        return major;
    }

    public void setMajor(boolean major) {
        this.major = major;
    }

    public Sdkman.Command getCommand() {
        return command;
    }

    public void setCommand(Sdkman.Command command) {
        this.command = command;
    }

    public void setCommand(String str) {
        this.command = Sdkman.Command.of(str);
    }

    public String resolveCommand() {
        return command != null ? command.name() : null;
    }

    public boolean isCommandSet() {
        return command != null;
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            isNotBlank(consumerKey) ||
            isNotBlank(consumerToken) ||
            isNotBlank(candidate) ||
            isNotBlank(releaseNotesUrl) ||
            null != command;
    }
}
