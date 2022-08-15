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
package org.jreleaser.sdk.github;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.UpdateSection;
import org.jreleaser.model.releaser.spi.AbstractReleaser;
import org.jreleaser.model.releaser.spi.Asset;
import org.jreleaser.model.releaser.spi.Release;
import org.jreleaser.model.releaser.spi.ReleaseException;
import org.jreleaser.model.releaser.spi.Repository;
import org.jreleaser.model.releaser.spi.User;
import org.jreleaser.model.util.VersionUtils;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.git.ChangelogGenerator;
import org.jreleaser.sdk.git.ChangelogProvider;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.sdk.git.ReleaseUtils;
import org.jreleaser.sdk.github.api.GhAsset;
import org.jreleaser.sdk.github.api.GhRelease;
import org.jreleaser.sdk.github.api.GhReleaseNotes;
import org.jreleaser.sdk.github.api.GhReleaseNotesParams;
import org.jreleaser.util.JReleaserException;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHMilestone;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHReleaseUpdater;
import org.kohsuke.github.GHRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.jreleaser.sdk.git.GitSdk.extractTagName;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class GithubReleaser extends AbstractReleaser {
    public GithubReleaser(JReleaserContext context, List<Asset> assets) {
        super(context, assets);
    }

    @Override
    public String generateReleaseNotes() throws IOException {
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();

        if (github.getReleaseNotes().isEnabled()) {
            return ChangelogProvider.storeChangelog(context, generateReleaseNotesByAPI());
        }

        try {
            return ChangelogProvider.getChangelog(context).trim();
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_changelog"), e);
        }
    }

    private String generateReleaseNotesByAPI() throws JReleaserException {
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();
        String tagName = github.getEffectiveTagName(context.getModel());

        try {
            Git git = GitSdk.of(context).open();
            ChangelogGenerator.Tags tags = new ChangelogGenerator().resolveTags(git, context);
            GhReleaseNotesParams params = new GhReleaseNotesParams();
            params.setTagName(tagName);
            if (!isTagInRemote(context, tagName)) {
                params.setTagName("HEAD");
            }
            if (tags.getPrevious().isPresent()) {
                params.setPreviousTagName(extractTagName(tags.getPrevious().get()));
            }
            params.setTargetCommitish(github.getBranch());
            GhReleaseNotes releaseNotes = new XGithub(context.getLogger(),
                github.getApiEndpoint(),
                github.getResolvedToken(),
                github.getConnectTimeout(),
                github.getReadTimeout())
                .generateReleaseNotes(github.getOwner(), github.getName(), params);
            return releaseNotes.getBody().replace("...HEAD", "..." + tagName);
        } catch (IOException | GitAPIException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_changelog"), e);
        }
    }

    protected boolean isTagInRemote(JReleaserContext context, String tagName) {
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();

        try {
            Github api = new Github(context.getLogger(),
                github.getApiEndpoint(),
                github.getResolvedToken(),
                github.getConnectTimeout(),
                github.getReadTimeout());
            GHRepository repository = api.findRepository(github.getOwner(), github.getName());
            if (null == repository) {
                // remote does not exist!
                throw new IllegalStateException(RB.$("ERROR_git_repository_not_exists", github.getCanonicalRepoName()));
            }

            return null != repository.getRef("tags/" + tagName);
        } catch (FileNotFoundException e) {
            // OK, it means tag does not exist
            return false;
        } catch (IOException e) {
            context.getLogger().trace(e);
            throw new JReleaserException(e);
        }
    }

    @Override
    protected void createTag() throws ReleaseException {
        ReleaseUtils.createTag(context);
    }

    @Override
    protected void createRelease() throws ReleaseException {
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();
        context.getLogger().info(RB.$("git.releaser.releasing"), github.getResolvedRepoUrl(context.getModel()));
        String tagName = github.getEffectiveTagName(context.getModel());

        try {
            Github api = new Github(context.getLogger(),
                github.getApiEndpoint(),
                github.getResolvedToken(),
                github.getConnectTimeout(),
                github.getReadTimeout());

            String branch = github.getBranch();
            Map<String, GHBranch> branches = api.listBranches(github.getOwner(), github.getName());
            if (!branches.containsKey(branch)) {
                throw new ReleaseException(RB.$("ERROR_git_release_branch_not_exists", branch, branches.keySet()));
            }

            String changelog = context.getChangelog();

            context.getLogger().debug(RB.$("git.releaser.release.lookup"), tagName, github.getCanonicalRepoName());
            GHRelease release = api.findReleaseByTag(github.getCanonicalRepoName(), tagName);
            boolean snapshot = context.getModel().getProject().isSnapshot();
            if (null != release) {
                context.getLogger().debug(RB.$("git.releaser.release.exists"), tagName);
                if (github.isOverwrite() || snapshot) {
                    context.getLogger().debug(RB.$("git.releaser.release.delete"), tagName);
                    if (!context.isDryrun()) {
                        release.delete();
                    }
                    context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                    createRelease(api, tagName, changelog, github.isMatch());
                } else if (github.getUpdate().isEnabled()) {
                    context.getLogger().debug(RB.$("git.releaser.release.update"), tagName);
                    if (!context.isDryrun()) {
                        GHReleaseUpdater updater = release.update();
                        updater.prerelease(github.getPrerelease().isEnabled());
                        updater.draft(github.isDraft());
                        if (github.getUpdate().getSections().contains(UpdateSection.TITLE)) {
                            context.getLogger().info(RB.$("git.releaser.release.update.title"), github.getEffectiveReleaseName());
                            updater.name(github.getEffectiveReleaseName());
                        }
                        if (github.getUpdate().getSections().contains(UpdateSection.BODY)) {
                            context.getLogger().info(RB.$("git.releaser.release.update.body"));
                            updater.body(changelog);
                        }
                        release = updater.update();

                        if (github.getUpdate().getSections().contains(UpdateSection.ASSETS)) {
                            updateAssets(api, release);
                        }
                        linkDiscussion(tagName, release);
                    }
                } else {
                    if (context.isDryrun()) {
                        context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                        createRelease(api, tagName, changelog, false);
                        return;
                    }

                    throw new IllegalStateException(RB.$("ERROR_git_releaser_cannot_release",
                        "GitHub", tagName));
                }
            } else {
                context.getLogger().debug(RB.$("git.releaser.release.not.found"), tagName);
                context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                createRelease(api, tagName, changelog, snapshot && github.isMatch());
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

    @Override
    public Repository maybeCreateRepository(String owner, String repo, String password) throws IOException {
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();
        context.getLogger().debug(RB.$("git.repository.lookup"), owner, repo);

        Github api = new Github(context.getLogger(),
            github.getApiEndpoint(),
            password,
            github.getConnectTimeout(),
            github.getReadTimeout());
        GHRepository repository = api.findRepository(owner, repo);
        if (null == repository) {
            repository = api.createRepository(owner, repo);
        }

        return new Repository(
            Repository.Kind.GITHUB,
            owner,
            repo,
            repository.getUrl().toExternalForm(),
            repository.getHttpTransportUrl());
    }

    @Override
    public Optional<User> findUser(String email, String name) {
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();

        try {
            return new XGithub(context.getLogger(),
                github.getApiEndpoint(),
                github.getResolvedToken(),
                github.getConnectTimeout(),
                github.getReadTimeout())
                .findUser(email, name);
        } catch (RestAPIException | IOException e) {
            context.getLogger().trace(e);
            context.getLogger().debug(RB.$("git.releaser.user.not.found"), email);
        }

        return Optional.empty();
    }

    @Override
    public List<Release> listReleases(String owner, String repo) throws IOException {
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();

        Github api = new Github(context.getLogger(),
            github.getApiEndpoint(),
            github.getResolvedToken(),
            github.getConnectTimeout(),
            github.getReadTimeout());

        List<Release> releases = new ArrayList<>();

        for (GHRelease ghRelease : api.listReleases(owner, repo).toList()) {
            if (ghRelease.isDraft() || ghRelease.isPrerelease()) continue;
            releases.add(new Release(
                ghRelease.getName(),
                ghRelease.getTagName(),
                ghRelease.getHtmlUrl().toExternalForm(),
                ghRelease.getPublished_at()
            ));
        }

        VersionUtils.clearUnparseableTags();
        Pattern versionPattern = VersionUtils.resolveVersionPattern(context);
        for (Release release : releases) {
            release.setVersion(VersionUtils.version(context, release.getTagName(), versionPattern));
        }

        releases.sort((r1, r2) -> r2.getVersion().compareTo(r1.getVersion()));

        return releases;
    }

    private void createRelease(Github api, String tagName, String changelog, boolean deleteTags) throws IOException {
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();

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
            deleteTags(api, github.getCanonicalRepoName(), tagName);
        }

        // local tag
        if (deleteTags || !github.isSkipTag()) {
            context.getLogger().debug(RB.$("git.releaser.repository.tag"), tagName);
            GitSdk.of(context).tag(tagName, true, context);
        }

        // remote tag/release
        GHRelease release = api.createRelease(github.getCanonicalRepoName(), tagName)
            .commitish(github.getBranch())
            .name(github.getEffectiveReleaseName())
            .draft(github.isDraft())
            .prerelease(github.getPrerelease().isEnabled())
            .body(changelog)
            .create();
        api.uploadAssets(release, assets);

        if (github.getMilestone().isClose() && !context.getModel().getProject().isSnapshot()) {
            Optional<GHMilestone> milestone = api.findMilestoneByName(
                github.getOwner(),
                github.getName(),
                github.getMilestone().getEffectiveName());
            if (milestone.isPresent()) {
                api.closeMilestone(github.getOwner(),
                    github.getName(),
                    milestone.get());
            }
        }

        linkDiscussion(tagName, release);
    }

    private void updateAssets(Github api, GHRelease release) throws IOException {
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();

        List<Asset> assetsToBeUpdated = new ArrayList<>();
        List<Asset> assetsToBeUploaded = new ArrayList<>();

        Map<String, GHAsset> existingAssets = api.listAssets(github.getOwner(), github.getName(), release);
        Map<String, Asset> assetsToBePublished = new LinkedHashMap<>();
        assets.forEach(asset -> assetsToBePublished.put(asset.getFilename(), asset));

        existingAssets.keySet().forEach(name -> {
            if (existingAssets.containsKey(name)) {
                assetsToBeUpdated.add(assetsToBePublished.get(name));
            } else {
                assetsToBeUploaded.add(assetsToBePublished.get(name));
            }
        });

        api.updateAssets(release, assetsToBeUpdated, existingAssets);
        api.uploadAssets(release, assetsToBeUploaded);
    }

    private void linkDiscussion(String tagName, GHRelease release) {
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();

        String discussionCategoryName = github.getDiscussionCategoryName();
        if (context.getModel().getProject().isSnapshot() ||
            isBlank(discussionCategoryName) ||
            github.isDraft()) return;

        context.getLogger().debug(RB.$("git.releaser.link.discussion"), tagName, discussionCategoryName);

        if (context.isDryrun()) return;

        try {
            XGithub xapi = new XGithub(context.getLogger(),
                github.getApiEndpoint(),
                github.getResolvedToken(),
                github.getConnectTimeout(),
                github.getReadTimeout());

            GhRelease ghRelease = new GhRelease();
            ghRelease.setDiscussionCategoryName(discussionCategoryName);
            xapi.updateRelease(github.getOwner(),
                github.getName(),
                tagName,
                release.getId(),
                ghRelease);
        } catch (RestAPIException | IOException e) {
            context.getLogger().trace(e);
            context.getLogger().warn(RB.$("git.releaser.link.discussion.error"),
                tagName, discussionCategoryName);
        }
    }

    private void deleteTags(Github api, String repo, String tagName) {
        // delete remote tag
        try {
            api.deleteTag(repo, tagName);
        } catch (IOException ignored) {
            //noop
        }
    }
}
