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
package org.jreleaser.model.internal.validation.release;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.release.GiteaReleaser;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.api.release.Releaser.DRAFT;
import static org.jreleaser.model.api.release.Releaser.PRERELEASE_PATTERN;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class GiteaReleaserValidator extends BaseReleaserValidator {
    public static boolean validateGitea(JReleaserContext context, Mode mode, GiteaReleaser gitea, Errors errors) {
        if (null == gitea) return false;
        context.getLogger().debug("release.gitea");

        validateGitService(context, mode, gitea, errors);

        if (isBlank(gitea.getApiEndpoint())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "gitea.internal.mutableEndpoint"));
        }

        if (context.getModel().getProject().isSnapshot()) {
            gitea.getPrerelease().setEnabled(true);
        }

        gitea.getPrerelease().setPattern(
            checkProperty(context,
                PRERELEASE_PATTERN,
                "release.gitea.prerelease.pattern",
                gitea.getPrerelease().getPattern(),
                ""));
        gitea.getPrerelease().isPrerelease(context.getModel().getProject().getResolvedVersion());

        if (!gitea.isDraftSet()) {
            gitea.setDraft(
                checkProperty(context,
                    DRAFT,
                    "gitea.draft",
                    null,
                    false));
        }

        if (gitea.isDraft()) {
            gitea.getMilestone().setClose(false);
        }

        return gitea.isEnabled();
    }
}
