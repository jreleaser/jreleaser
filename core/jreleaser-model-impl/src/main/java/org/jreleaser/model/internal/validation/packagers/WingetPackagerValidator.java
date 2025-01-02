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
import org.jreleaser.model.api.packagers.WingetPackager.Installer;
import org.jreleaser.model.api.packagers.WingetPackager.Installer.Scope;
import org.jreleaser.model.api.packagers.WingetPackager.Installer.UpgradeBehavior;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.WingetPackager;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.model.internal.release.Releaser;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Errors;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.api.packagers.WingetPackager.Installer.Mode.SILENT;
import static org.jreleaser.model.api.packagers.WingetPackager.Installer.Mode.SILENT_WITH_PROGRESS;
import static org.jreleaser.model.internal.validation.common.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.internal.validation.common.TemplateValidator.validateTemplate;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateCommitAuthor;
import static org.jreleaser.model.internal.validation.common.Validator.validateContinueOnError;
import static org.jreleaser.model.internal.validation.distributions.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class WingetPackagerValidator {
    private static final Pattern PATTERN_PACKAGE_IDENTIFIER = Pattern.compile("^[^\\.\\s\\\\/:\\*\\?\"<>\\|\\x01-\\x1f]{1,32}(\\.[^\\.\\s\\\\/:\\*\\?\"<>\\|\\x01-\\x1f]{1,32}){1,3}$");
    private static final Pattern PATTERN_PACKAGE_VERSION = Pattern.compile("^[^\\\\/:\\*\\?\"<>\\|\\x01-\\x1f]+$");
    private static final Pattern PATTERN_LOCALE = Pattern.compile("^([a-zA-Z]{2,3}|[iI]-[a-zA-Z]+|[xX]-[a-zA-Z]{1,8})(-[a-zA-Z]{1,8})*$");
    private static final Pattern PATTERN_MINIMUM_OS_VERSION = Pattern.compile("^(0|[1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])(\\.(0|[1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])){0,3}$");

    private WingetPackagerValidator() {
        // noop
    }

    public static void validateWinget(JReleaserContext context, Distribution distribution, WingetPackager packager, Errors errors) {
        context.getLogger().debug("distribution.{}." + packager.getType(), distribution.getName());
        JReleaserModel model = context.getModel();
        WingetPackager parentPackager = model.getPackagers().getWinget();

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
        } else if (candidateArtifacts.size() > 1 &&
            distribution.getType() == org.jreleaser.model.Distribution.DistributionType.NATIVE_PACKAGE) {
            errors.configuration(RB.$("validation_packager_multiple_artifacts", "distribution." + distribution.getName() + ".winget"));
            context.getLogger().debug(RB.$("validation.disabled.multiple.artifacts"));
            errors.warning(RB.$("WARNING.validation.packager.multiple.artifacts", distribution.getName(),
                packager.getType(), candidateArtifacts.stream()
                    .map(Artifact::getPath)
                    .collect(toList())));
            packager.disable();
            return;
        }

        validateCommitAuthor(packager, parentPackager);
        WingetPackager.WingetRepository repository = packager.getRepository();
        Validator.validateRepository(context, distribution, repository, parentPackager.getRepository(), "winget.repository");
        if (isBlank(packager.getRepository().getName())) {
            packager.getRepository().setName("winget-" + distribution.getName());
        }
        packager.getRepository().setTapName("winget-" + distribution.getName());
        validateTemplate(context, distribution, packager, parentPackager, errors);
        mergeExtraProperties(packager, parentPackager);
        validateContinueOnError(packager, parentPackager);
        if (isBlank(packager.getDownloadUrl())) {
            packager.setDownloadUrl(parentPackager.getDownloadUrl());
        }
        validateArtifactPlatforms(distribution, packager, candidateArtifacts, errors);

        if (isBlank(packager.getDefaultLocale())) {
            packager.setDefaultLocale(parentPackager.getDefaultLocale());
        }
        if (isBlank(packager.getDefaultLocale())) {
            packager.setDefaultLocale("en-US");
        }
        if (!PATTERN_LOCALE.matcher(packager.getDefaultLocale()).matches()) {
            errors.configuration(RB.$("validation_is_invalid",
                "distribution." + distribution.getName() + ".winget.defaultLocale", packager.getDefaultLocale()));
        }

        if (isBlank(packager.getMoniker())) {
            packager.setMoniker(parentPackager.getMoniker());
        }
        if (isBlank(packager.getMoniker())) {
            packager.setMoniker(distribution.getExecutable().getName());
        }

        if (isBlank(packager.getMinimumOsVersion())) {
            packager.setMinimumOsVersion(parentPackager.getMinimumOsVersion());
        }
        if (isBlank(packager.getMinimumOsVersion())) {
            packager.setMinimumOsVersion("10.0.0.0");
        }
        if (!PATTERN_MINIMUM_OS_VERSION.matcher(packager.getMinimumOsVersion()).matches()) {
            errors.configuration(RB.$("validation_is_invalid",
                "distribution." + distribution.getName() + ".winget.minimumOsVersion", packager.getMinimumOsVersion()));
        }

        validateWingetPackage(context, distribution, packager, parentPackager, errors);
        validateWingetPublisher(context, distribution, packager, parentPackager, errors);
        validateWingetInstaller(context, distribution, packager, parentPackager, errors);

        if (isBlank(packager.getProductCode())) {
            packager.setProductCode(parentPackager.getProductCode());
        }
        if (isBlank(packager.getProductCode())) {
            packager.setProductCode(packager.getPackage().getName() + " {{projectVersion}}");
        }

        if (isBlank(packager.getAuthor())) {
            packager.setAuthor(parentPackager.getAuthor());
        }

        List<String> tags = new ArrayList<>();
        tags.addAll(context.getModel().getProject().getTags());
        tags.addAll(parentPackager.getTags());
        tags.addAll(packager.getTags());
        packager.setTags(tags);
    }

    private static void validateWingetPackage(JReleaserContext context, Distribution distribution, WingetPackager packager, WingetPackager parentPackager, Errors errors) {
        if (isBlank(packager.getPackage().getName())) {
            packager.getPackage().setName(parentPackager.getPackage().getName());
        }
        if (isBlank(packager.getPackage().getName())) {
            packager.getPackage().setName(distribution.getName());
        }

        if (isBlank(packager.getPackage().getIdentifier())) {
            packager.getPackage().setIdentifier(parentPackager.getPackage().getIdentifier());
        }
        if (isBlank(packager.getPackage().getIdentifier())) {
            String vendor = context.getModel().getProject().getVendor();
            if (isBlank(vendor)) {
                errors.configuration(RB.$("validation_is_missing",
                    "distribution." + distribution.getName() + ".winget.package.identifier"));
            } else {
                packager.getPackage().setIdentifier(vendor + "." + packager.getPackage().getName());
            }
        }
        if (isNotBlank(packager.getPackage().getIdentifier()) &&
            !PATTERN_PACKAGE_IDENTIFIER.matcher(packager.getPackage().getIdentifier()).matches()) {
            errors.configuration(RB.$("validation_is_invalid",
                "distribution." + distribution.getName() + ".winget.package.identifier", packager.getPackage().getIdentifier()));
        }

        if (isBlank(packager.getPackage().getVersion())) {
            packager.getPackage().setVersion(parentPackager.getPackage().getVersion());
        }
        if (isBlank(packager.getPackage().getVersion())) {
            packager.getPackage().setVersion(context.getModel().getProject().getResolvedVersion());
        }
        if (!PATTERN_PACKAGE_VERSION.matcher(packager.getPackage().getVersion()).matches()) {
            errors.configuration(RB.$("validation_is_invalid",
                "distribution." + distribution.getName() + ".winget.package.version", packager.getPackage().getVersion()));
        }

        if (isBlank(packager.getPackage().getUrl())) {
            packager.getPackage().setUrl(parentPackager.getPackage().getUrl());
        }
        if (isBlank(packager.getPackage().getUrl())) {
            packager.getPackage().setUrl(context.getModel().getProject().getLinks().getHomepage());
        }
    }

    private static void validateWingetPublisher(JReleaserContext context, Distribution distribution, WingetPackager packager, WingetPackager parentPackager, Errors errors) {
        if (isBlank(packager.getPublisher().getName())) {
            packager.getPublisher().setName(parentPackager.getPublisher().getName());
        }
        if (isBlank(packager.getPublisher().getName())) {
            packager.getPublisher().setName(context.getModel().getProject().getVendor());
        }
        if (isBlank(packager.getPublisher().getName())) {
            errors.configuration(RB.$("validation_is_missing",
                "distribution." + distribution.getName() + ".winget.publisher.name"));
        }

        if (isBlank(packager.getPublisher().getUrl())) {
            packager.getPublisher().setUrl(parentPackager.getPublisher().getUrl());
        }
        if (isBlank(packager.getPublisher().getUrl())) {
            packager.getPublisher().setUrl(context.getModel().getProject().getLinks().getHomepage());
        }

        if (isBlank(packager.getPublisher().getSupportUrl())) {
            packager.getPublisher().setSupportUrl(parentPackager.getPublisher().getSupportUrl());
        }
        if (isBlank(packager.getPublisher().getSupportUrl())) {
            packager.getPublisher().setSupportUrl(context.getModel().getProject().getLinks().getBugTracker());
        }
    }

    private static void validateWingetInstaller(JReleaserContext context, Distribution distribution, WingetPackager packager, WingetPackager parentPackager, Errors errors) {
        if (packager.getInstaller().getModes().isEmpty()) {
            packager.getInstaller().setModes(parentPackager.getInstaller().getModes());
        }

        if (packager.getInstaller().getModes().isEmpty()) {
            packager.getInstaller().getModes().add(SILENT);
            packager.getInstaller().getModes().add(SILENT_WITH_PROGRESS);
        }

        if (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE ||
            distribution.getType() == org.jreleaser.model.Distribution.DistributionType.BINARY ||
            distribution.getType() == org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY ||
            distribution.getType() == org.jreleaser.model.Distribution.DistributionType.JLINK) {
            packager.getInstaller().setType(Installer.Type.ZIP);
            packager.getInstaller().getModes().clear();
            packager.getInstaller().setUpgradeBehavior((UpgradeBehavior) null);
            packager.getInstaller().setScope((Scope) null);
        }

        packager.getInstaller().getDependencies().merge(parentPackager.getInstaller().getDependencies());

        int count = 0;
        for (WingetPackager.PackageDependency pd : packager.getInstaller().getDependencies().getPackageDependencies()) {
            int index = count++;

            if (isBlank(pd.getPackageIdentifier())) {
                errors.configuration(RB.$("validation_must_not_be_blank",
                    "distribution." + distribution.getName() + ".winget.installer.dependencies.packageDependencies[" + index + "].packageIdentifier"));
            }

            if (isNotBlank(pd.getPackageIdentifier()) &&
                !PATTERN_PACKAGE_IDENTIFIER.matcher(pd.getPackageIdentifier()).matches()) {
                errors.configuration(RB.$("validation_is_invalid",
                    "distribution." + distribution.getName() + ".winget.installer.dependencies.packageDependencies[" + index + "].packageIdentifier",
                    pd.getPackageIdentifier()));
            }

            if (isNotBlank(pd.getMinimumVersion()) &&
                !PATTERN_PACKAGE_VERSION.matcher(pd.getMinimumVersion()).matches()) {
                errors.configuration(RB.$("validation_is_invalid",
                    "distribution." + distribution.getName() + ".winget.installer.dependencies.packageDependencies[" + index + "].minimumVersion",
                    pd.getMinimumVersion()));
            }
        }
    }

    public static void postValidateWinget(JReleaserContext context, Distribution distribution, WingetPackager packager, Errors errors) {
        if (!packager.isEnabled()) return;
        context.getLogger().debug("distribution.{}.winget", distribution.getName());

        Project project = context.getModel().getProject();

        if (isBlank(project.getLinks().getLicense())) {
            errors.configuration(RB.$("ERROR_project_no_license_url"));
        }

        if (isBlank(packager.getAuthor()) && !project.getAuthors().isEmpty()) {
            packager.setAuthor(project.getAuthors().get(0));
        }
    }
}

