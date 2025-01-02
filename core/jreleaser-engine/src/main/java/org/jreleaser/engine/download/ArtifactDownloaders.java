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
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.download.Downloader;
import org.jreleaser.model.spi.download.ArtifactDownloader;
import org.jreleaser.model.spi.download.ArtifactDownloaderFactory;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class ArtifactDownloaders {
    private ArtifactDownloaders() {
        // noop
    }

    public static <A extends org.jreleaser.model.api.download.Downloader, D extends Downloader<A>> ArtifactDownloader<A, D> findDownloader(JReleaserContext context, D downloader) {
        Map<String, ArtifactDownloader<?, ?>> downloaders = StreamSupport.stream(ServiceLoader.load(ArtifactDownloaderFactory.class,
                ArtifactDownloaders.class.getClassLoader()).spliterator(), false)
            .collect(Collectors.toMap(ArtifactDownloaderFactory::getName, factory -> factory.getArtifactDownloader(context)));

        if (downloaders.containsKey(downloader.getType())) {
            ArtifactDownloader artifactDownloader = downloaders.get(downloader.getType());
            artifactDownloader.setDownloader(downloader);
            return artifactDownloader;
        }

        throw new JReleaserException(RB.$("ERROR_unsupported_downloader", downloader.getType()));
    }
}
