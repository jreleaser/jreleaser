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

import org.jreleaser.model.Github;
import org.jreleaser.model.JReleaserContext;

import java.util.List;

import static org.jreleaser.model.GitService.BRANCH;
import static org.jreleaser.model.GitService.PRERELEASE;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class GithubValidator extends GitServiceValidator {
    public static boolean validateGithub(JReleaserContext context, Github github, List<String> errors) {
        if (null == github) return false;
        context.getLogger().debug("release.github");

        validateGitService(context, github, errors);

        github.setTargetCommitish(
            checkProperty(context.getModel().getEnvironment(),
                BRANCH,
                "github.targetCommitish",
                github.getTargetCommitish(),
                "main"));

        if (context.getModel().getProject().isSnapshot()) {
            github.setPrerelease(true);
        }

        if (!github.isPrereleaseSet()) {
            github.setPrerelease(
                checkProperty(context.getModel().getEnvironment(),
                    PRERELEASE,
                    "github.prerelease",
                    null,
                    false));
        }

        return github.isEnabled();
    }
}
