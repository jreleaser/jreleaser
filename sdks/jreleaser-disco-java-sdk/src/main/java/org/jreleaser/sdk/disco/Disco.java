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
package org.jreleaser.sdk.disco;

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
import org.jreleaser.sdk.disco.api.DiscoAPI;
import org.jreleaser.sdk.disco.api.EphemeralId;
import org.jreleaser.sdk.disco.api.Pkg;
import org.jreleaser.sdk.disco.api.Result;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
public class Disco {
    private static final String ENDPOINT = "https://api.foojay.io/disco/v3.0";

    private final JReleaserLogger logger;
    private final DiscoAPI api;

    public Disco(JReleaserLogger logger, int connectTimeout, int readTimeout) {
        ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

        this.logger = logger;
        this.api = Feign.builder()
            .encoder(new JacksonEncoder(objectMapper))
            .decoder(new JacksonDecoder(objectMapper))
            .requestInterceptor(template -> template.header("User-Agent", "JReleaser/" + JReleaserVersion.getPlainVersion()))
            .errorDecoder((methodKey, response) -> new RestAPIException(response.request(), response.status(), response.reason(), response.headers()))
            .options(new Request.Options(connectTimeout, TimeUnit.SECONDS, readTimeout, TimeUnit.SECONDS, true))
            .target(DiscoAPI.class, ENDPOINT);
    }

    public JReleaserLogger getLogger() {
        return logger;
    }

    public List<Pkg> packages(Pkg pkg) throws RestAPIException {
        logger.debug(RB.$("disco.fetch.packages"), pkg.formatAsQuery());

        Result<List<Pkg>> packages = api.packages(pkg.asQuery());

        if (packages.getResult().isEmpty()) {
            if (isNotBlank(packages.getMessage())) {
                logger.warn(packages.getMessage());
            } else {
                logger.warn(RB.$("ERROR_disco_resolve_package", pkg));
            }

            return Collections.emptyList();
        }

        return packages.getResult();
    }

    public List<EphemeralId> pkg(String id) throws RestAPIException {
        logger.debug(RB.$("disco.fetch.package"), id);

        Result<List<EphemeralId>> ephemeralIds = api.ids(id);

        if (ephemeralIds.getResult().isEmpty()) {
            if (isNotBlank(ephemeralIds.getMessage())) {
                logger.warn(ephemeralIds.getMessage());
            } else {
                logger.warn(RB.$("ERROR_disco_resolve_pkg", id));
            }

            return Collections.emptyList();
        }

        return ephemeralIds.getResult();
    }
}
