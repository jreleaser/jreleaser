/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public class HttpUploader extends AbstractUploader {
    public static final String NAME = "http";

    private final Map<String, String> headers = new LinkedHashMap<>();
    private String target;
    private String username;
    private String password;
    private Authorization authorization;
    private Method method;

    public HttpUploader() {
        super(NAME);
    }

    void setAll(HttpUploader http) {
        super.setAll(http);
        this.username = http.username;
        this.password = http.password;
        this.target = http.target;
        this.authorization = http.authorization;
        this.method = http.method;
        setHeaders(http.headers);
    }

    public Authorization resolveAuthorization() {
        if (null == authorization) {
            authorization = Authorization.NONE;
        }

        return authorization;
    }

    public String getResolvedTarget(Map<String, Object> props) {
        Map<String, Object> p = new LinkedHashMap<>(props);
        p.putAll(getResolvedExtraProperties());
        return applyTemplate(target, p);
    }

    public String getResolvedUsername() {
        return Env.resolve("HTTP_" + Env.toVar(name) + "_USERNAME", username);
    }

    public String getResolvedPassword() {
        return Env.resolve("HTTP_" + Env.toVar(name) + "_PASSWORD", password);
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

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

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
    protected void asMap(Map<String, Object> props, boolean full) {
        props.put("target", target);
        props.put("authorization", authorization);
        props.put("method", method);
        props.put("username", isNotBlank(getResolvedUsername()) ? HIDE : UNSET);
        props.put("password", isNotBlank(getResolvedPassword()) ? HIDE : UNSET);
        props.put("headers", headers);
    }
}
