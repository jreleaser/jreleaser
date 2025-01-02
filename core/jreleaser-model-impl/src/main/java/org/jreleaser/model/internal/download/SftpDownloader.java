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

import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.api.download.SftpDownloader.TYPE;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class SftpDownloader extends AbstractSshDownloader<org.jreleaser.model.api.download.SftpDownloader, SftpDownloader> {
    private static final long serialVersionUID = -3303832713268446672L;

    @JsonIgnore
    private final org.jreleaser.model.api.download.SftpDownloader immutable = new org.jreleaser.model.api.download.SftpDownloader() {
        private static final long serialVersionUID = 8300210807291243549L;

        private List<? extends org.jreleaser.model.api.download.Downloader.Asset> assets;

        @Override
        public String getType() {
            return SftpDownloader.this.getType();
        }

        @Override
        public String getName() {
            return SftpDownloader.this.getName();
        }

        @Override
        public List<? extends org.jreleaser.model.api.download.Downloader.Asset> getAssets() {
            if (null == assets) {
                assets = SftpDownloader.this.getAssets().stream()
                    .map(Downloader.Asset::asImmutable)
                    .collect(toList());
            }
            return assets;
        }

        @Override
        public Active getActive() {
            return SftpDownloader.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return SftpDownloader.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(SftpDownloader.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return SftpDownloader.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(SftpDownloader.this.getExtraProperties());
        }

        @Override
        public String getUsername() {
            return SftpDownloader.this.getUsername();
        }

        @Override
        public String getPassword() {
            return SftpDownloader.this.getPassword();
        }

        @Override
        public String getHost() {
            return SftpDownloader.this.getHost();
        }

        @Override
        public Integer getPort() {
            return SftpDownloader.this.getPort();
        }

        @Override
        public String getKnownHostsFile() {
            return SftpDownloader.this.getKnownHostsFile();
        }

        @Override
        public String getPublicKey() {
            return SftpDownloader.this.getPublicKey();
        }

        @Override
        public String getPrivateKey() {
            return SftpDownloader.this.getPrivateKey();
        }

        @Override
        public String getPassphrase() {
            return SftpDownloader.this.getPassphrase();
        }

        @Override
        public String getFingerprint() {
            return SftpDownloader.this.getFingerprint();
        }

        @Override
        public Integer getConnectTimeout() {
            return SftpDownloader.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return SftpDownloader.this.getReadTimeout();
        }
    };

    public SftpDownloader() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.download.SftpDownloader asImmutable() {
        return immutable;
    }
}
