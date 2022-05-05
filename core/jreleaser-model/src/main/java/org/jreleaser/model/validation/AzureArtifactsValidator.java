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
package org.jreleaser.model.validation;

import org.jreleaser.model.Active;
import org.jreleaser.model.AzureArtifacts;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.util.Map;

/**
 * @author JIHUN KIM
 * @since 1.1.0
 */
public abstract class AzureArtifactsValidator extends Validator {
    public static void validateAzureArtifacts(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().debug("azureArtifacts");
        Map<String, AzureArtifacts> azureArtifacts = context.getModel().getUpload().getAzureArtifacts();

        for (Map.Entry<String, AzureArtifacts> e : azureArtifacts.entrySet()) {
            e.getValue().setName(e.getKey());
            if (!mode.validateConfig()) {
                validateAzureArtifacts(context, mode, e.getValue(), new Errors());
            } else {
                validateAzureArtifacts(context, mode, e.getValue(), errors);
            }
        }
    }

    private static void validateAzureArtifacts(JReleaserContext context, JReleaserContext.Mode mode,
            AzureArtifacts azureArtifacts, Errors errors) {
        context.getLogger().debug("azureArtifacts.{}", azureArtifacts.getName());

        if (!azureArtifacts.isActiveSet()) {
            azureArtifacts.setActive(Active.NEVER);
        }

        if (!azureArtifacts.resolveEnabled(context.getModel().getProject())) {
            return;
        }

        if (!azureArtifacts.isArtifacts() && !azureArtifacts.isFiles() && !azureArtifacts.isSignatures()) {
            azureArtifacts.disable();
            return;
        }

        azureArtifacts.setHost(
                checkProperty(context,
                        "AZURE_ARTIFACTS_" + Env.toVar(azureArtifacts.getName()) + "_HOST",
                        "azureArtifacts.host",
                        azureArtifacts.getHost(),
                        errors));

        azureArtifacts.setUsername(
                checkProperty(context,
                        "AZURE_ARTIFACTS_" + Env.toVar(azureArtifacts.getName()) + "_USERNAME",
                        "azureArtifacts.username",
                        azureArtifacts.getUsername(),
                        errors,
                        context.isDryrun()));

        azureArtifacts.setPersonalAccessToken(
                checkProperty(context,
                        "AZURE_ARTIFACTS_" + Env.toVar(azureArtifacts.getName()) + "_PERSONAL_ACCESS_TOKEN",
                        "azureArtifacts.personalAccessToken",
                        azureArtifacts.getPersonalAccessToken(),
                        errors,
                        context.isDryrun()));

        azureArtifacts.setProject(
                checkProperty(context,
                        "AZURE_ARTIFACTS_" + Env.toVar(azureArtifacts.getName()) + "_PROJECT",
                        "azureArtifacts.project",
                        azureArtifacts.getProject(),
                        errors));

        azureArtifacts.setOrganization(
                checkProperty(context,
                        "AZURE_ARTIFACTS_" + Env.toVar(azureArtifacts.getName()) + "_ORGANIZATION",
                        "azureArtifacts.organization",
                        azureArtifacts.getOrganization(),
                        errors));

        azureArtifacts.setFeed(
                checkProperty(context,
                        "AZURE_ARTIFACTS_" + Env.toVar(azureArtifacts.getName()) + "_FEED",
                        "azureArtifacts.feed",
                        azureArtifacts.getFeed(),
                        errors));

        azureArtifacts.setPath(
                checkProperty(context,
                        "AZURE_ARTIFACTS_" + Env.toVar(azureArtifacts.getName()) + "_PATH",
                        "azureArtifacts.path",
                        azureArtifacts.getPath(),
                        "{{projectJavaGroupId}}/{{projectJavaArtifactId}}/{{projectVersion}}/{{artifactFile}}"));

        validateTimeout(azureArtifacts);
    }
}
