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

import org.jreleaser.model.api.common.Activatable;
import org.jreleaser.model.api.common.Domain;

import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public interface Upload extends Domain, Activatable {
    Map<String, ? extends ArtifactoryUploader> getArtifactory();

    Map<String, ? extends FtpUploader> getFtp();

    Map<String, ? extends ForgejoUploader> getForgejo();

    Map<String, ? extends GiteaUploader> getGitea();

    Map<String, ? extends GitlabUploader> getGitlab();

    Map<String, ? extends HttpUploader> getHttp();

    Map<String, ? extends S3Uploader> getS3();

    Map<String, ? extends ScpUploader> getScp();

    Map<String, ? extends SftpUploader> getSftp();
}
