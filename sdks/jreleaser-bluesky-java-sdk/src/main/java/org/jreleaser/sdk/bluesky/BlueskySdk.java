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
package org.jreleaser.sdk.bluesky;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.sdk.bluesky.api.BlueskyAPI;
import org.jreleaser.sdk.bluesky.api.CreateRecordResponse;
import org.jreleaser.sdk.bluesky.api.CreateSessionRequest;
import org.jreleaser.sdk.bluesky.api.CreateSessionResponse;
import org.jreleaser.sdk.bluesky.api.CreateTextRecordRequest;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.RestAPIException;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Tom Cools
 * @author Simon Verhoeven
 * @since 1.7.0
 */
public class BlueskySdk {
    private final JReleaserContext context;
    private final BlueskyAPI api;
    private final boolean dryrun;
    private final String handle;
    private final String password;
    private final BlueskyRecordFactory factory;

    private BlueskySdk(JReleaserContext context,
                       boolean dryrun,
                       String host,
                       String handle,
                       String password,
                       int connectTimeout,
                       int readTimeout) {
        this.context = requireNonNull(context, "'context' must not be null");
        requireNonBlank(host, "'host' must not be blank");
        this.handle = requireNonBlank(handle, "'handle' must not be blank");
        this.password = requireNonBlank(password, "'password' must not be blank");
        this.dryrun = dryrun;

        ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

        api = ClientUtils.builder(context, connectTimeout, readTimeout)
            .encoder(new FormEncoder(new JacksonEncoder(objectMapper)))
            .decoder(new JacksonDecoder(objectMapper))
            .target(BlueskyAPI.class, host);

        factory = new BlueskyRecordFactory(api);

        context.getLogger().debug(RB.$("workflow.dryrun"), dryrun);
    }

    public void skeet(List<String> statuses) throws BlueskyException {
        wrap(() -> {
            CreateSessionResponse session = createSession();
            String identifier = session.getDid();

            // To skeet a thread, the first and previous statuses must be added to a new status.
            CreateTextRecordRequest firstStatusRequest = factory.textRecord(identifier, statuses.get(0));
            CreateRecordResponse firstStatus = api.createRecord(firstStatusRequest, session.getAccessJwt());
            CreateRecordResponse previousStatus = firstStatus;

            for (int i = 1; i < statuses.size(); i++) {
                String status = statuses.get(i);
                CreateTextRecordRequest nextStatusRequest = factory.textRecord(identifier, status, firstStatus, previousStatus);
                previousStatus = api.createRecord(nextStatusRequest, session.getAccessJwt());
            }
        });
    }

    private CreateSessionResponse createSession() {
        CreateSessionRequest sessionRequest = CreateSessionRequest.of(handle, password);
        return api.createSession(sessionRequest);
    }

    private void wrap(Runnable runnable) throws BlueskyException {
        try {
            if (!dryrun) runnable.run();
        } catch (RestAPIException e) {
            context.getLogger().trace(e);
            throw new BlueskyException(RB.$("sdk.operation.failed", "Bluesky"), e);
        }
    }

    public static Builder builder(JReleaserContext context) {
        return new Builder(context);
    }

    public static class Builder {
        private final JReleaserContext context;
        private boolean dryrun;
        private String host;
        private String handle;
        private String password;
        private int connectTimeout = 20;
        private int readTimeout = 60;

        private Builder(JReleaserContext context) {
            this.context = requireNonNull(context, "'context' must not be null");
        }

        public Builder dryrun(boolean dryrun) {
            this.dryrun = dryrun;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder handle(String handle) {
            this.handle = requireNonNull(handle, "'handle' must not be null");
            return this;
        }

        public Builder password(String password) {
            this.password = requireNonNull(password, "'password' must not be null");
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

        private void validate() {
            requireNonBlank(host, "'host' must not be blank");
            requireNonBlank(handle, "'handle' must not be blank");
            requireNonBlank(password, "'password' must not be blank");
        }

        public BlueskySdk build() {
            validate();

            return new BlueskySdk(
                context,
                dryrun,
                host,
                handle,
                password,
                connectTimeout,
                readTimeout);
        }
    }

}