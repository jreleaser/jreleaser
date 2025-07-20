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
package org.jreleaser.sdk.forgejo.api;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import feign.form.FormData;
import org.jreleaser.sdk.forgejo.internal.Page;

import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.18.0
 */
@org.jreleaser.infra.nativeimage.annotations.ProxyConfig
public interface ForgejoAPI {
    @RequestLine("GET /repos/{owner}/{repo}")
    Repository getRepository(@Param("owner") String owner, @Param("repo") String repo);

    @RequestLine("GET /orgs/{org}")
    Organization getOrganization(@Param("org") String org);

    @RequestLine("POST /orgs/{org}/repos")
    @Headers("Content-Type: application/json")
    Repository createRepository(Map<String, Object> data, @Param("org") String org);

    @RequestLine("POST /user/repos")
    @Headers("Content-Type: application/json")
    Repository createRepository(Map<String, Object> data);

    @RequestLine("GET /repos/{owner}/{repo}/releases/tags/{tag}")
    Release getReleaseByTagName(@Param("owner") String owner, @Param("repo") String repo, @Param("tag") String tag);

    @RequestLine("DELETE /repos/{owner}/{repo}/releases/{id}")
    void deleteRelease(@Param("owner") String owner, @Param("repo") String repo, @Param("id") Integer id);

    @RequestLine("DELETE /repos/{owner}/{repo}/releases/tags/{tag}")
    void deleteTag(@Param("owner") String owner, @Param("repo") String repo, @Param("tag") String tag);

    @RequestLine("POST /repos/{owner}/{repo}/releases")
    @Headers("Content-Type: application/json")
    Release createRelease(Release release, @Param("owner") String owner, @Param("repo") String repo);

    @RequestLine("PATCH /repos/{owner}/{repo}/releases/{id}")
    @Headers("Content-Type: application/json")
    void updateRelease(Release release, @Param("owner") String owner, @Param("repo") String repo, @Param("id") Integer id);

    @RequestLine("POST /repos/{owner}/{repo}/releases/{id}/assets")
    @Headers("Content-Type: multipart/form-data")
    Attachment uploadAsset(@Param("owner") String owner, @Param("repo") String repo, @Param("id") Integer id, @Param("attachment") FormData file);

    @RequestLine("GET /repos/{owner}/{repo}/milestones/{milestoneName}")
    Milestone findMilestoneByTitle(@Param("owner") String owner, @Param("repo") String repo, @Param("milestoneName") String milestoneName);

    @RequestLine("PATCH /repos/{owner}/{repo}/milestones/{id}")
    @Headers("Content-Type: application/json")
    void updateMilestone(Map<String, Object> params, @Param("owner") String owner, @Param("repo") String repo, @Param("id") Integer id);

    @RequestLine("GET /users/search")
    @Headers("Content-Type: application/json")
    SearchUser searchUser(@QueryMap Map<String, String> q);

    @RequestLine("GET /repos/{owner}/{repo}/releases")
    @Headers("Content-Type: application/json")
    Page<List<Release>> listReleases(@Param("owner") String owner, @Param("repo") String repo, @QueryMap Map<String, Object> q);

    @RequestLine("GET /repos/{owner}/{repo}/branches")
    @Headers("Content-Type: application/json")
    Page<List<Branch>> listBranches(@Param("owner") String owner, @Param("repo") String repo, @QueryMap Map<String, Object> q);

    @RequestLine("GET /repos/{owner}/{repo}/releases/{releaseId}/assets")
    @Headers("Content-Type: application/json")
    List<Asset> listAssets(@Param("owner") String owner, @Param("repo") String repo, @Param("releaseId") Integer releaseId);

    @RequestLine("DELETE /repos/{owner}/{repo}/releases/{releaseId}/assets/{assetId}")
    @Headers("Content-Type: application/json")
    void deleteAsset(@Param("owner") String owner, @Param("repo") String repo, @Param("releaseId") Integer releaseId, @Param("assetId") Integer assetId);

    @RequestLine("GET /repos/{owner}/{repo}/labels")
    @Headers("Content-Type: application/json")
    Page<List<Label>> listLabels(@Param("owner") String owner, @Param("repo") String repo, @QueryMap Map<String, Object> q);

    @RequestLine("POST /repos/{owner}/{repo}/labels")
    @Headers("Content-Type: application/json")
    Label createLabel(@Param("owner") String owner, @Param("repo") String repo, @Param("name") String name, @Param("color") String color, @Param("description") String description);

    @RequestLine("GET /repos/{owner}/{repo}/issues/{issueNumber}")
    @Headers("Content-Type: application/json")
    Issue findIssue(@Param("owner") String owner, @Param("repo") String repo, @Param("issueNumber") int issueNumber);

    @RequestLine("POST /repos/{owner}/{repo}/issues/{issueNumber}/labels")
    @Headers("Content-Type: application/json")
    void labelIssue(Map<String, List<Integer>> labels, @Param("owner") String owner, @Param("repo") String repo, @Param("issueNumber") Integer issueNumber);

    @RequestLine("POST /repos/{owner}/{repo}/issues/{issueNumber}/comments")
    @Headers("Content-Type: application/json")
    void commentIssue(Map<String, String> params, @Param("owner") String owner, @Param("repo") String repo, @Param("issueNumber") Integer issueNumber);

    @RequestLine("PATCH /repos/{owner}/{repo}/issues/{index}")
    @Headers("Content-Type: application/json")
    void updateIssue(Map<String, Object> params, @Param("owner") String owner, @Param("repo") String repo, @Param("index") Integer index);

    @RequestLine("DELETE /packages/{owner}/{type}/{name}/{version}")
    void deletePackage(@Param("owner") String owner, @Param("type") String type, @Param("name") String name, @Param("version") String version);
}
