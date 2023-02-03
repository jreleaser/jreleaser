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
package org.jreleaser.sdk.telegram;

import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.telegram.api.Message;
import org.jreleaser.sdk.telegram.api.TelegramAPI;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public class TelegramSdk {
    private final JReleaserLogger logger;
    private final TelegramAPI api;
    private final boolean dryrun;

    private TelegramSdk(JReleaserLogger logger,
                        String apiHost,
                        int connectTimeout,
                        int readTimeout,
                        boolean dryrun) {
        requireNonNull(logger, "'logger' must not be null");
        requireNonBlank(apiHost, "'apiHost' must not be blank");

        this.logger = logger;
        this.dryrun = dryrun;
        this.api = ClientUtils.builder(logger, connectTimeout, readTimeout)
            .target(TelegramAPI.class, apiHost);

        this.logger.debug(RB.$("workflow.dryrun"), dryrun);
    }

    public void sendMessage(String chatId, String message) throws TelegramException {
        Message payload = Message.of(chatId, message);
        logger.debug("telegram.message: " + payload);
        wrap(() -> api.sendMessage(payload));
    }

    private void wrap(Runnable runnable) throws TelegramException {
        try {
            if (!dryrun) runnable.run();
        } catch (RestAPIException e) {
            logger.trace(e.getStatus() + ": " + e.getReason());
            logger.trace(e);
            throw new TelegramException(RB.$("sdk.operation.failed", "Telegram"), e);
        }
    }

    public static Builder builder(JReleaserLogger logger) {
        return new Builder(logger);
    }

    public static class Builder {
        private final JReleaserLogger logger;
        private boolean dryrun;
        private String apiHost;
        private String token;
        private int connectTimeout = 20;
        private int readTimeout = 60;

        private Builder(JReleaserLogger logger) {
            this.logger = requireNonNull(logger, "'logger' must not be null");
        }

        public Builder dryrun(boolean dryrun) {
            this.dryrun = dryrun;
            return this;
        }

        public Builder apiHost(String apiHost) {
            this.apiHost = requireNonBlank(apiHost, "'apiHost' must not be blank").trim();
            return this;
        }

        public Builder token(String token) {
            this.token = requireNonBlank(token, "'token' must not be blank").trim();
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
            requireNonBlank(token, "'token' must not be blank");
            if (isBlank(apiHost)) {
                apiHost("https://api.telegram.org/bot");
            }

            if (!apiHost.endsWith(token)) {
                apiHost += token;
            }
        }

        public TelegramSdk build() {
            validate();

            return new TelegramSdk(
                logger,
                apiHost,
                connectTimeout,
                readTimeout,
                dryrun);
        }
    }
}
