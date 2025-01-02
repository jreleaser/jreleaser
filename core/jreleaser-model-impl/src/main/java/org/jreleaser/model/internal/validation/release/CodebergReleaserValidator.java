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

import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.release.CodebergReleaser;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.api.release.Releaser.DRAFT;
import static org.jreleaser.model.api.release.Releaser.PRERELEASE_PATTERN;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.release.BaseReleaserValidator.validateGitService;


/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class CodebergReleaserValidator {
    private CodebergReleaserValidator() {
        // noop
    }

    public static boolean validateCodeberg(JReleaserContext context, Mode mode, CodebergReleaser service, Errors errors) {
        if (null == service) return false;
        context.getLogger().debug("release.codeberg");

        validateGitService(context, mode, service, errors);

        if (context.getModel().getProject().isSnapshot()) {
            service.getPrerelease().setEnabled(true);
        }

        service.getPrerelease().setPattern(
            checkProperty(context,
                PRERELEASE_PATTERN,
                "release.codeberg.prerelease.pattern",
                service.getPrerelease().getPattern(),
                ""));
        service.getPrerelease().isPrerelease(context.getModel().getProject().getResolvedVersion());

        if (!service.isDraftSet()) {
            service.setDraft(
                checkProperty(context,
                    DRAFT,
                    "release.codeberg.draft",
                    null,
                    false));
        }

        if (service.isDraft()) {
            service.getMilestone().setClose(false);
        }

        return service.isEnabled();
    }
}
