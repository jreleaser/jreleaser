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
package org.jreleaser.sdk.mavencentral.api;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import feign.form.FormData;

import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.12.0
 */
@org.jreleaser.infra.nativeimage.annotations.ProxyConfig
public interface MavenCentralAPI {
    @RequestLine("DELETE /deployment/{deploymentId}")
    @Headers("Content-Type: application/json")
    void delete(@Param("deploymentId") String deploymentId);

    @RequestLine("POST /deployment/{deploymentId}")
    @Headers("Content-Type: application/json")
    void publish(@Param("deploymentId") String deploymentId);

    @RequestLine("POST /upload")
    @Headers("Content-Type: multipart/form-data")
    String upload(@Param("bundle") FormData bundle);

    @RequestLine("POST /status")
    @Headers("Content-Type: application/json")
    Deployment status(@QueryMap Map<String, Object> params);

    @RequestLine("GET /published")
    @Headers("Content-Type: application/json")
    Deployment published(@Param("namespace") String namespace, @Param("name") String name, @Param("version") String version);
}
