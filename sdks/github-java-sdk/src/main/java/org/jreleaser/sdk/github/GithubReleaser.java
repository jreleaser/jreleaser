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
package org.jreleaser.sdk.github;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.UpdateSection;
import org.jreleaser.model.releaser.spi.ReleaseException;
import org.jreleaser.model.releaser.spi.Releaser;
import org.jreleaser.model.releaser.spi.Repository;
import org.jreleaser.sdk.git.GitSdk;
import org.kohsuke.github.GHMilestone;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHReleaseUpdater;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class GithubReleaser implements Releaser {
    private final JReleaserContext context;
    private final List<Path> assets = new ArrayList<>();

    GithubReleaser(JReleaserContext context, List<Path> assets) {
        this.context = context;
        this.assets.addAll(assets);
    }

    public void release() throws ReleaseException {
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();
        context.getLogger().info("Releasing to {}", github.getResolvedRepoUrl(context.getModel()));
        String tagName = github.getEffectiveTagName(context.getModel());

        try {
            String changelog = context.getChangelog();

            Github api = new Github(context.getLogger(),
                github.getUsername(),
                github.getResolvedToken(),
                github.getConnectTimeout(),
                github.getReadTimeout());

            context.getLogger().debug("looking up release with tag {} at repository {}", tagName, github.getCanonicalRepoName());
            GHRelease release = api.findReleaseByTag(github.getCanonicalRepoName(), tagName);
            boolean snapshot = context.getModel().getProject().isSnapshot();
            if (null != release) {
                context.getLogger().debug("release {} exists", tagName);
                if (github.isOverwrite() || snapshot) {
                    context.getLogger().debug("deleting release {}", tagName);
                    if (!context.isDryrun()) {
                        release.delete();
                    }
                    context.getLogger().debug("creating release {}", tagName);
                    createRelease(api, tagName, changelog, true);
                } else if (github.isUpdate()) {
                    context.getLogger().debug("updating release {}", tagName);
                    if (!context.isDryrun()) {
                        boolean update = false;
                        GHReleaseUpdater updater = release.update();
                        if (github.getUpdateSections().contains(UpdateSection.TITLE)) {
                            update = true;
                            context.getLogger().info("updating release title to {}", github.getEffectiveReleaseName());
                            updater.name(github.getEffectiveReleaseName());
                        }
                        if (github.getUpdateSections().contains(UpdateSection.BODY)) {
                            update = true;
                            context.getLogger().info("updating release body");
                            updater.body(changelog);
                        }
                        if (update) updater.update();

                        if (github.getUpdateSections().contains(UpdateSection.ASSETS)) {
                            api.uploadAssets(release, assets);
                        }
                    }
                } else {
                    throw new IllegalStateException("Github release failed because release " +
                        tagName + " already exists. overwrite = false; update = false");
                }
            } else {
                context.getLogger().debug("release {} does not exist", tagName);
                context.getLogger().debug("creating release {}", tagName);
                createRelease(api, tagName, changelog, snapshot);
            }
        } catch (IOException | IllegalStateException e) {
            context.getLogger().trace(e);
            throw new ReleaseException(e);
        }
    }

    @Override
    public Repository maybeCreateRepository(String owner, String repo, String password) throws IOException {
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();
        context.getLogger().debug("looking up {}/{}", owner, repo);

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

    private void createRelease(Github api, String tagName, String changelog, boolean deleteTags) throws IOException {
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();

        if (context.isDryrun()) {
            for (Path asset : assets) {
                if (0 == asset.toFile().length() || !Files.exists(asset)) {
                    // do not upload empty or non existent files
                    continue;
                }

                context.getLogger().info(" - uploading {}", asset.getFileName().toString());
            }
            return;
        }

        if (deleteTags) {
            deleteTags(api, github.getCanonicalRepoName(), tagName);
        }

        // local tag
        if (deleteTags || !github.isSkipTag()) {
            context.getLogger().debug("tagging local repository with {}", tagName);
            GitSdk.of(context).tag(tagName, true, context);
        }

        // remote tag/release
        GHRelease release = api.createRelease(github.getCanonicalRepoName(), tagName)
            .commitish(github.getBranch())
            .name(github.getEffectiveReleaseName())
            .draft(github.isDraft())
            .prerelease(github.isPrerelease())
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
