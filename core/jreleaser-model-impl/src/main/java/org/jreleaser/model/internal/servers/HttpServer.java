/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 The JReleaser authors.
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
package org.jreleaser.model.internal.servers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Http;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @author Jan Wloka
 * @since 1.20.0
 */
public final class HttpServer extends AbstractServer<org.jreleaser.model.api.servers.HttpServer, HttpServer>
    implements org.jreleaser.model.internal.common.Http {
    private static final long serialVersionUID = -5528122350198194278L;

    private final Map<String, String> headers = new LinkedHashMap<>();

    private String username;
    private String password;
    private Http.Authorization authorization;

    @JsonIgnore
    private final org.jreleaser.model.api.servers.HttpServer immutable = new org.jreleaser.model.api.servers.HttpServer() {
        private static final long serialVersionUID = -6170197904971814055L;

        @Override
        public String getName() {
            return HttpServer.this.getName();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(HttpServer.this.asMap(full));
        }

        @Override
        public String getHost() {
            return HttpServer.this.getHost();
        }

        @Override
        public Integer getPort() {
            return HttpServer.this.getPort();
        }

        @Override
        public String getUsername() {
            return HttpServer.this.getUsername();
        }

        @Override
        public String getPassword() {
            return HttpServer.this.getPassword();
        }

        @Override
        public Integer getConnectTimeout() {
            return HttpServer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return HttpServer.this.getReadTimeout();
        }

        @Override
        public Authorization getAuthorization() {
            return HttpServer.this.getAuthorization();
        }

        @Override
        public Map<String, String> getHeaders() {
            return unmodifiableMap(HttpServer.this.getHeaders());
        }
    };

    @Override
    public org.jreleaser.model.api.servers.HttpServer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(HttpServer source) {
        super.merge(source);
        this.username = merge(this.username, source.getUsername());
        this.password = merge(this.password, source.getPassword());
        this.authorization = merge(this.authorization, source.getAuthorization());
        setHeaders(merge(this.headers, source.headers));
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
    public Http.Authorization getAuthorization() {
        return authorization;
    }

    @Override
    public void setAuthorization(Http.Authorization authorization) {
        this.authorization = authorization;
    }

    @Override
    public void setAuthorization(String authorization) {
        this.authorization = Http.Authorization.of(authorization);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("authorization", getAuthorization());
        props.put("username", isNotBlank(getUsername()) ? HIDE : UNSET);
        props.put("password", isNotBlank(getPassword()) ? HIDE : UNSET);
        props.put("headers", getHeaders());
    }

    @Override
    public Http.Authorization resolveAuthorization() {
        if (null == authorization) {
            authorization = Http.Authorization.NONE;
        }

        return authorization;
    }

    public void mergeWith(HttpServer other) {
        super.mergeWith(other);
        setAuthorization(merge(other.getAuthorization(), getAuthorization()));
        setHeaders(merge(other.getHeaders(), getHeaders()));
    }
}
