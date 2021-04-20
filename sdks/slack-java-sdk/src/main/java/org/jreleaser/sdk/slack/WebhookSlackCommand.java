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
package org.jreleaser.sdk.slack;

import org.jreleaser.util.JReleaserLogger;

import static org.jreleaser.sdk.slack.SlackWebhook.WEBHOOKS_URI;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class WebhookSlackCommand implements SlackCommand {
    protected final boolean dryrun;
    protected final SlackWebhook slack;
    private final String webhook;
    private final String message;

    private WebhookSlackCommand(JReleaserLogger logger,
                                int connectTimeout,
                                int readTimeout,
                                boolean dryrun,
                                String webhook,
                                String message) {
        this.slack = new SlackWebhook(logger, connectTimeout, readTimeout, dryrun);
        this.dryrun = dryrun;
        this.webhook = webhook.substring(WEBHOOKS_URI.length() + 1);
        this.message = message;
    }

    @Override
    public void execute() throws SlackException {
        slack.webhook(webhook, message);
    }

    public static Builder builder(JReleaserLogger logger) {
        return new Builder(logger);
    }

    public static class Builder extends AbstractSlackCommand.Builder<Builder> {
        private String webhook;
        private String message;

        protected Builder(JReleaserLogger logger) {
            super(logger);
        }

        public Builder webhook(String webhook) {
            this.webhook = webhook;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public WebhookSlackCommand build() {
            requireNonBlank(webhook, "'webhook' must not be blank");
            requireNonBlank(message, "'message' must not be blank");

            return new WebhookSlackCommand(
                logger,
                connectTimeout,
                readTimeout,
                dryrun,
                webhook,
                message);
        }
    }
}
