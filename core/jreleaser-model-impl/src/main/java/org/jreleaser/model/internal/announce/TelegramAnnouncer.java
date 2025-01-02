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
import static org.jreleaser.model.api.announce.TelegramAnnouncer.TYPE;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public final class TelegramAnnouncer extends AbstractMessageAnnouncer<TelegramAnnouncer, org.jreleaser.model.api.announce.TelegramAnnouncer> {
    private static final long serialVersionUID = -2583758348750365323L;

    private String token;
    private String chatId;

    @JsonIgnore
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
            return TelegramAnnouncer.this.getMessage();
        }

        @Override
        public String getMessageTemplate() {
            return TelegramAnnouncer.this.getMessageTemplate();
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
            return TelegramAnnouncer.this.prefix();
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

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("token", isNotBlank(token) ? HIDE : UNSET);
        props.put("chatId", isNotBlank(chatId) ? HIDE : UNSET);
        super.asMap(full, props);
    }
}
