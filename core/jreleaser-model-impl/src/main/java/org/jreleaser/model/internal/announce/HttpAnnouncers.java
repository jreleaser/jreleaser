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

import org.jreleaser.model.Active;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.jreleaser.model.api.announce.HttpAnnouncers.TYPE;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class HttpAnnouncers extends AbstractAnnouncer<HttpAnnouncers> {
    private final Map<String, HttpAnnouncer> httpAnnouncers = new LinkedHashMap<>();

    private final org.jreleaser.model.api.announce.HttpAnnouncers immutable = new org.jreleaser.model.api.announce.HttpAnnouncers() {
        private Map<String, ? extends org.jreleaser.model.api.announce.HttpAnnouncer> httpAnnouncers;

        @Override
        public Map<String, ? extends org.jreleaser.model.api.announce.HttpAnnouncer> getHttpAnnouncers() {
            if (null == httpAnnouncers) {
                httpAnnouncers = HttpAnnouncers.this.httpAnnouncers.values().stream()
                    .map(HttpAnnouncer::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.announce.HttpAnnouncer::getName, identity()));
            }
            return httpAnnouncers;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isSnapshotSupported() {
            return HttpAnnouncers.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return active;
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
            return HttpAnnouncers.this.getPrefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }

        @Override
        public Integer getConnectTimeout() {
            return connectTimeout;
        }

        @Override
        public Integer getReadTimeout() {
            return readTimeout;
        }
    };

    public HttpAnnouncers() {
        super(TYPE);
    }

    public org.jreleaser.model.api.announce.HttpAnnouncers asImmutable() {
        return immutable;
    }

    @Override
    public void merge(HttpAnnouncers source) {
        super.merge(source);
        setHttpAnnouncers(mergeModel(this.httpAnnouncers, source.httpAnnouncers));
    }

    public List<HttpAnnouncer> getActiveHttpAnnouncers() {
        return httpAnnouncers.values().stream()
            .filter(HttpAnnouncer::isEnabled)
            .collect(Collectors.toList());
    }

    public Map<String, HttpAnnouncer> getHttpAnnouncers() {
        return httpAnnouncers;
    }

    public void setHttpAnnouncers(Map<String, HttpAnnouncer> https) {
        this.httpAnnouncers.clear();
        this.httpAnnouncers.putAll(https);
    }

    public void addHttpAnnouncer(HttpAnnouncer http) {
        this.httpAnnouncers.put(http.getName(), http);
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
        this.httpAnnouncers.values()
            .stream()
            .filter(h -> full || h.isEnabled())
            .map(d -> d.asMap(full))
            .forEach(props::putAll);
    }
}
