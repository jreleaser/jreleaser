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
import org.jreleaser.mustache.TemplateContext;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.KEY_TAG_NAME;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.announce.TwitterAnnouncer.TYPE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class TwitterAnnouncer extends AbstractAnnouncer<TwitterAnnouncer, org.jreleaser.model.api.announce.TwitterAnnouncer> {
    private static final long serialVersionUID = -5723247167488210082L;

    private final List<String> statuses = new ArrayList<>();
    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessTokenSecret;
    private String status;
    private String statusTemplate;

    @JsonIgnore
    private final org.jreleaser.model.api.announce.TwitterAnnouncer immutable = new org.jreleaser.model.api.announce.TwitterAnnouncer() {
        private static final long serialVersionUID = -7092168952957318545L;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.TwitterAnnouncer.TYPE;
        }

        @Override
        public String getConsumerKey() {
            return consumerKey;
        }

        @Override
        public String getConsumerSecret() {
            return consumerSecret;
        }

        @Override
        public String getAccessToken() {
            return accessToken;
        }

        @Override
        public String getAccessTokenSecret() {
            return accessTokenSecret;
        }

        @Override
        public String getStatus() {
            return status;
        }

        @Override
        public List<String> getStatuses() {
            return unmodifiableList(statuses);
        }

        @Override
        public String getStatusTemplate() {
            return statusTemplate;
        }

        @Override
        public String getName() {
            return TwitterAnnouncer.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return TwitterAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return TwitterAnnouncer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return TwitterAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(TwitterAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return TwitterAnnouncer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(TwitterAnnouncer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return TwitterAnnouncer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return TwitterAnnouncer.this.getReadTimeout();
        }
    };

    public TwitterAnnouncer() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.TwitterAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(TwitterAnnouncer source) {
        super.merge(source);
        this.consumerKey = merge(this.consumerKey, source.consumerKey);
        this.consumerSecret = merge(this.consumerSecret, source.consumerSecret);
        this.accessToken = merge(this.accessToken, source.accessToken);
        this.accessTokenSecret = merge(this.accessTokenSecret, source.accessTokenSecret);
        this.status = merge(this.status, source.status);
        setStatuses(merge(this.statuses, source.statuses));
        this.statusTemplate = merge(this.statusTemplate, source.statusTemplate);
    }

    public String getResolvedStatusTemplate(JReleaserContext context, TemplateContext extraProps) {
        TemplateContext props = context.fullProps();
        context.getChangelog().apply(props);
        applyTemplates(props, resolvedExtraProperties());
        props.set(KEY_TAG_NAME, context.getModel().getRelease().getReleaser()
            .getEffectiveTagName(context.getModel()));
        props.set(Constants.KEY_PREVIOUS_TAG_NAME,
            context.getModel().getRelease().getReleaser()
                .getResolvedPreviousTagName(context.getModel()));
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

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
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
        props.put("consumerKey", isNotBlank(consumerKey) ? HIDE : UNSET);
        props.put("consumerSecret", isNotBlank(consumerSecret) ? HIDE : UNSET);
        props.put("accessToken", isNotBlank(accessToken) ? HIDE : UNSET);
        props.put("accessTokenSecret", isNotBlank(accessTokenSecret) ? HIDE : UNSET);
        props.put("status", status);
        props.put("statuses", statuses);
        props.put("statusTemplate", statusTemplate);
    }
}
