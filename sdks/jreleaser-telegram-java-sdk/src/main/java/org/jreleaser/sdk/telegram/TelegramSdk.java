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
package org.jreleaser.sdk.telegram;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext;
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
    private final JReleaserContext context;
    private final TelegramAPI api;
    private final boolean dryrun;

    private TelegramSdk(JReleaserContext context,
                        String apiHost,
                        int connectTimeout,
                        int readTimeout,
                        boolean dryrun) {
        requireNonNull(context, "'context' must not be null");
        requireNonBlank(apiHost, "'apiHost' must not be blank");

        this.context = context;
        this.dryrun = dryrun;
        this.api = ClientUtils.builder(context, connectTimeout, readTimeout)
            .target(TelegramAPI.class, apiHost);

        this.context.getLogger().debug(RB.$("workflow.dryrun"), dryrun);
    }

    public void sendMessage(String chatId, String message) throws TelegramException {
        Message payload = Message.of(chatId, message);
        context.getLogger().debug("telegram.message: " + payload);
        wrap(() -> api.sendMessage(payload));
    }

    private void wrap(Runnable runnable) throws TelegramException {
        try {
            if (!dryrun) runnable.run();
        } catch (RestAPIException e) {
            context.getLogger().trace(e.getStatus() + ": " + e.getReason());
            context.getLogger().trace(e);
            throw new TelegramException(RB.$("sdk.operation.failed", "Telegram"), e);
        }
    }

    public static Builder builder(JReleaserContext context) {
        return new Builder(context);
    }

    public static class Builder {
        private final JReleaserContext context;
        private boolean dryrun;
        private String apiHost;
        private String token;
        private int connectTimeout = 20;
        private int readTimeout = 60;

        private Builder(JReleaserContext context) {
            this.context = requireNonNull(context, "'context' must not be null");
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
                context,
                apiHost,
                connectTimeout,
                readTimeout,
                dryrun);
        }
    }
}
