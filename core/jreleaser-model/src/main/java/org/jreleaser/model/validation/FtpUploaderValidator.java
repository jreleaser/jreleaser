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
import org.jreleaser.model.FtpUploader;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public abstract class FtpUploaderValidator extends Validator {
    public static void validateFtpUploader(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        Map<String, FtpUploader> ftp = context.getModel().getUpload().getFtp();
        if (!ftp.isEmpty()) context.getLogger().debug("upload.ftp");

        for (Map.Entry<String, FtpUploader> e : ftp.entrySet()) {
            e.getValue().setName(e.getKey());
            if (!mode.validateConfig()) {
                validateFtp(context, mode, e.getValue(), new Errors());
            } else {
                validateFtp(context, mode, e.getValue(), errors);
            }
        }
    }

    private static void validateFtp(JReleaserContext context, JReleaserContext.Mode mode, FtpUploader ftp, Errors errors) {
        context.getLogger().debug("upload.ftp.{}", ftp.getName());

        if (!ftp.isActiveSet()) {
            ftp.setActive(Active.NEVER);
        }
        if (!ftp.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!ftp.isArtifacts() && !ftp.isFiles() && !ftp.isSignatures()) {
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            ftp.disable();
            return;
        }

        ftp.setUsername(
            checkProperty(context,
                "FTP_" + Env.toVar(ftp.getName()) + "_USERNAME",
                "ftp.username",
                ftp.getUsername(),
                errors,
                context.isDryrun()));

        ftp.setPassword(
            checkProperty(context,
                "FTP_" + Env.toVar(ftp.getName()) + "_PASSWORD",
                "ftp.password",
                ftp.getPassword(),
                errors,
                context.isDryrun()));

        ftp.setHost(
            checkProperty(context,
                "FTP_" + Env.toVar(ftp.getName()) + "_HOST",
                "ftp.host",
                ftp.getHost(),
                errors,
                context.isDryrun()));

        ftp.setPort(
            checkProperty(context,
                "FTP_" + Env.toVar(ftp.getName()) + "_PORT",
                "ftp.port",
                ftp.getPort(),
                errors,
                context.isDryrun()));

        if (isBlank(ftp.getPath())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "ftp." + ftp.getName() + ".path"));
        }
        validateTimeout(ftp);
    }
}
