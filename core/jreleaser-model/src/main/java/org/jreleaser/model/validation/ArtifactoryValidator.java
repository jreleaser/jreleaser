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
import org.jreleaser.model.Active;
import org.jreleaser.model.Artifactory;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public abstract class ArtifactoryValidator extends Validator {
    public static void validateArtifactory(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().debug("artifactory");
        Map<String, Artifactory> artifactory = context.getModel().getUpload().getArtifactory();

        for (Map.Entry<String, Artifactory> e : artifactory.entrySet()) {
            e.getValue().setName(e.getKey());
            validateArtifactory(context, mode, e.getValue(), errors);
        }
    }

    private static void validateArtifactory(JReleaserContext context, JReleaserContext.Mode mode, Artifactory artifactory, Errors errors) {
        context.getLogger().debug("artifactory.{}", artifactory.getName());

        if (!artifactory.isActiveSet()) {
            artifactory.setActive(Active.NEVER);
        }
        if (!artifactory.resolveEnabled(context.getModel().getProject()) || mode != JReleaserContext.Mode.FULL) {
            return;
        }

        if (!artifactory.isArtifacts() && !artifactory.isFiles() && !artifactory.isSignatures()) {
            artifactory.disable();
            return;
        }

        if (artifactory.getRepositories().isEmpty()) {
            errors.configuration(RB.$("validation_artifactory_no_repositories", "artifactory." + artifactory.getName()));
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
                break;
        }

        validateTimeout(artifactory);

        for (Artifactory.ArtifactoryRepository repository : artifactory.getRepositories()) {
            if (!repository.isActiveSet()) {
                repository.setActive(artifactory.getActive());
            }
            repository.resolveEnabled(context.getModel().getProject());
        }

        if (artifactory.getRepositories().stream().noneMatch(Artifactory.ArtifactoryRepository::isEnabled)) {
            errors.warning(RB.$("validation_artifactory_disabled_repositories", "artifactory." + artifactory.getName()));
            artifactory.disable();
        }
    }
}
