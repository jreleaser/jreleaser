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
import org.jreleaser.model.Jbang;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.jreleaser.model.validation.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;
import static org.jreleaser.util.Constants.KEY_REVERSE_REPO_HOST;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class JbangValidator extends Validator {
    public static void validateJbang(JReleaserContext context, Distribution distribution, Jbang tool, List<String> errors) {
        JReleaserModel model = context.getModel();

        if (!tool.isEnabledSet() && model.getPackagers().getJbang().isEnabledSet()) {
            tool.setEnabled(model.getPackagers().getJbang().isEnabled());
        }
        if (!tool.supportsDistribution(distribution)) {
            tool.setEnabled(false);
        }
        if (!tool.isEnabled()) return;
        context.getLogger().debug("distribution.{}.jbang", distribution.getName());

        validateCommitAuthor(tool, model.getPackagers().getJbang());
        validateOwner(tool.getCatalog(), model.getPackagers().getJbang().getCatalog());
        validateTemplate(context, distribution, tool, model.getPackagers().getJbang(), errors);
        mergeExtraProperties(tool, model.getPackagers().getJbang());

        if (isBlank(tool.getAlias())) {
            tool.setAlias(distribution.getExecutable());
        }
        if (isBlank(tool.getCatalog().getName())) {
            tool.getCatalog().setName(model.getPackagers().getJbang().getCatalog().getName());
        }
        if (isBlank(tool.getCatalog().getUsername())) {
            tool.getCatalog().setUsername(model.getPackagers().getJbang().getCatalog().getUsername());
        }
        if (isBlank(tool.getCatalog().getToken())) {
            tool.getCatalog().setToken(model.getPackagers().getJbang().getCatalog().getToken());
        }

        if (model.getProject().getExtraProperties().containsKey(KEY_REVERSE_REPO_HOST) &&
            !model.getPackagers().getJbang().getExtraProperties().containsKey(KEY_REVERSE_REPO_HOST)) {
            model.getPackagers().getJbang().getExtraProperties().put(KEY_REVERSE_REPO_HOST,
                model.getProject().getExtraProperties().get(KEY_REVERSE_REPO_HOST));
        }
        if (model.getPackagers().getJbang().getExtraProperties().containsKey(KEY_REVERSE_REPO_HOST) &&
            !distribution.getExtraProperties().containsKey(KEY_REVERSE_REPO_HOST)) {
            distribution.getExtraProperties().put(KEY_REVERSE_REPO_HOST,
                model.getPackagers().getJbang().getExtraProperties().get(KEY_REVERSE_REPO_HOST));
        }

        if (distribution.getExtraProperties().containsKey(KEY_REVERSE_REPO_HOST) &&
            !tool.getExtraProperties().containsKey(KEY_REVERSE_REPO_HOST)) {
            tool.getExtraProperties().put(KEY_REVERSE_REPO_HOST,
                distribution.getExtraProperties().get(KEY_REVERSE_REPO_HOST));
        }
        if (isBlank(model.getRelease().getGitService().getReverseRepoHost()) &&
            !tool.getExtraProperties().containsKey(KEY_REVERSE_REPO_HOST)) {
            errors.add("distribution." + distribution.getName() +
                ".jbang must define an extra property named '" +
                KEY_REVERSE_REPO_HOST + "'");
        }

        if (isBlank(distribution.getJava().getMainClass())) {
            errors.add("distribution." + distribution.getName() + ".java.mainClass must not be blank, required by jbang");
        }
    }

    public static void postValidateJBang(JReleaserContext context, List<String> errors) {
        Map<String, List<Distribution>> map = context.getModel().getDistributions().values().stream()
            .filter(d -> d.getJbang().isEnabled())
            .collect(groupingBy(d -> d.getJbang().getAlias()));

        map.forEach((alias, distributions) -> {
            if (distributions.size() > 1) {
                errors.add("jbang.alias '" + alias + "' is defined for more than one distribution: " +
                    distributions.stream().map(Distribution::getName).collect(Collectors.joining(", ")));
            }
        });
    }
}
