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
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.util.Env;

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
import static org.jreleaser.model.api.announce.TwitterAnnouncer.TWITTER_ACCESS_TOKEN;
import static org.jreleaser.model.api.announce.TwitterAnnouncer.TWITTER_ACCESS_TOKEN_SECRET;
import static org.jreleaser.model.api.announce.TwitterAnnouncer.TWITTER_CONSUMER_KEY;
import static org.jreleaser.model.api.announce.TwitterAnnouncer.TWITTER_CONSUMER_SECRET;
import static org.jreleaser.model.api.announce.TwitterAnnouncer.TYPE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class TwitterAnnouncer extends AbstractAnnouncer<TwitterAnnouncer, org.jreleaser.model.api.announce.TwitterAnnouncer> {
    private final List<String> statuses = new ArrayList<>();
    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessTokenSecret;
    private String status;
    private String statusTemplate;

    private final org.jreleaser.model.api.announce.TwitterAnnouncer immutable = new org.jreleaser.model.api.announce.TwitterAnnouncer() {
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
            return name;
        }

        @Override
        public boolean isSnapshotSupported() {
            return TwitterAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return active;
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
            return TwitterAnnouncer.this.getPrefix();
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

    public String getResolvedStatus(JReleaserContext context) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        context.getModel().getRelease().getReleaser().fillProps(props, context.getModel());
        return resolveTemplate(status, props);
    }

    public String getResolvedStatusTemplate(JReleaserContext context, Map<String, Object> extraProps) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        props.put(KEY_TAG_NAME, context.getModel().getRelease().getReleaser()
            .getEffectiveTagName(context.getModel()));
        props.putAll(extraProps);

        Path templatePath = context.getBasedir().resolve(statusTemplate);
        try {
            Reader reader = java.nio.file.Files.newBufferedReader(templatePath);
            return applyTemplate(reader, props);
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_reading_template",
                context.relativizeToBasedir(templatePath)));
        }
    }

    public String getResolvedConsumerKey() {
        return Env.env(TWITTER_CONSUMER_KEY, consumerKey);
    }

    public String getResolvedConsumerSecret() {
        return Env.env(TWITTER_CONSUMER_SECRET, consumerSecret);
    }

    public String getResolvedAccessToken() {
        return Env.env(TWITTER_ACCESS_TOKEN, accessToken);
    }

    public String getResolvedAccessTokenSecret() {
        return Env.env(TWITTER_ACCESS_TOKEN_SECRET, accessTokenSecret);
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
        props.put("consumerKey", isNotBlank(getResolvedConsumerKey()) ? HIDE : UNSET);
        props.put("consumerSecret", isNotBlank(getResolvedConsumerSecret()) ? HIDE : UNSET);
        props.put("accessToken", isNotBlank(getResolvedAccessToken()) ? HIDE : UNSET);
        props.put("accessTokenSecret", isNotBlank(getResolvedAccessTokenSecret()) ? HIDE : UNSET);
        props.put("status", status);
        props.put("statuses", statuses);
        props.put("statusTemplate", statusTemplate);
    }
}
