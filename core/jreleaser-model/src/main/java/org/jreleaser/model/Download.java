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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.util.Env;

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
public class Download extends AbstractModelObject<Download> implements Domain, Activatable {
    private final Map<String, FtpDownloader> ftp = new LinkedHashMap<>();
    private final Map<String, HttpDownloader> http = new LinkedHashMap<>();
    private final Map<String, ScpDownloader> scp = new LinkedHashMap<>();
    private final Map<String, SftpDownloader> sftp = new LinkedHashMap<>();

    private Active active;
    @JsonIgnore
    private boolean enabled = true;

    @Override
    public void freeze() {
        super.freeze();
        ftp.values().forEach(FtpDownloader::freeze);
        http.values().forEach(HttpDownloader::freeze);
        scp.values().forEach(ScpDownloader::freeze);
        sftp.values().forEach(SftpDownloader::freeze);
    }

    @Override
    public void merge(Download download) {
        freezeCheck();
        this.active = merge(this.active, download.active);
        this.enabled = merge(this.enabled, download.enabled);
        setFtp(mergeModel(this.ftp, download.ftp));
        setHttp(mergeModel(this.http, download.http));
        setScp(mergeModel(this.scp, download.scp));
        setSftp(mergeModel(this.sftp, download.sftp));
    }

    @Override
    public boolean isEnabled() {
        return enabled && active != null;
    }

    public void disable() {
        active = Active.NEVER;
        enabled = false;
    }

    public boolean resolveEnabled(Project project) {
        if (null == active) {
            setActive(Env.resolveOrDefault("download.active", "", "ALWAYS"));
        }
        enabled = active.check(project);
        return enabled;
    }

    @Override
    public Active getActive() {
        return active;
    }

    @Override
    public void setActive(Active active) {
        freezeCheck();
        this.active = active;
    }

    @Override
    public void setActive(String str) {
        setActive(Active.of(str));
    }

    @Override
    public boolean isActiveSet() {
        return active != null;
    }

    public List<FtpDownloader> getActiveFtps() {
        return ftp.values().stream()
            .filter(FtpDownloader::isEnabled)
            .collect(toList());
    }

    public Map<String, FtpDownloader> getFtp() {
        return freezeWrap(ftp);
    }

    public void setFtp(Map<String, FtpDownloader> ftp) {
        freezeCheck();
        this.ftp.clear();
        this.ftp.putAll(ftp);
    }

    public void addFtp(FtpDownloader ftp) {
        freezeCheck();
        this.ftp.put(ftp.getName(), ftp);
    }

    public List<HttpDownloader> getActiveHttps() {
        return http.values().stream()
            .filter(HttpDownloader::isEnabled)
            .collect(toList());
    }

    public Map<String, HttpDownloader> getHttp() {
        return freezeWrap(http);
    }

    public void setHttp(Map<String, HttpDownloader> http) {
        freezeCheck();
        this.http.clear();
        this.http.putAll(http);
    }

    public void addHttp(HttpDownloader http) {
        freezeCheck();
        this.http.put(http.getName(), http);
    }

    public List<ScpDownloader> getActiveScps() {
        return scp.values().stream()
            .filter(ScpDownloader::isEnabled)
            .collect(toList());
    }

    public Map<String, ScpDownloader> getScp() {
        return freezeWrap(scp);
    }

    public void setScp(Map<String, ScpDownloader> scp) {
        freezeCheck();
        this.scp.clear();
        this.scp.putAll(scp);
    }

    public void addScp(ScpDownloader scp) {
        freezeCheck();
        this.scp.put(scp.getName(), scp);
    }

    public List<SftpDownloader> getActiveSftps() {
        return sftp.values().stream()
            .filter(SftpDownloader::isEnabled)
            .collect(toList());
    }

    public Map<String, SftpDownloader> getSftp() {
        return freezeWrap(sftp);
    }

    public void setSftp(Map<String, SftpDownloader> sftp) {
        freezeCheck();
        this.sftp.clear();
        this.sftp.putAll(sftp);
    }

    public void addSftp(SftpDownloader sftp) {
        freezeCheck();
        this.sftp.put(sftp.getName(), sftp);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", enabled);
        map.put("active", active);

        List<Map<String, Object>> ftp = this.ftp.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!ftp.isEmpty()) map.put("ftp", ftp);

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
            case FtpDownloader.TYPE:
                return (Map<String, A>) ftp;
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
        downloaders.addAll((List<A>) getActiveFtps());
        downloaders.addAll((List<A>) getActiveHttps());
        downloaders.addAll((List<A>) getActiveScps());
        downloaders.addAll((List<A>) getActiveSftps());
        return downloaders;
    }

    public static Set<String> supportedDownloaders() {
        Set<String> set = new LinkedHashSet<>();
        set.add(FtpDownloader.TYPE);
        set.add(HttpDownloader.TYPE);
        set.add(ScpDownloader.TYPE);
        set.add(SftpDownloader.TYPE);
        return Collections.unmodifiableSet(set);
    }
}
