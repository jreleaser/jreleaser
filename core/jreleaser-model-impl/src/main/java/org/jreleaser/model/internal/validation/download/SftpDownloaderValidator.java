/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
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
import org.jreleaser.model.internal.download.SftpDownloader;
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
public final class SftpDownloaderValidator {
    private SftpDownloaderValidator() {
        // noop
    }

    public static void validateSftpDownloader(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, SftpDownloader> sftp = context.getModel().getDownload().getSftp();
        if (!sftp.isEmpty()) context.getLogger().debug("download.sftp");

        for (Map.Entry<String, SftpDownloader> e : sftp.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig() || mode.validateDownload()) {
                validateSftpDownloader(context, e.getValue(), errors);
            }
        }
    }

    private static void validateSftpDownloader(JReleaserContext context, SftpDownloader downloader, Errors errors) {
        context.getLogger().debug("download.sftp.{}", downloader.getName());

        resolveActivatable(context, downloader,
            listOf("download.sftp." + downloader.getName(), "download.sftp"),
            "ALWAYS");
        if (!downloader.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        validateSsh(context, downloader, downloader.getType(), downloader.getName(), "download.", errors);
        validateTimeout(downloader);

        if (downloader.getAssets().isEmpty()) {
            errors.configuration(RB.$("validation_must_not_be_empty", "download.sftp." + downloader.getName() + ".assets"));
        } else {
            int index = 0;
            for (Downloader.Asset asset : downloader.getAssets()) {
                if (isBlank(asset.getInput())) {
                    errors.configuration(RB.$("validation_must_not_be_null", "download.sftp." + downloader.getName() + ".asset[" + (index++) + "].input"));
                }
            }
        }
    }
}
