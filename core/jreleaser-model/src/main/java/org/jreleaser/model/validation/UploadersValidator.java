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
import static org.jreleaser.model.validation.HttpValidator.validateHttp;
import static org.jreleaser.model.validation.S3Validator.validateS3;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public abstract class UploadersValidator extends Validator {
    public static void validateUploaders(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        if (!mode.validateConfig()) {
            return;
        }

        context.getLogger().debug("upload");

        Upload upload = context.getModel().getUpload();
        validateArtifactory(context, mode, errors);
        validateHttp(context, mode, errors);
        validateS3(context, mode, errors);

        if (!upload.isEnabledSet()) {
            upload.setEnabled(!upload.getActiveArtifactories().isEmpty() ||
                !upload.getActiveHttps().isEmpty() ||
                !upload.getActiveS3s().isEmpty());
        }
    }
}