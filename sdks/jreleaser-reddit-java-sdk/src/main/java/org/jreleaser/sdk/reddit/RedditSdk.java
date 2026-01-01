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
package org.jreleaser.sdk.reddit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.auth.BasicAuthRequestInterceptor;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.reddit.api.AccessTokenResponse;
import org.jreleaser.sdk.reddit.api.RedditAPI;
import org.jreleaser.sdk.reddit.api.SubmissionRequest;
import org.jreleaser.sdk.reddit.api.SubmissionResponse;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Usman Shaikh
 * @since 1.21.0
 */
public class RedditSdk {
    private static final String REDDIT_BASE_URL = "https://www.reddit.com";
    private static final String REDDIT_OAUTH_URL = "https://oauth.reddit.com";
    
    private final JReleaserContext context;
    private final RedditAPI authApi;
    private final RedditAPI oauthApi;
    private final String clientId;
    private final String clientSecret;
    private final String username;
    private final String password;
    private final String userAgent;
    private final boolean dryrun;
    
    private String accessToken;

    private RedditSdk(JReleaserContext context,
                      String clientId,
                      String clientSecret,
                      String username,
                      String password,
                      int connectTimeout,
                      int readTimeout,
                      boolean dryrun,
                      String baseUrl,
                      String oauthUrl) {
        requireNonNull(context, "'context' must not be null");
        requireNonBlank(clientId, "'clientId' must not be blank");
        requireNonBlank(clientSecret, "'clientSecret' must not be blank");
        requireNonBlank(username, "'username' must not be blank");
        requireNonBlank(password, "'password' must not be blank");

        ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

        this.context = context;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.username = username;
        this.password = password;
        this.dryrun = dryrun;
        this.userAgent = String.format("JReleaser/%s by /u/%s", 
            context.getModel().getProject().getVersion(), username);

        // API for authentication
        this.authApi = ClientUtils.builder(context, connectTimeout, readTimeout)
            .encoder(new FormEncoder(new JacksonEncoder(objectMapper)))
            .decoder(new JacksonDecoder(objectMapper))
            .requestInterceptor(new BasicAuthRequestInterceptor(clientId, clientSecret))
            .requestInterceptor(template -> {
                template.header("User-Agent", userAgent);
                template.body("grant_type=password&username=" + username + "&password=" + password);
            })
            .target(RedditAPI.class, baseUrl);

        // API for authenticated requests
        this.oauthApi = ClientUtils.builder(context, connectTimeout, readTimeout)
            .encoder(new FormEncoder())
            .decoder(new JacksonDecoder(objectMapper))
            .requestInterceptor(template -> template.header("User-Agent", userAgent))
            .target(RedditAPI.class, oauthUrl);

        this.context.getLogger().debug(RB.$("workflow.dryrun"), dryrun);
    }

    public void submitTextPost(String subreddit, String title, String text) throws RedditSdkException {
        ensureAuthenticated();
        wrap(() -> {
            SubmissionRequest request = SubmissionRequest.forTextPost(subreddit, title, text);
            SubmissionResponse response = oauthApi.submit(accessToken, request);
            if (response.hasErrors()) {
                throw new RedditSdkException(RB.$("sdk.api.errors", "Reddit", response.getErrors()));
            }
        });
    }

    public void submitLinkPost(String subreddit, String title, String url) throws RedditSdkException {
        ensureAuthenticated();
        wrap(() -> {
            SubmissionRequest request = SubmissionRequest.forLinkPost(subreddit, title, url);
            SubmissionResponse response = oauthApi.submit(accessToken, request);
            if (response.hasErrors()) {
                throw new RedditSdkException(RB.$("sdk.api.errors", "Reddit", response.getErrors()));
            }
        });
    }

    private void ensureAuthenticated() throws RedditSdkException {
        if (accessToken == null) {
            authenticate();
        }
    }

    private void authenticate() throws RedditSdkException {
        wrap(() -> {
            AccessTokenResponse response = authApi.getAccessToken();
            this.accessToken = response.getAccessToken();
        });
    }

    private void wrap(Runnable runnable) throws RedditSdkException {
        try {
            if (!dryrun) {
                runnable.run();
            }
        } catch (RestAPIException e) {
            context.getLogger().trace(e);
            throw new RedditSdkException(RB.$("sdk.operation.failed", "Reddit"), e);
        }
    }

    public static Builder builder(JReleaserContext context) {
        return new Builder(context);
    }

    public static class Builder {
        private final JReleaserContext context;
        private String clientId;
        private String clientSecret;
        private String username;
        private String password;
        private int connectTimeout = 20;
        private int readTimeout = 60;
        private boolean dryrun;
        private String baseUrl;
        private String oauthUrl;

        private Builder(JReleaserContext context) {
            this.context = requireNonNull(context, "'context' must not be null");
        }

        public Builder clientId(String clientId) {
            this.clientId = requireNonBlank(clientId, "'clientId' must not be blank");
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            this.clientSecret = requireNonBlank(clientSecret, "'clientSecret' must not be blank");
            return this;
        }

        public Builder username(String username) {
            this.username = requireNonBlank(username, "'username' must not be blank");
            return this;
        }

        public Builder password(String password) {
            this.password = requireNonBlank(password, "'password' must not be blank");
            return this;
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder dryrun(boolean dryrun) {
            this.dryrun = dryrun;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder oauthUrl(String oauthUrl) {
            this.oauthUrl = oauthUrl;
            return this;
        }

        private void validate() {
            requireNonBlank(clientId, "'clientId' must not be blank");
            requireNonBlank(clientSecret, "'clientSecret' must not be blank");
            requireNonBlank(username, "'username' must not be blank");
            requireNonBlank(password, "'password' must not be blank");
            
            if (isBlank(baseUrl)) {
                this.baseUrl = REDDIT_BASE_URL;
            }
            if (isBlank(oauthUrl)) {
                this.oauthUrl = REDDIT_OAUTH_URL;
            }
        }

        public RedditSdk build() {
            validate();

            return new RedditSdk(context, clientId, clientSecret, username, password,
                connectTimeout, readTimeout, dryrun, baseUrl, oauthUrl);
        }
    }
}