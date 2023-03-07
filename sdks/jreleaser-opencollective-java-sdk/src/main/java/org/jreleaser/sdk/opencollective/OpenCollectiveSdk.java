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
package org.jreleaser.sdk.opencollective;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.opencollective.api.Envelope;
import org.jreleaser.sdk.opencollective.api.Mutation;
import org.jreleaser.sdk.opencollective.api.OpenCollectiveAPI;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.CollectionUtils.map;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 1.6.0
 */
public class OpenCollectiveSdk {
    private static final String MUTATION_CREATE_UPDATE = "mutation (\n" +
        "  $update: UpdateCreateInput!\n" +
        ") {\n" +
        "  createUpdate(update: $update) {\n" +
        "    id\n" +
        "  }\n" +
        "}";
    private static final String MUTATION_PUBLISH_UPDATE = "mutation (\n" +
        "  $id: String!\n" +
        "  $audience: UpdateAudience\n" +
        ") {\n" +
        "  publishUpdate(id: $id, notificationAudience: $audience) {\n" +
        "    id\n" +
        "  }\n" +
        "}";

    private final JReleaserLogger logger;
    private final OpenCollectiveAPI api;
    private final boolean dryrun;

    private OpenCollectiveSdk(JReleaserLogger logger,
                              String host,
                              String token,
                              int connectTimeout,
                              int readTimeout,
                              boolean dryrun) {
        requireNonNull(logger, "'logger' must not be null");
        requireNonBlank(host, "'host' must not be blank");
        requireNonBlank(token, "'token' must not be blank");

        ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

        this.logger = logger;
        this.dryrun = dryrun;

        this.api = Feign.builder()
            .encoder(new JacksonEncoder(objectMapper))
            .decoder(new JacksonDecoder(objectMapper))
            .requestInterceptor(template -> {
                template.header("User-Agent", "JReleaser/" + JReleaserVersion.getPlainVersion());
                template.header("Personal-Token", token);
            })
            .errorDecoder((methodKey, response) -> new RestAPIException(response.request(), response.status(), response.reason(), response.headers()))
            .options(new Request.Options(connectTimeout, TimeUnit.SECONDS, readTimeout, TimeUnit.SECONDS, true))
            .target(OpenCollectiveAPI.class, host);

        this.logger.debug(RB.$("workflow.dryrun"), dryrun);
    }

    public void postUpdate(String slug, String title, String body) throws OpenCollectiveException {
        wrap(() -> {
            Mutation mutation = new Mutation();
            mutation.setQuery(MUTATION_CREATE_UPDATE);
            mutation.setVariables(map()
                .e("update", map()
                    .e("title", title)
                    .e("html", body)
                    .e("account", map()
                        .e("slug", slug))));

            Envelope envelope = api.createUpdate(mutation);

            mutation.setQuery(MUTATION_PUBLISH_UPDATE);
            mutation.setVariables(map()
                .e("id", envelope.getData().getCreateUpdate().getId())
                .e("audience", "ALL"));

            api.publishUpdate(mutation);
        });
    }

    private void wrap(OpenCollectiveOperation op) throws OpenCollectiveException {
        try {
            if (!dryrun) op.run();
        } catch (RestAPIException e) {
            logger.trace(e);
            throw new OpenCollectiveException(RB.$("sdk.operation.failed", "openCollective"), e);
        }
    }

    public static Builder builder(JReleaserLogger logger) {
        return new Builder(logger);
    }

    @FunctionalInterface
    public interface OpenCollectiveOperation {
        void run() throws OpenCollectiveException;
    }

    public static class Builder {
        private final JReleaserLogger logger;
        private boolean dryrun;
        private String token;
        private String host;
        private int connectTimeout = 20;
        private int readTimeout = 60;

        private Builder(JReleaserLogger logger) {
            this.logger = requireNonNull(logger, "'logger' must not be null");
        }

        public Builder dryrun(boolean dryrun) {
            this.dryrun = dryrun;
            return this;
        }

        public Builder token(String token) {
            this.token = requireNonBlank(token, "'token' must not be blank").trim();
            return this;
        }

        public Builder host(String host) {
            this.host = requireNonBlank(host, "'host' must not be blank").trim();
            return this;
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        private void validate() {
            requireNonBlank(host, "'host' must not be blank");
            requireNonBlank(token, "'token' must not be blank");
        }

        public OpenCollectiveSdk build() {
            validate();

            return new OpenCollectiveSdk(
                logger,
                host,
                token,
                connectTimeout,
                readTimeout,
                dryrun);
        }
    }
}
