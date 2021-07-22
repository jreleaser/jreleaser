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
import org.jreleaser.sdk.git.JReleaserGpgSigner;
import org.jreleaser.util.Constants;
import org.jreleaser.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractRepositoryToolProcessor<T extends RepositoryTool> extends AbstractToolProcessor<T> {
    protected AbstractRepositoryToolProcessor(JReleaserContext context) {
        super(context);
    }

    protected boolean doPublishDistribution(Distribution distribution, Releaser releaser, Map<String, Object> props) throws ToolProcessingException {
        context.getLogger().info("setting up repository {}", tool.getRepositoryTap().getCanonicalRepoName());
        if (context.isDryrun()) {
            return true;
        }

        GitService gitService = context.getModel().getRelease().getGitService();

        try {
            // get the repository
            context.getLogger().debug("locating repository {}", tool.getRepositoryTap().getCanonicalRepoName());
            Repository repository = releaser.maybeCreateRepository(
                tool.getRepositoryTap().getOwner(),
                tool.getRepositoryTap().getResolvedName(),
                resolveGitToken(gitService));

            UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                resolveGitUsername(gitService),
                resolveGitToken(gitService));

            // clone the repository
            context.getLogger().debug("cloning {}", repository.getHttpUrl());
            Path directory = Files.createTempDirectory("jreleaser-" + tool.getRepositoryTap().getResolvedName());
            Git git = Git.cloneRepository()
                .setCredentialsProvider(credentialsProvider)
                .setBranch("HEAD")
                .setDirectory(directory.toFile())
                .setURI(repository.getHttpUrl())
                .call();

            prepareWorkingCopy(props, directory, distribution);

            // add everything
            git.add()
                .addFilepattern(".")
                .call();

            // setup commit
            context.getLogger().debug("setting up commit");
            CommitCommand commitCommand = git.commit()
                .setAll(true)
                .setMessage(distribution.getExecutable() + " " + gitService.getResolvedTagName(context.getModel()))
                .setAuthor(tool.getCommitAuthor().getName(), tool.getCommitAuthor().getEmail());
            commitCommand.setCredentialsProvider(credentialsProvider);
            commitCommand = commitCommand
                .setSign(gitService.isSign())
                .setSigningKey("**********")
                .setGpgSigner(new JReleaserGpgSigner(context, gitService.isSign()));

            commitCommand.call();

            context.getLogger().info("pushing to {}", tool.getRepositoryTap().getCanonicalRepoName());
            // push commit
            context.getLogger().debug("pushing commit to remote");
            git.push()
                .setDryRun(false)
                .setPushAll()
                .setCredentialsProvider(credentialsProvider)
                .call();
        } catch (Exception e) {
            throw new ToolProcessingException("Unexpected error updating " + tool.getRepositoryTap().getCanonicalRepoName(), e);
        }

        return true;
    }

    protected void prepareWorkingCopy(Map<String, Object> props, Path directory, Distribution distribution) throws ToolProcessingException, IOException {
        Path packageDirectory = (Path) props.get(Constants.KEY_DISTRIBUTION_PACKAGE_DIRECTORY);
        prepareWorkingCopy(packageDirectory, directory);
    }

    protected void prepareWorkingCopy(Path source, Path destination) throws IOException {
        context.getLogger().debug("copying files from {}", context.relativizeToBasedir(source));

        if (!FileUtils.copyFilesRecursive(context.getLogger(), source, destination)) {
            throw new IOException("Could not copy files from " +
                context.relativizeToBasedir(source));
        }
    }

    protected String resolveGitUsername(GitService gitService) {
        String username = tool.getRepositoryTap().getResolvedUsername(gitService);
        return isNotBlank(username) ? username : gitService.getResolvedUsername();
    }

    protected String resolveGitToken(GitService gitService) {
        String token = tool.getRepositoryTap().getResolvedToken(gitService);
        return isNotBlank(token) ? token : gitService.getResolvedToken();
    }
}
