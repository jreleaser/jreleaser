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
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.util.JReleaserLogger;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHDiscussion;
import org.kohsuke.github.GHException;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHMilestone;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHReleaseBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.HttpConnector;
import org.kohsuke.github.PagedIterable;
import org.kohsuke.github.extras.ImpatientHttpConnector;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.jreleaser.sdk.git.GitSdk.REFS_TAGS;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
class Github {
    static final String ENDPOINT = "https://api.github.com";
    private final Tika tika = new Tika();

    private final JReleaserLogger logger;
    private final GitHub github;

    Github(JReleaserLogger logger,
           String username,
           String password,
           int connectTimeout,
           int readTimeout) throws IOException {
        this(logger, ENDPOINT, username, password, connectTimeout, readTimeout);
    }

    Github(JReleaserLogger logger,
           String endpoint,
           String username,
           String password,
           int connectTimeout,
           int readTimeout) throws IOException {
        this.logger = logger;

        if (isBlank(endpoint)) {
            endpoint = ENDPOINT;
        }

        github = new GitHubBuilder()
            .withConnector(new JReleaserHttpConnector(connectTimeout, readTimeout))
            .withEndpoint(endpoint)
            .withOAuthToken(password, username)
            .build();
    }

    GHRepository findRepository(String owner, String repo) throws IOException {
        logger.debug("lookup repository {}/{}", owner, repo);
        try {
            return github.getRepository(owner + "/" + repo);
        } catch (GHFileNotFoundException e) {
            // OK, this means the repository does not exist
            return null;
        }
    }

    GHRepository createRepository(String owner, String repo) throws IOException {
        logger.debug("creating repository {}/{}", owner, repo);

        GHOrganization organization = resolveOrganization(owner);
        if (null != organization) {
            return organization.createRepository(repo)
                .create();
        }

        return github.createRepository(repo)
            .create();
    }

    Optional<GHMilestone> findMilestoneByName(String owner, String repo, String milestoneName) throws IOException {
        logger.debug("lookup milestone '{}' on {}/{}", milestoneName, owner, repo);

        GHRepository repository = findRepository(owner, repo);
        PagedIterable<GHMilestone> milestones = repository.listMilestones(GHIssueState.OPEN);
        return StreamSupport.stream(milestones.spliterator(), false)
            .filter(m -> milestoneName.equals(m.getTitle()))
            .findFirst();
    }

    void closeMilestone(String owner, String repo, GHMilestone milestone) throws IOException {
        logger.debug("closing milestone '{}' on {}/{}", milestone.getTitle(), owner, repo);

        milestone.close();
    }

    GHRelease findReleaseByTag(String repo, String tagName) throws IOException {
        logger.debug("fetching release on {} with tag {}", repo, tagName);
        return github.getRepository(repo)
            .getReleaseByTagName(tagName);
    }

    void deleteTag(String repo, String tagName) throws IOException {
        logger.debug("deleting tag {} from {}", tagName, repo);
        github.getRepository(repo)
            .getRef(REFS_TAGS + tagName)
            .delete();
    }

    GHReleaseBuilder createRelease(String repo, String tagName) throws IOException {
        logger.debug("creating release on {} with tag {}", repo, tagName);
        return github.getRepository(repo)
            .createRelease(tagName);
    }

    void uploadAssets(GHRelease release, List<Path> assets) throws IOException {
        for (Path asset : assets) {
            if (0 == asset.toFile().length() || !Files.exists(asset)) {
                // do not upload empty or non existent files
                continue;
            }

            logger.info(" - uploading {}", asset.getFileName().toString());
            GHAsset ghasset = release.uploadAsset(asset.toFile(), MediaType.parse(tika.detect(asset)).toString());
            if (!"uploaded".equalsIgnoreCase(ghasset.getState())) {
                logger.warn(" x failed to upload {}", asset.getFileName());
            }
        }
    }

    Optional<GHDiscussion> findDiscussion(String organization, String team, String title) throws IOException {
        GHTeam ghTeam = resolveTeam(organization, team);

        try {
            return StreamSupport.stream(ghTeam.listDiscussions().spliterator(), false)
                .filter(d -> title.equals(d.getTitle()))
                .findFirst();
        } catch (GHException ghe) {
            if (ghe.getCause() instanceof GHFileNotFoundException) {
                // OK
                return Optional.empty();
            }
            throw ghe;
        }
    }

    GHDiscussion createDiscussion(String organization, String team, String title, String message) throws IOException {
        GHTeam ghTeam = resolveTeam(organization, team);

        return ghTeam.createDiscussion(title)
            .body(message)
            .done();
    }

    private GHOrganization resolveOrganization(String name) throws IOException {
        try {
            return github.getOrganization(name);
        } catch (GHFileNotFoundException ignored) {
            // OK, means the organization does not exist
            return null;
        }
    }

    private GHTeam resolveTeam(String organization, String team) throws IOException {
        GHOrganization ghOrganization = null;

        try {
            ghOrganization = github.getOrganization(organization);
        } catch (GHFileNotFoundException e) {
            throw new IllegalStateException("Organization '" + organization + "' does not exist");
        }

        GHTeam ghTeam = null;

        try {
            ghTeam = ghOrganization.getTeamByName(team);
        } catch (IOException e) {
            throw new IllegalStateException("Team '" + team + "' does not exist");
        }

        if (null == ghTeam) {
            throw new IllegalStateException("Team '" + team + "' does not exist");
        }

        return ghTeam;
    }

    private static class JReleaserHttpConnector extends ImpatientHttpConnector {
        public JReleaserHttpConnector(int connectTimeout, int readTimeout) {
            super(HttpConnector.DEFAULT, connectTimeout * 1000, readTimeout * 1000);
        }

        @Override
        public HttpURLConnection connect(URL url) throws IOException {
            HttpURLConnection connection = super.connect(url);
            connection.addRequestProperty("User-Agent", "JReleaser/" + JReleaserVersion.getPlainVersion());
            return connection;
        }
    }
}
