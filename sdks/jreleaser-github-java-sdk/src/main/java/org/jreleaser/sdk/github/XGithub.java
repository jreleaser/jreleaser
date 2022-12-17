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
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.spi.release.User;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.github.api.GhPackageVersion;
import org.jreleaser.sdk.github.api.GhRelease;
import org.jreleaser.sdk.github.api.GhReleaseNotes;
import org.jreleaser.sdk.github.api.GhReleaseNotesParams;
import org.jreleaser.sdk.github.api.GhSearchUser;
import org.jreleaser.sdk.github.api.GhUser;
import org.jreleaser.sdk.github.api.GithubAPI;
import org.jreleaser.sdk.github.internal.Page;
import org.jreleaser.sdk.github.internal.PaginatingDecoder;
import org.jreleaser.util.CollectionUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
class XGithub {
    public static final String USERS_NOREPLY_GITHUB_COM = "@users.noreply.github.com";
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
            .decoder(new PaginatingDecoder(new JacksonDecoder(objectMapper)))
            .requestInterceptor(template -> template.header("Authorization", String.format("token %s", token)))
            .target(GithubAPI.class, endpoint);
    }

    void updateRelease(String owner, String repo, String tag, Long id, GhRelease release) throws RestAPIException {
        logger.debug(RB.$("git.update.release"), owner, repo, tag);

        api.updateRelease(release, owner, repo, id);
    }

    private String getPrivateEmailUserId(String email) {
        if (!email.endsWith(USERS_NOREPLY_GITHUB_COM)) return null;
        String username = email.substring(0, email.indexOf("@"));
        if (username.contains("+")) {
            username = username.substring(username.indexOf("+") + 1);
        }
        return username;
    }

    Optional<User> findUser(String email, String name) throws RestAPIException {
        logger.debug(RB.$("git.user.lookup"), name, email);

        String username = getPrivateEmailUserId(email);
        if (username != null) {
            GhUser user = api.getUser(username);
            if (user != null) {
                return Optional.of(new User(user.getLogin(), email, user.getHtmlUrl()));
            }
        }

        GhSearchUser search = api.searchUser(CollectionUtils.<String, String>mapOf("q", email));
        if (search.getTotalCount() > 0) {
            GhUser user = search.getItems().get(0);
            return Optional.of(new User(user.getLogin(), email, user.getHtmlUrl()));
        }

        return Optional.empty();
    }

    GhReleaseNotes generateReleaseNotes(String owner, String repo, GhReleaseNotesParams params) throws RestAPIException {
        logger.info(RB.$("github.generate.release.notes"), owner, repo, params.getPreviousTagName(), params.getTagName());

        return api.generateReleaseNotes(params, owner, repo);
    }

    List<GhPackageVersion> listPackageVersions(String packageType, String packageName) throws IOException {
        logger.debug(RB.$("github.list.versions"), packageType, packageName);

        List<GhPackageVersion> issues = new ArrayList<>();
        Page<List<GhPackageVersion>> page = api.listPackageVersions0(packageType, packageName);
        issues.addAll(page.getContent());

        if (page.hasLinks() && page.getLinks().hasNext()) {
            try {
                collectPackageVersions(page, issues);
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }

        return issues;
    }

    private void collectPackageVersions(Page<List<GhPackageVersion>> page, List<GhPackageVersion> issues) throws URISyntaxException {
        URI next = new URI(page.getLinks().next());
        logger.debug(next.toString());

        page = api.listPackageVersions1(next);
        issues.addAll(page.getContent());

        if (page.hasLinks() && page.getLinks().hasNext()) {
            collectPackageVersions(page, issues);
        }
    }

    void deletePackageVersion(String packageType, String packageName, String packageVersion) throws RestAPIException {
        logger.debug(RB.$("github.delete.package.version"), packageVersion, packageName);

        api.deletePackageVersion(packageType, packageName, packageVersion);
    }

    void deletePackage(String packageType, String packageName) throws RestAPIException {
        logger.debug(RB.$("github.delete.package"), packageType, packageName);

        api.deletePackage(packageType, packageName);
    }
}
