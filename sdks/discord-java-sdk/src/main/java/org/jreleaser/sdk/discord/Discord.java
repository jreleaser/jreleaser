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
package org.jreleaser.sdk.discord;

import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.jreleaser.sdk.discord.api.DiscordAPIException;
import org.jreleaser.sdk.discord.api.Message;
import org.jreleaser.sdk.discord.api.WebhookDiscordAPI;
import org.jreleaser.util.JReleaserLogger;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class Discord {
    public static final String WEBHOOKS_URI = "https://discord.com/api/webhooks";

    private final JReleaserLogger logger;
    private final WebhookDiscordAPI api;
    private final boolean dryrun;

    public Discord(JReleaserLogger logger,
                   int connectTimeout,
                   int readTimeout,
                   boolean dryrun) {
        requireNonNull(logger, "'logger' must not be blank");

        this.logger = logger;
        this.dryrun = dryrun;
        this.api = Feign.builder()
            .encoder(new JacksonEncoder())
            .decoder(new JacksonDecoder())
            .errorDecoder((methodKey, response) -> new DiscordAPIException(response.status(), response.reason()))
            .options(new Request.Options(connectTimeout, TimeUnit.SECONDS, readTimeout, TimeUnit.SECONDS, true))
            .target(WebhookDiscordAPI.class, WEBHOOKS_URI);

        this.logger.debug("Discord dryrun set to {}", dryrun);
    }

    public void webhook(String webhook, String message) throws DiscordException {
        Message payload = Message.of(message);
        logger.debug("discord.message: " + payload.toString());
        wrap(() -> api.sendMessage(payload, webhook));
    }

    private void wrap(Runnable runnable) throws DiscordException {
        try {
            if (!dryrun) runnable.run();
        } catch (DiscordAPIException e) {
            logger.trace(e);
            throw new DiscordException("Discord operation failed", e);
        }
    }
}
