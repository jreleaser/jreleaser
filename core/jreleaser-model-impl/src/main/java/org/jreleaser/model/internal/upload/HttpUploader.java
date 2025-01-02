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
import org.jreleaser.model.internal.common.HttpDelegate;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.api.upload.HttpUploader.TYPE;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public final class HttpUploader extends AbstractWebUploader<org.jreleaser.model.api.upload.HttpUploader, HttpUploader>
    implements org.jreleaser.model.internal.common.Http {
    private static final long serialVersionUID = 3851047281417864436L;

    private final HttpDelegate delegate = new HttpDelegate();

    @JsonIgnore
    private final org.jreleaser.model.api.upload.HttpUploader immutable = new org.jreleaser.model.api.upload.HttpUploader() {
        private static final long serialVersionUID = -2422450427549788470L;

        @Override
        public Method getMethod() {
            return HttpUploader.this.getMethod();
        }

        @Override
        public String getUploadUrl() {
            return HttpUploader.this.getUploadUrl();
        }

        @Override
        public String getDownloadUrl() {
            return HttpUploader.this.getDownloadUrl();
        }

        @Override
        public String getUsername() {
            return HttpUploader.this.getUsername();
        }

        @Override
        public String getPassword() {
            return HttpUploader.this.getPassword();
        }

        @Override
        public Authorization getAuthorization() {
            return HttpUploader.this.getAuthorization();
        }

        @Override
        public Map<String, String> getHeaders() {
            return unmodifiableMap(HttpUploader.this.getHeaders());
        }

        @Override
        public String getType() {
            return HttpUploader.this.getType();
        }

        @Override
        public String getName() {
            return HttpUploader.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return HttpUploader.this.isSnapshotSupported();
        }

        @Override
        public boolean isArtifacts() {
            return HttpUploader.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return HttpUploader.this.isFiles();
        }

        @Override
        public boolean isSignatures() {
            return HttpUploader.this.isSignatures();
        }

        @Override
        public boolean isChecksums() {
            return HttpUploader.this.isChecksums();
        }

        @Override
        public boolean isCatalogs() {
            return HttpUploader.this.isCatalogs();
        }

        @Override
        public Active getActive() {
            return HttpUploader.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return HttpUploader.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(HttpUploader.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return HttpUploader.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(HttpUploader.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return HttpUploader.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return HttpUploader.this.getReadTimeout();
        }
    };

    public HttpUploader() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.upload.HttpUploader asImmutable() {
        return immutable;
    }

    @Override
    public void merge(HttpUploader source) {
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

    public Method getMethod() {
        return delegate.getMethod();
    }

    public void setMethod(Method method) {
        delegate.setMethod(method);
    }

    public void setMethod(String method) {
        delegate.setMethod(method);
    }

    @Override
    public Authorization getAuthorization() {
        return delegate.getAuthorization();
    }

    @Override
    public void setAuthorization(Authorization authorization) {
        delegate.setAuthorization(authorization);
    }

    @Override
    public void setAuthorization(String authorization) {
        delegate.setAuthorization(authorization);
    }

    @Override
    public Map<String, String> getHeaders() {
        return delegate.getHeaders();
    }

    public void setHeaders(Map<String, String> headers) {
        delegate.setHeaders(headers);
    }

    @Override
    public Authorization resolveAuthorization() {
        return delegate.resolveAuthorization();
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        delegate.asMap(props);
    }
}
