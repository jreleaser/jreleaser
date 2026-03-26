/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.sdk.zernio;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.zernio.api.Accounts;
import org.jreleaser.sdk.zernio.api.Platform;
import org.jreleaser.sdk.zernio.api.Post;
import org.jreleaser.sdk.zernio.api.ZernioAPI;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 1.24.0
 */
public class ZernioSdk {
    private final JReleaserContext context;
    private final ZernioAPI api;
    private final boolean dryrun;

    private ZernioSdk(JReleaserContext context,
                      String apiHost,
                      String token,
                      int connectTimeout,
                      int readTimeout,
                      boolean dryrun) {
        requireNonNull(context, "'context' must not be null");
        requireNonBlank(apiHost, "'apiHost' must not be blank");
        requireNonBlank(token, "'token' must not be blank");

        this.context = context;
        this.dryrun = dryrun;
        this.api = ClientUtils.builder(context, connectTimeout, readTimeout)
            .requestInterceptor(template -> template.header("Authorization", String.format("Bearer %s", token)))
            .target(ZernioAPI.class, apiHost);

        this.context.getLogger().debug(RB.$("workflow.dryrun"), dryrun);
    }

    public Accounts listAccounts(String profileId) throws ZernioException {
        return wrap(() -> {
            Map<String, String> map = new LinkedHashMap<>();
            if (isNotBlank(profileId)) {
                map.put("profileId", profileId);
            }
            return api.listAccounts(map);
        });
    }

    public void post(String content,
                     Set<Platform> platforms) throws ZernioException {
        Post payload = Post.of(content, platforms);
        context.getLogger().info(RB.$("zernio.post", platforms.stream().map(Platform::getPlatform).collect(joining(", "))));
        context.getLogger().debug("zernio.content: " + content);
        wrap(() -> api.post(payload));
    }

    private void wrap(Runnable runnable) throws ZernioException {
        try {
            if (!dryrun) runnable.run();
        } catch (RestAPIException e) {
            context.getLogger().trace(e);
            throw new ZernioException(RB.$("sdk.operation.failed", "Zernio"), e);
        }
    }

    private <T> T wrap(Callable<T> callable) throws ZernioException {
        try {
            if (!dryrun) {
                return callable.call();
            }
            return null;
        } catch (ZernioException e) {
            context.getLogger().trace(e);
            throw e;
        } catch (Exception e) {
            context.getLogger().trace(e);
            throw new ZernioException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    public static Builder builder(JReleaserContext context) {
        return new Builder(context);
    }

    public static class Builder {
        private final JReleaserContext context;
        private boolean dryrun;
        private String token;
        private String apiHost;
        private int connectTimeout = 20;
        private int readTimeout = 60;

        private Builder(JReleaserContext context) {
            this.context = requireNonNull(context, "'context' must not be null");
        }

        public Builder dryrun(boolean dryrun) {
            this.dryrun = dryrun;
            return this;
        }

        public Builder token(String token) {
            this.token = requireNonBlank(token, "'token' must not be blank").trim();
            return this;
        }

        public Builder apiHost(String apiHost) {
            this.apiHost = requireNonBlank(apiHost, "'apiHost' must not be blank").trim();
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
            requireNonBlank(apiHost, "'apiHost' must not be blank");
            requireNonBlank(token, "'token' must not be blank");
        }

        public ZernioSdk build() {
            validate();

            return new ZernioSdk(
                context,
                apiHost,
                token,
                connectTimeout,
                readTimeout,
                dryrun);
        }
    }
}
