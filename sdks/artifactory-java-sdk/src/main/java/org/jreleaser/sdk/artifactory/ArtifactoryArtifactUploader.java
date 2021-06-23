/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
import org.jreleaser.model.Artifactory;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.uploader.spi.UploadException;
import org.jreleaser.sdk.commons.AbstractArtifactUploader;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.ChecksumUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class ArtifactoryArtifactUploader extends AbstractArtifactUploader<Artifactory> {
    private Artifactory uploader;

    public ArtifactoryArtifactUploader(JReleaserContext context) {
        super(context);
    }

    @Override
    public Artifactory getUploader() {
        return uploader;
    }

    @Override
    public void setUploader(Artifactory uploader) {
        this.uploader = uploader;
    }

    @Override
    public String getType() {
        return Artifactory.NAME;
    }

    @Override
    public void upload(String name) throws UploadException {
        List<Path> paths = collectPaths();
        if (paths.isEmpty()) {
            context.getLogger().info("No matching artifacts. Skipping");
        }

        String target = uploader.getResolvedTarget(context);
        String username = uploader.getResolvedUsername();
        String password = uploader.getResolvedPassword();

        for (Path path : paths) {
            context.getLogger().info(" - {}", path.getFileName());

            if (!context.isDryrun()) {
                try {
                    FormData data = ClientUtils.toFormData(path);

                    Map<String, String> headers = new LinkedHashMap<>();
                    switch (uploader.resolveAuthorization()) {
                        case BASIC:
                            String auth = username + ":" + password;
                            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
                            auth = new String(encodedAuth);
                            headers.put("Authorization", "Basic " + auth);
                            break;
                        case BEARER:
                            headers.put("Authorization", "Bearer " + password);
                            break;
                    }

                    headers.put("X-Checksum-Deploy", "false");
                    headers.put("X-Checksum-Sha1", ChecksumUtils.checksum(Algorithm.SHA_1, data.getData()));
                    headers.put("X-Checksum-Sha256", ChecksumUtils.checksum(Algorithm.SHA_256, data.getData()));
                    headers.put("X-Checksum", ChecksumUtils.checksum(Algorithm.MD5, data.getData()));

                    ClientUtils.putFile(context.getLogger(),
                        target,
                        uploader.getConnectTimeout(),
                        uploader.getReadTimeout(),
                        data,
                        headers);
                } catch (IOException e) {
                    context.getLogger().trace(e);
                    throw new UploadException("Unexpected error when deploying " +
                        context.getBasedir().relativize(path), e);
                }
            }
        }
    }
}
