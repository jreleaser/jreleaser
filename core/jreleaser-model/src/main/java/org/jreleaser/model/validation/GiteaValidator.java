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

import org.jreleaser.model.Gitea;
import org.jreleaser.model.JReleaserContext;

import java.util.List;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class GiteaValidator extends GitServiceValidator {
    public static boolean validateGitea(JReleaserContext context, Gitea gitea, List<String> errors) {
        if (null == gitea) return false;

        validateGitService(context, gitea, errors);

        if (isBlank(gitea.getApiEndpoint())) {
            errors.add("gitea.apiEndpoint must not be blank");
        }

        if (isBlank(gitea.getTargetCommitish())) {
            gitea.setTargetCommitish("main");
        }

        if (context.getModel().getProject().isSnapshot()) {
            gitea.setPrerelease(true);
        }

        return gitea.isEnabled();
    }
}
