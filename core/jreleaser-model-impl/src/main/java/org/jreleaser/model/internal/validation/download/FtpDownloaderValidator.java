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
package org.jreleaser.model.internal.validation.download;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.download.Downloader;
import org.jreleaser.model.internal.download.FtpDownloader;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class FtpDownloaderValidator {
    private FtpDownloaderValidator() {
        // noop
    }

    public static void validateFtpDownloader(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, FtpDownloader> ftp = context.getModel().getDownload().getFtp();
        if (!ftp.isEmpty()) context.getLogger().debug("download.ftp");

        for (Map.Entry<String, FtpDownloader> e : ftp.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig() || mode.validateDownload()) {
                validateFtp(context, e.getValue(), errors);
            }
        }
    }

    private static void validateFtp(JReleaserContext context, FtpDownloader ftp, Errors errors) {
        context.getLogger().debug("download.ftp.{}", ftp.getName());

        resolveActivatable(context, ftp,
            listOf("download.ftp." + ftp.getName(), "download.ftp"),
            "ALWAYS");
        if (!ftp.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        // allow anonymous access
        String baseKey1 = "download.ftp." + ftp.getName();
        String baseKey2 = "download.ftp";
        String baseKey3 = "ftp." + ftp.getName();
        String baseKey4 = "ftp";
        ftp.setUsername(
            checkProperty(context,
                listOf(
                    baseKey1 + ".username",
                    baseKey2 + ".username",
                    baseKey3 + ".username",
                    baseKey4 + ".username"),
                baseKey1 + ".username",
                ftp.getUsername(),
                errors,
                true));

        ftp.setPassword(
            checkProperty(context,
                listOf(
                    baseKey1 + ".password",
                    baseKey2 + ".password",
                    baseKey3 + ".password",
                    baseKey4 + ".password"),
                baseKey1 + ".password",
                ftp.getPassword(),
                errors,
                true));

        ftp.setHost(
            checkProperty(context,
                listOf(
                    baseKey1 + ".host",
                    baseKey2 + ".host",
                    baseKey3 + ".host",
                    baseKey4 + ".host"),
                baseKey1 + ".host",
                ftp.getHost(),
                errors,
                context.isDryrun()));

        ftp.setPort(
            checkProperty(context,
                listOf(
                    baseKey1 + ".port",
                    baseKey2 + ".port",
                    baseKey3 + ".port",
                    baseKey4 + ".port"),
                baseKey1 + ".port",
                ftp.getPort(),
                errors,
                context.isDryrun()));

        validateTimeout(ftp);

        if (ftp.getAssets().isEmpty()) {
            errors.configuration(RB.$("validation_must_not_be_empty", baseKey1 + ".assets"));
        } else {
            int index = 0;
            for (Downloader.Asset asset : ftp.getAssets()) {
                if (isBlank(asset.getInput())) {
                    errors.configuration(RB.$("validation_must_not_be_null", baseKey1 + ".asset[" + (index++) + "].input"));
                }
            }
        }
    }
}
