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
import org.jreleaser.model.internal.release.GiteaReleaser;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.api.release.Releaser.DRAFT;
import static org.jreleaser.model.api.release.Releaser.PRERELEASE_PATTERN;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.release.BaseReleaserValidator.validateGitService;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class GiteaReleaserValidator {
    private GiteaReleaserValidator() {
        // noop
    }

    public static boolean validateGitea(JReleaserContext context, Mode mode, GiteaReleaser service, Errors errors) {
        if (null == service) return false;
        context.getLogger().debug("release.gitea");

        validateGitService(context, mode, service, errors);

        if (isBlank(service.getApiEndpoint())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "gitea.apiEndpoint"));
        }

        if (context.getModel().getProject().isSnapshot()) {
            service.getPrerelease().setEnabled(true);
        }

        service.getPrerelease().setPattern(
            checkProperty(context,
                PRERELEASE_PATTERN,
                "release.gitea.prerelease.pattern",
                service.getPrerelease().getPattern(),
                ""));
        service.getPrerelease().isPrerelease(context.getModel().getProject().getResolvedVersion());

        if (!service.isDraftSet()) {
            service.setDraft(
                checkProperty(context,
                    DRAFT,
                    "release.gitea.draft",
                    null,
                    false));
        }

        if (!service.getUpdate().isEnabled()) {
            if (!service.getPrerelease().isEnabledSet()) {
                service.getPrerelease().setEnabled(false);
            }
            if (!service.isDraftSet()) {
                service.setDraft(false);
            }
        }

        if (service.isDraft()) {
            service.getMilestone().setClose(false);
        }

        return service.isEnabled();
    }
}
