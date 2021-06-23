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

import org.jreleaser.model.GenericGit;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Errors;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public abstract class GenericGitValidator extends GitServiceValidator {
    public static boolean validateGeneric(JReleaserContext context, JReleaserContext.Mode mode, GenericGit generic, Errors errors) {
        if (null == generic) return false;
        context.getLogger().debug("release.generic");

        validateGitService(context, mode, generic, errors);
        generic.getChangelog().setLinks(false);

        return generic.isEnabled();
    }
}
