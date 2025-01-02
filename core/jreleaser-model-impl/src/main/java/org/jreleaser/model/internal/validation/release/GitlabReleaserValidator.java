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
import org.jreleaser.model.internal.release.GitlabReleaser;
import org.jreleaser.model.internal.upload.Uploader;
import org.jreleaser.util.Errors;

import java.util.Map;
import java.util.Optional;

import static org.jreleaser.model.internal.validation.release.BaseReleaserValidator.validateGitService;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class GitlabReleaserValidator {
    private GitlabReleaserValidator() {
        // noop
    }

    public static boolean validateGitlab(JReleaserContext context, Mode mode, GitlabReleaser service, Errors errors) {
        if (null == service) return false;
        context.getLogger().debug("release.gitlab");

        validateGitService(context, mode, service, errors);
        service.getPrerelease().disable();

        for (Map.Entry<String, String> e : service.getUploadLinks().entrySet()) {
            Optional<? extends Uploader<?>> uploader = context.getModel().getUpload().getUploader(e.getKey(), e.getValue());
            if (!uploader.isPresent()) {
                errors.configuration(RB.$("validation_gitlab_non_matching_uploader", e.getKey(), e.getValue()));
            }
        }

        return service.isEnabled();
    }
}
