/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.deploy.DeployException;
import org.jreleaser.sdk.commons.AbstractMavenDeployer;
import org.jreleaser.model.spi.deploy.maven.Deployable;
import org.jreleaser.sdk.nexus2.api.NexusAPIException;

import java.util.Set;

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
        Set<Deployable> deployables = collectDeployables();

        if (deployables.isEmpty()) {
            context.getLogger().info(RB.$("artifacts.no.match"));
        }

        boolean isSnapshot = context.getModel().getProject().isSnapshot();
        String baseUrl = deployer.getResolvedUrl(context.fullProps());
        if (isSnapshot) {
            baseUrl = deployer.getResolvedSnapshotUrl(context.fullProps());
        }
        String username = deployer.getUsername();
        String password = deployer.getPassword();

        Nexus2 nexus = new Nexus2(context.getLogger(), baseUrl, username, password,
            deployer.getConnectTimeout(), deployer.getReadTimeout(), context.isDryrun(),
            deployer.getTransitionDelay(), deployer.getTransitionMaxRetries());

        String groupId = context.getModel().getProject().getJava().getGroupId();

        String stagingProfileId = null;
        String stagingRepositoryId = null;

        if (!isSnapshot) {
            try {
                context.getLogger().info(RB.$("nexus.lookup.staging.profile", groupId));
                stagingProfileId = nexus.findStagingProfileId(groupId);
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

            if (!context.isDryrun()) {
                try {
                    context.getLogger().info(RB.$("nexus.create.staging.repository", groupId));
                    stagingRepositoryId = nexus.createStagingRepository(stagingProfileId, groupId);
                } catch (Nexus2Exception e) {
                    context.getLogger().trace(e);
                    throw new DeployException(RB.$("ERROR_nexus_create_staging_repository", groupId), e);
                }
            }
        }

        for (Deployable deployable : deployables) {
            context.getLogger().info(" - {}", deployable.getFullDeployPath());

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
}
