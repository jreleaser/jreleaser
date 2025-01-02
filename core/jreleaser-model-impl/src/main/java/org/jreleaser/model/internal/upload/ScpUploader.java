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
import static org.jreleaser.model.api.upload.ScpUploader.TYPE;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class ScpUploader extends AbstractSshUploader<org.jreleaser.model.api.upload.ScpUploader, ScpUploader> {
    private static final long serialVersionUID = -6887462905710198298L;

    @JsonIgnore
    private final org.jreleaser.model.api.upload.ScpUploader immutable = new org.jreleaser.model.api.upload.ScpUploader() {
        private static final long serialVersionUID = 3793298601082284442L;

        @Override
        public String getPath() {
            return ScpUploader.this.getPath();
        }

        @Override
        public String getDownloadUrl() {
            return ScpUploader.this.getDownloadUrl();
        }

        @Override
        public String getUsername() {
            return ScpUploader.this.getUsername();
        }

        @Override
        public String getPassword() {
            return ScpUploader.this.getPassword();
        }

        @Override
        public String getHost() {
            return ScpUploader.this.getHost();
        }

        @Override
        public Integer getPort() {
            return ScpUploader.this.getPort();
        }

        @Override
        public String getKnownHostsFile() {
            return ScpUploader.this.getKnownHostsFile();
        }

        @Override
        public String getPublicKey() {
            return ScpUploader.this.getPublicKey();
        }

        @Override
        public String getPrivateKey() {
            return ScpUploader.this.getPrivateKey();
        }

        @Override
        public String getPassphrase() {
            return ScpUploader.this.getPassphrase();
        }

        @Override
        public String getFingerprint() {
            return ScpUploader.this.getFingerprint();
        }

        @Override
        public String getType() {
            return ScpUploader.this.getType();
        }

        @Override
        public String getName() {
            return ScpUploader.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return ScpUploader.this.isSnapshotSupported();
        }

        @Override
        public boolean isArtifacts() {
            return ScpUploader.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return ScpUploader.this.isFiles();
        }

        @Override
        public boolean isSignatures() {
            return ScpUploader.this.isSignatures();
        }

        @Override
        public boolean isChecksums() {
            return ScpUploader.this.isChecksums();
        }

        @Override
        public boolean isCatalogs() {
            return ScpUploader.this.isCatalogs();
        }

        @Override
        public Active getActive() {
            return ScpUploader.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return ScpUploader.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(ScpUploader.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return ScpUploader.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(ScpUploader.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return ScpUploader.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return ScpUploader.this.getReadTimeout();
        }
    };

    public ScpUploader() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.upload.ScpUploader asImmutable() {
        return immutable;
    }
}
