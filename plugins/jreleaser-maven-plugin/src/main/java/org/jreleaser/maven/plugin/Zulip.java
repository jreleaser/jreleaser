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
package org.jreleaser.maven.plugin;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Zulip extends AbstractAnnouncer {
    private String account;
    private String apiKey;
    private String apiHost;
    private String channel;
    private String subject;
    private String message;
    private String messageTemplate;

    void setAll(Zulip zulip) {
        super.setAll(zulip);
        this.account = zulip.account;
        this.apiKey = zulip.apiKey;
        this.apiHost = zulip.apiHost;
        this.channel = zulip.channel;
        this.subject = zulip.subject;
        this.message = zulip.message;
        this.messageTemplate = zulip.messageTemplate;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiHost() {
        return apiHost;
    }

    public void setApiHost(String apiHost) {
        this.apiHost = apiHost;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            isNotBlank(account) ||
            isNotBlank(apiHost) ||
            isNotBlank(apiKey) ||
            isNotBlank(channel) ||
            isNotBlank(subject) ||
            isNotBlank(message) ||
            isNotBlank(messageTemplate);
    }
}
