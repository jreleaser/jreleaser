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
package org.jreleaser.sdk.zulip;

import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
class Stubs {
    static void verifyPostContains(String endpoint, String maybeJson) {
        verifyRequestContains(postRequestedFor(urlEqualTo(endpoint)), maybeJson);
    }

    private static void verifyRequest(RequestPatternBuilder builder, String maybeJson) {
        verify(builder.withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
            .withHeader("Accept", equalTo("*/*"))
            .withRequestBody(maybeJson.startsWith("{") ? equalToJson(maybeJson) : equalTo(maybeJson)));
    }

    private static void verifyRequestContains(RequestPatternBuilder builder, String maybeJson) {
        verify(builder.withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
            .withHeader("Accept", equalTo("*/*"))
            .withRequestBody(maybeJson.startsWith("{") ? containing(maybeJson.substring(1, maybeJson.length() - 1)) : containing(maybeJson)));
    }
}
