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
package org.jreleaser.sdk.artifactory;

import feign.form.FormData;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.upload.ArtifactoryUploader;
import org.jreleaser.model.spi.upload.UploadException;
import org.jreleaser.sdk.commons.AbstractArtifactUploader;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.ChecksumUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class ArtifactoryArtifactUploader extends AbstractArtifactUploader<org.jreleaser.model.api.upload.ArtifactoryUploader, ArtifactoryUploader> {
    private ArtifactoryUploader uploader;

    public ArtifactoryArtifactUploader(JReleaserContext context) {
        super(context);
    }

    @Override
    public ArtifactoryUploader getUploader() {
        return uploader;
    }

    @Override
    public void setUploader(ArtifactoryUploader uploader) {
        this.uploader = uploader;
    }

    @Override
    public String getType() {
        return org.jreleaser.model.api.upload.ArtifactoryUploader.TYPE;
    }

    @Override
    public void upload(String name) throws UploadException {
        Set<Artifact> artifacts = collectArtifacts();
        if (artifacts.isEmpty()) {
            context.getLogger().info(RB.$("artifacts.no.match"));
        }

        String username = uploader.getUsername();
        String password = uploader.getPassword();

        for (Artifact artifact : artifacts) {
            Path path = artifact.getEffectivePath(context);
            context.getLogger().info(" - {}", path.getFileName());

            if (!context.isDryrun()) {
                try {
                    FormData data = ClientUtils.toFormData(path);

                    Map<String, String> headers = new LinkedHashMap<>();
                    switch (uploader.resolveAuthorization()) {
                        case BASIC:
                            String auth = username + ":" + password;
                            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(UTF_8));
                            auth = new String(encodedAuth, UTF_8);
                            headers.put("Authorization", "Basic " + auth);
                            break;
                        case BEARER:
                            headers.put("Authorization", "Bearer " + password);
                            break;
                        default:
                            // noop
                    }

                    headers.put("X-Checksum-Deploy", "false");
                    headers.put("X-Checksum-Sha1", ChecksumUtils.checksum(Algorithm.SHA_1, data.getData()));
                    headers.put("X-Checksum-Sha256", ChecksumUtils.checksum(Algorithm.SHA_256, data.getData()));
                    headers.put("X-Checksum", ChecksumUtils.checksum(Algorithm.MD5, data.getData()));

                    ClientUtils.putFile(context.getLogger(),
                        uploader.getResolvedUploadUrl(context, artifact),
                        uploader.getConnectTimeout(),
                        uploader.getReadTimeout(),
                        data,
                        headers);
                } catch (IOException e) {
                    context.getLogger().trace(e);
                    throw new UploadException(RB.$("ERROR_unexpected_upload",
                        context.getBasedir().relativize(path)), e);
                }
            }
        }
    }
}
