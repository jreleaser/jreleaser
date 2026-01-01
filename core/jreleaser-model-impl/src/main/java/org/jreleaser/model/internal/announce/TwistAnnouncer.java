/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.Constants;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.mustache.TemplateContext;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.KEY_TAG_NAME;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.announce.TwistAnnouncer.TYPE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Usman Shaikh
 * @since 1.23.0
 */
public final class TwistAnnouncer extends AbstractAnnouncer<TwistAnnouncer, org.jreleaser.model.api.announce.TwistAnnouncer> {
    private static final long serialVersionUID = 2932524374605971375L;

    private String webhook;
    private String messageTemplate;

    @JsonIgnore
    private final org.jreleaser.model.api.announce.TwistAnnouncer immutable = new org.jreleaser.model.api.announce.TwistAnnouncer() {
        private static final long serialVersionUID = 349478020816684741L;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.TwistAnnouncer.TYPE;
        }

        @Override
        public String getWebhook() {
            return webhook;
        }

        @Override
        public String getMessageTemplate() {
            return messageTemplate;
        }

        @Override
        public String getName() {
            return TwistAnnouncer.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return TwistAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return TwistAnnouncer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return TwistAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(TwistAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return TwistAnnouncer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(TwistAnnouncer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return TwistAnnouncer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return TwistAnnouncer.this.getReadTimeout();
        }
    };

    public TwistAnnouncer() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.TwistAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(TwistAnnouncer source) {
        super.merge(source);
        this.webhook = merge(this.webhook, source.webhook);
        this.messageTemplate = merge(this.messageTemplate, source.messageTemplate);
    }

    @Override
    protected boolean isSet() {
        return super.isSet() ||
            isNotBlank(webhook) ||
            isNotBlank(messageTemplate);
    }

    public String getResolvedMessageTemplate(JReleaserContext context, TemplateContext extraProps) {
        TemplateContext props = context.fullProps();
        applyTemplates(context.getLogger(), props, resolvedExtraProperties());
        props.set(KEY_TAG_NAME, context.getModel().getRelease().getReleaser().getEffectiveTagName(context));
        props.set(Constants.KEY_PREVIOUS_TAG_NAME, context.getModel().getRelease().getReleaser().getResolvedPreviousTagName(context));
        props.setAll(extraProps);

        Path templatePath = context.getBasedir().resolve(messageTemplate);
        try {
            Reader reader = java.nio.file.Files.newBufferedReader(templatePath);
            return applyTemplate(context.getLogger(), reader, props);
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_reading_template",
                context.relativizeToBasedir(templatePath)));
        }
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("webhook", isNotBlank(webhook) ? HIDE : UNSET);
        props.put("messageTemplate", messageTemplate);
    }

    public WebhookAnnouncer asWebhookAnnouncer() {
        WebhookAnnouncer announcer = new WebhookAnnouncer();
        announcer.setName(getName());
        announcer.setWebhook(webhook);
        announcer.setMessageTemplate(messageTemplate);
        announcer.setStructuredMessage(false);
        announcer.setConnectTimeout(getConnectTimeout());
        announcer.setReadTimeout(getReadTimeout());
        announcer.setExtraProperties(getExtraProperties());
        return announcer;
    }
}
