/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.Upload;
import org.jreleaser.model.Uploader;
import org.jreleaser.model.uploader.spi.UploadException;

import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class Uploaders {
    public static void upload(JReleaserContext context) {
        Upload upload = context.getModel().getUpload();
        if (!upload.isEnabled()) {
            context.getLogger().info("Uploading is not enabled. Skipping.");
            return;
        }

        if (context.hasUploaderType()) {
            Map<String, Uploader> uploaders = upload.findUploadersByType(context.getUploaderType());

            if (uploaders.isEmpty()) {
                context.getLogger().debug("No uploaders match {}", context.getUploaderType());
                return;
            }

            if (context.hasUploaderName()) {
                if (!uploaders.containsKey(context.getUploaderName())) {
                    context.getLogger().error("Uploader {}/{} is not configured",
                        context.getUploaderType(),
                        context.getUploaderName());
                    return;
                }

                context.getLogger().info("Uploading with {}/{}",
                    context.getUploaderType(),
                    context.getUploaderName());
                upload(context, uploaders.get(context.getUploaderName()));
            } else {
                context.getLogger().info("Uploading all artifacts with {}",
                    context.getUploaderType());
                uploaders.values().forEach(uploader -> upload(context, uploader));
            }
        } else if (context.hasUploaderName()) {
            context.getLogger().info("Uploading all artifacts to {}",
                context.getUploaderName());
            upload.findAllUploaders().stream()
                .filter(a -> context.getUploaderName().equals(a.getName()))
                .forEach(uploader -> upload(context, uploader));
        } else {
            context.getLogger().info("Uploading all artifacts");
            upload.findAllUploaders().forEach(uploader -> upload(context, uploader));
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
            throw new JReleaserException("Unexpected error", e);
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
