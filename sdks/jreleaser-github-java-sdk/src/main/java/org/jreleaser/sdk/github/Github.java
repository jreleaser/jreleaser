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
package org.jreleaser.sdk.github;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import feign.form.FormData;
import feign.form.FormEncoder;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.model.spi.release.Asset;
import org.jreleaser.model.spi.release.Release;
import org.jreleaser.model.spi.release.User;
import org.jreleaser.model.spi.upload.UploadException;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.github.api.GhAsset;
import org.jreleaser.sdk.github.api.GhAttachment;
import org.jreleaser.sdk.github.api.GhBranch;
import org.jreleaser.sdk.github.api.GhDiscussion;
import org.jreleaser.sdk.github.api.GhIssue;
import org.jreleaser.sdk.github.api.GhLabel;
import org.jreleaser.sdk.github.api.GhMilestone;
import org.jreleaser.sdk.github.api.GhOrganization;
import org.jreleaser.sdk.github.api.GhPackageVersion;
import org.jreleaser.sdk.github.api.GhRelease;
import org.jreleaser.sdk.github.api.GhReleaseNotes;
import org.jreleaser.sdk.github.api.GhReleaseNotesParams;
import org.jreleaser.sdk.github.api.GhRepository;
import org.jreleaser.sdk.github.api.GhSearchUser;
import org.jreleaser.sdk.github.api.GhTag;
import org.jreleaser.sdk.github.api.GhUser;
import org.jreleaser.sdk.github.api.GithubAPI;
import org.jreleaser.sdk.github.internal.Page;
import org.jreleaser.sdk.github.internal.PaginatingDecoder;
import org.jreleaser.util.CollectionUtils;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.sdk.commons.ClientUtils.toFormData;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
class Github {
    private static final String USERS_NOREPLY_GITHUB_COM = "@users.noreply.github.com";
    private static final String ENDPOINT = "https://api.github.com";
    private static final String GITHUB_API_VERSION = "2022-11-28";
    private static final String GITHUB_MIME_TYPE = "application/vnd.github+json";

    private final JReleaserContext context;
    private final ObjectMapper objectMapper;
    private final GithubAPI api;
    private final String token;
    private final int connectTimeout;
    private final int readTimeout;

    Github(JReleaserContext context,
           String token,
           int connectTimeout,
           int readTimeout) {
        this(context, ENDPOINT, token, connectTimeout, readTimeout);
    }

    Github(JReleaserContext context,
           String endpoint,
           String token,
           int connectTimeout,
           int readTimeout) {
        this.context = requireNonNull(context, "'context' must not be null");
        this.token = requireNonBlank(token, "'token' must not be blank");
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        requireNonBlank(endpoint, "'endpoint' must not be blank");

        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }

        this.objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

