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
package org.jreleaser.sdk.zulip;

import feign.auth.BasicAuthRequestInterceptor;
import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.zulip.api.Message;
import org.jreleaser.sdk.zulip.api.ZulipAPI;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ZulipSdk {
    private final JReleaserLogger logger;
    private final ZulipAPI api;
    private final boolean dryrun;

    private ZulipSdk(JReleaserLogger logger,
                     String apiHost,
                     String account,
                     String apiKey,
                     int connectTimeout,
                     int readTimeout,
                     boolean dryrun) {
        requireNonNull(logger, "'logger' must not be null");
        requireNonBlank(apiHost, "'apiHost' must not be blank");
        requireNonBlank(account, "'account' must not be blank");
        requireNonBlank(apiKey, "'apiKey' must not be blank");

        this.logger = logger;
        this.dryrun = dryrun;
        this.api = ClientUtils.builder(logger, connectTimeout, readTimeout)
            .requestInterceptor(new BasicAuthRequestInterceptor(account, apiKey))
            .target(ZulipAPI.class, apiHost);

        this.logger.debug(RB.$("workflow.dryrun"), dryrun);
    }

    public void message(String channel,
                        String subject,
                        String message) throws ZulipException {
        Message payload = Message.of(channel, subject, message);
        logger.debug("zulip.message: " + payload);
        wrap(() -> api.message(payload));
    }

    private void wrap(Runnable runnable) throws ZulipException {
        try {
            if (!dryrun) runnable.run();
        } catch (RestAPIException e) {
            logger.trace(e);
            throw new ZulipException(RB.$("sdk.operation.failed", "Zulip"), e);
        }
    }

    public static Builder builder(JReleaserLogger logger) {
        return new Builder(logger);
    }

    public static class Builder {
        private final JReleaserLogger logger;
        private boolean dryrun;
        private String account;
        private String apiKey;
        private String apiHost;
        private int connectTimeout = 20;
        private int readTimeout = 60;

        private Builder(JReleaserLogger logger) {
            this.logger = requireNonNull(logger, "'logger' must not be null");
        }

        public Builder dryrun(boolean dryrun) {
            this.dryrun = dryrun;
            return this;
        }

        public Builder account(String account) {
            this.account = requireNonBlank(account, "'account' must not be blank").trim();
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = requireNonBlank(apiKey, "'apiKey' must not be blank").trim();
            return this;
        }

        public Builder apiHost(String apiHost) {
            this.apiHost = requireNonBlank(apiHost, "'apiHost' must not be blank").trim();
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
            requireNonBlank(apiHost, "'apiHost' must not be blank");
            requireNonBlank(account, "'account' must not be blank");
            requireNonBlank(apiKey, "'apiKey' must not be blank");
        }

        public ZulipSdk build() {
            validate();

            return new ZulipSdk(
                logger,
                apiHost,
                account,
                apiKey,
                connectTimeout,
                readTimeout,
                dryrun);
        }
    }
}
