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
package org.jreleaser.sdk.sdkman;

import org.jreleaser.util.Logger;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractSdkmanCommand implements SdkmanCommand {
    protected final boolean dryRun;
    protected final String candidate;
    protected final String version;
    protected final Sdkman sdkman;

    protected AbstractSdkmanCommand(Logger logger,
                                    String apiHost,
                                    String consumerKey,
                                    String consumerToken,
                                    String candidate,
                                    String version,
                                    boolean dryRun) {
        this.sdkman = new Sdkman(logger, apiHost, consumerKey, consumerToken, dryRun);
        this.candidate = candidate;
        this.version = version;
        this.dryRun = dryRun;
    }

    static class Builder<S extends Builder<S>> {
        protected final Logger logger;
        protected boolean dryRun;
        protected String consumerKey;
        protected String consumerToken;
        protected String candidate;
        protected String version;
        protected String apiHost = "https://vendors.sdkman.io";

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

        /**
         * The SDK consumer key
         */
        public S consumerKey(String consumerKey) {
            this.consumerKey = requireNonBlank(consumerKey, "'consumerKey' must not be blank").trim();
            return self();
        }

        /**
         * The SDK consumer token
         */
        public S consumerToken(String consumerToken) {
            this.consumerToken = requireNonBlank(consumerToken, "'consumerToken' must not be blank").trim();
            return self();
        }

        /**
         * candidate identifier
         */
        public S candidate(String candidate) {
            this.candidate = requireNonBlank(candidate, "'candidate' must not be blank").trim();
            return self();
        }

        /**
         * candidate version
         */
        public S version(String version) {
            this.version = requireNonBlank(version, "'version' must not be blank").trim();
            return self();
        }

        /**
         * SDK service hostname
         */
        public S apiHost(String apiHost) {
            this.apiHost = requireNonBlank(apiHost, "'apiHost' must not be blank").trim();
            return self();
        }
    }
}
