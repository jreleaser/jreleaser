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

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
public class Webhook extends AbstractAnnouncer {
    private String name;
    private String webhook;
    private String message;
    private String messageProperty;
    private String messageTemplate;

    void setAll(Webhook webhook) {
        super.setAll(webhook);
        this.name = webhook.name;
        this.webhook = webhook.webhook;
        this.message = webhook.message;
        this.messageProperty = webhook.messageProperty;
        this.messageTemplate = webhook.messageTemplate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageProperty() {
        return messageProperty;
    }

    public void setMessageProperty(String messageProperty) {
        this.messageProperty = messageProperty;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }
}
