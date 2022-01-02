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
package org.jreleaser.announcers;

import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Article;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;
import org.jreleaser.model.releaser.spi.Repository;
import org.jreleaser.sdk.git.JReleaserGpgSigner;
import org.jreleaser.util.Constants;
import org.jreleaser.util.FileUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.jreleaser.util.FileUtils.createDirectoriesWithFullAccess;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.MustacheUtils.passThrough;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
public class ArticleAnnouncer implements Announcer {
    private final JReleaserContext context;
    private Article article;

    ArticleAnnouncer(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return Article.NAME;
    }

    @Override
    public boolean isEnabled() {
        return context.getModel().getAnnounce().getArticle().isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        article = context.getModel().getAnnounce().getArticle();

        Path prepareDirectory = context.getPrepareDirectory().resolve("article");
        prepareFiles(prepareDirectory);
        publishToRepository(prepareDirectory);
    }

    private void prepareFiles(Path prepareDirectory) throws AnnounceException {
        Path templateDirectory = context.getBasedir().resolve(article.getTemplateDirectory());

        try {
            FileUtils.deleteFiles(prepareDirectory);
            Files.createDirectories(prepareDirectory);
        } catch (IOException e) {
            context.getLogger().trace(e);
            throw new AnnounceException(RB.$("ERROR_unexpected_article_announcer", context.relativizeToBasedir(templateDirectory)), e);
        }

        context.getLogger().debug(RB.$("announcer.article.resolve.templates"));

        Map<String, Object> props = context.props();
        props.put(Constants.KEY_CHANGELOG, passThrough(context.getChangelog()));
        context.getModel().getRelease().getGitService().fillProps(props, context.getModel());
        props.putAll(article.getResolvedExtraProperties());

        try {
            for (Artifact file : article.getFiles()) {
                Path input = file.getResolvedPath(context, templateDirectory, true);
                Path output = file.getResolvedTransform(context, prepareDirectory);
                if (null == output) {
                    output = file.getResolvedPath(context, prepareDirectory, false);
                }

                Reader reader = Files.newBufferedReader(input);
                context.getLogger().debug(RB.$("announcer.article.eval.template"), context.relativizeToBasedir(input));
                String content = applyTemplate(reader, props);
                context.getLogger().debug(RB.$("announcer.article.write.template"), context.relativizeToBasedir(input));
                writeFile(content, output);
            }
        } catch (JReleaserException e) {
            context.getLogger().warn(e.getMessage());
            context.getLogger().trace(e);
            throw new AnnounceException(RB.$("ERROR_unexpected_template_resolution", context.relativizeToBasedir(templateDirectory)), e);
        } catch (IOException e) {
            context.getLogger().trace(e);
            throw new AnnounceException(RB.$("ERROR_unexpected_template_resolution", context.relativizeToBasedir(templateDirectory)), e);
        }
    }

    private void publishToRepository(Path prepareDirectory) throws AnnounceException {
        context.getLogger().info(RB.$("repository.setup"), article.getRepository().getCanonicalRepoName());
        if (context.isDryrun()) {
            return;
        }

        GitService gitService = context.getModel().getRelease().getGitService();

        try {
            // get the repository
            context.getLogger().debug(RB.$("repository.locate"), article.getRepository().getCanonicalRepoName());
            Repository repository = context.getReleaser().maybeCreateRepository(
                article.getRepository().getOwner(),
                article.getRepository().getResolvedName(),
                resolveGitToken(gitService));

            UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                resolveGitUsername(gitService),
                resolveGitToken(gitService));

            // clone the repository
            context.getLogger().debug(RB.$("repository.clone"), repository.getHttpUrl());
            Path directory = Files.createTempDirectory("jreleaser-" + article.getRepository().getResolvedName());
            Git git = Git.cloneRepository()
                .setCredentialsProvider(credentialsProvider)
                .setBranch(article.getRepository().getBranch())
                .setDirectory(directory.toFile())
                .setURI(repository.getHttpUrl())
                .call();

            copyFiles(prepareDirectory, directory);

            // add everything
            git.add()
                .addFilepattern(".")
                .call();

            Map<String, Object> props = context.props();
            context.getModel().getRelease().getGitService().fillProps(props, context.getModel());

            // setup commit
            context.getLogger().debug(RB.$("repository.commit.setup"));
            CommitCommand commitCommand = git.commit()
                .setAll(true)
                .setMessage(article.getRepository().getResolvedCommitMessage(props))
                .setAuthor(article.getCommitAuthor().getName(), article.getCommitAuthor().getEmail());
            commitCommand.setCredentialsProvider(credentialsProvider);
            commitCommand = commitCommand
                .setSign(gitService.isSign())
                .setSigningKey("**********")
                .setGpgSigner(new JReleaserGpgSigner(context, gitService.isSign()));

            commitCommand.call();

            context.getLogger().info(RB.$("repository.push"), article.getRepository().getCanonicalRepoName());
            // push commit
            context.getLogger().debug(RB.$("repository.commit.push"));
            git.push()
                .setDryRun(false)
                .setPushAll()
                .setCredentialsProvider(credentialsProvider)
                .call();
        } catch (Exception e) {
            throw new AnnounceException(RB.$("ERROR_unexpected_repository_update", article.getRepository().getCanonicalRepoName()), e);
        }
    }

    private void copyFiles(Path source, Path destination) throws IOException {
        context.getLogger().debug(RB.$("repository.copy.files"), context.relativizeToBasedir(source));

        if (!FileUtils.copyFilesRecursive(context.getLogger(), source, destination)) {
            throw new IOException(RB.$("ERROR_repository_copy_files",
                context.relativizeToBasedir(source)));
        }
    }

    private String resolveGitUsername(GitService gitService) {
        String username = article.getRepository().getResolvedUsername(gitService);
        return isNotBlank(username) ? username : gitService.getResolvedUsername();
    }

    private String resolveGitToken(GitService gitService) {
        String token = article.getRepository().getResolvedToken(gitService);
        return isNotBlank(token) ? token : gitService.getResolvedToken();
    }

    private void writeFile(String content, Path outputFile) throws AnnounceException {
        try {
            createDirectoriesWithFullAccess(outputFile.getParent());
            Files.write(outputFile, content.getBytes(), CREATE, WRITE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new AnnounceException(RB.$("ERROR_unexpected_error_writing_file", outputFile.toAbsolutePath()), e);
        }
    }
}
