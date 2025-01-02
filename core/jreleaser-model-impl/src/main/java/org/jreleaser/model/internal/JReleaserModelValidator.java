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
package org.jreleaser.model.internal;

import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.announce.AnnouncersValidator.validateAnnouncers;
import static org.jreleaser.model.internal.validation.assemble.AssemblersValidator.postValidateAssemblers;
import static org.jreleaser.model.internal.validation.assemble.AssemblersValidator.validateAssemblers;
import static org.jreleaser.model.internal.validation.catalog.CatalogValidator.validateCatalog;
import static org.jreleaser.model.internal.validation.catalog.swid.SwidTagValidator.validateSwid;
import static org.jreleaser.model.internal.validation.checksum.ChecksumValidator.validateChecksum;
import static org.jreleaser.model.internal.validation.common.MatrixValidator.validateMatrix;
import static org.jreleaser.model.internal.validation.deploy.DeployValidator.validateDeploy;
import static org.jreleaser.model.internal.validation.distributions.DistributionsValidator.postValidateDistributions;
import static org.jreleaser.model.internal.validation.distributions.DistributionsValidator.validateDistributions;
import static org.jreleaser.model.internal.validation.download.DownloadersValidator.validateDownloaders;
import static org.jreleaser.model.internal.validation.extensions.ExtensionsValidator.validateExtensions;
import static org.jreleaser.model.internal.validation.files.FilesValidator.validateFiles;
import static org.jreleaser.model.internal.validation.hooks.HooksValidator.validateHooks;
import static org.jreleaser.model.internal.validation.packagers.PackagersValidator.validatePackagers;
import static org.jreleaser.model.internal.validation.project.ProjectValidator.postValidateProject;
import static org.jreleaser.model.internal.validation.project.ProjectValidator.validateProject;
import static org.jreleaser.model.internal.validation.release.ReleaseValidator.validateRelease;
import static org.jreleaser.model.internal.validation.signing.SigningValidator.postValidateSigning;
import static org.jreleaser.model.internal.validation.signing.SigningValidator.validateSigning;
import static org.jreleaser.model.internal.validation.upload.UploadersValidator.validateUploaders;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class JReleaserModelValidator {
    private JReleaserModelValidator() {
        // noop
    }

    public static void validate(JReleaserContext context, Mode mode, Errors errors) {
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

    private static void validateModel(JReleaserContext context, Mode mode, Errors errors) {
        validateExtensions(context, errors);
        validateMatrix(context, context.getModel().getMatrix(), "matrix", errors);
        validateHooks(context, errors);
        validateProject(context, mode, errors);
        validateDownloaders(context, mode, errors);
        validateSwid(context, mode, errors);
        validateAssemblers(context, mode, errors);
        validateSigning(context, mode, errors);
        if (null != context.getModel().getCommit()) {
            validateRelease(context, mode, errors);
        }

        validateChecksum(context);
        validateDeploy(context, mode, errors);
        validateUploaders(context, mode, errors);
        validatePackagers(context, mode, errors);
        validateDistributions(context, mode, errors);
        validateFiles(context, mode, errors);
        validateCatalog(context, mode, errors);
        validateAnnouncers(context, mode, errors);

        context.getLogger().setPrefix("postvalidation");
        try {
            postValidateProject(context, mode, errors);
            if (mode.validateConfig() || mode.validateAssembly()) postValidateAssemblers(context);
            if (mode.validateConfig()) postValidateDistributions(context, errors);
            postValidateSigning(context, mode, errors);
        } finally {
            context.getLogger().restorePrefix();
        }
    }
}
