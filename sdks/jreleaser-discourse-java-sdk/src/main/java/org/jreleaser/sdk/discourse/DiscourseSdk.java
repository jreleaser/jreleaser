/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.discourse.api.Category;
import org.jreleaser.sdk.discourse.api.CategoryList;
import org.jreleaser.sdk.discourse.api.DiscourseAPI;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author shblue21
 * @since 1.3.0
 */
public class DiscourseSdk {
    private final JReleaserContext context;
    private final DiscourseAPI api;
    private final boolean dryrun;

    private DiscourseSdk(JReleaserContext context,
                         String host,
                         String userName,
                         String apiKey,
                         int connectTimeout,
                         int readTimeout,
                         boolean dryrun) {
        requireNonNull(context, "'context' must not be null");
        requireNonBlank(host, "'host' must not be blank");
        requireNonBlank(userName, "'userName' must not be blank");
        requireNonBlank(apiKey, "'apiKey' must not be blank");

        ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

        this.context = context;
        this.dryrun = dryrun;

        this.api = ClientUtils.builder(context, connectTimeout, readTimeout)
            .encoder(new JacksonEncoder(objectMapper))
            .decoder(new JacksonDecoder(objectMapper))
            .requestInterceptor(template -> {
                template.header("User-Agent", "JReleaser/" + JReleaserVersion.getPlainVersion());
                template.header("Api-Key", apiKey);
                template.header("Api-Username", userName);
            })
            .target(DiscourseAPI.class, host);

        this.context.getLogger().debug(RB.$("workflow.dryrun"), dryrun);
    }

    public void createPost(String title, String body, String categoryName) throws DiscourseException {
        wrap(() -> {
            Category category = findCategoryByName(categoryName);
            Map<String, String> params = new LinkedHashMap<>();
            params.put("title", title);
            params.put("raw", body);
            params.put("category", String.valueOf(category.getId()));
            api.createPost(params);
        });
    }

    public Category findCategoryByName(String categoryName) throws DiscourseException {
        CategoryList categoryList = api.getCategories().getCategoryList();

        return categoryList.getCategories().stream()
            .filter(category -> category.getName().equals(categoryName))
            .findFirst()
            .orElseThrow(() -> new DiscourseException(RB.$("sdk.operation.failed", categoryName)));
    }

    private void wrap(DiscourseOperation op) throws DiscourseException {
        try {
            if (!dryrun) op.run();
        } catch (RestAPIException e) {
            context.getLogger().trace(e);
            throw new DiscourseException(RB.$("sdk.operation.failed", "discourse"), e);
        }
    }

    public static Builder builder(JReleaserContext context) {
        return new Builder(context);
    }

    @FunctionalInterface
    public interface DiscourseOperation {
        void run() throws DiscourseException;
    }

    public static class Builder {
        private final JReleaserContext context;
        private boolean dryrun;
        private String userName;
        private String apiKey;
        private String host;
        private int connectTimeout = 20;
        private int readTimeout = 60;

        private Builder(JReleaserContext context) {
            this.context = requireNonNull(context, "'context' must not be null");
        }

        public Builder dryrun(boolean dryrun) {
            this.dryrun = dryrun;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = requireNonBlank(userName, "'userName' must not be blank").trim();
            return this;
        }

        public Builder apiKey(String accessToken) {
            this.apiKey = requireNonBlank(accessToken, "'apiKey' must not be blank").trim();
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
            requireNonBlank(userName, "'userName' must not be blank");
            requireNonBlank(apiKey, "'apiKey' must not be blank");
        }

        public DiscourseSdk build() {
            validate();

            return new DiscourseSdk(
                context,
                host,
                userName,
                apiKey,
                connectTimeout,
                readTimeout,
                dryrun);
        }
    }
}
