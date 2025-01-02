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
import static org.jreleaser.model.api.announce.SlackAnnouncer.TYPE;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class SlackAnnouncer extends AbstractMessageAnnouncer<SlackAnnouncer, org.jreleaser.model.api.announce.SlackAnnouncer> {
    private static final long serialVersionUID = 588094747076475409L;

    private String token;
    private String webhook;
    private String channel;

    @JsonIgnore
    private final org.jreleaser.model.api.announce.SlackAnnouncer immutable = new org.jreleaser.model.api.announce.SlackAnnouncer() {
        private static final long serialVersionUID = -6078751771948977999L;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.SlackAnnouncer.TYPE;
        }

        @Override
        public String getToken() {
            return token;
        }

        @Override
        public String getWebhook() {
            return webhook;
        }

        @Override
        public String getChannel() {
            return channel;
        }

        @Override
        public String getMessage() {
            return SlackAnnouncer.this.getMessage();
        }

        @Override
        public String getMessageTemplate() {
            return SlackAnnouncer.this.getMessageTemplate();
        }

        @Override
        public String getName() {
            return SlackAnnouncer.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return SlackAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return SlackAnnouncer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return SlackAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(SlackAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return SlackAnnouncer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(SlackAnnouncer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return SlackAnnouncer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return SlackAnnouncer.this.getReadTimeout();
        }
    };

    public SlackAnnouncer() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.SlackAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(SlackAnnouncer source) {
        super.merge(source);
        this.token = merge(this.token, source.token);
        this.channel = merge(this.channel, source.channel);
        this.webhook = merge(this.webhook, source.webhook);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("webhook", isNotBlank(webhook) ? HIDE : UNSET);
        props.put("token", isNotBlank(token) ? HIDE : UNSET);
        props.put("channel", channel);
        super.asMap(full, props);
    }
}