        this.api = ClientUtils.builder(context, connectTimeout, readTimeout)
            .client(new ApacheHttpClient())
            .encoder(new FormEncoder(new JacksonEncoder(objectMapper)))
            .decoder(new PaginatingDecoder(new JacksonDecoder(objectMapper)))
            .requestInterceptor(template -> {
                template.header("Accept", GITHUB_MIME_TYPE);
                template.header("X-GitHub-Api-Version", GITHUB_API_VERSION);
                template.header("Authorization", String.format("Bearer %s", token));
            })
            .target(GithubAPI.class, endpoint);
    }

    GhRepository findRepository(String owner, String repo) {
        context.getLogger().debug(RB.$("git.repository.lookup"), owner, repo);
        try {
            return api.getRepository(owner, repo);
        } catch (RestAPIException e) {
            if (e.isNotFound()) {
                // ok
                return null;
            }
            throw e;
        }
    }

    List<Release> listReleases(String owner, String repoName) {
        context.getLogger().debug(RB.$("git.list.releases"), owner, repoName);

        List<Release> releases = new ArrayList<>();

        int pageCount = 0;
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("draft", false);
        params.put("prerelease", false);
        params.put("per_page", 20);

        boolean consume = true;
        do {
            params.put("page", ++pageCount);
            Page<List<GhRelease>> page = api.listReleases(owner, repoName, params);
            page.getContent().stream()
                .map(r -> new Release(
                    r.getName(),
                    r.getTagName(),
                    r.getHtmlUrl(),
                    r.getPublishedAt()
                ))
                .forEach(releases::add);

            if (!page.hasLinks() || !page.getLinks().hasNext()) {
                consume = false;
            }
        }
        while (consume);

        return releases;
    }

    List<String> listBranches(String owner, String repoName) {
        context.getLogger().debug(RB.$("git.list.branches"), owner, repoName);

        List<String> branches = new ArrayList<>();

        int pageCount = 0;
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("per_page", 20);

        boolean consume = true;
        do {
            params.put("page", ++pageCount);
            Page<List<GhBranch>> page = api.listBranches(owner, repoName, params);
            page.getContent().stream()
                .map(GhBranch::getName)
                .forEach(branches::add);

            if (!page.hasLinks() || !page.getLinks().hasNext()) {
                consume = false;
            }
        }
        while (consume);

        return branches;
    }

    Map<String, GhAsset> listAssets(String owner, String repo, GhRelease release) {
        context.getLogger().debug(RB.$("git.list.assets.github"), owner, repo, release.getId());

        Map<String, GhAsset> assets = new LinkedHashMap<>();
        for (GhAsset asset : api.listAssets(owner, repo, release.getId())) {
            assets.put(asset.getName(), asset);
        }

        return assets;
    }

    List<GhMilestone> listMilestones(String owner, String repoName, String state) {
        context.getLogger().debug(RB.$("git.list.milestones"), owner, repoName, state);

        List<GhMilestone> milestones = new ArrayList<>();

        int pageCount = 0;
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("state", state);
        params.put("per_page", 20);

        boolean consume = true;
        do {
            params.put("page", ++pageCount);
            Page<List<GhMilestone>> page = api.listMilestones(owner, repoName, params);
            milestones.addAll(page.getContent());

            if (!page.hasLinks() || !page.getLinks().hasNext()) {
                consume = false;
            }
        }
        while (consume);

        return milestones;
    }

    Optional<GhMilestone> findMilestoneByName(String owner, String repo, String milestoneName) {
        context.getLogger().debug(RB.$("git.milestone.lookup"), milestoneName, owner, repo);

        return findMilestone(owner, repo, milestoneName, "open");
    }

    Optional<GhMilestone> findClosedMilestoneByName(String owner, String repo, String milestoneName) {
        context.getLogger().debug(RB.$("git.milestone.lookup.closed"), milestoneName, owner, repo);

        return findMilestone(owner, repo, milestoneName, "closed");
    }

    private Optional<GhMilestone> findMilestone(String owner, String repo, String milestoneName, String state) {
        return listMilestones(owner, repo, state).stream()
            .filter(m -> milestoneName.equals(m.getTitle()))
            .findFirst();
    }

    void closeMilestone(String owner, String repo, GhMilestone milestone) {
        context.getLogger().debug(RB.$("git.milestone.close"), milestone.getTitle(), owner, repo);

        api.updateMilestone(CollectionUtils.<String, Object>map()
            .e("state", "closed"), owner, repo, milestone.getNumber());
    }

    GhRepository createRepository(String owner, String repo) {
        context.getLogger().debug(RB.$("git.repository.create"), owner, repo);

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("name", repo);
        params.put("private", false);

        GhOrganization organization = resolveOrganization(owner);
        if (null != organization) {
            return api.createRepository(params, owner);
        }

        return api.createRepository(params);
    }

    List<GhTag> listTags(String owner, String repoName) {
        context.getLogger().debug(RB.$("git.list.milestones"), owner, repoName);

        List<GhTag> tags = new ArrayList<>();

        int pageCount = 0;
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("per_page", 20);

        boolean consume = true;
        do {
            params.put("page", ++pageCount);
            Page<List<GhTag>> page = api.listTags(owner, repoName, params);
            tags.addAll(page.getContent());

            if (!page.hasLinks() || !page.getLinks().hasNext()) {
                consume = false;
            }
        }
        while (consume);

        return tags;
    }

    GhRelease findReleaseByTag(String owner, String repo, String tagName) {
        context.getLogger().debug(RB.$("git.fetch.release.by.tag"), owner, repo, tagName);

        try {
            return api.getReleaseByTagName(owner, repo, tagName);
        } catch (RestAPIException e) {
            if (e.isNotFound()) {
                // ok
                return null;
            }
            throw e;
        }
    }

    GhRelease findReleaseById(String owner, String repo, Long id) {
        context.getLogger().debug(RB.$("git.fetch.release.by.id"), owner, repo, id);

        return api.getRelease(owner, repo, id);
    }

    void deleteTag(String owner, String repo, String tagName) throws RestAPIException {
        context.getLogger().debug(RB.$("git.delete.tag.from"), tagName, owner, repo);

        try {
            api.deleteTag(owner, repo, tagName);
        } catch (RestAPIException e) {
            if (e.isNotFound()) {
                context.getLogger().debug(RB.$("git.tag.not.exist"), tagName);
            }
        }
    }

    GhRelease createRelease(String owner, String repo, GhRelease release) throws RestAPIException {
        context.getLogger().debug(RB.$("git.create.release"), owner, repo, release.getTagName());

        return api.createRelease(release, owner, repo);
    }

    void updateRelease(String owner, String repo, Long id, GhRelease release) throws RestAPIException {
        context.getLogger().debug(RB.$("git.update.release"), owner, repo, release.getTagName());

        api.updateRelease(release, owner, repo, id);
    }

    void deleteRelease(String owner, String repo, String tagName, Long id) throws RestAPIException {
        context.getLogger().debug(RB.$("git.delete.release.from.id"), tagName, owner, repo, id);

        try {
            api.deleteRelease(owner, repo, id);
        } catch (RestAPIException e) {
            if (e.isNotFound()) {
                // OK. Release might have been deleted but
                // tag still exists.
                return;
            }
            throw e;
        }
    }

    void uploadAssets(GhRelease release, Set<Asset> assets) throws IOException {
        for (Asset asset : assets) {
            if (0 == Files.size(asset.getPath()) || !Files.exists(asset.getPath())) {
                // do not upload empty or non existent files
                continue;
            }

            uploadOrUpdateAsset(asset, release, "git.upload.asset", "git.upload.asset.failure");
        }
    }

    void updateAssets(String owner, String repo, GhRelease release, Set<Asset> assets, Map<String, GhAsset> existingAssets) throws IOException {
        for (Asset asset : assets) {
            if (0 == Files.size(asset.getPath()) || !Files.exists(asset.getPath())) {
                // do not upload empty or non existent files
                continue;
            }

            context.getLogger().debug(" " + RB.$("git.delete.asset"), asset.getFilename());
            try {
                api.deleteAsset(owner, repo, existingAssets.get(asset.getFilename()).getId());
            } catch (RestAPIException e) {
                context.getLogger().error(" " + RB.$("git.delete.asset.failure"), asset.getFilename());
                throw e;
            }

            uploadOrUpdateAsset(asset, release, "git.update.asset", "git.update.asset.failure");
        }
    }

    private void uploadOrUpdateAsset(Asset asset, GhRelease release, String operationMessageKey, String operationErrorMessageKey) throws IOException {
        context.getLogger().info(" " + RB.$(operationMessageKey), asset.getFilename());

        try {
            String uploadUrl = release.getUploadUrl();
            if (uploadUrl.endsWith("{?name,label}")) {
                uploadUrl = uploadUrl.substring(0, uploadUrl.length() - 13);
            }

            URI uri = new URI(uploadUrl + "?name=" + asset.getFilename());
            GhAttachment attachment = uploadAsset(uri, toFormData(asset.getPath()));
            if (!"uploaded".equalsIgnoreCase(attachment.getState())) {
                context.getLogger().warn(" " + RB.$(operationErrorMessageKey), asset.getFilename());
            }
        } catch (URISyntaxException shouldNeverHappen) {
            context.getLogger().error(" " + RB.$(operationErrorMessageKey), asset.getFilename());
            throw new IllegalStateException(RB.$("ERROR_unexpected_error"), shouldNeverHappen);
        } catch (RestAPIException e) {
            context.getLogger().error(" " + RB.$(operationErrorMessageKey), asset.getFilename());
            throw e;
        } catch (UploadException e) {
            context.getLogger().error(" " + RB.$(operationErrorMessageKey), asset.getFilename());
            if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
            throw new IOException(e);
        }
    }

    private GhAttachment uploadAsset(URI uri, FormData data) throws UploadException, IOException {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", GITHUB_MIME_TYPE);
        headers.put("X-GitHub-Api-Version", GITHUB_API_VERSION);
        headers.put("Authorization", String.format("Bearer %s", token));

        Reader reader = ClientUtils.postFile(context.getLogger(),
            uri,
            connectTimeout,
            readTimeout,
            data,
            headers);

        return objectMapper.readValue(reader, GhAttachment.class);
    }

    Optional<GhDiscussion> findDiscussion(String organization, String team, String title) {
        return listDiscussions(organization, team).stream()
            .filter(d -> title.equals(d.getTitle()))
            .findFirst();
    }

    List<GhDiscussion> listDiscussions(String organization, String team) {
        context.getLogger().debug(RB.$("git.list.discussions"), organization, team);

        List<GhDiscussion> discussions = new ArrayList<>();

        int pageCount = 0;
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("per_page", 20);

        boolean consume = true;
        do {
            params.put("page", ++pageCount);
            Page<List<GhDiscussion>> page = api.listDiscussions(organization, team, params);
            discussions.addAll(page.getContent());

            if (!page.hasLinks() || !page.getLinks().hasNext()) {
                consume = false;
            }
        }
        while (consume);

        return discussions;
    }

    void createDiscussion(String organization, String team, String title, String message) {
        context.getLogger().debug(RB.$("git.releaser.discussion.create"), title);

        GhDiscussion discussion = new GhDiscussion();
        discussion.setTitle(title);
        discussion.setBody(message);

        api.createDiscussion(discussion, organization, team);
    }

    GhLabel getOrCreateLabel(String owner, String name, String labelName, String labelColor, String description) {
        context.getLogger().debug(RB.$("git.label.fetch", labelName));

        List<GhLabel> labels = listLabels(owner, name);
        Optional<GhLabel> label = labels.stream()
            .filter(l -> l.getName().equals(labelName))
            .findFirst();

        if (label.isPresent()) {
            return label.get();
        }

        context.getLogger().debug(RB.$("git.label.create", labelName));
        return api.createLabel(owner, name, labelName, labelColor, description);
    }

    public Optional<GhIssue> findIssue(String owner, String name, int issueNumber) {
        context.getLogger().debug(RB.$("git.issue.fetch", issueNumber));
        try {
            return Optional.of(api.findIssue(owner, name, issueNumber));
        } catch (RestAPIException e) {
            if (e.isNotFound()) {
                return Optional.empty();
            }
            throw e;
        }
    }

    void addLabelToIssue(String owner, String name, GhIssue issue, GhLabel label) {
        context.getLogger().debug(RB.$("git.issue.label", label.getName(), issue.getNumber()));

        Map<String, List<String>> labels = new LinkedHashMap<>();
        List<String> list = labels.computeIfAbsent("labels", k -> new ArrayList<>());
        list.addAll(issue.getLabels().stream().map(GhLabel::getName).collect(toList()));
        list.add(label.getName());

        api.labelIssue(labels, owner, name, issue.getNumber());
    }

    void commentOnIssue(String owner, String name, GhIssue issue, String comment) {
        context.getLogger().debug(RB.$("git.issue.comment", issue.getNumber()));

        Map<String, String> params = new LinkedHashMap<>();
        params.put("body", comment);

        api.commentIssue(params, owner, name, issue.getNumber());
    }

    void setMilestoneOnIssue(String owner, String name, GhIssue issue, GhMilestone milestone) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("milestone", milestone.getNumber());

        api.updateIssue(params, owner, name, issue.getNumber());
    }

    private List<GhLabel> listLabels(String owner, String repoName) {
        context.getLogger().debug(RB.$("git.list.labels"), owner, repoName);

        List<GhLabel> labels = new ArrayList<>();

        int pageCount = 0;
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("limit", 20);

        boolean consume = true;
        do {
            params.put("page", ++pageCount);
            Page<List<GhLabel>> page = api.listLabels(owner, repoName, params);
            labels.addAll(page.getContent());

            if (!page.hasLinks() || !page.getLinks().hasNext()) {
                consume = false;
            }
        }
        while (consume);

        return labels;
    }

    private GhOrganization resolveOrganization(String name) {
        try {
            return api.getOrganization(name);
        } catch (RestAPIException e) {
            if (e.isNotFound()) {
                // ok
                return null;
            }
            throw e;
        }
    }

    void updateRelease(String owner, String repo, String tag, Long id, GhRelease release) throws RestAPIException {
        context.getLogger().debug(RB.$("git.update.release"), owner, repo, tag);

        api.updateRelease(release, owner, repo, id);
    }

    private String getPrivateEmailUserId(String email) {
        if (!email.endsWith(USERS_NOREPLY_GITHUB_COM)) return null;
        String username = email.substring(0, email.indexOf("@"));
        if (username.contains("+")) {
            username = username.substring(username.indexOf("+") + 1);
        }
        return username;
    }

    Optional<User> findUser(String email, String name) throws RestAPIException {
        context.getLogger().debug(RB.$("git.user.lookup"), name, email);

        String username = getPrivateEmailUserId(email);
        if (null != username) {
            GhUser user = api.getUser(username);
            if (null != user) {
                return Optional.of(new User(user.getLogin(), email, user.getHtmlUrl()));
            }
        }

        GhSearchUser search = api.searchUser(CollectionUtils.<String, String>mapOf("q", email));
        if (search.getTotalCount() > 0) {
            GhUser user = search.getItems().get(0);
            return Optional.of(new User(user.getLogin(), email, user.getHtmlUrl()));
        }

        return Optional.empty();
    }

    GhReleaseNotes generateReleaseNotes(String owner, String repo, GhReleaseNotesParams params) throws RestAPIException {
        context.getLogger().info(RB.$("github.generate.release.notes"), owner, repo, params.getPreviousTagName(), params.getTagName());

        return api.generateReleaseNotes(params, owner, repo);
    }

    List<GhPackageVersion> listPackageVersions(String packageType, String packageName) throws IOException {
        context.getLogger().debug(RB.$("github.list.versions"), packageType, packageName);

        List<GhPackageVersion> issues = new ArrayList<>();
        Page<List<GhPackageVersion>> page = api.listPackageVersions0(packageType, packageName);
        issues.addAll(page.getContent());

        if (page.hasLinks() && page.getLinks().hasNext()) {
            try {
                collectPackageVersions(page, issues);
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }

        return issues;
    }

    private void collectPackageVersions(Page<List<GhPackageVersion>> page, List<GhPackageVersion> issues) throws URISyntaxException {
        URI next = new URI(page.getLinks().next());
        context.getLogger().debug(next.toString());

        page = api.listPackageVersions1(next);
        issues.addAll(page.getContent());

        if (page.hasLinks() && page.getLinks().hasNext()) {
            collectPackageVersions(page, issues);
        }
    }

    void deletePackageVersion(String packageType, String packageName, String packageVersion) throws RestAPIException {
        context.getLogger().debug(RB.$("github.delete.package.version"), packageVersion, packageName);

        api.deletePackageVersion(packageType, packageName, packageVersion);
    }

    void deletePackage(String packageType, String packageName) throws RestAPIException {
        context.getLogger().debug(RB.$("github.delete.package"), packageType, packageName);

        api.deletePackage(packageType, packageName);
    }
}
