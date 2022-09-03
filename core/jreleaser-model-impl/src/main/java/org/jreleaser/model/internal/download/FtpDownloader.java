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
package org.jreleaser.model.internal.download;

import org.jreleaser.model.Active;
import org.jreleaser.model.internal.common.Ftp;
import org.jreleaser.util.Env;

import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.download.FtpDownloader.TYPE;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class FtpDownloader extends AbstractDownloader<FtpDownloader> implements Ftp {
    private String username;
    private String password;
    private String host;
    private Integer port;

    private final org.jreleaser.model.api.download.FtpDownloader immutable = new org.jreleaser.model.api.download.FtpDownloader() {
        private List<? extends org.jreleaser.model.api.download.Downloader.Asset> assets;

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<? extends org.jreleaser.model.api.download.Downloader.Asset> getAssets() {
            if (null == assets) {
                assets = FtpDownloader.this.assets.stream()
                    .map(AbstractDownloader.Asset::asImmutable)
                    .collect(toList());
            }
            return assets;
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return FtpDownloader.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(FtpDownloader.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return FtpDownloader.this.getPrefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public Integer getPort() {
            return port;
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

    public FtpDownloader() {
        super(TYPE);
    }

    public org.jreleaser.model.api.download.FtpDownloader asImmutable() {
        return immutable;
    }

    @Override
    public void merge(FtpDownloader ftp) {
        super.merge(ftp);
        this.username = merge(this.username, ftp.username);
        this.password = merge(this.password, ftp.password);
        this.host = merge(this.host, ftp.host);
        this.port = merge(this.port, ftp.port);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("host", getResolvedHost());
        props.put("port", getResolvedPort());
        props.put("username", isNotBlank(getResolvedUsername()) ? HIDE : UNSET);
        props.put("password", isNotBlank(getResolvedPassword()) ? HIDE : UNSET);
    }

    public String getResolvedUsername() {
        return Env.env("FTP_" + Env.toVar(name) + "_USERNAME", username);
    }

    public String getResolvedPassword() {
        return Env.env("FTP_" + Env.toVar(name) + "_PASSWORD", password);
    }

    public String getResolvedHost() {
        return Env.env("FTP_" + Env.toVar(name) + "_HOST", host);
    }

    public Integer getResolvedPort() {
        String value = Env.env("FTP_" + Env.toVar(name) + "_PORT", null == port ? "" : String.valueOf(port));
        return isBlank(value) ? 21 : Integer.parseInt(value);
    }
}
