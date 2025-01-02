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
package org.jreleaser.sdk.commons;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.download.Downloader;
import org.jreleaser.model.spi.download.ArtifactDownloader;
import org.jreleaser.model.spi.download.DownloadException;
import org.jreleaser.util.FileType;
import org.jreleaser.util.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public abstract class AbstractArtifactDownloader<A extends org.jreleaser.model.api.download.Downloader, D extends Downloader<A>> implements ArtifactDownloader<A, D> {
    protected final JReleaserContext context;

    protected AbstractArtifactDownloader(JReleaserContext context) {
        this.context = context;
    }

    protected void unpack(Downloader.Unpack unpack, Path outputPath) throws DownloadException {
        Optional<FileType> fileType = FileType.getFileType(outputPath);
        if (unpack.isEnabled() && fileType.isPresent() && fileType.get().archive()) {
            try {
                context.getLogger().info(RB.$("downloader.unpack"), outputPath.getFileName().toString());
                if (context.isDryrun()) return;
                FileUtils.unpackArchive(outputPath,
                    outputPath.getParent(),
                    unpack.isSkipRootEntry(),
                    false);
            } catch (IOException e) {
                throw new DownloadException(RB.$("ERROR_download_url_unpack", context.relativizeToBasedir(outputPath)), e);
            }
        }
    }
}
