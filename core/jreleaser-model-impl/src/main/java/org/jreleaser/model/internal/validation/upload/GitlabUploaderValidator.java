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
import org.jreleaser.model.internal.upload.GitlabUploader;
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
public final class GitlabUploaderValidator {
    private GitlabUploaderValidator() {
        // noop
    }

    public static void validateGitlabUploader(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, GitlabUploader> gitlab = context.getModel().getUpload().getGitlab();
        if (!gitlab.isEmpty()) context.getLogger().debug("upload.gitlab");

        for (Map.Entry<String, GitlabUploader> e : gitlab.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig()) {
                validateGitlabUploader(context, e.getValue(), errors);
            }
        }
    }

    private static void validateGitlabUploader(JReleaserContext context, GitlabUploader gitlab, Errors errors) {
        context.getLogger().debug("upload.gitlab.{}", gitlab.getName());

        resolveActivatable(context, gitlab,
            listOf("upload.gitlab." + gitlab.getName(), "upload.gitlab"),
            "NEVER");
        if (!gitlab.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!gitlab.isArtifacts() && !gitlab.isFiles() && !gitlab.isSignatures()) {
            errors.warning(RB.$("WARNING.validation.uploader.no.artifacts", gitlab.getType(), gitlab.getName()));
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            gitlab.disable();
            return;
        }

        String baseKey1 = "upload.gitlab." + gitlab.getName();
        String baseKey2 = "upload.gitlab";
        String baseKey3 = "gitlab." + gitlab.getName();
        String baseKey4 = "gitlab";

        gitlab.setToken(
            checkProperty(context,
                listOf(
                    baseKey1 + ".token",
                    baseKey2 + ".token",
                    baseKey3 + ".token",
                    baseKey4 + ".token"),
                baseKey1 + ".token",
                gitlab.getToken(),
                errors,
                context.isDryrun()));

        gitlab.setHost(
            checkProperty(context,
                listOf(
                    baseKey1 + ".host",
                    baseKey2 + ".host",
                    baseKey3 + ".host",
                    baseKey4 + ".host"),
                baseKey1 + ".host",
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
            errors.configuration(RB.$("validation_must_not_be_blank", baseKey1 + ".projectIdentifier"));
        }

        validateTimeout(gitlab);
    }
}
