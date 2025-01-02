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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.jreleaser.model.api.announce.WebhooksAnnouncer.TYPE;

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
public final class WebhooksAnnouncer extends AbstractAnnouncer<WebhooksAnnouncer, org.jreleaser.model.api.announce.WebhooksAnnouncer> {
    private static final long serialVersionUID = 6152385950214311240L;

    private final Map<String, WebhookAnnouncer> webhooks = new LinkedHashMap<>();

    @JsonIgnore
    private final org.jreleaser.model.api.announce.WebhooksAnnouncer immutable = new org.jreleaser.model.api.announce.WebhooksAnnouncer() {
        private static final long serialVersionUID = -8196857821339657945L;

        private Map<String, ? extends org.jreleaser.model.api.announce.WebhookAnnouncer> webhooks;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.WebhooksAnnouncer.TYPE;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.announce.WebhookAnnouncer> getWebhooks() {
            if (null == webhooks) {
                webhooks = WebhooksAnnouncer.this.webhooks.values().stream()
                    .map(WebhookAnnouncer::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.announce.WebhookAnnouncer::getName, identity()));
            }
            return webhooks;
        }

        @Override
        public String getName() {
            return WebhooksAnnouncer.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return WebhooksAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return WebhooksAnnouncer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return WebhooksAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(WebhooksAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return WebhooksAnnouncer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(WebhooksAnnouncer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return WebhooksAnnouncer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return WebhooksAnnouncer.this.getReadTimeout();
        }
    };

    public WebhooksAnnouncer() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.WebhooksAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(WebhooksAnnouncer source) {
        super.merge(source);
        setWebhooks(mergeModel(this.webhooks, source.webhooks));
    }

    public Map<String, WebhookAnnouncer> getWebhooks() {
        return webhooks;
    }

    public void setWebhooks(Map<String, WebhookAnnouncer> webhooks) {
        this.webhooks.clear();
        this.webhooks.putAll(webhooks);
    }

    public void addWebhook(WebhookAnnouncer webhook) {
        this.webhooks.put(webhook.getName(), webhook);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        asMap(full, props);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(getName(), props);
        return map;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        this.webhooks.values()
            .stream()
            .filter(w -> full || w.isEnabled())
            .map(d -> d.asMap(full))
            .forEach(props::putAll);
    }
}
