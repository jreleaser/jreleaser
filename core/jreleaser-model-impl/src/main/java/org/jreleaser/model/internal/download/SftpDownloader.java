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
    private final org.jreleaser.model.api.download.SftpDownloader immutable = new org.jreleaser.model.api.download.SftpDownloader() {
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
                assets = SftpDownloader.this.assets.stream()
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
            return SftpDownloader.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(SftpDownloader.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return SftpDownloader.this.getPrefix();
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
        public String getKnownHostsFile() {
            return knownHostsFile;
        }

        @Override
        public String getPublicKey() {
            return publicKey;
        }

        @Override
        public String getPrivateKey() {
            return privateKey;
        }

        @Override
        public String getPassphrase() {
            return passphrase;
        }

        @Override
        public String getFingerprint() {
            return fingerprint;
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

    public SftpDownloader() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.download.SftpDownloader asImmutable() {
        return immutable;
    }
}
