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
package org.jreleaser.model.internal.validation.upload;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.upload.GitlabUploader;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public abstract class GitlabUploaderValidator extends Validator {
    public static void validateGitlabUploader(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, GitlabUploader> gitlab = context.getModel().getUpload().getGitlab();
        if (!gitlab.isEmpty()) context.getLogger().debug("upload.gitlab");

        for (Map.Entry<String, GitlabUploader> e : gitlab.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig()) {
                validateGitlabUploader(context, mode, e.getValue(), errors);
            }
        }
    }

    private static void validateGitlabUploader(JReleaserContext context, Mode mode, GitlabUploader gitlab, Errors errors) {
        context.getLogger().debug("upload.gitlab.{}", gitlab.getName());

        if (!gitlab.isActiveSet()) {
            gitlab.setActive(Active.NEVER);
        }
        if (!gitlab.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!gitlab.isArtifacts() && !gitlab.isFiles() && !gitlab.isSignatures()) {
            errors.warning(RB.$("WARNING.validation.uploader.no.artifacts", gitlab.getType(), gitlab.getName()));
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            gitlab.disable();
            return;
        }

        String baseKey = "upload.gitlab" + gitlab.getName() + ".";
        gitlab.setToken(
            checkProperty(context,
                listOf(
                    "GITLAB_" + Env.toVar(gitlab.getName()) + "_TOKEN",
                    "GITLAB_TOKEN"),
                baseKey + "token",
                gitlab.getToken(),
                errors,
                context.isDryrun()));

        gitlab.setHost(
            checkProperty(context,
                "GITLAB_" + Env.toVar(gitlab.getName()) + "_HOST",
                baseKey + "host",
                gitlab.getHost(),
                errors,
                context.isDryrun()));

        if (isBlank(gitlab.getPackageName())) {
            gitlab.setPackageName(gitlab.getName());
        }
        if (isBlank(gitlab.getPackageVersion())) {
            gitlab.setPackageVersion("{{projectVersion}}");
        }

        if (isBlank(gitlab.getProjectIdentifier())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "gitlab." + gitlab.getName() + ".projectIdentifier"));
        }

        validateTimeout(gitlab);
    }
}
