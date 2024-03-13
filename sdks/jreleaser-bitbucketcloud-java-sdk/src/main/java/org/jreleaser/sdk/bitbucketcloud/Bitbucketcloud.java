/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
package org.jreleaser.sdk.bitbucketcloud;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.Feign;
import feign.Request;
import feign.Response;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.sdk.bitbucketcloud.api.BBCRepository;
import org.jreleaser.sdk.bitbucketcloud.api.BitbucketcloudAPI;
import org.jreleaser.sdk.commons.RestAPIException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Hasnae Rehioui
 * @since 1.7.0
 */
public class Bitbucketcloud {
    private static final String ENDPOINT = "https://api.bitbucket.org/2.0";

    private final JReleaserLogger logger;
    private final BitbucketcloudAPI api;

    public Bitbucketcloud(JReleaserLogger logger, String token, int connectTimeout, int readTimeout) {
        this.logger = logger;
        ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

        this.api = Feign.builder()
            .encoder(new FormEncoder())
            .decoder(new JacksonDecoder(objectMapper))
            .requestInterceptor(template -> {
                    template.header("User-Agent", "JReleaser/" + JReleaserVersion.getPlainVersion());
                    template.header("Authorization", "Bearer " + token);
                }
            )
            .options(new Request.Options(connectTimeout, TimeUnit.SECONDS, readTimeout, TimeUnit.SECONDS, true))
            .target(BitbucketcloudAPI.class, ENDPOINT);
    }

    public JReleaserLogger getLogger() {
        return logger;
    }

    public Response uploadArtifact(String workspace, String repoName, Path artifact) throws IOException {
        logger.debug(RB.$("uploader.uploading.to", "bitbucketcloud"));
        return api.uploadArtifact(workspace, repoName, new File[] {
            artifact.toFile()
        });
    }

    public BBCRepository findRepository(String owner, String repo) {
        logger.debug(RB.$("git.repository.lookup"), owner, repo);
        try {
            return api.getRepository(owner, repo);
        } catch (RestAPIException e) {
            if (e.isNotFound()) {
                return null;
            }
            throw e;
        }
    }

    public BBCRepository createRepository(String owner, String repo) {
        logger.debug(RB.$("git.repository.create"), owner, repo);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("scm", "git");

        return api.createRepository(owner, repo, data);
    }
}
