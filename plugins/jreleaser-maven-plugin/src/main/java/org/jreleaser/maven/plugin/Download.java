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
package org.jreleaser.maven.plugin;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public class Download implements Activatable {
    private final Map<String, FtpDownloader> ftp = new LinkedHashMap<>();
    private final Map<String, HttpDownloader> http = new LinkedHashMap<>();
    private final Map<String, ScpDownloader> scp = new LinkedHashMap<>();
    private final Map<String, SftpDownloader> sftp = new LinkedHashMap<>();
    private Active active;

    void setAll(Download download) {
        this.active = download.active;
        setFtp(download.ftp);
        setHttp(download.http);
        setScp(download.scp);
        setSftp(download.sftp);
    }

    @Override
    public Active getActive() {
        return active;
    }

    @Override
    public void setActive(Active active) {
        this.active = active;
    }

    @Override
    public String resolveActive() {
        return active != null ? active.name() : null;
    }

    public Map<String, FtpDownloader> getFtp() {
        return ftp;
    }

    public void setFtp(Map<String, FtpDownloader> ftp) {
        this.ftp.clear();
        this.ftp.putAll(ftp);
    }

    public Map<String, HttpDownloader> getHttp() {
        return http;
    }

    public void setHttp(Map<String, HttpDownloader> http) {
        this.http.clear();
        this.http.putAll(http);
    }

    public Map<String, ScpDownloader> getScp() {
        return scp;
    }

    public void setScp(Map<String, ScpDownloader> scp) {
        this.scp.clear();
        this.scp.putAll(scp);
    }

    public Map<String, SftpDownloader> getSftp() {
        return sftp;
    }

    public void setSftp(Map<String, SftpDownloader> sftp) {
        this.sftp.clear();
        this.sftp.putAll(sftp);
    }
}
