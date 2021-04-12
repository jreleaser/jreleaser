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

import org.jreleaser.model.Brew;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;

import java.util.ArrayList;
import java.util.List;

import static org.jreleaser.model.validation.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.model.validation.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class BrewValidator extends Validator {
    public static void validateBrew(JReleaserContext context, Distribution distribution, Brew tool, List<String> errors) {
        JReleaserModel model = context.getModel();

        if (!tool.isEnabledSet() && model.getPackagers().getBrew().isEnabledSet()) {
            tool.setEnabled(model.getPackagers().getBrew().isEnabled());
        }
        if (!tool.supportsDistribution(distribution)) {
            tool.setEnabled(false);
        }
        if (!tool.isEnabled()) return;
        context.getLogger().debug("distribution.{}.brew", distribution.getName());

        validateCommitAuthor(tool, model.getPackagers().getBrew());
        validateOwner(tool.getTap(), model.getPackagers().getBrew().getTap());
        validateTemplate(context, distribution, tool, model.getPackagers().getBrew(), errors);
        mergeExtraProperties(tool, model.getPackagers().getBrew());

        List<Brew.Dependency> dependencies = new ArrayList<>(model.getPackagers().getBrew().getDependenciesAsList());
        dependencies.addAll(tool.getDependenciesAsList());
        tool.setDependenciesAsList(dependencies);

        if (isBlank(tool.getTap().getName())) {
            tool.getTap().setName(model.getPackagers().getBrew().getTap().getName());
        }
        if (isBlank(tool.getTap().getUsername())) {
            tool.getTap().setUsername(model.getPackagers().getBrew().getTap().getUsername());
        }
        if (isBlank(tool.getTap().getToken())) {
            tool.getTap().setToken(model.getPackagers().getBrew().getTap().getToken());
        }

        validateArtifactPlatforms(context, distribution, tool, errors);
    }
}
