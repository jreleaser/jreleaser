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
package org.jreleaser.model.internal.validation.announce;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.announce.ArticleAnnouncer;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.io.File;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
public abstract class ArticleAnnouncerValidator extends Validator {
    public static void validateArticle(JReleaserContext context, ArticleAnnouncer article, Errors errors) {
        context.getLogger().debug("announce.article");
        if (!article.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        BaseReleaser<?, ?> service = context.getModel().getRelease().getReleaser();
        ArticleAnnouncer.Repository repository = article.getRepository();

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
                service.getUsername()));

        repository.setToken(
            checkProperty(context,
                Env.toVar(repository.getBasename() + "_" + service.getServiceName()) + "_TOKEN",
                "announce.article.repository.token",
                repository.getToken(),
                service.getToken()));

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