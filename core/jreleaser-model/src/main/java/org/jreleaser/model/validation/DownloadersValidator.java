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

import org.jreleaser.model.Download;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.validation.FtpDownloaderValidator.validateFtpDownloader;
import static org.jreleaser.model.validation.HttpDownloaderValidator.validateHttpDownloader;
import static org.jreleaser.model.validation.ScpDownloaderValidator.validateScpDownloader;
import static org.jreleaser.model.validation.SftpDownloaderValidator.validateSftpDownloader;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public abstract class DownloadersValidator extends Validator {
    public static void validateDownloaders(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        if (mode == JReleaserContext.Mode.CHANGELOG) {
            return;
        }

        context.getLogger().debug("download");

        Download download = context.getModel().getDownload();
        validateFtpDownloader(context, mode, errors);
        validateHttpDownloader(context, mode, errors);
        validateScpDownloader(context, mode, errors);
        validateSftpDownloader(context, mode, errors);

        boolean activeSet = download.isActiveSet();
        if (mode.validateConfig() || mode.validateDownload()) {
            download.resolveEnabled(context.getModel().getProject());
        }

        if (download.isEnabled()) {
            boolean enabled = !download.getActiveFtps().isEmpty() ||
                !download.getActiveHttps().isEmpty() ||
                !download.getActiveScps().isEmpty() ||
                !download.getActiveSftps().isEmpty();

            if (!activeSet && !enabled) download.disable();
        }
    }
}