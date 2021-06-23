/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import feign.form.FormEncoder;
import feign.jackson.JacksonEncoder;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.mastodon.api.MastodonAPI;
import org.jreleaser.sdk.mastodon.api.Status;
import org.jreleaser.util.JReleaserLogger;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public class MastodonSdk {
    private final JReleaserLogger logger;
    private final MastodonAPI api;
    private final boolean dryrun;
    private static final String API_V1 = "/api/v1";

    private MastodonSdk(JReleaserLogger logger,
                        String host,
                        String accessToken,
                        int connectTimeout,
                        int readTimeout,
                        boolean dryrun) {
        requireNonNull(logger, "'logger' must not be null");
        requireNonBlank(host, "'host' must not be blank");
        requireNonBlank(accessToken, "'accessToken' must not be blank");

        if (!host.endsWith(API_V1)) {
            if (host.endsWith("/")) {
                host = host.substring(0, host.length() - 1);
            }
            host += API_V1;
        }

        this.logger = logger;
        this.dryrun = dryrun;
        this.api = ClientUtils.builder(logger, connectTimeout, readTimeout)
            .encoder(new FormEncoder(new JacksonEncoder()))
            .requestInterceptor(template -> template.header("Authorization", String.format("Bearer %s", accessToken)))
            .target(MastodonAPI.class, host);

        this.logger.debug("Mastodon dryrun set to {}", dryrun);
    }

    public void status(String status) throws MastodonException {
        Status payload = Status.of(status);
        logger.debug("mastodon.status: " + payload.toString());
        wrap(() -> api.status(payload));
    }

    private void wrap(Runnable runnable) throws MastodonException {
        try {
            if (!dryrun) runnable.run();
        } catch (RestAPIException e) {
            logger.trace(e);
            throw new MastodonException("Mastodon operation failed", e);
        }
    }

    public static Builder builder(JReleaserLogger logger) {
        return new Builder(logger);
    }

    static class Builder {
        private final JReleaserLogger logger;
        private boolean dryrun;
        private String accessToken;
        private String host;
        private int connectTimeout = 20;
        private int readTimeout = 60;

        private Builder(JReleaserLogger logger) {
            this.logger = requireNonNull(logger, "'logger' must not be null");
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
                logger,
                host,
                accessToken,
                connectTimeout,
                readTimeout,
                dryrun);
        }
    }
}
