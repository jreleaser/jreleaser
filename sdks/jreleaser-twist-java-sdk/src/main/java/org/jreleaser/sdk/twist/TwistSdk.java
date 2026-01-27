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
package org.jreleaser.sdk.twist;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.twist.api.CommentRequest;
import org.jreleaser.sdk.twist.api.ThreadRequest;
import org.jreleaser.sdk.twist.api.TwistAPI;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Usman Shaikh
 * @since 1.23.0
 */
public class TwistSdk {
    private static final String TWIST_API_URL = "https://api.twist.com";

    private final JReleaserContext context;
    private final TwistAPI api;
    private final String accessToken;
    private final boolean dryrun;

    private TwistSdk(JReleaserContext context,
                     String apiHost,
                     String accessToken,
                     int connectTimeout,
                     int readTimeout,
                     boolean dryrun) {
        requireNonNull(context, "'context' must not be null");
        requireNonBlank(apiHost, "'apiHost' must not be blank");
        requireNonBlank(accessToken, "'accessToken' must not be blank");

        ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

        this.context = context;
        this.accessToken = accessToken;
        this.dryrun = dryrun;

        this.api = ClientUtils.builder(context, connectTimeout, readTimeout)
            .encoder(new JacksonEncoder(objectMapper))
            .decoder(new JacksonDecoder(objectMapper))
            .requestInterceptor(template -> template.header("Authorization", String.format("Bearer %s", accessToken)))
            .target(TwistAPI.class, apiHost);

        this.context.getLogger().debug(RB.$("workflow.dryrun"), dryrun);
    }

    public void createThread(String channelId, String title, String content) throws TwistSdkException {
        wrap(() -> {
            ThreadRequest request = new ThreadRequest(Integer.parseInt(channelId), title, content);
            api.createThread(request);
        });
    }

    public void createComment(String threadId, String content) throws TwistSdkException {
        wrap(() -> {
            CommentRequest request = new CommentRequest(Integer.parseInt(threadId), content);
            api.createComment(request);
        });
    }

    private void wrap(Runnable runnable) throws TwistSdkException {
        try {
            if (!dryrun) {
                runnable.run();
            }
        } catch (RestAPIException e) {
            context.getLogger().trace(e);
            throw new TwistSdkException(RB.$("sdk.operation.failed", "Twist"), e);
        }
    }

    public static Builder builder(JReleaserContext context) {
        return new Builder(context);
    }

    public static class Builder {
        private final JReleaserContext context;
        private String apiHost;
        private String accessToken;
        private int connectTimeout = 20;
        private int readTimeout = 60;
        private boolean dryrun;

        private Builder(JReleaserContext context) {
            this.context = requireNonNull(context, "'context' must not be null");
        }

        public Builder apiHost(String apiHost) {
            this.apiHost = requireNonBlank(apiHost, "'apiHost' must not be blank");
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = requireNonBlank(accessToken, "'accessToken' must not be blank");
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

        private void validate() {
            requireNonBlank(accessToken, "'accessToken' must not be blank");

            if (isBlank(apiHost)) {
                this.apiHost = TWIST_API_URL;
            }
        }

        public TwistSdk build() {
            validate();

            return new TwistSdk(context, apiHost, accessToken,
                connectTimeout, readTimeout, dryrun);
        }
    }
}
