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
package org.jreleaser.engine.upload;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Upload;
import org.jreleaser.model.Uploader;
import org.jreleaser.model.uploader.spi.UploadException;
import org.jreleaser.util.JReleaserException;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class Uploaders {
    public static void upload(JReleaserContext context) {
        Upload upload = context.getModel().getUpload();
        if (!upload.isEnabled()) {
            context.getLogger().info(RB.$("uploaders.not.enabled"));
            return;
        }

        if (!context.getIncludedUploaderTypes().isEmpty()) {
            for (String uploaderType : context.getIncludedUploaderTypes()) {
                // check if the uploaderType is valid
                if (!Upload.supportedUploaders().contains(uploaderType)) {
                    context.getLogger().warn(RB.$("ERROR_unsupported_uploader", uploaderType));
                    continue;
                }

                Map<String, Uploader> uploaders = upload.findUploadersByType(uploaderType);

                if (uploaders.isEmpty()) {
                    context.getLogger().debug(RB.$("uploaders.no.match"), uploaderType);
                    return;
                }

                if (!context.getIncludedUploaderNames().isEmpty()) {
                    for (String uploaderName : context.getIncludedUploaderNames()) {
                        if (!uploaders.containsKey(uploaderName)) {
                            context.getLogger().warn(RB.$("uploaders.uploader.not.configured"), uploaderType, uploaderName);
                            continue;
                        }

                        Uploader uploader = uploaders.get(uploaderName);
                        if (!uploader.isEnabled()) {
                            context.getLogger().info(RB.$("uploaders.uploader.disabled"), uploaderType, uploaderName);
                            continue;
                        }

                        context.getLogger().info(RB.$("uploaders.upload.with"),
                            uploaderType,
                            uploaderName);
                        upload(context, uploader);
                    }
                } else {
                    context.getLogger().info(RB.$("uploaders.upload.all.artifacts.with"), uploaderType);
                    uploaders.values().forEach(uploader -> upload(context, uploader));
                }
            }
        } else if (!context.getIncludedUploaderNames().isEmpty()) {
            for (String uploaderName : context.getIncludedUploaderNames()) {
                List<Uploader> filteredUploaders = upload.findAllActiveUploaders().stream()
                    .filter(a -> uploaderName.equals(a.getName()))
                    .collect(toList());

                if (!filteredUploaders.isEmpty()) {
                    context.getLogger().info(RB.$("uploaders.upload.all.artifacts.to"), uploaderName);
                    filteredUploaders.forEach(uploader -> upload(context, uploader));
                } else {
                    context.getLogger().warn(RB.$("uploaders.uploader.not.configured2"), uploaderName);
                }
            }
        } else {
            context.getLogger().info(RB.$("uploaders.upload.all.artifacts"));
            for (Uploader uploader : upload.findAllActiveUploaders()) {
                String uploaderType = uploader.getType();
                String uploaderName = uploader.getName();

                if (context.getExcludedUploaderTypes().contains(uploaderType) ||
                    context.getExcludedUploaderNames().contains(uploaderName)) {
                    context.getLogger().info(RB.$("uploaders.uploader.excluded"), uploaderType, uploaderName);
                    continue;
                }

                upload(context, uploader);
            }
        }
    }

    private static void upload(JReleaserContext context, Uploader uploader) {
        try {
            context.getLogger().increaseIndent();
            context.getLogger().setPrefix(uploader.getType());
            ProjectUploader projectUploader = createProjectUploader(context, uploader);
            projectUploader.upload();
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
        } catch (UploadException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private static ProjectUploader createProjectUploader(JReleaserContext context,
                                                         Uploader uploader) {
        return ProjectUploader.builder()
            .context(context)
            .uploader(uploader)
            .build();
    }
}
