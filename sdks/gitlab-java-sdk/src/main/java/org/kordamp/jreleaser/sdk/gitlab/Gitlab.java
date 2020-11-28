/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Andres Almiray.
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
package org.kordamp.jreleaser.sdk.gitlab;

import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.FileUpload;
import org.gitlab4j.api.models.Release;
import org.gitlab4j.api.models.ReleaseParams;
import org.kordamp.jreleaser.model.releaser.ReleaseException;
import org.kordamp.jreleaser.util.Logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Gitlab {
    static final String ENDPOINT = "https://gitlab.com/";
    private final Tika tika = new Tika();

    private final Logger logger;
    private final GitLabApi gitlab;

    Gitlab(Logger logger, String authorization) throws ReleaseException {
        this(logger, ENDPOINT, authorization);
    }

    Gitlab(Logger logger, String endpoint, String authorization) throws ReleaseException {
        this.logger = logger;

        gitlab = new GitLabApi(endpoint, authorization);
    }

    Release findReleaseByTag(String repo, String tagName) throws GitLabApiException, IOException {
        logger.debug("Fetching release on {} with tag {}", repo, tagName);
        return gitlab.getReleasesApi().getRelease(encode(repo), tagName);
    }

    void deleteRelease(String repo, String tagName) throws GitLabApiException, IOException {
        logger.debug("Deleting release on {} with tag {}", repo, tagName);
        gitlab.getReleasesApi().deleteRelease(encode(repo), tagName);
    }

    Release createRelease(String repo, ReleaseParams params) throws GitLabApiException, IOException {
        logger.debug("Creating release on {} with tag {}", repo, params.getTagName());
        return gitlab.getReleasesApi().createRelease(encode(repo), params);
    }

    void uploadAssets(String repo, Release release, List<Path> assets) throws GitLabApiException, IOException {
        for (Path asset : assets) {
            if (0 == asset.toFile().length()) {
                // do not upload empty files
                continue;
            }

            logger.debug("Uploading asset {}", asset.getFileName().toString());
            try {
                FileUpload upload = gitlab.getProjectApi()
                    .uploadFile(encode(repo), asset.toFile(), MediaType.parse(tika.detect(asset)).toString());
                // gitlab.getReleaseLinksApi().link(...)
            } catch (Exception e) {
                logger.warn("Failed to upload " + asset.getFileName(), e);
            }
        }
    }

    private Object encode(String repo) throws IOException {
        return URLEncoder.encode(repo, StandardCharsets.UTF_8.displayName());
    }
}
