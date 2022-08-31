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
            context.getLogger().debug("--== {} ==--", mode);
            validateModel(context, mode, errors);
        } finally {
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
        }
    }

    private static void validateModel(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        validateHooks(context, mode, errors);
        validateProject(context, mode, errors);
        if (mode.validateConfig() || mode.validateDownload()) validateDownloaders(context, mode, errors);
        if (mode.validateConfig() || mode.validateAssembly()) validateAssemblers(context, mode, errors);
        if (context.getModel().getCommit() != null) {
            validateSigning(context, mode, errors);
            validateRelease(context, mode, errors);
        }

        if (mode.validateConfig()) validateChecksum(context, mode, errors);
        if (mode.validateConfig()) validateUploaders(context, mode, errors);
        if (mode.validateConfig()) validatePackagers(context, mode, errors);
        if (mode.validateConfig()) validateDistributions(context, mode, errors);
        if (mode.validateConfig()) validateFiles(context, mode, errors);
        if (mode.validateConfig() || mode.validateAnnounce()) validateAnnouncers(context, mode, errors);

        context.getLogger().setPrefix("postvalidation");
        try {
            postValidateProject(context, mode, errors);
            if (mode.validateConfig() || mode.validateAssembly()) postValidateAssemblers(context, mode, errors);
            if (mode.validateConfig()) postValidateDistributions(context, mode, errors);
        } finally {
            context.getLogger().restorePrefix();
        }
    }
}
