/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
package org.jreleaser.sdk.github;

import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.jreleaser.model.releaser.ReleaseException;
import org.jreleaser.util.Logger;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHReleaseBuilder;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
class Github {
    static final String ENDPOINT = "https://api.github.com";
    private final Tika tika = new Tika();

    private final Logger logger;
    private final GitHub github;

    Github(Logger logger, String authorization) throws ReleaseException {
        this(logger, ENDPOINT, authorization);
    }

    Github(Logger logger, String endpoint, String authorization) throws ReleaseException {
        this.logger = logger;

        if (isBlank(endpoint)) {
            endpoint = ENDPOINT;
        }
        
        try {
            github = new GitHubBuilder()
                .withEndpoint(endpoint)
                .withOAuthToken(authorization)
                .build();
        } catch (IOException e) {
            throw new ReleaseException("Unexpected error setting up GitHub endpoint", e);
        }
    }

    GHRelease findReleaseByTag(String repo, String tagName) throws IOException {
        logger.debug("Fetching release on {} with tag {}", repo, tagName);
        return github.getRepository(repo).getReleaseByTagName(tagName);
    }

    GHReleaseBuilder createRelease(String repo, String tagName) throws IOException {
        logger.debug("Creating release on {} with tag {}", repo, tagName);
        return github.getRepository(repo)
            .createRelease(tagName);
    }

    void uploadAssets(GHRelease release, List<Path> assets) throws IOException {
        for (Path asset : assets) {
            if (0 == asset.toFile().length()) {
                // do not upload empty files
                continue;
            }

            logger.debug("Uploading asset {}", asset.getFileName().toString());
            GHAsset ghasset = release.uploadAsset(asset.toFile(), MediaType.parse(tika.detect(asset)).toString());
            if (!"uploaded".equalsIgnoreCase(ghasset.getState())) {
                logger.warn("Failed to upload " + asset.getFileName());
            }
        }
    }
}
