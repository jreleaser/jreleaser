/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class Upload implements Domain, EnabledAware {
    private final Map<String, Artifactory> artifactory = new LinkedHashMap<>();
    private final Map<String, HttpUploader> http = new LinkedHashMap<>();
    private Boolean enabled;

    void setAll(Upload assemble) {
        this.enabled = assemble.enabled;
        setArtifactory(assemble.artifactory);
        setHttp(assemble.http);
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

    public List<Artifactory> getActiveArtifactories() {
        return artifactory.values().stream()
            .filter(Artifactory::isEnabled)
            .collect(Collectors.toList());
    }

    public Map<String, Artifactory> getArtifactory() {
        return artifactory;
    }

    public void setArtifactory(Map<String, Artifactory> artifactory) {
        this.artifactory.clear();
        this.artifactory.putAll(artifactory);
    }

    public void addArtifactory(Artifactory artifactory) {
        this.artifactory.put(artifactory.getType(), artifactory);
    }

    public Artifactory findArtifactory(String name) {
        if (isBlank(name)) {
            throw new JReleaserException("Artifactory name must not be blank");
        }

        if (artifactory.containsKey(name)) {
            return artifactory.get(name);
        }

        throw new JReleaserException("Artifactory '" + name + "' not found");
    }

    public List<HttpUploader> getActiveHttps() {
        return http.values().stream()
            .filter(HttpUploader::isEnabled)
            .collect(Collectors.toList());
    }

    public Map<String, HttpUploader> getHttp() {
        return http;
    }

    public void setHttp(Map<String, HttpUploader> http) {
        this.http.clear();
        this.http.putAll(http);
    }

    public void addHttp(HttpUploader http) {
        this.http.put(http.getType(), http);
    }

    public HttpUploader findHttp(String name) {
        if (isBlank(name)) {
            throw new JReleaserException("HttpUploader name must not be blank");
        }

        if (http.containsKey(name)) {
            return http.get(name);
        }

        throw new JReleaserException("HttpUploader '" + name + "' not found");
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());

        List<Map<String, Object>> artifactory = this.artifactory.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(Collectors.toList());
        if (!artifactory.isEmpty()) map.put("artifactory", artifactory);

        List<Map<String, Object>> http = this.http.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(Collectors.toList());
        if (!http.isEmpty()) map.put("http", http);

        return map;
    }

    public <A extends Uploader> Map<String, A> findUploadersByType(String uploaderName) {
        switch (uploaderName) {
            case Artifactory.NAME:
                return (Map<String, A>) artifactory;
                case HttpUploader.NAME:
                return (Map<String, A>) http;
        }

        return Collections.emptyMap();
    }

    public <A extends Uploader> Collection<A> findAllUploaders() {
        List<A> uploaders = new ArrayList<>();
        uploaders.addAll((List<A>) getActiveArtifactories());
        uploaders.addAll((List<A>) getActiveHttps());
        return uploaders;
    }
}
