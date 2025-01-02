/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
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
package org.jreleaser.model.internal.announce;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
public final class WebhookAnnouncer extends AbstractMessageAnnouncer<WebhookAnnouncer, org.jreleaser.model.api.announce.WebhookAnnouncer> {
    private static final long serialVersionUID = 771685577904254805L;

    private String webhook;
    private String messageProperty;
    private Boolean structuredMessage;

    @JsonIgnore
    private final org.jreleaser.model.api.announce.WebhookAnnouncer immutable = new org.jreleaser.model.api.announce.WebhookAnnouncer() {
        private static final long serialVersionUID = 6579288631060633630L;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.WebhooksAnnouncer.TYPE;
        }

        @Override
        public String getWebhook() {
            return webhook;
        }

        @Override
        public String getMessage() {
            return WebhookAnnouncer.this.getMessage();
        }

        @Override
        public String getMessageProperty() {
            return messageProperty;
        }

        @Override
        public String getMessageTemplate() {
            return WebhookAnnouncer.this.getMessageTemplate();
        }

        @Override
        public boolean isStructuredMessage() {
            return WebhookAnnouncer.this.isStructuredMessage();
        }

        @Override
        public String getName() {
            return WebhookAnnouncer.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return WebhookAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return WebhookAnnouncer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return WebhookAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(WebhookAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return WebhookAnnouncer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(WebhookAnnouncer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return WebhookAnnouncer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return WebhookAnnouncer.this.getReadTimeout();
        }
    };

    public WebhookAnnouncer() {
        super("");
    }

    @Override
    public org.jreleaser.model.api.announce.WebhookAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(WebhookAnnouncer source) {
        super.merge(source);
        setName(merge(this.getName(), source.getName()));
        this.webhook = merge(this.webhook, source.webhook);
        this.messageProperty = merge(this.messageProperty, source.messageProperty);
        this.structuredMessage = merge(this.structuredMessage, source.structuredMessage);
    }

    @Override
    public String prefix() {
        return "webhook";
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public String getMessageProperty() {
        return messageProperty;
    }

    public void setMessageProperty(String messageProperty) {
        this.messageProperty = messageProperty;
    }

    public boolean isStructuredMessage() {
        return null != structuredMessage && structuredMessage;
    }

    public void setStructuredMessage(Boolean structuredMessage) {
        this.structuredMessage = structuredMessage;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("webhook", isNotBlank(webhook) ? HIDE : UNSET);
        super.asMap(full, props);
        props.put("messageProperty", messageProperty);
        props.put("structuredMessage", isStructuredMessage());
    }
}
