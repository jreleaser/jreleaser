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
package org.jreleaser.sdk.reddit;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.test.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RedditSdkTest {

    private static final String ACCESS_TOKEN_ENDPOINT = "/api/v1/access_token";
    private static final String SUBMIT_ENDPOINT = "/api/submit";

    @RegisterExtension
    WireMockExtension api = new WireMockExtension(options()
        .extensions(new ResponseTemplateTransformer(false))
        .dynamicPort());

    @Test
    void testSubmitTextPost() throws RedditSdkException {
        // given:
        stubFor(post(urlEqualTo(ACCESS_TOKEN_ENDPOINT))
            .willReturn(ok("{\"access_token\":\"test-token\",\"token_type\":\"bearer\",\"expires_in\":3600}")));
        stubFor(post(urlEqualTo(SUBMIT_ENDPOINT))
            .willReturn(ok("{\"json\":{\"errors\":[]}}")));

        RedditSdk sdk = baseBuilder().build();

        // when:
        sdk.submitTextPost("test", "Test Title", "Test content for self post");

        // then:
        assertThat(WireMock.findUnmatchedRequests()).isEmpty();
    }

    @Test
    void testSubmitLinkPost() throws RedditSdkException {
        // given:
        stubFor(post(urlEqualTo(ACCESS_TOKEN_ENDPOINT))
            .willReturn(ok("{\"access_token\":\"test-token\",\"token_type\":\"bearer\",\"expires_in\":3600}")));
        stubFor(post(urlEqualTo(SUBMIT_ENDPOINT))
            .willReturn(ok("{\"json\":{\"errors\":[]}}")));

        RedditSdk sdk = baseBuilder().build();

        // when:
        sdk.submitLinkPost("test", "Test Title", "https://example.com/release");

        // then:
        assertThat(WireMock.findUnmatchedRequests()).isEmpty();
    }

    @Test
    void testMultipleSubmissions() throws RedditSdkException {
        // given:
        stubFor(post(urlEqualTo(ACCESS_TOKEN_ENDPOINT))
            .willReturn(ok("{\"access_token\":\"test-token\",\"token_type\":\"bearer\",\"expires_in\":3600}")));
        stubFor(post(urlEqualTo(SUBMIT_ENDPOINT))
            .willReturn(ok("{\"json\":{\"errors\":[]}}")));

        RedditSdk sdk = baseBuilder().build();

        // when:
        sdk.submitTextPost("test1", "First Post", "First content");
        sdk.submitLinkPost("test2", "Second Post", "https://example.com");
        sdk.submitTextPost("test3", "Third Post", "Third content");

        // then:
        assertThat(WireMock.findUnmatchedRequests()).isEmpty();
    }

    @Test
    void testDryRun() throws RedditSdkException {
        // given:
        RedditSdk sdk = baseBuilder().dryrun(true).build();

        // when:
        sdk.submitTextPost("test", "Test Title", "Test content");

        // then:
        assertThat(WireMock.findUnmatchedRequests()).isEmpty();
    }

    @Test
    void testError() {
        // given:
        stubFor(post(urlEqualTo(ACCESS_TOKEN_ENDPOINT))
            .willReturn(aResponse().withStatus(400)));
        stubFor(post(urlEqualTo(SUBMIT_ENDPOINT))
            .willReturn(aResponse().withStatus(400)));

        RedditSdk sdk = baseBuilder().build();

        // expected:
        assertThrows(RedditSdkException.class, () -> sdk.submitTextPost("test", "Test Title", "Test content"));
    }

    private RedditSdk.Builder baseBuilder() {
        return RedditSdk
            .builder(JReleaserContext.empty().asImmutable())
            .clientId("test-client-id")
            .clientSecret("test-client-secret")
            .username("testuser")
            .password("testpass")
            .baseUrl(api.baseUrl())
            .oauthUrl(api.baseUrl())
            .connectTimeout(20)
            .readTimeout(60)
            .dryrun(false);
    }
}