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
import org.jreleaser.model.internal.upload.ArtifactoryUploader;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public abstract class ArtifactoryUploaderValidator extends Validator {
    public static void validateArtifactory(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, ArtifactoryUploader> artifactory = context.getModel().getUpload().getArtifactory();
        if (!artifactory.isEmpty()) context.getLogger().debug("upload.artifactory");

        for (Map.Entry<String, ArtifactoryUploader> e : artifactory.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig()) {
                validateArtifactory(context, mode, e.getValue(), errors);
            }
        }
    }

    private static void validateArtifactory(JReleaserContext context, Mode mode, ArtifactoryUploader artifactory, Errors errors) {
        context.getLogger().debug("upload.artifactory.{}", artifactory.getName());

        if (!artifactory.isActiveSet()) {
            artifactory.setActive(Active.NEVER);
        }
        if (!artifactory.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!artifactory.isArtifacts() && !artifactory.isFiles() && !artifactory.isSignatures()) {
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            artifactory.disable();
            return;
        }

        if (artifactory.getRepositories().isEmpty()) {
            errors.configuration(RB.$("validation_artifactory_no_repositories", "artifactory." + artifactory.getName()));
            context.getLogger().debug(RB.$("validation.disabled.no.repositories"));
            artifactory.disable();
            return;
        }

        artifactory.setHost(
            checkProperty(context,
                "ARTIFACTORY_" + Env.toVar(artifactory.getName()) + "_HOST",
                "artifactory.host",
                artifactory.getHost(),
                errors));

        switch (artifactory.resolveAuthorization()) {
            case BEARER:
                artifactory.setPassword(
                    checkProperty(context,
                        "ARTIFACTORY_" + Env.toVar(artifactory.getName()) + "_PASSWORD",
                        "artifactory.password",
                        artifactory.getPassword(),
                        errors,
                        context.isDryrun()));
                break;
            case BASIC:
                artifactory.setUsername(
                    checkProperty(context,
                        "ARTIFACTORY_" + Env.toVar(artifactory.getName()) + "_USERNAME",
                        "artifactory.username",
                        artifactory.getUsername(),
                        errors,
                        context.isDryrun()));

                artifactory.setPassword(
                    checkProperty(context,
                        "ARTIFACTORY_" + Env.toVar(artifactory.getName()) + "_PASSWORD",
                        "artifactory.password",
                        artifactory.getPassword(),
                        errors,
                        context.isDryrun()));
                break;
            case NONE:
                errors.configuration(RB.$("validation_value_cannot_be", "artifactory." + artifactory.getName() + ".authorization", "NONE"));
                context.getLogger().debug(RB.$("validation.disabled.error"));
                artifactory.disable();
                break;
        }

        validateTimeout(artifactory);

        for (ArtifactoryUploader.ArtifactoryRepository repository : artifactory.getRepositories()) {
            if (!repository.isActiveSet()) {
                repository.setActive(artifactory.getActive());
            }
            repository.resolveEnabled(context.getModel().getProject());
        }

        if (artifactory.getRepositories().stream().noneMatch(ArtifactoryUploader.ArtifactoryRepository::isEnabled)) {
            errors.warning(RB.$("validation_artifactory_disabled_repositories", "artifactory." + artifactory.getName()));
            context.getLogger().debug(RB.$("validation.disabled.no.repositories"));
            artifactory.disable();
        }
    }
}
