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
package org.jreleaser.engine.download;

import org.jreleaser.bundle.RB;
import org.jreleaser.extensions.api.workflow.WorkflowListenerException;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.hooks.ExecutionEvent;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.download.Download;
import org.jreleaser.model.internal.download.Downloader;
import org.jreleaser.model.spi.download.DownloadException;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.internal.JReleaserSupport.supportedDownloaders;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class Downloaders {
    private Downloaders() {
        // noop
    }

    public static void download(JReleaserContext context) {
        Download download = context.getModel().getDownload();
        if (!download.isEnabled()) {
            context.getLogger().info(RB.$("downloaders.not.enabled"));
            return;
        }

        if (!context.getIncludedDownloaderTypes().isEmpty()) {
            for (String downloaderType : context.getIncludedDownloaderTypes()) {
                // check if the downloaderType is valid
                if (!supportedDownloaders().contains(downloaderType)) {
                    context.getLogger().warn(RB.$("ERROR_unsupported_downloader", downloaderType));
                    continue;
                }

                Map<String, Downloader<?>> downloaders = download.findDownloadersByType(downloaderType);

                if (downloaders.isEmpty()) {
                    context.getLogger().debug(RB.$("downloaders.no.match"), downloaderType);
                    return;
                }

                if (!context.getIncludedDownloaderNames().isEmpty()) {
                    for (String downloaderName : context.getIncludedDownloaderNames()) {
                        if (!downloaders.containsKey(downloaderName)) {
                            context.getLogger().warn(RB.$("downloaders.downloader.not.configured"), downloaderType, downloaderName);
                            continue;
                        }

                        Downloader<?> downloader = downloaders.get(downloaderName);
                        if (!downloader.isEnabled()) {
                            context.getLogger().info(RB.$("downloaders.downloader.disabled"), downloaderType, downloaderName);
                            continue;
                        }

                        context.getLogger().info(RB.$("downloaders.download.with"),
                            downloaderType,
                            downloaderName);
                        download(context, downloader);
                    }
                } else {
                    context.getLogger().info(RB.$("downloaders.download.all.artifacts.with"), downloaderType);
                    downloaders.values().forEach(downloader -> download(context, downloader));
                }
            }
        } else if (!context.getIncludedDownloaderNames().isEmpty()) {
            for (String downloaderName : context.getIncludedDownloaderNames()) {
                List<Downloader<?>> filteredDownloaders = download.findAllActiveDownloaders().stream()
                    .filter(a -> downloaderName.equals(a.getName()))
                    .collect(toList());

                if (!filteredDownloaders.isEmpty()) {
                    context.getLogger().info(RB.$("downloaders.download.all.artifacts.from"), downloaderName);
                    filteredDownloaders.forEach(downloader -> download(context, downloader));
                } else {
                    context.getLogger().warn(RB.$("downloaders.downloader.not.configured2"), downloaderName);
                }
            }
        } else {
            context.getLogger().info(RB.$("downloaders.download.all.artifacts"));
            for (Downloader<?> downloader : download.findAllActiveDownloaders()) {
                String downloaderType = downloader.getType();
                String downloaderName = downloader.getName();

                if (context.getExcludedDownloaderTypes().contains(downloaderType) ||
                    context.getExcludedDownloaderNames().contains(downloaderName)) {
                    context.getLogger().info(RB.$("downloaders.downloader.excluded"), downloaderType, downloaderName);
                    continue;
                }

                download(context, downloader);
            }
        }
    }

    private static void download(JReleaserContext context, Downloader<?> downloader) {
        try {
            context.getLogger().increaseIndent();
            context.getLogger().setPrefix(downloader.getType());
            fireDownloadEvent(ExecutionEvent.before(JReleaserCommand.DOWNLOAD.toStep()), context, downloader);

            ProjectDownloader projectDownloader = createProjectDownloader(context, downloader);
            projectDownloader.download();

            fireDownloadEvent(ExecutionEvent.success(JReleaserCommand.DOWNLOAD.toStep()), context, downloader);
        } catch (DownloadException e) {
            fireDownloadEvent(ExecutionEvent.failure(JReleaserCommand.DOWNLOAD.toStep(), e), context, downloader);
            throw new JReleaserException(RB.$("ERROR_unexpected_error"), e);
        } finally {
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
        }
    }

    private static ProjectDownloader createProjectDownloader(JReleaserContext context,
                                                             Downloader<?> downloader) {
        return ProjectDownloader.builder()
            .context(context)
            .downloader(downloader)
            .build();
    }

    private static void fireDownloadEvent(ExecutionEvent event, JReleaserContext context, Downloader<?> downloader) {
        if (!downloader.isEnabled()) return;

        try {
            context.fireDownloadStepEvent(event, downloader.asImmutable());
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
