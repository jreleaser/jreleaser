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
    private final Map<String, ScpDownloader> scp = new LinkedHashMap<>();
    private final Map<String, SftpDownloader> sftp = new LinkedHashMap<>();
    private Boolean enabled;

    void setAll(Download download) {
        this.enabled = download.enabled;
        setHttp(download.http);
        setScp(download.scp);
        setSftp(download.sftp);
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
        this.http.put(http.getName(), http);
    }

    public List<ScpDownloader> getActiveScps() {
        return scp.values().stream()
            .filter(ScpDownloader::isEnabled)
            .collect(toList());
    }

    public Map<String, ScpDownloader> getScp() {
        return scp;
    }

    public void setScp(Map<String, ScpDownloader> scp) {
        this.scp.clear();
        this.scp.putAll(scp);
    }

    public void addScp(ScpDownloader scp) {
        this.scp.put(scp.getName(), scp);
    }

    public List<SftpDownloader> getActiveSftps() {
        return sftp.values().stream()
            .filter(SftpDownloader::isEnabled)
            .collect(toList());
    }

    public Map<String, SftpDownloader> getSftp() {
        return sftp;
    }

    public void setSftp(Map<String, SftpDownloader> sftp) {
        this.sftp.clear();
        this.sftp.putAll(sftp);
    }

    public void addSftp(SftpDownloader sftp) {
        this.sftp.put(sftp.getName(), sftp);
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

        List<Map<String, Object>> scp = this.scp.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!scp.isEmpty()) map.put("scp", scp);

        List<Map<String, Object>> sftp = this.sftp.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!sftp.isEmpty()) map.put("sftp", sftp);

        return map;
    }

    public <A extends Downloader> Map<String, A> findDownloadersByType(String downloaderType) {
        switch (downloaderType) {
            case HttpDownloader.TYPE:
                return (Map<String, A>) http;
            case ScpDownloader.TYPE:
                return (Map<String, A>) scp;
            case SftpDownloader.TYPE:
                return (Map<String, A>) sftp;
        }

        return Collections.emptyMap();
    }

    public <A extends Downloader> List<A> findAllActiveDownloaders() {
        List<A> downloaders = new ArrayList<>();
        downloaders.addAll((List<A>) getActiveHttps());
        downloaders.addAll((List<A>) getActiveScps());
        downloaders.addAll((List<A>) getActiveSftps());
        return downloaders;
    }

    public static Set<String> supportedDownloaders() {
        Set<String> set = new LinkedHashSet<>();
        set.add(HttpDownloader.TYPE);
        set.add(ScpDownloader.TYPE);
        set.add(SftpDownloader.TYPE);
        return Collections.unmodifiableSet(set);
    }
}
