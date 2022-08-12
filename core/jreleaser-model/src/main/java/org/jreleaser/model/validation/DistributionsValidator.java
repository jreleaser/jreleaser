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
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Packager;
import org.jreleaser.model.Project;
import org.jreleaser.util.Errors;
import org.jreleaser.util.FileType;
import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.jreleaser.model.GitService.KEY_SKIP_RELEASE_SIGNATURES;
import static org.jreleaser.model.validation.AppImageValidator.validateAppImage;
import static org.jreleaser.model.validation.AsdfValidator.validateAsdf;
import static org.jreleaser.model.validation.BrewValidator.postValidateBrew;
import static org.jreleaser.model.validation.BrewValidator.validateBrew;
import static org.jreleaser.model.validation.ChocolateyValidator.postValidateChocolatey;
import static org.jreleaser.model.validation.ChocolateyValidator.validateChocolatey;
import static org.jreleaser.model.validation.DockerValidator.validateDocker;
import static org.jreleaser.model.validation.GofishValidator.validateGofish;
import static org.jreleaser.model.validation.JbangValidator.postValidateJBang;
import static org.jreleaser.model.validation.JbangValidator.validateJbang;
import static org.jreleaser.model.validation.MacportsValidator.validateMacports;
import static org.jreleaser.model.validation.ScoopValidator.validateScoop;
import static org.jreleaser.model.validation.SdkmanValidator.postValidateSdkman;
import static org.jreleaser.model.validation.SdkmanValidator.validateSdkman;
import static org.jreleaser.model.validation.SnapValidator.validateSnap;
import static org.jreleaser.model.validation.SpecValidator.validateSpec;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class DistributionsValidator extends Validator {
    public static void validateDistributions(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        if (!mode.validateConfig()) {
            return;
        }

        context.getLogger().debug("distributions");
        Map<String, Distribution> distributions = context.getModel().getDistributions();

        for (Map.Entry<String, Distribution> e : distributions.entrySet()) {
            Distribution distribution = e.getValue();
            if (isBlank(distribution.getName())) {
                distribution.setName(e.getKey());
            }
            if (context.isDistributionIncluded(distribution)) {
                validateDistribution(context, mode, distribution, errors);
            } else {
                distribution.setActive(Active.NEVER);
                distribution.resolveEnabled(context.getModel().getProject());
            }
        }

        postValidateBrew(context, errors);
        postValidateJBang(context, errors);
        postValidateSdkman(context, errors);
    }

    private static void validateDistribution(JReleaserContext context, JReleaserContext.Mode mode, Distribution distribution, Errors errors) {
        context.getLogger().debug("distribution.{}", distribution.getName());

        if (!distribution.isActiveSet()) {
            distribution.setActive(Active.ALWAYS);
        }
        if (!distribution.resolveEnabled(context.getModel().getProject())) return;

        if (!selectArtifactsByPlatform(context, distribution)) {
            distribution.setActive(Active.NEVER);
            distribution.disable();
            return;
        }

        if (isBlank(distribution.getName())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "distribution.name"));
            return;
        }
        if (null == distribution.getType()) {
            errors.configuration(RB.$("validation_must_not_be_null", "distribution." + distribution.getName() + ".type"));
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
                case NATIVE_IMAGE:
                    distribution.getExecutable().setWindowsExtension("exe");
                    break;
                default:
                    distribution.getExecutable().setWindowsExtension("bat");
            }
        }

        if (Distribution.JAVA_DISTRIBUTION_TYPES.contains(distribution.getType())) {
            context.getLogger().debug("distribution.{}.java", distribution.getName());
            if (!validateJava(context, distribution, errors)) {
                return;
            }
        }

        // validate distribution type
        if (!distribution.getJava().isEnabled() && Distribution.JAVA_DISTRIBUTION_TYPES.contains(distribution.getType())) {
            errors.configuration(RB.$("validation_distributions_java",
                "distribution." + distribution.getName() + ".type",
                distribution.getType(),
                "distribution." + distribution.getName() + ".java",
                "project.java"));
            return;
        }

        if (null == distribution.getArtifacts() || distribution.getArtifacts().isEmpty()) {
            errors.configuration(RB.$("validation_is_empty", "distribution." + distribution.getName() + ".artifacts"));
            return;
        }

        List<String> tags = new ArrayList<>();
        tags.addAll(context.getModel().getProject().getTags());
        tags.addAll(distribution.getTags());
        distribution.setTags(tags);

        int i = 0;
        for (Artifact artifact : distribution.getArtifacts()) {
            if (artifact.isActive()) {
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
            .filter(Artifact::isActive)
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

        validateAppImage(context, mode, distribution, distribution.getAppImage(), errors);
        validateAsdf(context, distribution, distribution.getAsdf(), errors);
        validateBrew(context, distribution, distribution.getBrew(), errors);
        validateChocolatey(context, distribution, distribution.getChocolatey(), errors);
        validateDocker(context, distribution, distribution.getDocker(), errors);
        validateGofish(context, distribution, distribution.getGofish(), errors);
        validateJbang(context, distribution, distribution.getJbang(), errors);
        validateMacports(context, distribution, distribution.getMacports(), errors);
        validateScoop(context, distribution, distribution.getScoop(), errors);
        validateSdkman(context, distribution, distribution.getSdkman(), errors);
        validateSnap(context, distribution, distribution.getSnap(), errors);
        validateSpec(context, distribution, distribution.getSpec(), errors);
    }

    private static boolean selectArtifactsByPlatform(JReleaserContext context, Distribution distribution) {
        boolean activeArtifacts = false;
        for (Artifact artifact : distribution.getArtifacts()) {
            if (context.isPlatformSelected(artifact)) {
                artifact.activate();
                activeArtifacts = true;
            }
        }
        return activeArtifacts;
    }

    private static boolean validateJava(JReleaserContext context, Distribution distribution, Errors errors) {
        Project project = context.getModel().getProject();

        if (!distribution.getJava().isEnabledSet() && project.getJava().isSet()) {
            distribution.getJava().setEnabled(project.getJava().isSet());
        }
        if (!distribution.getJava().isEnabledSet()) {
            distribution.getJava().setEnabled(distribution.getJava().isSet());
        }

        if (distribution.getType() == Distribution.DistributionType.NATIVE_PACKAGE) {
            distribution.getJava().setEnabled(false);
        }

        if (!distribution.getJava().isEnabled()) return true;

        if (isBlank(distribution.getJava().getArtifactId())) {
            distribution.getJava().setArtifactId(distribution.getName());
        }
        if (isBlank(distribution.getJava().getGroupId())) {
            distribution.getJava().setGroupId(project.getJava().getGroupId());
        }
        if (isBlank(distribution.getJava().getVersion())) {
            distribution.getJava().setVersion(project.getJava().getVersion());
        }
        if (isBlank(distribution.getJava().getMainModule())) {
            distribution.getJava().setMainModule(project.getJava().getMainModule());
        }
        if (isBlank(distribution.getJava().getMainClass())) {
            distribution.getJava().setMainClass(project.getJava().getMainClass());
        }

        if (distribution.getType() == Distribution.DistributionType.NATIVE_IMAGE) {
            return true;
        }

        if (isBlank(distribution.getJava().getGroupId())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "distribution." + distribution.getName() + ".java.groupId"));
        }
        if (!distribution.getJava().isMultiProjectSet()) {
            distribution.getJava().setMultiProject(project.getJava().isMultiProject());
        }

        // validate distribution type
        if (!Distribution.JAVA_DISTRIBUTION_TYPES.contains(distribution.getType())) {
            errors.configuration(RB.$("validation_distributions_java_types",
                "distribution." + distribution.getName() + ".type",
                Distribution.JAVA_DISTRIBUTION_TYPES.stream()
                    .map(Distribution.DistributionType::name)
                    .collect(Collectors.joining(", "))));
            return false;
        }

        return true;
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
                distribution.getName(), index, artifact.getPlatform(), System.lineSeparator(),
                PlatformUtils.getSupportedOsNames(), System.lineSeparator(), PlatformUtils.getSupportedOsArchs()));
        }
    }

    public static void validateArtifactPlatforms(JReleaserContext context, Distribution distribution, Packager packager,
                                                 List<Artifact> candidateArtifacts, Errors errors) {
        // validate distribution type
        if (distribution.getType() == Distribution.DistributionType.BINARY ||
            distribution.getType() == Distribution.DistributionType.JLINK ||
            distribution.getType() == Distribution.DistributionType.NATIVE_IMAGE ||
            distribution.getType() == Distribution.DistributionType.NATIVE_PACKAGE) {
            // ensure all artifacts define a platform

            AtomicBoolean universal = new AtomicBoolean();

            String noPlatform = "<nil>";
            Map<String, List<Artifact>> byPlatform = candidateArtifacts.stream()
                .peek(artifact -> {
                    if (distribution.getType() == Distribution.DistributionType.BINARY &&
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
                context.getLogger().warn(RB.$("validation_distributions_disable",
                    distribution.getName(), packager.getType()));
                packager.disable();
            }
        }
    }

    public static void postValidateDistributions(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        if (!mode.validateConfig()) {
            return;
        }

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
    }
}
