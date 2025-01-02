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
package org.jreleaser.sdk.nexus2;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.Stage;
import org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.StageOperation;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.deploy.DeployException;
import org.jreleaser.model.spi.deploy.maven.Deployable;
import org.jreleaser.sdk.commons.AbstractMavenDeployer;
import org.jreleaser.sdk.nexus2.api.NexusAPIException;
import org.jreleaser.sdk.nexus2.api.StagingProfile;
import org.jreleaser.sdk.nexus2.api.StagingProfileRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.jreleaser.util.StringUtils.capitalize;
import static org.jreleaser.util.StringUtils.getClassNameForLowerCaseHyphenSeparatedName;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public class Nexus2MavenDeployer extends AbstractMavenDeployer<org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer,
    org.jreleaser.model.internal.deploy.maven.Nexus2MavenDeployer> {

    private org.jreleaser.model.internal.deploy.maven.Nexus2MavenDeployer deployer;

    public Nexus2MavenDeployer(JReleaserContext context) {
        super(context);
    }

    @Override
    public org.jreleaser.model.internal.deploy.maven.Nexus2MavenDeployer getDeployer() {
        return deployer;
    }

    @Override
    public void setDeployer(org.jreleaser.model.internal.deploy.maven.Nexus2MavenDeployer deployer) {
        this.deployer = deployer;
    }

    @Override
    public String getType() {
        return org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.TYPE;
    }

    @Override
    public void deploy(String name) throws DeployException {
        Stage startStage = deployer.getStartStage();
        Stage endStage = deployer.getEndStage();
        StageOperation op = StageOperation.of(startStage, endStage);
        String stagingProfileId = deployer.getStagingProfileId();
        String stagingRepositoryId = deployer.getStagingRepositoryId();

        if (null != startStage) {
            context.getLogger().debug(RB.$("set.to", "start.stage", startStage));
        }
        if (null != endStage) {
            context.getLogger().debug(RB.$("set.to", "end.stage", endStage));
        }
        if (null != op) {
            context.getLogger().info(RB.$("set.to", "nexus2", op));
        }
        if (isNotBlank(stagingProfileId)) {
            context.getLogger().debug(RB.$("set.to", "stagingProfileId", stagingProfileId));
        }
        if (isNotBlank(stagingRepositoryId)) {
            context.getLogger().debug(RB.$("set.to", "stagingRepositoryId", stagingRepositoryId));
        }

        Set<Deployable> deployables = Collections.emptySet();

        if (op == StageOperation.FULL_DEPLOYMENT || op == StageOperation.UPLOAD || op == StageOperation.UPLOAD_AND_CLOSE) {
            deployables = collectDeployables();
            if (deployables.isEmpty()) {
                context.getLogger().info(RB.$("artifacts.no.match"));
                return;
            }
        }

        boolean isSnapshot = context.getModel().getProject().isSnapshot();
        String baseUrl = deployer.getResolvedUrl(context.fullProps());
        if (isSnapshot) {
            baseUrl = deployer.getResolvedSnapshotUrl(context.fullProps());
        }
        String username = deployer.getUsername();
        String password = deployer.getPassword();
        String groupId = context.getModel().getProject().getLanguages().getJava().getGroupId();

        Nexus2 nexus = new Nexus2(context.asImmutable(), baseUrl, username, password,
            deployer.getConnectTimeout(), deployer.getReadTimeout(), context.isDryrun(),
            deployer.getTransitionDelay(), deployer.getTransitionMaxRetries());

        if (!isSnapshot) {
            if (op == StageOperation.FULL_DEPLOYMENT || op == StageOperation.UPLOAD || op == StageOperation.UPLOAD_AND_CLOSE) {
                if (isBlank(stagingProfileId)) {
                    stagingProfileId = findStagingProfileId(nexus, groupId);
                }

                if (isBlank(stagingRepositoryId) && !context.isDryrun()) {
                    stagingRepositoryId = createStagingRepository(nexus, groupId, stagingProfileId);
                }
            } else {
                if (isBlank(stagingProfileId)) {
                    stagingProfileId = findStagingProfileId(nexus, groupId);
                }
                if (isBlank(stagingProfileId) && !context.isDryrun()) {
                    throw new DeployException(RB.$("ERROR_nexus_find_staging_profile", groupId));
                }

                if (isBlank(stagingRepositoryId)) {
                    List<StagingProfileRepository> repositories = findStagingRepositories(nexus, groupId, stagingProfileId);

                    if (op == StageOperation.RELEASE) {
                        stagingRepositoryId = repositories.stream()
                            .filter(r -> r.getState() == StagingProfileRepository.State.CLOSED)
                            .map(StagingProfileRepository::getRepositoryId)
                            .findFirst()
                            .orElse(null);
                    }

                    if (isBlank(stagingRepositoryId)) {
                        stagingRepositoryId = repositories.stream()
                            .filter(r -> r.getState() == StagingProfileRepository.State.OPEN)
                            .map(StagingProfileRepository::getRepositoryId)
                            .findFirst()
                            .orElse(null);
                    }
                }

                if (isBlank(stagingRepositoryId) && !context.isDryrun()) {
                    throw new DeployException(RB.$("ERROR_nexus_find_staging_repository", groupId));
                }
            }
        }

        if (op == StageOperation.FULL_DEPLOYMENT || op == StageOperation.UPLOAD || op == StageOperation.UPLOAD_AND_CLOSE) {
            uploadArtifacts(nexus, deployables, stagingRepositoryId);
            if (Stage.UPLOAD == endStage) return;
        }

        if (op == StageOperation.FULL_DEPLOYMENT || op == StageOperation.CLOSE || op == StageOperation.UPLOAD_AND_CLOSE ||
            op == StageOperation.CLOSE_AND_RELEASE) {
            closeRepository(nexus, isSnapshot, groupId, stagingProfileId, stagingRepositoryId);
            if (Stage.CLOSE == endStage) return;
        }

        if (op == StageOperation.RELEASE) {
            try {
                // attempt to close the repository
                closeRepository(nexus, isSnapshot, groupId, stagingProfileId, stagingRepositoryId);
            } catch (DeployException e) {
                // ignored, repository is already closed
            }
        }

        releaseRepository(nexus, isSnapshot, groupId, stagingProfileId, stagingRepositoryId);
    }

    private String findStagingProfileId(Nexus2 nexus, String groupId) throws DeployException {
        try {
            context.getLogger().info(RB.$("nexus.lookup.staging.profile", groupId));
            Optional<StagingProfile> stagingProfile = nexus.findStagingProfiles(groupId).stream().findFirst();
            if (stagingProfile.isPresent()) {
                String stagingProfileId = stagingProfile.get().getId();
                context.getAdditionalProperties().put(prefix("stagingProfileId"), stagingProfileId);
                return stagingProfileId;
            } else if (context.isDryrun()) {
                context.getLogger().warn(RB.$("ERROR_nexus_find_staging_profile", groupId));
            } else {
                throw new DeployException(RB.$("ERROR_nexus_find_staging_profile", groupId));
            }
        } catch (Nexus2Exception e) {
            if (e.getCause() instanceof NexusAPIException) {
                NexusAPIException ne = (NexusAPIException) e.getCause();
                if (context.isDryrun()) {
                    if (ne.isUnauthorized() || ne.isForbidden()) {
                        context.getLogger().warn(RB.$("ERROR_nexus_forbidden"));
                    } else {
                        context.getLogger().warn(RB.$("ERROR_nexus_find_staging_profile", groupId), e);
                    }
                } else if (ne.isUnauthorized() || ne.isForbidden()) {
                    throw new DeployException(RB.$("ERROR_nexus_forbidden"), ne);
                }
                if (!context.isDryrun()) {
                    throw new DeployException(RB.$("ERROR_nexus_find_staging_profile", groupId), e);
                }
            } else if (context.isDryrun()) {
                context.getLogger().warn(RB.$("ERROR_nexus_find_staging_profile", groupId));
            } else {
                throw new DeployException(RB.$("ERROR_nexus_find_staging_profile", groupId), e);
            }
        }

        return null;
    }

    private String createStagingRepository(Nexus2 nexus, String groupId, String stagingProfileId) throws DeployException {
        String stagingRepositoryId;
        try {
            context.getLogger().info(RB.$("nexus.create.staging.repository", groupId));
            stagingRepositoryId = nexus.createStagingRepository(stagingProfileId, groupId);
            context.getAdditionalProperties().put(prefix("stagingRepositoryId"), stagingRepositoryId);
        } catch (Nexus2Exception e) {
            context.getLogger().trace(e);
            throw new DeployException(RB.$("ERROR_nexus_create_staging_repository", groupId), e);
        }
        return stagingRepositoryId;
    }

    private List<StagingProfileRepository> findStagingRepositories(Nexus2 nexus, String groupId, String stagingProfileId) throws DeployException {
        try {
            context.getLogger().info(RB.$("nexus.lookup.staging.repositories", groupId));
            return nexus.findStagingProfileRepositories(stagingProfileId, groupId);
        } catch (Nexus2Exception e) {
            context.getLogger().trace(e);
            throw new DeployException(RB.$("ERROR_nexus_find_staging_repositories", groupId), e);
        }
    }

    private void uploadArtifacts(Nexus2 nexus, Set<Deployable> deployables, String stagingRepositoryId) throws DeployException {
        boolean success = true;
        for (Deployable deployable : deployables) {
            context.getLogger().info(" - {}", deployable.getFullDeployPath());

            if (nexus.artifactExists(deployable, context.getModel().getProject().isSnapshot() ? null : getDeployer().getVerifyUrl())) {
                success = false;
            }

            if (!context.isDryrun()) {
                try {
                    // if project is snapshot then stagingRepositoryId will be null, and this is expected
                    nexus.deploy(stagingRepositoryId, deployable.getDeployPath(), deployable.getLocalPath());
                } catch (Nexus2Exception e) {
                    context.getLogger().trace(e);
                    throw new DeployException(RB.$("ERROR_unexpected_deploy",
                        context.getBasedir().relativize(deployable.getLocalPath()), e.getMessage()), e);
                }
            }
        }

        if (!success) {
            throw new DeployException(RB.$("ERROR_nexus_deploy_artifacts"));
        }
    }

    private void closeRepository(Nexus2 nexus, boolean isSnapshot, String groupId, String stagingProfileId, String stagingRepositoryId) throws DeployException {
        if (!isSnapshot && !context.isDryrun() && deployer.isCloseRepository()) {
            try {
                context.getLogger().info(RB.$("nexus.close.repository", stagingRepositoryId));
                context.getLogger().info(RB.$("nexus.wait.operation"));
                nexus.closeStagingRepository(stagingProfileId, stagingRepositoryId, groupId);
            } catch (Nexus2Exception e) {
                context.getLogger().trace(e);
                throw new DeployException(RB.$("ERROR_nexus_close_repository", stagingRepositoryId), e);
            }
        }
    }

    private void releaseRepository(Nexus2 nexus, boolean isSnapshot, String groupId, String stagingProfileId, String stagingRepositoryId) throws DeployException {
        if (!isSnapshot && !context.isDryrun() && deployer.isReleaseRepository()) {
            try {
                context.getLogger().info(RB.$("nexus.release.repository", stagingRepositoryId));
                context.getLogger().info(RB.$("nexus.wait.operation"));
                nexus.releaseStagingRepository(stagingProfileId, stagingRepositoryId, groupId);
            } catch (Nexus2Exception e) {
                context.getLogger().trace(e);
                throw new DeployException(RB.$("ERROR_nexus_release_repository", stagingRepositoryId), e);
            }
        }
    }

    private String prefix(String input) {
        return "deploy" +
            capitalize(getDeployer().getType()) +
            getClassNameForLowerCaseHyphenSeparatedName(getDeployer().getName()) +
            capitalize(input);
    }
}
