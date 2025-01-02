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
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.upload.ScpUploader;
import org.jreleaser.model.spi.upload.UploadException;
import org.jreleaser.sdk.commons.AbstractArtifactUploader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.jreleaser.sdk.ssh.SshUtils.createDirectories;
import static org.jreleaser.sdk.ssh.SshUtils.createSSHClient;
import static org.jreleaser.sdk.ssh.SshUtils.disconnect;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class ScpArtifactUploader extends AbstractArtifactUploader<org.jreleaser.model.api.upload.ScpUploader, ScpUploader> {
    private ScpUploader uploader;

    public ScpArtifactUploader(JReleaserContext context) {
        super(context);
    }

    @Override
    public ScpUploader getUploader() {
        return uploader;
    }

    @Override
    public void setUploader(ScpUploader uploader) {
        this.uploader = uploader;
    }

    @Override
    public String getType() {
        return org.jreleaser.model.api.upload.ScpUploader.TYPE;
    }

    @Override
    public void upload(String name) throws UploadException {
        Set<Artifact> artifacts = collectArtifacts();
        if (artifacts.isEmpty()) {
            context.getLogger().info(RB.$("artifacts.no.match"));
        }

        SSHClient ssh = createSSHClient(context, uploader);

        try {
            for (Artifact artifact : artifacts) {
                Path path = artifact.getEffectivePath(context);
                context.getLogger().info(" - {}", path.getFileName());

                if (!context.isDryrun()) {
                    try {
                        String uploadPath = uploader.getResolvedPath(context, artifact);
                        context.getLogger().debug("   " + RB.$("uploader.uploading.to", uploadPath));
                        createDirectories(context, uploader, ssh, Paths.get(uploadPath).getParent());
                        ssh.newSCPFileTransfer().upload(path.toAbsolutePath().toString(), uploadPath);
                    } catch (IOException e) {
                        context.getLogger().trace(e);
                        throw new UploadException(RB.$("ERROR_unexpected_upload",
                            context.getBasedir().relativize(path)), e);
                    }
                }
            }
        } finally {
            disconnect(uploader, ssh);
        }
    }
}
