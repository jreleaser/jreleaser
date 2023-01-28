/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
import org.jreleaser.model.internal.upload.FtpUploader;
import org.jreleaser.model.internal.validation.common.FtpValidator;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class FtpUploaderValidator {
    private FtpUploaderValidator() {
        // noop
    }

    public static void validateFtpUploader(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, FtpUploader> ftp = context.getModel().getUpload().getFtp();
        if (!ftp.isEmpty()) context.getLogger().debug("upload.ftp");

        for (Map.Entry<String, FtpUploader> e : ftp.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig()) {
                validateFtp(context, e.getValue(), errors);
            }
        }
    }

    private static void validateFtp(JReleaserContext context, FtpUploader ftp, Errors errors) {
        context.getLogger().debug("upload.ftp.{}", ftp.getName());

        resolveActivatable(context, ftp,
            listOf("upload.ftp." + ftp.getName(), "upload.ftp"),
            "NEVER");
        if (!ftp.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!ftp.isArtifacts() && !ftp.isFiles() && !ftp.isSignatures()) {
            errors.warning(RB.$("WARNING.validation.uploader.no.artifacts", ftp.getType(), ftp.getName()));
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            ftp.disable();
            return;
        }

        FtpValidator.validateFtp(context, ftp, "upload", ftp.getName(), errors, context.isDryrun());

        if (isBlank(ftp.getPath())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "upload.ftp." + ftp.getName() + ".path"));
        }
        validateTimeout(ftp);
    }
}
