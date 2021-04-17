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

import org.jreleaser.model.Distribution;
import org.jreleaser.model.Docker;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Registry;
import org.jreleaser.util.Errors;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
        Docker parentTool = model.getPackagers().getDocker();

        if (!tool.isActiveSet() && parentTool.isActiveSet()) {
            tool.setActive(parentTool.getActive());
        }
        if (!tool.resolveEnabled(context.getModel().getProject(), distribution)) return;
        context.getLogger().debug("distribution.{}.docker", distribution.getName());

        validateTemplate(context, distribution, tool, parentTool, errors);
        mergeExtraProperties(tool, parentTool);

        if (isBlank(tool.getBaseImage())) {
            tool.setBaseImage(parentTool.getBaseImage());
        }
        if (isBlank(tool.getBaseImage())) {
            if (distribution.getType() != Distribution.DistributionType.JLINK) {
                int version = Integer.parseInt(distribution.getJava().getVersion());
                boolean ltsmts = version == 8 || version % 2 == 1;
                tool.setBaseImage("azul/zulu-openjdk-alpine:{{distributionJavaVersion}}" + (ltsmts ? "-jre" : ""));
            } else {
                tool.setBaseImage("alpine:3.5");
            }
        }

        if (tool.getImageNames().isEmpty()) {
            tool.setImageNames(parentTool.getImageNames());
        }

        if (tool.getImageNames().isEmpty()) {
            tool.addImageName("{{repoOwner}}/{{distributionName}}:{{tagName}}");
        }

        if (tool.getBuildArgs().isEmpty() && !parentTool.getBuildArgs().isEmpty()) {
            tool.setBuildArgs(parentTool.getBuildArgs());
        }

        if (tool.getPreCommands().isEmpty() && !parentTool.getPreCommands().isEmpty()) {
            tool.setPreCommands(parentTool.getPreCommands());
        }

        if (tool.getPostCommands().isEmpty() && !parentTool.getPostCommands().isEmpty()) {
            tool.setPostCommands(parentTool.getPostCommands());
        }

        Map<String, String> labels = new LinkedHashMap<>();
        labels.putAll(parentTool.getLabels());
        labels.putAll(tool.getLabels());
        tool.setLabels(labels);

        if (!tool.getLabels().containsKey(LABEL_OCI_IMAGE_TITLE)) {
            tool.getLabels().put(LABEL_OCI_IMAGE_TITLE, "{{distributionName}}");
        }
        if (!tool.getLabels().containsKey(LABEL_OCI_IMAGE_DESCRIPTION)) {
            tool.getLabels().put(LABEL_OCI_IMAGE_DESCRIPTION, "{{projectDescription}}");
        }
        if (!tool.getLabels().containsKey(LABEL_OCI_IMAGE_URL)) {
            tool.getLabels().put(LABEL_OCI_IMAGE_URL, "{{projectWebsite}}");
        }
        if (!tool.getLabels().containsKey(LABEL_OCI_IMAGE_LICENSES)) {
            tool.getLabels().put(LABEL_OCI_IMAGE_LICENSES, "{{projectLicense}}");
        }
        if (!tool.getLabels().containsKey(LABEL_OCI_IMAGE_VERSION)) {
            tool.getLabels().put(LABEL_OCI_IMAGE_VERSION, "{{projectVersion}}");
        }
        if (!tool.getLabels().containsKey(LABEL_OCI_IMAGE_REVISION)) {
            tool.getLabels().put(LABEL_OCI_IMAGE_REVISION, "{{commitFullHash}}");
        }

        validateArtifactPlatforms(context, distribution, tool, errors);

        Set<Registry> registries = new LinkedHashSet<>();
        registries.addAll(tool.getRegistries());
        registries.addAll(parentTool.getRegistries());
        tool.setRegistries(registries);

        if (registries.isEmpty()) {
            context.getLogger().warn("distribution." + distribution.getName() + "." + tool.getName() +
                " does not define any registries. Image publication will be disabled");
            return;
        }

        for (Registry registry : registries) {
            if (isBlank(registry.getUsername())) {
                registry.setUsername(model.getRelease().getGitService().getUsername());
            }
            if (isBlank(registry.getRepositoryName())) {
                registry.setRepositoryName(model.getRelease().getGitService().getOwner());
            }

            if (isBlank(registry.getUsername())) {
                errors.configuration("distribution." + distribution.getName() + "." + tool.getName() +
                    ".registry." + registry.getServerName() + ".username must not be blank");
            }

            registry.setPassword(
                checkProperty(context.getModel().getEnvironment(),
                    "DOCKER_" + registry.getServerName() + "_PASSWORD",
                    "registry." + registry.getServerName() + ".password",
                    registry.getPassword(),
                    errors));
        }
    }
}
