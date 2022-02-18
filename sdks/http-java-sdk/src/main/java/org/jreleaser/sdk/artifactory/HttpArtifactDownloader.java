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
package org.jreleaser.sdk.artifactory;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.HttpDownloader;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.downloader.spi.DownloadException;
import org.jreleaser.sdk.commons.AbstractArtifactDownloader;
import org.jreleaser.util.FileType;
import org.jreleaser.util.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public class HttpArtifactDownloader extends AbstractArtifactDownloader<HttpDownloader> {
    private HttpDownloader downloader;

    public HttpArtifactDownloader(JReleaserContext context) {
        super(context);
    }

    @Override
    public HttpDownloader getDownloader() {
        return downloader;
    }

    @Override
    public void setDownloader(HttpDownloader downloader) {
        this.downloader = downloader;
    }

    @Override
    public String getType() {
        return HttpDownloader.TYPE;
    }

    @Override
    public void download(String name) throws DownloadException {
        String input = downloader.getResolvedInput(context);
        String output = downloader.getResolvedOutput(context);

        if (isBlank(output)) {
            output = Paths.get(input).getFileName().toString();
        }

        Path outputPath = context.getDownloadDirectory().resolve(output);
        context.getLogger().info("{} -> {}", input, context.relativizeToBasedir(outputPath));

        try {
            org.apache.commons.io.FileUtils.copyURLToFile(
                new URL(input),
                outputPath.toFile(),
                downloader.getConnectTimeout() * 1000,
                downloader.getReadTimeout() * 1000);
        } catch (IOException e) {
            throw new DownloadException(RB.$("ERROR_unexpected_download", input), e);
        }

        Optional<FileType> fileType = FileType.getFileType(outputPath);
        if (downloader.getUnpack().isEnabled() && fileType.isPresent() && fileType.get().archive()) {
            try {
                context.getLogger().info(RB.$("downloader.unpack"), outputPath.getFileName().toString());
                FileUtils.unpackArchive(outputPath,
                    context.getDownloadDirectory(),
                    downloader.getUnpack().isSkipRootEntry(),
                    false);
            } catch (IOException e) {
                throw new DownloadException(RB.$("ERROR_download_url_unpack", context.relativizeToBasedir(outputPath)), e);
            }
        }
    }
}
