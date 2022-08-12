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
import org.jreleaser.model.AppImage;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Stereotype;
import org.jreleaser.util.Errors;

import java.util.List;

import static org.jreleaser.model.validation.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.model.validation.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public abstract class AppImageValidator extends Validator {
    public static void validateAppImage(JReleaserContext context, JReleaserContext.Mode mode, Distribution distribution, AppImage packager, Errors errors) {
        JReleaserModel model = context.getModel();
        AppImage parentPackager = model.getPackagers().getAppImage();

        if (!packager.isActiveSet() && parentPackager.isActiveSet()) {
            packager.setActive(parentPackager.getActive());
        }
        if (!packager.resolveEnabled(context.getModel().getProject(), distribution)) return;
        GitService service = model.getRelease().getGitService();
        if (!service.isReleaseSupported()) {
            packager.disable();
            return;
        }

        List<Artifact> candidateArtifacts = packager.resolveCandidateArtifacts(context, distribution);
        if (candidateArtifacts.size() == 0) {
            packager.setActive(Active.NEVER);
            packager.disable();
            return;
        } else if (candidateArtifacts.stream()
            .filter(artifact -> isBlank(artifact.getPlatform()))
            .count() > 1) {
            errors.configuration(RB.$("validation_packager_multiple_artifacts", "distribution." + distribution.getName() + ".appImage"));
            packager.disable();
            return;
        }

        if (isBlank(packager.getComponentId()) && isNotBlank(parentPackager.getComponentId())) {
            packager.setComponentId(parentPackager.getComponentId());
        }
        if (isBlank(packager.getComponentId())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "distribution." + distribution.getName() + ".appImage.componentId"));
        }

        if (packager.getCategories().isEmpty()) {
            packager.setCategories(parentPackager.getCategories());
        }
        if (packager.getCategories().isEmpty()) {
            errors.configuration(RB.$("validation_is_empty", "distribution." + distribution.getName() + ".appImage.categories"));
        }

        if (!packager.isRequiresTerminalSet() && parentPackager.isRequiresTerminalSet()) {
            packager.setRequiresTerminal(parentPackager.isRequiresTerminal());
        }
        if (distribution.getStereotype() == Stereotype.CLI) {
            packager.setRequiresTerminal(true);
        }

        if (isBlank(packager.getDeveloperName())) {
            packager.setDeveloperName(parentPackager.getDeveloperName());
        }

        if (packager.getScreenshots().isEmpty()) {
            packager.setScreenshots(parentPackager.getScreenshots());
        }
        if (packager.getScreenshots().isEmpty()) {
            errors.configuration(RB.$("validation_is_empty", "distribution." + distribution.getName() + ".appImage.screenshots"));
        }
        validateScreenshots(context, mode, packager.getScreenshots(), errors, "distribution." + distribution.getName() + ".appImage");

        if (isBlank(packager.getRepository().getName())) {
            packager.getRepository().setName(distribution.getName() + "-appimage");
        }
        packager.getRepository().setTapName(distribution.getName() + "-appimage");

        if (distribution.getStereotype() != Stereotype.CLI && distribution.getStereotype() != Stereotype.DESKTOP) {
            errors.configuration(RB.$("validation_stereotype_invalid",
                "distribution." + distribution.getName() + ".stereotype",
                distribution.getStereotype(),
                listOf(Stereotype.CLI, Stereotype.DESKTOP)));
        }

        validateCommitAuthor(packager, parentPackager);
        AppImage.AppImageRepository repository = packager.getRepository();
        repository.resolveEnabled(model.getProject());
        validateTap(context, distribution, repository, parentPackager.getRepository(), "appImage.repository");
        validateTemplate(context, distribution, packager, parentPackager, errors);
        mergeExtraProperties(packager, parentPackager);
        validateContinueOnError(packager, parentPackager);
        if (isBlank(packager.getDownloadUrl())) {
            packager.setDownloadUrl(parentPackager.getDownloadUrl());
        }
        validateArtifactPlatforms(context, distribution, packager, candidateArtifacts, errors);
    }
}
