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
import org.jreleaser.model.Downloader;
import org.jreleaser.model.FtpDownloader;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public abstract class FtpDownloaderValidator extends Validator {
    public static void validateFtpDownloader(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().debug("ftp");
        Map<String, FtpDownloader> ftp = context.getModel().getDownload().getFtp();

        for (Map.Entry<String, FtpDownloader> e : ftp.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateDownload() || mode.validateConfig()) {
                validateFtp(context, mode, e.getValue(), errors);
            } else {
                validateFtp(context, mode, e.getValue(), new Errors());
            }
        }
    }

    private static void validateFtp(JReleaserContext context, JReleaserContext.Mode mode, FtpDownloader ftp, Errors errors) {
        context.getLogger().debug("ftp.{}", ftp.getName());

        if (!ftp.isActiveSet()) {
            ftp.setActive(Active.ALWAYS);
        }
        if (!ftp.resolveEnabled(context.getModel().getProject())) {
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
