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
package org.jreleaser.model.validation;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Upload;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.validation.ArtifactoryValidator.validateArtifactory;
import static org.jreleaser.model.validation.FtpUploaderValidator.validateFtpUploader;
import static org.jreleaser.model.validation.HttpUploaderValidator.validateHttpUploader;
import static org.jreleaser.model.validation.S3Validator.validateS3;
import static org.jreleaser.model.validation.ScpUploaderValidator.validateScpUploader;
import static org.jreleaser.model.validation.SftpUploaderValidator.validateSftpUploader;
import static org.jreleaser.model.validation.AzureArtifactsValidator.validateAzureArtifacts;;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public abstract class UploadersValidator extends Validator {
    public static void validateUploaders(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().debug("upload");

        Upload upload = context.getModel().getUpload();
        validateArtifactory(context, mode, errors);
        validateFtpUploader(context, mode, errors);
        validateHttpUploader(context, mode, errors);
        validateS3(context, mode, errors);
        validateScpUploader(context, mode, errors);
        validateSftpUploader(context, mode, errors);
        validateAzureArtifacts(context, mode, errors);

        if (mode.validateConfig() && !upload.isEnabledSet()) {
            upload.setEnabled(!upload.getActiveArtifactories().isEmpty() ||
                !upload.getActiveFtps().isEmpty() ||
                !upload.getActiveHttps().isEmpty() ||
                !upload.getActiveS3s().isEmpty() ||
                !upload.getActiveScps().isEmpty() ||
                !upload.getActiveSftps().isEmpty() ||
                !upload.getActiveAzureArtifactses().isEmpty());
        }
    }
}