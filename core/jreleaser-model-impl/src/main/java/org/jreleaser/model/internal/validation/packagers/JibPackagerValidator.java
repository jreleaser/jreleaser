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
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.JibConfiguration;
import org.jreleaser.model.internal.packagers.JibPackager;
import org.jreleaser.model.internal.packagers.JibSpec;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.DefaultVersions;
import org.jreleaser.util.Errors;
import org.jreleaser.util.PlatformUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;
import static org.jreleaser.model.Constants.DOCKER_IO;
import static org.jreleaser.model.Constants.LABEL_OCI_IMAGE_DESCRIPTION;
import static org.jreleaser.model.Constants.LABEL_OCI_IMAGE_LICENSES;
import static org.jreleaser.model.Constants.LABEL_OCI_IMAGE_REVISION;
import static org.jreleaser.model.Constants.LABEL_OCI_IMAGE_TITLE;
import static org.jreleaser.model.Constants.LABEL_OCI_IMAGE_URL;
import static org.jreleaser.model.Constants.LABEL_OCI_IMAGE_VERSION;
import static org.jreleaser.model.internal.validation.common.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.internal.validation.common.TemplateValidator.validateTemplate;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateCommitAuthor;
import static org.jreleaser.model.internal.validation.common.Validator.validateContinueOnError;
import static org.jreleaser.model.internal.validation.distributions.DistributionsValidator.isGraalVMDistribution;
import static org.jreleaser.model.internal.validation.distributions.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.TAR;
import static org.jreleaser.util.FileType.TAR_GZ;
import static org.jreleaser.util.FileType.TAR_XZ;
import static org.jreleaser.util.FileType.TGZ;
import static org.jreleaser.util.FileType.TXZ;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.6.0
 */
public final class JibPackagerValidator {
    private static final Set<String> EXTENSIONS = setOf(
        TAR_GZ.extension(),
        TAR_XZ.extension(),
        TGZ.extension(),
        TXZ.extension(),
        TAR.extension(),
        ZIP.extension());

    private JibPackagerValidator() {
        // noop
    }

