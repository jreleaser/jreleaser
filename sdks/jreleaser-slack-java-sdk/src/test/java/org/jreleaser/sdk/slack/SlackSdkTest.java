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
package org.jreleaser.sdk.slack;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.test.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jreleaser.sdk.slack.ApiEndpoints.MESSAGES_ENDPOINT;
import static org.jreleaser.test.WireMockStubs.verifyPostContains;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SlackSdkTest {
    @RegisterExtension
    WireMockExtension api = new WireMockExtension(options().dynamicPort());

    @Test
    void testMessage() throws SlackException {
        // given:
        stubFor(post(urlEqualTo(MESSAGES_ENDPOINT))
            .willReturn(okJson("{\"status\": 202, \"ok\":\"true\"}")));

        SlackSdk sdk = SlackSdk
            .builder(JReleaserContext.empty().asImmutable())
            .token("TOKEN")
            .apiHost(api.baseUrl())
            .connectTimeout(20)
            .readTimeout(60)
            .dryrun(false)
            .build();

        // when:
        sdk.message("#announce", "App 1.0.0 has been released");

        // then:
        verifyPostContains(MESSAGES_ENDPOINT,
            "channel=%23announce");
        verifyPostContains(MESSAGES_ENDPOINT,
            "text=App+1.0.0+has+been+released");
    }

    @Test
    void testDryrun() throws SlackException {
        // given:
        stubFor(post(urlEqualTo(MESSAGES_ENDPOINT))
            .willReturn(okJson("{\"status\": 202, \"ok\":\"true\"}")));

        SlackSdk sdk = SlackSdk
            .builder(JReleaserContext.empty().asImmutable())
            .token("TOKEN")
            .apiHost(api.baseUrl())
            .connectTimeout(20)
            .readTimeout(60)
            .dryrun(true)
            .build();

        // when:
        sdk.message("#announce", "App 1.0.0 has been released");

        // then:
        assertThat(WireMock.findUnmatchedRequests())
            .isEmpty();
    }

    @Test
    void testError() {
        // given:
        stubFor(post(urlEqualTo(MESSAGES_ENDPOINT))
            .willReturn(aResponse().withStatus(400)));

        SlackSdk sdk = SlackSdk
            .builder(JReleaserContext.empty().asImmutable())
            .token("TOKEN")
            .apiHost(api.baseUrl())
            .connectTimeout(20)
            .readTimeout(60)
            .dryrun(false)
            .build();

        // expected:
        assertThrows(SlackException.class, () -> sdk.message("#announce", "App 1.0.0 has been released"));
    }
}
