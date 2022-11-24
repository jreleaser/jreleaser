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
import org.jreleaser.model.internal.upload.GiteaUploader;
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
public abstract class GiteaUploaderValidator extends Validator {
    public static void validateGiteaUploader(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, GiteaUploader> gitea = context.getModel().getUpload().getGitea();
        if (!gitea.isEmpty()) context.getLogger().debug("upload.gitea");

        for (Map.Entry<String, GiteaUploader> e : gitea.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig()) {
                validateGiteaUploader(context, mode, e.getValue(), errors);
            }
        }
    }

    private static void validateGiteaUploader(JReleaserContext context, Mode mode, GiteaUploader gitea, Errors errors) {
        context.getLogger().debug("upload.gitea.{}", gitea.getName());

        if (!gitea.isActiveSet()) {
            gitea.setActive(Active.NEVER);
        }
        if (!gitea.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!gitea.isArtifacts() && !gitea.isFiles() && !gitea.isSignatures()) {
            errors.warning(RB.$("WARNING.validation.uploader.no.artifacts", gitea.getType(), gitea.getName()));
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            gitea.disable();
            return;
        }

        String baseKey = "upload.gitea" + gitea.getName() + ".";
        gitea.setToken(
            checkProperty(context,
                listOf(
                    "GITEA_" + Env.toVar(gitea.getName()) + "_TOKEN",
                    "GITEA_TOKEN"),
                baseKey + "token",
                gitea.getToken(),
                errors,
                context.isDryrun()));

        gitea.setHost(
            checkProperty(context,
                "GITEA_" + Env.toVar(gitea.getName()) + "_HOST",
                baseKey + "host",
                gitea.getHost(),
                errors,
                context.isDryrun()));

        if (isBlank(gitea.getPackageName())) {
            gitea.setPackageName(gitea.getName());
        }
        if (isBlank(gitea.getPackageVersion())) {
            gitea.setPackageVersion("{{projectVersion}}");
        }

        if (isBlank(gitea.getOwner())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "gitea." + gitea.getName() + ".owner"));
        }

        validateTimeout(gitea);
    }
}
