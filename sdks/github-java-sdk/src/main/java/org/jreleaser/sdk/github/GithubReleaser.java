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

import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.releaser.spi.AbstractReleaserBuilder;
import org.jreleaser.model.releaser.spi.ReleaseException;
import org.jreleaser.model.releaser.spi.Releaser;
import org.jreleaser.sdk.git.ChangelogProvider;
import org.jreleaser.util.Logger;
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
    private final Logger logger;
    private final Path basedir;
    private final JReleaserModel model;
    private final List<Path> assets = new ArrayList<>();

    public GithubReleaser(Path basedir, Logger logger, JReleaserModel model, List<Path> assets) {
        this.basedir = basedir;
        this.logger = logger;
        this.model = model;
        this.assets.addAll(assets);
    }

    public void release(boolean dryrun) throws ReleaseException {
        org.jreleaser.model.Github github = model.getRelease().getGithub();

        Github api = new Github(logger, github.getApiEndpoint(), github.getResolvedAuthorization());

        String tagName = github.getTagName();

        try {
            logger.info("Looking up release with tag {} at repository {}", tagName, github.getCanonicalRepoName());
            GHRelease release = api.findReleaseByTag(github.getCanonicalRepoName(), tagName);
            if (null != release) {
                logger.info("Release {} exists", tagName);
                if (github.isOverwrite()) {
                    logger.info("Deleting release {}", tagName);
                    if (!dryrun) release.delete();
                    logger.info("Creating release {}", tagName);
                    createRelease(api, dryrun);
                } else if (github.isAllowUploadToExisting()) {
                    logger.info("Updating release {}", tagName);
                    if (!dryrun) api.uploadAssets(release, assets);
                } else {
                    throw new IllegalStateException("Github release failed because release " +
                        tagName + " already exists. overwrite = false; allowUploadToExisting = false");
                }
            } else {
                logger.info("Release {} does not exist", tagName);
                logger.info("Creating release {}", tagName);
                createRelease(api, dryrun);
            }
        } catch (IOException | IllegalStateException e) {
            throw new ReleaseException(e);
        }
    }

    private void createRelease(Github api, boolean dryrun) throws IOException {
        org.jreleaser.model.Github github = model.getRelease().getGithub();

        String changelog = ChangelogProvider.getChangelog(basedir, github.getResolvedCommitUrl(), github.getChangelog());
        logger.info("changelog:{}{}", System.lineSeparator(), changelog);
        if (dryrun) return;

        GHRelease release = api.createRelease(github.getCanonicalRepoName(), github.getTagName())
            .commitish(github.getTargetCommitish())
            .name(github.getReleaseName())
            .draft(github.isDraft())
            .prerelease(github.isPrerelease())
            .body(changelog)
            .create();
        api.uploadAssets(release, assets);
    }

    public static Builder builder(Logger logger) {
        Builder builder = new Builder();
        builder.logger(logger);
        return builder;
    }

    public static class Builder extends AbstractReleaserBuilder<GithubReleaser, Builder> {
        @Override
        public GithubReleaser build() {
            validate();

            return new GithubReleaser(basedir, logger, model, assets);
        }
    }
}
