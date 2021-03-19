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
import org.jreleaser.util.Logger;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHReleaseBuilder;
import org.kohsuke.github.GHRepository;
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

    Github(Logger logger, String username, String password) throws IOException {
        this(logger, ENDPOINT, username, password);
    }

    Github(Logger logger, String endpoint, String username, String password) throws IOException {
        this.logger = logger;

        if (isBlank(endpoint)) {
            endpoint = ENDPOINT;
        }

        github = new GitHubBuilder()
            .withEndpoint(endpoint)
            .withOAuthToken(password, username)
            .build();
    }

    GHRepository findRepository(String owner, String repo) throws IOException {
        logger.debug("Lookup repository {}/{}", owner, repo);
        try {
            return github.getRepository(owner + "/" + repo);
        } catch (GHFileNotFoundException e) {
            // OK, this means the repository does not exist
            return null;
        }
    }

    GHRepository createRepository(String owner, String repo) throws IOException {
        logger.debug("Creating repository {}/{}", owner, repo);

        GHOrganization organization = resolveOrganization(owner);
        if (null != organization) {
            return organization.createRepository(repo)
                .create();
        }

        return github.createRepository(repo)
            .create();
    }

    GHRelease findReleaseByTag(String repo, String tagName) throws IOException {
        logger.debug("Fetching release on {} with tag {}", repo, tagName);
        return github.getRepository(repo)
            .getReleaseByTagName(tagName);
    }

    GHReleaseBuilder createRelease(String owner, String repo, String tagName) throws IOException {
        logger.debug("Creating release on {}/{} with tag {}", owner, repo, tagName);
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

    private GHOrganization resolveOrganization(String name) throws IOException {
        try {
            return github.getOrganization(name);
        } catch (GHFileNotFoundException ignored) {
            // OK, means the organization does not exist
            return null;
        }
    }
}
