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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public class Download implements Domain, EnabledAware {
    private final Map<String, HttpDownloader> http = new LinkedHashMap<>();
    private Boolean enabled;

    void setAll(Download download) {
        this.enabled = download.enabled;
        setHttp(download.http);
    }

    @Override
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabledSet() {
        return enabled != null;
    }

    public List<HttpDownloader> getActiveHttps() {
        return http.values().stream()
            .filter(HttpDownloader::isEnabled)
            .collect(toList());
    }

    public Map<String, HttpDownloader> getHttp() {
        return http;
    }

    public void setHttp(Map<String, HttpDownloader> http) {
        this.http.clear();
        this.http.putAll(http);
    }

    public void addHttp(HttpDownloader http) {
        this.http.put(http.getName(),http);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());

        List<Map<String, Object>> http = this.http.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!http.isEmpty()) map.put("http", http);

        return map;
    }

    public <A extends Downloader> Map<String, A> findDownloadersByType(String uploaderType) {
        switch (uploaderType) {
            case HttpUploader.TYPE:
                return (Map<String, A>) http;
        }

        return Collections.emptyMap();
    }

    public <A extends Downloader> List<A> findAllActiveDownloaders() {
        List<A> uploaders = new ArrayList<>();
        uploaders.addAll((List<A>) getActiveHttps());
        return uploaders;
    }

    public static Set<String> supportedDownloaders() {
        Set<String> set = new LinkedHashSet<>();
        set.add(HttpUploader.TYPE);
        return Collections.unmodifiableSet(set);
    }
}
