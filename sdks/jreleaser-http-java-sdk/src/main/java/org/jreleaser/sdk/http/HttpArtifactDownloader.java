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
package org.jreleaser.sdk.http;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.download.Downloader;
import org.jreleaser.model.internal.download.HttpDownloader;
import org.jreleaser.model.spi.download.DownloadException;
import org.jreleaser.sdk.commons.AbstractArtifactDownloader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public class HttpArtifactDownloader extends AbstractArtifactDownloader<org.jreleaser.model.api.download.HttpDownloader, HttpDownloader> {
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
        return org.jreleaser.model.api.download.HttpDownloader.TYPE;
    }

    @Override
    public void download(String name) throws DownloadException {
        for (Downloader.Asset asset : downloader.getAssets()) {
            downloadAsset(name, asset);
        }
    }

    private void downloadAsset(String name, Downloader.Asset asset) throws DownloadException {
        String input = asset.getResolvedInput(context, downloader);
        String output = asset.getResolvedOutput(context, downloader, getFilename(input));

        if (isBlank(output)) {
            output = getFilename(input);
        }

        Path outputPath = context.getDownloadDirectory().resolve(name).resolve(output);
        context.getLogger().info("{} -> {}", input, context.relativizeToBasedir(outputPath));

        if (!context.isDryrun()) {
            try {
                org.apache.commons.io.FileUtils.copyURLToFile(
                    new URI(input).toURL(),
                    outputPath.toFile(),
                    downloader.getConnectTimeout() * 1000,
                    downloader.getReadTimeout() * 1000);
            } catch (URISyntaxException | IOException e) {
                throw new DownloadException(RB.$("ERROR_unexpected_download", input), e);
            }
        }

        unpack(asset.getUnpack(), outputPath);
    }

    private String getFilename(String name) {
        return name.substring(name.lastIndexOf('/') + 1);
    }
}
