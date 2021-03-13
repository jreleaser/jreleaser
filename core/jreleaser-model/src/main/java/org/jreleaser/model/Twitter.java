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

import java.io.StringReader;
import java.util.Map;

import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Twitter extends AbstractAnnouncer {
    public static final String NAME = "twitter";

    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessTokenSecret;
    private String status = "{{projectNameCapitalized}} {{tagName}} has been released! {{latestReleaseUrl}}";

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

    public String getResolvedStatus(JReleaserModel model) {
        return applyTemplate(new StringReader(status), model.newContext());
    }

    public String getResolvedConsumerKey() {
        if (isNotBlank(consumerKey)) {
            return consumerKey;
        }
        return System.getenv("TWITTER_CONSUMER_KEY");
    }

    public String getResolvedConsumerSecret() {
        if (isNotBlank(consumerSecret)) {
            return consumerSecret;
        }
        return System.getenv("TWITTER_CONSUMER_SECRET");
    }

    public String getResolvedAccessToken() {
        if (isNotBlank(accessToken)) {
            return accessToken;
        }
        return System.getenv("TWITTER_ACCESS_TOKEN");
    }

    public String getResolvedAccessTokenSecret() {
        if (isNotBlank(accessTokenSecret)) {
            return accessTokenSecret;
        }
        return System.getenv("TWITTER_ACCESS_TOKEN_SECRET");
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
        props.put("consumerKey", isNotBlank(getResolvedConsumerKey()) ? "************" : "**unset**");
        props.put("consumerSecret", isNotBlank(getResolvedConsumerSecret()) ? "************" : "**unset**");
        props.put("accessToken", isNotBlank(getResolvedAccessToken()) ? "************" : "**unset**");
        props.put("accessTokenSecret", isNotBlank(getResolvedAccessTokenSecret()) ? "************" : "**unset**");
        props.put("status", status);
    }
}
