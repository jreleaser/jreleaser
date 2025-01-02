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
package org.jreleaser.sdk.linkedin;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext;
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
    private final JReleaserContext context;
    private final LinkedinAPI api;
    private final boolean dryrun;
    private final int connectTimeout;
    private final int readTimeout;
    private final String apiHost;
    private final String accessToken;

    private LinkedinSdk(JReleaserContext context,
                        String apiHost,
                        String accessToken,
                        int connectTimeout,
                        int readTimeout,
                        boolean dryrun) {
        this.context = requireNonNull(context, "'context' must not be null");
        this.apiHost = requireNonBlank(apiHost, "'apiHost' must not be blank");
        this.accessToken = requireNonBlank(accessToken, "'accessToken' must not be blank");

        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.dryrun = dryrun;

        this.api = ClientUtils.builder(context, connectTimeout, readTimeout)
            .requestInterceptor(template -> template.header("Authorization", String.format("Bearer %s", accessToken)))
            .target(LinkedinAPI.class, apiHost);

        this.context.getLogger().debug(RB.$("workflow.dryrun"), dryrun);
    }

    public void share(String owner, Message payload) throws LinkedinException {
        context.getLogger().debug("linkedin.subject: " + payload.getSubject());

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
        context.getLogger().debug("linkedin.subject: " + subject);

        if (dryrun) return;

        if (isNotBlank(owner)) {
            text = text.replace("{{" + KEY_LINKEDIN_OWNER + "}}", owner);
        } else {
            Profile profile = wrap(api::getProfile);
            text = text.replace("{{" + KEY_LINKEDIN_OWNER + "}}", profile.urn());
        }

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", String.format("Bearer %s", accessToken));
        ClientUtils.post(context.getLogger(),
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
            context.getLogger().trace(e.getStatus() + ": " + e.getReason());
            context.getLogger().trace(e);
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
            context.getLogger().trace(e.getStatus() + ": " + e.getReason());
            context.getLogger().trace(e);
            throw new LinkedinException(RB.$("sdk.operation.failed", "Linkedin"), e);
        }
    }

    public static Builder builder(JReleaserContext context) {
        return new Builder(context);
    }

    public static class Builder {
        private final JReleaserContext context;
        private boolean dryrun;
        private String apiHost;
        private String accessToken;
        private int connectTimeout = 20;
        private int readTimeout = 60;

        private Builder(JReleaserContext context) {
            this.context = requireNonNull(context, "'context' must not be null");
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
                context,
                apiHost,
                accessToken,
                connectTimeout,
                readTimeout,
                dryrun);
        }
    }
}
