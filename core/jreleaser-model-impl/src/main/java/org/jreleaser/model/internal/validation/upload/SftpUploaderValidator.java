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
import org.jreleaser.model.internal.upload.SftpUploader;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.SshValidator.validateSsh;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class SftpUploaderValidator {
    private SftpUploaderValidator() {
        // noop
    }

    public static void validateSftpUploader(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, SftpUploader> sftp = context.getModel().getUpload().getSftp();
        if (!sftp.isEmpty()) context.getLogger().debug("upload.sftp");

        for (Map.Entry<String, SftpUploader> e : sftp.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig()) {
                validateSftpUploader(context, e.getValue(), errors);
            }
        }
    }

    private static void validateSftpUploader(JReleaserContext context, SftpUploader sftp, Errors errors) {
        context.getLogger().debug("upload.sftp.{}", sftp.getName());

        resolveActivatable(context, sftp,
            listOf("upload.sftp." + sftp.getName(), "upload.sftp"),
            "NEVER");
        if (!sftp.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!sftp.isArtifacts() && !sftp.isFiles() && !sftp.isSignatures()) {
            errors.warning(RB.$("WARNING.validation.uploader.no.artifacts", sftp.getType(), sftp.getName()));
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            sftp.disable();
            return;
        }

        validateSsh(context, sftp, sftp.getType(), sftp.getName(), "upload.", errors);
        if (isBlank(sftp.getPath())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "upload.sftp." + sftp.getName() + ".path"));
        }
        validateTimeout(sftp);
    }
}
