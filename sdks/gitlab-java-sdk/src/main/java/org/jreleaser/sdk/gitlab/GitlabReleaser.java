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

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.releaser.spi.ReleaseException;
import org.jreleaser.model.releaser.spi.Releaser;
import org.jreleaser.model.releaser.spi.Repository;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.sdk.gitlab.api.FileUpload;
import org.jreleaser.sdk.gitlab.api.GitlabAPIException;
import org.jreleaser.sdk.gitlab.api.Project;
import org.jreleaser.sdk.gitlab.api.Release;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class GitlabReleaser implements Releaser {
    private final JReleaserContext context;
    private final List<Path> assets = new ArrayList<>();

    GitlabReleaser(JReleaserContext context, List<Path> assets) {
        this.context = context;
        this.assets.addAll(assets);
    }

    public void release() throws ReleaseException {
        org.jreleaser.model.Gitlab gitlab = context.getModel().getRelease().getGitlab();
        context.getLogger().info("Releasing to {}", gitlab.getResolvedRepoUrl(context.getModel().getProject()));
        String tagName = gitlab.getEffectiveTagName(context.getModel().getProject());

        try {
            String changelog = context.getChangelog();

            Gitlab api = new Gitlab(context.getLogger(), gitlab.getApiEndpoint(), gitlab.getResolvedToken());

            context.getLogger().debug("Looking up release with tag {} at repository {}", tagName, gitlab.getCanonicalRepoName());
            Release release = api.findReleaseByTag(gitlab.getOwner(), gitlab.getName(), tagName);
            if (null != release) {
                context.getLogger().debug("Release {} exists", tagName);
                if (gitlab.isOverwrite()) {
                    context.getLogger().debug("Deleting release {}", tagName);
                    if (!context.isDryrun()) {
                        api.deleteRelease(gitlab.getOwner(), gitlab.getName(), tagName);
                    }
                    context.getLogger().debug("Creating release {}", tagName);
                    createRelease(api, tagName, changelog, context.getModel().getProject().isSnapshot());
                } else if (gitlab.isAllowUploadToExisting()) {
                    context.getLogger().debug("Updating release {}", tagName);
                    if (!context.isDryrun()) {
                        List<FileUpload> uploads = api.uploadAssets(gitlab.getOwner(), gitlab.getName(), assets);
                        api.linkAssets(gitlab.getOwner(), gitlab.getName(), release, uploads);
                    }
                } else {
                    throw new IllegalStateException("Gitlab release failed because release " +
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
        org.jreleaser.model.Gitlab gitlab = context.getModel().getRelease().getGitlab();
        context.getLogger().debug("Looking up {}/{}", owner, repo);

        Gitlab api = new Gitlab(context.getLogger(), gitlab.getApiEndpoint(), password);
        Project project = null;

        try {
            project = api.findProject(repo);
        } catch (GitlabAPIException e) {
            if (!e.isNotFound()) {
                throw e;
            }
        }

        if (null == project) {
            project = api.createProject(owner, repo);
        }

        return new Repository(
            owner,
            repo,
            project.getWebUrl(),
            project.getHttpUrlToRepo());
    }

    private void createRelease(Gitlab api, String tagName, String changelog, boolean deleteTags) throws IOException {
        org.jreleaser.model.Gitlab gitlab = context.getModel().getRelease().getGitlab();

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
            deleteTags(api, gitlab.getOwner(), gitlab.getName(), tagName);
        }

        // local tag
        if (deleteTags || !context.getModel().getRelease().getGitService().isSkipTagging()) {
            context.getLogger().debug("Tagging local repository with {}", tagName);
            GitSdk.of(context).tag(tagName, true);
        }


        List<FileUpload> uploads = api.uploadAssets(gitlab.getOwner(), gitlab.getName(), assets);

        Release release = new Release();
        release.setName(gitlab.getResolvedReleaseName(context.getModel().getProject()));
        release.setTagName(gitlab.getEffectiveTagName(context.getModel().getProject()));
        release.setRef(gitlab.getRef());
        release.setDescription(changelog);

        // remote tag/release
        api.createRelease(gitlab.getOwner(), gitlab.getName(), release);
        api.linkAssets(gitlab.getOwner(), gitlab.getName(), release, uploads);
    }

    private void deleteTags(Gitlab api, String owner, String repo, String tagName) {
        // delete remote tag
        try {
            api.deleteTag(owner, repo, tagName);
        } catch (GitlabAPIException ignored) {
            //noop
        }
    }
}
