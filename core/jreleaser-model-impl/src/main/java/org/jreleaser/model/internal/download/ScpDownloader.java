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
import static org.jreleaser.model.api.download.ScpDownloader.TYPE;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class ScpDownloader extends AbstractSshDownloader<org.jreleaser.model.api.download.ScpDownloader, ScpDownloader> {
    private static final long serialVersionUID = -59581960524753148L;

    @JsonIgnore
    private final org.jreleaser.model.api.download.ScpDownloader immutable = new org.jreleaser.model.api.download.ScpDownloader() {
        private static final long serialVersionUID = 322325976684206094L;

        private List<? extends org.jreleaser.model.api.download.Downloader.Asset> assets;

        @Override
        public String getType() {
            return ScpDownloader.this.getType();
        }

        @Override
        public String getName() {
            return ScpDownloader.this.getName();
        }

        @Override
        public List<? extends org.jreleaser.model.api.download.Downloader.Asset> getAssets() {
            if (null == assets) {
                assets = ScpDownloader.this.getAssets().stream()
                    .map(Downloader.Asset::asImmutable)
                    .collect(toList());
            }
            return assets;
        }

        @Override
        public Active getActive() {
            return ScpDownloader.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return ScpDownloader.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(ScpDownloader.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return ScpDownloader.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(ScpDownloader.this.getExtraProperties());
        }

        @Override
        public String getUsername() {
            return ScpDownloader.this.getUsername();
        }

        @Override
        public String getPassword() {
            return ScpDownloader.this.getPassword();
        }

        @Override
        public String getHost() {
            return ScpDownloader.this.getHost();
        }

        @Override
        public Integer getPort() {
            return ScpDownloader.this.getPort();
        }

        @Override
        public String getKnownHostsFile() {
            return ScpDownloader.this.getKnownHostsFile();
        }

        @Override
        public String getPublicKey() {
            return ScpDownloader.this.getPublicKey();
        }

        @Override
        public String getPrivateKey() {
            return ScpDownloader.this.getPrivateKey();
        }

        @Override
        public String getPassphrase() {
            return ScpDownloader.this.getPassphrase();
        }

        @Override
        public String getFingerprint() {
            return ScpDownloader.this.getFingerprint();
        }

        @Override
        public Integer getConnectTimeout() {
            return ScpDownloader.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return ScpDownloader.this.getReadTimeout();
        }
    };

    public ScpDownloader() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.download.ScpDownloader asImmutable() {
        return immutable;
    }
}
