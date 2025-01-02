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

import com.github.tomakehurst.wiremock.client.WireMock;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.sdk.linkedin.api.Message;
import org.jreleaser.test.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jreleaser.sdk.linkedin.ApiEndpoints.ME_ENDPOINT;
import static org.jreleaser.sdk.linkedin.ApiEndpoints.SHARES_ENDPOINT;
import static org.jreleaser.test.WireMockStubs.verifyJsonPostContains;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

@DisabledOnOs(WINDOWS)
class LinkedinSdkTest {
    @RegisterExtension
    WireMockExtension api = new WireMockExtension(options().dynamicPort());

    @Test
    void testMessageWithImplicitOwner() throws LinkedinException {
        // given:
        stubFor(get(urlEqualTo(ME_ENDPOINT))
            .withHeader("Authorization", equalTo("Bearer TOKEN"))
            .willReturn(okJson("{\"id\":\"324_kGGaLE\"}")));
        stubFor(post(urlEqualTo(SHARES_ENDPOINT))
            //.withHeader("Authorization", equalTo("Bearer TOKEN"))
            .willReturn(okJson("{\"id\":\"6275832358189047808\"}")));

        LinkedinSdk sdk = LinkedinSdk
            .builder(JReleaserContext.empty().asImmutable())
            .apiHost(api.baseUrl())
            .accessToken("TOKEN")
            .connectTimeout(20)
            .readTimeout(60)
            .dryrun(false)
            .build();

        // when:
        sdk.share("", Message.of("App 1.0.0 released", "App 1.0.0 has been released"));

        // then:
        verifyJsonPostContains(SHARES_ENDPOINT,
            "{\n" +
                "  \"owner\" : \"urn:li:person:324_kGGaLE\",\n" +
                "  \"subject\" : \"App 1.0.0 released\",\n" +
                "  \"text\" : {\n" +
                "    \"text\" : \"App 1.0.0 has been released\"\n" +
                "  }\n" +
                "}");
    }

    @Test
    void testMessageWithExplicitOwner() throws LinkedinException {
        // given:
        stubFor(post(urlEqualTo(SHARES_ENDPOINT))
            .withHeader("Authorization", equalTo("Bearer TOKEN"))
            .willReturn(okJson("{\"id\":\"6275832358189047808\"}")));

        LinkedinSdk sdk = LinkedinSdk
            .builder(JReleaserContext.empty().asImmutable())
            .apiHost(api.baseUrl())
            .accessToken("TOKEN")
            .connectTimeout(20)
            .readTimeout(60)
            .dryrun(false)
            .build();

        // when:
        sdk.share("urn:li:person:324_kGGaLE", Message.of("App 1.0.0 released", "App 1.0.0 has been released"));

        // then:
        verifyJsonPostContains(SHARES_ENDPOINT,
            "{\n" +
                "  \"owner\" : \"urn:li:person:324_kGGaLE\",\n" +
                "  \"subject\" : \"App 1.0.0 released\",\n" +
                "  \"text\" : {\n" +
                "    \"text\" : \"App 1.0.0 has been released\"\n" +
                "  }\n" +
                "}");
    }

    @Test
    void testContent() throws LinkedinException, AnnounceException {
        // given:
        stubFor(get(urlEqualTo(ME_ENDPOINT))
            .willReturn(okJson("{\"id\":\"324_kGGaLE\"}")));
        stubFor(post(urlEqualTo(SHARES_ENDPOINT))
            .willReturn(okJson("{\"id\":\"6275832358189047808\"}")));

        LinkedinSdk sdk = LinkedinSdk
            .builder(JReleaserContext.empty().asImmutable())
            .apiHost(api.baseUrl())
            .accessToken("TOKEN")
            .connectTimeout(20)
            .readTimeout(60)
            .dryrun(false)
            .build();

        // when:
        sdk.share("", "App 1.0.0 released",
            "{\n" +
                "    \"content\": {\n" +
                "        \"contentEntities\": [\n" +
                "            {\n" +
                "                \"entityLocation\": \"https://www.example.com/content.html\",\n" +
                "                \"thumbnails\": [\n" +
                "                    {\n" +
                "                        \"resolvedUrl\": \"https://www.example.com/image.jpg\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"title\": \"App 1.0.0 released\"\n" +
                "    },\n" +
                "    \"distribution\": {\n" +
                "        \"linkedInDistributionTarget\": {}\n" +
                "    },\n" +
                "    \"owner\": \"{{linkedinOwner}}\",\n" +
                "    \"subject\": \"App 1.0.0 released\",\n" +
                "    \"text\": {\n" +
                "        \"text\": \"App 1.0.0 has been released\"\n" +
                "    }\n" +
                "}");

        // then:
        verifyJsonPostContains(SHARES_ENDPOINT,
            "{\n" +
                "    \"content\": {\n" +
                "        \"contentEntities\": [\n" +
                "            {\n" +
                "                \"entityLocation\": \"https://www.example.com/content.html\",\n" +
                "                \"thumbnails\": [\n" +
                "                    {\n" +
                "                        \"resolvedUrl\": \"https://www.example.com/image.jpg\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"title\": \"App 1.0.0 released\"\n" +
                "    },\n" +
                "    \"distribution\": {\n" +
                "        \"linkedInDistributionTarget\": {}\n" +
                "    },\n" +
                "    \"owner\": \"urn:li:person:324_kGGaLE\",\n" +
                "    \"subject\": \"App 1.0.0 released\",\n" +
                "    \"text\": {\n" +
                "        \"text\": \"App 1.0.0 has been released\"\n" +
                "    }\n" +
                "}");
    }

    @Test
    void testDryrun() throws LinkedinException {
        // given:
        stubFor(get(urlEqualTo(ME_ENDPOINT))
            .withHeader("Authorization", equalTo("Bearer TOKEN"))
            .willReturn(okJson("{\"id\":\"324_kGGaLE\"}")));
        stubFor(post(urlEqualTo(SHARES_ENDPOINT))
            .withHeader("Authorization", equalTo("Bearer TOKEN"))
            .willReturn(okJson("{\"id\":\"6275832358189047808\"}")));

        LinkedinSdk sdk = LinkedinSdk
            .builder(JReleaserContext.empty().asImmutable())
            .apiHost(api.baseUrl())
            .accessToken("TOKEN")
            .connectTimeout(20)
            .readTimeout(60)
            .dryrun(true)
            .build();

        // when:
        sdk.share("", Message.of("App 1.0.0 released", "App 1.0.0 has been released"));

        // then:
        assertThat(WireMock.findUnmatchedRequests())
            .isEmpty();
    }

    @Test
    void testError() {
        // given:
        stubFor(get(urlEqualTo(ME_ENDPOINT))
            .withHeader("Authorization", equalTo("Bearer TOKEN"))
            .willReturn(aResponse().withStatus(400)));
        stubFor(post(urlEqualTo(SHARES_ENDPOINT))
            .withHeader("Authorization", equalTo("Bearer TOKEN"))
            .willReturn(aResponse().withStatus(400)));

        LinkedinSdk sdk = LinkedinSdk
            .builder(JReleaserContext.empty().asImmutable())
            .apiHost(api.baseUrl())
            .accessToken("TOKEN")
            .connectTimeout(20)
            .readTimeout(60)
            .dryrun(false)
            .build();

        // expected:
        assertThrows(LinkedinException.class, () -> sdk.share("", Message.of("App 1.0.0 released", "App 1.0.0 has been released")));
    }
}
