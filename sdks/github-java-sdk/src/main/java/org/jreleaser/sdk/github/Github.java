/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.model.releaser.spi.Asset;
import org.jreleaser.util.JReleaserLogger;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHBranch;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
           String token,
           int connectTimeout,
           int readTimeout) throws IOException {
        this(logger, ENDPOINT, token, connectTimeout, readTimeout);
    }

    Github(JReleaserLogger logger,
           String endpoint,
           String token,
           int connectTimeout,
           int readTimeout) throws IOException {
        this.logger = logger;

        if (isBlank(endpoint)) {
            endpoint = ENDPOINT;
        }

        github = new GitHubBuilder()
            .withConnector(new JReleaserHttpConnector(connectTimeout, readTimeout))
            .withEndpoint(endpoint)
            .withOAuthToken(token)
            .build();
    }

    PagedIterable<GHRelease> listReleases(String owner, String repo) throws IOException {
        logger.debug(RB.$("git.list.releases"), owner, repo);
        return github.getRepository(owner + "/" + repo)
            .listReleases();
    }

    Map<String, GHBranch> listBranches(String owner, String repo) throws IOException {
        logger.debug(RB.$("git.list.branches"), owner, repo);
        return github.getRepository(owner + "/" + repo)
            .getBranches();
    }

    Map<String, GHAsset> listAssets(String owner, String repo, GHRelease release) throws IOException {
        logger.debug(RB.$("git.list.assets.github"), owner, repo, release.getId());

        Map<String, GHAsset> assets = new LinkedHashMap<>();
        for (GHAsset asset : release.listAssets()) {
            assets.put(asset.getName(), asset);
        }

        return assets;
    }

    GHRepository findRepository(String owner, String repo) throws IOException {
        logger.debug(RB.$("git.repository.lookup"), owner, repo);
        try {
            return github.getRepository(owner + "/" + repo);
        } catch (GHFileNotFoundException e) {
            // OK, this means the repository does not exist
            return null;
        }
    }

    GHRepository createRepository(String owner, String repo) throws IOException {
        logger.debug(RB.$("git.repository.create"), owner, repo);

        GHOrganization organization = resolveOrganization(owner);
        if (null != organization) {
            return organization.createRepository(repo)
                .create();
        }

        return github.createRepository(repo)
            .create();
    }

    Optional<GHMilestone> findMilestoneByName(String owner, String repo, String milestoneName) throws IOException {
        logger.debug(RB.$("git.milestone.lookup"), milestoneName, owner, repo);

        GHRepository repository = findRepository(owner, repo);
        PagedIterable<GHMilestone> milestones = repository.listMilestones(GHIssueState.OPEN);
        return StreamSupport.stream(milestones.spliterator(), false)
            .filter(m -> milestoneName.equals(m.getTitle()))
            .findFirst();
    }

    void closeMilestone(String owner, String repo, GHMilestone milestone) throws IOException {
        logger.debug(RB.$("git.milestone.close"), milestone.getTitle(), owner, repo);

        milestone.close();
    }

    GHRelease findReleaseByTag(String repo, String tagName) throws IOException {
        logger.debug(RB.$("git.fetch.release.on.tag"), repo, tagName);
        return github.getRepository(repo)
            .getReleaseByTagName(tagName);
    }

    void deleteTag(String repo, String tagName) throws IOException {
        logger.debug(RB.$("git.delete.tag.from.repository"), tagName, repo);
        github.getRepository(repo)
            .getRef(REFS_TAGS + tagName)
            .delete();
    }

    GHReleaseBuilder createRelease(String repo, String tagName) throws IOException {
        logger.debug(RB.$("git.create.release.repository"), repo, tagName);
        return github.getRepository(repo)
            .createRelease(tagName);
    }

    void uploadAssets(GHRelease release, List<Asset> assets) throws IOException {
        for (Asset asset : assets) {
            if (0 == Files.size(asset.getPath()) || !Files.exists(asset.getPath())) {
                // do not upload empty or non existent files
                continue;
            }

            logger.info(" " + RB.$("git.upload.asset"), asset.getFilename());
            GHAsset ghasset = release.uploadAsset(asset.getPath().toFile(), MediaType.parse(tika.detect(asset.getPath())).toString());
            if (!"uploaded".equalsIgnoreCase(ghasset.getState())) {
                logger.warn(" " + RB.$("git.upload.asset.failure"), asset.getFilename());
            }
        }
    }

    void updateAssets(GHRelease release, List<Asset> assets, Map<String, GHAsset> existingAssets) throws IOException {
        for (Asset asset : assets) {
            if (0 == Files.size(asset.getPath()) || !Files.exists(asset.getPath())) {
                // do not upload empty or non existent files
                continue;
            }

            logger.debug(" " + RB.$("git.delete.asset"), asset.getFilename());
            try {
                existingAssets.get(asset.getFilename()).delete();
            } catch (IOException e) {
                logger.error(" " + RB.$("git.delete.asset.failure"), asset.getFilename());
                throw e;
            }

            logger.info(" " + RB.$("git.update.asset"), asset.getFilename());
            GHAsset ghasset = release.uploadAsset(asset.getPath().toFile(), MediaType.parse(tika.detect(asset.getPath())).toString());
            if (!"uploaded".equalsIgnoreCase(ghasset.getState())) {
                logger.warn(" " + RB.$("git.update.asset.failure"), asset.getFilename());
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
            throw new IllegalStateException(RB.$("ERROR_git_organization_not_exist", organization));
        }

        GHTeam ghTeam = null;

        try {
            ghTeam = ghOrganization.getTeamByName(team);
        } catch (IOException e) {
            throw new IllegalStateException(RB.$("ERROR_git_team_not_exist"));
        }

        if (null == ghTeam) {
            throw new IllegalStateException(RB.$("ERROR_git_team_not_exist"));
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
