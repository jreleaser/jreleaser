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
package org.jreleaser.sdk.mastodon;

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
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.mastodon.api.MastodonAPI;
import org.jreleaser.sdk.mastodon.api.Status;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public class MastodonSdk {
    private static final String API_V1 = "/api/v1";
    private final JReleaserContext context;
    private final MastodonAPI api;
    private final boolean dryrun;

    private MastodonSdk(JReleaserContext context,
                        String host,
                        String accessToken,
                        int connectTimeout,
                        int readTimeout,
                        boolean dryrun) {
        requireNonNull(context, "'context' must not be null");
        requireNonBlank(host, "'host' must not be blank");
        requireNonBlank(accessToken, "'accessToken' must not be blank");

        if (!host.endsWith(API_V1)) {
            if (host.endsWith("/")) {
                host = host.substring(0, host.length() - 1);
            }
            host += API_V1;
        }

        ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

        this.context = context;
        this.dryrun = dryrun;
        this.api = ClientUtils.builder(context, connectTimeout, readTimeout)
            .encoder(new FormEncoder(new JacksonEncoder(objectMapper)))
            .decoder(new JacksonDecoder(objectMapper))
            .requestInterceptor(template -> template.header("Authorization", String.format("Bearer %s", accessToken)))
            .target(MastodonAPI.class, host);

        this.context.getLogger().debug(RB.$("workflow.dryrun"), dryrun);
    }

    public void toot(List<String> statuses) throws MastodonException {
        wrap(() -> {
            Status status = api.status(Status.of(statuses.get(0)));
            for (int i = 1; i < statuses.size(); i++) {
                status = api.status(Status.of(statuses.get(i), status.getId()));
            }
        });
    }

    private void wrap(Runnable runnable) throws MastodonException {
        try {
            if (!dryrun) runnable.run();
        } catch (RestAPIException e) {
            context.getLogger().trace(e);
            throw new MastodonException(RB.$("sdk.operation.failed", "Mastodon"), e);
        }
    }

    public static Builder builder(JReleaserContext context) {
        return new Builder(context);
    }

    public static class Builder {
        private final JReleaserContext context;
        private boolean dryrun;
        private String accessToken;
        private String host;
        private int connectTimeout = 20;
        private int readTimeout = 60;

        private Builder(JReleaserContext context) {
            this.context = requireNonNull(context, "'context' must not be null");
        }

        public Builder dryrun(boolean dryrun) {
            this.dryrun = dryrun;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = requireNonBlank(accessToken, "'accessToken' must not be blank").trim();
            return this;
        }

        public Builder host(String host) {
            this.host = requireNonBlank(host, "'host' must not be blank").trim();
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
            requireNonBlank(accessToken, "'accessToken' must not be blank");
        }

        public MastodonSdk build() {
            validate();

            return new MastodonSdk(
                context,
                host,
                accessToken,
                connectTimeout,
                readTimeout,
                dryrun);
        }
    }
}
