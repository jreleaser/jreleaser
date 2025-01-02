/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
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
package org.jreleaser.model.internal.validation.packagers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Icon;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.FlatpakPackager;
import org.jreleaser.model.internal.release.Releaser;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Errors;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Constants.SKIP_OPENJDK;
import static org.jreleaser.model.api.packagers.FlatpakPackager.SKIP_FLATPAK;
import static org.jreleaser.model.internal.validation.common.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.internal.validation.common.TemplateValidator.validateTemplate;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateCommitAuthor;
import static org.jreleaser.model.internal.validation.common.Validator.validateContinueOnError;
import static org.jreleaser.model.internal.validation.common.Validator.validateIcons;
import static org.jreleaser.model.internal.validation.common.Validator.validateScreenshots;
import static org.jreleaser.model.internal.validation.distributions.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isFalse;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class FlatpakPackagerValidator {
    private FlatpakPackagerValidator() {
        // noop
    }

    public static void validateFlatpak(JReleaserContext context, Distribution distribution, FlatpakPackager packager, Errors errors) {
        context.getLogger().debug("distribution.{}." + packager.getType(), distribution.getName());
        JReleaserModel model = context.getModel();
        FlatpakPackager parentPackager = model.getPackagers().getFlatpak();

        resolveActivatable(context, packager, "distributions." + distribution.getName() + "." + packager.getType(), parentPackager);
        if (!packager.resolveEnabled(context.getModel().getProject(), distribution)) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }
        Releaser<?> service = model.getRelease().getReleaser();
        if (!service.isReleaseSupported()) {
            context.getLogger().debug(RB.$("validation.disabled.release"));
            packager.disable();
            return;
        }

        List<Artifact> candidateArtifacts = packager.resolveCandidateArtifacts(context, distribution);
        if (candidateArtifacts.isEmpty()) {
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            errors.warning(RB.$("WARNING.validation.packager.no.artifacts", distribution.getName(),
                packager.getType(), packager.getSupportedFileExtensions(distribution.getType())));
            packager.disable();
            return;
        } else if (candidateArtifacts.stream()
            .filter(artifact -> isBlank(artifact.getPlatform()))
            .count() > 1) {
            errors.configuration(RB.$("validation_packager_multiple_artifacts", "distribution." + distribution.getName() + ".flatpak"));
            context.getLogger().debug(RB.$("validation.disabled.multiple.artifacts"));
            errors.warning(RB.$("WARNING.validation.packager.multiple.artifacts", distribution.getName(),
                packager.getType(), candidateArtifacts.stream()
                    .filter(artifact -> isBlank(artifact.getPlatform()))
                    .map(Artifact::getPath)
                    .collect(toList())));
            packager.disable();
            return;
        }

        if (isBlank(packager.getComponentId()) && isNotBlank(parentPackager.getComponentId())) {
            packager.setComponentId(parentPackager.getComponentId());
        }
        if (isBlank(packager.getComponentId())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "distribution." + distribution.getName() + ".flatpak.componentId"));
        }

        if (packager.getCategories().isEmpty()) {
            packager.setCategories(parentPackager.getCategories());
        }
        if (packager.getCategories().isEmpty()) {
            errors.configuration(RB.$("validation_is_empty", "distribution." + distribution.getName() + ".flatpak.categories"));
        }

        if (isBlank(packager.getDeveloperName())) {
            packager.setDeveloperName(parentPackager.getDeveloperName());
        }

        if (packager.getScreenshots().isEmpty()) {
            packager.setScreenshots(parentPackager.getScreenshots());
        }
        if (packager.getScreenshots().isEmpty()) {
            errors.configuration(RB.$("validation_is_empty", "distribution." + distribution.getName() + ".flatpak.screenshots"));
        }
        validateScreenshots(packager.getScreenshots(), errors, "distribution." + distribution.getName() + ".flatpak");
        packager.getScreenshots().removeIf(screenshot -> isTrue(screenshot.getExtraProperties().get(SKIP_FLATPAK)));
        if (packager.getScreenshots().isEmpty()) {
            errors.configuration(RB.$("validation_is_empty", "distribution." + distribution.getName() + ".flatpak.screenshots"));
        }

        if (packager.getIcons().isEmpty()) {
            packager.setIcons(parentPackager.getIcons());
        }
        if (packager.getIcons().isEmpty()) {
            errors.configuration(RB.$("validation_is_empty", "distribution." + distribution.getName() + ".flatpak.icons"));
        }
        validateIcons(packager.getIcons(), errors, "distribution." + distribution.getName() + ".flatpak", false);
        packager.getIcons().removeIf(icon -> isTrue(icon.getExtraProperties().get(SKIP_FLATPAK)));
        if (packager.getIcons().isEmpty()) {
            errors.configuration(RB.$("validation_is_empty", "distribution." + distribution.getName() + ".flatpak.icons"));
        }
        for (int i = 0; i < packager.getIcons().size(); i++) {
            Icon icon = packager.getIcons().get(i);
            if (null != icon.getWidth() && !icon.getWidth().equals(icon.getHeight())) {
                errors.configuration(RB.$("validation_must_be_equal",
                    "distribution." + distribution.getName() + ".flatpak.icons[" + i + "].width", icon.getWidth(),
                    "distribution." + distribution.getName() + ".flatpak.icons[" + i + "].height", icon.getHeight()));
            }
        }

        if (null == packager.getRuntime()) {
            packager.setRuntime(parentPackager.getRuntime());
        }
        if (null == packager.getRuntime()) {
            errors.configuration(RB.$("validation_is_missing", "distribution." + distribution.getName() + ".flatpak.runtime"));
        }

        if (isBlank(packager.getRuntimeVersion())) {
            packager.setRuntimeVersion(parentPackager.getRuntimeVersion());
        }
        if (isBlank(packager.getRuntimeVersion())) {
            errors.configuration(RB.$("validation_is_missing", "distribution." + distribution.getName() + ".flatpak.runtimeVersion"));
        }

        if (packager.getSdkExtensions().isEmpty()) {
            packager.setSdkExtensions(parentPackager.getSdkExtensions());
        }

        if (packager.getFinishArgs().isEmpty()) {
            packager.setFinishArgs(parentPackager.getFinishArgs());
        }

        if (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY &&
            isFalse(packager.getExtraProperties().get(SKIP_OPENJDK))) {
            packager.getSdkExtensions().add("org.freedesktop.Sdk.Extension.openjdk");
            packager.getFinishArgs().add("--env=PATH=/app/jre/bin:/usr/bin:/app/bin");
            packager.getFinishArgs().add("--env=JAVA_HOME=/app/jre");
        }

        if (distribution.getStereotype() != Stereotype.CLI && distribution.getStereotype() != Stereotype.DESKTOP) {
            errors.configuration(RB.$("validation_stereotype_invalid",
                "distribution." + distribution.getName() + ".stereotype",
                distribution.getStereotype(),
                listOf(Stereotype.CLI, Stereotype.DESKTOP)));
        }

        validateCommitAuthor(packager, parentPackager);
        FlatpakPackager.FlatpakRepository repository = packager.getRepository();
        Validator.validateRepository(context, distribution, repository, parentPackager.getRepository(), "flatpak.repository");
        if (isBlank(repository.getName())) {
            repository.setName(packager.getComponentId());
        }
        repository.setTapName(packager.getComponentId());
        validateTemplate(context, distribution, packager, parentPackager, errors);
        mergeExtraProperties(packager, parentPackager);
        validateContinueOnError(packager, parentPackager);
        if (isBlank(packager.getDownloadUrl())) {
            packager.setDownloadUrl(parentPackager.getDownloadUrl());
        }
        validateArtifactPlatforms(distribution, packager, candidateArtifacts, errors);
    }
}
