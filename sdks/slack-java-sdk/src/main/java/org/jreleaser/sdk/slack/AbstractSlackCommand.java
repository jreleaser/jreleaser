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

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractSlackCommand implements SlackCommand {
    protected final boolean dryrun;
    protected final Slack slack;

    protected AbstractSlackCommand(JReleaserLogger logger,
                                   String token,
                                   String apiHost,
                                   int connectTimeout,
                                   int readTimeout,
                                   boolean dryrun) {
        this.slack = new Slack(logger, token, apiHost, connectTimeout, readTimeout, dryrun);
        this.dryrun = dryrun;
    }

    static class Builder<S extends Builder<S>> {
        protected final JReleaserLogger logger;
        protected boolean dryrun;
        protected String token;
        protected String apiHost;
        protected int connectTimeout = 20;
        protected int readTimeout = 60;

        protected Builder(JReleaserLogger logger) {
            this.logger = requireNonNull(logger, "'logger' must not be blank");
        }

        protected final S self() {
            return (S) this;
        }

        public S dryrun(boolean dryrun) {
            this.dryrun = dryrun;
            return self();
        }

        public S token(String token) {
            this.token = requireNonBlank(token, "'token' must not be blank").trim();
            return self();
        }

        public S apiHost(String apiHost) {
            this.apiHost = requireNonBlank(apiHost, "'apiHost' must not be blank").trim();
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
            requireNonBlank(token, "'token' must not be blank");
            requireNonBlank(apiHost, "'apiHost' must not be blank");
        }
    }
}
