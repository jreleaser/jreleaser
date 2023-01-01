/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
import static org.jreleaser.model.api.announce.TelegramAnnouncer.TYPE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public final class TelegramAnnouncer extends AbstractAnnouncer<TelegramAnnouncer, org.jreleaser.model.api.announce.TelegramAnnouncer> {
    private static final long serialVersionUID = -7942186218415757885L;

    private String token;
    private String chatId;
    private String message;
    private String messageTemplate;

    private final org.jreleaser.model.api.announce.TelegramAnnouncer immutable = new org.jreleaser.model.api.announce.TelegramAnnouncer() {
        private static final long serialVersionUID = -5918930180588439497L;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.TelegramAnnouncer.TYPE;
        }

        @Override
        public String getToken() {
            return token;
        }

        @Override
        public String getChatId() {
            return chatId;
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
            return TelegramAnnouncer.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return TelegramAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return TelegramAnnouncer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return TelegramAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(TelegramAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return TelegramAnnouncer.this.getPrefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(TelegramAnnouncer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return TelegramAnnouncer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return TelegramAnnouncer.this.getReadTimeout();
        }
    };

    public TelegramAnnouncer() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.TelegramAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(TelegramAnnouncer source) {
        super.merge(source);
        this.token = merge(this.token, source.token);
        this.chatId = merge(this.chatId, source.chatId);
        this.message = merge(this.message, source.message);
        this.messageTemplate = merge(this.messageTemplate, source.messageTemplate);
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
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
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("token", isNotBlank(token) ? HIDE : UNSET);
        props.put("chatId", isNotBlank(chatId) ? HIDE : UNSET);
        props.put("message", message);
        props.put("messageTemplate", messageTemplate);
    }
}
