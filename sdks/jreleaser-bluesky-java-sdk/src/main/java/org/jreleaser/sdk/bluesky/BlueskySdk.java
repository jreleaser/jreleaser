/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.sdk.bluesky.api.BlueskyAPI;
import org.jreleaser.sdk.bluesky.api.CreateSessionRequest;
import org.jreleaser.sdk.bluesky.api.CreateSessionResponse;
import org.jreleaser.sdk.bluesky.api.CreateTextRecordRequest;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.RestAPIException;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Tom cools
 * @since 1.7.0
 */
public class BlueskySdk {
    private final JReleaserLogger logger;
    private final BlueskyAPI api;
    private final boolean dryrun;
    private final String handle;
    private final String password;

    private BlueskySdk(JReleaserLogger logger,
                       boolean dryrun,
                       String host,
                       String handle,
                       String password,
                       int connectTimeout,
                       int readTimeout) {
        requireNonNull(host, "'host' may not be null");

        this.logger = requireNonNull(logger, "'logger' must not be null");
        this.dryrun = dryrun;
        this.handle = requireNonNull(handle, "'handle' may not be null");
        this.password = requireNonNull(password, "'password' may not be null");

        ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

        api = ClientUtils.builder(logger, connectTimeout, readTimeout)
            .encoder(new FormEncoder(new JacksonEncoder(objectMapper)))
            .decoder(new JacksonDecoder(objectMapper))
            .target(BlueskyAPI.class, host);

        this.logger.debug(RB.$("workflow.dryrun"), dryrun);
    }

    public void skeet(List<String> statuses) throws BlueskyException {
        wrap(() -> {
            CreateSessionRequest sessionRequest = CreateSessionRequest.of(handle, password);
            CreateSessionResponse session = api.createSession(sessionRequest);

            for (String status : statuses) {
                CreateTextRecordRequest record = CreateTextRecordRequest.of(session.getDid(), status);
                api.createRecord(record, session.getAccessJwt());
            }
        });
    }

    private void wrap(Runnable runnable) throws BlueskyException {
        try {
            if (!dryrun) runnable.run();
        } catch (RestAPIException e) {
            logger.trace(e);
            throw new BlueskyException(RB.$("sdk.operation.failed", "Bluesky"), e);
        }
    }

    public static Builder builder(JReleaserLogger logger) {
        return new Builder(logger);
    }

    public static class Builder {
        private final JReleaserLogger logger;
        private boolean dryrun;
        private String host;
        private String handle;
        private String password;
        private int connectTimeout = 20;
        private int readTimeout = 60;

        private Builder(JReleaserLogger logger) {
            this.logger = requireNonNull(logger, "'logger' must not be null");
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
                logger,
                dryrun,
                host,
                handle,
                password,
                connectTimeout,
                readTimeout);
        }
    }

}