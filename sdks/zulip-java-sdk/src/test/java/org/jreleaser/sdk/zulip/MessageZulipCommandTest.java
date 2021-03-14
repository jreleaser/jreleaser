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
package org.jreleaser.sdk.zulip;

import org.jreleaser.util.SimpleJReleaserLoggerAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.jreleaser.sdk.zulip.ApiEndpoints.MESSAGES_ENDPOINT;
import static org.jreleaser.sdk.zulip.Stubs.verifyPostContains;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class MessageZulipCommandTest {
    @RegisterExtension
    WireMockExtension api = new WireMockExtension(options().dynamicPort());

    @Test
    public void testMessage() throws ZulipException {
        // given:
        stubFor(post(urlEqualTo(MESSAGES_ENDPOINT))
            .willReturn(okJson("{\"status\": 202, \"message\":\"success\"}")));

        MessageZulipCommand command = MessageZulipCommand
            .builder(new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.DEBUG))
            .apiHost(api.baseUrl())
            .account("ACCOUNT")
            .apiKey("API_KEY")
            .channel("announce")
            .subject("App 1.0.0")
            .message("App 1.0.0 has been released")
            .build();

        // when:
        command.execute();

        // then:
        verifyPostContains(MESSAGES_ENDPOINT,
            "type=stream");
        verifyPostContains(MESSAGES_ENDPOINT,
            "to=announce");
        verifyPostContains(MESSAGES_ENDPOINT,
            "subject=App+1.0.0");
        verifyPostContains(MESSAGES_ENDPOINT,
            "content=App+1.0.0+has+been+released");
    }

    @Test
    public void testError() {
        // given:
        stubFor(post(urlEqualTo(MESSAGES_ENDPOINT))
            .willReturn(aResponse().withStatus(400)));

        MessageZulipCommand command = MessageZulipCommand
            .builder(new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.DEBUG))
            .apiHost(api.baseUrl())
            .account("ACCOUNT")
            .apiKey("API_KEY")
            .channel("announce")
            .subject("App 1.0.0")
            .message("App 1.0.0 has been released")
            .build();

        // expected:
        assertThrows(ZulipException.class, command::execute);
    }
}
