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
package org.jreleaser.sdk.gitea;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.Feign;
import feign.Request;
import feign.form.FormData;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.jreleaser.sdk.gitea.api.GiteaAPI;
import org.jreleaser.sdk.gitea.api.GiteaAPIException;
import org.jreleaser.sdk.gitea.api.GtOrganization;
import org.jreleaser.sdk.gitea.api.GtRelease;
import org.jreleaser.sdk.gitea.api.GtRepository;
import org.jreleaser.util.CollectionUtils;
import org.jreleaser.util.JReleaserLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
class Gitea {
    private static final String API_V1 = "/api/v1";
    private final Tika tika = new Tika();

    private final JReleaserLogger logger;
    private final GiteaAPI api;

    Gitea(JReleaserLogger logger, String endpoint, String token) throws IOException {
        requireNonNull(logger, "'logger' must not be blank");
        requireNonBlank(token, "'token' must not be blank");
        requireNonBlank(endpoint, "'endpoint' must not be blank");

        if (!endpoint.endsWith(API_V1)) {
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
            endpoint += API_V1;
        }

        ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

        this.logger = logger;
        this.api = Feign.builder()
            .encoder(new FormEncoder(new JacksonEncoder(objectMapper)))
            .decoder(new JacksonDecoder(objectMapper))
            .requestInterceptor(template -> template.header("Authorization", String.format("token %s", token)))
            .errorDecoder((methodKey, response) -> new GiteaAPIException(response.status(), response.reason(), response.headers()))
            .options(new Request.Options(20, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, true))
            .target(GiteaAPI.class, endpoint);
    }

    GtRepository findRepository(String owner, String repo) {
        logger.debug("Lookup repository {}/{}", owner, repo);
        try {
            return api.getRepository(owner, repo);
        } catch (GiteaAPIException e) {
            if (e.isNotFound()) {
                // ok
                return null;
            }
            throw e;
        }
    }

    GtRepository createRepository(String owner, String repo) {
        logger.debug("Creating repository {}/{}", owner, repo);

        Map<String, Object> params = CollectionUtils.<String, Object>map()
            .e("name", repo)
            .e("private", false);

        GtOrganization organization = resolveOrganization(owner);
        if (null != organization) {
            return api.createRepository(params, owner);
        }

        return api.createRepository(params);
    }

    private GtOrganization resolveOrganization(String name) {
        try {
            return api.getOrganization(name);
        } catch (GiteaAPIException e) {
            if (e.isNotFound()) {
                // ok
                return null;
            }
            throw e;
        }
    }

    GtRelease findReleaseByTag(String owner, String repo, String tagName) {
        logger.debug("Fetching release on {}/{} with tag {}", owner, repo, tagName);

        try {
            return api.getReleaseByTagName(owner, repo, tagName);
        } catch (GiteaAPIException e) {
            if (e.isNotFound()) {
                // ok
                return null;
            }
            throw e;
        }
    }

    void deleteRelease(String owner, String repo, String tagName, Integer id) throws GiteaAPIException {
        logger.debug("Deleting release {} from {}/{} ({})", tagName, owner, repo, id);

        try {
            api.deleteRelease(owner, repo, id);
        } catch (GiteaAPIException e) {
            if (e.isNotFound()) {
                // OK. Release might have been deleted but
                // tag still exists.
                return;
            }
            throw e;
        }
    }

    void deleteTag(String owner, String repo, String tagName) throws GiteaAPIException {
        logger.debug("Deleting tag {} from {}/{}", tagName, owner, repo);

        api.deleteTag(owner, repo, tagName);
    }

    GtRelease createRelease(String owner, String repo, GtRelease release) throws GiteaAPIException {
        logger.debug("Creating release on {}/{} with tag {}", owner, repo, release.getTagName());

        return api.createRelease(release, owner, repo);
    }

    void uploadAssets(String owner, String repo, GtRelease release, List<Path> assets) throws IOException {
        for (Path asset : assets) {
            if (0 == asset.toFile().length() || !Files.exists(asset)) {
                // do not upload empty or non existent files
                continue;
            }

            logger.info(" - Uploading {}", asset.getFileName().toString());
            try {
                api.uploadAsset(owner, repo, release.getId(), toFormData(asset));
            } catch (GiteaAPIException e) {
                logger.error(" x Failed to upload {}", asset.getFileName());
                throw e;
            }
        }
    }

    private FormData toFormData(Path asset) throws IOException {
        return FormData.builder()
            .fileName(asset.getFileName().toString())
            .contentType(MediaType.parse(tika.detect(asset)).toString())
            .data(Files.readAllBytes(asset))
            .build();
    }
}
