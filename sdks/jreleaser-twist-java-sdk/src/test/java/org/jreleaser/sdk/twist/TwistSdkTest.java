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
package org.jreleaser.sdk.twist;

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

class TwistSdkTest {

    private static final String ADD_THREAD_API_ENDPOINT = "/api/v3/threads/add";
    private static final String ADD_COMMENT_API_ENDPOINT = "/api/v3/comments/add";

    @RegisterExtension
    WireMockExtension api = new WireMockExtension(options()
        .extensions(new ResponseTemplateTransformer(false))
        .dynamicPort());

    @Test
    void testCreateThread() throws TwistSdkException {
        // given:
        stubFor(post(urlEqualTo(ADD_THREAD_API_ENDPOINT))
            .willReturn(ok("{\"id\":45678,\"title\":\"Test Release\",\"content\":\"Release announcement\"}")));

        TwistSdk sdk = baseBuilder().build();

        // when:
        sdk.createThread("6984", "Test Release", "Release announcement");

        // then:
        assertThat(WireMock.findUnmatchedRequests()).isEmpty();
    }

    @Test
    void testCreateComment() throws TwistSdkException {
        // given:
        stubFor(post(urlEqualTo(ADD_COMMENT_API_ENDPOINT))
            .willReturn(ok("{\"id\":98765,\"content\":\"Test comment\"}")));

        TwistSdk sdk = baseBuilder().build();

        // when:
        sdk.createComment("32038", "Test comment");

        // then:
        assertThat(WireMock.findUnmatchedRequests()).isEmpty();
    }

    @Test
    void testErrorOnCreateThread() {
        // given:
        stubFor(post(urlEqualTo(ADD_THREAD_API_ENDPOINT))
            .willReturn(aResponse().withStatus(400)));

        TwistSdk sdk = baseBuilder().build();

        // expected:
        assertThrows(TwistSdkException.class, () -> sdk.createThread("6984", "Title", "Content"));
    }

    @Test
    void testErrorOnCreateComment() {
        // given:
        stubFor(post(urlEqualTo(ADD_COMMENT_API_ENDPOINT))
            .willReturn(aResponse().withStatus(400)));

        TwistSdk sdk = baseBuilder().build();

        // expected:
        assertThrows(TwistSdkException.class, () -> sdk.createComment("123", "content"));
    }

    @Test
    void testDryRun() throws TwistSdkException {
        // given:
        TwistSdk sdk = baseBuilder().dryrun(true).build();

        // when:
        sdk.createThread("6984", "Test Title", "Test content");
        sdk.createComment("123", "Test comment");

        // then:
        assertThat(WireMock.findUnmatchedRequests()).isEmpty();
    }

    private TwistSdk.Builder baseBuilder() {
        return TwistSdk
            .builder(JReleaserContext.empty().asImmutable())
            .apiHost(api.baseUrl())
            .accessToken("TOKEN")
            .connectTimeout(20)
            .readTimeout(60)
            .dryrun(false);
    }
}