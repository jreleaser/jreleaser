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

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
public class Sdkman extends AbstractTool {
    private Command command;
    private String candidate;
    private String releaseNotesUrl;
    private String consumerKey;
    private String consumerToken;
    private int connectTimeout;
    private int readTimeout;

    void setAll(Sdkman sdkman) {
        super.setAll(sdkman);
        this.candidate = sdkman.candidate;
        this.releaseNotesUrl = sdkman.releaseNotesUrl;
        this.command = sdkman.command;
        this.consumerKey = sdkman.consumerKey;
        this.consumerToken = sdkman.consumerToken;
        this.connectTimeout = sdkman.connectTimeout;
        this.readTimeout = sdkman.readTimeout;
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

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public void setCommand(String str) {
        this.command = Command.of(str);
    }

    public String resolveCommand() {
        return command != null ? command.name() : null;
    }

    public boolean isCommandSet() {
        return command != null;
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

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            isNotBlank(candidate) ||
            isNotBlank(releaseNotesUrl) ||
            isNotBlank(consumerKey) ||
            isNotBlank(consumerToken) ||
            null != command;
    }

    public enum Command {
        MAJOR,
        MINOR;

        public String toString() {
            return name().toLowerCase();
        }

        public static Command of(String str) {
            if (isBlank(str)) return null;
            return Command.valueOf(str.toUpperCase().trim());
        }
    }
}
