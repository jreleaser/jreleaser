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

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.Chocolatey;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Project;
import org.jreleaser.util.Errors;

import java.util.List;

import static org.jreleaser.model.Chocolatey.CHOCOLATEY_API_KEY;
import static org.jreleaser.model.Chocolatey.DEFAULT_CHOCOLATEY_PUSH_URL;
import static org.jreleaser.model.validation.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.model.validation.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class ChocolateyValidator extends Validator {
    public static void validateChocolatey(JReleaserContext context, Distribution distribution, Chocolatey packager, Errors errors) {
        JReleaserModel model = context.getModel();
        Chocolatey parentPackager = model.getPackagers().getChocolatey();

        if (!packager.isActiveSet() && parentPackager.isActiveSet()) {
            packager.setActive(parentPackager.getActive());
        }
        if (!packager.resolveEnabled(context.getModel().getProject(), distribution)) return;
        GitService service = model.getRelease().getGitService();
        if (!service.isReleaseSupported()) {
            packager.disable();
            return;
        }

        context.getLogger().debug("distribution.{}.chocolatey", distribution.getName());

        List<Artifact> candidateArtifacts = packager.resolveCandidateArtifacts(context, distribution);
        if (candidateArtifacts.size() == 0) {
            packager.setActive(Active.NEVER);
            packager.disable();
            return;
        } else if (candidateArtifacts.size() > 1) {
            errors.configuration(RB.$("validation_packager_multiple_artifacts", "distribution." + distribution.getName() + ".chocolatey"));
            packager.disable();
            return;
        }

        validateCommitAuthor(packager, parentPackager);
        Chocolatey.ChocolateyBucket bucket = packager.getBucket();
        bucket.resolveEnabled(model.getProject());
        validateTap(context, distribution, bucket, parentPackager.getBucket(), "chocolatey.bucket");
        validateTemplate(context, distribution, packager, parentPackager, errors);
        mergeExtraProperties(packager, parentPackager);
        validateContinueOnError(packager, parentPackager);
        if (isBlank(packager.getDownloadUrl())) {
            packager.setDownloadUrl(parentPackager.getDownloadUrl());
        }

        if (isBlank(packager.getPackageName())) {
            packager.setPackageName(parentPackager.getPackageName());
            if (isBlank(packager.getPackageName())) {
                packager.setPackageName(distribution.getName());
            }
        }

        if (isBlank(packager.getUsername())) {
            packager.setUsername(service.getOwner());
        }
        if (!packager.isRemoteBuildSet() && parentPackager.isRemoteBuildSet()) {
            packager.setRemoteBuild(parentPackager.isRemoteBuild());
        }

        if (isBlank(packager.getTitle())) {
            packager.setTitle(parentPackager.getTitle());
        }
        if (isBlank(packager.getTitle())) {
            packager.setTitle(model.getProject().getName());
        }

        if (isBlank(packager.getIconUrl())) {
            packager.setIconUrl(parentPackager.getIconUrl());
        }

        if (isBlank(packager.getSource())) {
            packager.setSource(parentPackager.getSource());
        }
        if (isBlank(packager.getSource())) {
            packager.setSource(DEFAULT_CHOCOLATEY_PUSH_URL);
        }

        if (!packager.isRemoteBuild()) {
            packager.setApiKey(
                checkProperty(context,
                    CHOCOLATEY_API_KEY,
                    "chocolatey.apiKey",
                    packager.getApiKey(),
                    errors,
                    context.isDryrun()));
        }

        validateArtifactPlatforms(context, distribution, packager, candidateArtifacts, errors);
    }

    public static void postValidateChocolatey(JReleaserContext context, Distribution distribution, Chocolatey packager, Errors errors) {
        context.getLogger().debug("distribution.{}.chocolatey", distribution.getName());

        JReleaserModel model = context.getModel();
        Project project = model.getProject();

        if (isBlank(project.getLicenseUrl())) {
            errors.configuration(RB.$("ERROR_project_no_license_url"));
        }
    }
}
