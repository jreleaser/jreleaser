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
package org.jreleaser.sdk.github;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.releaser.spi.AbstractReleaserBuilder;
import org.jreleaser.model.releaser.spi.ReleaseException;
import org.jreleaser.model.releaser.spi.Releaser;
import org.jreleaser.model.releaser.spi.Repository;
import org.jreleaser.sdk.git.ChangelogProvider;
import org.jreleaser.sdk.git.GitSdk;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class GithubReleaser implements Releaser {
    private final JReleaserContext context;
    private final List<Path> assets = new ArrayList<>();

    public GithubReleaser(JReleaserContext context, List<Path> assets) {
        this.context = context;
        this.assets.addAll(assets);
    }

    public void release() throws ReleaseException {
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();
        context.getLogger().info("Releasing to {}", github.getResolvedRepoUrl(context.getModel().getProject()));
        String tagName = github.getEffectiveTagName(context.getModel().getProject());

        try {
            String changelog = ChangelogProvider.getChangelog(context,
                github.getResolvedCommitUrl(context.getModel().getProject()), github.getChangelog());

            Github api = new Github(context.getLogger(), github.getApiEndpoint(), github.getResolvedToken());

            context.getLogger().debug("Looking up release with tag {} at repository {}", tagName, github.getCanonicalRepoName());
            GHRelease release = api.findReleaseByTag(github.getCanonicalRepoName(), tagName);
            if (null != release) {
                context.getLogger().debug("Release {} exists", tagName);
                if (github.isOverwrite()) {
                    context.getLogger().debug("Deleting release {}", tagName);
                    if (!context.isDryrun()) {
                        release.delete();
                    }
                    context.getLogger().debug("Creating release {}", tagName);
                    createRelease(api, tagName, changelog, context.getModel().getProject().isSnapshot());
                } else if (github.isAllowUploadToExisting()) {
                    context.getLogger().debug("Updating release {}", tagName);
                    if (!context.isDryrun()) api.uploadAssets(release, assets);
                } else {
                    throw new IllegalStateException("Github release failed because release " +
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
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();
        context.getLogger().debug("Looking up {}/{}", owner, repo);

        Github api = new Github(context.getLogger(), github.getApiEndpoint(), password);
        GHRepository repository = api.findRepository(owner, repo);
        if (null == repository) {
            repository = api.createRepository(owner, repo);
        }

        return new Repository(
            owner,
            repo,
            repository.getUrl(),
            repository.getGitTransportUrl(),
            repository.getHttpTransportUrl());
    }

    private void createRelease(Github api, String tagName, String changelog, boolean deleteTags) throws IOException {
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();

        if (context.isDryrun()) {
            for (Path asset : assets) {
                if (0 == asset.toFile().length() || !Files.exists(asset)) {
                    // do not upload empty or non existent files
                    continue;
                }

                context.getLogger().debug("Uploading asset {}", asset.getFileName().toString());
            }
            return;
        }

        if (deleteTags) {
            deleteTags(api, github.getCanonicalRepoName(), tagName);
        }

        // local tag
        if (deleteTags || !context.getModel().getRelease().getGitService().isSkipTagging()) {
            context.getLogger().debug("Tagging local repository with {}", tagName);
            GitSdk.of(context).tag(tagName, true);
        }

        // remote tag/release
        GHRelease release = api.createRelease(github.getCanonicalRepoName(),
            github.getEffectiveTagName(context.getModel().getProject()))
            .commitish(github.getTargetCommitish())
            .name(github.getResolvedReleaseName(context.getModel().getProject()))
            .draft(github.isDraft())
            .prerelease(github.isPrerelease())
            .body(changelog)
            .create();
        api.uploadAssets(release, assets);
    }

    private void deleteTags(Github api, String repo, String tagName) {
        // delete remote tag
        try {
            api.deleteTag(repo, tagName);
        } catch (IOException ignored) {
            //noop
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractReleaserBuilder<GithubReleaser, Builder> {
        @Override
        public GithubReleaser build() {
            validate();

            return new GithubReleaser(context, assets);
        }
    }
}
