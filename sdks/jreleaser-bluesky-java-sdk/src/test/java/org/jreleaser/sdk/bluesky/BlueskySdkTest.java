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

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import org.jreleaser.logging.SimpleJReleaserLoggerAdapter;
import org.jreleaser.test.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jreleaser.test.WireMockStubs.verifyJsonRequestContains;

class BlueskySdkTest {

    private static final String RECORD_ENDPOINT = "/xrpc/com.atproto.repo.createRecord";

    @RegisterExtension
    WireMockExtension api = new WireMockExtension(options()
        .extensions(new ResponseTemplateTransformer(false))
        .dynamicPort());

    @Test
    void testUpdateStatus() throws BlueskyException {
        BlueskySdk sdk = baseBuilder().build();

        sdk.skeet(Collections.singletonList("success"));

        verifyJsonRequestContains(postRequestedFor(urlEqualTo(RECORD_ENDPOINT)), "\"text\" : \"success\"");
    }

    @Test
    void givenDryRun_whenExecutingSkeet_shouldNotMatchAnyEndpoint() throws BlueskyException {
        BlueskySdk sdk = baseBuilder()
            .dryrun(true)
            .build();

        sdk.skeet(Collections.singletonList("success"));

        // then:
        assertThat(WireMock.findUnmatchedRequests())
            .isEmpty();
    }

    @Test
    void testUpdateStatuses() throws BlueskyException {
        BlueskySdk sdk = baseBuilder().build();

        sdk.skeet(List.of("success-one", "success-two", "success-three"));

        api.verify(postRequestedFor(urlEqualTo(RECORD_ENDPOINT))
            .withRequestBody(matchingJsonPath("$.record.text", equalTo("success-one"))
                .and(matchingJsonPath("$.record.reply", absent()))));

        api.verify(postRequestedFor(urlEqualTo(RECORD_ENDPOINT))
            .withRequestBody(matchingJsonPath("$.record.text", equalTo("success-two"))
                .and(matchingJsonPath("$.record.reply.root.uri", equalTo("success-one")))
                .and(matchingJsonPath("$.record.reply.parent.uri", equalTo("success-one")))
            ));

        api.verify(postRequestedFor(urlEqualTo(RECORD_ENDPOINT))
            .withRequestBody(matchingJsonPath("$.record.text", equalTo("success-three"))
                .and(matchingJsonPath("$.record.reply.root.uri", equalTo("success-one")))
                .and(matchingJsonPath("$.record.reply.parent.uri", equalTo("success-two")))
            ));
    }

    private BlueskySdk.Builder baseBuilder() {
        return BlueskySdk
            .builder(new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.DEBUG))
            .handle("API-HANDLE")
            .password("API-PASSWORD")
            .host(api.baseUrl())
            .dryrun(false)
            .connectTimeout(20)
            .readTimeout(60);
    }
}
