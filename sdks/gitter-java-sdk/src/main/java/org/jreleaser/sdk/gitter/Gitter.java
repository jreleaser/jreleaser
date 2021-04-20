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

import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.sdk.gitter.api.GitterAPIException;
import org.jreleaser.sdk.gitter.api.Message;
import org.jreleaser.sdk.gitter.api.GitterWebhookAPI;
import org.jreleaser.util.JReleaserLogger;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class Gitter {
    public static final String WEBHOOKS_URI = "https://webhooks.gitter.im";

    private final JReleaserLogger logger;
    private final GitterWebhookAPI api;
    private final boolean dryrun;

    public Gitter(JReleaserLogger logger,
                  int connectTimeout,
                  int readTimeout,
                  boolean dryrun) {
        requireNonNull(logger, "'logger' must not be blank");

        this.logger = logger;
        this.dryrun = dryrun;
        this.api = Feign.builder()
            .encoder(new JacksonEncoder())
            .decoder(new JacksonDecoder())
            .requestInterceptor(template -> template.header("User-Agent", "JReleaser/" + JReleaserVersion.getPlainVersion()))
            .errorDecoder((methodKey, response) -> new GitterAPIException(response.status(), response.reason(), response.headers()))
            .options(new Request.Options(connectTimeout, TimeUnit.SECONDS, readTimeout, TimeUnit.SECONDS, true))
            .target(GitterWebhookAPI.class, WEBHOOKS_URI);

        this.logger.debug("Gitter dryrun set to {}", dryrun);
    }

    public void webhook(String webhook, String message) throws GitterException {
        Message payload = Message.of(message);
        logger.debug("gitter.message: " + payload.toString());
        wrap(() -> api.sendMessage(payload, webhook));
    }

    private void wrap(Runnable runnable) throws GitterException {
        try {
            if (!dryrun) runnable.run();
        } catch (GitterAPIException e) {
            logger.trace(e);
            throw new GitterException("Gitter operation failed", e);
        }
    }
}
