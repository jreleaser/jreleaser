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
import static org.jreleaser.model.api.announce.OpenCollectiveAnnouncer.TYPE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.6.0
 */
public final class OpenCollectiveAnnouncer extends AbstractMessageAnnouncer<OpenCollectiveAnnouncer, org.jreleaser.model.api.announce.OpenCollectiveAnnouncer> {
    private static final long serialVersionUID = -815384178159843616L;
    
    private String host;
    private String token;
    private String slug;
    private String title;

    @JsonIgnore
    private final org.jreleaser.model.api.announce.OpenCollectiveAnnouncer immutable = new org.jreleaser.model.api.announce.OpenCollectiveAnnouncer() {
        private static final long serialVersionUID = -18925646955049784L;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.OpenCollectiveAnnouncer.TYPE;
        }

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public String getToken() {
            return token;
        }

        @Override
        public String getSlug() {
            return slug;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getMessage() {
            return OpenCollectiveAnnouncer.this.getMessage();
        }

        @Override
        public String getMessageTemplate() {
            return OpenCollectiveAnnouncer.this.getMessageTemplate();
        }

        @Override
        public String getName() {
            return OpenCollectiveAnnouncer.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return OpenCollectiveAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return OpenCollectiveAnnouncer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return OpenCollectiveAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(OpenCollectiveAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return OpenCollectiveAnnouncer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(OpenCollectiveAnnouncer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return OpenCollectiveAnnouncer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return OpenCollectiveAnnouncer.this.getReadTimeout();
        }
    };

    public OpenCollectiveAnnouncer() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.OpenCollectiveAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(OpenCollectiveAnnouncer source) {
        super.merge(source);
        this.host = merge(this.host, source.host);
        this.token = merge(this.token, source.token);
        this.slug = merge(this.slug, source.slug);
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
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
        props.put("token", isNotBlank(token) ? HIDE : UNSET);
        props.put("slug", slug);
        props.put("title", title);
        super.asMap(full, props);
    }
}
