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

import org.jreleaser.model.Chocolatey;
import org.jreleaser.model.ChocolateyBucket;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.Chocolatey.CHOCOLATEY_API_KEY;
import static org.jreleaser.model.validation.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.model.validation.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;
import static org.jreleaser.util.StringUtils.isBlank;

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
        ChocolateyBucket bucket = tool.getBucket();
        validateOwner(bucket, parentTool.getBucket());
        if (isBlank(bucket.getBranch())) {
            bucket.setBranch(parentTool.getBucket().getBranch());
        }
        validateTemplate(context, distribution, tool, parentTool, errors);
        mergeExtraProperties(tool, parentTool);
        validateContinueOnError(tool, parentTool);

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

        if (!tool.isRemoteBuild()) {
            tool.setApiKey(
                checkProperty(context,
                    CHOCOLATEY_API_KEY,
                    "chocolatey.apiKey",
                    tool.getApiKey(),
                    errors,
                    context.isDryrun()));
        }

        if (isBlank(bucket.getName())) {
            bucket.setName(parentTool.getBucket().getName());
        }
        if (isBlank(bucket.getUsername())) {
            bucket.setUsername(parentTool.getBucket().getUsername());
        }
        if (isBlank(bucket.getToken())) {
            bucket.setToken(parentTool.getBucket().getToken());
        }

        validateTap(context, distribution, bucket, "chocolatey.bucket");

        validateArtifactPlatforms(context, distribution, tool, errors);
    }
}
