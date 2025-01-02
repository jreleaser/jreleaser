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
package org.jreleaser.sdk.github.api;

import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import org.jreleaser.infra.nativeimage.annotations.ProxyConfig;
import org.jreleaser.sdk.github.internal.Page;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
@ProxyConfig
public interface GithubAPI {
    @RequestLine("GET /repos/{owner}/{repo}")
    GhRepository getRepository(@Param("owner") String owner, @Param("repo") String repo);

    @RequestLine("GET /orgs/{org}")
    GhOrganization getOrganization(@Param("org") String org);

    @RequestLine("POST /orgs/{org}/repos")
    @Headers("Content-Type: application/json")
    GhRepository createRepository(Map<String, Object> data, @Param("org") String org);

    @RequestLine("POST /user/repos")
    @Headers("Content-Type: application/json")
    GhRepository createRepository(Map<String, Object> data);

    @RequestLine("GET /repos/{owner}/{repo}/releases/tags/{tag}")
    GhRelease getReleaseByTagName(@Param("owner") String owner, @Param("repo") String repo, @Param("tag") String tag);

    @RequestLine("GET /repos/{owner}/{repo}/releases/{id}")
    GhRelease getRelease(@Param("owner") String owner, @Param("repo") String repo, @Param("id") Long id);

    @RequestLine("DELETE /repos/{owner}/{repo}/releases/{id}")
    void deleteRelease(@Param("owner") String owner, @Param("repo") String repo, @Param("id") Long id);

    @RequestLine("DELETE /repos/{owner}/{repo}/git/refs/tags/{tag}")
    void deleteTag(@Param("owner") String owner, @Param("repo") String repo, @Param("tag") String tag);

    @RequestLine("POST /repos/{owner}/{repo}/releases")
    @Headers("Content-Type: application/json")
    GhRelease createRelease(GhRelease release, @Param("owner") String owner, @Param("repo") String repo);

    @RequestLine("PATCH /repos/{owner}/{repo}/releases/{id}")
    @Headers("Content-Type: application/json")
    void updateRelease(GhRelease release, @Param("owner") String owner, @Param("repo") String repo, @Param("id") Long id);

    @RequestLine("GET /repos/{owner}/{repo}/milestones")
    @Headers("Content-Type: application/json")
    Page<List<GhMilestone>> listMilestones(@Param("owner") String owner, @Param("repo") String repo, @QueryMap Map<String, Object> q);

    @RequestLine("PATCH /repos/{owner}/{repo}/milestones/{number}")
    @Headers("Content-Type: application/json")
    void updateMilestone(Map<String, Object> params, @Param("owner") String owner, @Param("repo") String repo, @Param("number") Integer number);

    @RequestLine("GET /search/users")
    @Headers("Content-Type: application/json")
    GhSearchUser searchUser(@QueryMap Map<String, String> q);

    @RequestLine("GET /repos/{owner}/{repo}/releases")
    @Headers("Content-Type: application/json")
    Page<List<GhRelease>> listReleases(@Param("owner") String owner, @Param("repo") String repo, @QueryMap Map<String, Object> q);

    @RequestLine("GET /repos/{owner}/{repo}/branches")
    @Headers("Content-Type: application/json")
    Page<List<GhBranch>> listBranches(@Param("owner") String owner, @Param("repo") String repo, @QueryMap Map<String, Object> q);

    @RequestLine("GET /repos/{owner}/{repo}/releases/{releaseId}/assets")
    @Headers("Content-Type: application/json")
    List<GhAsset> listAssets(@Param("owner") String owner, @Param("repo") String repo, @Param("releaseId") Long releaseId);

    @RequestLine("DELETE /repos/{owner}/{repo}/releases/assets/{assetId}")
    @Headers("Content-Type: application/json")
    void deleteAsset(@Param("owner") String owner, @Param("repo") String repo, @Param("assetId") Long assetId);

    @RequestLine("GET /repos/{owner}/{repo}/labels")
    @Headers("Content-Type: application/json")
    Page<List<GhLabel>> listLabels(@Param("owner") String owner, @Param("repo") String repo, @QueryMap Map<String, Object> q);

    @RequestLine("POST /repos/{owner}/{repo}/labels")
    @Headers("Content-Type: application/json")
    GhLabel createLabel(@Param("owner") String owner, @Param("repo") String repo, @Param("name") String name, @Param("color") String color, @Param("description") String description);

    @RequestLine("GET /repos/{owner}/{repo}/issues/{issueNumber}")
    @Headers("Content-Type: application/json")
    GhIssue findIssue(@Param("owner") String owner, @Param("repo") String repo, @Param("issueNumber") int issueNumber);

    @RequestLine("POST /repos/{owner}/{repo}/issues/{issueNumber}/labels")
    @Headers("Content-Type: application/json")
    void labelIssue(Map<String, List<String>> labels, @Param("owner") String owner, @Param("repo") String repo, @Param("issueNumber") Long issueNumber);

    @RequestLine("POST /repos/{owner}/{repo}/issues/{issueNumber}/comments")
    @Headers("Content-Type: application/json")
    void commentIssue(Map<String, String> params, @Param("owner") String owner, @Param("repo") String repo, @Param("issueNumber") Long issueNumber);

    @RequestLine("PATCH /repos/{owner}/{repo}/issues/{issueNumber}")
    @Headers("Content-Type: application/json")
    void updateIssue(Map<String, Object> params, @Param("owner") String owner, @Param("repo") String repo, @Param("issueNumber") Long issueNumber);

    @RequestLine("GET /orgs/{org}/teams/{team}/discussions")
    @Headers("Content-Type: application/json")
    Page<List<GhDiscussion>> listDiscussions(@Param("org") String org, @Param("team") String team, @QueryMap Map<String, Object> q);

    @RequestLine("POST /orgs/{org}/teams/{team}/discussions")
    @Headers("Content-Type: application/json")
    void createDiscussion(GhDiscussion discussion, @Param("org") String org, @Param("team") String team);

    @RequestLine("GET /repos/{owner}/{repo}/tags")
    @Headers("Content-Type: application/json")
    Page<List<GhTag>> listTags(@Param("owner") String owner, @Param("repo") String repo, @QueryMap Map<String, Object> q);

    @RequestLine("GET /users/{username}")
    @Headers("Content-Type: application/json")
    GhUser getUser(@Param("username") String username);

    @RequestLine("POST /repos/{owner}/{repo}/releases/generate-notes")
    @Headers("Content-Type: application/json")
    GhReleaseNotes generateReleaseNotes(GhReleaseNotesParams params, @Param("owner") String owner, @Param("repo") String repo);

    @RequestLine("DELETE /user/packages/{packageType}/{packageName}/versions/{versionId}")
    @Headers("Content-Type: application/json")
    void deletePackageVersion(@Param("packageType") String packageType, @Param("packageName") String packageName, @Param("versionId") String versionId);

    @RequestLine("DELETE /user/packages/{packageType}/{packageName}")
    @Headers("Content-Type: application/json")
    void deletePackage(@Param("packageType") String packageType, @Param("packageName") String packageName);

    @RequestLine("GET /user/packages/{packageType}/{packageName}/versions")
    @Headers("Content-Type: application/json")
    Page<List<GhPackageVersion>> listPackageVersions0(@Param("packageType") String packageType, @Param("packageName") String packageName);

    @RequestLine("GET")
    @Headers("Content-Type: application/json")
    Page<List<GhPackageVersion>> listPackageVersions1(URI uri);
}
