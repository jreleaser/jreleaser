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
package org.jreleaser.sdk.ssh;

import net.schmizz.sshj.SSHClient;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.download.Downloader;
import org.jreleaser.model.internal.download.ScpDownloader;
import org.jreleaser.model.spi.download.DownloadException;
import org.jreleaser.sdk.commons.AbstractArtifactDownloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.jreleaser.sdk.ssh.SshUtils.createSSHClient;
import static org.jreleaser.sdk.ssh.SshUtils.disconnect;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public class ScpArtifactDownloader extends AbstractArtifactDownloader<org.jreleaser.model.api.download.ScpDownloader, ScpDownloader> {
    private ScpDownloader downloader;

    public ScpArtifactDownloader(JReleaserContext context) {
        super(context);
    }

    @Override
    public ScpDownloader getDownloader() {
        return downloader;
    }

    @Override
    public void setDownloader(ScpDownloader downloader) {
        this.downloader = downloader;
    }

    @Override
    public String getType() {
        return org.jreleaser.model.api.download.ScpDownloader.TYPE;
    }

    @Override
    public void download(String name) throws DownloadException {
        SSHClient ssh = createSSHClient(context, downloader);

        try {
            for (Downloader.Asset asset : downloader.getAssets()) {
                downloadAsset(name, ssh, asset);
            }
        } finally {
            disconnect(downloader, ssh);
        }
    }

    private void downloadAsset(String name, SSHClient ssh, Downloader.Asset asset) throws DownloadException {
        String input = asset.getResolvedInput(context, downloader);
        String output = asset.getResolvedOutput(context, downloader, Paths.get(input).getFileName().toString());

        if (isBlank(output)) {
            output = Paths.get(input).getFileName().toString();
        }

        Path outputPath = context.getDownloadDirectory().resolve(name).resolve(output);
        context.getLogger().info("{} -> {}", input, context.relativizeToBasedir(outputPath));

        if (!context.isDryrun()) {
            try {
                Files.createDirectories(outputPath.toAbsolutePath().getParent());
                ssh.newSCPFileTransfer().download(input, outputPath.toAbsolutePath().toString());
            } catch (IOException e) {
                throw new DownloadException(RB.$("ERROR_unexpected_download", input), e);
            }
        }

        unpack(asset.getUnpack(), outputPath);
    }
}
