/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
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
package org.jreleaser.sdk.git.release;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.spi.release.ReleaseException;
import org.jreleaser.model.spi.release.Repository;
import org.jreleaser.sdk.git.GitSdk;

import java.io.IOException;

/**
 * @author Andres Almiray
 * @since 0.7.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public final class ReleaseUtils {
    private ReleaseUtils() {
        // noop
    }

    public static void createTag(JReleaserContext context) throws ReleaseException {
        BaseReleaser<?, ?> service = context.getModel().getRelease().getReleaser();

        try {
            GitSdk git = GitSdk.of(context);
            Repository repository = git.getRemote();

            context.getLogger().info(RB.$("git.tag"), repository.getHttpUrl());
            String tagName = service.getEffectiveTagName(context.getModel());

            context.getLogger().debug(RB.$("git.tag.lookup"), tagName);
            boolean tagged = git.findTag(tagName);
            boolean snapshot = context.getModel().getProject().isSnapshot();
            if (tagged) {
                context.getLogger().debug(RB.$("git.tag.exists"), tagName);
                if (service.isOverwrite() || snapshot) {
                    context.getLogger().debug(RB.$("git.tag.release"), tagName);
                    tagRelease(context, repository, tagName);
                } else if (!context.isDryrun()) {
                    throw new IllegalStateException(RB.$("ERROR_git_release_existing_tag", tagName));
                }
            } else {
                context.getLogger().debug(RB.$("git.tag.not.exist"), tagName);
                context.getLogger().debug(RB.$("git.tag.release"), tagName);
                tagRelease(context, repository, tagName);
            }
        } catch (IOException | IllegalStateException e) {
            context.getLogger().trace(e);
            throw new ReleaseException(e);
        }
    }

    private static void tagRelease(JReleaserContext context, Repository repository, String tagName) throws ReleaseException {
        try {
            GitSdk gitSdk = GitSdk.of(context);
            gitSdk.tag(tagName, true, context);

            context.getLogger().info(RB.$("git.push.release"), repository.getHttpUrl());
            context.getLogger().debug(RB.$("git.push.tag"), context.isDryrun());

            UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                context.getModel().getRelease().getReleaser().getUsername(),
                context.getModel().getRelease().getReleaser().getToken());

            try (Git git = gitSdk.open()) {
                git.push()
                    .setDryRun(context.isDryrun())
                    .setPushTags()
                    .setCredentialsProvider(credentialsProvider)
                    .call();
            }
        } catch (Exception e) {
            context.getLogger().trace(e);
            throw new ReleaseException(e);
        }
    }
}
