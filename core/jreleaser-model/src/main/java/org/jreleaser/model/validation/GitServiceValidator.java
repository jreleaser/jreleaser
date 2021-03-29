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

import org.jreleaser.model.Changelog;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Project;
import org.jreleaser.util.StringUtils;

import java.util.List;

import static org.jreleaser.model.GitService.TAG_NAME;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class GitServiceValidator extends Validator {
    public static void validateGitService(JReleaserContext context, GitService service, List<String> errors) {
        JReleaserModel model = context.getModel();
        Project project = model.getProject();

        if (!service.isEnabledSet()) {
            service.setEnabled(true);
        }
        if (isBlank(service.getOwner())) {
            errors.add(service.getServiceName() + ".owner must not be blank");
        }
        if (isBlank(service.getName())) {
            service.setName(project.getName());
        }
        if (isBlank(service.getUsername())) {
            service.setUsername(service.getOwner());
        }

        service.setToken(
            checkProperty(context.getModel().getEnvironment(),
                service.getServiceName().toUpperCase() + "_TOKEN",
                service.getServiceName() + ".token",
                service.getToken(),
                errors));

        service.setTagName(
            checkProperty(context.getModel().getEnvironment(),
                TAG_NAME,
                service.getServiceName() + ".tagName",
                service.getTagName(),
                errors));

        if (isBlank(service.getTagName())) {
            service.setTagName("v{{" + project.getResolvedVersion() + "}}");
        }
        if (!service.getChangelog().isEnabledSet()) {
            service.getChangelog().setEnabled(true);
        }
        if (service.isSign() && !model.getSigning().isEnabled()) {
            errors.add(service.getServiceName() + ".sign is set to `true` but signing is not enabled");
        }
        if (isBlank(service.getCommitAuthor().getName())) {
            service.getCommitAuthor().setName("jreleaser-bot");
        }
        if (isBlank(service.getCommitAuthor().getEmail())) {
            service.getCommitAuthor().setEmail("jreleaser-bot@jreleaser.org");
        }

        if (project.isSnapshot()) {
            service.setReleaseName(StringUtils.capitalize(project.getName()) + " Early-Access");
            service.getChangelog().setExternal(null);
            service.getChangelog().setSort(Changelog.Sort.DESC);
            service.setOverwrite(true);
        }

        // eager resolve
        service.getEffectiveTagName(project);
    }
}
