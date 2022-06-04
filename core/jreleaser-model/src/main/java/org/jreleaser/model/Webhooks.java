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
package org.jreleaser.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
public class Webhooks extends AbstractAnnouncer<Webhooks> {
    public static final String NAME = "webhooks";
    private final Map<String, Webhook> webhooks = new LinkedHashMap<>();

    public Webhooks() {
        super(NAME);
    }

    @Override
    public void freeze() {
        super.freeze();
        webhooks.values().forEach(Webhook::freeze);
    }

    @Override
    public void merge(Webhooks webhook) {
        freezeCheck();
        super.merge(webhook);
        setWebhooks(mergeModel(this.webhooks, webhook.webhooks));
    }

    public List<Webhook> getActiveWebhooks() {
        return webhooks.values().stream()
            .filter(Webhook::isEnabled)
            .collect(Collectors.toList());
    }

    public Map<String, Webhook> getWebhooks() {
        return freezeWrap(webhooks);
    }

    public void setWebhooks(Map<String, Webhook> webhooks) {
        freezeCheck();
        this.webhooks.clear();
        this.webhooks.putAll(webhooks);
    }

    public void addWebhook(Webhook webhook) {
        freezeCheck();
        this.webhooks.put(webhook.getName(), webhook);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        asMap(props, full);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(getName(), props);
        return map;
    }

    @Override
    protected void asMap(Map<String, Object> props, boolean full) {
        this.webhooks.values()
            .stream()
            .filter(Webhook::isEnabled)
            .map(d -> d.asMap(full))
            .forEach(props::putAll);
    }
}
