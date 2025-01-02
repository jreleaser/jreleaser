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
package org.jreleaser.packagers;

import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.RepositoryPackager;
import org.jreleaser.model.internal.packagers.RepositoryTap;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;
import org.jreleaser.model.spi.release.Repository;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.git.JReleaserGpgSigner;
import org.jreleaser.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_PACKAGE_DIRECTORY;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractRepositoryPackagerProcessor<T extends RepositoryPackager<?>> extends AbstractTemplatePackagerProcessor<T> {
    protected AbstractRepositoryPackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPublishDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        RepositoryTap tap = packager.getRepositoryTap();
        if (!tap.isEnabled()) {
            context.getLogger().info(RB.$("repository.disabled"), tap.getCanonicalRepoName());
            return;
        }

        context.getLogger().info(RB.$("repository.setup"), tap.getCanonicalRepoName());
        if (context.isDryrun()) {
            return;
        }

        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();
        String target = tap.getCanonicalRepoName();

        try {
            // get the repository
            context.getLogger().debug(RB.$("repository.locate"), tap.getCanonicalRepoName());
            Repository repository = context.getReleaser().maybeCreateRepository(
                tap.getOwner(),
                tap.getResolvedName(),
                resolveGitToken(releaser),
                tap.asImmutable());

            UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                resolveGitUsername(releaser),
                resolveGitToken(releaser));

            // clone the repository
            target = repository.getHttpUrl();
            context.getLogger().debug(RB.$("repository.clone"), repository.getHttpUrl());
            Path directory = Files.createTempDirectory("jreleaser-" + tap.getResolvedName());

            String pullBranch = tap.getBranch();
            String pushBranch = resolveTemplate(tap.getBranchPush(), props);

            Git git = Git.cloneRepository()
                .setCredentialsProvider(credentialsProvider)
                .setBranch(pullBranch)
                .setDirectory(directory.toFile())
                .setURI(repository.getHttpUrl())
                .call();

            boolean emptyRepository = true;
            try {
                for (RevCommit commit : git.log().call()) {
                    emptyRepository = false;
                    break;
                }
            } catch (NoHeadException ignored) {
                // ok
            }

            boolean mustBranch = !pushBranch.equals(pullBranch);
            if (mustBranch && !emptyRepository) {
                context.getLogger().debug(RB.$("repository.branching", pushBranch));
                git.checkout()
                    .setName(pushBranch)
                    .setCreateBranch(true)
                    .call();
            }

            prepareWorkingCopy(props, directory, distribution);

            // add everything
            git.add()
                .addFilepattern(".")
                .call();

            props.setAll(distribution.props());
            context.getModel().getRelease().getReleaser().fillProps(props, context.getModel());

            // setup commit
            context.getLogger().debug(RB.$("repository.commit.setup"));
            CommitCommand commitCommand = git.commit()
                .setAll(true)
                .setMessage(packager.getRepositoryTap().getResolvedCommitMessage(props))
                .setAuthor(packager.getCommitAuthor().getName(), packager.getCommitAuthor().getEmail());
            commitCommand.setCredentialsProvider(credentialsProvider);

            boolean signingEnabled = releaser.isSign();
            String signingKey = "**********";
            JReleaserGpgSigner signer = new JReleaserGpgSigner(context, signingEnabled);

            commitCommand = commitCommand
                .setSign(signingEnabled)
                .setSigningKey(signingKey)
                .setGpgSigner(signer);

            commitCommand.call();

            if (mustBranch && emptyRepository) {
                context.getLogger().debug(RB.$("repository.branching", pushBranch));
                git.checkout()
                    .setName(pushBranch)
                    .setCreateBranch(true)
                    .call();
            }

            String tagName = tap.getResolvedTagName(props);
            context.getLogger().debug(RB.$("git.releaser.repository.tag"), tagName, tagName);
            git.tag()
                .setSigned(signingEnabled)
                .setSigningKey(signingKey)
                .setGpgSigner(signer)
                .setName(tagName)
                .setForceUpdate(true)
                .call();

            context.getLogger().info(RB.$("repository.push"), target);
            // push commit
            context.getLogger().debug(RB.$("repository.commit.push"));
            git.push()
                .setDryRun(false)
                .setPushAll()
                .setCredentialsProvider(credentialsProvider)
                .setPushTags()
                .call();
        } catch (Exception e) {
            throw new PackagerProcessingException(RB.$("ERROR_unexpected_repository_update", target), e);
        }
    }

    protected void prepareWorkingCopy(TemplateContext props, Path directory, Distribution distribution) throws IOException {
        Path packageDirectory = props.get(KEY_DISTRIBUTION_PACKAGE_DIRECTORY);
        prepareWorkingCopy(packageDirectory, directory);
    }

    protected void prepareWorkingCopy(Path source, Path destination) throws IOException {
        context.getLogger().debug(RB.$("repository.copy.files"), context.relativizeToBasedir(source));

        if (!FileUtils.copyFilesRecursive(context.getLogger(), source, destination)) {
            throw new IOException(RB.$("ERROR_repository_copy_files",
                context.relativizeToBasedir(source)));
        }
    }

    protected void prepareWorkingCopy(Path source, Path destination, Predicate<Path> filter) throws IOException {
        context.getLogger().debug(RB.$("repository.copy.files"), context.relativizeToBasedir(source));

        if (!FileUtils.copyFilesRecursive(context.getLogger(), source, destination, filter)) {
            throw new IOException(RB.$("ERROR_repository_copy_files",
                context.relativizeToBasedir(source)));
        }
    }

    protected String resolveGitUsername(BaseReleaser<?, ?> releaser) {
        String username = packager.getRepositoryTap().getUsername();
        return isNotBlank(username) ? username : releaser.getUsername();
    }

    protected String resolveGitToken(BaseReleaser<?, ?> releaser) {
        String token = packager.getRepositoryTap().getToken();
        return isNotBlank(token) ? token : releaser.getToken();
    }
}
