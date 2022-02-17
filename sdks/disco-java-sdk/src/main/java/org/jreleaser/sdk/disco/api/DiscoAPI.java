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
package org.jreleaser.sdk.disco.api;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import org.jreleaser.infra.nativeimage.annotations.ProxyConfig;

import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
@ProxyConfig
public interface DiscoAPI {
    @RequestLine("GET /packages")
    @Headers("Content-Type: application/json")
    Result<List<Pkg>> packages(@QueryMap Map<String, String> q);

    @RequestLine("GET /ids/{id}")
    @Headers("Content-Type: application/json")
    Result<List<EphemeralId>> ids(@Param("id") String id);
}
