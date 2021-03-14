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

import org.jreleaser.util.Logger;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractZulipCommand implements ZulipCommand {
    protected final boolean dryrun;
    protected final Zulip zulip;

    protected AbstractZulipCommand(Logger logger,
                                   String apiHost,
                                   String account,
                                   String apiKey,
                                   boolean dryrun) {
        this.zulip = new Zulip(logger, apiHost, account, apiKey, dryrun);
        this.dryrun = dryrun;
    }

    static class Builder<S extends Builder<S>> {
        protected final Logger logger;
        protected boolean dryrun;
        protected String account;
        protected String apiKey;
        protected String apiHost;

        protected Builder(Logger logger) {
            this.logger = requireNonNull(logger, "'logger' must not be blank");
        }

        protected final S self() {
            return (S) this;
        }

        public S dryrun(boolean dryrun) {
            this.dryrun = dryrun;
            return self();
        }

        public S account(String account) {
            this.account = requireNonBlank(account, "'account' must not be blank").trim();
            return self();
        }

        public S apiKey(String apiKey) {
            this.apiKey = requireNonBlank(apiKey, "'apiKey' must not be blank").trim();
            return self();
        }

        public S apiHost(String apiHost) {
            this.apiHost = requireNonBlank(apiHost, "'apiHost' must not be blank").trim();
            return self();
        }

        protected void validate() {
            requireNonBlank(apiHost, "'apiHost' must not be blank");
            requireNonBlank(account, "'account' must not be blank");
            requireNonBlank(apiKey, "'apiKey' must not be blank");
        }
    }
}
