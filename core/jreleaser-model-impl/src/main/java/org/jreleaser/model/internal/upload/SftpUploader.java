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
package org.jreleaser.model.internal.upload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.api.upload.SftpUploader.TYPE;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class SftpUploader extends AbstractSshUploader<org.jreleaser.model.api.upload.SftpUploader, SftpUploader> {
    private static final long serialVersionUID = -1747846855467388566L;

    @JsonIgnore
    private final org.jreleaser.model.api.upload.SftpUploader immutable = new org.jreleaser.model.api.upload.SftpUploader() {
        private static final long serialVersionUID = -3588257846441022384L;

        @Override
        public String getPath() {
            return SftpUploader.this.getPath();
        }

        @Override
        public String getDownloadUrl() {
            return SftpUploader.this.getDownloadUrl();
        }

        @Override
        public String getUsername() {
            return SftpUploader.this.getUsername();
        }

        @Override
        public String getPassword() {
            return SftpUploader.this.getPassword();
        }

        @Override
        public String getHost() {
            return SftpUploader.this.getHost();
        }

        @Override
        public Integer getPort() {
            return SftpUploader.this.getPort();
        }

        @Override
        public String getKnownHostsFile() {
            return SftpUploader.this.getKnownHostsFile();
        }

        @Override
        public String getPublicKey() {
            return SftpUploader.this.getPublicKey();
        }

        @Override
        public String getPrivateKey() {
            return SftpUploader.this.getPrivateKey();
        }

        @Override
        public String getPassphrase() {
            return SftpUploader.this.getPassphrase();
        }

        @Override
        public String getFingerprint() {
            return SftpUploader.this.getFingerprint();
        }

        @Override
        public String getType() {
            return SftpUploader.this.getType();
        }

        @Override
        public String getName() {
            return SftpUploader.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return SftpUploader.this.isSnapshotSupported();
        }

        @Override
        public boolean isArtifacts() {
            return SftpUploader.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return SftpUploader.this.isFiles();
        }

        @Override
        public boolean isSignatures() {
            return SftpUploader.this.isSignatures();
        }

        @Override
        public boolean isChecksums() {
            return SftpUploader.this.isChecksums();
        }

        @Override
        public boolean isCatalogs() {
            return SftpUploader.this.isCatalogs();
        }

        @Override
        public Active getActive() {
            return SftpUploader.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return SftpUploader.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(SftpUploader.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return SftpUploader.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(SftpUploader.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return SftpUploader.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return SftpUploader.this.getReadTimeout();
        }
    };

    public SftpUploader() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.upload.SftpUploader asImmutable() {
        return immutable;
    }
}
