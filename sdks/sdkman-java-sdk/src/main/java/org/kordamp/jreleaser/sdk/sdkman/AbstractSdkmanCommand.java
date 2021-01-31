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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.kordamp.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractSdkmanCommand implements SdkmanCommand {
    protected static final MediaType JSON = MediaType.get("application/json; charset=UTF-8");

    protected final String consumerKey;
    protected final String consumerToken;
    protected final String candidate;
    protected final String version;
    protected final String apiHost;
    protected final boolean https;

    protected AbstractSdkmanCommand(String consumerKey,
                                    String consumerToken,
                                    String candidate,
                                    String version,
                                    String apiHost,
                                    boolean https) {
        this.consumerKey = consumerKey;
        this.consumerToken = consumerToken;
        this.candidate = candidate;
        this.version = version;
        this.apiHost = apiHost;
        this.https = https;
    }

    protected Map<String, String> getPayload() {
        Map<String, String> payload = new HashMap<>();
        payload.put("candidate", candidate);
        payload.put("version", version);
        return payload;
    }

    protected String toJson(Map<String, String> payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON", e);
        }
    }

    protected Response execCall(Request req) throws IOException {
        req = req.newBuilder()
            .addHeader("Consumer-Key", consumerKey)
            .addHeader("Consumer-Token", consumerToken)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build();

        OkHttpClient client = new OkHttpClient();
        try (Response response = client.newCall(req).execute()) {
            return response;
        }
    }

    protected URL createURL(String endpoint) {
        try {
            return createURI(endpoint).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL", e);
        }
    }

    private URI createURI(String endpoint) throws URISyntaxException {
        String host = apiHost;
        int i = host.indexOf("://");
        if (i > -1) {
            host = host.substring(i + 3);
        }

        String[] parts = host.split(":");
        if (parts.length == 1) {
            return new URI(https ? "https" : "http", host, endpoint, null);
        } else if (parts.length == 2) {
            return new URI(https ? "https" : "http", null, parts[0], Integer.parseInt(parts[1]), endpoint, null, null);
        } else {
            throw new URISyntaxException(apiHost, "Invalid");
        }
    }

    static class Builder<S extends Builder<S>> {
        protected String consumerKey;
        protected String consumerToken;
        protected String candidate;
        protected String version;
        protected String apiHost = "vendors.sdkman.io";
        protected boolean https = true;

        protected final S self() {
            return (S) this;
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

        /**
         * Use HTTPS
         */
        public S https(boolean https) {
            this.https = https;
            return self();
        }
    }
}
