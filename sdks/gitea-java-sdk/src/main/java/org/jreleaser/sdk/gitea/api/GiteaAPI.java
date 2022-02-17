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
package org.jreleaser.sdk.gitea.api;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import feign.form.FormData;
import org.jreleaser.infra.nativeimage.annotations.ProxyConfig;

import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@ProxyConfig
public interface GiteaAPI {
    @RequestLine("GET /repos/{owner}/{repo}")
    GtRepository getRepository(@Param("owner") String owner, @Param("repo") String repo);

    @RequestLine("GET /orgs/{org}")
    GtOrganization getOrganization(@Param("org") String org);

    @RequestLine("POST /orgs/{org}/repos")
    @Headers("Content-Type: application/json")
    GtRepository createRepository(Map<String, Object> data, @Param("org") String org);

    @RequestLine("POST /user/repos")
    @Headers("Content-Type: application/json")
    GtRepository createRepository(Map<String, Object> data);

    @RequestLine("GET /repos/{owner}/{repo}/releases/tags/{tag}")
    GtRelease getReleaseByTagName(@Param("owner") String owner, @Param("repo") String repo, @Param("tag") String tag);

    @RequestLine("DELETE /repos/{owner}/{repo}/releases/{id}")
    void deleteRelease(@Param("owner") String owner, @Param("repo") String repo, @Param("id") Integer id);

    @RequestLine("DELETE /repos/{owner}/{repo}/releases/tags/{tag}")
    void deleteTag(@Param("owner") String owner, @Param("repo") String repo, @Param("tag") String tag);

    @RequestLine("POST /repos/{owner}/{repo}/releases")
    @Headers("Content-Type: application/json")
    GtRelease createRelease(GtRelease release, @Param("owner") String owner, @Param("repo") String repo);

    @RequestLine("PATCH /repos/{owner}/{repo}/releases/{id}")
    @Headers("Content-Type: application/json")
    void updateRelease(GtRelease release, @Param("owner") String owner, @Param("repo") String repo, @Param("id") Integer id);

    @RequestLine("POST /repos/{owner}/{repo}/releases/{id}/assets")
    @Headers("Content-Type: multipart/form-data")
    GtAttachment uploadAsset(@Param("owner") String owner, @Param("repo") String repo, @Param("id") Integer id, @Param("attachment") FormData file);

    @RequestLine("GET /repos/{owner}/{repo}/milestones/{milestoneName}")
    GtMilestone findMilestoneByTitle(@Param("owner") String owner, @Param("repo") String repo, @Param("milestoneName") String milestoneName);

    @RequestLine("PATCH /repos/{owner}/{repo}/milestones/{id}")
    @Headers("Content-Type: application/json")
    void updateMilestone(Map<String, Object> params, @Param("owner") String owner, @Param("repo") String repo, @Param("id") Integer id);

    @RequestLine("GET /users/search")
    @Headers("Content-Type: application/json")
    GtSearchUser searchUser(@QueryMap Map<String, String> q);
}
