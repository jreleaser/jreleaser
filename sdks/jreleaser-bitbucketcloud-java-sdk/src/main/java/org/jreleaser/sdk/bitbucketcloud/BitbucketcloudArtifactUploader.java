/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
package org.jreleaser.sdk.bitbucketcloud;

import feign.Response;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.upload.BitbucketcloudUploader;
import org.jreleaser.model.spi.upload.UploadException;
import org.jreleaser.sdk.commons.AbstractArtifactUploader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/**
 * @author Hasnae Rehioui
 * @since 1.7.0
 */
public class BitbucketcloudArtifactUploader extends AbstractArtifactUploader<org.jreleaser.model.api.upload.BitbucketcloudUploader, BitbucketcloudUploader> {

    private BitbucketcloudUploader uploader;

    public BitbucketcloudArtifactUploader(JReleaserContext context) {
        super(context);
    }

    @Override
    public BitbucketcloudUploader getUploader() {
        return uploader;
    }

    @Override
    public void setUploader(BitbucketcloudUploader uploader) {
        this.uploader = uploader;
    }

    @Override
    public String getType() {
        return org.jreleaser.model.api.upload.BitbucketcloudUploader.TYPE;
    }

    @Override
    public void upload(String name) throws UploadException {
        Set<Artifact> artifacts = collectArtifacts();
        if (artifacts.isEmpty()) {
            context.getLogger().info(RB.$("artifacts.no.match"));
        }

        String token = uploader.getToken();
        Bitbucketcloud api = new Bitbucketcloud(context.getLogger(), token, uploader.getConnectTimeout(), uploader.getReadTimeout());

        for (Artifact artifact : artifacts) {
            Path path = artifact.getEffectivePath(context);
            context.getLogger().info(" - {}", path.getFileName());

            if (context.isDryrun()) {
                continue;
            }

            try {
                Response response = api.uploadArtifact(uploader.getProjectIdentifier(), uploader.getPackageName(), path);
                context.getLogger().info(RB.$("uploader.uploading.to", "bitbucketcloud: " + response.reason()));
            } catch (IOException e) {
                context.getLogger().trace(e);
                throw new UploadException(RB.$("ERROR_unexpected_upload",
                    context.getBasedir().relativize(path)), e);
            }
        }
    }
}
