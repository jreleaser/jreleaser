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

import org.jreleaser.util.Env;

import java.util.Map;

import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public class FtpDownloader extends AbstractDownloader<FtpDownloader> implements Ftp {
    public static final String TYPE = "ftp";
    private String username;
    private String password;
    private String host;
    private Integer port;

    public FtpDownloader() {
        super(TYPE);
    }

    @Override
    public void merge(FtpDownloader ftp) {
        freezeCheck();
        super.merge(ftp);
        this.username = merge(this.username, ftp.username);
        this.password = merge(this.password, ftp.password);
        this.host = merge(this.host, ftp.host);
        this.port = merge(this.port, ftp.port);
    }

    @Override
    public String getResolvedUsername() {
        return Env.env("FTP_" + Env.toVar(name) + "_USERNAME", username);
    }

    @Override
    public String getResolvedPassword() {
        return Env.env("FTP_" + Env.toVar(name) + "_PASSWORD", password);
    }

    @Override
    public String getResolvedHost() {
        return Env.env("FTP_" + Env.toVar(name) + "_HOST", host);
    }

    @Override
    public Integer getResolvedPort() {
        String value = Env.env("FTP_" + Env.toVar(name) + "_PORT", null == port ? "" : String.valueOf(port));
        return isBlank(value) ? 21 : Integer.parseInt(value);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        freezeCheck();
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        freezeCheck();
        this.password = password;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        freezeCheck();
        this.host = host;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public void setPort(Integer port) {
        freezeCheck();
        this.port = port;
    }

    @Override
    protected void asMap(Map<String, Object> props, boolean full) {
        props.put("host", getResolvedHost());
        props.put("port", getResolvedPort());
        props.put("username", isNotBlank(getResolvedUsername()) ? HIDE : UNSET);
        props.put("password", isNotBlank(getResolvedPassword()) ? HIDE : UNSET);
    }
}
