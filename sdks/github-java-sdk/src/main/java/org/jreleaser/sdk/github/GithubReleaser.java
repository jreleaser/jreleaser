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

import org.jreleaser.model.Changelog;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.releaser.AbstractReleaserBuilder;
import org.jreleaser.model.releaser.ReleaseException;
import org.jreleaser.model.releaser.Releaser;
import org.jreleaser.sdk.git.ChangelogProvider;
import org.jreleaser.util.Logger;
import org.kohsuke.github.GHRelease;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class GithubReleaser implements Releaser {
    private final Logger logger;
    private final Path basedir;
    private final String repo;
    private final String commitsUrl;
    private final String authorization;
    private final String tagName;
    private final String targetCommitish;
    private final String releaseName;
    private final Changelog changelog;
    private final boolean draft;
    private final boolean prerelease;
    private final boolean overwrite;
    private final boolean allowUploadToExisting;
    private final String apiEndpoint;
    private final List<Path> assets = new ArrayList<>();

    public GithubReleaser(Path basedir, Logger logger, String repo, String commitsUrl, String authorization,
                          String tagName, String targetCommitish, String releaseName,
                          Changelog changelog, boolean draft, boolean prerelease, boolean overwrite,
                          boolean allowUploadToExisting, String apiEndpoint, List<Path> assets) {
        this.basedir = basedir;
        this.logger = logger;
        this.repo = repo;
        this.commitsUrl = commitsUrl;
        this.authorization = authorization;
        this.tagName = tagName;
        this.targetCommitish = targetCommitish;
        this.releaseName = releaseName;
        this.changelog = changelog;
        this.draft = draft;
        this.prerelease = prerelease;
        this.overwrite = overwrite;
        this.allowUploadToExisting = allowUploadToExisting;
        this.apiEndpoint = apiEndpoint;
        this.assets.addAll(assets);
    }

    public void release() throws ReleaseException {
        Github api = new Github(logger, apiEndpoint, authorization);

        try {
            logger.info("Looking up release with tag {} at repository {}", tagName, repo);
            GHRelease release = api.findReleaseByTag(repo, tagName);
            if (null != release) {
                logger.info("Release {} exists", tagName);
                if (overwrite) {
                    logger.info("Deleting release {}", tagName);
                    release.delete();
                    logger.info("Creating release {}", tagName);
                    createRelease(api);
                } else if (allowUploadToExisting) {
                    logger.info("Updating release {}", tagName);
                    api.uploadAssets(release, assets);
                } else {
                    throw new IllegalStateException("Github release failed because release " +
                        tagName + " already exists. overwrite = false; allowUploadToExisting = false");
                }
            } else {
                logger.info("Release {} does not exist", tagName);
                logger.info("Creating release {}", tagName);
                createRelease(api);
            }
        } catch (IOException | IllegalStateException e) {
            throw new ReleaseException(e);
        }
    }

    private void createRelease(Github api) throws IOException {
        GHRelease release = api.createRelease(repo, tagName)
            .commitish(targetCommitish)
            .name(releaseName)
            .draft(draft)
            .prerelease(prerelease)
            .body(ChangelogProvider.getChangelog(basedir, commitsUrl, changelog))
            .create();
        api.uploadAssets(release, assets);
    }

    public static Builder builder(Logger logger) {
        Builder builder = new Builder();
        builder.logger(logger);
        return builder;
    }

    public static class Builder extends AbstractReleaserBuilder<GithubReleaser, Builder> {
        private String targetCommitish = "main";
        private boolean draft;
        private boolean prerelease;
        private String apiEndpoint = Github.ENDPOINT;

        public Builder targetCommitish(String targetCommitish) {
            this.targetCommitish = requireNonBlank(targetCommitish, "'targetCommitish' must not be blank");
            return self();
        }

        public Builder draft(boolean draft) {
            this.draft = draft;
            return self();
        }

        public Builder prerelease(boolean prerelease) {
            this.prerelease = prerelease;
            return self();
        }

        public Builder apiEndpoint(String apiEndpoint) {
            this.apiEndpoint = isNotBlank(apiEndpoint) ? apiEndpoint : Github.ENDPOINT;
            return self();
        }

        @Override
        public GithubReleaser build() {
            validate();
            requireNonBlank(targetCommitish, "'targetCommitish' must not be blank");

            return new GithubReleaser(basedir, logger, repo, commitsUrl, authorization,
                tagName, targetCommitish, releaseName,
                changelog, draft, prerelease, overwrite,
                allowUploadToExisting, apiEndpoint, assets);
        }

        @Override
        public Builder configureWith(Path basedir, JReleaserModel model) {
            super.configureWith(basedir, model);

            org.jreleaser.model.Github github = model.getRelease().getGithub();

            targetCommitish(github.getTargetCommitish());
            draft(github.isDraft());
            prerelease(github.isPrerelease());
            apiEndpoint(github.getApiEndpoint());

            return self();
        }
    }
}
