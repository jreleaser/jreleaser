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
import org.jreleaser.model.Artifact;
import org.jreleaser.model.HttpUploader;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Uploader;
import org.jreleaser.model.uploader.spi.UploadException;
import org.jreleaser.sdk.commons.AbstractArtifactUploader;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.util.Constants;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.getFilename;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public class HttpArtifactUploader extends AbstractArtifactUploader<HttpUploader> {
    private HttpUploader uploader;

    public HttpArtifactUploader(JReleaserContext context) {
        super(context);
    }

    @Override
    public HttpUploader getUploader() {
        return uploader;
    }

    @Override
    public void setUploader(HttpUploader uploader) {
        this.uploader = uploader;
    }

    @Override
    public String getType() {
        return HttpUploader.NAME;
    }

    @Override
    public void upload(String name) throws UploadException {
        List<Artifact> artifacts = collectArtifacts();
        if (artifacts.isEmpty()) {
            context.getLogger().info("No matching artifacts. Skipping");
        }

        String username = uploader.getResolvedUsername();
        String password = uploader.getResolvedPassword();

        for (Artifact artifact : artifacts) {
            Path path = artifact.getEffectivePath(context);
            context.getLogger().info(" - {}", path.getFileName());

            if (!context.isDryrun()) {
                try {
                    FormData data = ClientUtils.toFormData(path);

                    Map<String, String> headers = new LinkedHashMap<>();
                    switch (uploader.resolveAuthorization()) {
                        case NONE:
                            break;
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

                    resolveHeaders(artifact, headers);

                    if (uploader.getMethod() == Uploader.Method.POST) {
                        ClientUtils.postFile(context.getLogger(),
                            resolveUrl(artifact),
                            uploader.getConnectTimeout(),
                            uploader.getReadTimeout(),
                            data,
                            headers);
                    } else {
                        ClientUtils.putFile(context.getLogger(),
                            resolveUrl(artifact),
                            uploader.getConnectTimeout(),
                            uploader.getReadTimeout(),
                            data,
                            headers);
                    }
                } catch (IOException e) {
                    context.getLogger().trace(e);
                    throw new UploadException("Unexpected error when uploading " +
                        context.getBasedir().relativize(path), e);
                }
            }
        }
    }

    private void resolveHeaders(Artifact artifact, Map<String, String> headers) {
        Map<String, Object> props = artifactProps(artifact);
        uploader.getHeaders().forEach((k, v) -> {
            String value = applyTemplate(v, props);
            if (isNotBlank(value)) headers.put(k, value);
        });
    }

    private String resolveUrl(Artifact artifact) {
        return uploader.getResolvedTarget(artifactProps(artifact));
    }

    private Map<String, Object> artifactProps(Artifact artifact) {
        Map<String, Object> props = context.props();

        String platform = isNotBlank(artifact.getPlatform()) ? artifact.getPlatform() : "";
        // add extra properties without clobbering existing keys
        Map<String, Object> artifactProps = artifact.getResolvedExtraProperties("artifact");
        artifactProps.keySet().stream()
            .filter(k -> !props.containsKey(k))
            .filter(k -> !k.startsWith("artifactSkip"))
            .forEach(k -> props.put(k, artifactProps.get(k)));
        String artifactFileName = artifact.getEffectivePath(context).getFileName().toString();
        props.put(Constants.KEY_ARTIFACT_PLATFORM, platform);
        props.put(Constants.KEY_ARTIFACT_FILE_NAME, artifactFileName);
        props.put(Constants.KEY_ARTIFACT_NAME, getFilename(artifactFileName));
        return props;
    }
}
