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
package org.jreleaser.model.internal.announce;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.Http;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.KEY_TAG_NAME;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class HttpAnnouncer extends AbstractAnnouncer<HttpAnnouncer, org.jreleaser.model.api.announce.HttpAnnouncer> implements Http {
    private final Map<String, String> headers = new LinkedHashMap<>();
    private String url;
    private String username;
    private String password;
    private Authorization authorization;
    private Method method;
    private String payload;
    private String payloadTemplate;
    private String bearerKeyword;

    private final org.jreleaser.model.api.announce.HttpAnnouncer immutable = new org.jreleaser.model.api.announce.HttpAnnouncer() {
        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.HttpAnnouncers.TYPE;
        }

        @Override
        public Method getMethod() {
            return null;
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String getPayload() {
            return payload;
        }

        @Override
        public String getPayloadTemplate() {
            return payloadTemplate;
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
        public String getBearerKeyword() {
            return HttpAnnouncer.this.getBearerKeyword();
        }

        @Override
        public Map<String, String> getHeaders() {
            return unmodifiableMap(headers);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isSnapshotSupported() {
            return HttpAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return HttpAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(HttpAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return HttpAnnouncer.this.getPrefix();
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

    public HttpAnnouncer() {
        super("");
    }

    @Override
    public org.jreleaser.model.api.announce.HttpAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(HttpAnnouncer source) {
        super.merge(source);
        this.url = merge(this.url, source.url);
        this.username = merge(this.username, source.username);
        this.password = merge(this.password, source.password);
        this.authorization = merge(this.authorization, source.authorization);
        this.bearerKeyword = merge(this.bearerKeyword, source.bearerKeyword);
        this.method = merge(this.method, source.method);
        this.payload = merge(this.payload, source.payload);
        this.payloadTemplate = merge(this.payloadTemplate, source.payloadTemplate);
        setHeaders(merge(this.headers, source.headers));
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
        props.put(KEY_TAG_NAME, context.getModel().getRelease().getReleaser()
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
        this.name = name;
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

    public String getBearerKeyword() {
        return bearerKeyword;
    }

    public void setBearerKeyword(String bearerKeyword) {
        this.bearerKeyword = bearerKeyword;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setMethod(String method) {
        this.method = Method.of(method);
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getPayloadTemplate() {
        return payloadTemplate;
    }

    public void setPayloadTemplate(String payloadTemplate) {
        this.payloadTemplate = payloadTemplate;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("url", url);
        props.put("authorization", authorization);
        props.put("bearerKeyword", bearerKeyword);
        props.put("method", method);
        props.put("username", isNotBlank(username) ? HIDE : UNSET);
        props.put("password", isNotBlank(password) ? HIDE : UNSET);
        props.put("headers", headers);
        props.put("payload", payload);
        props.put("payloadTemplate", payloadTemplate);
    }
}
