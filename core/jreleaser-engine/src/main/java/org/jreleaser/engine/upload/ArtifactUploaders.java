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
package org.jreleaser.engine.upload;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.Uploader;
import org.jreleaser.model.uploader.spi.ArtifactUploader;
import org.jreleaser.model.uploader.spi.ArtifactUploaderFactory;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class ArtifactUploaders {
    public static <U extends Uploader> ArtifactUploader<U> findUploader(JReleaserContext context, U uploader) {
        Map<String, ArtifactUploader> uploaders = StreamSupport.stream(ServiceLoader.load(ArtifactUploaderFactory.class,
            ArtifactUploaders.class.getClassLoader()).spliterator(), false)
            .collect(Collectors.toMap(ArtifactUploaderFactory::getName, factory -> factory.getArtifactUploader(context)));

        if (uploaders.containsKey(uploader.getType())) {
            ArtifactUploader<U> artifactUploader = uploaders.get(uploader.getType());
            artifactUploader.setUploader(uploader);
            return artifactUploader;
        }

        throw new JReleaserException("Unsupported uploader " + uploader.getType());
    }
}
