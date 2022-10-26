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
package org.jreleaser.sdk.discourse;

import org.jreleaser.logging.SimpleJReleaserLoggerAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.jreleaser.sdk.discourse.ApiEndpoints.CATEGORIES_ENDPOINT;
import static org.jreleaser.sdk.discourse.ApiEndpoints.POSTS_ENDPOINT;

/**
 * @author shblue21
 * @since 1.3.0
 */
public class DiscoursePostTest {
    @RegisterExtension
    WireMockExtension api = new WireMockExtension(options().dynamicPort());

    @Test
    public void testMessage() throws DiscourseException {
        // given:
        stubFor(post(urlEqualTo(POSTS_ENDPOINT))
            .willReturn(okJson("{\"topic_id\": 1, \"post_number\": 1}")));
        stubFor(get(urlEqualTo(CATEGORIES_ENDPOINT))
            .willReturn(okJson("{\"category_list\":{\"categories\":[{\"id\":1, \"name\":\"announce\"}]}}")));


        DiscourseSdk sdk = DiscourseSdk
            .builder(new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.DEBUG))
            .userName("API-USERNAME")
            .apiKey("API-KEY")
            .host(api.baseUrl())
            .build();
        // when:
        sdk.createPost("App 1.0.0", "App 1.0.0 has been released", "announce");


        // then:
        Stubs.verifyPost(POSTS_ENDPOINT, "{\n" +
                "   \"title\": \"App 1.0.0\",\n" +
                "   \"raw\": \"App 1.0.0 has been released\",\n" +
                "    \"category\": \"1\" \n" +
                "}"
        );
    }
}
