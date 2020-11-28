/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Andres Almiray.
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
package org.kordamp.jreleaser.sdk.gitlab;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Release;
import org.gitlab4j.api.models.ReleaseParams;
import org.kordamp.jreleaser.model.JReleaserModel;
import org.kordamp.jreleaser.model.releaser.ReleaseException;
import org.kordamp.jreleaser.model.releaser.Releaser;
import org.kordamp.jreleaser.model.releaser.ReleaserBuilder;
import org.kordamp.jreleaser.util.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.kordamp.jreleaser.util.StringUtils.isNotBlank;
import static org.kordamp.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class GitlabReleaser implements Releaser {
    private final Logger logger;
    private final String repo;
    private final String authorization;
    private final String tagName;
    private final String ref;
    private final String body;
    private final String releaseName;
    private final boolean overwrite;
    private final boolean allowUploadToExisting;
    private final String apiEndpoint;
    private final List<Path> assets = new ArrayList<>();

    public GitlabReleaser(Logger logger, String repo, String authorization,
                          String tagName, String ref, String releaseName,
                          String body, boolean overwrite,
                          boolean allowUploadToExisting, String apiEndpoint, List<Path> assets) {
        this.logger = logger;
        this.repo = repo;
        this.authorization = authorization;
        this.tagName = tagName;
        this.ref = ref;
        this.releaseName = releaseName;
        this.body = body;
        this.overwrite = overwrite;
        this.allowUploadToExisting = allowUploadToExisting;
        this.apiEndpoint = apiEndpoint;
        this.assets.addAll(assets);
    }

    public void release() throws ReleaseException {
        Gitlab api = new Gitlab(logger, apiEndpoint, authorization);

        try {
            Release release = api.findReleaseByTag(repo, tagName);
            if (null != release) {
                if (overwrite) {
                    api.deleteRelease(repo, tagName);
                    createRelease(api);
                } else if (allowUploadToExisting) {
                    api.uploadAssets(repo, release, assets);
                } else {
                    throw new IllegalStateException("Gitlab release failed because release " +
                        tagName + " already exists");
                }
            } else {
                createRelease(api);
            }
        } catch (GitLabApiException | IOException | IllegalStateException e) {
            throw new ReleaseException(e);
        }
    }

    private void createRelease(Gitlab api) throws GitLabApiException, IOException {
        ReleaseParams params = new ReleaseParams();
        params.setName(releaseName);
        params.setTagName(tagName);
        params.setRef(ref);
        Release release = api.createRelease(repo, params);
        api.uploadAssets(repo, release, assets);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements ReleaserBuilder<GitlabReleaser> {
        private final List<Path> assets = new ArrayList<>();
        private Logger logger;
        private String repo;
        private String authorization;
        private String tagName;
        private String ref = "main";
        private String releaseName;
        private String body = "";
        private boolean overwrite;
        private boolean allowUploadToExisting;
        private String apiEndpoint = Gitlab.ENDPOINT;

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

        public Builder ref(String ref) {
            this.ref = requireNonBlank(ref, "'ref' must not be blank");
            return this;
        }

        public Builder releaseName(String releaseName) {
            this.releaseName = requireNonBlank(releaseName, "'releaseName' must not be blank");
            return this;
        }

        public Builder body(String body) {
            this.body = body;
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
            this.apiEndpoint = isNotBlank(apiEndpoint) ? apiEndpoint : Gitlab.ENDPOINT;
            return this;
        }

        public Builder setReleaseAssets(List<Path> assets) {
            if (null != assets) {
                this.assets.addAll(assets);
            }
            return this;
        }

        @Override
        public GitlabReleaser build() {
            requireNonNull(logger, "'logger' must not be null");
            requireNonBlank(repo, "'repo' must not be blank");
            requireNonBlank(authorization, "'authorization' must not be blank");
            requireNonBlank(tagName, "'tagName' must not be blank");
            requireNonBlank(ref, "'ref' must not be blank");
            requireNonBlank(releaseName, "'releaseName' must not be blank");
            if (assets.isEmpty()) {
                throw new IllegalArgumentException("'assets must not be empty");
            }

            return new GitlabReleaser(logger, repo, authorization,
                tagName, ref, releaseName,
                body, overwrite,
                allowUploadToExisting, apiEndpoint, assets);
        }

        @Override
        public GitlabReleaser buildFromModel(JReleaserModel model) {
            repo(model.getRelease().getRepoName());
            authorization(model.getRelease().getAuthorization());
            tagName(model.getRelease().getTagName());
            ref(model.getRelease().getTargetCommitish());
            releaseName(model.getRelease().getRepoName());
            body(model.getRelease().getBody());
            overwrite(model.getRelease().isOverwrite());
            allowUploadToExisting(model.getRelease().isAllowUploadToExisting());
            apiEndpoint(model.getRelease().getApiEndpoint());
            return build();
        }
    }
}
