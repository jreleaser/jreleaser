/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.sdk.git;

import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.releaser.spi.ReleaseException;
import org.jreleaser.model.releaser.spi.Repository;

import java.io.IOException;

/**
 * @author Andres Almiray
 * @since 0.7.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public final class ReleaseUtils {
    private ReleaseUtils() {

    }

    public static void createTag(JReleaserContext context) throws ReleaseException {
        org.jreleaser.model.GitService service = context.getModel().getRelease().getGitService();

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
}
