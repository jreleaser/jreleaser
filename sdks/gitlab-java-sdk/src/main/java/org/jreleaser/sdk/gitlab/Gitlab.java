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
package org.jreleaser.sdk.gitlab;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.form.FormData;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.releaser.spi.Asset;
import org.jreleaser.model.releaser.spi.Release;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.gitlab.api.GitlabAPI;
import org.jreleaser.sdk.gitlab.api.GlBranch;
import org.jreleaser.sdk.gitlab.api.GlFileUpload;
import org.jreleaser.sdk.gitlab.api.GlLinkRequest;
import org.jreleaser.sdk.gitlab.api.GlMilestone;
import org.jreleaser.sdk.gitlab.api.GlProject;
import org.jreleaser.sdk.gitlab.api.GlRelease;
import org.jreleaser.sdk.gitlab.api.GlUser;
import org.jreleaser.sdk.gitlab.internal.Page;
import org.jreleaser.sdk.gitlab.internal.PaginatingDecoder;
import org.jreleaser.util.CollectionUtils;
import org.jreleaser.util.JReleaserLogger;
import org.jreleaser.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.sdk.gitlab.internal.UrlEncoder.urlEncode;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
class Gitlab {
    static final String ENDPOINT = "https://gitlab.com/api/v4";
    private static final String API_V4 = "/api/v4";
    private final Tika tika = new Tika();

    private final JReleaserLogger logger;
    private final GitlabAPI api;
    private final String apiHost;

    private GlUser user;
    private GlProject project;

    Gitlab(JReleaserLogger logger,
           String endpoint,
           String token,
           int connectTimeout,
           int readTimeout) throws IOException {
        requireNonNull(logger, "'logger' must not be null");
        requireNonBlank(token, "'token' must not be blank");

        if (isBlank(endpoint)) {
            endpoint = ENDPOINT;
        }

        if (!endpoint.endsWith(API_V4)) {
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
            endpoint += API_V4;
        }

        apiHost = endpoint.substring(0, endpoint.length() - API_V4.length());

        ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

        this.logger = logger;
        this.api = ClientUtils.builder(logger, connectTimeout, readTimeout)
            .encoder(new FormEncoder(new JacksonEncoder(objectMapper)))
            .decoder(new PaginatingDecoder(new JacksonDecoder(objectMapper)))
            .requestInterceptor(template -> template.header("Authorization", String.format("Bearer %s", token)))
            .target(GitlabAPI.class, endpoint);
    }

    GlProject findProject(String projectName, String projectIdentifier) throws RestAPIException {
        return getProject(projectName, projectIdentifier);
    }

