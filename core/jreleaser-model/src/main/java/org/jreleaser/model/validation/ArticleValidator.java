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
package org.jreleaser.model.validation;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Article;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Repository;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.io.File;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
public abstract class ArticleValidator extends Validator {
    public static void validateArticle(JReleaserContext context, Article article, Errors errors) {
        if (!article.resolveEnabled(context.getModel().getProject())) return;
        context.getLogger().debug("announce.article");

        GitService service = context.getModel().getRelease().getGitService();
        Repository repository = article.getRepository();

        validateCommitAuthor(article, service);
        validateOwner(repository, service);

        if (isBlank(repository.getName())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "announce.article.repository.name"));
        }

        if (isBlank(article.getRepository().getCommitMessage())) {
            article.getRepository().setCommitMessage("{{projectName}} {{tagName}}");
        }

        repository.setUsername(
            checkProperty(context,
                Env.toVar(repository.getBasename() + "_" + service.getServiceName()) + "_USERNAME",
                "announce.article.repository.username",
                repository.getUsername(),
                service.getResolvedUsername()));

        repository.setToken(
            checkProperty(context,
                Env.toVar(repository.getBasename() + "_" + service.getServiceName()) + "_TOKEN",
                "announce.article.repository.token",
                repository.getToken(),
                service.getResolvedToken()));

        repository.setBranch(
            checkProperty(context,
                Env.toVar(repository.getBasename() + "_" + service.getServiceName()) + "_BRANCH",
                "announce.article.repository.branch",
                repository.getBranch(),
                "HEAD"));

        if (isBlank(repository.getTagName())) {
            repository.setTagName(service.getTagName());
        }

        if (isBlank(article.getTemplateDirectory())) {
            article.setTemplateDirectory("src/jreleaser/templates/article");
        }

        File templateDirectoryFile = context.getBasedir().resolve(article.getTemplateDirectory().trim()).toFile();
        if (!(templateDirectoryFile.exists())) {
            errors.configuration(RB.$("validation_directory_not_exist", "announce.article.templateDirectory", article.getTemplateDirectory()));
        }

        if (!templateDirectoryFile.isDirectory()) {
            errors.configuration(RB.$("validation_is_not_a_directory", "announce.article.templateDirectory", article.getTemplateDirectory()));
        }

        if (templateDirectoryFile.listFiles() == null || templateDirectoryFile.listFiles().length == 0) {
            errors.configuration(RB.$("validation_directory_is_empty", "announce.article.templateDirectory", article.getTemplateDirectory()));
        }

        if (article.getFiles().isEmpty()) {
            errors.configuration(RB.$("validation_must_not_be_empty", "announce.article.files"));
        }
    }
}