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
package org.jreleaser.model.internal.download;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.Domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class Download extends AbstractActivatable<Download> implements Domain {
    private static final long serialVersionUID = -167665748357801922L;

    private final Map<String, FtpDownloader> ftp = new LinkedHashMap<>();
    private final Map<String, HttpDownloader> http = new LinkedHashMap<>();
    private final Map<String, ScpDownloader> scp = new LinkedHashMap<>();
    private final Map<String, SftpDownloader> sftp = new LinkedHashMap<>();

    @JsonIgnore
    private final org.jreleaser.model.api.download.Download immutable = new org.jreleaser.model.api.download.Download() {
        private static final long serialVersionUID = -6843721083893842034L;

        private Map<String, ? extends org.jreleaser.model.api.download.FtpDownloader> ftp;
        private Map<String, ? extends org.jreleaser.model.api.download.HttpDownloader> http;
        private Map<String, ? extends org.jreleaser.model.api.download.ScpDownloader> scp;
        private Map<String, ? extends org.jreleaser.model.api.download.SftpDownloader> sftp;

        @Override
        public Map<String, ? extends org.jreleaser.model.api.download.FtpDownloader> getFtp() {
            if (null == ftp) {
                ftp = Download.this.ftp.values().stream()
                    .map(FtpDownloader::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.download.Downloader::getName, identity()));
            }
            return ftp;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.download.HttpDownloader> getHttp() {
            if (null == http) {
                http = Download.this.http.values().stream()
                    .map(HttpDownloader::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.download.Downloader::getName, identity()));
            }
            return http;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.download.ScpDownloader> getScp() {
            if (null == scp) {
                scp = Download.this.scp.values().stream()
                    .map(ScpDownloader::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.download.Downloader::getName, identity()));
            }
            return scp;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.download.SftpDownloader> getSftp() {
            if (null == sftp) {
                sftp = Download.this.sftp.values().stream()
                    .map(SftpDownloader::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.download.Downloader::getName, identity()));
            }
            return sftp;
        }

        @Override
        public Active getActive() {
            return Download.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return Download.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Download.this.asMap(full));
        }
    };

    public Download() {
        enabledSet(true);
    }

    public org.jreleaser.model.api.download.Download asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Download source) {
        super.merge(source);
        setFtp(mergeModel(this.ftp, source.ftp));
        setHttp(mergeModel(this.http, source.http));
        setScp(mergeModel(this.scp, source.scp));
        setSftp(mergeModel(this.sftp, source.sftp));
    }

    public List<FtpDownloader> getActiveFtps() {
        return ftp.values().stream()
            .filter(FtpDownloader::isEnabled)
            .collect(toList());
    }

    public Map<String, FtpDownloader> getFtp() {
        return ftp;
    }

    public void setFtp(Map<String, FtpDownloader> ftp) {
        this.ftp.clear();
        this.ftp.putAll(ftp);
    }

    public void addFtp(FtpDownloader ftp) {
        this.ftp.put(ftp.getName(), ftp);
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
        map.put("active", getActive());

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

    public <A extends Downloader<?>> Map<String, A> findDownloadersByType(String downloaderType) {
        switch (downloaderType) {
            case org.jreleaser.model.api.download.FtpDownloader.TYPE:
                return (Map<String, A>) ftp;
            case org.jreleaser.model.api.download.HttpDownloader.TYPE:
                return (Map<String, A>) http;
            case org.jreleaser.model.api.download.ScpDownloader.TYPE:
                return (Map<String, A>) scp;
            case org.jreleaser.model.api.download.SftpDownloader.TYPE:
                return (Map<String, A>) sftp;
            default:
                return Collections.emptyMap();
        }
    }

    public <A extends Downloader<?>> List<A> findAllActiveDownloaders() {
        List<A> downloaders = new ArrayList<>();
        downloaders.addAll((List<A>) getActiveFtps());
        downloaders.addAll((List<A>) getActiveHttps());
        downloaders.addAll((List<A>) getActiveScps());
        downloaders.addAll((List<A>) getActiveSftps());
        return downloaders;
    }
}
