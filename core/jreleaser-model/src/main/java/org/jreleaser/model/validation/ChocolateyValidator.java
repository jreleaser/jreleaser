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
import org.jreleaser.model.Chocolatey;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static org.jreleaser.model.validation.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class ChocolateyValidator extends Validator {
    public static void validateChocolatey(JReleaserContext context, Distribution distribution, Chocolatey tool, List<String> errors) {
        JReleaserModel model = context.getModel();

        if (!tool.isEnabledSet() && model.getPackagers().getChocolatey().isEnabledSet()) {
            tool.setEnabled(model.getPackagers().getChocolatey().isEnabled());
        }
        if (!tool.isEnabled()) return;

        validateCommitAuthor(tool, model.getPackagers().getChocolatey());
        validateOwner(tool.getBucket(), model.getPackagers().getChocolatey().getBucket());
        validateTemplate(context, distribution, tool, model.getPackagers().getChocolatey(), errors);
        mergeExtraProperties(tool, model.getPackagers().getChocolatey());

        if (isBlank(tool.getUsername())) {
            tool.setUsername(model.getRelease().getGitService().getOwner());
        }
        if (!tool.isRemoteBuildSet() && model.getPackagers().getChocolatey().isRemoteBuildSet()) {
            tool.setRemoteBuild(model.getPackagers().getChocolatey().isRemoteBuild());
        }

        if (isBlank(tool.getBucket().getName())) {
            tool.getBucket().setName(distribution.getName() + "-chocolatey-bucket");
        }
        tool.getBucket().setBasename(distribution.getName() + "-chocolatey-bucket");
        if (isBlank(tool.getBucket().getUsername())) {
            tool.getBucket().setUsername(model.getPackagers().getChocolatey().getBucket().getUsername());
        }
        if (isBlank(tool.getBucket().getToken())) {
            tool.getBucket().setToken(model.getPackagers().getChocolatey().getBucket().getToken());
        }

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
