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
import org.jreleaser.model.internal.release.GenericGitReleaser;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.release.BaseReleaserValidator.validateGitService;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public final class GenericGitReleaserValidator {
    private GenericGitReleaserValidator() {
        // noop
    }

    public static boolean validateGeneric(JReleaserContext context, Mode mode, GenericGitReleaser service, Errors errors) {
        if (null == service) return false;
        context.getLogger().debug("release.generic");

        validateGitService(context, mode, service, errors);
        service.getChangelog().setLinks(false);

        return service.isEnabled();
    }
}
