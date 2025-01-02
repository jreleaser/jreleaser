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
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.mustache.TemplateContext;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.announce.ZulipAnnouncer.TYPE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class ZulipAnnouncer extends AbstractMessageAnnouncer<ZulipAnnouncer, org.jreleaser.model.api.announce.ZulipAnnouncer> {
    private static final long serialVersionUID = -8185095877157331540L;

    private String account;
    private String apiKey;
    private String apiHost;
    private String channel;
    private String subject;

    @JsonIgnore
    private final org.jreleaser.model.api.announce.ZulipAnnouncer immutable = new org.jreleaser.model.api.announce.ZulipAnnouncer() {
        private static final long serialVersionUID = -2240453843686094465L;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.ZulipAnnouncer.TYPE;
        }

        @Override
        public String getAccount() {
            return account;
        }

        @Override
        public String getApiKey() {
            return apiKey;
        }

        @Override
        public String getApiHost() {
            return apiHost;
        }

        @Override
        public String getChannel() {
            return channel;
        }

        @Override
        public String getSubject() {
            return subject;
        }

        @Override
        public String getMessage() {
            return ZulipAnnouncer.this.getMessage();
        }

        @Override
        public String getMessageTemplate() {
            return ZulipAnnouncer.this.getMessageTemplate();
        }

        @Override
        public String getName() {
            return ZulipAnnouncer.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return ZulipAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return ZulipAnnouncer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return ZulipAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(ZulipAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return ZulipAnnouncer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(ZulipAnnouncer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return ZulipAnnouncer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return ZulipAnnouncer.this.getReadTimeout();
        }
    };

    public ZulipAnnouncer() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.ZulipAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(ZulipAnnouncer source) {
        super.merge(source);
        this.account = merge(this.account, source.account);
        this.apiKey = merge(this.apiKey, source.apiKey);
        this.apiHost = merge(this.apiHost, source.apiHost);
        this.channel = merge(this.channel, source.channel);
        this.subject = merge(this.subject, source.subject);
    }

    public String getResolvedSubject(JReleaserContext context) {
        TemplateContext props = context.fullProps();
        applyTemplates(props, resolvedExtraProperties());
        return resolveTemplate(subject, props);
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

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("account", account);
        props.put("apiKey", isNotBlank(apiKey) ? HIDE : UNSET);
        props.put("apiHost", apiHost);
        props.put("channel", channel);
        props.put("subject", subject);
        super.asMap(full, props);
    }
}
