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
import org.jreleaser.model.Constants;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.util.Env;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.announce.DiscourseAnnouncer.TYPE;
import static org.jreleaser.model.api.announce.DiscourseAnnouncer.DISCOURSE_API_KEY;
import static org.jreleaser.model.api.announce.DiscourseAnnouncer.DISCOURSE_USERNAME;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author shblue21
 * @since 1.3.0
 */
public final class DiscourseAnnouncer extends AbstractAnnouncer<DiscourseAnnouncer, org.jreleaser.model.api.announce.DiscourseAnnouncer> {
    private String host;
    private String apiKey;
    private String userName;

    private String categoryName;
    private String title;
    private String message;
    private String messageTemplate;

    private final org.jreleaser.model.api.announce.DiscourseAnnouncer immutable = new org.jreleaser.model.api.announce.DiscourseAnnouncer() {
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
        public String getUserName() {
            return userName;
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
            return message;
        }

        @Override
        public String getMessageTemplate() {
            return messageTemplate;
        }
        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isSnapshotSupported() {
            return DiscourseAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return active;
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
            return DiscourseAnnouncer.this.getPrefix();
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
        this.userName = merge(this.userName, source.userName);
        this.categoryName = merge(this.categoryName, source.categoryName);
        this.title = merge(this.title, source.title);
        this.message = merge(this.message, source.message);
        this.messageTemplate = merge(this.messageTemplate, source.messageTemplate);
    }

    public String getResolvedTitle(JReleaserContext context) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        return resolveTemplate(title, props);
    }

    public String getResolvedMessage(JReleaserContext context) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        return resolveTemplate(message, props);
    }

    public String getResolvedMessageTemplate(JReleaserContext context, Map<String, Object> extraProps) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        props.put(Constants.KEY_TAG_NAME, context.getModel().getRelease().getReleaser()
                .getEffectiveTagName(context.getModel()));
        props.putAll(extraProps);

        Path templatePath = context.getBasedir().resolve(messageTemplate);
        try {
            Reader reader = java.nio.file.Files.newBufferedReader(templatePath);
            return applyTemplate(reader, props);
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_reading_template",
                    context.relativizeToBasedir(templatePath)));
        }
    }

    public String getResolvedApiKey() {
        return Env.env(DISCOURSE_API_KEY, apiKey);
    }

    public String getResolvedUserName() {
        return Env.env(DISCOURSE_USERNAME, userName);
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("host", host);
        props.put("apiKey", isNotBlank(getResolvedApiKey()) ? HIDE : UNSET);
        props.put("userName", isNotBlank(getResolvedUserName()) ? HIDE : UNSET);
        props.put("categoryName", categoryName);
        props.put("title", title);
        props.put("message", message);
        props.put("messageTemplate", messageTemplate);
    }
}
