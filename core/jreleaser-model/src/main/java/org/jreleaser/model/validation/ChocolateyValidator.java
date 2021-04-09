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

import org.jreleaser.model.Chocolatey;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;

import java.util.List;

import static org.jreleaser.model.validation.DistributionsValidator.validateArtifactPlatforms;
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
        context.getLogger().debug("distribution.{}.chocolatey", distribution.getName());

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
            tool.getBucket().setName("chocolatey-bucket");
        }
        tool.getBucket().setBasename("chocolatey-bucket");
        if (isBlank(tool.getBucket().getUsername())) {
            tool.getBucket().setUsername(model.getPackagers().getChocolatey().getBucket().getUsername());
        }
        if (isBlank(tool.getBucket().getToken())) {
            tool.getBucket().setToken(model.getPackagers().getChocolatey().getBucket().getToken());
        }

        validateArtifactPlatforms(context, distribution, tool, errors);
    }
}
