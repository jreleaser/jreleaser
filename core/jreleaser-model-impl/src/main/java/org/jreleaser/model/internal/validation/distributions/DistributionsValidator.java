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
package org.jreleaser.model.internal.validation.distributions;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Java;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.Packager;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.util.Errors;
import org.jreleaser.util.FileType;
import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static org.jreleaser.model.Constants.KEY_GRAALVM_NAGIVE_IMAGE;
import static org.jreleaser.model.api.release.Releaser.KEY_SKIP_RELEASE_SIGNATURES;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.packagers.AppImagePackagerValidator.validateAppImage;
import static org.jreleaser.model.internal.validation.packagers.AsdfPackagerValidator.validateAsdf;
import static org.jreleaser.model.internal.validation.packagers.BrewPackagerValidator.postValidateBrew;
import static org.jreleaser.model.internal.validation.packagers.BrewPackagerValidator.validateBrew;
import static org.jreleaser.model.internal.validation.packagers.ChocolateyPackagerValidator.postValidateChocolatey;
import static org.jreleaser.model.internal.validation.packagers.ChocolateyPackagerValidator.validateChocolatey;
import static org.jreleaser.model.internal.validation.packagers.DockerPackagerValidator.validateDocker;
import static org.jreleaser.model.internal.validation.packagers.FlatpakPackagerValidator.validateFlatpak;
import static org.jreleaser.model.internal.validation.packagers.GofishPackagerValidator.validateGofish;
import static org.jreleaser.model.internal.validation.packagers.JbangPackagerValidator.postValidateJBang;
import static org.jreleaser.model.internal.validation.packagers.JbangPackagerValidator.validateJbang;
import static org.jreleaser.model.internal.validation.packagers.JibPackagerValidator.validateJib;
import static org.jreleaser.model.internal.validation.packagers.MacportsPackagerValidator.validateMacports;
import static org.jreleaser.model.internal.validation.packagers.ScoopPackagerValidator.validateScoop;
import static org.jreleaser.model.internal.validation.packagers.SdkmanPackagerValidator.postValidateSdkman;
import static org.jreleaser.model.internal.validation.packagers.SdkmanPackagerValidator.validateSdkman;
import static org.jreleaser.model.internal.validation.packagers.SnapPackagerValidator.validateSnap;
import static org.jreleaser.model.internal.validation.packagers.SpecPackagerValidator.validateSpec;
import static org.jreleaser.model.internal.validation.packagers.WingetPackagerValidator.postValidateWinget;
import static org.jreleaser.model.internal.validation.packagers.WingetPackagerValidator.validateWinget;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class DistributionsValidator {
    private DistributionsValidator() {
        // noop
    }

    public static void validateDistributions(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, Distribution> distributions = context.getModel().getDistributions();
        if (!distributions.isEmpty()) context.getLogger().debug("distributions");

        for (Map.Entry<String, Distribution> e : distributions.entrySet()) {
            Distribution distribution = e.getValue();
            if (isBlank(distribution.getName())) {
                distribution.setName(e.getKey());
            }
            if (context.isDistributionIncluded(distribution)) {
                if (mode.validateConfig()) {
                    validateDistribution(context, distribution, errors);
                }
            } else {
                distribution.setActive(Active.NEVER);
                distribution.resolveEnabled(context.getModel().getProject());
            }
        }

        if (mode.validateConfig()) {
            postValidateBrew(context, errors);
            postValidateJBang(context, errors);
            postValidateSdkman(context, errors);
        }
    }

    private static void validateDistribution(JReleaserContext context, Distribution distribution, Errors errors) {
        context.getLogger().debug("distribution.{}", distribution.getName());

        resolveActivatable(context, distribution,
            listOf("distributions." + distribution.getName(), "distributions"),
            "ALWAYS");
        if (!distribution.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!selectArtifactsByPlatform(context, distribution)) {
            distribution.setActive(Active.NEVER);
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            errors.warning(RB.$("WARNING.validation.distribution.no.artifacts", distribution.getName()));
            distribution.disable();
            return;
        }

        if (isBlank(distribution.getName())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "distribution.name"));
            context.getLogger().debug(RB.$("validation.disabled.error"));
            distribution.disable();
            return;
        }
        if (null == distribution.getType()) {
            errors.configuration(RB.$("validation_must_not_be_null", "distribution." + distribution.getName() + ".type"));
            distribution.disable();
            return;
        }
        if (null == distribution.getStereotype()) {
            distribution.setStereotype(context.getModel().getProject().getStereotype());
        }
        if (isBlank(distribution.getExecutable().getName())) {
            distribution.getExecutable().setName(distribution.getName());
        }
        if (isBlank(distribution.getExecutable().getWindowsExtension())) {
            switch (distribution.getType()) {
                case BINARY:
                case NATIVE_PACKAGE:
                case FLAT_BINARY:
                    distribution.getExecutable().setWindowsExtension("exe");
                    break;
                default:
                    distribution.getExecutable().setWindowsExtension("bat");
            }
        }

        if (isJavaDistribution(distribution)) {
            context.getLogger().debug("distribution.{}.java", distribution.getName());
            if (!validateJava(context, distribution, errors)) {
                return;
            }
        }

        // validate distribution type
        if (!distribution.getJava().isEnabled() && isJavaDistribution(distribution)) {
            errors.configuration(RB.$("validation_distributions_java",
                "distribution." + distribution.getName() + ".type",
                distribution.getType(),
                "distribution." + distribution.getName() + ".java",
                "project.java"));
            return;
        }

        if (null == distribution.getArtifacts() || distribution.getArtifacts().isEmpty()) {
            errors.configuration(RB.$("validation_is_empty", "distribution." + distribution.getName() + ".artifacts"));
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            distribution.disable();
            return;
        }

        List<String> tags = new ArrayList<>();
        tags.addAll(context.getModel().getProject().getTags());
        tags.addAll(distribution.getTags());
        distribution.setTags(tags);

        int i = 0;
        for (Artifact artifact : distribution.getArtifacts()) {
            if (artifact.isActiveAndSelected()) {
                validateArtifact(context, distribution, artifact, i++, errors);
                if (distribution.getExtraProperties().containsKey(KEY_SKIP_RELEASE_SIGNATURES) &&
                    !artifact.getExtraProperties().containsKey(KEY_SKIP_RELEASE_SIGNATURES)) {
                    artifact.getExtraProperties().put(KEY_SKIP_RELEASE_SIGNATURES,
                        distribution.getExtraProperties().get(KEY_SKIP_RELEASE_SIGNATURES));
                }
            }
        }

        // validate artifact.platform is unique
        Map<String, List<Artifact>> byPlatform = distribution.getArtifacts().stream()
            .filter(Artifact::isActiveAndSelected)
            .collect(groupingBy(artifact -> isBlank(artifact.getPlatform()) ? "<nil>" : artifact.getPlatform()));
        // check platforms by extension
        byPlatform.forEach((p, artifacts) -> {
            String platform = "<nil>".equals(p) ? "no" : p;
            artifacts.stream()
                .collect(groupingBy(artifact -> FileType.getType(artifact.getPath())))
                .forEach((ext, matches) -> {
                    if (matches.size() > 1) {
                        errors.configuration(RB.$("validation_distributions_multiple",
                            "distribution." + distribution.getName(), platform, ext));
                    }
                });
        });

        validateAppImage(context, distribution, distribution.getAppImage(), errors);
        validateAsdf(context, distribution, distribution.getAsdf(), errors);
        validateBrew(context, distribution, distribution.getBrew(), errors);
        validateChocolatey(context, distribution, distribution.getChocolatey(), errors);
        validateDocker(context, distribution, distribution.getDocker(), errors);
        validateFlatpak(context, distribution, distribution.getFlatpak(), errors);
        validateGofish(context, distribution, distribution.getGofish(), errors);
        validateJbang(context, distribution, distribution.getJbang(), errors);
        validateJib(context, distribution, distribution.getJib(), errors);
        validateMacports(context, distribution, distribution.getMacports(), errors);
        validateScoop(context, distribution, distribution.getScoop(), errors);
        validateSdkman(context, distribution, distribution.getSdkman(), errors);
        validateSnap(context, distribution, distribution.getSnap(), errors);
        validateSpec(context, distribution, distribution.getSpec(), errors);
        validateWinget(context, distribution, distribution.getWinget(), errors);
    }

    private static boolean selectArtifactsByPlatform(JReleaserContext context, Distribution distribution) {
        boolean activeArtifacts = false;
        for (Artifact artifact : distribution.getArtifacts()) {
            if (artifact.resolveActiveAndSelected(context)) {
                activeArtifacts = true;
            }
        }
        return activeArtifacts;
    }

    private static boolean validateJava(JReleaserContext context, Distribution distribution, Errors errors) {
        Project project = context.getModel().getProject();

        Java projectJava = project.getLanguages().getJava();
        Java distributionJava = distribution.getJava();
        if (!distributionJava.isEnabledSet() && projectJava.isSet()) {
            distributionJava.setEnabled(projectJava.isSet());
        }
        if (!distributionJava.isEnabledSet()) {
            distributionJava.setEnabled(distributionJava.isSet());
        }

        if (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.NATIVE_PACKAGE) {
            distributionJava.setEnabled(false);
        }

        if (!distributionJava.isEnabled()) return true;

        if (isBlank(distributionJava.getArtifactId())) {
            distributionJava.setArtifactId(distribution.getName());
        }
        if (isBlank(distributionJava.getGroupId())) {
            distributionJava.setGroupId(projectJava.getGroupId());
        }
        if (isBlank(distributionJava.getVersion())) {
            distributionJava.setVersion(projectJava.getVersion());
        }
        if (isBlank(distributionJava.getMainModule())) {
            distributionJava.setMainModule(projectJava.getMainModule());
        }
        if (isBlank(distributionJava.getMainClass())) {
            distributionJava.setMainClass(projectJava.getMainClass());
        }

        if (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.BINARY) {
            return true;
        }

        if (distributionJava.getOptions().isEmpty()) {
            distributionJava.setOptions(projectJava.getOptions());
        } else {
            distributionJava.addOptions(projectJava.getOptions());
        }

        distributionJava.getJvmOptions().merge(distributionJava.getOptions());
        distributionJava.getJvmOptions().merge(projectJava.getJvmOptions());
        distributionJava.getEnvironmentVariables().merge(projectJava.getEnvironmentVariables());

        boolean valid = true;
        // TODO: activate in 2.0.0
        // if (isBlank(distribution.getJava().getVersion())) {
        //     errors.warning(RB.$("validation_is_missing", "distribution." + distribution.getName() + ".java.version"));
        //     valid = false;
        // }
        if (isBlank(distributionJava.getGroupId())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "distribution." + distribution.getName() + ".java.groupId"));
            valid = false;
        }
        if (!distributionJava.isMultiProjectSet()) {
            distributionJava.setMultiProject(projectJava.isMultiProject());
        }

        // validate distribution type
        if (!isJavaDistribution(distribution)) {
            errors.configuration(RB.$("validation_distributions_java_types",
                "distribution." + distribution.getName() + ".type",
                org.jreleaser.model.api.distributions.Distribution.JAVA_DISTRIBUTION_TYPES.stream()
                    .map(org.jreleaser.model.Distribution.DistributionType::name)
                    .collect(joining(", "))));
            valid = false;
        }

        return valid;
    }

    private static void validateArtifact(JReleaserContext context, Distribution distribution, Artifact artifact, int index, Errors errors) {
        if (null == artifact) {
            errors.configuration(RB.$("validation_is_null", "distribution." + distribution.getName() + ".artifact[" + index + "]"));
            return;
        }
        if (isBlank(artifact.getPath())) {
            errors.configuration(RB.$("validation_must_not_be_null", "distribution." + distribution.getName() + ".artifact[" + index + "].path"));
        }
        if (isNotBlank(artifact.getPlatform()) && !PlatformUtils.isSupported(artifact.getPlatform().trim())) {
            context.getLogger().warn(RB.$("validation_distributions_platform",
                distribution.getName(), index, artifact.getPlatform(), lineSeparator(),
                PlatformUtils.getSupportedOsNames(), lineSeparator(), PlatformUtils.getSupportedOsArchs()));
        }
    }

    public static void validateArtifactPlatforms(Distribution distribution, Packager<?> packager,
                                                 List<Artifact> candidateArtifacts, Errors errors) {
        // validate distribution type
        if (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.BINARY ||
            distribution.getType() == org.jreleaser.model.Distribution.DistributionType.FLAT_BINARY ||
            distribution.getType() == org.jreleaser.model.Distribution.DistributionType.JLINK ||
            distribution.getType() == org.jreleaser.model.Distribution.DistributionType.NATIVE_PACKAGE) {
            // ensure all artifacts define a platform

            AtomicBoolean universal = new AtomicBoolean();

            String noPlatform = "<nil>";
            Map<String, List<Artifact>> byPlatform = candidateArtifacts.stream()
                .peek(artifact -> {
                    if ((distribution.getType() == org.jreleaser.model.Distribution.DistributionType.BINARY ||
                        distribution.getType() == org.jreleaser.model.Distribution.DistributionType.FLAT_BINARY) &&
                        artifact.extraPropertyIsTrue("universal")) {
                        universal.compareAndSet(false, true);
                    }
                })
                .collect(groupingBy(artifact -> isBlank(artifact.getPlatform()) ? noPlatform : artifact.getPlatform()));

            if (byPlatform.containsKey(noPlatform) && !universal.get()) {
                errors.configuration(RB.$("validation_distributions_platform_check",
                    distribution.getName(), distribution.getType(), packager.getType()));
            }

            if (byPlatform.keySet().stream()
                .noneMatch(packager::supportsPlatform) && !universal.get()) {
                errors.warning(RB.$("WARNING.validation.packager.no.artifacts", distribution.getName(),
                    packager.getType(), packager.getSupportedFileExtensions(distribution.getType())));
                packager.disable();
            }
        }
    }

    public static void postValidateDistributions(JReleaserContext context, Errors errors) {
        context.getLogger().debug("distributions");
        Map<String, Distribution> distributions = context.getModel().getDistributions();

        for (Map.Entry<String, Distribution> e : distributions.entrySet()) {
            Distribution distribution = e.getValue();
            if (distribution.isEnabled()) {
                postValidateDistribution(context, distribution, errors);
            }
        }
    }

    private static void postValidateDistribution(JReleaserContext context, Distribution distribution, Errors errors) {
        context.getLogger().debug("distribution.{}", distribution.getName());

        postValidateChocolatey(context, distribution, distribution.getChocolatey(), errors);
        postValidateWinget(context, distribution, distribution.getWinget(), errors);
    }

    public static boolean isJavaDistribution(Distribution distribution) {
        return isGraalVMDistribution(distribution) ||
            org.jreleaser.model.api.distributions.Distribution.JAVA_DISTRIBUTION_TYPES.contains(distribution.getType());
    }

    public static boolean isGraalVMDistribution(Distribution distribution) {
        return distribution.getType() == org.jreleaser.model.Distribution.DistributionType.BINARY &&
            isTrue(distribution.getExtraProperties().get(KEY_GRAALVM_NAGIVE_IMAGE));
    }
}
