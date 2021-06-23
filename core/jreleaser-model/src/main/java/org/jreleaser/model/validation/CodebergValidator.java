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

import org.jreleaser.model.Codeberg;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.GitService.DRAFT;
import static org.jreleaser.model.GitService.PRERELEASE;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class CodebergValidator extends GitServiceValidator {
    public static boolean validateCodeberg(JReleaserContext context, JReleaserContext.Mode mode, Codeberg codeberg, Errors errors) {
        if (null == codeberg) return false;
        context.getLogger().debug("release.codeberg");

        validateGitService(context, mode, codeberg, errors);

        if (context.getModel().getProject().isSnapshot()) {
            codeberg.setPrerelease(true);
        }

        if (!codeberg.isPrereleaseSet()) {
            codeberg.setPrerelease(
                checkProperty(context.getModel().getEnvironment(),
                    PRERELEASE,
                    "codeberg.prerelease",
                    null,
                    false));
        }

        if (!codeberg.isDraftSet()) {
            codeberg.setDraft(
                checkProperty(context.getModel().getEnvironment(),
                    DRAFT,
                    "codeberg.draft",
                    null,
                    false));
        }

        if (codeberg.isDraft()) {
            codeberg.getMilestone().setClose(false);
        }

        return codeberg.isEnabled();
    }
}
