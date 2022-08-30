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

import org.jreleaser.bundle.RB;
import org.jreleaser.util.Env;
import org.jreleaser.util.JReleaserException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.KEY_TAG_NAME;
import static org.jreleaser.util.Constants.UNSET;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public class HttpAnnouncer extends AbstractAnnouncer<HttpAnnouncer> implements Http {
    private final Map<String, String> headers = new LinkedHashMap<>();
    private String url;
    private String username;
    private String password;
    private Authorization authorization;
    private Method method;
    private String payload;
    private String payloadTemplate;

    public HttpAnnouncer() {
        super("");
    }

    @Override
    public void merge(HttpAnnouncer http) {
        freezeCheck();
        super.merge(http);
        this.url = merge(this.url, http.url);
        this.username = merge(this.username, http.username);
        this.password = merge(this.password, http.password);
        this.authorization = merge(this.authorization, http.authorization);
        this.method = merge(this.method, http.method);
        this.payload = merge(this.payload, http.payload);
        this.payloadTemplate = merge(this.payloadTemplate, http.payloadTemplate);
        setHeaders(merge(this.headers, http.headers));
    }

    @Override
    public String getPrefix() {
        return "http";
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

    public String getResolvedUrl(JReleaserContext context) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        return resolveTemplate(url, props);
    }

    public String getResolvedPayload(JReleaserContext context) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        return resolveTemplate(payload, props);
    }

    public String getResolvedPayloadTemplate(JReleaserContext context, Map<String, Object> extraProps) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        props.put(KEY_TAG_NAME, context.getModel().getRelease().getGitService()
            .getEffectiveTagName(context.getModel()));
        props.putAll(extraProps);

        Path templatePath = context.getBasedir().resolve(payloadTemplate);
        try {
            Reader reader = java.nio.file.Files.newBufferedReader(templatePath);
            return applyTemplate(reader, props);
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_reading_template",
                context.relativizeToBasedir(templatePath)));
        }
    }

    public void setName(String name) {
        freezeCheck();
        this.name = name;
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
        this.method = Method.of(method);
    }

    public Map<String, String> getHeaders() {
        return freezeWrap(headers);
    }

    public void setHeaders(Map<String, String> headers) {
        freezeCheck();
        this.headers.putAll(headers);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        freezeCheck();
        this.url = url;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        freezeCheck();
        this.payload = payload;
    }

    public String getPayloadTemplate() {
        return payloadTemplate;
    }

    public void setPayloadTemplate(String payloadTemplate) {
        freezeCheck();
        this.payloadTemplate = payloadTemplate;
    }

    @Override
    protected void asMap(Map<String, Object> props, boolean full) {
        props.put("url", url);
        props.put("authorization", authorization);
        props.put("method", method);
        props.put("username", isNotBlank(getResolvedUsername()) ? HIDE : UNSET);
        props.put("password", isNotBlank(getResolvedPassword()) ? HIDE : UNSET);
        props.put("headers", headers);
        props.put("payload", payload);
        props.put("payloadTemplate", payloadTemplate);
    }
}
