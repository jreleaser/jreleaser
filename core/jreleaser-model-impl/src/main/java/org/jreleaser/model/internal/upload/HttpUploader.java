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
package org.jreleaser.model.internal.upload;

import org.jreleaser.model.Active;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.upload.HttpUploader.TYPE;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public final class HttpUploader extends AbstractWebUploader<org.jreleaser.model.api.upload.HttpUploader, HttpUploader> {
    private static final long serialVersionUID = -2372935050836857644L;

    private final Map<String, String> headers = new LinkedHashMap<>();
    private String username;
    private String password;
    private Authorization authorization;
    private Method method;

    private final org.jreleaser.model.api.upload.HttpUploader immutable = new org.jreleaser.model.api.upload.HttpUploader() {
        private static final long serialVersionUID = 3000310615738273509L;

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public String getUploadUrl() {
            return uploadUrl;
        }

        @Override
        public String getDownloadUrl() {
            return downloadUrl;
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
        public Authorization getAuthorization() {
            return authorization;
        }

        @Override
        public Map<String, String> getHeaders() {
            return unmodifiableMap(headers);
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getName() {
            return name;
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
        public Active getActive() {
            return active;
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
            return HttpUploader.this.getPrefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
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
        this.username = merge(this.username, source.username);
        this.password = merge(this.password, source.password);
        this.authorization = merge(this.authorization, source.authorization);
        this.method = merge(this.method, source.method);
        setHeaders(merge(this.headers, source.headers));
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Authorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization(Authorization authorization) {
        this.authorization = authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = Authorization.of(authorization);
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setMethod(String method) {
        this.method = HttpUploader.Method.of(method);
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("authorization", authorization);
        props.put("method", method);
        props.put("username", isNotBlank(username) ? HIDE : UNSET);
        props.put("password", isNotBlank(password) ? HIDE : UNSET);
        props.put("headers", headers);
    }

    public Authorization resolveAuthorization() {
        if (null == authorization) {
            authorization = Authorization.NONE;
        }

        return authorization;
    }
}
