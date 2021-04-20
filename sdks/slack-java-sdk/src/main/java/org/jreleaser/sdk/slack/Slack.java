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

import feign.Feign;
import feign.Request;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.jreleaser.sdk.slack.api.Message;
import org.jreleaser.sdk.slack.api.SlackAPI;
import org.jreleaser.sdk.slack.api.SlackAPIException;
import org.jreleaser.sdk.slack.api.SlackResponse;
import org.jreleaser.util.JReleaserLogger;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Slack {
    private final JReleaserLogger logger;
    private final SlackAPI api;
    private final boolean dryrun;

    public Slack(JReleaserLogger logger,
                 String token,
                 String apiHost,
                 int connectTimeout,
                 int readTimeout,
                 boolean dryrun) {
        requireNonNull(logger, "'logger' must not be blank");
        requireNonBlank(token, "'token' must not be blank");

        this.logger = logger;
        this.dryrun = dryrun;
        this.api = Feign.builder()
            .encoder(new FormEncoder(new JacksonEncoder()))
            .decoder(new JacksonDecoder())
            .requestInterceptor(template -> template.header("Authorization", String.format("Bearer %s", token)))
            .errorDecoder((methodKey, response) -> new SlackAPIException(response.status(), response.reason(), response.headers()))
            .options(new Request.Options(connectTimeout, TimeUnit.SECONDS, readTimeout, TimeUnit.SECONDS, true))
            .target(SlackAPI.class, apiHost);

        this.logger.debug("Slack dryrun set to {}", dryrun);
    }

    public void message(String channel,
                        String message) throws SlackException {
        Message payload = Message.of(channel, message);
        logger.debug("slack.message: " + payload.toString());
        decode(wrap(() -> {
            SlackResponse response = api.message(payload);
            return response.getError();
        }));
    }

    private void decode(String error) throws SlackException {
        if (isNotBlank(error) && !"null".equals(error)) {
            throw new SlackException(error);
        }
    }

    private String wrap(Callable<String> runnable) throws SlackException {
        try {
            if (!dryrun) return runnable.call();
        } catch (Exception e) {
            logger.trace(e);
            throw new SlackException("Slack operation failed", e);
        }

        return null;
    }
}
