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
package org.jreleaser.engine.download;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Download;
import org.jreleaser.model.Downloader;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.downloader.spi.DownloadException;
import org.jreleaser.util.JReleaserException;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public class Downloaders {
    public static void download(JReleaserContext context) {
        Download download = context.getModel().getDownload();
        if (!download.isEnabled()) {
            context.getLogger().info(RB.$("downloaders.not.enabled"));
            return;
        }

        if (!context.getIncludedDownloaderTypes().isEmpty()) {
            for (String downloaderType : context.getIncludedDownloaderTypes()) {
                // check if the downloaderType is valid
                if (!Download.supportedDownloaders().contains(downloaderType)) {
                    context.getLogger().warn(RB.$("ERROR_unsupported_downloader", downloaderType));
                    continue;
                }

                Map<String, Downloader> downloaders = download.findDownloadersByType(downloaderType);

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

                        Downloader downloader = downloaders.get(downloaderName);
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
                List<Downloader> filteredDownloaders = download.findAllActiveDownloaders().stream()
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
            for (Downloader downloader : download.findAllActiveDownloaders()) {
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

    private static void download(JReleaserContext context, Downloader downloader) {
        try {
            context.getLogger().increaseIndent();
            context.getLogger().setPrefix(downloader.getType());
            ProjectDownloader projectDownloader = createProjectDownloader(context, downloader);
            projectDownloader.download();
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
        } catch (DownloadException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private static ProjectDownloader createProjectDownloader(JReleaserContext context,
                                                             Downloader downloader) {
        return ProjectDownloader.builder()
            .context(context)
            .downloader(downloader)
            .build();
    }
}
