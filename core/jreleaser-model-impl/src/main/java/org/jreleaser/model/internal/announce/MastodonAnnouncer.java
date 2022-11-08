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

import org.jreleaser.model.Active;
import org.jreleaser.model.internal.JReleaserContext;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.announce.MastodonAnnouncer.TYPE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public final class MastodonAnnouncer extends AbstractAnnouncer<MastodonAnnouncer, org.jreleaser.model.api.announce.MastodonAnnouncer> {
    private String host;
    private String accessToken;
    private String status;

    private final org.jreleaser.model.api.announce.MastodonAnnouncer immutable = new org.jreleaser.model.api.announce.MastodonAnnouncer() {
        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.MastodonAnnouncer.TYPE;
        }

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public String getAccessToken() {
            return accessToken;
        }

        @Override
        public String getStatus() {
            return status;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isSnapshotSupported() {
            return MastodonAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return MastodonAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(MastodonAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return MastodonAnnouncer.this.getPrefix();
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

    public MastodonAnnouncer() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.MastodonAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(MastodonAnnouncer source) {
        super.merge(source);
        this.host = merge(this.host, source.host);
        this.accessToken = merge(this.accessToken, source.accessToken);
        this.status = merge(this.status, source.status);
    }

    public String getResolvedStatus(JReleaserContext context) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        context.getModel().getRelease().getReleaser().fillProps(props, context.getModel());
        return resolveTemplate(status, props);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("host", host);
        props.put("accessToken", isNotBlank(accessToken) ? HIDE : UNSET);
        props.put("status", status);
    }
}
