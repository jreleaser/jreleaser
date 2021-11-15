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

import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Macports;
import org.jreleaser.model.MacportsRepository;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.Errors;

import java.util.Collections;
import java.util.Set;

import static org.jreleaser.model.validation.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.model.validation.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;

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
        MacportsRepository repository = tool.getRepository();
        repository.resolveEnabled(model.getProject());
        validateTap(context, distribution, repository, parentTool.getRepository(), "macports.repository");
        validateTemplate(context, distribution, tool, parentTool, errors);
        mergeExtraProperties(tool, parentTool);
        validateContinueOnError(tool, parentTool);
        validateArtifactPlatforms(context, distribution, tool, errors);

        // activate rmd160 checksum
        Set<String> fileExtensions = tool.getSupportedExtensions();
        long count = distribution.getArtifacts().stream()
            .filter(Artifact::isActive)
            .filter(artifact -> fileExtensions.stream().anyMatch(ext -> artifact.getPath().endsWith(ext)))
            .filter(artifact -> tool.supportsPlatform(artifact.getPlatform()))
            .count();
        if (count > 0) {
            context.getModel().getChecksum().getAlgorithms().add(Algorithm.RMD160);
        }
    }
}
