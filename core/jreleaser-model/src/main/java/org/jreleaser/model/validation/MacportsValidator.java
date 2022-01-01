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
import org.jreleaser.model.Macports;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.Errors;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Macports.SKIP_MACPORTS;
import static org.jreleaser.model.validation.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.model.validation.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
public abstract class MacportsValidator extends Validator {
    public static void validateMacports(JReleaserContext context, Distribution distribution, Macports tool, Errors errors) {
        JReleaserModel model = context.getModel();
        Macports parentTool = model.getPackagers().getMacports();

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

        context.getLogger().debug("distribution.{}.macports", distribution.getName());

        if (null == tool.getRevision()) {
            tool.setRevision(parentTool.getRevision());
        }
        if (null == tool.getRevision()) {
            tool.setRevision(0);
        }

        if (tool.getMaintainers().isEmpty()) {
            tool.setMaintainers(parentTool.getMaintainers());
        }
        if (tool.getCategories().isEmpty()) {
            tool.setCategories(parentTool.getCategories());
        }
        if (tool.getCategories().isEmpty()) {
            tool.setCategories(Collections.singletonList("devel"));
        }

        validateCommitAuthor(tool, parentTool);
        Macports.MacportsRepository repository = tool.getRepository();
        repository.resolveEnabled(model.getProject());
        validateTap(context, distribution, repository, parentTool.getRepository(), "macports.repository");
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

        Set<String> fileExtensions = tool.getSupportedExtensions();
        List<Artifact> candidateArtifacts = distribution.getArtifacts().stream()
            .filter(Artifact::isActive)
            .filter(artifact -> fileExtensions.stream().anyMatch(ext -> artifact.getPath().endsWith(ext)))
            .filter(artifact -> tool.supportsPlatform(artifact.getPlatform()))
            .filter(artifact -> !isTrue(artifact.getExtraProperties().get(SKIP_MACPORTS)))
            .collect(toList());

        if (candidateArtifacts.size() == 0) {
            tool.setActive(Active.NEVER);
            tool.disable();
        } else if (candidateArtifacts.size() > 1) {
            errors.configuration(RB.$("validation_tool_multiple_artifacts", "distribution." + distribution.getName() + ".macports"));
            tool.disable();
        } else {
            // activate rmd160 checksum
            context.getModel().getChecksum().getAlgorithms().add(Algorithm.RMD160);
        }
    }
}
