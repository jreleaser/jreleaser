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
 * @since 1.3.0
 */
public class HttpAnnouncers extends AbstractAnnouncer<HttpAnnouncers> {
    public static final String NAME = "http";
    private final Map<String, HttpAnnouncer> https = new LinkedHashMap<>();

    public HttpAnnouncers() {
        super(NAME);
    }

    @Override
    public void freeze() {
        super.freeze();
        https.values().forEach(HttpAnnouncer::freeze);
    }

    @Override
    public void merge(HttpAnnouncers http) {
        freezeCheck();
        super.merge(http);
        setHttpAnnouncers(mergeModel(this.https, http.https));
    }

    public List<HttpAnnouncer> getActiveHttpAnnouncers() {
        return https.values().stream()
            .filter(HttpAnnouncer::isEnabled)
            .collect(Collectors.toList());
    }

    public Map<String, HttpAnnouncer> getHttpAnnouncers() {
        return freezeWrap(https);
    }

    public void setHttpAnnouncers(Map<String, HttpAnnouncer> https) {
        freezeCheck();
        this.https.clear();
        this.https.putAll(https);
    }

    public void addHttpAnnouncer(HttpAnnouncer http) {
        freezeCheck();
        this.https.put(http.getName(), http);
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
        this.https.values()
            .stream()
            .filter(HttpAnnouncer::isEnabled)
            .map(d -> d.asMap(full))
            .forEach(props::putAll);
    }
}
