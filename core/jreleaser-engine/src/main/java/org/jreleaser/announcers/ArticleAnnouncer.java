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
package org.jreleaser.announcers;

import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.model.spi.release.Repository;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.git.JReleaserGpgSigner;
import org.jreleaser.util.FileUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.FileUtils.createDirectoriesWithFullAccess;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class ArticleAnnouncer implements Announcer<org.jreleaser.model.api.announce.ArticleAnnouncer> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.ArticleAnnouncer article;

    public ArticleAnnouncer(JReleaserContext context) {
        this.context = context;
        this.article = context.getModel().getAnnounce().getArticle();
    }

    @Override
    public org.jreleaser.model.api.announce.ArticleAnnouncer getAnnouncer() {
        return article.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.ArticleAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return article.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        Path prepareDirectory = context.getPrepareDirectory().resolve("article");
        TemplateContext props = prepareFiles(prepareDirectory);
        publishToRepository(prepareDirectory, props);
    }

    private TemplateContext prepareFiles(Path prepareDirectory) throws AnnounceException {
        Path templateDirectory = context.getBasedir().resolve(article.getTemplateDirectory());

        try {
            FileUtils.deleteFiles(prepareDirectory);
            Files.createDirectories(prepareDirectory);
        } catch (IOException e) {
            context.getLogger().trace(e);
            throw new AnnounceException(RB.$("ERROR_unexpected_article_announcer", context.relativizeToBasedir(templateDirectory)), e);
        }

        context.getLogger().debug(RB.$("announcer.article.resolve.templates"));

        TemplateContext props = context.fullProps();
        context.getChangelog().apply(props);
        context.getModel().getRelease().getReleaser().fillProps(props, context.getModel());
        props.setAll(article.resolvedExtraProperties());

        try {
            for (Artifact file : article.getFiles()) {
                Path input = file.getResolvedPath(context, templateDirectory, true);
                Path output = file.getResolvedTransform(context, prepareDirectory);
                if (null == output) {
                    output = file.getResolvedPath(context, prepareDirectory, false);
                }

                try (Reader reader = Files.newBufferedReader(input)) {
                    context.getLogger().debug(RB.$("announcer.article.eval.template"), context.relativizeToBasedir(input));
                    String content = applyTemplate(reader, props);
                    context.getLogger().debug(RB.$("announcer.article.write.template"), context.relativizeToBasedir(input));
                    writeFile(content, output);
                }
            }
        } catch (JReleaserException e) {
            context.getLogger().warn(e.getMessage());
            context.getLogger().trace(e);
            throw new AnnounceException(RB.$("ERROR_unexpected_template_resolution", context.relativizeToBasedir(templateDirectory)), e);
        } catch (IOException e) {
            context.getLogger().trace(e);
            throw new AnnounceException(RB.$("ERROR_unexpected_template_resolution", context.relativizeToBasedir(templateDirectory)), e);
        }

        return props;
    }

    private void publishToRepository(Path prepareDirectory, TemplateContext props) throws AnnounceException {
        context.getLogger().info(RB.$("repository.setup"), article.getRepository().getCanonicalRepoName());
        if (context.isDryrun()) {
            return;
        }

        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();

        try {
            // get the repository
            context.getLogger().debug(RB.$("repository.locate"), article.getRepository().getCanonicalRepoName());
            Repository repository = context.getReleaser().maybeCreateRepository(
                article.getRepository().getOwner(),
                article.getRepository().getResolvedName(),
                resolveGitToken(releaser),
                article.getRepository().asImmutable());

            UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                resolveGitUsername(releaser),
                resolveGitToken(releaser));

            // clone the repository
            context.getLogger().debug(RB.$("repository.clone"), repository.getHttpUrl());
            Path directory = Files.createTempDirectory("jreleaser-" + article.getRepository().getResolvedName());

            String pullBranch = article.getRepository().getBranch();
            String pushBranch = resolveTemplate(article.getRepository().getBranchPush(), props);

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

            copyFiles(prepareDirectory, directory);

            // add everything
            git.add()
                .addFilepattern(".")
                .call();

            // setup commit
            context.getLogger().debug(RB.$("repository.commit.setup"));
            CommitCommand commitCommand = git.commit()
                .setAll(true)
                .setMessage(article.getRepository().getResolvedCommitMessage(props))
                .setAuthor(article.getCommitAuthor().getName(), article.getCommitAuthor().getEmail());
            commitCommand.setCredentialsProvider(credentialsProvider);
            commitCommand = commitCommand
                .setSign(releaser.isSign())
                .setSigningKey("**********")
                .setGpgSigner(new JReleaserGpgSigner(context, releaser.isSign()));

            commitCommand.call();

            if (mustBranch && emptyRepository) {
                context.getLogger().debug(RB.$("repository.branching", pushBranch));
                git.checkout()
                    .setName(pushBranch)
                    .setCreateBranch(true)
                    .call();
            }

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

    private String resolveGitUsername(BaseReleaser<?, ?> releaser) {
        String username = article.getRepository().getUsername();
        return isNotBlank(username) ? username : releaser.getUsername();
    }

    private String resolveGitToken(BaseReleaser<?, ?> releaser) {
        String token = article.getRepository().getToken();
        return isNotBlank(token) ? token : releaser.getToken();
    }

    private void writeFile(String content, Path outputFile) throws AnnounceException {
        try {
            createDirectoriesWithFullAccess(outputFile.getParent());
            Files.write(outputFile, content.getBytes(UTF_8), CREATE, WRITE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new AnnounceException(RB.$("ERROR_unexpected_error_writing_file", outputFile.toAbsolutePath()), e);
        }
    }
}