    public static void validateJib(JReleaserContext context, Distribution distribution, JibPackager packager, Errors errors) {
        String element = "distribution." + distribution.getName() + ".jib";
        context.getLogger().debug(element);
        JReleaserModel model = context.getModel();
        Project project = model.getProject();
        JibPackager parentPackager = model.getPackagers().getJib();

        resolveActivatable(context, packager, "distributions." + distribution.getName() + "." + packager.getType(), parentPackager);
        if (!packager.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        List<Artifact> candidateArtifacts = packager.resolveCandidateArtifacts(context, distribution);
        if (candidateArtifacts.isEmpty()) {
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            errors.warning(RB.$("WARNING.validation.packager.no.artifacts", distribution.getName(),
                packager.getType(), packager.getSupportedFileExtensions(distribution.getType())));
            packager.disable();
            return;
        }

        // check specs for active status
        for (JibSpec spec : packager.getSpecs().values()) {
            if (!spec.isActiveSet() && packager.isActiveSet()) {
                spec.setActive(packager.getActive());
            }
            resolveActivatable(context, packager, "distributions." + distribution.getName() + "." + packager.getType() + "." + spec.getName(), "NEVER");
            spec.resolveEnabled(context.getModel().getProject());
        }

        validateTemplate(context, distribution, packager, parentPackager, errors);

        validateCommitAuthor(packager, parentPackager);
        JibPackager.JibRepository repository = packager.getPackagerRepository();
        if (!repository.isVersionedSubfoldersSet()) {
            repository.setVersionedSubfolders(parentPackager.getPackagerRepository().isVersionedSubfolders());
        }
        Validator.validateRepository(context, distribution, repository, parentPackager.getRepositoryTap(), "jib.repository");
        if (isBlank(repository.getName())) {
            repository.setName(project.getName() + "-jib");
        }

        mergeExtraProperties(packager, parentPackager);
        validateContinueOnError(packager, parentPackager);
        if (isBlank(packager.getDownloadUrl())) {
            packager.setDownloadUrl(parentPackager.getDownloadUrl());
        }

        if (isBlank(packager.getVersion())) {
            packager.setVersion(DefaultVersions.getInstance().getJibVersion());
        }

        if (isBlank(packager.getCreationTime())) {
            packager.setCreationTime(parentPackager.getCreationTime());
        }
        if (isBlank(packager.getUser())) {
            packager.setUser(parentPackager.getUser());
        }
        if (null == packager.getFormat()) {
            packager.setFormat(parentPackager.getFormat());
        }
        if (null == packager.getFormat()) {
            packager.setFormat(org.jreleaser.model.api.packagers.JibConfiguration.Format.DOCKER);
        }
        if (isBlank(packager.getWorkingDirectory())) {
            packager.setWorkingDirectory(parentPackager.getWorkingDirectory());
        }
        if (isBlank(packager.getWorkingDirectory())) {
            packager.setWorkingDirectory("/workspace");
        }
        if (packager.getVolumes().isEmpty()) {
            packager.setVolumes(parentPackager.getVolumes());
        }
        if (packager.getExposedPorts().isEmpty()) {
            packager.setExposedPorts(parentPackager.getExposedPorts());
        }

        if (isBlank(packager.getBaseImage())) {
            packager.setBaseImage(parentPackager.getBaseImage());
        }
        validateBaseImage(distribution, packager, errors);

        if (packager.getImageNames().isEmpty()) {
            packager.setImageNames(parentPackager.getImageNames());
        }
        if (packager.getImageNames().isEmpty()) {
            packager.addImageName("{{repoOwner}}/{{distributionName}}:{{tagName}}");
        }

        if (context.getModel().getProject().isSnapshot()) {
            // find the 1st image that ends with :{{tagName}}
            Optional<String> imageName = packager.getImageNames().stream()
                .filter(n -> n.endsWith(":{{tagName}}") || n.endsWith(":{{ tagName }}"))
                .findFirst();
            // use the first finding or the first imageName
            packager.setImageNames(singleton(imageName.orElse(packager.getImageNames().iterator().next())));
        }

        Map<String, String> environment = new LinkedHashMap<>();
        environment.putAll(parentPackager.getEnvironment());
        environment.putAll(packager.getEnvironment());
        packager.setEnvironment(environment);

        Map<String, String> labels = new LinkedHashMap<>();
        labels.putAll(parentPackager.getLabels());
        labels.putAll(packager.getLabels());
        packager.setLabels(labels);

        if (!packager.getLabels().containsKey(LABEL_OCI_IMAGE_TITLE)) {
            packager.getLabels().put(LABEL_OCI_IMAGE_TITLE, "{{distributionName}}");
        }
        validateLabels(packager);

        validateArtifactPlatforms(distribution, packager, candidateArtifacts, errors);

        validateRegistries(context, packager, parentPackager, errors, element);

        for (Map.Entry<String, JibSpec> e : packager.getSpecs().entrySet()) {
            JibSpec spec = e.getValue();
            if (isBlank(spec.getName())) {
                spec.setName(e.getKey());
            }
            validateJibSpec(context, distribution, spec, packager, errors);
        }
    }

    public static void validateJibSpec(JReleaserContext context, Distribution distribution, JibSpec spec, JibPackager jib, Errors errors) {
        if (!spec.isEnabled()) return;

        String element = "distribution." + distribution.getName() + ".jib.spec." + spec.getName();
        context.getLogger().debug(element);

        validateTemplate(context, distribution, spec, jib, errors);
        mergeExtraProperties(spec, jib);

        validateBaseImage(distribution, spec, errors);

        if (spec.getImageNames().isEmpty()) {
            spec.addImageName("{{repoOwner}}/{{distributionName}}-{{jibSpecName}}:{{tagName}}");
        }

        if (context.getModel().getProject().isSnapshot()) {
            // find the 1st image that ends with :{{tagName}}
            Optional<String> imageName = spec.getImageNames().stream()
                .filter(n -> n.endsWith(":{{tagName}}") || n.endsWith(":{{ tagName }}"))
                .findFirst();
            spec.setImageNames(singleton(imageName.orElse("{{repoOwner}}/{{distributionName}}-{{jibSpecName}}:{{tagName}}")));
        }

        Map<String, String> environment = new LinkedHashMap<>();
        environment.putAll(jib.getEnvironment());
        environment.putAll(spec.getEnvironment());
        spec.setEnvironment(environment);

        Map<String, String> labels = new LinkedHashMap<>();
        labels.putAll(jib.getLabels());
        labels.putAll(spec.getLabels());
        if (!spec.getLabels().containsKey(LABEL_OCI_IMAGE_TITLE)) {
            labels.put(LABEL_OCI_IMAGE_TITLE, jib.getLabels().get(LABEL_OCI_IMAGE_TITLE) + "-{{jibSpecName}}");
        }
        spec.setLabels(labels);
        validateLabels(spec);

        if (isBlank(spec.getCreationTime())) {
            spec.setCreationTime(jib.getCreationTime());
        }
        if (isBlank(spec.getUser())) {
            spec.setUser(jib.getUser());
        }
        if (null == spec.getFormat()) {
            spec.setFormat(jib.getFormat());
        }
        if (null == spec.getFormat()) {
            spec.setFormat(org.jreleaser.model.api.packagers.JibConfiguration.Format.DOCKER);
        }
        if (isBlank(spec.getWorkingDirectory())) {
            spec.setWorkingDirectory(jib.getWorkingDirectory());
        }
        if (isBlank(spec.getWorkingDirectory())) {
            spec.setWorkingDirectory("/workspace");
        }
        if (spec.getVolumes().isEmpty()) {
            spec.setVolumes(jib.getVolumes());
        }
        if (spec.getExposedPorts().isEmpty()) {
            spec.setExposedPorts(jib.getExposedPorts());
        }

        validateRegistries(context, spec, jib, errors, element);

        long artifactCount = distribution.getArtifacts().stream()
            .filter(Artifact::isActiveAndSelected)
            .count();

        if (artifactCount > 1 && spec.getMatchers().isEmpty()) {
            errors.configuration(RB.$("validation_must_not_be_empty", element + ".matchers"));
        }

        if (null == spec.getArtifact()) {
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            spec.disable();
        }
    }

    private static void validateBaseImage(Distribution distribution, JibConfiguration jib, Errors errors) {
        if (isBlank(jib.getBaseImage())) {
            if (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY ||
                distribution.getType() == org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR ||
                isGraalVMDistribution(distribution)) {
                // TODO: remove in 2.0.0
                if (isBlank(distribution.getJava().getVersion())) {
                    errors.configuration(RB.$("validation_is_missing", "distribution." + distribution.getName() + ".java.version"));
                    return;
                }
                int version = Integer.parseInt(distribution.getJava().getVersion());
                boolean ltsmts = version == 8 || version % 2 == 1;
                jib.setBaseImage("azul/zulu-openjdk-alpine:{{distributionJavaVersion}}" + (ltsmts ? "-jre" : ""));
            } else if (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.JLINK) {
                if (isAlpineCompatible(distribution, jib)) {
                    jib.setBaseImage("alpine:latest");
                } else {
                    jib.setBaseImage("ubuntu:latest");
                }
            } else {
                jib.setBaseImage("scratch");
            }
        } else if (jib instanceof JibSpec) {
            JibSpec spec = (JibSpec) jib;
            distribution.getArtifacts().stream()
                .filter(artifact -> artifact.getPath().endsWith(".zip"))
                .filter(spec::matches)
                .findFirst()
                .ifPresent(spec::setArtifact);
        }
    }

    private static boolean isAlpineCompatible(Distribution distribution, JibConfiguration jib) {
        List<Artifact> artifacts = distribution.getArtifacts().stream()
            .filter(artifact -> EXTENSIONS.stream().anyMatch(ext -> artifact.getPath().endsWith(ext)))
            .collect(Collectors.toList());

        if (jib instanceof JibSpec) {
            JibSpec spec = (JibSpec) jib;
            Optional<Artifact> artifact = artifacts.stream()
                .filter(spec::matches)
                .findFirst();
            if (artifact.isPresent()) {
                spec.setArtifact(artifact.get());
                return PlatformUtils.isAlpineLinux(artifact.get().getPlatform());
            }

            return false;
        }

        return artifacts.stream()
            .anyMatch(artifact -> PlatformUtils.isAlpineLinux(artifact.getPlatform()));
    }

    private static void validateLabels(JibConfiguration self) {
        if (!self.getLabels().containsKey(LABEL_OCI_IMAGE_DESCRIPTION)) {
            self.getLabels().put(LABEL_OCI_IMAGE_DESCRIPTION, "{{projectDescription}}");
        }
        if (!self.getLabels().containsKey(LABEL_OCI_IMAGE_URL)) {
            self.getLabels().put(LABEL_OCI_IMAGE_URL, "{{projectWebsite}}");
        }
        if (!self.getLabels().containsKey(LABEL_OCI_IMAGE_LICENSES)) {
            self.getLabels().put(LABEL_OCI_IMAGE_LICENSES, "{{projectLicense}}");
        }
        if (!self.getLabels().containsKey(LABEL_OCI_IMAGE_VERSION)) {
            self.getLabels().put(LABEL_OCI_IMAGE_VERSION, "{{projectVersion}}");
        }
        if (!self.getLabels().containsKey(LABEL_OCI_IMAGE_REVISION)) {
            self.getLabels().put(LABEL_OCI_IMAGE_REVISION, "{{commitFullHash}}");
        }
    }

    private static void validateRegistries(JReleaserContext context, JibConfiguration self, JibConfiguration other, Errors errors, String element) {
        JReleaserModel model = context.getModel();

        Set<JibConfiguration.Registry> registries = new LinkedHashSet<>();
        registries.addAll(self.getRegistries());
        registries.addAll(other.getRegistries());
        self.setRegistries(registries);

        if (self.getRegistries().isEmpty()) {
            context.getLogger().warn(RB.$("validation_jib_no_registries", element));
            return;
        }

        for (JibConfiguration.Registry registry : registries) {
            BaseReleaser<?, ?> releaser = model.getRelease().getReleaser();
            String registryName = registry.getName();

            if (isBlank(registry.getServer())) {
                registry.setServer(DOCKER_IO);
            }

            registry.setToPassword(
                checkProperty(context,
                    resolveKeys(element, registryName, ".topassword"),
                    "registry." + registryName + ".toPassword",
                    registry.getToPassword(),
                    (String) null));

            registry.setFromPassword(
                checkProperty(context,
                    resolveKeys(element, registryName, ".frompassword"),
                    "registry." + registryName + ".fromPassword",
                    registry.getFromPassword(),
                    (String) null));

            registry.setPassword(
                checkProperty(context,
                    resolveKeys(element, registryName, ".password"),
                    "registry." + registryName + ".password",
                    registry.getPassword(),
                    (String) null));

            if (isBlank(registry.getPassword()) && isBlank(registry.getToPassword())) {
                errors.configuration(RB.$("validation_must_not_be_blank", element +
                    ".registry." + registryName + ".toPassword"));
            }

            registry.setToUsername(
                checkProperty(context,
                    resolveKeys(element, registryName, ".tousername"),
                    "registry." + registryName + ".toUsername",
                    registry.getToUsername(),
                    (String) null));

            registry.setFromUsername(
                checkProperty(context,
                    resolveKeys(element, registryName, ".fromusername"),
                    "registry." + registryName + ".fromUsername",
                    registry.getFromUsername(),
                    (String) null));

            registry.setUsername(
                checkProperty(context,
                    resolveKeys(element, registryName, ".username"),
                    "registry." + registryName + ".username",
                    registry.getUsername(),
                    (String) null));

            if (isBlank(registry.getToUsername()) && isBlank(registry.getFromUsername()) && isBlank(registry.getUsername())) {
                if (isNotBlank(registry.getToPassword())) {
                    registry.setToUsername(releaser.getUsername());
                } else {
                    registry.setUsername(releaser.getUsername());
                }
            }
        }
    }

    private static List<String> resolveKeys(String element, String registryName, String property) {
        return listOf(
            element + "." + registryName + property,
            "jib." + registryName + property);
    }
}
