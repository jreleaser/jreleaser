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
package org.jreleaser.sdk.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.upload.FtpUploader;
import org.jreleaser.model.spi.upload.UploadException;
import org.jreleaser.sdk.commons.AbstractArtifactUploader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class FtpArtifactUploader extends AbstractArtifactUploader<org.jreleaser.model.api.upload.FtpUploader, FtpUploader> {
    private FtpUploader uploader;

    public FtpArtifactUploader(JReleaserContext context) {
        super(context);
    }

    @Override
    public FtpUploader getUploader() {
        return uploader;
    }

    @Override
    public void setUploader(FtpUploader uploader) {
        this.uploader = uploader;
    }

    @Override
    public String getType() {
        return org.jreleaser.model.api.upload.FtpUploader.TYPE;
    }

    @Override
    public void upload(String name) throws UploadException {
        Set<Artifact> artifacts = collectArtifacts();
        if (artifacts.isEmpty()) {
            context.getLogger().info(RB.$("artifacts.no.match"));
        }

        FTPClient ftp = FtpUtils.open(context, uploader);

        try {
            for (Artifact artifact : artifacts) {
                Path path = artifact.getEffectivePath(context);
                context.getLogger().info(" - {}", path.getFileName());

                if (!context.isDryrun()) {
                    try (InputStream in = Files.newInputStream(path)) {
                        String uploadPath = uploader.getResolvedPath(context, artifact);
                        context.getLogger().debug("   " + RB.$("uploader.uploading.to", uploadPath));
                        ftp.storeFile(uploadPath, in);
                    } catch (IOException e) {
                        context.getLogger().trace(e);
                        throw new UploadException(RB.$("ERROR_unexpected_upload",
                            context.getBasedir().relativize(path)), e);
                    }
                }
            }
        } finally {
            FtpUtils.close(uploader, ftp);
        }
    }
}
