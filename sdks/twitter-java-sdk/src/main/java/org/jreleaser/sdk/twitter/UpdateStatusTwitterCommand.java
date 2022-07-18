/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.sdk.twitter;

import org.jreleaser.util.JReleaserLogger;

import java.util.List;

import static org.jreleaser.util.ObjectUtils.requireNonEmpty;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class UpdateStatusTwitterCommand extends AbstractTwitterCommand {
    private final List<String> statuses;

    private UpdateStatusTwitterCommand(JReleaserLogger logger,
                                       String apiHost,
                                       int connectTimeout,
                                       int readTimeout,
                                       String consumerKey,
                                       String consumerToken,
                                       String accessToken,
                                       String accessTokenSecret,
                                       boolean dryrun,
                                       List<String> statuses) {
        super(logger, apiHost, connectTimeout, readTimeout, consumerKey, consumerToken, accessToken, accessTokenSecret, dryrun);
        this.statuses = statuses;
    }

    @Override
    public void execute() throws TwitterException {
        twitter.updateStatus(statuses);
    }

    public static Builder builder(JReleaserLogger logger) {
        return new Builder(logger);
    }

    public static class Builder extends AbstractTwitterCommand.Builder<Builder> {
        private List<String> statuses;

        protected Builder(JReleaserLogger logger) {
            super(logger);
        }

        public Builder statuses(List<String> statuses) {
            this.statuses = (List<String>) requireNonEmpty(statuses, "'statuses' must not be empty");
            return this;
        }

        public UpdateStatusTwitterCommand build() {
            validate();
            this.statuses = (List<String>) requireNonEmpty(statuses, "'statuses' must not be empty");

            return new UpdateStatusTwitterCommand(
                logger,
                apiHost,
                connectTimeout,
                readTimeout,
                consumerKey,
                consumerToken,
                accessToken,
                accessTokenSecret,
                dryrun,
                statuses);
        }
    }
}
