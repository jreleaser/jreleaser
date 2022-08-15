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
package org.jreleaser.sdk.gitea;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.UpdateSection;
import org.jreleaser.model.releaser.spi.AbstractReleaser;
import org.jreleaser.model.releaser.spi.Asset;
import org.jreleaser.model.releaser.spi.ReleaseException;
import org.jreleaser.model.releaser.spi.Repository;
import org.jreleaser.model.releaser.spi.User;
import org.jreleaser.model.util.VersionUtils;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.git.ChangelogProvider;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.sdk.git.ReleaseUtils;
import org.jreleaser.sdk.gitea.api.GtAsset;
import org.jreleaser.sdk.gitea.api.GtMilestone;
import org.jreleaser.sdk.gitea.api.GtRelease;
import org.jreleaser.sdk.gitea.api.GtRepository;
import org.jreleaser.util.JReleaserException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.jreleaser.util.StringUtils.capitalize;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class GiteaReleaser extends AbstractReleaser {
    public GiteaReleaser(JReleaserContext context, List<Asset> assets) {
        super(context, assets);
    }

    @Override
    public String generateReleaseNotes() throws IOException {
        try {
            return ChangelogProvider.getChangelog(context).trim();
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_changelog"), e);
        }
    }

    @Override
    protected void createTag() throws ReleaseException {
        ReleaseUtils.createTag(context);
    }

    @Override
    protected void createRelease() throws ReleaseException {
        org.jreleaser.model.Gitea gitea = resolveGiteaFromModel();
        context.getLogger().info(RB.$("git.releaser.releasing"), gitea.getResolvedRepoUrl(context.getModel()));
        String tagName = gitea.getEffectiveTagName(context.getModel());

        try {
            Gitea api = new Gitea(context.getLogger(),
                gitea.getApiEndpoint(),
                gitea.getResolvedToken(),
                gitea.getConnectTimeout(),
                gitea.getReadTimeout());

            String branch = gitea.getBranch();

            List<String> branchNames = api.listBranches(gitea.getOwner(), gitea.getName());
            if (!branchNames.contains(branch)) {
                throw new ReleaseException(RB.$("ERROR_git_release_branch_not_exists", branch, branchNames));
            }

            String changelog = context.getChangelog();

            context.getLogger().debug(RB.$("git.releaser.release.lookup"), tagName, gitea.getCanonicalRepoName());
            GtRelease release = api.findReleaseByTag(gitea.getOwner(), gitea.getName(), tagName);
            boolean snapshot = context.getModel().getProject().isSnapshot();
            if (null != release) {
                context.getLogger().debug(RB.$("git.releaser.release.exists"), tagName);
                if (gitea.isOverwrite() || snapshot) {
                    context.getLogger().debug(RB.$("git.releaser.release.delete"), tagName);
                    if (!context.isDryrun()) {
                        api.deleteRelease(gitea.getOwner(), gitea.getName(), tagName, release.getId());
                    }
                    context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                    createRelease(api, tagName, changelog, gitea.isMatch());
                } else if (gitea.getUpdate().isEnabled()) {
                    context.getLogger().debug(RB.$("git.releaser.release.update"), tagName);
                    if (!context.isDryrun()) {
                        GtRelease updater = new GtRelease();
                        updater.setPrerelease(gitea.getPrerelease().isEnabled());
                        updater.setDraft(gitea.isDraft());
                        if (gitea.getUpdate().getSections().contains(UpdateSection.TITLE)) {
                            context.getLogger().info(RB.$("git.releaser.release.update.title"), gitea.getEffectiveReleaseName());
                            updater.setName(gitea.getEffectiveReleaseName());
                        }
                        if (gitea.getUpdate().getSections().contains(UpdateSection.BODY)) {
                            context.getLogger().info(RB.$("git.releaser.release.update.body"));
                            updater.setBody(changelog);
                        }
                        api.updateRelease(gitea.getOwner(), gitea.getName(), release.getId(), updater);

                        if (gitea.getUpdate().getSections().contains(UpdateSection.ASSETS)) {
                            updateAssets(api, release);
                        }
                    }
                } else {
                    if (context.isDryrun()) {
                        context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                        createRelease(api, tagName, changelog, false);
                        return;
                    }

                    throw new IllegalStateException(RB.$("ERROR_git_releaser_cannot_release",
                        capitalize(gitea.getServiceName()), tagName));
                }
            } else {
                context.getLogger().debug(RB.$("git.releaser.release.not.found"), tagName);
                context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                createRelease(api, tagName, changelog, snapshot && gitea.isMatch());
            }
        } catch (RestAPIException e) {
            context.getLogger().trace(e.getStatus() + " " + e.getReason());
            context.getLogger().trace(e);
            throw new ReleaseException(e);
        } catch (IOException | IllegalStateException e) {
            context.getLogger().trace(e);
            throw new ReleaseException(e);
        }
    }

    protected org.jreleaser.model.Gitea resolveGiteaFromModel() {
        return context.getModel().getRelease().getGitea();
    }

    protected Repository.Kind resolveRepositoryKind() {
        return Repository.Kind.OTHER;
    }

    @Override
    public Repository maybeCreateRepository(String owner, String repo, String password) throws IOException {
        org.jreleaser.model.Gitea gitea = resolveGiteaFromModel();
        context.getLogger().debug(RB.$("git.repository.lookup"), owner, repo);

        Gitea api = new Gitea(context.getLogger(),
            gitea.getApiEndpoint(),
            password,
            gitea.getConnectTimeout(),
            gitea.getReadTimeout());
        GtRepository repository = api.findRepository(owner, repo);
        if (null == repository) {
            repository = api.createRepository(owner, repo);
        }

        return new Repository(
            resolveRepositoryKind(),
            owner,
            repo,
            repository.getHtmlUrl(),
            repository.getCloneUrl());
    }

    @Override
    public Optional<User> findUser(String email, String name) {
        org.jreleaser.model.Gitea gitea = resolveGiteaFromModel();

        try {
            String host = gitea.getHost();
            String endpoint = gitea.getApiEndpoint();
            if (endpoint.startsWith("https")) {
                host = "https://" + host;
            } else {
                host = "http://" + host;
            }
            if (!host.endsWith("/")) {
                host += "/";
            }

            return new Gitea(context.getLogger(),
                gitea.getApiEndpoint(),
                gitea.getResolvedToken(),
                gitea.getConnectTimeout(),
                gitea.getReadTimeout())
                .findUser(email, name, host);
        } catch (RestAPIException | IOException e) {
            context.getLogger().trace(e);
            context.getLogger().debug(RB.$("git.releaser.user.not.found"), email);
        }

        return Optional.empty();
    }

    @Override
    public List<org.jreleaser.model.releaser.spi.Release> listReleases(String owner, String repo) throws IOException {
        org.jreleaser.model.Gitea gitea = resolveGiteaFromModel();

        Gitea api = new Gitea(context.getLogger(),
            gitea.getApiEndpoint(),
            gitea.getResolvedToken(),
            gitea.getConnectTimeout(),
            gitea.getReadTimeout());

        List<org.jreleaser.model.releaser.spi.Release> releases = api.listReleases(owner, repo);

        VersionUtils.clearUnparseableTags();
        Pattern versionPattern = VersionUtils.resolveVersionPattern(context);
        for (org.jreleaser.model.releaser.spi.Release release : releases) {
            release.setVersion(VersionUtils.version(context, release.getTagName(), versionPattern));
        }

        releases.sort((r1, r2) -> r2.getVersion().compareTo(r1.getVersion()));

        return releases;
    }

    private void createRelease(Gitea api, String tagName, String changelog, boolean deleteTags) throws IOException {
        org.jreleaser.model.Gitea gitea = resolveGiteaFromModel();

        if (context.isDryrun()) {
            for (Asset asset : assets) {
                if (0 == Files.size(asset.getPath()) || !Files.exists(asset.getPath())) {
                    // do not upload empty or non existent files
                    continue;
                }

                context.getLogger().info(" " + RB.$("git.upload.asset"), asset.getFilename());
            }
            return;
        }

        if (deleteTags) {
            deleteTags(api, gitea.getOwner(), gitea.getName(), tagName);
        }

        // local tag
        if (deleteTags || !gitea.isSkipTag()) {
            context.getLogger().debug(RB.$("git.releaser.repository.tag"), tagName);
            GitSdk.of(context).tag(tagName, true, context);
        }

        // remote tag/release
        GtRelease release = new GtRelease();
        release.setName(gitea.getEffectiveReleaseName());
        release.setTagName(tagName);
        release.setTargetCommitish(gitea.getBranch());
        release.setBody(changelog);

        release = api.createRelease(gitea.getOwner(), gitea.getName(), release);
        api.uploadAssets(gitea.getOwner(), gitea.getName(), release, assets);

        if (gitea.getMilestone().isClose() && !context.getModel().getProject().isSnapshot()) {
            Optional<GtMilestone> milestone = api.findMilestoneByName(
                gitea.getOwner(),
                gitea.getName(),
                gitea.getMilestone().getEffectiveName());
            if (milestone.isPresent()) {
                api.closeMilestone(gitea.getOwner(),
                    gitea.getName(),
                    milestone.get());
            }
        }
    }

    private void updateAssets(Gitea api, GtRelease release) throws IOException {
        org.jreleaser.model.Gitea gitea = resolveGiteaFromModel();

        List<Asset> assetsToBeUpdated = new ArrayList<>();
        List<Asset> assetsToBeUploaded = new ArrayList<>();

        Map<String, GtAsset> existingAssets = api.listAssets(gitea.getOwner(), gitea.getName(), release);
        Map<String, Asset> assetsToBePublished = new LinkedHashMap<>();
        assets.forEach(asset -> assetsToBePublished.put(asset.getFilename(), asset));

        existingAssets.keySet().forEach(name -> {
            if (existingAssets.containsKey(name)) {
                assetsToBeUpdated.add(assetsToBePublished.get(name));
            } else {
                assetsToBeUploaded.add(assetsToBePublished.get(name));
            }
        });

        api.updateAssets(gitea.getOwner(), gitea.getName(), release, assetsToBeUpdated, existingAssets);
        api.uploadAssets(gitea.getOwner(), gitea.getName(), release, assetsToBeUploaded);
    }

    private void deleteTags(Gitea api, String owner, String repo, String tagName) {
        // delete remote tag
        try {
            api.deleteTag(owner, repo, tagName);
        } catch (RestAPIException ignored) {
            //noop
        }
    }
}
