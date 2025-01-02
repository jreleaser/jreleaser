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
package org.jreleaser.model.internal.validation.upload;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.upload.Upload;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.upload.ArtifactoryUploaderValidator.validateArtifactory;
import static org.jreleaser.model.internal.validation.upload.FtpUploaderValidator.validateFtpUploader;
import static org.jreleaser.model.internal.validation.upload.GiteaUploaderValidator.validateGiteaUploader;
import static org.jreleaser.model.internal.validation.upload.GitlabUploaderValidator.validateGitlabUploader;
import static org.jreleaser.model.internal.validation.upload.HttpUploaderValidator.validateHttpUploader;
import static org.jreleaser.model.internal.validation.upload.S3UploaderValidator.validateS3;
import static org.jreleaser.model.internal.validation.upload.ScpUploaderValidator.validateScpUploader;
import static org.jreleaser.model.internal.validation.upload.SftpUploaderValidator.validateSftpUploader;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public final class UploadersValidator {
    private UploadersValidator() {
        // noop
    }

    public static void validateUploaders(JReleaserContext context, Mode mode, Errors errors) {
        Upload upload = context.getModel().getUpload();
        context.getLogger().debug("upload");

        validateArtifactory(context, mode, errors);
        validateFtpUploader(context, mode, errors);
        validateGiteaUploader(context, mode, errors);
        validateGitlabUploader(context, mode, errors);
        validateHttpUploader(context, mode, errors);
        validateS3(context, mode, errors);
        validateScpUploader(context, mode, errors);
        validateSftpUploader(context, mode, errors);

        if (mode.validateConfig()) {
            boolean activeSet = upload.isActiveSet();
            resolveActivatable(context, upload, "upload", "ALWAYS");
            upload.resolveEnabledWithSnapshot(context.getModel().getProject());

            if (upload.isEnabled()) {
                boolean enabled = !upload.getActiveArtifactories().isEmpty() ||
                    !upload.getActiveFtps().isEmpty() ||
                    !upload.getActiveGiteas().isEmpty() ||
                    !upload.getActiveGitlabs().isEmpty() ||
                    !upload.getActiveHttps().isEmpty() ||
                    !upload.getActiveS3s().isEmpty() ||
                    !upload.getActiveScps().isEmpty() ||
                    !upload.getActiveSftps().isEmpty();

                if (!activeSet && !enabled) {
                    context.getLogger().debug(RB.$("validation.disabled"));
                    upload.disable();
                }
            }
        }
    }
}