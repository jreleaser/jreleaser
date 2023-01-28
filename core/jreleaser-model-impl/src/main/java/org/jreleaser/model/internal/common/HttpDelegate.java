/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
package org.jreleaser.model.internal.common;

import org.jreleaser.model.Http;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class HttpDelegate extends AbstractModelObject<HttpDelegate> implements Serializable {
    private static final long serialVersionUID = 6067002719379369883L;

    private final Map<String, String> headers = new LinkedHashMap<>();

    private String username;
    private String password;
    private Http.Method method;
    private Http.Authorization authorization;

    @Override
    public void merge(HttpDelegate source) {
        this.username = merge(this.username, source.getUsername());
        this.password = merge(this.password, source.getPassword());
        this.authorization = merge(this.authorization, source.getAuthorization());
        this.method = merge(this.method, source.getMethod());
        setHeaders(merge(this.headers, source.headers));
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Http.Method getMethod() {
        return method;
    }

    public void setMethod(Http.Method method) {
        this.method = method;
    }

    public void setMethod(String method) {
        this.method = Http.Method.of(method);
    }

    public Http.Authorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization(Http.Authorization authorization) {
        this.authorization = authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = Http.Authorization.of(authorization);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    public void asMap(Map<String, Object> props) {
        props.put("authorization", authorization);
        props.put("username", isNotBlank(username) ? HIDE : UNSET);
        props.put("password", isNotBlank(password) ? HIDE : UNSET);
        props.put("method", method);
        props.put("headers", headers);
    }

    public Http.Authorization resolveAuthorization() {
        if (null == authorization) {
            authorization = Http.Authorization.NONE;
        }

        return authorization;
    }
}
