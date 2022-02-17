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

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.UpdateSection;
import org.jreleaser.model.releaser.spi.AbstractReleaser;
import org.jreleaser.model.releaser.spi.Asset;
import org.jreleaser.model.releaser.spi.ReleaseException;
import org.jreleaser.model.releaser.spi.Repository;
import org.jreleaser.model.releaser.spi.User;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.sdk.git.ReleaseUtils;
import org.jreleaser.sdk.github.api.GhRelease;
import org.kohsuke.github.GHMilestone;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHReleaseUpdater;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

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
    protected void createTag() throws ReleaseException {
        ReleaseUtils.createTag(context);
    }

    @Override
    protected void createRelease() throws ReleaseException {
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();
        context.getLogger().info(RB.$("git.releaser.releasing"), github.getResolvedRepoUrl(context.getModel()));
        String tagName = github.getEffectiveTagName(context.getModel());

        try {
            String branch = github.getBranch();
            List<String> branchNames = GitSdk.of(context)
                .getRemoteBranches();
            if (!branchNames.contains(branch)) {
                throw new ReleaseException(RB.$("ERROR_git_release_branch_not_exists", branch, branchNames));
            }

            String changelog = context.getChangelog();

            Github api = new Github(context.getLogger(),
                github.getApiEndpoint(),
                github.getResolvedToken(),
                github.getConnectTimeout(),
                github.getReadTimeout());

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
                        updater.update();

                        if (github.getUpdate().getSections().contains(UpdateSection.ASSETS)) {
                            api.uploadAssets(release, assets);
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
