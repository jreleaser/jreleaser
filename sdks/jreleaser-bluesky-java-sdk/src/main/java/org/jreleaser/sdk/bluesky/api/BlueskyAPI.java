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
package org.jreleaser.sdk.bluesky.api;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.jreleaser.infra.nativeimage.annotations.ProxyConfig;

/**
 * @author Simon Verhoeven
 * @author Tom Cools
 * @since 1.7.0
 */
@ProxyConfig
public interface BlueskyAPI {

    @RequestLine("POST /xrpc/com.atproto.server.createSession")
    @Headers("Content-Type: application/json")
    CreateSessionResponse createSession(CreateSessionRequest request);

    @RequestLine("POST /xrpc/com.atproto.repo.createRecord")
    @Headers({"Content-Type: application/json", "Authorization: Bearer {accessToken}"})
    CreateRecordResponse createRecord(CreateTextRecordRequest request, @Param("accessToken") String accessToken);

    @RequestLine("GET /xrpc/com.atproto.identity.resolveHandle?handle={handle}")
    @Headers("Accept: application/json")
    ResolveHandleResponse resolveHandle(@Param("handle") String handle);
}
