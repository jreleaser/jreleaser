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
package org.jreleaser.model.internal.upload;

import org.jreleaser.model.Http;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public interface WebUploader<A extends org.jreleaser.model.api.upload.WebUploader> extends Uploader<A>, Http {
    String getUploadUrl();

    void setUploadUrl(String uploadUrl);

    String getDownloadUrl();

    void setDownloadUrl(String downloadUrl);

    String getResolvedUploadUrl(JReleaserContext context, Artifact artifact);
}
