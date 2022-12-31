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
package org.jreleaser.model.internal.announce;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.KEY_TAG_NAME;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
public final class WebhookAnnouncer extends AbstractAnnouncer<WebhookAnnouncer, org.jreleaser.model.api.announce.WebhookAnnouncer> {
    private static final long serialVersionUID = 3768821724964181104L;

    private String webhook;
    private String message;
    private String messageProperty;
    private String messageTemplate;
    private Boolean structuredMessage;

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
            return message;
        }

        @Override
        public String getMessageProperty() {
            return messageProperty;
        }

        @Override
        public String getMessageTemplate() {
            return messageTemplate;
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
            return WebhookAnnouncer.this.getPrefix();
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
        this.message = merge(this.message, source.message);
        this.messageTemplate = merge(this.messageTemplate, source.messageTemplate);
        this.messageProperty = merge(this.messageProperty, source.messageProperty);
        this.structuredMessage = merge(this.structuredMessage, source.structuredMessage);
    }

    public String getResolvedMessage(JReleaserContext context) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        return resolveTemplate(message, props);
    }

    public String getResolvedMessageTemplate(JReleaserContext context, Map<String, Object> extraProps) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        props.put(KEY_TAG_NAME, context.getModel().getRelease().getReleaser()
            .getEffectiveTagName(context.getModel()));
        props.putAll(extraProps);

        Path templatePath = context.getBasedir().resolve(messageTemplate);
        try {
            Reader reader = java.nio.file.Files.newBufferedReader(templatePath);
            return applyTemplate(reader, props);
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_reading_template",
                context.relativizeToBasedir(templatePath)));
        }
    }

    @Override
    public String getPrefix() {
        return "webhook";
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

    public boolean isStructuredMessage() {
        return structuredMessage != null && structuredMessage;
    }

    public void setStructuredMessage(Boolean structuredMessage) {
        this.structuredMessage = structuredMessage;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("webhook", isNotBlank(webhook) ? HIDE : UNSET);
        props.put("message", message);
        props.put("messageProperty", messageProperty);
        props.put("messageTemplate", messageTemplate);
        props.put("structuredMessage", isStructuredMessage());
    }
}
