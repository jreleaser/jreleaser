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
package org.kordamp.jreleaser.sdk.sdkman;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.kordamp.jreleaser.sdk.sdkman.ApiEndpoints.ANNOUNCE_ENDPOINT;
import static org.kordamp.jreleaser.sdk.sdkman.ApiEndpoints.RELEASE_ENDPOINT;
import static org.kordamp.jreleaser.util.StringUtils.isBlank;
import static org.kordamp.jreleaser.util.StringUtils.isNotBlank;
import static org.kordamp.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class MinorReleaseSdkmanCommand extends AbstractMultiSdkmanCommand {
    private final String hashtag;
    private final String releaseNotesUrl;
    private final String url;
    private final Map<String, String> platforms = new LinkedHashMap<>();

    private MinorReleaseSdkmanCommand(String consumerKey,
                                      String consumerToken,
                                      String candidate,
                                      String version,
                                      String apiHost,
                                      boolean https,
                                      String hashtag,
                                      String releaseNotesUrl,
                                      String url,
                                      Map<String, String> platforms) {
        super(consumerKey,
            consumerToken,
            candidate,
            version,
            apiHost,
            https);
        this.hashtag = hashtag;
        this.releaseNotesUrl = releaseNotesUrl;
        this.url = url;
        this.platforms.putAll(platforms);
    }

    @Override
    protected Response executeRequests() throws IOException {
        List<Response> responses = new ArrayList<>();

        if (platforms.isEmpty()) {
            responses.add(execCall(createRequest(getReleasePayload())));
        } else {
            for (Map.Entry<String, String> platform : platforms.entrySet()) {
                Map<String, String> payload = super.getPayload();
                payload.put("platform", platform.getKey());
                payload.put("url", platform.getValue());
                responses.add(execCall(createRequest(payload)));
            }
        }

        responses.add(execCall(createAnnounceRequest()));

        return responses.stream()
            .filter(resp -> {
                int statusCode = resp.code();
                return statusCode < 200 || statusCode >= 300;
            })
            .findFirst()
            .orElse(responses.get(responses.size() - 1));
    }

    private Map<String, String> getReleasePayload() {
        Map<String, String> payload = super.getPayload();
        payload.put("platform", "UNIVERSAL");
        payload.put("url", url);
        return payload;
    }

    private Request createRequest(Map<String, String> payload) {
        RequestBody body = RequestBody.create(JSON, toJson(payload));
        return new Request.Builder()
            .url(createURL(RELEASE_ENDPOINT))
            .post(body)
            .build();
    }

    private Request createAnnounceRequest() {
        Map<String, String> payload = super.getPayload();
        if (isNotBlank(hashtag)) payload.put("hashtag", hashtag.trim());
        if (isNotBlank(releaseNotesUrl)) payload.put("url", releaseNotesUrl.trim());

        RequestBody body = RequestBody.create(JSON, toJson(payload));
        return new Request.Builder()
            .url(createURL(ANNOUNCE_ENDPOINT))
            .post(body)
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractSdkmanCommand.Builder<Builder> {
        private final Map<String, String> platforms = new LinkedHashMap<>();
        private String hashtag;
        private String releaseNotesUrl;
        private String url;

        /**
         * The hashtag to use (legacy)
         */
        public Builder hashtag(String hashtag) {
            this.hashtag = hashtag;
            return this;
        }

        /**
         * The URL where the release notes can be found
         */
        public Builder releaseNotesUrl(String releaseNotesUrl) {
            this.releaseNotesUrl = releaseNotesUrl;
            return this;
        }

        /**
         * The URL from where the candidate version can be downloaded
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * Platform to downlodable URL mappings.
         * Supported platforms are:
         * <ul>
         * <li>MAC_OSX</li>
         * <li>WINDOWS_64</li>
         * <li>LINUX_64</li>
         * <li>LINUX_32</li>
         * </ul>
         * Example:
         * <pre>
         *     "MAC_OSX"   :"https://host/micronaut-x.y.z-macosx.zip"
         *     "LINUX_64"  :"https://host/micronaut-x.y.z-linux64.zip"
         *     "WINDOWS_64":"https://host/micronaut-x.y.z-win.zip"
         * </pre>
         */
        public Builder platforms(Map<String, String> platforms) {
            this.platforms.putAll(platforms);
            return this;
        }

        public Builder platform(String platform, String url) {
            this.platforms.put(
                requireNonBlank(platform, "'platform' must not be blank").trim(),
                requireNonBlank(url, "'url' must not be blank").trim());
            return this;
        }

        public MinorReleaseSdkmanCommand build() {
            requireNonBlank(consumerKey, "'consumerKey' must not be blank");
            requireNonBlank(consumerToken, "'consumerToken' must not be blank");
            requireNonBlank(candidate, "'candidate' must not be blank");
            requireNonBlank(version, "'version' must not be blank");
            requireNonBlank(apiHost, "'apiHost' must not be blank");

            // url is required if platforms is empty
            if ((platforms.isEmpty()) && isBlank(url)) {
                throw new IllegalArgumentException("Missing url");
            }

            return new MinorReleaseSdkmanCommand(
                consumerKey,
                consumerToken,
                candidate,
                version,
                apiHost,
                https,
                hashtag,
                releaseNotesUrl,
                url,
                platforms);
        }
    }
}
