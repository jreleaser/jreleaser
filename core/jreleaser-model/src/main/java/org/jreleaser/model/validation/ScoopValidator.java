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
import org.jreleaser.model.Scoop;
import org.jreleaser.util.Errors;

import java.util.List;

import static org.jreleaser.model.Checksum.INDIVIDUAL_CHECKSUM;
import static org.jreleaser.model.validation.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.model.validation.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class ScoopValidator extends Validator {
    public static void validateScoop(JReleaserContext context, Distribution distribution, Scoop tool, Errors errors) {
        JReleaserModel model = context.getModel();
        Scoop parentTool = model.getPackagers().getScoop();

        if (!tool.isActiveSet() && parentTool.isActiveSet()) {
            tool.setActive(parentTool.getActive());
        }
        if (!tool.resolveEnabled(context.getModel().getProject(), distribution)) return;
        GitService service = model.getRelease().getGitService();
        if (!service.isReleaseSupported()) {
            tool.disable();
            return;
        }

        context.getLogger().debug("distribution.{}.scoop", distribution.getName());

        List<Artifact> candidateArtifacts = tool.resolveCandidateArtifacts(context, distribution);
        if (candidateArtifacts.size() == 0) {
            tool.setActive(Active.NEVER);
            tool.disable();
            return;
        } else if (candidateArtifacts.size() > 1) {
            errors.configuration(RB.$("validation_tool_multiple_artifacts", "distribution." + distribution.getName() + ".scoop"));
            tool.disable();
            return;
        }

        // activate individual checksums on matching artifacts
        candidateArtifacts.forEach(artifact -> artifact.getExtraProperties().put(INDIVIDUAL_CHECKSUM, true));

        validateCommitAuthor(tool, parentTool);
        Scoop.ScoopBucket bucket = tool.getBucket();
        bucket.resolveEnabled(model.getProject());
        validateTap(context, distribution, bucket, parentTool.getBucket(), "scoop.bucket");
        validateTemplate(context, distribution, tool, parentTool, errors);
        mergeExtraProperties(tool, parentTool);
        validateContinueOnError(tool, parentTool);
        if (isBlank(tool.getDownloadUrl())) {
            tool.setDownloadUrl(parentTool.getDownloadUrl());
        }

        if (isBlank(tool.getPackageName())) {
            tool.setPackageName(parentTool.getPackageName());
            if (isBlank(tool.getPackageName())) {
                tool.setPackageName(distribution.getExecutable());
            }
        }
        if (isBlank(tool.getCheckverUrl())) {
            tool.setCheckverUrl(parentTool.getCheckverUrl());
            if (isBlank(tool.getCheckverUrl())) {
                tool.setCheckverUrl(service.getLatestReleaseUrl());
            }
        }
        if (isBlank(tool.getAutoupdateUrl())) {
            tool.setAutoupdateUrl(parentTool.getAutoupdateUrl());
        }

        validateArtifactPlatforms(context, distribution, tool, candidateArtifacts, errors);
    }
}
