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
import org.jreleaser.model.internal.common.Ftp;
import org.jreleaser.model.internal.common.FtpDelegate;

import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.api.download.FtpDownloader.TYPE;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class FtpDownloader extends AbstractDownloader<org.jreleaser.model.api.download.FtpDownloader, FtpDownloader> implements Ftp {
    private static final long serialVersionUID = 8625938906357137469L;

    private final FtpDelegate delegate = new FtpDelegate();

    @JsonIgnore
    private final org.jreleaser.model.api.download.FtpDownloader immutable = new org.jreleaser.model.api.download.FtpDownloader() {
        private static final long serialVersionUID = -3069423247317140050L;

        private List<? extends org.jreleaser.model.api.download.Downloader.Asset> assets;

        @Override
        public String getType() {
            return FtpDownloader.this.getType();
        }

        @Override
        public String getName() {
            return FtpDownloader.this.getName();
        }

        @Override
        public List<? extends org.jreleaser.model.api.download.Downloader.Asset> getAssets() {
            if (null == assets) {
                assets = FtpDownloader.this.getAssets().stream()
                    .map(Downloader.Asset::asImmutable)
                    .collect(toList());
            }
            return assets;
        }

        @Override
        public Active getActive() {
            return FtpDownloader.this.getActive();
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
            return FtpDownloader.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(FtpDownloader.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return FtpDownloader.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return FtpDownloader.this.getReadTimeout();
        }

        @Override
        public String getUsername() {
            return FtpDownloader.this.getUsername();
        }

        @Override
        public String getPassword() {
            return FtpDownloader.this.getPassword();
        }

        @Override
        public String getHost() {
            return FtpDownloader.this.getHost();
        }

        @Override
        public Integer getPort() {
            return FtpDownloader.this.getPort();
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
        this.delegate.merge(source.delegate);
    }

    @Override
    public String getUsername() {
        return delegate.getUsername();
    }

    @Override
    public void setUsername(String username) {
        delegate.setUsername(username);
    }

    @Override
    public String getPassword() {
        return delegate.getPassword();
    }

    @Override
    public void setPassword(String password) {
        delegate.setPassword(password);
    }

    @Override
    public String getHost() {
        return delegate.getHost();
    }

    @Override
    public void setHost(String host) {
        delegate.setHost(host);
    }

    @Override
    public Integer getPort() {
        return delegate.getPort();
    }

    @Override
    public void setPort(Integer port) {
        delegate.setPort(port);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        delegate.asMap(props);
    }
}
