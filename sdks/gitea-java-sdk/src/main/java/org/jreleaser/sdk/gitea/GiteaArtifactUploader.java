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
package org.jreleaser.sdk.gitea;

import feign.form.FormData;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.GiteaUploader;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.uploader.spi.UploadException;
import org.jreleaser.sdk.commons.AbstractArtifactUploader;
import org.jreleaser.sdk.commons.ClientUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class GiteaArtifactUploader extends AbstractArtifactUploader<GiteaUploader> {
    private GiteaUploader uploader;

    public GiteaArtifactUploader(JReleaserContext context) {
        super(context);
    }

    @Override
    public GiteaUploader getUploader() {
        return uploader;
    }

    @Override
    public void setUploader(GiteaUploader uploader) {
        this.uploader = uploader;
    }

    @Override
    public String getType() {
        return GiteaUploader.TYPE;
    }

    @Override
    public void upload(String name) throws UploadException {
        List<Artifact> artifacts = collectArtifacts();
        if (artifacts.isEmpty()) {
            context.getLogger().info(RB.$("artifacts.no.match"));
        }

        String token = uploader.getResolvedToken();

        for (Artifact artifact : artifacts) {
            Path path = artifact.getEffectivePath(context);
            context.getLogger().info(" - {}", path.getFileName());

            if (!context.isDryrun()) {
                try {
                    Map<String, String> headers = new LinkedHashMap<>();
                    headers.put("Authorization", "token " + token);
                    FormData data = ClientUtils.toFormData(path);

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
                } catch (UploadException e) {
                    throw new UploadException(RB.$("ERROR_unexpected_upload",
                        context.getBasedir().relativize(path)), e.getCause());
                }
            }
        }
    }
}
