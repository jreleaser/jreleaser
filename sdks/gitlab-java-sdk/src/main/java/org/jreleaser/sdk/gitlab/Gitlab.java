/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.gitlab.api.FileUpload;
import org.jreleaser.sdk.gitlab.api.GitlabAPI;
import org.jreleaser.sdk.gitlab.api.Milestone;
import org.jreleaser.sdk.gitlab.api.Project;
import org.jreleaser.sdk.gitlab.api.Release;
import org.jreleaser.sdk.gitlab.api.User;
import org.jreleaser.util.CollectionUtils;
import org.jreleaser.util.JReleaserLogger;
import org.jreleaser.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

    private User user;
    private Project project;

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
            .decoder(new JacksonDecoder(objectMapper))
            .requestInterceptor(template -> template.header("Authorization", String.format("Bearer %s", token)))
            .target(GitlabAPI.class, endpoint);
    }

    Project findProject(String projectName, String identifier) throws RestAPIException {
        return getProject(projectName, identifier);
    }

    Optional<Milestone> findMilestoneByName(String owner, String repo, String identifier, String milestoneName) throws IOException {
        logger.debug("lookup milestone '{}' on {}/{}", milestoneName, owner, repo);

        Project project = getProject(repo, identifier);

        try {
            List<Milestone> milestones = api.findMilestoneByTitle(project.getId(), CollectionUtils.<String, Object>map()
                .e("title", milestoneName));

            if (milestones == null || milestones.isEmpty()) {
                return Optional.empty();
            }

            Milestone milestone = milestones.get(0);
            return "active".equals(milestone.getState()) ? Optional.of(milestone) : Optional.empty();
        } catch (RestAPIException e) {
            if (e.isNotFound() || e.isForbidden()) {
                // ok
                return Optional.empty();
            }
            throw e;
        }
    }

    void closeMilestone(String owner, String repo, String identifier, Milestone milestone) throws IOException {
        logger.debug("closing milestone '{}' on {}/{}", milestone.getTitle(), owner, repo);

        Project project = getProject(repo, identifier);

        api.updateMilestone(CollectionUtils.<String, Object>map()
                .e("state_event", "close"),
            project.getId(), milestone.getId());
    }

    Project createProject(String owner, String repo) throws IOException {
        logger.debug("creating project {}/{}", owner, repo);

        return api.createProject(repo, "public");
    }

    User getCurrentUser() throws RestAPIException {
        if (null == user) {
            logger.debug("fetching current user");
            user = api.getCurrentUser();
        }

        return user;
    }

    Project getProject(String projectName, String identifier) throws RestAPIException {

        if (null == project) {

            if (StringUtils.isNotBlank(identifier)) {
                logger.debug("fetching project with GitLab id {}", identifier);
                project =  api.getProject(identifier.trim());
            } else {
                User u = getCurrentUser();

                logger.debug("fetching project {} for user {} ({})", projectName, u.getUsername(), u.getId());
                List<Project> projects = api.getProject(u.getId(), CollectionUtils.<String, Object>map()
                        .e("search", projectName));

                if (projects == null || projects.isEmpty()) {
                    throw new RestAPIException(404, "Project " + projectName + " does not exist or it's not visible");
                }

                project = projects.get(0);
            }

            logger.debug("found {} (ID: {})", project.getNameWithNamespace(), project.getId());
        }

        return project;
    }

    Release findReleaseByTag(String owner, String repoName, String identifier, String tagName) throws RestAPIException {
        logger.debug("fetching release on {}/{} with tag {}", owner, repoName, tagName);

        Project project = getProject(repoName, identifier);

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

    void deleteTag(String owner, String repoName, String identifier, String tagName) throws RestAPIException {
        logger.debug("deleting tag {} from {}/{}", tagName, owner, repoName);

        Project project = getProject(repoName, identifier);

        api.deleteTag(project.getId(), urlEncode(tagName));
    }

    void deleteRelease(String owner, String repoName, String identifier, String tagName) throws RestAPIException {
        logger.debug("deleting release {} from {}/{}", tagName, owner, repoName);

        Project project = getProject(repoName, identifier);

        api.deleteRelease(project.getId(), urlEncode(tagName));
    }

    void createRelease(String owner, String repoName, String identifier, Release release) throws RestAPIException {
        logger.debug("creating release on {}/{} with tag {}", owner, repoName, release.getTagName());

        Project project = getProject(repoName, identifier);

        api.createRelease(release, project.getId());
    }

    void updateRelease(String owner, String repoName, String identifier, Release release) throws RestAPIException {
        logger.debug("creating release on {}/{} with tag {}", owner, repoName, release.getTagName());

        Project project = getProject(repoName, identifier);

        api.updateRelease(release, project.getId());
    }

    List<FileUpload> uploadAssets(String owner, String repoName, String identifier, List<Path> assets) throws IOException, RestAPIException {
        logger.debug("uploading assets to {}/{}", owner, repoName);

        List<FileUpload> uploads = new ArrayList<>();

        Project project = getProject(repoName, identifier);

        for (Path asset : assets) {
            if (0 == asset.toFile().length() || !Files.exists(asset)) {
                // do not upload empty or non existent files
                continue;
            }

            logger.info(" - uploading {}", asset.getFileName().toString());
            try {
                FileUpload upload = api.uploadFile(project.getId(), toFormData(asset));
                upload.setName(asset.getFileName().toString());
                uploads.add(upload);
            } catch (IOException | RestAPIException e) {
                logger.error(" x Failed to upload {}", asset.getFileName());
                throw e;
            }
        }

        return uploads;
    }

    void linkAssets(String owner, String repoName, Release release, String identifier, List<FileUpload> uploads) throws IOException, RestAPIException {
        logger.debug("linking assets to {}/{} with tag {}", owner, repoName, release.getTagName());

        Project project = getProject(repoName, identifier);

        for (FileUpload upload : uploads) {
            logger.debug(" - linking {}", upload.getName());
            try {
                api.linkAsset(upload.toLinkRequest(apiHost), project.getId(), release.getTagName());
            } catch (RestAPIException e) {
                logger.error(" x failed to link {}", upload.getName());
                throw e;
            }
        }
    }

    private FormData toFormData(Path asset) throws IOException {
        return FormData.builder()
            .fileName(asset.getFileName().toString())
            .contentType(MediaType.parse(tika.detect(asset)).toString())
            .data(Files.readAllBytes(asset))
            .build();
    }
}
