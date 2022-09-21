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
package org.jreleaser.model.internal.validation.download;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.download.Downloader;
import org.jreleaser.model.internal.download.FtpDownloader;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public abstract class FtpDownloaderValidator extends Validator {
    public static void validateFtpDownloader(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, FtpDownloader> ftp = context.getModel().getDownload().getFtp();
        if (!ftp.isEmpty()) context.getLogger().debug("download.ftp");

        for (Map.Entry<String, FtpDownloader> e : ftp.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig() || mode.validateDownload()) {
                validateFtp(context, mode, e.getValue(), errors);
            }
        }
    }

    private static void validateFtp(JReleaserContext context, Mode mode, FtpDownloader ftp, Errors errors) {
        context.getLogger().debug("download.ftp.{}", ftp.getName());

        if (!ftp.isActiveSet()) {
            ftp.setActive(Active.ALWAYS);
        }
        if (!ftp.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        // allow anonymous access
        ftp.setUsername(
            checkProperty(context,
                "FTP_" + Env.toVar(ftp.getName()) + "_USERNAME",
                "ftp.username",
                ftp.getUsername(),
                errors,
                true));

        ftp.setPassword(
            checkProperty(context,
                "FTP_" + Env.toVar(ftp.getName()) + "_PASSWORD",
                "ftp.password",
                ftp.getPassword(),
                errors,
                true));

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

        validateTimeout(ftp);

        if (ftp.getAssets().isEmpty()) {
            errors.configuration(RB.$("validation_must_not_be_empty", "ftp." + ftp.getName() + ".assets"));
        } else {
            int index = 0;
            for (Downloader.Asset asset : ftp.getAssets()) {
                if (isBlank(asset.getInput())) {
                    errors.configuration(RB.$("validation_must_not_be_null", "ftp." + ftp.getName() + ".asset[" + (index++) + "].input"));
                }
            }
        }
    }
}
