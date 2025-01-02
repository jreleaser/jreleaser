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
package org.jreleaser.engine.download;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.download.Downloader;
import org.jreleaser.model.spi.download.ArtifactDownloader;
import org.jreleaser.model.spi.download.DownloadException;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public class ProjectDownloader {
    private final JReleaserContext context;
    private final Downloader<?> downloader;

    private ProjectDownloader(JReleaserContext context,
                              Downloader<?> downloader) {
        this.context = context;
        this.downloader = downloader;
    }

    public Downloader<?> getDownloader() {
        return downloader;
    }

    public void download() throws DownloadException {
        if (!downloader.isEnabled()) {
            context.getLogger().debug(RB.$("downloaders.skip.download"), downloader.getName());
            return;
        }

        ArtifactDownloader<?, ?> artifactDownloader = ArtifactDownloaders.findDownloader(context, downloader);

        context.getLogger().info(RB.$("downloaders.download"), downloader.getName());

        artifactDownloader.download(downloader.getName());
    }

    public static ProjectDownloaderBuilder builder() {
        return new ProjectDownloaderBuilder();
    }

    public static class ProjectDownloaderBuilder {
        private JReleaserContext context;
        private Downloader<?> downloader;

        public ProjectDownloaderBuilder context(JReleaserContext context) {
            this.context = requireNonNull(context, "'context' must not be null");
            return this;
        }

        public ProjectDownloaderBuilder downloader(Downloader<?> downloader) {
            this.downloader = requireNonNull(downloader, "'downloader' must not be null");
            return this;
        }

        public ProjectDownloader build() {
            requireNonNull(context, "'context' must not be null");
            requireNonNull(downloader, "'downloader' must not be null");
            return new ProjectDownloader(context, downloader);
        }
    }
}
