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

import org.jreleaser.util.JReleaserLogger;

import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class MessageZulipCommand extends AbstractZulipCommand {
    private final String channel;
    private final String subject;
    private final String message;

    private MessageZulipCommand(JReleaserLogger logger,
                                String apiHost,
                                String account,
                                String apiKey,
                                boolean dryrun,
                                String channel,
                                String subject,
                                String message) {
        super(logger, apiHost, account, apiKey, dryrun);
        this.channel = channel;
        this.subject = subject;
        this.message = message;
    }

    @Override
    public void execute() throws ZulipException {
        zulip.message(channel, subject, message);
    }

    public static Builder builder(JReleaserLogger logger) {
        return new Builder(logger);
    }

    public static class Builder extends AbstractZulipCommand.Builder<Builder> {
        private String channel;
        private String subject;
        private String message;

        protected Builder(JReleaserLogger logger) {
            super(logger);
        }

        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public MessageZulipCommand build() {
            validate();
            requireNonBlank(channel, "'channel' must not be blank");
            requireNonBlank(subject, "'subject' must not be blank");
            requireNonBlank(message, "'message' must not be blank");

            return new MessageZulipCommand(
                logger,
                apiHost,
                account,
                apiKey,
                dryrun,
                channel,
                subject,
                message);
        }
    }
}
