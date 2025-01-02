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
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.mustache.TemplateContext;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.announce.DiscourseAnnouncer.TYPE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author shblue21
 * @since 1.3.0
 */
public final class DiscourseAnnouncer extends AbstractMessageAnnouncer<DiscourseAnnouncer, org.jreleaser.model.api.announce.DiscourseAnnouncer> {
    private static final long serialVersionUID = 1521072015436279873L;

    private String host;
    private String apiKey;
    private String username;
    private String categoryName;
    private String title;

    @JsonIgnore
    private final org.jreleaser.model.api.announce.DiscourseAnnouncer immutable = new org.jreleaser.model.api.announce.DiscourseAnnouncer() {
        private static final long serialVersionUID = -913374837542980481L;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.DiscourseAnnouncer.TYPE;
        }

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public String getApiKey() {
            return apiKey;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getCategoryName() {
            return categoryName;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getMessage() {
            return DiscourseAnnouncer.this.getMessage();
        }

        @Override
        public String getMessageTemplate() {
            return DiscourseAnnouncer.this.getMessageTemplate();
        }

        @Override
        public String getName() {
            return DiscourseAnnouncer.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return DiscourseAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return DiscourseAnnouncer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return DiscourseAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(DiscourseAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return DiscourseAnnouncer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(DiscourseAnnouncer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return DiscourseAnnouncer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return DiscourseAnnouncer.this.getReadTimeout();
        }
    };

    public DiscourseAnnouncer() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.DiscourseAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(DiscourseAnnouncer source) {
        super.merge(source);
        this.host = merge(this.host, source.host);
        this.apiKey = merge(this.apiKey, source.apiKey);
        this.username = merge(this.username, source.username);
        this.categoryName = merge(this.categoryName, source.categoryName);
        this.title = merge(this.title, source.title);
    }

    public String getResolvedTitle(JReleaserContext context) {
        TemplateContext props = context.fullProps();
        applyTemplates(props, resolvedExtraProperties());
        return resolveTemplate(title, props);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("host", host);
        props.put("apiKey", isNotBlank(apiKey) ? HIDE : UNSET);
        props.put("username", isNotBlank(username) ? HIDE : UNSET);
        props.put("categoryName", categoryName);
        props.put("title", title);
        super.asMap(full, props);
    }
}
