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
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.KEY_PREVIOUS_TAG_NAME;
import static org.jreleaser.model.Constants.KEY_TAG_NAME;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.api.announce.RedditAnnouncer.TYPE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Usman Shaikh
 * @since 1.21.0
 */
public final class RedditAnnouncer extends AbstractAnnouncer<RedditAnnouncer, org.jreleaser.model.api.announce.RedditAnnouncer> {
    private static final long serialVersionUID = 4379635430074452143L;

    private String clientId;
    private String clientSecret;
    private String username;
    private String password;
    private String subreddit;
    private String title;
    private String text;
    private String textTemplate;
    private String url;
    private org.jreleaser.model.api.announce.RedditAnnouncer.SubmissionType submissionType =
        org.jreleaser.model.api.announce.RedditAnnouncer.SubmissionType.SELF;

    @JsonIgnore
    private final org.jreleaser.model.api.announce.RedditAnnouncer immutable = new org.jreleaser.model.api.announce.RedditAnnouncer() {
        private static final long serialVersionUID = -1316280067796370506L;

        @Override
        public String getType() {
            return org.jreleaser.model.api.announce.RedditAnnouncer.TYPE;
        }

        @Override
        public String getClientId() {
            return clientId;
        }

        @Override
        public String getClientSecret() {
            return clientSecret;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getSubreddit() {
            return subreddit;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public String getTextTemplate() {
            return textTemplate;
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public SubmissionType getSubmissionType() {
            return submissionType;
        }

        @Override
        public String getName() {
            return RedditAnnouncer.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return RedditAnnouncer.this.isSnapshotSupported();
        }

        @Override
        public Active getActive() {
            return RedditAnnouncer.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return RedditAnnouncer.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(RedditAnnouncer.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return RedditAnnouncer.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(RedditAnnouncer.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return RedditAnnouncer.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return RedditAnnouncer.this.getReadTimeout();
        }
    };

    public RedditAnnouncer() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.announce.RedditAnnouncer asImmutable() {
        return immutable;
    }

    @Override
    public void merge(RedditAnnouncer source) {
        super.merge(source);
        this.clientId = merge(this.clientId, source.clientId);
        this.clientSecret = merge(this.clientSecret, source.clientSecret);
        this.username = merge(this.username, source.username);
        this.password = merge(this.password, source.password);
        this.subreddit = merge(this.subreddit, source.subreddit);
        this.title = merge(this.title, source.title);
        this.text = merge(this.text, source.text);
        this.textTemplate = merge(this.textTemplate, source.textTemplate);
        this.url = merge(this.url, source.url);
        this.submissionType = merge(this.submissionType, source.submissionType);
    }

    public String getResolvedTitle(JReleaserContext context) {
        TemplateContext props = context.fullProps();
        applyTemplates(props, resolvedExtraProperties());
        return resolveTemplate(title, props);
    }

    public String getResolvedText(JReleaserContext context) {
        TemplateContext props = context.fullProps();
        applyTemplates(props, resolvedExtraProperties());
        return resolveTemplate(text, props);
    }

    public String getResolvedTextTemplate(JReleaserContext context, TemplateContext extraProps) {
        TemplateContext props = context.fullProps();
        context.getChangelog().apply(props);
        applyTemplates(props, resolvedExtraProperties());
        props.set(KEY_TAG_NAME, context.getModel().getRelease().getReleaser().getEffectiveTagName(context.getModel()));
        props.set(KEY_PREVIOUS_TAG_NAME, context.getModel().getRelease().getReleaser().getResolvedPreviousTagName(context.getModel()));
        props.setAll(extraProps);

        Path templatePath = context.getBasedir().resolve(textTemplate);
        try {
            Reader reader = java.nio.file.Files.newBufferedReader(templatePath);
            return applyTemplate(reader, props);
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_reading_template",
                context.relativizeToBasedir(templatePath)));
        }
    }

    public String getResolvedUrl(JReleaserContext context) {
        TemplateContext props = context.fullProps();
        applyTemplates(props, resolvedExtraProperties());
        return resolveTemplate(url, props);
    }


    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("clientId", isNotBlank(getClientId()) ? HIDE : UNSET);
        props.put("clientSecret", isNotBlank(getClientSecret()) ? HIDE : UNSET);
        props.put("username", isNotBlank(getUsername()) ? HIDE : UNSET);
        props.put("password", isNotBlank(getPassword()) ? HIDE : UNSET);
        props.put("subreddit", subreddit);
        props.put("submissionType", submissionType);
        props.put("title", title);
        props.put("text", text);
        props.put("textTemplate", textTemplate);
        props.put("url", url);
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTextTemplate() {
        return textTemplate;
    }

    public void setTextTemplate(String textTemplate) {
        this.textTemplate = textTemplate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public org.jreleaser.model.api.announce.RedditAnnouncer.SubmissionType getSubmissionType() {
        return submissionType;
    }

    public void setSubmissionType(org.jreleaser.model.api.announce.RedditAnnouncer.SubmissionType submissionType) {
        this.submissionType = submissionType;
    }
}