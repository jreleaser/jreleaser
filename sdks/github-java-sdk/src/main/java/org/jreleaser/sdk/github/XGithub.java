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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.form.FormEncoder;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.releaser.spi.User;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.github.api.GhRelease;
import org.jreleaser.sdk.github.api.GhReleaseNotes;
import org.jreleaser.sdk.github.api.GhReleaseNotesParams;
import org.jreleaser.sdk.github.api.GhSearchUser;
import org.jreleaser.sdk.github.api.GhUser;
import org.jreleaser.sdk.github.api.GithubAPI;
import org.jreleaser.util.CollectionUtils;
import org.jreleaser.util.JReleaserLogger;

import java.io.IOException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
class XGithub {
    private final JReleaserLogger logger;
    private final GithubAPI api;

    XGithub(JReleaserLogger logger,
            String endpoint,
            String token,
            int connectTimeout,
            int readTimeout) throws IOException {
        requireNonNull(logger, "'logger' must not be null");
        requireNonBlank(token, "'token' must not be blank");
        requireNonBlank(endpoint, "'endpoint' must not be blank");

        ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

        this.logger = logger;
        this.api = ClientUtils.builder(logger, connectTimeout, readTimeout)
            .client(new ApacheHttpClient())
            .encoder(new FormEncoder(new JacksonEncoder(objectMapper)))
            .decoder(new JacksonDecoder(objectMapper))
            .requestInterceptor(template -> template.header("Authorization", String.format("token %s", token)))
            .target(GithubAPI.class, endpoint);
    }

    void updateRelease(String owner, String repo, String tag, Long id, GhRelease release) throws RestAPIException {
        logger.debug(RB.$("git.update.release"), owner, repo, tag);

        api.updateRelease(release, owner, repo, id);
    }

    Optional<User> findUser(String email, String name) throws RestAPIException {
        logger.debug(RB.$("git.user.lookup"), name, email);

        GhSearchUser search = api.searchUser(CollectionUtils.<String, String>mapOf("q", email));
        if (search.getTotalCount() > 0) {
            GhUser user = search.getItems().get(0);
            return Optional.of(new User(user.getLogin(), email, user.getHtmlUrl()));
        }

        // use full name instead
        String query = "fullname:" + name + " type:user";
        search = api.searchUser(CollectionUtils.<String, String>mapOf("q", query));
        if (search.getTotalCount() > 0) {
            GhUser user = search.getItems().get(0);
            GhUser test = api.getUser(user.getLogin());
            if (name.equals(test.getName())) {
                return Optional.of(new User(user.getLogin(), email, user.getHtmlUrl()));
            }
        }

        return Optional.empty();
    }

    GhReleaseNotes generateReleaseNotes(String owner, String repo, GhReleaseNotesParams params) throws RestAPIException {
        logger.info(RB.$("github.generate.release.notes"), owner, repo, params.getPreviousTagName(), params.getTagName());

        return api.generateReleaseNotes(params, owner, repo);
    }
}
