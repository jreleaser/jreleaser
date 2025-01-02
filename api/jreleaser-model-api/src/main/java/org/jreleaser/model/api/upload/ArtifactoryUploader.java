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
package org.jreleaser.model.api.upload;

import org.jreleaser.model.Http;
import org.jreleaser.model.api.common.Activatable;
import org.jreleaser.model.api.common.Domain;
import org.jreleaser.model.api.common.ExtraProperties;
import org.jreleaser.util.FileType;

import java.util.List;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public interface ArtifactoryUploader extends Uploader, ExtraProperties {
    String TYPE = "artifactory";

    String getHost();

    String getUsername();

    String getPassword();

    Http.Authorization getAuthorization();

    List<? extends ArtifactoryRepository> getRepositories();

    interface ArtifactoryRepository extends Domain, Activatable {
        String getPath();

        Set<FileType> getFileTypes();
    }
}
