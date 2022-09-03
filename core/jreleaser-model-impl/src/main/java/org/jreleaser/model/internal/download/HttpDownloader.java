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
import org.jreleaser.model.Http;
import org.jreleaser.util.Env;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.download.HttpDownloader.TYPE;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class HttpDownloader extends AbstractDownloader<HttpDownloader> implements Http {
    private final Map<String, String> headers = new LinkedHashMap<>();
    private String username;
    private String password;
    private Authorization authorization;

    private final org.jreleaser.model.api.download.HttpDownloader immutable = new org.jreleaser.model.api.download.HttpDownloader() {
        private List<? extends Asset> assets;

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
            return unmodifiableMap(HttpDownloader.this.getHeaders());
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
        public List<? extends Asset> getAssets() {
            if (null == assets) {
                assets = HttpDownloader.this.assets.stream()
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
            return HttpDownloader.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(HttpDownloader.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return HttpDownloader.this.getPrefix();
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

    public HttpDownloader() {
        super(TYPE);
    }

    public org.jreleaser.model.api.download.HttpDownloader asImmutable() {
        return immutable;
    }

    @Override
    public void merge(HttpDownloader http) {
        super.merge(http);
        this.username = merge(this.username, http.username);
        this.password = merge(this.password, http.password);
        this.authorization = merge(this.authorization, http.authorization);
        setHeaders(merge(this.headers, http.headers));
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
    public Http.Authorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization(Http.Authorization authorization) {
        this.authorization = authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = Http.Authorization.of(authorization);
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    public void addHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("authorization", authorization);
        props.put("username", isNotBlank(getResolvedUsername()) ? HIDE : UNSET);
        props.put("password", isNotBlank(getResolvedPassword()) ? HIDE : UNSET);
        props.put("headers", headers);
    }

    public String getResolvedUsername() {
        return Env.env("HTTP_" + Env.toVar(name) + "_USERNAME", username);
    }

    public String getResolvedPassword() {
        return Env.env("HTTP_" + Env.toVar(name) + "_PASSWORD", password);
    }

    public Http.Authorization resolveAuthorization() {
        if (null == authorization) {
            authorization = Http.Authorization.NONE;
        }

        return authorization;
    }
}
