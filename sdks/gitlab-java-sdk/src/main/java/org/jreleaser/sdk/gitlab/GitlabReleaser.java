/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.UpdateSection;
import org.jreleaser.model.releaser.spi.AbstractReleaser;
import org.jreleaser.model.releaser.spi.ReleaseException;
import org.jreleaser.model.releaser.spi.Repository;
import org.jreleaser.model.releaser.spi.User;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.sdk.git.ReleaseUtils;
import org.jreleaser.sdk.gitlab.api.FileUpload;
import org.jreleaser.sdk.gitlab.api.Milestone;
import org.jreleaser.sdk.gitlab.api.Project;
import org.jreleaser.sdk.gitlab.api.Release;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class GitlabReleaser extends AbstractReleaser {
    public GitlabReleaser(JReleaserContext context, List<Path> assets) {
        super(context, assets);
    }

    @Override
    protected void createTag() throws ReleaseException {
        ReleaseUtils.createTag(context);
    }

    @Override
    protected void createRelease() throws ReleaseException {
        org.jreleaser.model.Gitlab gitlab = context.getModel().getRelease().getGitlab();
        context.getLogger().info(RB.$("git.releaser.releasing"), gitlab.getResolvedRepoUrl(context.getModel()));
        String tagName = gitlab.getEffectiveTagName(context.getModel());

        try {
            String changelog = context.getChangelog();

            Gitlab api = new Gitlab(context.getLogger(),
                gitlab.getApiEndpoint(),
                gitlab.getResolvedToken(),
                gitlab.getConnectTimeout(),
                gitlab.getReadTimeout());

            context.getLogger().debug(RB.$("git.releaser.release.lookup"), tagName, gitlab.getCanonicalRepoName());
            Release release = api.findReleaseByTag(gitlab.getOwner(), gitlab.getName(), gitlab.getIdentifier(), tagName);
            boolean snapshot = context.getModel().getProject().isSnapshot();
            if (null != release) {
                context.getLogger().debug(RB.$("git.releaser.release.exists"), tagName);
                if (gitlab.isOverwrite() || snapshot) {
                    context.getLogger().debug(RB.$("git.releaser.release.delete"), tagName);
                    if (!context.isDryrun()) {
                        api.deleteRelease(gitlab.getOwner(), gitlab.getName(), gitlab.getIdentifier(), tagName);
                    }
                    context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                    createRelease(api, tagName, changelog, true);
                } else if (gitlab.isUpdate()) {
                    context.getLogger().debug(RB.$("git.releaser.release.update"), tagName);
                    if (!context.isDryrun()) {
                        boolean update = false;
                        Release updater = new Release();
                        if (gitlab.getUpdateSections().contains(UpdateSection.TITLE)) {
                            update = true;
                            context.getLogger().info(RB.$("git.releaser.release.update.title"), gitlab.getEffectiveReleaseName());
                            updater.setName(gitlab.getEffectiveReleaseName());
                        }
                        if (gitlab.getUpdateSections().contains(UpdateSection.BODY)) {
                            update = true;
                            context.getLogger().info(RB.$("git.releaser.release.update.body"));
                            updater.setDescription(changelog);
                        }
                        if (update) {
                            api.updateRelease(gitlab.getOwner(), gitlab.getName(), gitlab.getIdentifier(), updater);
                        }

                        if (gitlab.getUpdateSections().contains(UpdateSection.ASSETS)) {
                            List<FileUpload> uploads = api.uploadAssets(gitlab.getOwner(), gitlab.getName(), gitlab.getIdentifier(), assets);
                            api.linkAssets(gitlab.getOwner(), gitlab.getName(), release, gitlab.getIdentifier(), uploads);
                        }
                    }
                } else {
                    if (context.isDryrun()) {
                        context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                        createRelease(api, tagName, changelog, false);
                        return;
                    }

                    throw new IllegalStateException(RB.$("ERROR_git_releaser_cannot_release",
                        "GitLab", tagName));
                }
            } else {
                context.getLogger().debug(RB.$("git.releaser.release.not.found"), tagName);
                context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                createRelease(api, tagName, changelog, snapshot);
            }
        } catch (IOException | IllegalStateException e) {
            context.getLogger().trace(e);
            throw new ReleaseException(e);
        }
    }

    @Override
    public Repository maybeCreateRepository(String owner, String repo, String password) throws IOException {
        org.jreleaser.model.Gitlab gitlab = context.getModel().getRelease().getGitlab();
        context.getLogger().debug(RB.$("git.repository.lookup"), owner, repo);

        Gitlab api = new Gitlab(context.getLogger(),
            gitlab.getApiEndpoint(),
            password,
            gitlab.getConnectTimeout(),
            gitlab.getReadTimeout());
        Project project = null;

        try {
            project = api.findProject(repo, gitlab.getIdentifier());
        } catch (RestAPIException e) {
            if (!e.isNotFound()) {
                throw e;
            }
        }

        if (null == project) {
            project = api.createProject(owner, repo);
        }

        return new Repository(
            Repository.Kind.GITLAB,
            owner,
            repo,
            project.getWebUrl(),
            project.getHttpUrlToRepo());
    }

    @Override
    public Optional<User> findUser(String email, String name) {
        org.jreleaser.model.Gitlab gitlab = context.getModel().getRelease().getGitlab();

        try {
            return new Gitlab(context.getLogger(),
                gitlab.getApiEndpoint(),
                gitlab.getResolvedToken(),
                gitlab.getConnectTimeout(),
                gitlab.getReadTimeout())
                .findUser(email, name);
        } catch (RestAPIException | IOException e) {
            context.getLogger().trace(e);
            context.getLogger().debug(RB.$("git.releaser.user.not.found"), email);
        }

        return Optional.empty();
    }

    private void createRelease(Gitlab api, String tagName, String changelog, boolean deleteTags) throws IOException {
        org.jreleaser.model.Gitlab gitlab = context.getModel().getRelease().getGitlab();

        if (context.isDryrun()) {
            for (Path asset : assets) {
                if (0 == asset.toFile().length() || !Files.exists(asset)) {
                    // do not upload empty or non existent files
                    continue;
                }

                context.getLogger().info(" " + RB.$("git.upload.asset"), asset.getFileName().toString());
            }
            return;
        }

        if (deleteTags) {
            deleteTags(api, gitlab.getOwner(), gitlab.getName(), gitlab.getIdentifier(), tagName);
        }

        // local tag
        if (deleteTags || !gitlab.isSkipTag()) {
            context.getLogger().debug(RB.$("git.releaser.repository.tag"), tagName);
            GitSdk.of(context).tag(tagName, true, context);
        }

        List<FileUpload> uploads = api.uploadAssets(gitlab.getOwner(), gitlab.getName(), gitlab.getIdentifier(), assets);

        Release release = new Release();
        release.setName(gitlab.getEffectiveReleaseName());
        release.setTagName(tagName);
        release.setRef(gitlab.getBranch());
        release.setDescription(changelog);

        // remote tag/release
        api.createRelease(gitlab.getOwner(), gitlab.getName(), gitlab.getIdentifier(), release);
        api.linkAssets(gitlab.getOwner(), gitlab.getName(), release, gitlab.getIdentifier(), uploads);

        if (gitlab.getMilestone().isClose() && !context.getModel().getProject().isSnapshot()) {
            Optional<Milestone> milestone = api.findMilestoneByName(
                gitlab.getOwner(),
                gitlab.getName(),
                gitlab.getIdentifier(),
                gitlab.getMilestone().getEffectiveName());
            if (milestone.isPresent()) {
                api.closeMilestone(gitlab.getOwner(),
                    gitlab.getName(),
                    gitlab.getIdentifier(),
                    milestone.get());
            }
        }
    }

    private void deleteTags(Gitlab api, String owner, String repo, String identifier, String tagName) {
        // delete remote tag
        try {
            api.deleteTag(owner, repo, identifier, tagName);
        } catch (RestAPIException ignored) {
            //noop
        }
    }
}
