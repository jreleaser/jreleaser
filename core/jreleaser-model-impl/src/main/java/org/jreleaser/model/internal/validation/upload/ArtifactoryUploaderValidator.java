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
import org.jreleaser.model.internal.upload.ArtifactoryUploader;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public final class ArtifactoryUploaderValidator {
    private ArtifactoryUploaderValidator() {
        // noop
    }

    public static void validateArtifactory(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, ArtifactoryUploader> artifactory = context.getModel().getUpload().getArtifactory();
        if (!artifactory.isEmpty()) context.getLogger().debug("upload.artifactory");

        for (Map.Entry<String, ArtifactoryUploader> e : artifactory.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig()) {
                validateArtifactory(context, e.getValue(), errors);
            }
        }
    }

    private static void validateArtifactory(JReleaserContext context, ArtifactoryUploader artifactory, Errors errors) {
        context.getLogger().debug("upload.artifactory.{}", artifactory.getName());

        resolveActivatable(context, artifactory,
            listOf("upload.artifactory." + artifactory.getName(), "upload.artifactory"),
            "NEVER");
        if (!artifactory.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!artifactory.isArtifacts() && !artifactory.isFiles() && !artifactory.isSignatures()) {
            errors.warning(RB.$("WARNING.validation.uploader.no.artifacts", artifactory.getType(), artifactory.getName()));
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

        String baseKey1 = "upload.artifactory." + artifactory.getName();
        String baseKey2 = "upload.artifactory";
        String baseKey3 = "artifactory." + artifactory.getName();
        String baseKey4 = "artifactory";

        artifactory.setHost(
            checkProperty(context,
                listOf(
                    baseKey1 + ".host",
                    baseKey2 + ".host",
                    baseKey3 + ".host",
                    baseKey4 + ".host"),
                baseKey1 + ".host",
                artifactory.getHost(),
                errors));

        switch (artifactory.resolveAuthorization()) {
            case BEARER:
                artifactory.setPassword(
                    checkProperty(context,
                        listOf(
                            baseKey1 + ".password",
                            baseKey2 + ".password",
                            baseKey3 + ".password",
                            baseKey4 + ".password"),
                        baseKey1 + ".password",
                        artifactory.getPassword(),
                        errors,
                        context.isDryrun()));
                break;
            case BASIC:
                artifactory.setUsername(
                    checkProperty(context,
                        listOf(
                            baseKey1 + ".username",
                            baseKey2 + ".username",
                            baseKey3 + ".username",
                            baseKey4 + ".username"),
                        baseKey1 + ".username",
                        artifactory.getUsername(),
                        errors,
                        context.isDryrun()));

                artifactory.setPassword(
                    checkProperty(context,
                        listOf(
                            baseKey1 + ".password",
                            baseKey2 + ".password",
                            baseKey3 + ".password",
                            baseKey4 + ".password"),
                        baseKey1 + ".password",
                        artifactory.getPassword(),
                        errors,
                        context.isDryrun()));
                break;
            case NONE:
                errors.configuration(RB.$("validation_value_cannot_be", baseKey1 + ".authorization", "NONE"));
                context.getLogger().debug(RB.$("validation.disabled.error"));
                artifactory.disable();
                break;
        }

        validateTimeout(artifactory);

        for (ArtifactoryUploader.ArtifactoryRepository repository : artifactory.getRepositories()) {
            resolveActivatable(context, repository, baseKey1 + ".repository", "");
            if (!repository.isActiveSet()) {
                repository.setActive(artifactory.getActive());
            }
            repository.resolveEnabledWithSnapshot(context.getModel().getProject());
        }

        if (artifactory.getRepositories().stream().noneMatch(ArtifactoryUploader.ArtifactoryRepository::isEnabled)) {
            errors.warning(RB.$("validation_artifactory_disabled_repositories", baseKey1));
            context.getLogger().debug(RB.$("validation.disabled.no.repositories"));
            artifactory.disable();
        }
    }
}
