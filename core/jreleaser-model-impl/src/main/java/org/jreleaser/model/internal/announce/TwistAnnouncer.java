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
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.mustache.TemplateContext;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.KEY_PREVIOUS_TAG_NAME;
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
    private static final long serialVersionUID = 2935827447754906932L;

    private String accessToken;
    private String channelId;
    private String threadId;
    private String title;
    private String message;
    private String messageTemplate;

    @JsonIgnore
    private final org.jreleaser.model.api.announce.TwistAnnouncer immutable = new org.jreleaser.model.api.announce.TwistAnnouncer() {
        private static final long serialVersionUID = 3598076247298109854L;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.TwistAnnouncer.TYPE;
        }

        @Override
        public String getAccessToken() {
            return accessToken;
        }

        @Override
        public String getChannelId() {
            return channelId;
        }

        @Override
        public String getThreadId() {
            return threadId;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getMessage() {
            return message;
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
        this.accessToken = merge(this.accessToken, source.accessToken);
        this.channelId = merge(this.channelId, source.channelId);
        this.threadId = merge(this.threadId, source.threadId);
        this.title = merge(this.title, source.title);
        this.message = merge(this.message, source.message);
        this.messageTemplate = merge(this.messageTemplate, source.messageTemplate);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getResolvedTitle(JReleaserContext context) {
        TemplateContext props = context.fullProps();
        applyTemplates(context.getLogger(), props, resolvedExtraProperties());
        return applyTemplate(context.getLogger(), title, props);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResolvedMessage(JReleaserContext context) {
        TemplateContext props = context.fullProps();
        applyTemplates(context.getLogger(), props, resolvedExtraProperties());
        return applyTemplate(context.getLogger(), message, props);
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public String getResolvedMessageTemplate(JReleaserContext context, TemplateContext extraProps) {
        TemplateContext props = context.fullProps();
        context.getChangelog().apply(props);
        applyTemplates(context.getLogger(), props, resolvedExtraProperties());
        props.set(KEY_TAG_NAME, context.getModel().getRelease().getReleaser().getEffectiveTagName(context));
        props.set(KEY_PREVIOUS_TAG_NAME, context.getModel().getRelease().getReleaser().getResolvedPreviousTagName(context));
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

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("accessToken", isNotBlank(getAccessToken()) ? HIDE : UNSET);
        props.put("channelId", channelId);
        props.put("threadId", threadId);
        props.put("title", title);
        props.put("message", message);
        props.put("messageTemplate", messageTemplate);
    }
}
