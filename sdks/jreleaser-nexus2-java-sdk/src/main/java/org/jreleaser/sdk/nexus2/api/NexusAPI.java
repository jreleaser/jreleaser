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
package org.jreleaser.sdk.nexus2.api;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public interface NexusAPI {
    @RequestLine("GET /staging/profile_repositories/{profileId}")
    @Headers("Content-Type: application/json")
    Data<List<StagingProfileRepository>> getStagingProfileRepositories(@Param("profileId") String profileId);

    @RequestLine("GET /staging/profile_evaluate")
    @Headers("Content-Type: application/json")
    Data<List<StagingProfile>> evalStagingProfile(@QueryMap Map<String, Object> params);

    @RequestLine("GET /staging/repository/{repositoryId}")
    @Headers("Content-Type: application/json")
    StagingProfileRepository getStagingRepository(@Param("repositoryId") String repositoryId);

    @RequestLine("POST /staging/profiles/{profileId}/start")
    @Headers("Content-Type: application/json")
    Data<StagedRepository> startStagingRepository(Data<PromoteRequest> promoteRequest, @Param("profileId") String profileId);

    @RequestLine("POST /staging/profiles/{profileId}/finish")
    @Headers("Content-Type: application/json")
    void closeStagingRepository(Data<PromoteRequest> promoteRequest, @Param("profileId") String profileId);

    @RequestLine("POST /staging/profiles/{profileId}/promote")
    @Headers("Content-Type: application/json")
    void releaseStagingRepository(Data<PromoteRequest> promoteRequest, @Param("profileId") String profileId);

    @RequestLine("POST /staging/profiles/{profileId}/drop")
    @Headers("Content-Type: application/json")
    void dropStagingRepository(Data<PromoteRequest> promoteRequest, @Param("profileId") String profileId);

    @RequestLine("GET /staging/repository/{repositoryId}/activity")
    @Headers("Content-Type: application/json")
    List<StagingActivity> getActivities(@Param("repositoryId") String repositoryId);
}
