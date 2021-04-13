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
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Scoop;

import java.util.List;

import static org.jreleaser.model.validation.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.model.validation.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class ScoopValidator extends Validator {
    public static void validateScoop(JReleaserContext context, Distribution distribution, Scoop tool, List<String> errors) {
        JReleaserModel model = context.getModel();

        if (!tool.isActiveSet() && model.getPackagers().getScoop().isActiveSet()) {
            tool.setActive(model.getPackagers().getScoop().getActive());
        }
        if (!tool.resolveEnabled(context.getModel().getProject(),distribution)) return;
        context.getLogger().debug("distribution.{}.scoop", distribution.getName());

        validateCommitAuthor(tool, model.getPackagers().getScoop());
        validateOwner(tool.getBucket(), model.getPackagers().getScoop().getBucket());
        validateTemplate(context, distribution, tool, model.getPackagers().getScoop(), errors);
        Scoop commonScoop = model.getPackagers().getScoop();
        mergeExtraProperties(tool, model.getPackagers().getScoop());

        if (isBlank(tool.getCheckverUrl())) {
            tool.setCheckverUrl(commonScoop.getCheckverUrl());
            if (isBlank(tool.getCheckverUrl())) {
                tool.setCheckverUrl(model.getRelease().getGitService().getLatestReleaseUrlFormat());
            }
        }
        if (isBlank(tool.getAutoupdateUrl())) {
            tool.setAutoupdateUrl(commonScoop.getAutoupdateUrl());
            if (isBlank(tool.getAutoupdateUrl())) {
                tool.setAutoupdateUrl(model.getRelease().getGitService().getDownloadUrlFormat());
            }
        }

        if (isBlank(tool.getBucket().getName())) {
            tool.getBucket().setName(model.getPackagers().getScoop().getBucket().getName());
        }
        tool.getBucket().setBasename(model.getPackagers().getScoop().getBucket().getBasename());

        if (isBlank(tool.getBucket().getUsername())) {
            tool.getBucket().setUsername(model.getPackagers().getScoop().getBucket().getUsername());
        }
        if (isBlank(tool.getBucket().getToken())) {
            tool.getBucket().setToken(model.getPackagers().getScoop().getBucket().getToken());
        }

        validateArtifactPlatforms(context, distribution, tool, errors);
    }
}
