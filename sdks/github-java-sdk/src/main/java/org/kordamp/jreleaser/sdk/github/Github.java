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
package org.kordamp.jreleaser.sdk.github;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHReleaseBuilder;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.okhttp3.OkHttpConnector;
import org.kordamp.jreleaser.model.releaser.ReleaseException;
import org.kordamp.jreleaser.util.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

        try {
            Path githubCache = Paths.get(System.getProperty("user.home"))
                .resolve(".jreleaser")
                .resolve("caches")
                .resolve("github");

            github = new GitHubBuilder()
                .withEndpoint(endpoint)
                .withOAuthToken(authorization)
                .withConnector(new OkHttpConnector(new OkHttpClient.Builder()
                    .cache(new Cache(Files.createDirectory(githubCache).toFile(), 10 * 1024 * 1024))
                    .build()))
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
