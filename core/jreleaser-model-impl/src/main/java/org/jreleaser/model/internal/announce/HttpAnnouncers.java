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
import static org.jreleaser.model.api.announce.HttpAnnouncers.TYPE;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class HttpAnnouncers extends AbstractAnnouncer<HttpAnnouncers, org.jreleaser.model.api.announce.HttpAnnouncers> {
    private static final long serialVersionUID = 5295187663038593069L;

    private final Map<String, HttpAnnouncer> http = new LinkedHashMap<>();

    @JsonIgnore
    private final org.jreleaser.model.api.announce.HttpAnnouncers immutable = new org.jreleaser.model.api.announce.HttpAnnouncers() {
        private static final long serialVersionUID = 4604741967520778012L;

        private Map<String, ? extends org.jreleaser.model.api.announce.HttpAnnouncer> http;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.HttpAnnouncers.TYPE;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.announce.HttpAnnouncer> getHttpAnnouncers() {
            if (null == http) {
                http = HttpAnnouncers.this.http.values().stream()
                    .map(HttpAnnouncer::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.announce.HttpAnnouncer::getName, identity()));
            }
            return http;
        }

        @Override
        public String getName() {
            return HttpAnnouncers.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return HttpAnnouncers.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return HttpAnnouncers.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return HttpAnnouncers.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(HttpAnnouncers.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return HttpAnnouncers.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(HttpAnnouncers.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return HttpAnnouncers.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return HttpAnnouncers.this.getReadTimeout();
        }
    };

    public HttpAnnouncers() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.HttpAnnouncers asImmutable() {
        return immutable;
    }

    @Override
    public void merge(HttpAnnouncers source) {
        super.merge(source);
        setHttp(mergeModel(this.http, source.http));
    }

    public Map<String, HttpAnnouncer> getHttp() {
        return http;
    }

    public void setHttp(Map<String, HttpAnnouncer> http) {
        this.http.clear();
        this.http.putAll(http);
    }

    public void addHttpAnnouncer(HttpAnnouncer http) {
        this.http.put(http.getName(), http);
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
        this.http.values()
            .stream()
            .filter(h -> full || h.isEnabled())
            .map(d -> d.asMap(full))
            .forEach(props::putAll);
    }
}
