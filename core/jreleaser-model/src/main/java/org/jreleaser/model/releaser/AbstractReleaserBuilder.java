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
package org.jreleaser.model.releaser;

import org.jreleaser.model.Changelog;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.util.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractReleaserBuilder<R extends Releaser, B extends ReleaserBuilder<R, B>> implements ReleaserBuilder<R, B> {
    protected final List<Path> assets = new ArrayList<>();
    protected Path basedir;
    protected Logger logger;
    protected String repo;
    protected String authorization;
    protected String tagName;
    protected String releaseName;
    protected Changelog changelog;
    protected boolean overwrite;
    protected boolean allowUploadToExisting;
    protected String commitsUrl;

    protected final B self() {
        return (B) this;
    }

    @Override
    public B basedir(Path basedir) {
        this.basedir = requireNonNull(basedir, "'basedir' must not be null");
        return self();
    }

    @Override
    public B logger(Logger logger) {
        this.logger = requireNonNull(logger, "'logger' must not be null");
        return self();
    }

    @Override
    public B repo(String repo) {
        this.repo = requireNonBlank(repo, "'repo' must not be blank");
        return self();
    }

    @Override
    public B authorization(String authorization) {
        this.authorization = requireNonBlank(authorization, "'authorization' must not be blank");
        return self();
    }

    @Override
    public B tagName(String tagName) {
        this.tagName = requireNonBlank(tagName, "'tagName' must not be blank");
        return self();
    }

    @Override
    public B releaseName(String releaseName) {
        this.releaseName = requireNonBlank(releaseName, "'releaseName' must not be blank");
        return self();
    }

    @Override
    public B changelog(Changelog changelog) {
        this.changelog = changelog;
        return self();
    }

    @Override
    public B overwrite(boolean overwrite) {
        this.overwrite = overwrite;
        return self();
    }

    @Override
    public B allowUploadToExisting(boolean allowUploadToExisting) {
        this.allowUploadToExisting = allowUploadToExisting;
        return self();
    }

    @Override
    public B commitsUrl(String commitsUrl) {
        this.commitsUrl = commitsUrl;
        return self();
    }

    @Override
    public B addReleaseAsset(Path asset) {
        if (null != asset) {
            this.assets.add(asset);
        }
        return self();
    }

    @Override
    public B setReleaseAssets(List<Path> assets) {
        if (null != assets) {
            this.assets.addAll(assets);
        }
        return self();
    }

    protected void validate() {
        requireNonNull(basedir, "'basedir' must not be null");
        requireNonNull(logger, "'logger' must not be null");
        requireNonBlank(repo, "'repo' must not be blank");
        requireNonBlank(commitsUrl, "'commitsUrl' must not be blank");
        requireNonBlank(authorization, "'authorization' must not be blank");
        requireNonBlank(tagName, "'tagName' must not be blank");
        requireNonBlank(releaseName, "'releaseName' must not be blank");
        requireNonNull(changelog, "'changelog' must not be null");
        if (assets.isEmpty()) {
            throw new IllegalArgumentException("'assets must not be empty");
        }
    }

    @Override
    public B configureWith(Path basedir, JReleaserModel model) {
        org.jreleaser.model.GitService gitService = model.getRelease().getGitService();

        basedir(basedir);
        repo(gitService.getCanonicalRepo());
        commitsUrl(gitService.getCommitUrlFormat());
        authorization(gitService.getAuthorization());
        tagName(gitService.getTagName());
        releaseName("Release " + model.getProject().getVersion());
        overwrite(gitService.isOverwrite());
        allowUploadToExisting(gitService.isAllowUploadToExisting());
        changelog(gitService.getChangelog());
        model.getDistributions().values().forEach(distribution -> {
            distribution.getArtifacts().forEach(artifact -> {
                addReleaseAsset(basedir.resolve(Paths.get(artifact.getPath())));
            });
        });

        return self();
    }
}