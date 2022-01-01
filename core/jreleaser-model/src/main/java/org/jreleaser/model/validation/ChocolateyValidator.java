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
import org.jreleaser.model.Chocolatey;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.util.Errors;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Chocolatey.CHOCOLATEY_API_KEY;
import static org.jreleaser.model.Chocolatey.DEFAULT_CHOCOLATEY_PUSH_URL;
import static org.jreleaser.model.Chocolatey.SKIP_CHOCOLATEY;
import static org.jreleaser.model.validation.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.model.validation.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class ChocolateyValidator extends Validator {
    public static void validateChocolatey(JReleaserContext context, Distribution distribution, Chocolatey tool, Errors errors) {
        JReleaserModel model = context.getModel();
        Chocolatey parentTool = model.getPackagers().getChocolatey();

        if (!tool.isActiveSet() && parentTool.isActiveSet()) {
            tool.setActive(parentTool.getActive());
        }
        if (!tool.resolveEnabled(context.getModel().getProject(), distribution)) return;
        GitService service = model.getRelease().getGitService();
        if (!service.isReleaseSupported()) {
            tool.disable();
            return;
        }

        context.getLogger().debug("distribution.{}.chocolatey", distribution.getName());

        validateCommitAuthor(tool, parentTool);
        Chocolatey.ChocolateyBucket bucket = tool.getBucket();
        bucket.resolveEnabled(model.getProject());
        validateTap(context, distribution, bucket, parentTool.getBucket(), "chocolatey.bucket");
        validateTemplate(context, distribution, tool, parentTool, errors);
        mergeExtraProperties(tool, parentTool);
        validateContinueOnError(tool, parentTool);

        if (isBlank(tool.getPackageName())) {
            tool.setPackageName(parentTool.getPackageName());
            if (isBlank(tool.getPackageName())) {
                tool.setPackageName(distribution.getName());
            }
        }

        if (isBlank(tool.getUsername())) {
            tool.setUsername(service.getOwner());
        }
        if (!tool.isRemoteBuildSet() && parentTool.isRemoteBuildSet()) {
            tool.setRemoteBuild(parentTool.isRemoteBuild());
        }

        if (isBlank(tool.getTitle())) {
            tool.setTitle(parentTool.getTitle());
        }
        if (isBlank(tool.getTitle())) {
            tool.setTitle(model.getProject().getName());
        }

        if (isBlank(tool.getIconUrl())) {
            tool.setIconUrl(parentTool.getIconUrl());
        }

        if (isBlank(tool.getSource())) {
            tool.setSource(parentTool.getSource());
        }
        if (isBlank(tool.getSource())) {
            tool.setSource(DEFAULT_CHOCOLATEY_PUSH_URL);
        }

        if (!tool.isRemoteBuild()) {
            tool.setApiKey(
                checkProperty(context,
                    CHOCOLATEY_API_KEY,
                    "chocolatey.apiKey",
                    tool.getApiKey(),
                    errors,
                    context.isDryrun()));
        }

        validateArtifactPlatforms(context, distribution, tool, errors);

        List<Artifact> candidateArtifacts = distribution.getArtifacts().stream()
            .filter(Artifact::isActive)
            .filter(artifact -> tool.getSupportedExtensions().stream().anyMatch(ext -> artifact.getPath().endsWith(ext)))
            .filter(artifact -> tool.supportsPlatform(artifact.getPlatform()))
            .filter(artifact -> !isTrue(artifact.getExtraProperties().get(SKIP_CHOCOLATEY)))
            .collect(toList());

        if (candidateArtifacts.size() == 0) {
            tool.setActive(Active.NEVER);
            tool.disable();
        } else if (candidateArtifacts.size() > 1) {
            errors.configuration(RB.$("validation_tool_multiple_artifacts", "distribution." + distribution.getName() + ".spec"));
            tool.disable();
        }
    }
}
