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
package org.jreleaser.model;

import org.jreleaser.util.Errors;

import static org.jreleaser.model.validation.AnnouncersValidator.validateAnnouncers;
import static org.jreleaser.model.validation.AssemblersValidator.postValidateAssemblers;
import static org.jreleaser.model.validation.AssemblersValidator.validateAssemblers;
import static org.jreleaser.model.validation.ChecksumValidator.validateChecksum;
import static org.jreleaser.model.validation.DistributionsValidator.postValidateDistributions;
import static org.jreleaser.model.validation.DistributionsValidator.validateDistributions;
import static org.jreleaser.model.validation.DownloadersValidator.validateDownloaders;
import static org.jreleaser.model.validation.FilesValidator.validateFiles;
import static org.jreleaser.model.validation.HooksValidator.validateHooks;
import static org.jreleaser.model.validation.PackagersValidator.validatePackagers;
import static org.jreleaser.model.validation.ProjectValidator.postValidateProject;
import static org.jreleaser.model.validation.ProjectValidator.validateProject;
import static org.jreleaser.model.validation.ReleaseValidator.validateRelease;
import static org.jreleaser.model.validation.SigningValidator.validateSigning;
import static org.jreleaser.model.validation.UploadersValidator.validateUploaders;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class JReleaserModelValidator {
    private JReleaserModelValidator() {
        // noop
    }

    public static void validate(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().increaseIndent();
        context.getLogger().setPrefix("validation");
        try {
            validateModel(context, mode, errors);
        } finally {
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
        }
    }

    private static void validateModel(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        validateHooks(context, mode, errors);
        validateProject(context, mode, errors);
        validateDownloaders(context, mode, errors);
        validateAssemblers(context, mode, errors);
        if (context.getModel().getCommit() != null) {
            validateSigning(context, mode, errors);
            validateRelease(context, mode, errors);
        }

        validateChecksum(context, mode, errors);
        validateUploaders(context, mode, errors);
        validatePackagers(context, mode, errors);
        validateDistributions(context, mode, errors);
        validateFiles(context, mode, errors);
        validateAnnouncers(context, mode, errors);

        postValidateProject(context, mode, errors);
        postValidateAssemblers(context, mode, errors);
        if (!mode.validateStandalone()) {
            postValidateDistributions(context, mode, errors);
        }
    }
}
