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

import org.jreleaser.model.Github;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.GitService.DRAFT;
import static org.jreleaser.model.GitService.PRERELEASE_PATTERN;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class GithubValidator extends GitServiceValidator {
    public static boolean validateGithub(JReleaserContext context, JReleaserContext.Mode mode, Github github, Errors errors) {
        if (null == github) return false;
        context.getLogger().debug("release.github");

        validateGitService(context, mode, github, errors);

        if (context.getModel().getProject().isSnapshot()) {
            github.getPrerelease().setEnabled(true);
        }

        github.getPrerelease().setPattern(
            checkProperty(context,
                PRERELEASE_PATTERN,
                "release.github.prerelease.pattern",
                github.getPrerelease().getPattern(),
                ""));
        github.getPrerelease().isPrerelease(context.getModel().getProject().getResolvedVersion());

        if (!github.isDraftSet()) {
            github.setDraft(
                checkProperty(context,
                    DRAFT,
                    "github.draft",
                    null,
                    false));
        }

        if (github.isDraft()) {
            github.getMilestone().setClose(false);
        }

        return github.isEnabled();
    }
}
