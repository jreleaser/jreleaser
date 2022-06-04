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
package org.jreleaser.model;

import org.jreleaser.util.Env;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public class HttpUploader extends AbstractWebUploader<HttpUploader> {
    public static final String TYPE = "http";

    private final Map<String, String> headers = new LinkedHashMap<>();
    private String username;
    private String password;
    private Authorization authorization;
    private Method method;

    public HttpUploader() {
        super(TYPE);
    }

    @Override
    public void merge(HttpUploader http) {
        freezeCheck();
        super.merge(http);
        this.username = merge(this.username, http.username);
        this.password = merge(this.password, http.password);
        this.authorization = merge(this.authorization, http.authorization);
        this.method = merge(this.method, http.method);
        setHeaders(merge(this.headers, http.headers));
    }

    public Authorization resolveAuthorization() {
        if (null == authorization) {
            authorization = Authorization.NONE;
        }

        return authorization;
    }

    public String getResolvedUsername() {
        return Env.env("HTTP_" + Env.toVar(name) + "_USERNAME", username);
    }

    public String getResolvedPassword() {
        return Env.env("HTTP_" + Env.toVar(name) + "_PASSWORD", password);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        freezeCheck();
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        freezeCheck();
        this.password = password;
    }

    public Authorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization(Authorization authorization) {
        freezeCheck();
        this.authorization = authorization;
    }

    public void setAuthorization(String authorization) {
        freezeCheck();
        this.authorization = Authorization.of(authorization);
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        freezeCheck();
        this.method = method;
    }

    public void setMethod(String method) {
        freezeCheck();
        this.method = HttpUploader.Method.of(method);
    }

    public Map<String, String> getHeaders() {
        return freezeWrap(headers);
    }

    public void setHeaders(Map<String, String> headers) {
        freezeCheck();
        this.headers.putAll(headers);
    }

    @Override
    protected void asMap(Map<String, Object> props, boolean full) {
        props.put("authorization", authorization);
        props.put("method", method);
        props.put("username", isNotBlank(getResolvedUsername()) ? HIDE : UNSET);
        props.put("password", isNotBlank(getResolvedPassword()) ? HIDE : UNSET);
        props.put("headers", headers);
    }
}
