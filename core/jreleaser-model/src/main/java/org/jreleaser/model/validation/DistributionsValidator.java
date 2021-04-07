/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.Tool;
import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.jreleaser.model.validation.BrewValidator.validateBrew;
import static org.jreleaser.model.validation.ChocolateyValidator.validateChocolatey;
import static org.jreleaser.model.validation.DockerValidator.validateDocker;
import static org.jreleaser.model.validation.JbangValidator.validateJbang;
import static org.jreleaser.model.validation.ScoopValidator.validateScoop;
import static org.jreleaser.model.validation.SnapValidator.validateSnap;
import static org.jreleaser.util.StringUtils.getFilenameExtension;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class DistributionsValidator extends Validator {
    public static void validateDistributions(JReleaserContext context, List<String> errors) {
        context.getLogger().debug("distributions");
        Map<String, Distribution> distributions = context.getModel().getDistributions();

        if (distributions.size() == 1) {
            distributions.values().stream()
                .findFirst().ifPresent(distribution -> distribution.setName(context.getModel().getProject().getName()));
        }

        for (Map.Entry<String, Distribution> e : distributions.entrySet()) {
            Distribution distribution = e.getValue();
            if (isBlank(distribution.getName())) {
                distribution.setName(e.getKey());
            }
            validateDistribution(context, distribution, errors);
        }
    }

    private static void validateDistribution(JReleaserContext context, Distribution distribution, List<String> errors) {
        context.getLogger().debug("distribution.{}", distribution.getName());

        if (!distribution.isEnabledSet()) {
            distribution.setEnabled(true);
        }
        if (isBlank(distribution.getName())) {
            errors.add("distribution.name must not be blank");
            return;
        }
        if (null == distribution.getType()) {
            errors.add("distribution." + distribution.getName() + ".type must not be null");
            return;
        }
        if (isBlank(distribution.getExecutable())) {
            distribution.setExecutable(distribution.getName());
        }

        context.getLogger().debug("distribution.{}.java", distribution.getName());
        if (!validateJava(context, distribution, errors)) {
            return;
        }

        // validate distribution type
        if (!distribution.getJava().isEnabled() && Distribution.JAVA_DISTRIBUTION_TYPES.contains(distribution.getType())) {
            errors.add("distribution." + distribution.getName() + ".type is set to " +
                distribution.getType() + " but neither distribution." + distribution.getName() +
                ".java nor project.java have been set");
            return;
        }

        if (null == distribution.getArtifacts() || distribution.getArtifacts().isEmpty()) {
            errors.add("distribution." + distribution.getName() + ".artifacts is empty");
            return;
        }

        List<String> tags = new ArrayList<>();
        tags.addAll(context.getModel().getProject().getTags());
        tags.addAll(distribution.getTags());
        distribution.setTags(tags);

        for (int i = 0; i < distribution.getArtifacts().size(); i++) {
            validateArtifact(context, distribution, distribution.getArtifacts().get(i), i, errors);
        }

        // validate artifact.platform is unique
        Map<String, List<Artifact>> byPlatform = distribution.getArtifacts().stream()
            .collect(groupingBy(artifact -> isBlank(artifact.getPlatform()) ? "<nil>" : artifact.getPlatform()));
        // check platforms by extension
        byPlatform.entrySet().forEach(p -> {
            String platform = "<nil>".equals(p.getKey()) ? "no" : p.getKey();
            p.getValue().stream()
                .collect(groupingBy(artifact -> getFilenameExtension(artifact.getPath())))
                .entrySet().forEach(e -> {
                if (e.getValue().size() > 1) {
                    errors.add("distribution." + distribution.getName() +
                        " has more than one artifact with " + platform +
                        " platform for extension " + e.getValue());
                }
            });
        });

        validateBrew(context, distribution, distribution.getBrew(), errors);
        validateChocolatey(context, distribution, distribution.getChocolatey(), errors);
        validateDocker(context, distribution, distribution.getDocker(), errors);
        validateJbang(context, distribution, distribution.getJbang(), errors);
        validateScoop(context, distribution, distribution.getScoop(), errors);
        validateSnap(context, distribution, distribution.getSnap(), errors);
    }

    private static boolean validateJava(JReleaserContext context, Distribution distribution, List<String> errors) {
        Project project = context.getModel().getProject();

        if (!distribution.getJava().isEnabledSet() && project.getJava().isEnabledSet()) {
            distribution.getJava().setEnabled(project.getJava().isEnabled());
        }
        if (!distribution.getJava().isEnabledSet()) {
            distribution.getJava().setEnabled(distribution.getJava().isSet());
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
        if (isBlank(distribution.getJava().getMainClass())) {
            distribution.getJava().setMainClass(project.getJava().getMainClass());
        }

        if (isBlank(distribution.getJava().getGroupId())) {
            errors.add("distribution." + distribution.getName() + ".java.groupId must not be blank");
        }
        if (!distribution.getJava().isMultiProjectSet()) {
            distribution.getJava().setMultiProject(project.getJava().isMultiProject());
        }

        // validate distribution type
        if (!Distribution.JAVA_DISTRIBUTION_TYPES.contains(distribution.getType())) {
            errors.add("distribution." + distribution.getName() + ".type must be a valid Java distribution type," +
                " one of [" + Distribution.JAVA_DISTRIBUTION_TYPES.stream()
                .map(Distribution.DistributionType::name)
                .collect(Collectors.joining(", ")) + "]");
            return false;
        }

        return true;
    }

    private static void validateArtifact(JReleaserContext context, Distribution distribution, Artifact artifact, int index, List<String> errors) {
        if (null == artifact) {
            errors.add("distribution." + distribution.getName() + ".artifact[" + index + "] is null");
            return;
        }
        if (isBlank(artifact.getPath())) {
            errors.add("distribution." + distribution.getName() + ".artifact[" + index + "].path must not be null");
        }
        if (isNotBlank(artifact.getPlatform()) && !PlatformUtils.isSupported(artifact.getPlatform().trim())) {
            context.getLogger().warn("distribution.{}.artifact[{}].platform ({}) is not supported. Please use `${name}` or `${name}-${arch}` from{}       name = {}{}       arch = {}",
                distribution.getName(), index, artifact.getPlatform(), System.lineSeparator(),
                PlatformUtils.getSupportedOsNames(), System.lineSeparator(), PlatformUtils.getSupportedOsArchs());
        }
    }

    public static void validateArtifactPlatforms(JReleaserContext context, Distribution distribution, Tool tool, List<String> errors) {
        // validate distribution type
        if (distribution.getType() != Distribution.DistributionType.JAVA_BINARY) {
            // ensure all artifacts define a platform

            Set<String> fileExtensions = tool.getSupportedExtensions();
            String noPlatform = "<nil>";
            Map<String, List<Artifact>> byPlatform = distribution.getArtifacts().stream()
                .filter(artifact -> fileExtensions.stream().anyMatch(ext -> artifact.getPath().endsWith(ext)))
                .collect(groupingBy(artifact -> isBlank(artifact.getPlatform()) ? noPlatform : artifact.getPlatform()));

            if (byPlatform.containsKey(noPlatform)) {
                errors.add("distribution." + distribution.getName() +
                    " is of type " + distribution.getType() + " and " + tool.getName() +
                    " requires a explicit platform on each artifact");
            }
        }
    }
}
