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
package org.jreleaser.sdk.zernio;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.sdk.zernio.api.Accounts;
import org.jreleaser.sdk.zernio.api.Platform;
import org.jreleaser.test.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.unauthorized;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.jreleaser.test.WireMockStubs.verifyJsonRequestContains;

class ZernioSdkTest {

    private static final String API_V_1_ACCOUNTS = "/accounts";
    private static final String API_V_1_POSTS = "/posts";

    @RegisterExtension
    WireMockExtension api = new WireMockExtension(options()
        .extensions(new ResponseTemplateTransformer(false))
        .dynamicPort());

    @Test
    void testRetrieveAccountsFailsWith401() {
        ZernioSdk sdk = baseBuilder()
            .build();

        api.stubFor(get(urlEqualTo(API_V_1_ACCOUNTS)).willReturn(unauthorized()));

        assertThatThrownBy(() -> sdk.listAccounts(""))
            .isInstanceOf(ZernioException.class)
            .hasMessage("Unexpected error");
    }

    @Test
    void testRetrieveAccountsWithInvalidJSON() {
        ZernioSdk sdk = baseBuilder()
            .build();

        api.stubFor(get(urlEqualTo(API_V_1_ACCOUNTS)).willReturn(okJson("wrong json")));

        assertThatThrownBy(() -> sdk.listAccounts(""))
            .isInstanceOf(ZernioException.class)
            .hasMessage("Unexpected error");
    }

    @Test
    void testRetrieveAccountsEmpty() throws ZernioException {
        ZernioSdk sdk = baseBuilder()
            .build();

        api.stubFor(get(urlEqualTo(API_V_1_ACCOUNTS)).willReturn(okJson("{\"accounts\": []}")));

        assertThat(sdk.listAccounts(""))
            .isNotNull()
            .extracting(Accounts::getAccounts)
            .asInstanceOf(LIST)
            .isEmpty();
    }

    @Test
    void testRetrieveAccounts() throws ZernioException {
        ZernioSdk sdk = baseBuilder()
            .build();

        String responseJSON = """
            {
              "accounts": [
                {
                  "_id": "12",
                  "platform": "twitter",
                  "profileId": {
                    "_id": "13",
                    "name": "My Brand",
                    "slug": "my-brand"
                  },
                  "username": "@acme",
                  "displayName": "Acme",
                  "profileUrl": "https://x.com/acme",
                  "isActive": true
                },
                {
                  "_id": "22",
                  "platform": "bluesky",
                  "profileId": {
                    "_id": "23",
                    "name": "My Brand",
                    "slug": "my-brand"
                  },
                  "username": "@acme",
                  "displayName": "Acme",
                  "profileUrl": "https://bluesky.com/acme",
                  "isActive": false
                }
              ],
              "hasAnalyticsAccess": false
            }
            """;
        api.stubFor(get(urlEqualTo(API_V_1_ACCOUNTS)).willReturn(okJson(responseJSON)));

        Accounts accounts = sdk.listAccounts("");
        assertThat(accounts).isNotNull();
        assertThat(accounts.getAccounts())
            .hasSize(2)
            .extracting("id", "platform", "active")
            .containsExactlyInAnyOrder(
                tuple("12", "twitter", true),
                tuple("22", "bluesky", false)
            );
    }

    @Test
    void testPost() throws ZernioException {
        ZernioSdk sdk = baseBuilder()
            .build();

        String responseJSON = """
            {
              "post": {
                "_id": "65f1c0a9e2b5af0012ab34cd",
                "title": "Launch post",
                "content": "We just launched!",
                "status": "scheduled",
                "scheduledFor": "2024-11-01T10:00:00Z",
                "timezone": "UTC",
                "platforms": [
                  {
                    "platform": "twitter",
                    "accountId": {
                      "_id": "12",
                      "platform": "twitter",
                      "username": "@acme",
                      "displayName": "Acme Corp",
                      "isActive": true
                    },
                    "status": "pending"
                  }
                ]
              },
              "message": "Post scheduled successfully"
            }
            """;
        api.stubFor(post(urlEqualTo(API_V_1_POSTS)).willReturn(okJson(responseJSON).withStatus(201)));

        Platform platform = new Platform();
        platform.setPlatform("twitter");
        platform.setAccountId("12");

        sdk.post("Hello world!", Set.of(platform));

        RequestPatternBuilder builder = postRequestedFor(
            urlEqualTo(API_V_1_POSTS)).withHeader("Content-Type", equalTo("application/json")
        );
        String expectedRequest = """
            {
                "content": "Hello world!",
                "platforms": [
                  {
                    "platform": "twitter",
                    "accountId": "12"
                  }
                ],
                "publishNow": true
            }
            """;
        verifyJsonRequestContains(builder, expectedRequest);
    }

    private ZernioSdk.Builder baseBuilder() {
        return ZernioSdk
            .builder(JReleaserContext.empty().asImmutable())
            .token("API-TOKEN")
            .apiHost(api.baseUrl())
            .dryrun(false)
            .connectTimeout(20)
            .readTimeout(60);
    }
}
