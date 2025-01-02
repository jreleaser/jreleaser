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
import org.jreleaser.model.internal.download.Download;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.download.FtpDownloaderValidator.validateFtpDownloader;
import static org.jreleaser.model.internal.validation.download.HttpDownloaderValidator.validateHttpDownloader;
import static org.jreleaser.model.internal.validation.download.ScpDownloaderValidator.validateScpDownloader;
import static org.jreleaser.model.internal.validation.download.SftpDownloaderValidator.validateSftpDownloader;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class DownloadersValidator {
    private DownloadersValidator() {
        // noop
    }

    public static void validateDownloaders(JReleaserContext context, Mode mode, Errors errors) {
        Download download = context.getModel().getDownload();
        context.getLogger().debug("download");

        validateFtpDownloader(context, mode, errors);
        validateHttpDownloader(context, mode, errors);
        validateScpDownloader(context, mode, errors);
        validateSftpDownloader(context, mode, errors);

        if (mode.validateConfig() || mode.validateDownload()) {
            boolean activeSet = download.isActiveSet();
            resolveActivatable(context, download, "download", "ALWAYS");
            download.resolveEnabled(context.getModel().getProject());

            if (download.isEnabled()) {
                boolean enabled = !download.getActiveFtps().isEmpty() ||
                    !download.getActiveHttps().isEmpty() ||
                    !download.getActiveScps().isEmpty() ||
                    !download.getActiveSftps().isEmpty();

                if (!activeSet && !enabled) {
                    context.getLogger().debug(RB.$("validation.disabled"));
                    download.disable();
                }
            }
        }
    }
}