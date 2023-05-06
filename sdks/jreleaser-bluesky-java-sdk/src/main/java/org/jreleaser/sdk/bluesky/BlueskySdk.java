/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
package org.jreleaser.sdk.bluesky;

import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.sdk.bluesky.api.BlueskyAPI;
import org.jreleaser.sdk.commons.RestAPIException;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author Tom cools
 * @since 1.7.0
 */
public class BlueskySdk {
    private final JReleaserLogger logger;
    private final BlueskyAPI api;
    private final boolean dryrun;

    private BlueskySdk(JReleaserLogger logger,
                        boolean dryrun) {
        this.logger = requireNonNull(logger, "'logger' must not be null");
        this.dryrun = dryrun;

        //TODO setup api
        api = null;

        this.logger.debug(RB.$("workflow.dryrun"), dryrun);
    }

    public void skeet(List<String> statuses) throws BlueskyException {
        //TODO bejug placeholder
    }

    private void wrap(Runnable runnable) throws BlueskyException {
        try {
            if (!dryrun) runnable.run();
        } catch (RestAPIException e) {
            logger.trace(e);
            throw new BlueskyException(RB.$("sdk.operation.failed", "Bluesky"), e);
        }
    }

    public static Builder builder(JReleaserLogger logger) {
        return new Builder(logger);
    }

    public static class Builder {
        private final JReleaserLogger logger;
        private boolean dryrun;

        private Builder(JReleaserLogger logger) {
            this.logger = requireNonNull(logger, "'logger' must not be null");
        }

        public Builder dryrun(boolean dryrun) {
            this.dryrun = dryrun;
            return this;
        }

        private void validate() {
            // TODO BEJUG
        }

        public BlueskySdk build() {
            validate();

            return new BlueskySdk(
                logger,
                dryrun);
        }
    }

}
