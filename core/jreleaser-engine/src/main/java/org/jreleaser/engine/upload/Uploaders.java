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
package org.jreleaser.engine.upload;

import org.jreleaser.bundle.RB;
import org.jreleaser.extensions.api.workflow.WorkflowListenerException;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.hooks.ExecutionEvent;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.upload.Upload;
import org.jreleaser.model.internal.upload.Uploader;
import org.jreleaser.model.spi.upload.UploadException;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.internal.JReleaserSupport.supportedUploaders;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public final class Uploaders {
    private Uploaders() {
        // noop
    }

    public static void upload(JReleaserContext context) {
        context.getLogger().info(RB.$("uploaders.header"));
        context.getLogger().increaseIndent();
        context.getLogger().setPrefix("upload");

        Upload upload = context.getModel().getUpload();
        if (!upload.isEnabled()) {
            context.getLogger().info(RB.$("uploaders.not.enabled"));
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
            return;
        }

        try {
            doUpload(context, upload);
        } finally {
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
        }
    }

    private static void doUpload(JReleaserContext context, Upload upload) {
        if (!context.getIncludedUploaderTypes().isEmpty()) {
            for (String uploaderType : context.getIncludedUploaderTypes()) {
                // check if the uploaderType is valid
                if (!supportedUploaders().contains(uploaderType)) {
                    context.getLogger().warn(RB.$("ERROR_unsupported_uploader", uploaderType));
                    continue;
                }

                Map<String, Uploader<?>> uploaders = upload.findUploadersByType(uploaderType);

                if (uploaders.isEmpty()) {
                    context.getLogger().info(RB.$("uploaders.no.match"), uploaderType);
                    return;
                }

                if (!context.getIncludedUploaderNames().isEmpty()) {
                    boolean uploaded = false;
                    for (String uploaderName : context.getIncludedUploaderNames()) {
                        if (!uploaders.containsKey(uploaderName)) {
                            context.getLogger().warn(RB.$("uploaders.uploader.not.configured"), uploaderType, uploaderName);
                            continue;
                        }

                        Uploader<?> uploader = uploaders.get(uploaderName);
                        if (!uploader.isEnabled()) {
                            context.getLogger().info(RB.$("uploaders.uploader.disabled"), uploaderType, uploaderName);
                            continue;
                        }

                        context.getLogger().info(RB.$("uploaders.upload.with"),
                            uploaderType,
                            uploaderName);
                        if (upload(context, uploader)) uploaded = true;
                    }

                    if (!uploaded) {
                        context.getLogger().info(RB.$("uploaders.not.triggered"));
                    }
                } else {
                    context.getLogger().info(RB.$("uploaders.upload.all.artifacts.with"), uploaderType);
                    boolean[] uploaded = new boolean[]{false};
                    uploaders.values().forEach(uploader -> {
                        if (upload(context, uploader)) uploaded[0] = true;
                    });

                    if (!uploaded[0]) {
                        context.getLogger().info(RB.$("uploaders.not.triggered"));
                    }
                }
            }
        } else if (!context.getIncludedUploaderNames().isEmpty()) {
            boolean[] uploaded = new boolean[]{false};
            for (String uploaderName : context.getIncludedUploaderNames()) {
                List<Uploader<?>> filteredUploaders = upload.findAllActiveUploaders().stream()
                    .filter(a -> uploaderName.equals(a.getName()))
                    .collect(toList());

                if (!filteredUploaders.isEmpty()) {
                    context.getLogger().info(RB.$("uploaders.upload.all.artifacts.to"), uploaderName);
                    filteredUploaders.forEach(uploader -> {
                        if (upload(context, uploader)) uploaded[0] = true;
                    });
                } else {
                    context.getLogger().warn(RB.$("uploaders.uploader.not.configured2"), uploaderName);
                }
            }

            if (!uploaded[0]) {
                context.getLogger().info(RB.$("uploaders.not.triggered"));
            }
        } else {
            boolean uploaded = false;
            context.getLogger().info(RB.$("uploaders.upload.all.artifacts"));
            for (Uploader<?> uploader : upload.findAllActiveUploaders()) {
                String uploaderType = uploader.getType();
                String uploaderName = uploader.getName();

                if (context.getExcludedUploaderTypes().contains(uploaderType) ||
                    context.getExcludedUploaderNames().contains(uploaderName)) {
                    context.getLogger().info(RB.$("uploaders.uploader.excluded"), uploaderType, uploaderName);
                    continue;
                }

                if (upload(context, uploader)) uploaded = true;
            }

            if (!uploaded) {
                context.getLogger().info(RB.$("uploaders.not.triggered"));
            }
        }
    }

    private static boolean upload(JReleaserContext context, Uploader<?> uploader) {
        try {
            context.getLogger().increaseIndent();
            context.getLogger().setPrefix(uploader.getType());
            fireUploadEvent(ExecutionEvent.before(JReleaserCommand.UPLOAD.toStep()), context, uploader);

            ProjectUploader projectUploader = createProjectUploader(context, uploader);
            boolean uploaded = projectUploader.upload();

            fireUploadEvent(ExecutionEvent.success(JReleaserCommand.UPLOAD.toStep()), context, uploader);
            return uploaded;
        } catch (UploadException e) {
            fireUploadEvent(ExecutionEvent.failure(JReleaserCommand.UPLOAD.toStep(), e), context, uploader);
            throw new JReleaserException(RB.$("ERROR_unexpected_error"), e);
        } finally {
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
        }
    }

    private static ProjectUploader createProjectUploader(JReleaserContext context,
                                                         Uploader<?> uploader) {
        return ProjectUploader.builder()
            .context(context)
            .uploader(uploader)
            .build();
    }

    private static void fireUploadEvent(ExecutionEvent event, JReleaserContext context, Uploader<?> uploader) {
        if (!uploader.isEnabled()) return;

        try {
            context.fireUploadStepEvent(event, uploader.asImmutable());
        } catch (WorkflowListenerException e) {
            context.getLogger().error(RB.$("listener.failure", e.getListener().getClass().getName()));
            context.getLogger().trace(e);
            if (event.getType() != ExecutionEvent.Type.FAILURE && !e.getListener().isContinueOnError()) {
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                } else {
                    throw new JReleaserException(RB.$("ERROR_unexpected_error"), e.getCause());
                }
            }
        }
    }
}
