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
package org.jreleaser.tools;

import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.RepositoryTool;
import org.jreleaser.model.releaser.spi.Releaser;
import org.jreleaser.model.releaser.spi.Repository;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.sdk.git.InMemoryGpgSigner;
import org.jreleaser.util.Constants;
import org.jreleaser.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractRepositoryToolProcessor<T extends RepositoryTool> extends AbstractToolProcessor<T> {
    protected T tool;

    protected AbstractRepositoryToolProcessor(JReleaserContext context) {
        super(context);
    }

    protected boolean doUploadDistribution(Distribution distribution, Releaser releaser, Map<String, Object> props) throws ToolProcessingException {
        GitService gitService = context.getModel().getRelease().getGitService();

        try {
            // get the repository
            context.getLogger().debug("Locating repository {}", tool.getRepositoryTap().getCanonicalRepoName());
            Repository repository = releaser.maybeCreateRepository(
                tool.getRepositoryTap().getOwner(),
                tool.getRepositoryTap().getResolvedName(),
                resolveGitToken(gitService));

            UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                resolveGitUsername(gitService),
                resolveGitToken(gitService));

            // clone the repository
            context.getLogger().debug("Clonning {}", repository.getHttpUrl());
            Path directory = Files.createTempDirectory("jreleaser-" + tool.getRepositoryTap().getResolvedName());
            Git git = Git.cloneRepository()
                .setCredentialsProvider(credentialsProvider)
                .setBranch("HEAD")
                .setDirectory(directory.toFile())
                .setURI(repository.getHttpUrl())
                .call();

            prepareWorkingCopy(props, directory);

            // add everything
            git.add()
                .addFilepattern(".")
                .call();

            // setup commit
            context.getLogger().debug("Setting up commit");
            CommitCommand commitCommand = git.commit()
                .setAll(true)
                .setMessage(distribution.getExecutable() + " " + gitService.getResolvedTagName(context.getModel().getProject()))
                .setAuthor(tool.getCommitAuthor().getName(), tool.getCommitAuthor().getEmail());
            commitCommand.setCredentialsProvider(credentialsProvider);
            if (gitService.isSign()) {
                commitCommand = commitCommand
                    .setSign(true)
                    .setSigningKey("**********")
                    .setGpgSigner(new InMemoryGpgSigner(context));
            }

            commitCommand.call();

            // push commit
            context.getLogger().debug("Pushing commit to remote, dryrun = {}", context.isDryrun());
            git.push()
                .setDryRun(context.isDryrun())
                .setPushAll()
                .setCredentialsProvider(credentialsProvider)
                .call();
        } catch (Exception e) {
            throw new ToolProcessingException("Unexpected error updating " + tool.getRepositoryTap().getCanonicalRepoName(), e);
        }

        return true;
    }

    protected void prepareWorkingCopy(Map<String, Object> props, Path directory) throws IOException {
        // copy files over
        Path packageDirectory = (Path) props.get(Constants.KEY_PACKAGE_DIRECTORY);
        context.getLogger().debug("Copying files from {}", context.getBasedir().relativize(packageDirectory));

        if (!FileUtils.copyFilesRecursive(context.getLogger(), packageDirectory, directory)) {
            throw new IOException("Could not copy files from " +
                context.getBasedir().relativize(packageDirectory));
        }
    }

    protected String resolveGitUsername(GitService gitService) {
        if (isNotBlank(tool.getRepositoryTap().getUsername())) {
            return tool.getRepositoryTap().getUsername();
        }
        return gitService.getUsername();
    }

    protected String resolveGitToken(GitService gitService) {
        if (isNotBlank(tool.getRepositoryTap().getResolvedToken(gitService))) {
            return tool.getRepositoryTap().getResolvedToken(gitService);
        }
        return gitService.getResolvedToken();
    }
}
