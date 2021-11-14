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
import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Docker;
import org.jreleaser.model.DockerConfiguration;
import org.jreleaser.model.DockerRepository;
import org.jreleaser.model.DockerSpec;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Project;
import org.jreleaser.model.Registry;
import org.jreleaser.util.Env;
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
import static org.jreleaser.model.Docker.LABEL_OCI_IMAGE_DESCRIPTION;
import static org.jreleaser.model.Docker.LABEL_OCI_IMAGE_LICENSES;
import static org.jreleaser.model.Docker.LABEL_OCI_IMAGE_REVISION;
import static org.jreleaser.model.Docker.LABEL_OCI_IMAGE_TITLE;
import static org.jreleaser.model.Docker.LABEL_OCI_IMAGE_URL;
import static org.jreleaser.model.Docker.LABEL_OCI_IMAGE_VERSION;
import static org.jreleaser.model.validation.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.model.validation.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class DockerValidator extends Validator {
    public static void validateDocker(JReleaserContext context, Distribution distribution, Docker tool, Errors errors) {
        JReleaserModel model = context.getModel();
        Project project = model.getProject();
        Docker parentTool = model.getPackagers().getDocker();

        if (!tool.isActiveSet() && parentTool.isActiveSet()) {
            tool.setActive(parentTool.getActive());
        }
        if (!tool.resolveEnabled(context.getModel().getProject(), distribution)) return;

        String element = "distribution." + distribution.getName() + ".docker";
        context.getLogger().debug(element);

        // check specs for active status
        for (DockerSpec spec : tool.getSpecs().values()) {
            if (!spec.isActiveSet() && tool.isActiveSet()) {
                spec.setActive(tool.getActive());
            }
            spec.resolveEnabled(context.getModel().getProject(), distribution);
        }

        if (tool.getActiveSpecs().isEmpty()) {
            validateTemplate(context, distribution, tool, parentTool, errors);
        }

        validateCommitAuthor(tool, parentTool);
        DockerRepository repository = tool.getRepository();
        repository.resolveEnabled(model.getProject());
        validateOwner(repository, parentTool.getRepository());
        if (isBlank(repository.getBranch())) {
            repository.setBranch(parentTool.getRepository().getBranch());
        }
        if (!repository.isVersionedSubfoldersSet()) {
            repository.setVersionedSubfolders(parentTool.getRepository().isVersionedSubfolders());
        }
        mergeExtraProperties(tool, parentTool);
        validateContinueOnError(tool, parentTool);

        if (isBlank(repository.getName())) {
            repository.setName(project.getName() + "-docker");
        }
        if (isBlank(repository.getUsername())) {
            repository.setUsername(parentTool.getRepository().getUsername());
        }
        if (isBlank(repository.getToken())) {
            repository.setToken(parentTool.getRepository().getToken());
        }

        validateTap(context, distribution, repository, "docker.repository");

        if (isBlank(tool.getBaseImage())) {
            tool.setBaseImage(parentTool.getBaseImage());
        }
        validateBaseImage(distribution, tool);

        if (tool.getImageNames().isEmpty()) {
            tool.setImageNames(parentTool.getImageNames());
        }

        if (tool.getImageNames().isEmpty()) {
            tool.addImageName("{{repoOwner}}/{{distributionName}}:{{tagName}}");
        }

        if (context.getModel().getProject().isSnapshot()) {
            // find the 1st image that ends with :{{tagName}}
            Optional<String> imageName = tool.getImageNames().stream()
                .filter(n -> n.endsWith(":{{tagName}}") || n.endsWith(":{{ tagName }}"))
                .findFirst();
            tool.setImageNames(singleton(imageName.orElse("{{repoOwner}}/{{distributionName}}:{{tagName}}")));
        }

        validateCommands(tool, parentTool);

        Map<String, String> labels = new LinkedHashMap<>();
        labels.putAll(parentTool.getLabels());
        labels.putAll(tool.getLabels());
        tool.setLabels(labels);

        if (!tool.getLabels().containsKey(LABEL_OCI_IMAGE_TITLE)) {
            tool.getLabels().put(LABEL_OCI_IMAGE_TITLE, "{{distributionName}}");
        }
        validateLabels(tool);

        validateArtifactPlatforms(context, distribution, tool, errors);

        validateRegistries(context, tool, parentTool, errors, element);

        if (!tool.isUseLocalArtifactSet() && parentTool.isUseLocalArtifactSet()) {
            tool.setUseLocalArtifact(parentTool.isUseLocalArtifact());
        }
        if (distribution.getType() == Distribution.DistributionType.SINGLE_JAR) {
            tool.setUseLocalArtifact(true);
        }

        for (Map.Entry<String, DockerSpec> e : tool.getSpecs().entrySet()) {
            DockerSpec spec = e.getValue();
            if (isBlank(spec.getName())) {
                spec.setName(e.getKey());
            }
            validateDockerSpec(context, distribution, spec, tool, errors);
        }
    }

    public static void validateDockerSpec(JReleaserContext context, Distribution distribution, DockerSpec spec, Docker docker, Errors errors) {
        if (!spec.isEnabled()) return;

        String element = "distribution." + distribution.getName() + ".docker.spec." + spec.getName();
        context.getLogger().debug(element);

        validateTemplate(context, distribution, spec, docker, errors);
        mergeExtraProperties(spec, docker);

        validateBaseImage(distribution, spec);

        if (spec.getImageNames().isEmpty()) {
            spec.addImageName("{{repoOwner}}/{{distributionName}}-{{dockerSpecName}}:{{tagName}}");
        }

        if (context.getModel().getProject().isSnapshot()) {
            // find the 1st image that ends with :{{tagName}}
            Optional<String> imageName = spec.getImageNames().stream()
                .filter(n -> n.endsWith(":{{tagName}}") || n.endsWith(":{{ tagName }}"))
                .findFirst();
            spec.setImageNames(singleton(imageName.orElse("{{repoOwner}}/{{distributionName}}-{{dockerSpecName}}:{{tagName}}")));
        }

        validateCommands(spec, docker);

        Map<String, String> labels = new LinkedHashMap<>();
        labels.putAll(docker.getLabels());
        labels.putAll(spec.getLabels());
        if (!spec.getLabels().containsKey(LABEL_OCI_IMAGE_TITLE)) {
            labels.put(LABEL_OCI_IMAGE_TITLE, docker.getLabels().get(LABEL_OCI_IMAGE_TITLE) + "-{{dockerSpecName}}");
        }
        spec.setLabels(labels);
        validateLabels(spec);

        validateRegistries(context, spec, docker, errors, element);

        long artifactCount = distribution.getArtifacts().stream()
            .filter(Artifact::isActive)
            .count();

        if (artifactCount > 1 && spec.getMatchers().isEmpty()) {
            errors.configuration(RB.$("validation_must_not_be_empty", element + ".matchers"));
        }

        if (!spec.isUseLocalArtifactSet() && docker.isUseLocalArtifactSet()) {
            spec.setUseLocalArtifact(docker.isUseLocalArtifact());
        }
        if (distribution.getType() == Distribution.DistributionType.SINGLE_JAR) {
            spec.setUseLocalArtifact(true);
        }
    }

    private static void validateBaseImage(Distribution distribution, DockerConfiguration docker) {
        if (isBlank(docker.getBaseImage())) {
            if (distribution.getType() == Distribution.DistributionType.JAVA_BINARY ||
                distribution.getType() == Distribution.DistributionType.SINGLE_JAR) {
                int version = Integer.parseInt(distribution.getJava().getVersion());
                boolean ltsmts = version == 8 || version % 2 == 1;
                docker.setBaseImage("azul/zulu-openjdk-alpine:{{distributionJavaVersion}}" + (ltsmts ? "-jre" : ""));
            } else if (distribution.getType() == Distribution.DistributionType.JLINK) {
                if (isAlpineCompatible(distribution, docker)) {
                    docker.setBaseImage("alpine:latest");
                } else {
                    docker.setBaseImage("ubuntu:latest");
                }
            } else {
                docker.setBaseImage("scratch");
            }
        } else if (docker instanceof DockerSpec) {
            DockerSpec spec = (DockerSpec) docker;
            distribution.getArtifacts().stream()
                .filter(artifact -> artifact.getPath().endsWith(".zip"))
                .filter(spec::matches)
                .findFirst()
                .ifPresent(spec::setArtifact);
        }
    }

    private static boolean isAlpineCompatible(Distribution distribution, DockerConfiguration docker) {
        List<Artifact> artifacts = distribution.getArtifacts().stream()
            .filter(artifact -> artifact.getPath().endsWith(".zip"))
            .collect(Collectors.toList());

        if (docker instanceof DockerSpec) {
            DockerSpec spec = (DockerSpec) docker;
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

    private static void validateCommands(DockerConfiguration self, DockerConfiguration other) {
        if (self.getBuildArgs().isEmpty() && !other.getBuildArgs().isEmpty()) {
            self.setBuildArgs(other.getBuildArgs());
        }

        if (self.getPreCommands().isEmpty() && !other.getPreCommands().isEmpty()) {
            self.setPreCommands(other.getPreCommands());
        }

        if (self.getPostCommands().isEmpty() && !other.getPostCommands().isEmpty()) {
            self.setPostCommands(other.getPostCommands());
        }
    }

    private static void validateLabels(DockerConfiguration self) {
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

    private static void validateRegistries(JReleaserContext context, DockerConfiguration self, DockerConfiguration other, Errors errors, String element) {
        JReleaserModel model = context.getModel();

        Set<Registry> registries = new LinkedHashSet<>();
        registries.addAll(self.getRegistries());
        registries.addAll(other.getRegistries());
        self.setRegistries(registries);

        if (registries.isEmpty()) {
            context.getLogger().warn(RB.$("validation_docker_no_registries", element));
            return;
        }

        for (Registry registry : registries) {
            GitService service = model.getRelease().getGitService();
            String serverName = registry.getServerName();

            registry.setUsername(
                checkProperty(context,
                    "DOCKER_" + Env.toVar(serverName) + "_USERNAME",
                    "registry." + Env.toVar(serverName) + ".username",
                    registry.getUsername(),
                    service.getResolvedUsername()));

            if (isBlank(registry.getRepositoryName())) {
                registry.setRepositoryName(service.getOwner());
            }

            if (isBlank(registry.getUsername())) {
                errors.configuration(RB.$("validation_must_not_be_blank", element +
                    ".registry." + serverName + ".username"));
            }

            registry.setPassword(
                checkProperty(context,
                    "DOCKER_" + Env.toVar(serverName) + "_PASSWORD",
                    "registry." + Env.toVar(serverName) + ".password",
                    registry.getPassword(),
                    errors,
                    context.isDryrun()));
        }
    }
}
