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
import static org.jreleaser.model.api.announce.LinkedinAnnouncer.TYPE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class LinkedinAnnouncer extends AbstractMessageAnnouncer<LinkedinAnnouncer, org.jreleaser.model.api.announce.LinkedinAnnouncer> {
    private static final long serialVersionUID = -9154492643527195106L;

    private String owner;
    private String accessToken;
    private String subject;

    @JsonIgnore
    private final org.jreleaser.model.api.announce.LinkedinAnnouncer immutable = new org.jreleaser.model.api.announce.LinkedinAnnouncer() {
        private static final long serialVersionUID = 9008336230586137073L;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.LinkedinAnnouncer.TYPE;
        }

        @Override
        public String getOwner() {
            return LinkedinAnnouncer.this.getOwner();
        }

        @Override
        public String getAccessToken() {
            return LinkedinAnnouncer.this.getAccessToken();
        }

        @Override
        public String getSubject() {
            return LinkedinAnnouncer.this.getSubject();
        }

        @Override
        public String getMessage() {
            return LinkedinAnnouncer.this.getMessage();
        }

        @Override
        public String getMessageTemplate() {
            return LinkedinAnnouncer.this.getMessageTemplate();
        }

        @Override
        public String getName() {
            return LinkedinAnnouncer.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return LinkedinAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return LinkedinAnnouncer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return LinkedinAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(LinkedinAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return LinkedinAnnouncer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(LinkedinAnnouncer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return LinkedinAnnouncer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return LinkedinAnnouncer.this.getReadTimeout();
        }
    };

    public LinkedinAnnouncer() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.LinkedinAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(LinkedinAnnouncer source) {
        super.merge(source);
        this.owner = merge(this.owner, source.owner);
        this.accessToken = merge(this.accessToken, source.accessToken);
        this.subject = merge(this.subject, source.subject);
    }

    public String getResolvedTitle(JReleaserContext context) {
        TemplateContext props = context.fullProps();
        applyTemplates(props, resolvedExtraProperties());
        return resolveTemplate(subject, props);
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("owner", isNotBlank(owner) ? HIDE : UNSET);
        props.put("accessToken", isNotBlank(accessToken) ? HIDE : UNSET);
        props.put("subject", subject);
        super.asMap(full, props);
    }
}
