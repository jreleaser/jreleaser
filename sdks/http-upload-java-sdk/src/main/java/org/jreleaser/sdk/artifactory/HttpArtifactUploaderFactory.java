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

import org.jreleaser.model.Http;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.uploader.spi.ArtifactUploaderFactory;
import org.kordamp.jipsy.annotations.ServiceProviderFor;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
@ServiceProviderFor(ArtifactUploaderFactory.class)
public class HttpArtifactUploaderFactory implements ArtifactUploaderFactory<Http, HttpArtifactUploader> {
    @Override
    public String getName() {
        return Http.TYPE;
    }

    @Override
    public HttpArtifactUploader getArtifactUploader(JReleaserContext context) {
        return new HttpArtifactUploader(context);
    }
}