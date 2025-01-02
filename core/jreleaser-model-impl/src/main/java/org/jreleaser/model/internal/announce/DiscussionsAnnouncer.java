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
import static org.jreleaser.model.api.announce.DiscussionsAnnouncer.TYPE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.mustache.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class DiscussionsAnnouncer extends AbstractMessageAnnouncer<DiscussionsAnnouncer, org.jreleaser.model.api.announce.DiscussionsAnnouncer> {
    private static final long serialVersionUID = -3953718151030666891L;

    private String organization;
    private String team;
    private String title;

    @JsonIgnore
    private final org.jreleaser.model.api.announce.DiscussionsAnnouncer immutable = new org.jreleaser.model.api.announce.DiscussionsAnnouncer() {
        private static final long serialVersionUID = -3455087550754390394L;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.DiscussionsAnnouncer.TYPE;
        }

        @Override
        public String getOrganization() {
            return organization;
        }

        @Override
        public String getTeam() {
            return team;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getMessage() {
            return DiscussionsAnnouncer.this.getMessage();
        }

        @Override
        public String getMessageTemplate() {
            return DiscussionsAnnouncer.this.getMessageTemplate();
        }

        @Override
        public String getName() {
            return DiscussionsAnnouncer.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return DiscussionsAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return DiscussionsAnnouncer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return DiscussionsAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(DiscussionsAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return DiscussionsAnnouncer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(DiscussionsAnnouncer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return DiscussionsAnnouncer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return DiscussionsAnnouncer.this.getReadTimeout();
        }
    };

    public DiscussionsAnnouncer() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.DiscussionsAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(DiscussionsAnnouncer source) {
        super.merge(source);
        this.organization = merge(this.organization, source.organization);
        this.team = merge(this.team, source.team);
        this.title = merge(this.title, source.title);
    }

    public String getResolvedTitle(JReleaserContext context) {
        TemplateContext props = context.fullProps();
        applyTemplates(props, resolvedExtraProperties());
        return resolveTemplate(title, props);
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("organization", organization);
        props.put("team", team);
        props.put("title", title);
        super.asMap(full, props);
    }
}
