/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.sdk.twitter;

import org.jreleaser.util.SimpleJReleaserLoggerAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.jreleaser.sdk.twitter.ApiEndpoints.UPDATE_STATUS_ENDPOINT;
import static org.jreleaser.sdk.twitter.Stubs.verifyPostContains;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class UpdateStatusTwitterCommandTest {
    @RegisterExtension
    WireMockExtension api = new WireMockExtension(options().dynamicPort());

    @Test
    public void testUpdateStatus() throws TwitterException {
        // given:
        stubFor(post(urlEqualTo(UPDATE_STATUS_ENDPOINT + ".json"))
            .willReturn(okJson("{\"status\": 202, \"message\":\"success\"}")));

        UpdateStatusTwitterCommand command = UpdateStatusTwitterCommand
            .builder(new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.DEBUG))
            .apiHost(api.baseUrl() + "/")
            .consumerKey("CONSUMER_KEY")
            .consumerToken("CONSUMER_TOKEN")
            .accessToken("ACCESS_TOKEN")
            .accessTokenSecret("ACCESS_TOKEN_SECRET")
            .statuses(listOf("success"))
            .build();

        // when:
        command.execute();

        // then:
        verifyPostContains(UPDATE_STATUS_ENDPOINT + ".json",
            "status=success");
    }

    @Test
    public void testUpdateStatuses() throws TwitterException {
        // given:
        stubFor(post(urlEqualTo(UPDATE_STATUS_ENDPOINT + ".json"))
            .willReturn(okJson("{\"status\": 202, \"message\":\"success\"}")));

        UpdateStatusTwitterCommand command = UpdateStatusTwitterCommand
            .builder(new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.DEBUG))
            .apiHost(api.baseUrl() + "/")
            .consumerKey("CONSUMER_KEY")
            .consumerToken("CONSUMER_TOKEN")
            .accessToken("ACCESS_TOKEN")
            .accessTokenSecret("ACCESS_TOKEN_SECRET")
            .statuses(listOf("success", "success", "success"))
            .build();

        // when:
        command.execute();

        // then:
        verifyPostContains(UPDATE_STATUS_ENDPOINT + ".json",
            "status=success");
    }

    @Test
    public void testError() {
        // given:
        stubFor(post(urlEqualTo(UPDATE_STATUS_ENDPOINT + ".json"))
            .willReturn(aResponse().withStatus(400)));

        UpdateStatusTwitterCommand command = UpdateStatusTwitterCommand
            .builder(new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.DEBUG))
            .apiHost(api.baseUrl() + "/")
            .consumerKey("CONSUMER_KEY")
            .consumerToken("CONSUMER_TOKEN")
            .accessToken("ACCESS_TOKEN")
            .accessTokenSecret("ACCESS_TOKEN_SECRET")
            .statuses(listOf("failure"))
            .build();

        // expected:
        assertThrows(TwitterException.class, command::execute);
    }
}