    List<Release> listReleases(String owner, String repoName, String projectIdentifier) throws IOException {
        logger.debug(RB.$("git.list.releases"), owner, repoName);

        List<Release> releases = new ArrayList<>();

        if (isBlank(projectIdentifier)) {
            GlProject project = getProject(repoName, projectIdentifier);
            projectIdentifier = project.getId().toString();
        }

        Page<List<GlRelease>> page = api.listReleases0(projectIdentifier);
        page.getContent().stream()
            .map(r -> new Release(
                r.getName(),
                r.getTagName(),
                apiHost + r.getTagPath(),
                r.getReleasedAt()
            ))
            .forEach(releases::add);

        if (page.hasLinks() && page.getLinks().hasNext()) {
            try {
                collectReleases(page, releases);
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }

        return releases;
    }

    private void collectReleases(Page<List<GlRelease>> page, List<Release> releases) throws URISyntaxException {
        URI next = new URI(page.getLinks().next());
        logger.debug(next.toString());

        page = api.listReleases1(next);
        page.getContent().stream()
            .map(r -> new Release(
                r.getName(),
                r.getTagName(),
                apiHost + r.getTagPath(),
                r.getReleasedAt()
            ))
            .forEach(releases::add);

        if (page.hasLinks() && page.getLinks().hasNext()) {
            collectReleases(page, releases);
        }
    }

    List<String> listBranches(String owner, String repoName, String projectIdentifier) throws IOException {
        logger.debug(RB.$("git.list.branches"), owner, repoName);

        List<String> branches = new ArrayList<>();

        if (isBlank(projectIdentifier)) {
            GlProject project = getProject(repoName, projectIdentifier);
            projectIdentifier = project.getId().toString();
        }

        Page<List<GlBranch>> page = api.listBranches0(projectIdentifier);
        page.getContent().stream()
            .map(GlBranch::getName)
            .forEach(branches::add);

        if (page.hasLinks() && page.getLinks().hasNext()) {
            try {
                collectBranches(page, branches);
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }

        return branches;
    }

    private void collectBranches(Page<List<GlBranch>> page, List<String> branches) throws URISyntaxException {
        URI next = new URI(page.getLinks().next());
        logger.debug(next.toString());

        page = api.listBranches1(next);
        page.getContent().stream()
            .map(GlBranch::getName)
            .forEach(branches::add);

        if (page.hasLinks() && page.getLinks().hasNext()) {
            collectBranches(page, branches);
        }
    }

    Optional<GlMilestone> findMilestoneByName(String owner, String repo, String projectIdentifier, String milestoneName) throws IOException {
        logger.debug(RB.$("git.milestone.lookup"), milestoneName, owner, repo);

        GlProject project = getProject(repo, projectIdentifier);

        try {
            List<GlMilestone> milestones = api.findMilestoneByTitle(project.getId(), CollectionUtils.<String, Object>map()
                .e("title", milestoneName));

            if (milestones == null || milestones.isEmpty()) {
                return Optional.empty();
            }

            GlMilestone milestone = milestones.get(0);
            return "active".equals(milestone.getState()) ? Optional.of(milestone) : Optional.empty();
        } catch (RestAPIException e) {
            if (e.isNotFound() || e.isForbidden()) {
                // ok
                return Optional.empty();
            }
            throw e;
        }
    }

    void closeMilestone(String owner, String repo, String projectIdentifier, GlMilestone milestone) throws IOException {
        logger.debug(RB.$("git.milestone.close"), milestone.getTitle(), owner, repo);

        GlProject project = getProject(repo, projectIdentifier);

        api.updateMilestone(CollectionUtils.<String, Object>map()
                .e("state_event", "close"),
            project.getId(), milestone.getId());
    }

    GlProject createProject(String owner, String repo) throws IOException {
        logger.debug(RB.$("git.project.create"), owner, repo);

        return api.createProject(repo, "public");
    }

    GlUser getCurrentUser() throws RestAPIException {
        if (null == user) {
            logger.debug(RB.$("git.fetch.current.user"));
            user = api.getCurrentUser();
        }

        return user;
    }

    GlProject getProject(String projectName, String projectIdentifier) throws RestAPIException {
        if (null == project) {
            if (StringUtils.isNotBlank(projectIdentifier)) {
                logger.debug(RB.$("git.fetch.gitlab.project_by_id"), projectIdentifier);
                project = api.getProject(projectIdentifier.trim());
            } else {
                GlUser u = getCurrentUser();

                logger.debug(RB.$("git.fetch.gitlab.project.by.user"), projectName, u.getUsername(), u.getId());
                List<GlProject> projects = api.getProject(u.getId(), CollectionUtils.<String, Object>map()
                    .e("search", projectName));

                if (projects == null || projects.isEmpty()) {
                    throw new RestAPIException(404, RB.$("ERROR_project_not_exist", projectName));
                }

                project = projects.get(0);
            }

            logger.debug(RB.$("git.gitlab.project.found"), project.getNameWithNamespace(), project.getId());
        }

        return project;
    }

    GlRelease findReleaseByTag(String owner, String repoName, String projectIdentifier, String tagName) throws RestAPIException {
        logger.debug(RB.$("git.fetch.release.by.tag"), owner, repoName, tagName);

        GlProject project = getProject(repoName, projectIdentifier);

        try {
            return api.getRelease(project.getId(), urlEncode(tagName));
        } catch (RestAPIException e) {
            if (e.isNotFound() || e.isForbidden()) {
                // ok
                return null;
            }
            throw e;
        }
    }

    void deleteTag(String owner, String repoName, String projectIdentifier, String tagName) throws RestAPIException {
        logger.debug(RB.$("git.delete.tag.from"), tagName, owner, repoName);

        GlProject project = getProject(repoName, projectIdentifier);

        api.deleteTag(project.getId(), urlEncode(tagName));
    }

    void deleteRelease(String owner, String repoName, String projectIdentifier, String tagName) throws RestAPIException {
        logger.debug(RB.$("git.delete.release.from"), tagName, owner, repoName);

        GlProject project = getProject(repoName, projectIdentifier);

        api.deleteRelease(project.getId(), urlEncode(tagName));
    }

    void createRelease(String owner, String repoName, String projectIdentifier, GlRelease release) throws RestAPIException {
        logger.debug(RB.$("git.create.release"), owner, repoName, release.getTagName());

        GlProject project = getProject(repoName, projectIdentifier);

        api.createRelease(release, project.getId());
    }

    void updateRelease(String owner, String repoName, String projectIdentifier, GlRelease release) throws RestAPIException {
        logger.debug(RB.$("git.update.release"), owner, repoName, release.getTagName());

        GlProject project = getProject(repoName, projectIdentifier);

        api.updateRelease(release, project.getId());
    }

    Collection<GlFileUpload> uploadAssets(String owner, String repoName, String projectIdentifier, List<Asset> assets) throws IOException, RestAPIException {
        logger.debug(RB.$("git.upload.assets"), owner, repoName);

        List<GlFileUpload> uploads = new ArrayList<>();

        GlProject project = getProject(repoName, projectIdentifier);

        for (Asset asset : assets) {
            if (0 == Files.size(asset.getPath()) || !Files.exists(asset.getPath())) {
                // do not upload empty or non existent files
                continue;
            }

            logger.info(" " + RB.$("git.upload.asset"), asset.getFilename());
            try {
                GlFileUpload upload = api.uploadFile(project.getId(), toFormData(asset.getPath()));
                upload.setName(asset.getFilename());
                uploads.add(upload);
            } catch (IOException | RestAPIException e) {
                logger.error(" " + RB.$("git.upload.asset.failure"), asset.getFilename());
                throw e;
            }
        }

        return uploads;
    }

    void linkReleaseAssets(String owner, String repoName, GlRelease release, String projectIdentifier, Collection<GlFileUpload> uploads) throws IOException, RestAPIException {
        logger.debug(RB.$("git.upload.asset.links"), owner, repoName, release.getTagName());

        GlProject project = getProject(repoName, projectIdentifier);

        for (GlFileUpload upload : uploads) {
            logger.debug(" " + RB.$("git.upload.asset.link"), upload.getName());
            try {
                api.linkAsset(upload.toLinkRequest(apiHost), project.getId(), release.getTagName());
            } catch (RestAPIException e) {
                logger.error(" " + RB.$("git.upload.asset.link.failure"), upload.getName());
                throw e;
            }
        }
    }

    void linkAssets(String owner, String repoName, GlRelease release, String projectIdentifier, Collection<GlLinkRequest> links) throws IOException, RestAPIException {
        logger.debug(RB.$("git.upload.asset.links"), owner, repoName, release.getTagName());

        GlProject project = getProject(repoName, projectIdentifier);

        for (GlLinkRequest link : links) {
            logger.info(" " + RB.$("git.upload.asset.link"), link.getName());
            try {
                api.linkAsset(link, project.getId(), release.getTagName());
            } catch (RestAPIException e) {
                logger.error(" " + RB.$("git.upload.asset.link.failure"), link.getName());
                throw e;
            }
        }
    }

    Optional<org.jreleaser.model.releaser.spi.User> findUser(String email, String name) throws RestAPIException {
        logger.debug(RB.$("git.user.lookup"), name, email);

        List<GlUser> users = api.searchUser(CollectionUtils.<String, String>mapOf("scope", "users", "search", email));
        if (users != null && !users.isEmpty()) {
            GlUser user = users.get(0);
            return Optional.of(new org.jreleaser.model.releaser.spi.User(user.getUsername(), email, user.getWebUrl()));
        }

        users = api.searchUser(CollectionUtils.<String, String>mapOf("scope", "users", "search", name));
        if (users != null && !users.isEmpty()) {
            GlUser user = users.get(0);
            if (name.equals(user.getName())) {
                return Optional.of(new org.jreleaser.model.releaser.spi.User(user.getUsername(), email, user.getWebUrl()));
            }
        }

        return Optional.empty();
    }

    private FormData toFormData(Path asset) throws IOException {
        return FormData.builder()
            .fileName(asset.getFileName().toString())
            .contentType(MediaType.parse(tika.detect(asset)).toString())
            .data(Files.readAllBytes(asset))
            .build();
    }
}
