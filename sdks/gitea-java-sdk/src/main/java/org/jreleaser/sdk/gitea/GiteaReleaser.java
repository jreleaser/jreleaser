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
package org.jreleaser.sdk.gitea;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.releaser.spi.ReleaseException;
import org.jreleaser.model.releaser.spi.Releaser;
import org.jreleaser.model.releaser.spi.Repository;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.sdk.gitea.api.GiteaAPIException;
import org.jreleaser.sdk.gitea.api.GtRelease;
import org.jreleaser.sdk.gitea.api.GtRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class GiteaReleaser implements Releaser {
    private final JReleaserContext context;
    private final List<Path> assets = new ArrayList<>();

    GiteaReleaser(JReleaserContext context, List<Path> assets) {
        this.context = context;
        this.assets.addAll(assets);
    }

    public void release() throws ReleaseException {
        org.jreleaser.model.Gitea gitea = context.getModel().getRelease().getGitea();
        context.getLogger().info("Releasing to {}", gitea.getResolvedRepoUrl(context.getModel().getProject()));
        String tagName = gitea.getEffectiveTagName(context.getModel().getProject());

        try {
            String changelog = context.getChangelog();

            Gitea api = new Gitea(context.getLogger(), gitea.getApiEndpoint(), gitea.getResolvedToken());

            context.getLogger().debug("Looking up release with tag {} at repository {}", tagName, gitea.getCanonicalRepoName());
            GtRelease release = api.findReleaseByTag(gitea.getOwner(), gitea.getName(), tagName);
            if (null != release) {
                context.getLogger().debug("Release {} exists", tagName);
                if (gitea.isOverwrite()) {
                    context.getLogger().debug("Deleting release {}", tagName);
                    if (!context.isDryrun()) {
                        api.deleteRelease(gitea.getOwner(), gitea.getName(), tagName, release.getId());
                    }
                    context.getLogger().debug("Creating release {}", tagName);
                    createRelease(api, tagName, changelog, context.getModel().getProject().isSnapshot());
                } else if (gitea.isAllowUploadToExisting()) {
                    context.getLogger().debug("Updating release {}", tagName);
                    if (!context.isDryrun()) {
                        api.uploadAssets(gitea.getOwner(), gitea.getName(), release, assets);
                    }
                } else {
                    throw new IllegalStateException("Gitea release failed because release " +
                        tagName + " already exists. overwrite = false; allowUploadToExisting = false");
                }
            } else {
                context.getLogger().debug("Release {} does not exist", tagName);
                context.getLogger().debug("Creating release {}", tagName);
                createRelease(api, tagName, changelog, context.getModel().getProject().isSnapshot());
            }
        } catch (IOException | IllegalStateException e) {
            throw new ReleaseException(e);
        }
    }

    @Override
    public Repository maybeCreateRepository(String owner, String repo, String password) throws IOException {
        org.jreleaser.model.Gitea gitea = context.getModel().getRelease().getGitea();
        context.getLogger().debug("Looking up {}/{}", owner, repo);

        Gitea api = new Gitea(context.getLogger(), gitea.getApiEndpoint(), password);
        GtRepository repository = api.findRepository(owner, repo);
        if (null == repository) {
            repository = api.createRepository(owner, repo);
        }

        return new Repository(
            owner,
            repo,
            repository.getHtmlUrl(),
            repository.getCloneUrl());
    }

    private void createRelease(Gitea api, String tagName, String changelog, boolean deleteTags) throws IOException {
        org.jreleaser.model.Gitea gitea = context.getModel().getRelease().getGitea();

        if (context.isDryrun()) {
            for (Path asset : assets) {
                if (0 == asset.toFile().length() || !Files.exists(asset)) {
                    // do not upload empty or non existent files
                    continue;
                }

                context.getLogger().debug(" - Uploading asset {}", asset.getFileName().toString());
            }
            return;
        }

        if (deleteTags) {
            deleteTags(api, gitea.getOwner(), gitea.getName(), tagName);
        }

        // local tag
        if (deleteTags || !context.getModel().getRelease().getGitService().isSkipTagging()) {
            context.getLogger().debug("Tagging local repository with {}", tagName);
            GitSdk.of(context).tag(tagName, true);
        }

        // remote tag/release
        GtRelease release = new GtRelease();
        release.setName(gitea.getResolvedReleaseName(context.getModel().getProject()));
        release.setTagName(gitea.getEffectiveTagName(context.getModel().getProject()));
        release.setTargetCommitish(gitea.getTargetCommitish());
        release.setBody(changelog);

        release = api.createRelease(gitea.getOwner(), gitea.getName(), release);
        api.uploadAssets(gitea.getOwner(), gitea.getName(), release, assets);
    }

    private void deleteTags(Gitea api, String owner, String repo, String tagName) {
        // delete remote tag
        try {
            api.deleteTag(owner, repo, tagName);
        } catch (GiteaAPIException ignored) {
            //noop
        }
    }
}
