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

import org.kohsuke.github.GHRelease;
import org.jreleaser.model.Changelog;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.releaser.ReleaseException;
import org.jreleaser.model.releaser.Releaser;
import org.jreleaser.model.releaser.ReleaserBuilder;
import org.jreleaser.sdk.git.ChangelogProvider;
import org.jreleaser.util.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
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

    public GithubReleaser(Path basedir, Logger logger, String repo, String authorization,
                          String tagName, String targetCommitish, String releaseName,
                          Changelog changelog, boolean draft, boolean prerelease, boolean overwrite,
                          boolean allowUploadToExisting, String apiEndpoint, List<Path> assets) {
        this.basedir = basedir;
        this.logger = logger;
        this.repo = repo;
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
            GHRelease release = api.findReleaseByTag(repo, tagName);
            if (null != release) {
                if (overwrite) {
                    release.delete();
                    createRelease(api);
                } else if (allowUploadToExisting) {
                    api.uploadAssets(release, assets);
                } else {
                    throw new IllegalStateException("Github release failed because release " +
                        tagName + " already exists");
                }
            } else {
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
            .body(ChangelogProvider.getChangelog(basedir, changelog))
            .create();
        api.uploadAssets(release, assets);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements ReleaserBuilder<GithubReleaser> {
        private final List<Path> assets = new ArrayList<>();
        private Path basedir;
        private Logger logger;
        private String repo;
        private String authorization;
        private String tagName;
        private String targetCommitish = "main";
        private String releaseName;
        private Changelog changelog;
        private boolean draft;
        private boolean prerelease;
        private boolean overwrite;
        private boolean allowUploadToExisting;
        private String apiEndpoint = Github.ENDPOINT;

        public Builder basedir(Path basedir) {
            this.basedir = requireNonNull(basedir, "'basedir' must not be null");
            return this;
        }

        public Builder logger(Logger logger) {
            this.logger = requireNonNull(logger, "'logger' must not be null");
            return this;
        }

        public Builder repo(String repo) {
            this.repo = requireNonBlank(repo, "'repo' must not be blank");
            return this;
        }

        public Builder authorization(String authorization) {
            this.authorization = requireNonBlank(authorization, "'authorization' must not be blank");
            return this;
        }

        public Builder tagName(String tagName) {
            this.tagName = requireNonBlank(tagName, "'tagName' must not be blank");
            return this;
        }

        public Builder targetCommitish(String targetCommitish) {
            this.targetCommitish = requireNonBlank(targetCommitish, "'targetCommitish' must not be blank");
            return this;
        }

        public Builder releaseName(String releaseName) {
            this.releaseName = requireNonBlank(releaseName, "'releaseName' must not be blank");
            return this;
        }

        public Builder changelog(Changelog changelog) {
            this.changelog = changelog;
            return this;
        }

        public Builder draft(boolean draft) {
            this.draft = draft;
            return this;
        }

        public Builder prerelease(boolean prerelease) {
            this.prerelease = prerelease;
            return this;
        }

        public Builder overwrite(boolean overwrite) {
            this.overwrite = overwrite;
            return this;
        }

        public Builder allowUploadToExisting(boolean allowUploadToExisting) {
            this.allowUploadToExisting = allowUploadToExisting;
            return this;
        }

        public Builder apiEndpoint(String apiEndpoint) {
            this.apiEndpoint = isNotBlank(apiEndpoint) ? apiEndpoint : Github.ENDPOINT;
            return this;
        }

        public Builder setReleaseAssets(List<Path> assets) {
            if (null != assets) {
                this.assets.addAll(assets);
            }
            return this;
        }

        @Override
        public GithubReleaser build() {
            requireNonNull(basedir, "'basedir' must not be null");
            requireNonNull(logger, "'logger' must not be null");
            requireNonBlank(repo, "'repo' must not be blank");
            requireNonBlank(authorization, "'authorization' must not be blank");
            requireNonBlank(tagName, "'tagName' must not be blank");
            requireNonBlank(targetCommitish, "'targetCommitish' must not be blank");
            requireNonBlank(releaseName, "'releaseName' must not be blank");
            if (assets.isEmpty()) {
                throw new IllegalArgumentException("'assets must not be empty");
            }

            return new GithubReleaser(basedir, logger, repo, authorization,
                tagName, targetCommitish, releaseName,
                changelog, draft, prerelease, overwrite,
                allowUploadToExisting, apiEndpoint, assets);
        }

        @Override
        public GithubReleaser buildFromModel(Path basedir, JReleaserModel model) {
            basedir(basedir);
            repo(model.getRelease().getGithub().getRepoName());
            authorization(model.getRelease().getGithub().getAuthorization());
            tagName(model.getRelease().getGithub().getTagName());
            targetCommitish(model.getRelease().getGithub().getTargetCommitish());
            releaseName(model.getRelease().getGithub().getRepoName());
            draft(model.getRelease().getGithub().isDraft());
            prerelease(model.getRelease().getGithub().isPrerelease());
            overwrite(model.getRelease().getGithub().isOverwrite());
            allowUploadToExisting(model.getRelease().getGithub().isAllowUploadToExisting());
            apiEndpoint(model.getRelease().getGithub().getApiEndpoint());
            changelog(model.getRelease().getGithub().getChangelog());
            return build();
        }
    }
}
