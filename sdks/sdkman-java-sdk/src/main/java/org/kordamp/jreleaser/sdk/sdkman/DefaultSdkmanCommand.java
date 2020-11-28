/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Andres Almiray.
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
package org.kordamp.jreleaser.sdk.sdkman;

import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.Map;

import static org.kordamp.jreleaser.sdk.sdkman.ApiEndpoints.DEFAULT_ENDPOINT;
import static org.kordamp.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class DefaultSdkmanCommand extends AbstractSingleSdkmanCommand {
    private DefaultSdkmanCommand(String consumerKey,
                                 String consumerToken,
                                 String candidate,
                                 String version,
                                 String apiHost,
                                 boolean https) {
        super(consumerKey, consumerToken, candidate, version, apiHost, https);
    }

    @Override
    protected Request createRequest(Map<String, String> payload) {
        RequestBody body = RequestBody.create(JSON, toJson(payload));
        return new Request.Builder()
            .url(createURL(DEFAULT_ENDPOINT))
            .put(body)
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractSdkmanCommand.Builder<Builder> {
        public DefaultSdkmanCommand build() {
            requireNonBlank(consumerKey, "'consumerKey' must not be blank");
            requireNonBlank(consumerToken, "'consumerToken' must not be blank");
            requireNonBlank(candidate, "'candidate' must not be blank");
            requireNonBlank(version, "'version' must not be blank");
            requireNonBlank(apiHost, "'apiHost' must not be blank");

            return new DefaultSdkmanCommand(
                consumerKey,
                consumerToken,
                candidate,
                version,
                apiHost,
                https);
        }
    }
}
