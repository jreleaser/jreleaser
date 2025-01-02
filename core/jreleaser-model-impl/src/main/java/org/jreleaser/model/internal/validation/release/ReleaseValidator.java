/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
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
package org.jreleaser.model.internal.validation.release;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.release.Release;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.release.CodebergReleaserValidator.validateCodeberg;
import static org.jreleaser.model.internal.validation.release.GenericGitReleaserValidator.validateGeneric;
import static org.jreleaser.model.internal.validation.release.GiteaReleaserValidator.validateGitea;
import static org.jreleaser.model.internal.validation.release.GithubReleaserValidator.validateGithub;
import static org.jreleaser.model.internal.validation.release.GitlabReleaserValidator.validateGitlab;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class ReleaseValidator {
    private ReleaseValidator() {
        // noop
    }

    public static void validateRelease(JReleaserContext context, Mode mode, Errors errors) {
        context.getLogger().debug("release");
        Release release = context.getModel().getRelease();

        int count = 0;
        if (validateGithub(context, mode, release.getGithub(), errors)) count++;
        if (validateGitlab(context, mode, release.getGitlab(), errors)) count++;
        if (validateGitea(context, mode, release.getGitea(), errors)) count++;
        if (validateCodeberg(context, mode, release.getCodeberg(), errors)) count++;
        if (validateGeneric(context, mode, release.getGeneric(), errors)) count++;

        if (!mode.validateStandalone()) {
            if (0 == count) {
                errors.configuration(RB.$("validation_release_no_providers"));
                return;
            }
            if (count > 1) {
                errors.configuration(RB.$("validation_release_requirement", "release.[github|gitlab|gitea|codeberg|generic]"));
            }
        }
    }
}
