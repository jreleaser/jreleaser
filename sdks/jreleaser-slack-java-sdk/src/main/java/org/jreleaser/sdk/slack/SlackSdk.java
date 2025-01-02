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
package org.jreleaser.sdk.slack;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.slack.api.Message;
import org.jreleaser.sdk.slack.api.SlackAPI;
import org.jreleaser.sdk.slack.api.SlackResponse;

import java.util.concurrent.Callable;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class SlackSdk {
    private final JReleaserContext context;
    private final SlackAPI api;
    private final boolean dryrun;

    private SlackSdk(JReleaserContext context,
                     String token,
                     String apiHost,
                     int connectTimeout,
                     int readTimeout,
                     boolean dryrun) {
        requireNonNull(context, "'context' must not be null");
        requireNonBlank(token, "'token' must not be blank");

        this.context = context;
        this.dryrun = dryrun;
        this.api = ClientUtils.builder(context, connectTimeout, readTimeout)
            .requestInterceptor(template -> template.header("Authorization", String.format("Bearer %s", token)))
            .target(SlackAPI.class, apiHost);

        this.context.getLogger().debug(RB.$("workflow.dryrun"), dryrun);
    }

    public void message(String channel, String message) throws SlackException {
        Message payload = Message.of(channel, message);
        context.getLogger().debug("slack.message: " + payload);
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
            context.getLogger().trace(e);
            throw new SlackException(RB.$("sdk.operation.failed", "Slack"), e);
        }

        return null;
    }

    public static Builder builder(JReleaserContext context) {
        return new Builder(context);
    }

    public static class Builder {
        private final JReleaserContext context;
        private boolean dryrun;
        private String token;
        private String apiHost;
        private int connectTimeout = 20;
        private int readTimeout = 60;

        private Builder(JReleaserContext context) {
            this.context = requireNonNull(context, "'context' must not be null");
        }

        public Builder dryrun(boolean dryrun) {
            this.dryrun = dryrun;
            return this;
        }

        public Builder token(String token) {
            this.token = requireNonBlank(token, "'token' must not be blank").trim();
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
            requireNonBlank(token, "'token' must not be blank");
        }

        public SlackSdk build() {
            if (isBlank(apiHost)) {
                apiHost("https://slack.com/api/");
            }

            validate();

            return new SlackSdk(
                context,
                token,
                apiHost,
                connectTimeout,
                readTimeout,
                dryrun);
        }
    }
}
