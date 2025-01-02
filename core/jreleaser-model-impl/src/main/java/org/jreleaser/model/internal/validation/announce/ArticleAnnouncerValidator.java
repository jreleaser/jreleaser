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
package org.jreleaser.model.internal.validation.announce;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.announce.ArticleAnnouncer;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.util.Errors;

import java.io.File;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateCommitAuthor;
import static org.jreleaser.model.internal.validation.common.Validator.validateOwner;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
public final class ArticleAnnouncerValidator {
    private ArticleAnnouncerValidator() {
        // noop
    }

    public static void validateArticle(JReleaserContext context, ArticleAnnouncer announcer, Errors errors) {
        context.getLogger().debug("announce.article");
        resolveActivatable(context, announcer, "announce.article", "NEVER");
        if (!announcer.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        BaseReleaser<?, ?> service = context.getModel().getRelease().getReleaser();
        ArticleAnnouncer.Repository repository = announcer.getRepository();

        validateCommitAuthor(announcer, service);
        validateOwner(repository, service);

        if (isBlank(repository.getName())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "announce.article.repository.name"));
        }

        if (isBlank(announcer.getRepository().getCommitMessage())) {
            announcer.getRepository().setCommitMessage("{{projectName}} {{tagName}}");
        }

        repository.setUsername(
            checkProperty(context,
                listOf(
                    "announce.article.repository.username",
                    repository.getBasename() + "." + service.getServiceName() + ".username"),
                "announce.article.repository.username",
                repository.getUsername(),
                service.getUsername()));

        repository.setToken(
            checkProperty(context,
                listOf(
                    "announce.article.repository.token",
                    repository.getBasename() + "." + service.getServiceName() + ".token"),
                "announce.article.repository.token",
                repository.getToken(),
                service.getToken()));

        repository.setBranch(
            checkProperty(context,
                listOf(
                    "announce.article.repository.branch",
                    repository.getBasename() + "." + service.getServiceName() + ".branch"),
                "announce.article.repository.branch",
                repository.getBranch(),
                "HEAD"));

        repository.setBranchPush(
            checkProperty(context,
                listOf(
                    "announce.article.repository.branch.push",
                    repository.getBasename() + "." + service.getServiceName() + ".branch.push"),
                "announce.article.repository.branch.push",
                repository.getBranchPush(),
                repository.getBranch()));

        if (isBlank(repository.getTagName())) {
            repository.setTagName(service.getTagName());
        }

        if (isBlank(announcer.getTemplateDirectory())) {
            announcer.setTemplateDirectory("src/jreleaser/templates/article");
        }

        File templateDirectoryFile = context.getBasedir().resolve(announcer.getTemplateDirectory().trim()).toFile();
        if (!(templateDirectoryFile.exists())) {
            errors.configuration(RB.$("validation_directory_not_exist", "announce.article.templateDirectory", announcer.getTemplateDirectory()));
        }

        if (!templateDirectoryFile.isDirectory()) {
            errors.configuration(RB.$("validation_is_not_a_directory", "announce.article.templateDirectory", announcer.getTemplateDirectory()));
        }

        if (null == templateDirectoryFile.listFiles() || templateDirectoryFile.listFiles().length == 0) {
            errors.configuration(RB.$("validation_directory_is_empty", "announce.article.templateDirectory", announcer.getTemplateDirectory()));
        }

        if (announcer.getFiles().isEmpty()) {
            errors.configuration(RB.$("validation_must_not_be_empty", "announce.article.files"));
        }
    }
}