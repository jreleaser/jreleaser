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
import org.jreleaser.sdk.git.ChangelogProvider;
import org.kohsuke.github.GHRelease;

import java.io.IOException;
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
        context.getLogger().info("Releasing to {}", github.getResolvedRepoUrl());

        Github api = new Github(context.getLogger(), github.getApiEndpoint(), github.getResolvedAuthorization());

        String tagName = github.getTagName();

        try {
            context.getLogger().info("Looking up release with tag {} at repository {}", tagName, github.getCanonicalRepoName());
            GHRelease release = api.findReleaseByTag(github.getCanonicalRepoName(), tagName);
            if (null != release) {
                context.getLogger().info("Release {} exists", tagName);
                if (github.isOverwrite()) {
                    context.getLogger().info("Deleting release {}", tagName);
                    if (!context.isDryrun()) release.delete();
                    context.getLogger().info("Creating release {}", tagName);
                    createRelease(api, context.isDryrun());
                } else if (github.isAllowUploadToExisting()) {
                    context.getLogger().info("Updating release {}", tagName);
                    if (!context.isDryrun()) api.uploadAssets(release, assets);
                } else {
                    throw new IllegalStateException("Github release failed because release " +
                        tagName + " already exists. overwrite = false; allowUploadToExisting = false");
                }
            } else {
                context.getLogger().info("Release {} does not exist", tagName);
                context.getLogger().info("Creating release {}", tagName);
                createRelease(api, context.isDryrun());
            }
        } catch (IOException | IllegalStateException e) {
            throw new ReleaseException(e);
        }
    }

    private void createRelease(Github api, boolean dryrun) throws IOException {
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();

        String changelog = ChangelogProvider.getChangelog(context, github.getResolvedCommitUrl(), github.getChangelog());
        context.getLogger().info("changelog:{}{}", System.lineSeparator(), changelog);
        if (dryrun) {
            for (Path asset : assets) {
                if (0 == asset.toFile().length()) {
                    // do not upload empty files
                    continue;
                }

                context.getLogger().debug("Uploading asset {}", asset.getFileName().toString());
            }
            return;
        }

        GHRelease release = api.createRelease(github.getCanonicalRepoName(), github.getTagName())
            .commitish(github.getTargetCommitish())
            .name(github.getReleaseName())
            .draft(github.isDraft())
            .prerelease(github.isPrerelease())
            .body(changelog)
            .create();
        api.uploadAssets(release, assets);
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
