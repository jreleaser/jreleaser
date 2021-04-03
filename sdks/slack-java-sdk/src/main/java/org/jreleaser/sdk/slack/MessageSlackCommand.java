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

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class MessageSlackCommand extends AbstractSlackCommand {
    private final String channel;
    private final String message;

    private MessageSlackCommand(JReleaserLogger logger,
                                String token,
                                String apiHost,
                                boolean dryrun,
                                String channel,
                                String message) {
        super(logger, token, apiHost, dryrun);
        this.channel = channel;
        this.message = message;
    }

    @Override
    public void execute() throws SlackException {
        slack.message(channel, message);
    }

    public static Builder builder(JReleaserLogger logger) {
        return new Builder(logger);
    }

    public static class Builder extends AbstractSlackCommand.Builder<Builder> {
        private String channel;
        private String message;

        protected Builder(JReleaserLogger logger) {
            super(logger);
        }

        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public MessageSlackCommand build() {
            if (isBlank(apiHost)) {
                apiHost("https://slack.com/api/");
            }

            validate();

            requireNonBlank(channel, "'channel' must not be blank");
            requireNonBlank(message, "'message' must not be blank");

            return new MessageSlackCommand(
                logger,
                token,
                apiHost,
                dryrun,
                channel,
                message);
        }
    }
}
