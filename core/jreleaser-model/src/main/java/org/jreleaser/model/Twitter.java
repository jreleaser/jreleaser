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
package org.jreleaser.model;

import org.jreleaser.bundle.RB;
import org.jreleaser.util.Env;
import org.jreleaser.util.JReleaserException;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.KEY_TAG_NAME;
import static org.jreleaser.util.Constants.UNSET;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Twitter extends AbstractAnnouncer<Twitter> {
    public static final String NAME = "twitter";
    public static final String TWITTER_CONSUMER_KEY = "TWITTER_CONSUMER_KEY";
    public static final String TWITTER_CONSUMER_SECRET = "TWITTER_CONSUMER_SECRET";
    public static final String TWITTER_ACCESS_TOKEN = "TWITTER_ACCESS_TOKEN";
    public static final String TWITTER_ACCESS_TOKEN_SECRET = "TWITTER_ACCESS_TOKEN_SECRET";
    private final List<String> statuses = new ArrayList<>();
    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessTokenSecret;
    private String status;
    private String statusTemplate;

    public Twitter() {
        super(NAME);
    }

    @Override
    public void merge(Twitter twitter) {
        freezeCheck();
        super.merge(twitter);
        this.consumerKey = merge(this.consumerKey, twitter.consumerKey);
        this.consumerSecret = merge(this.consumerSecret, twitter.consumerSecret);
        this.accessToken = merge(this.accessToken, twitter.accessToken);
        this.accessTokenSecret = merge(this.accessTokenSecret, twitter.accessTokenSecret);
        this.status = merge(this.status, twitter.status);
        setStatuses(merge(this.statuses, twitter.statuses));
        this.statusTemplate = merge(this.statusTemplate, twitter.statusTemplate);
    }

    public String getResolvedStatus(JReleaserContext context) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        context.getModel().getRelease().getGitService().fillProps(props, context.getModel());
        return resolveTemplate(status, props);
    }

    public String getResolvedStatusTemplate(JReleaserContext context, Map<String, Object> extraProps) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        props.put(KEY_TAG_NAME, context.getModel().getRelease().getGitService()
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
        freezeCheck();
        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        freezeCheck();
        this.consumerSecret = consumerSecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        freezeCheck();
        this.accessToken = accessToken;
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public void setAccessTokenSecret(String accessTokenSecret) {
        freezeCheck();
        this.accessTokenSecret = accessTokenSecret;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        freezeCheck();
        this.status = status;
    }

    public List<String> getStatuses() {
        return freezeWrap(statuses);
    }

    public void setStatuses(List<String> statuses) {
        freezeCheck();
        this.statuses.clear();
        this.statuses.addAll(statuses);
    }

    public String getStatusTemplate() {
        return statusTemplate;
    }

    public void setStatusTemplate(String statusTemplate) {
        freezeCheck();
        this.statusTemplate = statusTemplate;
    }

    @Override
    protected void asMap(Map<String, Object> props, boolean full) {
        props.put("consumerKey", isNotBlank(getResolvedConsumerKey()) ? HIDE : UNSET);
        props.put("consumerSecret", isNotBlank(getResolvedConsumerSecret()) ? HIDE : UNSET);
        props.put("accessToken", isNotBlank(getResolvedAccessToken()) ? HIDE : UNSET);
        props.put("accessTokenSecret", isNotBlank(getResolvedAccessTokenSecret()) ? HIDE : UNSET);
        props.put("status", status);
        props.put("statuses", statuses);
        props.put("statusTemplate", statusTemplate);
    }
}
