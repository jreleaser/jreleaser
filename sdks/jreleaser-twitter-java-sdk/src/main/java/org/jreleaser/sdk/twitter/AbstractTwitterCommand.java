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
package org.jreleaser.sdk.twitter;

import org.jreleaser.logging.JReleaserLogger;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractTwitterCommand implements TwitterCommand {
    protected final boolean dryrun;
    protected final Twitter twitter;

    protected AbstractTwitterCommand(JReleaserLogger logger,
                                     int connectTimeout,
                                     int readTimeout,
                                     String consumerKey,
                                     String consumerToken,
                                     String accessToken,
                                     String accessTokenSecret,
                                     boolean dryrun) {
        this.twitter = new Twitter(logger, connectTimeout, readTimeout, consumerKey, consumerToken, accessToken, accessTokenSecret, dryrun);
        this.dryrun = dryrun;
    }

    static class Builder<S extends Builder<S>> {
        protected final JReleaserLogger logger;
        protected boolean dryrun;
        protected String consumerKey;
        protected String consumerToken;
        protected String accessToken;
        protected String accessTokenSecret;
        protected int connectTimeout = 20;
        protected int readTimeout = 60;

        protected Builder(JReleaserLogger logger) {
            this.logger = requireNonNull(logger, "'logger' must not be null");
        }

        @SuppressWarnings("unchecked")
        protected final S self() {
            return (S) this;
        }

        public S dryrun(boolean dryrun) {
            this.dryrun = dryrun;
            return self();
        }

        public S consumerKey(String consumerKey) {
            this.consumerKey = requireNonBlank(consumerKey, "'consumerKey' must not be blank").trim();
            return self();
        }

        public S consumerToken(String consumerToken) {
            this.consumerToken = requireNonBlank(consumerToken, "'consumerToken' must not be blank").trim();
            return self();
        }

        public S accessToken(String accessToken) {
            this.accessToken = requireNonBlank(accessToken, "'accessToken' must not be blank").trim();
            return self();
        }

        public S accessTokenSecret(String accessTokenSecret) {
            this.accessTokenSecret = requireNonBlank(accessTokenSecret, "'accessTokenSecret' must not be blank").trim();
            return self();
        }

        public S connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return self();
        }

        public S readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return self();
        }

        protected void validate() {
            requireNonBlank(consumerKey, "'consumerKey' must not be blank");
            requireNonBlank(consumerToken, "'consumerToken' must not be blank");
            requireNonBlank(accessToken, "'accessToken' must not be blank");
            requireNonBlank(accessTokenSecret, "'accessTokenSecret' must not be blank");
        }
    }
}
