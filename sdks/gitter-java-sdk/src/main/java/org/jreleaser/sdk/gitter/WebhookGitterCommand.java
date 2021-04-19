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
package org.jreleaser.sdk.gitter;

import org.jreleaser.util.JReleaserLogger;

import static org.jreleaser.sdk.gitter.Gitter.WEBHOOKS_URI;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class WebhookGitterCommand extends AbstractGitterCommand {
    private final String webhook;
    private final String message;

    private WebhookGitterCommand(JReleaserLogger logger,
                                 int connectTimeout,
                                 int readTimeout,
                                 boolean dryrun,
                                 String webhook,
                                 String message) {
        super(logger, connectTimeout, readTimeout, dryrun);
        this.webhook = webhook.substring(WEBHOOKS_URI.length() + 1);
        this.message = message;
    }

    @Override
    public void execute() throws GitterException {
        gitter.webhook(webhook, message);
    }

    public static Builder builder(JReleaserLogger logger) {
        return new Builder(logger);
    }

    public static class Builder extends AbstractGitterCommand.Builder<Builder> {
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

        public WebhookGitterCommand build() {
            requireNonBlank(webhook, "'webhook' must not be blank");
            requireNonBlank(message, "'message' must not be blank");

            return new WebhookGitterCommand(
                logger,
                connectTimeout,
                readTimeout,
                dryrun,
                webhook,
                message);
        }
    }
}
