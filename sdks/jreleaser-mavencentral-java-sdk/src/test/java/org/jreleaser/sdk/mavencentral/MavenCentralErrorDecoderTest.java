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
package org.jreleaser.sdk.mavencentral;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.logging.SimpleJReleaserLoggerAdapter;
import org.jreleaser.sdk.mavencentral.api.MavenCentralAPIException;
import org.junit.jupiter.api.Test;

import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import feign.RetryableException;

class MavenCentralErrorDecoderTest {
    private final JReleaserLogger logger = new SimpleJReleaserLoggerAdapter();

    private Response buildResponse(int status, String reason) {
        Request request = Request.create(Request.HttpMethod.GET, "http://localhost", Collections.emptyMap(), null,
                new RequestTemplate());
        return Response.builder()
                .status(status)
                .reason(reason)
                .request(request)
                .headers(Collections.emptyMap())
                .build();
    }

    @Test
    void returnsUnauthorizedExceptionFor401() {
        MavenCentral.MavenCentralErrorDecoder decoder = new MavenCentral.MavenCentralErrorDecoder(logger, "testDeployer");
        Response response = buildResponse(401, "Unauthorized");
        Exception ex = decoder.decode("method", response);
        assertThat(ex).isInstanceOf(MavenCentralAPIException.class);
        assertThat(((MavenCentralAPIException) ex).getStatus()).isEqualTo(401);
        assertThat(ex.getMessage()).contains("Unauthorized");
        assertThat(ex.getMessage()).contains("deploy.maven.testDeployer");
    }


    @Test
    void returnsForbiddenExceptionFor403() {
        MavenCentral.MavenCentralErrorDecoder decoder = new MavenCentral.MavenCentralErrorDecoder(logger, "testDeployer");
        Response response = buildResponse(403, "Forbidden");
        Exception ex = decoder.decode("method", response);
        assertThat(ex).isInstanceOf(MavenCentralAPIException.class);
        assertThat(((MavenCentralAPIException) ex).getStatus()).isEqualTo(403);
        assertThat(ex.getMessage()).contains("Forbidden");
    }


    @Test
    void returnsRetryableExceptionFor500() {
        MavenCentral.MavenCentralErrorDecoder decoder = new MavenCentral.MavenCentralErrorDecoder(logger, "testDeployer");
        Response response = buildResponse(500, "Internal Server Error");
        Exception ex = decoder.decode("method", response);
        assertThat(ex).isInstanceOf(RetryableException.class);
    }


    @Test
    void returnsDefaultExceptionForOtherStatus() {
        MavenCentral.MavenCentralErrorDecoder decoder = new MavenCentral.MavenCentralErrorDecoder(logger, "testDeployer");
        Response response = buildResponse(404, "Not Found");
        Exception ex = decoder.decode("method", response);
        assertThat(ex).isInstanceOf(MavenCentralAPIException.class);
        assertThat(((MavenCentralAPIException) ex).getStatus()).isEqualTo(404);
        assertThat(ex.getMessage()).contains("Not Found");
    }
}
