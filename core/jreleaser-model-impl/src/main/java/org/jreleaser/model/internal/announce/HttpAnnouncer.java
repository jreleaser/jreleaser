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
package org.jreleaser.model.internal.announce;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.Constants;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.HttpDelegate;
import org.jreleaser.mustache.TemplateContext;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.KEY_TAG_NAME;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.mustache.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class HttpAnnouncer extends AbstractAnnouncer<HttpAnnouncer, org.jreleaser.model.api.announce.HttpAnnouncer> implements org.jreleaser.model.internal.common.Http {
    private static final long serialVersionUID = -8348542653717001938L;

    private final HttpDelegate delegate = new HttpDelegate();
    private String url;
    private String payload;
    private String payloadTemplate;
    private String bearerKeyword;

    @JsonIgnore
    private final org.jreleaser.model.api.announce.HttpAnnouncer immutable = new org.jreleaser.model.api.announce.HttpAnnouncer() {
        private static final long serialVersionUID = -2918111244399624143L;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.HttpAnnouncers.TYPE;
        }

        @Override
        public Method getMethod() {
            return HttpAnnouncer.this.getMethod();
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
            return HttpAnnouncer.this.getUsername();
        }

        @Override
        public String getPassword() {
            return HttpAnnouncer.this.getPassword();
        }

        @Override
        public Authorization getAuthorization() {
            return HttpAnnouncer.this.getAuthorization();
        }

        @Override
        public String getBearerKeyword() {
            return HttpAnnouncer.this.getBearerKeyword();
        }

        @Override
        public Map<String, String> getHeaders() {
            return unmodifiableMap(HttpAnnouncer.this.getHeaders());
        }

        @Override
        public String getName() {
            return HttpAnnouncer.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return HttpAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return HttpAnnouncer.this.getActive();
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
            return HttpAnnouncer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(HttpAnnouncer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return HttpAnnouncer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return HttpAnnouncer.this.getReadTimeout();
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
        this.delegate.merge(this.delegate);
        this.url = merge(this.url, source.url);
        this.bearerKeyword = merge(this.bearerKeyword, source.bearerKeyword);
        this.payload = merge(this.payload, source.payload);
        this.payloadTemplate = merge(this.payloadTemplate, source.payloadTemplate);
    }

    @Override
    public String prefix() {
        return "http";
    }

    public String getResolvedUrl(JReleaserContext context) {
        TemplateContext props = context.fullProps();
        applyTemplates(props, resolvedExtraProperties());
        return resolveTemplate(url, props);
    }

    public String getResolvedPayload(JReleaserContext context) {
        TemplateContext props = context.fullProps();
        context.getChangelog().apply(props);
        applyTemplates(props, resolvedExtraProperties());
        return resolveTemplate(payload, props);
    }

    public String getResolvedPayloadTemplate(JReleaserContext context, TemplateContext extraProps) {
        TemplateContext props = context.fullProps();
        context.getChangelog().apply(props);
        applyTemplates(props, resolvedExtraProperties());
        props.set(KEY_TAG_NAME, context.getModel().getRelease().getReleaser()
            .getEffectiveTagName(context.getModel()));
        props.set(Constants.KEY_PREVIOUS_TAG_NAME,
            context.getModel().getRelease().getReleaser()
                .getResolvedPreviousTagName(context.getModel()));
        props.setAll(extraProps);

        Path templatePath = context.getBasedir().resolve(payloadTemplate);
        try {
            Reader reader = java.nio.file.Files.newBufferedReader(templatePath);
            return applyTemplate(reader, props);
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_reading_template",
                context.relativizeToBasedir(templatePath)));
        }
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

    public String getBearerKeyword() {
        return bearerKeyword;
    }

    public void setBearerKeyword(String bearerKeyword) {
        this.bearerKeyword = bearerKeyword;
    }

    public Authorization resolveAuthorization() {
        return delegate.resolveAuthorization();
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
        delegate.asMap(props);
        props.put("bearerKeyword", bearerKeyword);
        props.put("payload", payload);
        props.put("payloadTemplate", payloadTemplate);
    }
}
