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
package org.jreleaser.sdk.generic.git;

import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.releaser.spi.ReleaseException;
import org.jreleaser.model.releaser.spi.Releaser;
import org.jreleaser.model.releaser.spi.Repository;
import org.jreleaser.sdk.git.GitSdk;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public class GenericGitReleaser implements Releaser {
    private final JReleaserContext context;

    GenericGitReleaser(JReleaserContext context, List<Path> assets) {
        this.context = context;
    }

    public void release() throws ReleaseException {
        try {
            GitSdk git = GitSdk.of(context);
            Repository repository = git.getRemote();

            org.jreleaser.model.GenericGit generic = context.getModel().getRelease().getGeneric();
            context.getLogger().info("Releasing to a generic Git repository is not supported");
            context.getLogger().info("Tagging {}", repository.getHttpUrl());
            String tagName = generic.getEffectiveTagName();

            context.getLogger().debug("looking up tag {}", tagName);
            boolean tagged = git.findTag(tagName);
            boolean snapshot = context.getModel().getProject().isSnapshot();
            if (tagged) {
                context.getLogger().debug("tag {} exists", tagName);
                if (generic.isOverwrite() || snapshot) {
                    context.getLogger().debug("tagging release {}", tagName);
                    tagRelease(repository, tagName);
                } else if (!context.isDryrun()) {
                    throw new IllegalStateException("Generic release failed because tag " +
                        tagName + " already exists. overwrite = false");
                }
            } else {
                context.getLogger().debug("tag {} does not exist", tagName);
                context.getLogger().debug("tagging release {}", tagName);
                tagRelease(repository, tagName);
            }
        } catch (IOException | IllegalStateException e) {
            context.getLogger().trace(e);
            throw new ReleaseException(e);
        }
    }

    private void tagRelease(Repository repository, String tagName) throws ReleaseException {
        try {
            GitSdk gitSdk = GitSdk.of(context);
            gitSdk.tag(tagName, true, context);

            context.getLogger().info("pushing to {}", repository.getHttpUrl());
            context.getLogger().debug("pushing tag to remote, dryrun = {}", context.isDryrun());

            UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                context.getModel().getRelease().getGitService().getResolvedUsername(),
                context.getModel().getRelease().getGitService().getResolvedToken());

            gitSdk.open().push()
                .setDryRun(context.isDryrun())
                .setPushTags()
                .setCredentialsProvider(credentialsProvider)
                .call();
        } catch (Exception e) {
            context.getLogger().trace(e);
            throw new ReleaseException(e);
        }
    }

    @Override
    public Repository maybeCreateRepository(String owner, String repo, String password) throws IOException {
        return null;
    }
}
