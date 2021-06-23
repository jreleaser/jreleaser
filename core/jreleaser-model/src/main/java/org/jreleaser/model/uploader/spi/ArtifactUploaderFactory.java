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
package org.jreleaser.model.uploader.spi;

import org.jreleaser.model.Uploader;
import org.jreleaser.model.JReleaserContext;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public interface ArtifactUploaderFactory<D extends Uploader, AD extends ArtifactUploader<D>> {
    String getName();

    AD getArtifactUploader(JReleaserContext context);
}
