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

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.SftpUploader;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.validation.SshValidator.validateSsh;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public abstract class SftpUploaderValidator extends Validator {
    public static void validateSftpUploader(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().debug("sftp");
        Map<String, SftpUploader> sftp = context.getModel().getUpload().getSftp();

        for (Map.Entry<String, SftpUploader> e : sftp.entrySet()) {
            e.getValue().setName(e.getKey());
            if (!mode.validateConfig()) {
                validateSftpUploader(context, mode, e.getValue(), new Errors());
            } else {
                validateSftpUploader(context, mode, e.getValue(), errors);
            }
        }
    }

    private static void validateSftpUploader(JReleaserContext context, JReleaserContext.Mode mode, SftpUploader sftp, Errors errors) {
        context.getLogger().debug("sftp.{}", sftp.getName());

        if (!sftp.isActiveSet()) {
            sftp.setActive(Active.NEVER);
        }
        if (!sftp.resolveEnabled(context.getModel().getProject())) {
            return;
        }

        if (!sftp.isArtifacts() && !sftp.isFiles() && !sftp.isSignatures()) {
            sftp.disable();
            return;
        }

        validateSsh(context, sftp, sftp.getName(), "SFTP", sftp.getType(), errors);
        if (isBlank(sftp.getPath())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "sftp." + sftp.getName() + ".path"));
        }
        validateTimeout(sftp);
    }
}
