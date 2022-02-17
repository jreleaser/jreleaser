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
package org.jreleaser.sdk.gitlab.api;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import feign.form.FormData;
import org.jreleaser.infra.nativeimage.annotations.ProxyConfig;

import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@ProxyConfig
public interface GitlabAPI {
    @RequestLine("GET /user")
    User getCurrentUser();

    @RequestLine("GET /projects/{projectId}")
    @Headers("Content-Type: application/json")
    Project getProject(@Param("projectId") String projectId);

    @RequestLine("GET /users/{userId}/projects")
    List<Project> getProject(@Param("userId") Integer userId, @QueryMap Map<String, Object> queryMap);

    @RequestLine("POST /projects")
    @Headers("Content-Type: multipart/form-data")
    Project createProject(@Param("name") String projectName, @Param("visibility") String visibility);

    @RequestLine("GET /projects/{projectId}/releases/{tagName}")
    Release getRelease(@Param("projectId") Integer projectId, @Param("tagName") String tagName);

    @RequestLine("DELETE /projects/{projectId}/repository/tags/{tagName}")
    void deleteTag(@Param("projectId") Integer projectId, @Param("tagName") String tagName);

    @RequestLine("DELETE /projects/{projectId}/releases/{tagName}")
    void deleteRelease(@Param("projectId") Integer projectId, @Param("tagName") String tagName);

    @RequestLine("POST /projects/{projectId}/releases")
    @Headers("Content-Type: application/json")
    void createRelease(Release release, @Param("projectId") Integer projectId);

    @RequestLine("PUT /projects/{projectId}/releases/{tagName}")
    @Headers("Content-Type: application/json")
    void updateRelease(Release release, @Param("projectId") Integer projectId);

    @RequestLine("POST /projects/{projectId}/uploads")
    @Headers("Content-Type: multipart/form-data")
    FileUpload uploadFile(@Param("projectId") Integer projectId, @Param("file") FormData file);

    @RequestLine("POST /projects/{projectId}/releases/{tagName}/assets/links")
    @Headers("Content-Type: multipart/form-data")
    Link linkAsset(LinkRequest link, @Param("projectId") Integer projectId, @Param("tagName") String tagName);

    @RequestLine("GET /projects/{projectId}/milestones")
    List<Milestone> findMilestoneByTitle(@Param("projectId") Integer projectId, @QueryMap Map<String, Object> queryMap);

    @RequestLine("PUT /projects/{projectId}/milestones/{milestoneId}")
    @Headers("Content-Type: application/json")
    void updateMilestone(Map<String, Object> params, @Param("projectId") Integer projectId, @Param("milestoneId") Integer milestoneId);

    @RequestLine("GET /search")
    @Headers("Content-Type: application/json")
    List<User> searchUser(@QueryMap Map<String, String> q);
}
