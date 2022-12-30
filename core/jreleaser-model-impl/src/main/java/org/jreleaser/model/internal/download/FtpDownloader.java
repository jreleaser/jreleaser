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

import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.download.FtpDownloader.TYPE;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class FtpDownloader extends AbstractDownloader<org.jreleaser.model.api.download.FtpDownloader, FtpDownloader> implements Ftp {
    private static final long serialVersionUID = -4601112710782126715L;

    private String username;
    private String password;
    private String host;
    private Integer port;

    private final org.jreleaser.model.api.download.FtpDownloader immutable = new org.jreleaser.model.api.download.FtpDownloader() {
        private static final long serialVersionUID = -3069423247317140050L;

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
                    .map(Downloader.Asset::asImmutable)
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

    @Override
    public org.jreleaser.model.api.download.FtpDownloader asImmutable() {
        return immutable;
    }

    @Override
    public void merge(FtpDownloader source) {
        super.merge(source);
        this.username = merge(this.username, source.username);
        this.password = merge(this.password, source.password);
        this.host = merge(this.host, source.host);
        this.port = merge(this.port, source.port);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public Integer getPort() {
        return null != port ? port : 21;
    }

    @Override
    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("host", host);
        props.put("port", getPort());
        props.put("username", isNotBlank(username) ? HIDE : UNSET);
        props.put("password", isNotBlank(password) ? HIDE : UNSET);
    }
}
