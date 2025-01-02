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
package org.jreleaser.sdk.gitea;

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
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.model.spi.release.Asset;
import org.jreleaser.model.spi.release.Release;
import org.jreleaser.model.spi.release.User;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.gitea.api.GiteaAPI;
import org.jreleaser.sdk.gitea.api.GtAsset;
import org.jreleaser.sdk.gitea.api.GtBranch;
import org.jreleaser.sdk.gitea.api.GtIssue;
import org.jreleaser.sdk.gitea.api.GtLabel;
import org.jreleaser.sdk.gitea.api.GtMilestone;
import org.jreleaser.sdk.gitea.api.GtOrganization;
import org.jreleaser.sdk.gitea.api.GtRelease;
import org.jreleaser.sdk.gitea.api.GtRepository;
import org.jreleaser.sdk.gitea.api.GtSearchUser;
import org.jreleaser.sdk.gitea.api.GtUser;
import org.jreleaser.sdk.gitea.internal.Page;
import org.jreleaser.sdk.gitea.internal.PaginatingDecoder;
import org.jreleaser.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Gitea {
    private static final String API_V1 = "/api/v1";
    private final Tika tika = new Tika();

    private final JReleaserContext context;
    private final GiteaAPI api;

    public Gitea(JReleaserContext context,
                 String endpoint,
                 String token,
                 int connectTimeout,
                 int readTimeout) {
        requireNonNull(context, "'context' must not be null");
        requireNonBlank(token, "'token' must not be blank");
        requireNonBlank(endpoint, "'endpoint' must not be blank");

        if (!endpoint.endsWith(API_V1)) {
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
            endpoint += API_V1;
        }

        ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

        this.context = context;
        this.api = ClientUtils.builder(context, connectTimeout, readTimeout)
            .client(new ApacheHttpClient())
            .encoder(new FormEncoder(new JacksonEncoder(objectMapper)))
            .decoder(new PaginatingDecoder(new JacksonDecoder(objectMapper)))
            .requestInterceptor(template -> template.header("Authorization", String.format("token %s", token)))
            .target(GiteaAPI.class, endpoint);
    }

    public GtRepository findRepository(String owner, String repo) {
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

    public List<Release> listReleases(String owner, String repoName) {
        context.getLogger().debug(RB.$("git.list.releases"), owner, repoName);

        List<Release> releases = new ArrayList<>();

        int pageCount = 0;
        Map<String, Object> params = CollectionUtils.<String, Object>map()
            .e("draft", false)
            .e("prerelease", false)
            .e("limit", 20);

        boolean consume = true;
        do {
            params.put("page", ++pageCount);
            Page<List<GtRelease>> page = api.listReleases(owner, repoName, params);
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

    public List<String> listBranches(String owner, String repoName) {
        context.getLogger().debug(RB.$("git.list.branches"), owner, repoName);

        List<String> branches = new ArrayList<>();

        int pageCount = 0;
        Map<String, Object> params = CollectionUtils.<String, Object>map()
            .e("limit", 20);

        boolean consume = true;
        do {
            params.put("page", ++pageCount);
            Page<List<GtBranch>> page = api.listBranches(owner, repoName, params);
            page.getContent().stream()
                .map(GtBranch::getName)
                .forEach(branches::add);

            if (!page.hasLinks() || !page.getLinks().hasNext()) {
                consume = false;
            }
        }
        while (consume);

        return branches;
    }

    public Map<String, GtAsset> listAssets(String owner, String repo, GtRelease release) {
        context.getLogger().debug(RB.$("git.list.assets.github"), owner, repo, release.getId());

        Map<String, GtAsset> assets = new LinkedHashMap<>();
        for (GtAsset asset : api.listAssets(owner, repo, release.getId())) {
            assets.put(asset.getName(), asset);
        }

        return assets;
    }

    public Optional<GtMilestone> findMilestoneByName(String owner, String repo, String milestoneName) {
        context.getLogger().debug(RB.$("git.milestone.lookup"), milestoneName, owner, repo);

        return findMilestone(owner, repo, milestoneName, "open");
    }

    public Optional<GtMilestone> findClosedMilestoneByName(String owner, String repo, String milestoneName) {
        context.getLogger().debug(RB.$("git.milestone.lookup.closed"), milestoneName, owner, repo);

        return findMilestone(owner, repo, milestoneName, "closed");
    }

    private Optional<GtMilestone> findMilestone(String owner, String repo, String milestoneName, String state) {
        try {
            GtMilestone milestone = api.findMilestoneByTitle(owner, repo, milestoneName);

            if (null == milestone) {
                return Optional.empty();
            }

            return state.equals(milestone.getState()) ? Optional.of(milestone) : Optional.empty();
        } catch (RestAPIException e) {
            if (e.isNotFound()) {
                // ok
                return Optional.empty();
            }
            throw e;
        }
    }

    public void closeMilestone(String owner, String repo, GtMilestone milestone) {
        context.getLogger().debug(RB.$("git.milestone.close"), milestone.getTitle(), owner, repo);

        api.updateMilestone(CollectionUtils.<String, Object>map()
            .e("state", "closed"), owner, repo, milestone.getId());
    }

    public GtRepository createRepository(String owner, String repo) {
        context.getLogger().debug(RB.$("git.repository.create"), owner, repo);

        Map<String, Object> params = CollectionUtils.<String, Object>map()
            .e("name", repo)
            .e("private", false);

        GtOrganization organization = resolveOrganization(owner);
        if (null != organization) {
            return api.createRepository(params, owner);
        }

        return api.createRepository(params);
    }

    private GtOrganization resolveOrganization(String name) {
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

    public GtRelease findReleaseByTag(String owner, String repo, String tagName) {
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

    public void deleteRelease(String owner, String repo, String tagName, Integer id) throws RestAPIException {
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

    public void deleteTag(String owner, String repo, String tagName) throws RestAPIException {
        context.getLogger().debug(RB.$("git.delete.tag.from"), tagName, owner, repo);

        api.deleteTag(owner, repo, tagName);
    }

    public void deletePackage(String owner, String type, String name, String version) throws RestAPIException {
        context.getLogger().debug(RB.$("gitea.delete.package"), owner, type, name, version);

        api.deletePackage(owner, type, name, version);
    }

    public GtRelease createRelease(String owner, String repo, GtRelease release) throws RestAPIException {
        context.getLogger().debug(RB.$("git.create.release"), owner, repo, release.getTagName());

        return api.createRelease(release, owner, repo);
    }

    public void updateRelease(String owner, String repo, Integer id, GtRelease release) throws RestAPIException {
        context.getLogger().debug(RB.$("git.update.release"), owner, repo, release.getTagName());

        api.updateRelease(release, owner, repo, id);
    }

    public void uploadAssets(String owner, String repo, GtRelease release, Set<Asset> assets) throws IOException {
        for (Asset asset : assets) {
            if (0 == Files.size(asset.getPath()) || !Files.exists(asset.getPath())) {
                // do not upload empty or non existent files
                continue;
            }

            uploadOrUpdateAsset(asset, owner, repo, release,
                "git.upload.asset", "git.upload.asset.failure");
        }
    }

    public void updateAssets(String owner, String repo, GtRelease release, Set<Asset> assets, Map<String, GtAsset> existingAssets) throws IOException {
        for (Asset asset : assets) {
            if (0 == Files.size(asset.getPath()) || !Files.exists(asset.getPath())) {
                // do not upload empty or non existent files
                continue;
            }

            context.getLogger().debug(" " + RB.$("git.delete.asset"), asset.getFilename());
            try {
                api.deleteAsset(owner, repo, release.getId(), existingAssets.get(asset.getFilename()).getId());
            } catch (RestAPIException e) {
                context.getLogger().error(" " + RB.$("git.delete.asset.failure"), asset.getFilename());
                throw e;
            }

            uploadOrUpdateAsset(asset, owner, repo, release,
                "git.update.asset", "git.update.asset.failure");
        }
    }

    private void uploadOrUpdateAsset(Asset asset, String owner, String repo, GtRelease release, String operationMessageKey, String operationErrorMessageKey) throws IOException {
        context.getLogger().info(" " + RB.$(operationMessageKey), asset.getFilename());
        try {
            api.uploadAsset(owner, repo, release.getId(), toFormData(asset.getPath()));
        } catch (RestAPIException e) {
            context.getLogger().error(" " + RB.$(operationErrorMessageKey), asset.getFilename());
            throw e;
        }
    }

    public Optional<User> findUser(String email, String name, String host) throws RestAPIException {
        context.getLogger().debug(RB.$("git.user.lookup"), name, email);

        GtSearchUser search = api.searchUser(CollectionUtils.<String, String>mapOf("q", email));
        if (null != search.getData() && !search.getData().isEmpty()) {
            GtUser user = search.getData().get(0);
            return Optional.of(new User(user.getUsername(), email, host + user.getUsername()));
        }

        return Optional.empty();
    }

    public GtLabel getOrCreateLabel(String owner, String name, String labelName, String labelColor, String description) {
        context.getLogger().debug(RB.$("git.label.fetch", labelName));

        List<GtLabel> labels = listLabels(owner, name);
        Optional<GtLabel> label = labels.stream()
            .filter(l -> l.getName().equals(labelName))
            .findFirst();

        if (label.isPresent()) {
            return label.get();
        }

        context.getLogger().debug(RB.$("git.label.create", labelName));
        return api.createLabel(owner, name, labelName, labelColor, description);
    }

    public Optional<GtIssue> findIssue(String owner, String name, int issueNumber) {
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

    public void addLabelToIssue(String owner, String name, GtIssue issue, GtLabel label) {
        context.getLogger().debug(RB.$("git.issue.label", label.getName(), issue.getNumber()));

        Map<String, List<Integer>> labels = new LinkedHashMap<>();
        List<Integer> list = labels.computeIfAbsent("labels", k -> new ArrayList<>());
        list.addAll(issue.getLabels().stream().map(GtLabel::getId).collect(toList()));
        list.add(label.getId());

        api.labelIssue(labels, owner, name, issue.getNumber());
    }

    public void commentOnIssue(String owner, String name, GtIssue issue, String comment) {
        context.getLogger().debug(RB.$("git.issue.comment", issue.getNumber()));

        Map<String, String> params = new LinkedHashMap<>();
        params.put("body", comment);

        api.commentIssue(params, owner, name, issue.getNumber());
    }

    public void setMilestoneOnIssue(String owner, String name, GtIssue issue, GtMilestone milestone) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("milestone", milestone.getId());

        api.updateIssue(params, owner, name, issue.getNumber());
    }

    private List<GtLabel> listLabels(String owner, String repoName) {
        context.getLogger().debug(RB.$("git.list.labels"), owner, repoName);

        List<GtLabel> labels = new ArrayList<>();

        int pageCount = 0;
        Map<String, Object> params = CollectionUtils.<String, Object>map()
            .e("limit", 20);

        boolean consume = true;
        do {
            params.put("page", ++pageCount);
            Page<List<GtLabel>> page = api.listLabels(owner, repoName, params);
            labels.addAll(page.getContent());

            if (!page.hasLinks() || !page.getLinks().hasNext()) {
                consume = false;
            }
        }
        while (consume);

        return labels;
    }

    private FormData toFormData(Path asset) throws IOException {
        return FormData.builder()
            .fileName(asset.getFileName().toString())
            .contentType(MediaType.parse(tika.detect(asset)).toString())
            .data(Files.readAllBytes(asset))
            .build();
    }
}
