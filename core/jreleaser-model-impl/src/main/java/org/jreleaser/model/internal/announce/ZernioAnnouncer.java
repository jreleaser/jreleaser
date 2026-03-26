/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.announce.ZernioAnnouncer.TYPE;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class ZernioAnnouncer extends AbstractMessageAnnouncer<ZernioAnnouncer, org.jreleaser.model.api.announce.ZernioAnnouncer> {
    private static final long serialVersionUID = 1192066584634087828L;

    private String apiHost;
    private String token;
    private String profileId;

    @JsonIgnore
    private final org.jreleaser.model.api.announce.ZernioAnnouncer immutable = new org.jreleaser.model.api.announce.ZernioAnnouncer() {
        private static final long serialVersionUID = 3989721049562035329L;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.ZernioAnnouncer.TYPE;
        }

        @Override
        public String getApiHost() {
            return apiHost;
        }

        @Override
        public String getToken() {
            return token;
        }

        @Override
        public String getProfileId() {
            return profileId;
        }

        @Override
        public String getMessage() {
            return ZernioAnnouncer.this.getMessage();
        }

        @Override
        public String getMessageTemplate() {
            return ZernioAnnouncer.this.getMessageTemplate();
        }

        @Override
        public String getName() {
            return ZernioAnnouncer.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return ZernioAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return ZernioAnnouncer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return ZernioAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(ZernioAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return ZernioAnnouncer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(ZernioAnnouncer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return ZernioAnnouncer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return ZernioAnnouncer.this.getReadTimeout();
        }
    };

    public ZernioAnnouncer() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.ZernioAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(ZernioAnnouncer source) {
        super.merge(source);
        this.apiHost = merge(this.apiHost, source.apiHost);
        this.token = merge(this.token, source.token);
        this.profileId = merge(this.profileId, source.profileId);
    }

    public String getApiHost() {
        return apiHost;
    }

    public void setApiHost(String apiHost) {
        this.apiHost = apiHost;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("apiHost", apiHost);
        props.put("token", isNotBlank(token) ? HIDE : UNSET);
        props.put("profileId", profileId);
        super.asMap(full, props);
    }
}
