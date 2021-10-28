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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jreleaser.util.StringUtils.capitalize;
import static org.jreleaser.util.StringUtils.getClassNameForLowerCaseHyphenSeparatedName;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class Upload implements Domain, EnabledAware {
    private final Map<String, Artifactory> artifactory = new LinkedHashMap<>();
    private final Map<String, Http> http = new LinkedHashMap<>();
    private final Map<String, S3> s3 = new LinkedHashMap<>();
    private Boolean enabled;

    void setAll(Upload assemble) {
        this.enabled = assemble.enabled;
        setArtifactory(assemble.artifactory);
        setHttp(assemble.http);
        setS3(assemble.s3);
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

    public List<Http> getActiveHttps() {
        return http.values().stream()
            .filter(Http::isEnabled)
            .collect(Collectors.toList());
    }

    public Map<String, Http> getHttp() {
        return http;
    }

    public void setHttp(Map<String, Http> http) {
        this.http.clear();
        this.http.putAll(http);
    }

    public void addHttp(Http http) {
        this.http.put(http.getType(), http);
    }

    public List<S3> getActiveS3s() {
        return s3.values().stream()
            .filter(S3::isEnabled)
            .collect(Collectors.toList());
    }

    public Map<String, S3> getS3() {
        return s3;
    }

    public void setS3(Map<String, S3> s3) {
        this.s3.clear();
        this.s3.putAll(s3);
    }

    public void addS3(S3 s3) {
        this.s3.put(s3.getType(), s3);
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

        List<Map<String, Object>> s3 = this.s3.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(Collectors.toList());
        if (!s3.isEmpty()) map.put("s3", s3);

        return map;
    }

    public <A extends Uploader> Map<String, A> findUploadersByType(String uploaderType) {
        switch (uploaderType) {
            case Artifactory.TYPE:
                return (Map<String, A>) artifactory;
            case Http.TYPE:
                return (Map<String, A>) http;
            case S3.TYPE:
                return (Map<String, A>) s3;
        }

        return Collections.emptyMap();
    }

    public <A extends Uploader> List<A> findAllUploaders() {
        List<A> uploaders = new ArrayList<>();
        uploaders.addAll((List<A>) getActiveArtifactories());
        uploaders.addAll((List<A>) getActiveHttps());
        uploaders.addAll((List<A>) getActiveS3s());
        return uploaders;
    }

    public Map<String, String> resolveDownloadUrls(JReleaserContext context, Distribution distribution, Artifact artifact, String prefix) {
        Map<String, String> urls = new LinkedHashMap<>();

        List<Uploader> uploaders = findAllUploaders();
        for (Uploader uploader : uploaders) {
            List<String> keys = uploader.resolveSkipKeys();
            if (isSkip(distribution, keys) ||
                isSkip(artifact, keys)) continue;
            String key = prefix +
                "Download" +
                capitalize(uploader.getType()) +
                getClassNameForLowerCaseHyphenSeparatedName(uploader.getName()) +
                "Url";
            String url = uploader.getResolvedDownloadUrl(context, artifact);
            urls.put(key, url);

            if (findUploadersByType(uploader.getType()).size() == 1 && !isSkip(distribution, keys) &&
                !isSkip(artifact, keys)) {
                key = prefix +
                    "Download" +
                    capitalize(uploader.getType()) +
                    "Url";
                url = uploader.getResolvedDownloadUrl(context, artifact);
                urls.put(key, url);
            }
        }

        if (uploaders.size() == 1) {
            Uploader uploader = uploaders.get(0);
            List<String> keys = uploader.resolveSkipKeys();
            if (!isSkip(distribution, keys) &&
                !isSkip(artifact, keys)) {
                String key = prefix + "DownloadUrl";
                String url = uploader.getResolvedDownloadUrl(context, artifact);
                urls.put(key, url);
            }
        }

        return urls;
    }

    private boolean isSkip(ExtraProperties props, List<String> keys) {
        for (String key : keys) {
            if (props.extraPropertyIsTrue(key)) {
                return true;
            }
        }
        return false;
    }
}
