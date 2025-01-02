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
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.mustache.TemplateContext;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.KEY_PREVIOUS_TAG_NAME;
import static org.jreleaser.model.Constants.KEY_TAG_NAME;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.announce.BlueskyAnnouncer.TYPE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Simon Verhoeven
 * @author Tom Cools
 * @since 1.7.0
 */
public final class BlueskyAnnouncer extends AbstractAnnouncer<BlueskyAnnouncer, org.jreleaser.model.api.announce.BlueskyAnnouncer> {
    private static final long serialVersionUID = 9152609285615015647L;

    private final List<String> statuses = new ArrayList<>();
    private String host;
    private String handle;
    private String password;
    private String status;
    private String statusTemplate;

    @JsonIgnore
    private final org.jreleaser.model.api.announce.BlueskyAnnouncer immutable = new org.jreleaser.model.api.announce.BlueskyAnnouncer() {
        private static final long serialVersionUID = -8926470689255000598L;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.BlueskyAnnouncer.TYPE;
        }

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public String getHandle() {
            return handle;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getStatus() {
            return status;
        }

        @Override
        public List<String> getStatuses() {
            return statuses;
        }

        @Override
        public String getStatusTemplate() {
            return statusTemplate;
        }

        @Override
        public String getName() {
            return BlueskyAnnouncer.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return BlueskyAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return BlueskyAnnouncer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return BlueskyAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(BlueskyAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return BlueskyAnnouncer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(BlueskyAnnouncer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return BlueskyAnnouncer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return BlueskyAnnouncer.this.getReadTimeout();
        }
    };

    public BlueskyAnnouncer() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.BlueskyAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(BlueskyAnnouncer source) {
        super.merge(source);
        this.host = merge(this.host, source.host);
        this.handle = merge(this.handle, source.handle);
        this.password = merge(this.password, source.password);
        this.status = merge(this.status, source.status);
        setStatuses(merge(this.statuses, source.statuses));
        this.statusTemplate = merge(this.statusTemplate, source.statusTemplate);
    }

    public String getResolvedStatusTemplate(JReleaserContext context, TemplateContext extraProps) {
        TemplateContext props = context.fullProps();
        context.getChangelog().apply(props);
        applyTemplates(props, resolvedExtraProperties());
        props.set(KEY_TAG_NAME, context.getModel().getRelease().getReleaser().getEffectiveTagName(context.getModel()));
        props.set(KEY_PREVIOUS_TAG_NAME, context.getModel().getRelease().getReleaser().getResolvedPreviousTagName(context.getModel()));
        props.setAll(extraProps);

        Path templatePath = context.getBasedir().resolve(statusTemplate);
        try {
            Reader reader = java.nio.file.Files.newBufferedReader(templatePath);
            return applyTemplate(reader, props);
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_reading_template",
                context.relativizeToBasedir(templatePath)));
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<String> statuses) {
        this.statuses.clear();
        this.statuses.addAll(statuses);
    }

    public String getStatusTemplate() {
        return statusTemplate;
    }

    public void setStatusTemplate(String statusTemplate) {
        this.statusTemplate = statusTemplate;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("host", host);
        props.put("handle", isNotBlank(handle) ? HIDE : UNSET);
        props.put("password", isNotBlank(password) ? HIDE : UNSET);
        props.put("status", status);
        props.put("statuses", statuses);
        props.put("statusTemplate", statusTemplate);
    }
}
