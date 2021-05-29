/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import org.jreleaser.util.Env;

import java.util.Map;

import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.UNSET;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Twitter extends AbstractAnnouncer {
    public static final String NAME = "twitter";
    public static final String TWITTER_CONSUMER_KEY = "TWITTER_CONSUMER_KEY";
    public static final String TWITTER_CONSUMER_SECRET = "TWITTER_CONSUMER_SECRET";
    public static final String TWITTER_ACCESS_TOKEN = "TWITTER_ACCESS_TOKEN";
    public static final String TWITTER_ACCESS_TOKEN_SECRET = "TWITTER_ACCESS_TOKEN_SECRET";

    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessTokenSecret;
    private String status;

    public Twitter() {
        super(NAME);
    }

    void setAll(Twitter twitter) {
        super.setAll(twitter);
        this.consumerKey = twitter.consumerKey;
        this.consumerSecret = twitter.consumerSecret;
        this.accessToken = twitter.accessToken;
        this.accessTokenSecret = twitter.accessTokenSecret;
        this.status = twitter.status;
    }

    public String getResolvedStatus(JReleaserContext context) {
        Map<String, Object> props = context.props();
        applyTemplates(props, getResolvedExtraProperties());
        context.getModel().getRelease().getGitService().fillProps(props, context.getModel());
        return applyTemplate(status, props);
    }

    public String getResolvedConsumerKey() {
        return Env.resolve(TWITTER_CONSUMER_KEY, consumerKey);
    }

    public String getResolvedConsumerSecret() {
        return Env.resolve(TWITTER_CONSUMER_SECRET, consumerSecret);
    }

    public String getResolvedAccessToken() {
        return Env.resolve(TWITTER_ACCESS_TOKEN, accessToken);
    }

    public String getResolvedAccessTokenSecret() {
        return Env.resolve(TWITTER_ACCESS_TOKEN_SECRET, accessTokenSecret);
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

    @Override
    protected void asMap(Map<String, Object> props) {
        props.put("consumerKey", isNotBlank(getResolvedConsumerKey()) ? HIDE : UNSET);
        props.put("consumerSecret", isNotBlank(getResolvedConsumerSecret()) ? HIDE : UNSET);
        props.put("accessToken", isNotBlank(getResolvedAccessToken()) ? HIDE : UNSET);
        props.put("accessTokenSecret", isNotBlank(getResolvedAccessTokenSecret()) ? HIDE : UNSET);
        props.put("status", status);
    }
}
