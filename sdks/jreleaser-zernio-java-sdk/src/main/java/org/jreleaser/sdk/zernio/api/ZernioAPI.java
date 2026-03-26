/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.sdk.zernio.api;

import feign.Headers;
import feign.QueryMap;
import feign.RequestLine;

import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.24.0
 */
@org.jreleaser.infra.nativeimage.annotations.ProxyConfig
public interface ZernioAPI {
    @RequestLine("GET /accounts")
    @Headers("Content-Type: application/json")
    Accounts listAccounts(@QueryMap Map<String, String> q);

    @RequestLine("POST /posts")
    @Headers("Content-Type: application/json")
    void post(Post post);
}
