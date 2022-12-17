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
package org.jreleaser.sdk.github;


import org.jreleaser.logging.SimpleJReleaserLoggerAdapter;
import org.jreleaser.model.spi.release.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.*;

class XGithubTest {

  @RegisterExtension
  WireMockExtension api = new WireMockExtension(options().dynamicPort());

  @ParameterizedTest
  @ValueSource(strings = {"jreleaserbot", "12345+jreleaserbot"})
  @DisplayName("Github user from private email id")
  void userFromPrivateEmailId(String username) throws IOException {
    XGithub github = new XGithub(new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.DEBUG),
            api.baseUrl(),
            "GH_TOKEN",
            10000,
            10000);
    stubFor(get(urlPathEqualTo(ApiEndpoints.GET_USER_JRELEASER))
            .willReturn(aResponse().withStatus(200).withBodyFile("gh_get_user_jreleaser.json")));

    Optional<User> user = github.findUser(username + "@users.noreply.github.com", "jreleaserbot");

    assertThat(user)
            .isNotEmpty()
            .get()
            .extracting("username", "email", "url")
            .containsExactly("jreleaserbot", username + "@users.noreply.github.com", "https://github.com/jreleaserbot");
    assertThat(user)
            .isNotEmpty()
            .get()
            .extracting(u -> u.asLink("test"))
            .isEqualTo("[test](https://github.com/jreleaserbot)");
  }

  @Test
  @DisplayName("Github user not found")
  void userNotFound() throws IOException {
    XGithub github = new XGithub(new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.DEBUG),
            api.baseUrl(),
            "GH_TOKEN",
            10000,
            10000);
    stubFor(get(urlPathEqualTo(ApiEndpoints.SEARCH_USERS))
            .willReturn(aResponse().withStatus(200).withBodyFile("gh_search_user_response_0.json")));

    Optional<User> user = github.findUser("jreleaser@example.com", "JReleaser");

    assertThat(user).isEmpty();
  }

  @Test
  @DisplayName("Github user found with Email")
  void userFoundByEmail() throws IOException {

    XGithub github = new XGithub(new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.DEBUG),
            api.baseUrl(),
            "GH_TOKEN",
            10000,
            10000);

      stubFor(get(urlPathEqualTo(ApiEndpoints.SEARCH_USERS))
              .withQueryParam("q", equalTo("jreleaserbot@example.com"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withHeader("Authorization", equalTo("token GH_TOKEN"))
            .willReturn(aResponse().withStatus(200).withBodyFile("gh_search_user_response_1.json")));

    Optional<User> user = github.findUser("jreleaserbot@example.com", "JReleaser");

    assertThat(user)
            .isNotEmpty()
            .get()
            .extracting("username", "email", "url")
            .containsExactly("jreleaserbot", "jreleaserbot@example.com", "https://github.com/jreleaserbot");
    assertThat(user)
            .isNotEmpty()
            .get()
            .extracting(u -> u.asLink("test"))
            .isEqualTo("[test](https://github.com/jreleaserbot)");
  }

}