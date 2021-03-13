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
package org.jreleaser.sdk.twitter;

import org.jreleaser.util.Logger;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractTwitterCommand implements TwitterCommand {
    protected final boolean dryRun;
    protected final Twitter twitter;

    protected AbstractTwitterCommand(Logger logger,
                                     String apiHost,
                                     String consumerKey,
                                     String consumerToken,
                                     String accessToken,
                                     String accessTokenSecret,
                                     boolean dryRun) {
        this.twitter = new Twitter(logger, apiHost, consumerKey, consumerToken, accessToken, accessTokenSecret, dryRun);
        this.dryRun = dryRun;
    }

    static class Builder<S extends Builder<S>> {
        protected final Logger logger;
        protected boolean dryRun;
        protected String consumerKey;
        protected String consumerToken;
        protected String accessToken;
        protected String accessTokenSecret;
        protected String apiHost = "https://api.twitter.com/1.1/";

        protected Builder(Logger logger) {
            this.logger = requireNonNull(logger, "'logger' must not be blank");
        }

        protected final S self() {
            return (S) this;
        }

        public S dryRun(boolean dryRun) {
            this.dryRun = dryRun;
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

        public S apiHost(String apiHost) {
            this.apiHost = requireNonBlank(apiHost, "'apiHost' must not be blank").trim();
            return self();
        }

        protected void validate() {
            requireNonBlank(apiHost, "'apiHost' must not be blank");
            requireNonBlank(consumerKey, "'consumerKey' must not be blank");
            requireNonBlank(consumerToken, "'consumerToken' must not be blank");
            requireNonBlank(accessToken, "'accessToken' must not be blank");
            requireNonBlank(accessTokenSecret, "'accessTokenSecret' must not be blank");
        }
    }
}
