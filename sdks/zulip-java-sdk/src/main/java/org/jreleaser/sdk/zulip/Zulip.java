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
package org.jreleaser.sdk.zulip;

import feign.Feign;
import feign.Request;
import feign.auth.BasicAuthRequestInterceptor;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.jreleaser.sdk.zulip.api.Message;
import org.jreleaser.sdk.zulip.api.ZulipAPI;
import org.jreleaser.util.JReleaserLogger;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Zulip {
    private final JReleaserLogger logger;
    private final ZulipAPI api;
    private final boolean dryrun;

    public Zulip(JReleaserLogger logger, String apiHost, String account, String apiKey, boolean dryrun) {
        requireNonNull(logger, "'logger' must not be blank");
        requireNonBlank(apiHost, "'apiHost' must not be blank");
        requireNonBlank(account, "'account' must not be blank");
        requireNonBlank(apiKey, "'apiKey' must not be blank");

        this.logger = logger;
        this.dryrun = dryrun;
        this.api = Feign.builder()
            .encoder(new FormEncoder(new JacksonEncoder()))
            .decoder(new JacksonDecoder())
            .requestInterceptor(new BasicAuthRequestInterceptor(account, apiKey))
            .errorDecoder((methodKey, response) -> new IllegalStateException("Server returned error " + response.reason()))
            .options(new Request.Options(20, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, true))
            .target(ZulipAPI.class, apiHost);

        this.logger.debug("Zulip dryrun set to {}", dryrun);
    }

    public void message(String channel,
                        String subject,
                        String message) throws ZulipException {
        Message payload = Message.of(channel, subject, message);
        logger.debug("zulip.message: " + payload.toString());
        wrap(() -> api.message(payload));
    }

    private void wrap(Runnable runnable) throws ZulipException {
        try {
            if (!dryrun) runnable.run();
        } catch (RuntimeException e) {
            logger.trace(e);
            throw new ZulipException("Zulip operation failed", e);
        }
    }
}
