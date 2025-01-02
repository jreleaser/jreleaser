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
import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.model.spi.release.Asset;
import org.jreleaser.model.spi.release.Release;
import org.jreleaser.model.spi.release.User;
import org.jreleaser.model.spi.upload.UploadException;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.gitlab.api.GitlabAPI;
import org.jreleaser.sdk.gitlab.api.GlBranch;
import org.jreleaser.sdk.gitlab.api.GlFileUpload;
import org.jreleaser.sdk.gitlab.api.GlIssue;
import org.jreleaser.sdk.gitlab.api.GlLabel;
import org.jreleaser.sdk.gitlab.api.GlLink;
import org.jreleaser.sdk.gitlab.api.GlLinkRequest;
import org.jreleaser.sdk.gitlab.api.GlMilestone;
import org.jreleaser.sdk.gitlab.api.GlPackage;
import org.jreleaser.sdk.gitlab.api.GlProject;
import org.jreleaser.sdk.gitlab.api.GlRelease;
import org.jreleaser.sdk.gitlab.api.GlUser;
import org.jreleaser.sdk.gitlab.internal.Page;
import org.jreleaser.sdk.gitlab.internal.PaginatingDecoder;
import org.jreleaser.util.CollectionUtils;
import org.jreleaser.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.mustache.Templates.resolveTemplate;
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
    private static final String GRAPQL_DELETE_PAYLOAD = "{\n" +
        "  \"query\": \"mutation {uploadDelete(input: { secret: \\\"{{secret}}\\\", filename: \\\"{{filename}}\\\", projectPath: \\\"{{projectPath}}\\\"}) { upload { id size path } errors }}\",\n" +
        "  \"variables\": null\n" +
        "}\n";
    private static final Pattern UPLOADS_PATTERN = Pattern.compile("(.*?)/uploads/(.*?)");

    private final Tika tika = new Tika();
    private final JReleaserContext context;
    private final GitlabAPI api;
    private final String apiHost;
    private final String graphQlEndpoint;
    private final int connectTimeout;
    private final int readTimeout;

    private GlUser user;
    private GlProject project;

    Gitlab(JReleaserContext context,
           String endpoint,
           String token,
           int connectTimeout,
           int readTimeout) {
        requireNonNull(context, "'context' must not be null");
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

        this.apiHost = endpoint.substring(0, endpoint.length() - API_V4.length());
        this.graphQlEndpoint = endpoint.replace("v4", "graphql");
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;

        ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

        this.context = context;
        this.api = ClientUtils.builder(context, connectTimeout, readTimeout)
            .encoder(new FormEncoder(new JacksonEncoder(objectMapper)))
            .decoder(new PaginatingDecoder(new JacksonDecoder(objectMapper)))
            .requestInterceptor(template -> template.header("Authorization", String.format("Bearer %s", token)))
            .target(GitlabAPI.class, endpoint);
    }

    GlProject findProject(String projectName, String projectIdentifier) throws RestAPIException {
        return getProject(projectName, projectIdentifier);
    }

    List<Release> listReleases(String owner, String repoName, String projectIdentifier) throws IOException {
        context.getLogger().debug(RB.$("git.list.releases"), owner, repoName);

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
        context.getLogger().debug(next.toString());

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
        context.getLogger().debug(RB.$("git.list.branches"), owner, repoName);

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
        context.getLogger().debug(next.toString());

        page = api.listBranches1(next);
        page.getContent().stream()
            .map(GlBranch::getName)
            .forEach(branches::add);

        if (page.hasLinks() && page.getLinks().hasNext()) {
            collectBranches(page, branches);
        }
    }

    Optional<GlMilestone> findMilestoneByName(String owner, String repo, Integer projectIdentifier, String milestoneName) {
        context.getLogger().debug(RB.$("git.milestone.lookup"), milestoneName, owner, repo);

        return findMilestone(projectIdentifier, milestoneName, "active");
    }

    Optional<GlMilestone> findClosedMilestoneByName(String owner, String repo, Integer projectIdentifier, String milestoneName) {
        context.getLogger().debug(RB.$("git.milestone.lookup.closed"), milestoneName, owner, repo);

        return findMilestone(projectIdentifier, milestoneName, "closed");
    }

    private Optional<GlMilestone> findMilestone(Integer projectIdentifier, String milestoneName, String state) {
        try {
            List<GlMilestone> milestones = api.findMilestoneByTitle(projectIdentifier, CollectionUtils.<String, Object>map()
                .e("title", milestoneName));

            if (null == milestones || milestones.isEmpty()) {
                return Optional.empty();
            }

            GlMilestone milestone = milestones.get(0);
            return state.equals(milestone.getState()) ? Optional.of(milestone) : Optional.empty();
        } catch (RestAPIException e) {
            if (e.isNotFound() || e.isForbidden()) {
                // ok
                return Optional.empty();
            }
            throw e;
        }
    }

    void closeMilestone(String owner, String repo, Integer projectIdentifier, GlMilestone milestone) {
        context.getLogger().debug(RB.$("git.milestone.close"), milestone.getTitle(), owner, repo);

        api.updateMilestone(CollectionUtils.<String, Object>map()
                .e("state_event", "close"),
            projectIdentifier, milestone.getId());
    }

    GlProject createProject(String owner, String repo) {
        context.getLogger().debug(RB.$("git.project.create"), owner, repo);

        return api.createProject(repo, "public");
    }

    GlUser getCurrentUser() throws RestAPIException {
        if (null == user) {
            context.getLogger().debug(RB.$("git.fetch.current.user"));
            user = api.getCurrentUser();
        }

        return user;
    }

    GlProject getProject(String projectName, String projectIdentifier) throws RestAPIException {
        if (null == project) {
            if (StringUtils.isNotBlank(projectIdentifier)) {
                context.getLogger().debug(RB.$("git.fetch.gitlab.project_by_id"), projectIdentifier);
                project = api.getProject(projectIdentifier.trim());
            } else {
                GlUser u = getCurrentUser();

                context.getLogger().debug(RB.$("git.fetch.gitlab.project.by.user"), projectName, u.getUsername(), u.getId());
                List<GlProject> projects = api.getProject(u.getId(), CollectionUtils.<String, Object>map()
                    .e("search", projectName));

                if (null == projects || projects.isEmpty()) {
                    throw new RestAPIException(404, RB.$("ERROR_project_not_exist", projectName));
                }

                project = projects.get(0);
            }

            context.getLogger().debug(RB.$("git.gitlab.project.found"), project.getNameWithNamespace(), project.getId());
        }

        return project;
    }

    GlRelease findReleaseByTag(String owner, String repoName, String projectIdentifier, String tagName) throws RestAPIException {
        context.getLogger().debug(RB.$("git.fetch.release.by.tag"), owner, repoName, tagName);

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

    void deleteTag(String owner, String repoName, Integer projectIdentifier, String tagName) throws RestAPIException {
        context.getLogger().debug(RB.$("git.delete.tag.from"), tagName, owner, repoName);

        api.deleteTag(projectIdentifier, urlEncode(tagName));
    }

    void deletePackage(Integer projectIdentifier, Integer packageId) throws RestAPIException {
        context.getLogger().debug(RB.$("gitlab.delete.package"), packageId, projectIdentifier);

        api.deletePackage(projectIdentifier, packageId);
    }

    void deleteRelease(String owner, String repoName, String projectIdentifier, String tagName) throws RestAPIException {
        context.getLogger().debug(RB.$("git.delete.release.from"), tagName, owner, repoName);

        GlProject project = getProject(repoName, projectIdentifier);

        api.deleteRelease(project.getId(), urlEncode(tagName));
    }

    void createRelease(String owner, String repoName, Integer projectIdentifier, GlRelease release) throws RestAPIException {
        context.getLogger().debug(RB.$("git.create.release"), owner, repoName, release.getTagName());

        api.createRelease(release, projectIdentifier);
    }

    void updateRelease(String owner, String repoName, String projectIdentifier, GlRelease release) throws RestAPIException {
        context.getLogger().debug(RB.$("git.update.release"), owner, repoName, release.getTagName());

        GlProject project = getProject(repoName, projectIdentifier);

        api.updateRelease(release, project.getId());
    }

    Collection<GlFileUpload> uploadAssets(String owner, String repoName, Integer projectIdentifier, Set<Asset> assets) throws IOException, RestAPIException {
        context.getLogger().debug(RB.$("git.upload.assets"), owner, repoName);

        List<GlFileUpload> uploads = new ArrayList<>();

        for (Asset asset : assets) {
            if (0 == Files.size(asset.getPath()) || !Files.exists(asset.getPath())) {
                // do not upload empty or non existent files
                continue;
            }

            context.getLogger().info(" " + RB.$("git.upload.asset"), asset.getFilename());
            try {
                GlFileUpload upload = api.uploadFile(projectIdentifier, toFormData(asset.getPath()));
                upload.setName(asset.getFilename());
                uploads.add(upload);
            } catch (IOException | RestAPIException e) {
                context.getLogger().error(" " + RB.$("git.upload.asset.failure"), asset.getFilename());
                throw e;
            }
        }

        return uploads;
    }

    void linkReleaseAssets(String owner, String repoName, GlRelease release, Integer projectIdentifier, Collection<GlFileUpload> uploads) throws RestAPIException {
        context.getLogger().info(RB.$("git.upload.asset.links"), owner, repoName, release.getTagName());

        for (GlFileUpload upload : uploads) {
            context.getLogger().info(" " + RB.$("git.upload.asset.link"), upload.getName());
            try {
                api.linkAsset(upload.toLinkRequest(apiHost), projectIdentifier, release.getTagName());
            } catch (RestAPIException e) {
                context.getLogger().error(" " + RB.$("git.upload.asset.link.failure"), upload.getName());
                throw e;
            }
        }
    }

    void linkAssets(String owner, String repoName, GlRelease release, Integer projectIdentifier, Collection<GlLinkRequest> links) throws RestAPIException {
        context.getLogger().info(RB.$("git.upload.asset.links"), owner, repoName, release.getTagName());

        for (GlLinkRequest link : links) {
            context.getLogger().info(" " + RB.$("git.upload.asset.link"), link.getName());
            try {
                api.linkAsset(link, projectIdentifier, release.getTagName());
            } catch (RestAPIException e) {
                context.getLogger().error(" " + RB.$("git.upload.asset.link.failure"), link.getName());
                throw e;
            }
        }
    }

    Optional<User> findUser(String email, String name) throws RestAPIException {
        context.getLogger().debug(RB.$("git.user.lookup"), name, email);

        List<GlUser> users = api.searchUser(CollectionUtils.<String, String>mapOf("scope", "users", "search", email));
        if (null != users && !users.isEmpty()) {
            GlUser user = users.get(0);
            return Optional.of(new User(user.getUsername(), email, user.getWebUrl()));
        }

        users = api.searchUser(CollectionUtils.<String, String>mapOf("scope", "users", "search", name));
        if (null != users && !users.isEmpty()) {
            GlUser user = users.get(0);
            if (name.equals(user.getName())) {
                return Optional.of(new User(user.getUsername(), email, user.getWebUrl()));
            }
        }

        return Optional.empty();
    }

    GlLabel getOrCreateLabel(Integer projectIdentifier, String labelName, String labelColor, String description) throws IOException {
        context.getLogger().debug(RB.$("git.label.fetch", labelName));

        List<GlLabel> labels = listLabels(projectIdentifier);
        Optional<GlLabel> label = labels.stream()
            .filter(l -> l.getName().equals(labelName))
            .findFirst();

        if (label.isPresent()) {
            return label.get();
        }

        context.getLogger().debug(RB.$("git.label.create", labelName));
        return api.createLabel(projectIdentifier, labelName, labelColor, description);
    }

    void addLabelToIssue(Integer projectIdentifier, GlIssue issue, GlLabel label) {
        context.getLogger().debug(RB.$("git.issue.label", label.getName(), issue.getIid()));

        Map<String, Object> params = new LinkedHashMap<>();
        List<String> list = (List<String>) params.computeIfAbsent("labels", k -> new ArrayList<>());
        list.addAll(issue.getLabels());
        list.add(label.getName());

        api.updateIssue(params, projectIdentifier, issue.getIid());
    }

    void commentOnIssue(Integer projectIdentifier, GlIssue issue, String comment) {
        context.getLogger().debug(RB.$("git.issue.comment", issue.getIid()));

        Map<String, String> params = new LinkedHashMap<>();
        params.put("body", comment);

        api.commentIssue(params, projectIdentifier, issue.getIid());
    }

    void setMilestoneOnIssue(Integer projectIdentifier, GlIssue issue, GlMilestone milestone) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("milestone_id", milestone.getId());

        api.updateIssue(params, projectIdentifier, issue.getIid());
    }

    List<GlLabel> listLabels(Integer projectIdentifier) throws IOException {
        context.getLogger().debug(RB.$("gitlab.list.labels"), projectIdentifier);

        List<GlLabel> labels = new ArrayList<>();
        Page<List<GlLabel>> page = api.listLabels0(projectIdentifier);
        labels.addAll(page.getContent());

        if (page.hasLinks() && page.getLinks().hasNext()) {
            try {
                collectLabels(page, labels);
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }

        return labels;
    }

    private void collectLabels(Page<List<GlLabel>> page, List<GlLabel> labels) throws URISyntaxException {
        URI next = new URI(page.getLinks().next());
        context.getLogger().debug(next.toString());

        page = api.listLabels1(next);
        labels.addAll(page.getContent());

        if (page.hasLinks() && page.getLinks().hasNext()) {
            collectLabels(page, labels);
        }
    }

    List<GlIssue> listIssues(Integer projectIdentifier) throws IOException {
        context.getLogger().debug(RB.$("gitlab.list.issues"), projectIdentifier);

        List<GlIssue> issues = new ArrayList<>();
        Page<List<GlIssue>> page = api.listIssues0(projectIdentifier);
        issues.addAll(page.getContent());

        if (page.hasLinks() && page.getLinks().hasNext()) {
            try {
                collectIssues(page, issues);
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }

        return issues;
    }

    private void collectIssues(Page<List<GlIssue>> page, List<GlIssue> issues) throws URISyntaxException {
        URI next = new URI(page.getLinks().next());
        context.getLogger().debug(next.toString());

        page = api.listIssues1(next);
        issues.addAll(page.getContent());

        if (page.hasLinks() && page.getLinks().hasNext()) {
            collectIssues(page, issues);
        }
    }

    List<GlPackage> listPackages(Integer projectIdentifier, String packageType) throws IOException {
        context.getLogger().debug(RB.$("gitlab.list.packages"), projectIdentifier);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("package_type", packageType);

        List<GlPackage> packages = new ArrayList<>();
        Page<List<GlPackage>> page = api.listPackages0(projectIdentifier, params);
        packages.addAll(page.getContent());

        if (page.hasLinks() && page.getLinks().hasNext()) {
            try {
                collectPackages(page, packages);
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }

        return packages;
    }

    private void collectPackages(Page<List<GlPackage>> page, List<GlPackage> packages) throws URISyntaxException {
        URI next = new URI(page.getLinks().next());
        context.getLogger().debug(next.toString());

        page = api.listPackages1(next);
        packages.addAll(page.getContent());

        if (page.hasLinks() && page.getLinks().hasNext()) {
            collectPackages(page, packages);
        }
    }

    Map<String, GlLink> listLinks(Integer projectIdentifier, String tagName) throws IOException {
        context.getLogger().debug(RB.$("gitlab.list.links"), tagName, projectIdentifier);

        List<GlLink> links = new ArrayList<>();
        Page<List<GlLink>> page = api.listLinks0(projectIdentifier, tagName);
        links.addAll(page.getContent());

        if (page.hasLinks() && page.getLinks().hasNext()) {
            try {
                collectLinks(page, links);
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }

        return links.stream()
            .collect(Collectors.toMap(GlLink::getName, Function.identity()));
    }

    private void collectLinks(Page<List<GlLink>> page, List<GlLink> links) throws URISyntaxException {
        URI next = new URI(page.getLinks().next());
        context.getLogger().debug(next.toString());

        page = api.listLinks1(next);
        links.addAll(page.getContent());

        if (page.hasLinks() && page.getLinks().hasNext()) {
            collectLinks(page, links);
        }
    }

    void deleteLinkedAsset(String token, Integer projectIdentifier, String tagName, GlLink link) throws IOException {
        context.getLogger().info(" " + RB.$("git.delete.asset"), link.getName());

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer " + token);

        context.getLogger().debug(RB.$("gitlab.delete.file", link.getName()));
        try {
            TemplateContext props = new TemplateContext();
            props.set("filename", link.getName());

            String url = link.getUrl().substring(apiHost.length() + 1);
            url = url.substring(0, url.length() - link.getName().length() - 1);
            Matcher matcher = UPLOADS_PATTERN.matcher(url);
            if (matcher.matches()) {
                props.set("projectPath", matcher.group(1));
                props.set("secret", matcher.group(2));
            } else {
                throw new IOException(RB.$("ERROR_gitlab_invalid_upload_link", link.getUrl()));
            }

            String payload = resolveTemplate(GRAPQL_DELETE_PAYLOAD, props);

            FormData data = ClientUtils.toFormData(
                "payload",
                headers.computeIfAbsent("Content-Type", k -> "text/plain"),
                payload);

            ClientUtils.postFile(context.getLogger(),
                graphQlEndpoint,
                connectTimeout,
                readTimeout,
                data,
                headers);
        } catch (UploadException e) {
            if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
            throw new IOException(e);
        }

        context.getLogger().debug(RB.$("gitlab.delete.link", link.getId(), link.getName()));
        api.deleteLink(projectIdentifier, tagName, link.getId());
    }

    private FormData toFormData(Path asset) throws IOException {
        return FormData.builder()
            .fileName(asset.getFileName().toString())
            .contentType(MediaType.parse(tika.detect(asset)).toString())
            .data(Files.readAllBytes(asset))
            .build();
    }
}
