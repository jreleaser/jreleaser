/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.sdk.sdkman;

import org.jreleaser.util.SimpleJReleaserLoggerAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.jreleaser.sdk.sdkman.ApiEndpoints.ANNOUNCE_ENDPOINT;
import static org.jreleaser.sdk.sdkman.Stubs.verifyPost;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class AnnounceSdkmanCommandTest {
    @RegisterExtension
    WireMockExtension api = new WireMockExtension(options().dynamicPort());

    @Test
    public void testStructuredAnnouncement() throws SdkmanException {
        // given:
        stubFor(post(urlEqualTo(ANNOUNCE_ENDPOINT))
            .willReturn(okJson("{\"status\": 202, \"message\":\"success\"}")));

        AnnounceSdkmanCommand command = AnnounceSdkmanCommand
            .builder(new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.DEBUG))
            .apiHost(api.baseUrl())
            .consumerKey("CONSUMER_KEY")
            .consumerToken("CONSUMER_TOKEN")
            .candidate("jreleaser")
            .version("1.0.0")
            .build();

        // when:
        command.execute();

        // then:
        verifyPost(ANNOUNCE_ENDPOINT, "{\n" +
            "   \"candidate\": \"jreleaser\",\n" +
            "   \"version\": \"1.0.0\"\n" +
            "}");
    }

    @Test
    public void testError() {
        // given:
        stubFor(post(urlEqualTo(ANNOUNCE_ENDPOINT))
            .willReturn(aResponse().withStatus(400)));

        AnnounceSdkmanCommand command = AnnounceSdkmanCommand
            .builder(new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.DEBUG))
            .apiHost(api.baseUrl())
            .consumerKey("CONSUMER_KEY")
            .consumerToken("CONSUMER_TOKEN")
            .candidate("jreleaser")
            .version("1.0.0")
            .build();

        // expected:
        assertThrows(SdkmanException.class, command::execute);
    }
}
