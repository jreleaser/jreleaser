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
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.sdk.gitea.api.GtMilestone;
import org.jreleaser.sdk.gitea.api.GtRelease;
import org.jreleaser.sdk.gitea.api.GtRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.jreleaser.util.StringUtils.capitalize;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class GiteaReleaser implements Releaser {
    protected final JReleaserContext context;
    protected final List<Path> assets = new ArrayList<>();

    public GiteaReleaser(JReleaserContext context, List<Path> assets) {
        this.context = context;
        this.assets.addAll(assets);
    }

    protected org.jreleaser.model.Gitea resolveGiteaFromModel() {
        return context.getModel().getRelease().getGitea();
    }

    protected Repository.Kind resolveRepositoryKind() {
        return Repository.Kind.OTHER;
    }

    public void release() throws ReleaseException {
        org.jreleaser.model.Gitea gitea = resolveGiteaFromModel();
        context.getLogger().info("Releasing to {}", gitea.getResolvedRepoUrl(context.getModel()));
        String tagName = gitea.getEffectiveTagName(context.getModel());

        try {
            String changelog = context.getChangelog();

            Gitea api = new Gitea(context.getLogger(),
                gitea.getApiEndpoint(),
                gitea.getResolvedToken(),
                gitea.getConnectTimeout(),
                gitea.getReadTimeout());

            context.getLogger().debug("looking up release with tag {} at repository {}", tagName, gitea.getCanonicalRepoName());
            GtRelease release = api.findReleaseByTag(gitea.getOwner(), gitea.getName(), tagName);
            boolean snapshot = context.getModel().getProject().isSnapshot();
            if (null != release) {
                context.getLogger().debug("release {} exists", tagName);
                if (gitea.isOverwrite() || snapshot) {
                    context.getLogger().debug("deleting release {}", tagName);
                    if (!context.isDryrun()) {
                        api.deleteRelease(gitea.getOwner(), gitea.getName(), tagName, release.getId());
                    }
                    context.getLogger().debug("creating release {}", tagName);
                    createRelease(api, tagName, changelog, true);
                } else if (gitea.isUpdate()) {
                    context.getLogger().debug("updating release {}", tagName);
                    if (!context.isDryrun()) {
                        api.uploadAssets(gitea.getOwner(), gitea.getName(), release, assets);
                    }
                } else {
                    throw new IllegalStateException(capitalize(gitea.getServiceName()) + " release failed because release " +
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
        org.jreleaser.model.Gitea gitea = resolveGiteaFromModel();
        context.getLogger().debug("looking up {}/{}", owner, repo);

        Gitea api = new Gitea(context.getLogger(),
            gitea.getApiEndpoint(),
            password,
            gitea.getConnectTimeout(),
            gitea.getReadTimeout());
        GtRepository repository = api.findRepository(owner, repo);
        if (null == repository) {
            repository = api.createRepository(owner, repo);
        }

        return new Repository(
            resolveRepositoryKind(),
            owner,
            repo,
            repository.getHtmlUrl(),
            repository.getCloneUrl());
    }

    private void createRelease(Gitea api, String tagName, String changelog, boolean deleteTags) throws IOException {
        org.jreleaser.model.Gitea gitea = resolveGiteaFromModel();

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
            deleteTags(api, gitea.getOwner(), gitea.getName(), tagName);
        }

        // local tag
        if (deleteTags || !gitea.isSkipTag()) {
            context.getLogger().debug("tagging local repository with {}", tagName);
            GitSdk.of(context).tag(tagName, true);
        }

        // remote tag/release
        GtRelease release = new GtRelease();
        release.setName(gitea.getEffectiveReleaseName());
        release.setTagName(tagName);
        release.setTargetCommitish(gitea.getBranch());
        release.setBody(changelog);

        release = api.createRelease(gitea.getOwner(), gitea.getName(), release);
        api.uploadAssets(gitea.getOwner(), gitea.getName(), release, assets);

        if (gitea.getMilestone().isClose() && !context.getModel().getProject().isSnapshot()) {
            Optional<GtMilestone> milestone = api.findMilestoneByName(
                gitea.getOwner(),
                gitea.getName(),
                gitea.getMilestone().getEffectiveName());
            if (milestone.isPresent()) {
                api.closeMilestone(gitea.getOwner(),
                    gitea.getName(),
                    milestone.get());
            }
        }
    }

    private void deleteTags(Gitea api, String owner, String repo, String tagName) {
        // delete remote tag
        try {
            api.deleteTag(owner, repo, tagName);
        } catch (RestAPIException ignored) {
            //noop
        }
    }
}
