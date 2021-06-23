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

import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
class Stubs {
    static void verifyPost(String endpoint, String json) {
        verifyRequest(postRequestedFor(urlEqualTo(endpoint)), json);
    }

    static void verifyPut(String endpoint, String json) {
        verifyRequest(putRequestedFor(urlEqualTo(endpoint)), json);
    }

    private static void verifyRequest(RequestPatternBuilder builder, String json) {
        verify(builder.withHeader("Content-Type", equalTo("application/json"))
            .withHeader("Accept", equalTo("application/json"))
            .withHeader("Consumer-Key", equalTo("CONSUMER_KEY"))
            .withHeader("Consumer-Token", equalTo("CONSUMER_TOKEN"))
            .withRequestBody(equalToJson(json)));
    }
}
