/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.upload.GiteaUploader;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class GiteaUploaderValidator {
    private GiteaUploaderValidator() {
        // noop
    }

    public static void validateGiteaUploader(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, GiteaUploader> gitea = context.getModel().getUpload().getGitea();
        if (!gitea.isEmpty()) context.getLogger().debug("upload.gitea");

        for (Map.Entry<String, GiteaUploader> e : gitea.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig()) {
                validateGiteaUploader(context, e.getValue(), errors);
            }
        }
    }

    private static void validateGiteaUploader(JReleaserContext context, GiteaUploader gitea, Errors errors) {
        context.getLogger().debug("upload.gitea.{}", gitea.getName());

        resolveActivatable(context, gitea,
            listOf("upload.gitea." + gitea.getName(), "upload.gitea"),
            "NEVER");
        if (!gitea.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!gitea.isArtifacts() && !gitea.isFiles() && !gitea.isSignatures()) {
            errors.warning(RB.$("WARNING.validation.uploader.no.artifacts", gitea.getType(), gitea.getName()));
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            gitea.disable();
            return;
        }

        String baseKey1 = "upload.gitea." + gitea.getName();
        String baseKey2 = "upload.gitea";
        String baseKey3 = "gitea." + gitea.getName();
        String baseKey4 = "gitea";

        gitea.setToken(
            checkProperty(context,
                listOf(
                    baseKey1 + ".token",
                    baseKey2 + ".token",
                    baseKey3 + ".token",
                    baseKey4 + ".token"),
                baseKey1 + ".token",
                gitea.getToken(),
                errors,
                context.isDryrun()));

        gitea.setHost(
            checkProperty(context,
                listOf(
                    baseKey1 + ".host",
                    baseKey2 + ".host",
                    baseKey3 + ".host",
                    baseKey4 + ".host"),
                baseKey1 + ".host",
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
            errors.configuration(RB.$("validation_must_not_be_blank", baseKey1 + ".owner"));
        }

        validateTimeout(gitea);
    }
}
