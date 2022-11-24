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
package org.jreleaser.model.internal.validation.upload;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.upload.FtpUploader;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public abstract class FtpUploaderValidator extends Validator {
    public static void validateFtpUploader(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, FtpUploader> ftp = context.getModel().getUpload().getFtp();
        if (!ftp.isEmpty()) context.getLogger().debug("upload.ftp");

        for (Map.Entry<String, FtpUploader> e : ftp.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig()) {
                validateFtp(context, mode, e.getValue(), errors);
            }
        }
    }

    private static void validateFtp(JReleaserContext context, Mode mode, FtpUploader ftp, Errors errors) {
        context.getLogger().debug("upload.ftp.{}", ftp.getName());

        if (!ftp.isActiveSet()) {
            ftp.setActive(Active.NEVER);
        }
        if (!ftp.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!ftp.isArtifacts() && !ftp.isFiles() && !ftp.isSignatures()) {
            errors.warning(RB.$("WARNING.validation.uploader.no.artifacts", ftp.getType(), ftp.getName()));
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            ftp.disable();
            return;
        }

        String baseKey = "upload.ftp." + ftp.getName() + ".";
        ftp.setUsername(
            checkProperty(context,
                "FTP_" + Env.toVar(ftp.getName()) + "_USERNAME",
                baseKey + "username",
                ftp.getUsername(),
                errors,
                context.isDryrun()));

        ftp.setPassword(
            checkProperty(context,
                "FTP_" + Env.toVar(ftp.getName()) + "_PASSWORD",
                baseKey + "password",
                ftp.getPassword(),
                errors,
                context.isDryrun()));

        ftp.setHost(
            checkProperty(context,
                "FTP_" + Env.toVar(ftp.getName()) + "_HOST",
                baseKey + "host",
                ftp.getHost(),
                errors,
                context.isDryrun()));

        ftp.setPort(
            checkProperty(context,
                "FTP_" + Env.toVar(ftp.getName()) + "_PORT",
                baseKey + "port",
                ftp.getPort(),
                errors,
                context.isDryrun()));

        if (isBlank(ftp.getPath())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "ftp." + ftp.getName() + ".path"));
        }
        validateTimeout(ftp);
    }
}
