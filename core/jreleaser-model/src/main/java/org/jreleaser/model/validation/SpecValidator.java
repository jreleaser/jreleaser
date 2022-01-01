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
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Spec;
import org.jreleaser.util.Errors;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Spec.SKIP_SPEC;
import static org.jreleaser.model.validation.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.model.validation.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 0.9.1
 */
public abstract class SpecValidator extends Validator {
    public static void validateSpec(JReleaserContext context, Distribution distribution, Spec tool, Errors errors) {
        JReleaserModel model = context.getModel();
        Spec parentTool = model.getPackagers().getSpec();

        if (!tool.isActiveSet() && parentTool.isActiveSet()) {
            tool.setActive(parentTool.getActive());
        }
        if (!tool.resolveEnabled(context.getModel().getProject(), distribution)) {
            tool.disable();
            return;
        }
        GitService service = model.getRelease().getGitService();
        if (!service.isReleaseSupported()) {
            tool.disable();
            return;
        }

        context.getLogger().debug("distribution.{}.spec", distribution.getName());

        if (isBlank(tool.getRelease())) {
            tool.setRelease(parentTool.getRelease());
        }
        if (isBlank(tool.getRelease())) {
            tool.setRelease("1");
        }

        try {
            Integer.parseInt(tool.getRelease());
            tool.setRelease(tool.getRelease() + "%{?dist}");
        } catch (NumberFormatException ignored) {
            // ok?
        }

        if (tool.getRequires().isEmpty()) {
            tool.setRequires(parentTool.getRequires());
        }
        if (tool.getRequires().isEmpty()) {
            tool.setRequires(Collections.singletonList("java"));
        }

        validateCommitAuthor(tool, parentTool);
        Spec.SpecRepository repository = tool.getRepository();
        repository.resolveEnabled(model.getProject());
        validateTap(context, distribution, repository, parentTool.getRepository(), "spec.repository");
        validateTemplate(context, distribution, tool, parentTool, errors);
        mergeExtraProperties(tool, parentTool);
        validateContinueOnError(tool, parentTool);
        validateArtifactPlatforms(context, distribution, tool, errors);

        if (isBlank(tool.getPackageName())) {
            tool.setPackageName(parentTool.getPackageName());
            if (isBlank(tool.getPackageName())) {
                tool.setPackageName(distribution.getName());
            }
        }

        List<Artifact> candidateArtifacts = distribution.getArtifacts().stream()
            .filter(Artifact::isActive)
            .filter(artifact -> tool.getSupportedExtensions().stream().anyMatch(ext -> artifact.getPath().endsWith(ext)))
            .filter(artifact -> tool.supportsPlatform(artifact.getPlatform()))
            .filter(artifact -> !isTrue(artifact.getExtraProperties().get(SKIP_SPEC)))
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
