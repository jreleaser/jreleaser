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
package org.jreleaser.sdk.linkedin;

import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.linkedin.api.LinkedinAPI;
import org.jreleaser.sdk.linkedin.api.Message;
import org.jreleaser.sdk.linkedin.api.Profile;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.model.Constants.KEY_LINKEDIN_OWNER;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public class LinkedinSdk {
    private final JReleaserLogger logger;
    private final LinkedinAPI api;
    private final boolean dryrun;
    private final int connectTimeout;
    private final int readTimeout;
    private final String apiHost;
    private final String accessToken;

    private LinkedinSdk(JReleaserLogger logger,
                        String apiHost,
                        String accessToken,
                        int connectTimeout,
                        int readTimeout,
                        boolean dryrun) {
        this.logger = requireNonNull(logger, "'logger' must not be null");
        this.apiHost = requireNonBlank(apiHost, "'apiHost' must not be blank");
        this.accessToken = requireNonBlank(accessToken, "'accessToken' must not be blank");

        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.dryrun = dryrun;

        this.api = ClientUtils.builder(logger, connectTimeout, readTimeout)
            .requestInterceptor(template -> template.header("Authorization", String.format("Bearer %s", accessToken)))
            .target(LinkedinAPI.class, apiHost);

        this.logger.debug(RB.$("workflow.dryrun"), dryrun);
    }

    public void share(String owner, Message payload) throws LinkedinException {
        logger.debug("linkedin.subject: " + payload.getSubject());

        wrap(() -> {
            if (isNotBlank(owner)) {
                payload.setOwner(owner);
            } else {
                Profile profile = api.getProfile();
                payload.setOwner(profile.urn());
            }
            api.share(payload);
        });
    }

    public void share(String owner, String subject, String text) throws AnnounceException, LinkedinException {
        logger.debug("linkedin.subject: " + subject);

        if (dryrun) return;

        if (isNotBlank(owner)) {
            text = text.replace("{{" + KEY_LINKEDIN_OWNER + "}}", owner);
        } else {
            Profile profile = wrap(api::getProfile);
            text = text.replace("{{" + KEY_LINKEDIN_OWNER + "}}", profile.urn());
        }

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", String.format("Bearer %s", accessToken));
        ClientUtils.post(logger,
            apiHost + "/shares",
            connectTimeout,
            readTimeout,
            text,
            headers);
    }

    private void wrap(Runnable runnable) throws LinkedinException {
        try {
            if (!dryrun) runnable.run();
        } catch (RestAPIException e) {
            logger.trace(e.getStatus() + ": " + e.getReason());
            logger.trace(e);
            throw new LinkedinException(RB.$("sdk.operation.failed", "Linkedin"), e);
        }
    }

    private <T> T wrap(Supplier<T> supplier) throws LinkedinException {
        try {
            if (!dryrun) {
                return supplier.get();
            }
            return null;
        } catch (RestAPIException e) {
            logger.trace(e.getStatus() + ": " + e.getReason());
            logger.trace(e);
            throw new LinkedinException(RB.$("sdk.operation.failed", "Linkedin"), e);
        }
    }

    public static Builder builder(JReleaserLogger logger) {
        return new Builder(logger);
    }

    public static class Builder {
        private final JReleaserLogger logger;
        private boolean dryrun;
        private String apiHost;
        private String accessToken;
        private int connectTimeout = 20;
        private int readTimeout = 60;

        private Builder(JReleaserLogger logger) {
            this.logger = requireNonNull(logger, "'logger' must not be null");
        }

        public Builder dryrun(boolean dryrun) {
            this.dryrun = dryrun;
            return this;
        }

        public Builder apiHost(String apiHost) {
            this.apiHost = requireNonBlank(apiHost, "'apiHost' must not be blank").trim();
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = requireNonBlank(accessToken, "'accessToken' must not be blank").trim();
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
            requireNonBlank(accessToken, "'token' must not be blank");
            if (isBlank(apiHost)) {
                apiHost("https://api.linkedin.com/v2");
            }
        }

        public LinkedinSdk build() {
            validate();

            return new LinkedinSdk(
                logger,
                apiHost,
                accessToken,
                connectTimeout,
                readTimeout,
                dryrun);
        }
    }
}
