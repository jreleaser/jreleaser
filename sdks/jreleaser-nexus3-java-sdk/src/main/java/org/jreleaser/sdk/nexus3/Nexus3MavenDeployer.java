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
package org.jreleaser.sdk.nexus3;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.deploy.DeployException;
import org.jreleaser.model.spi.deploy.maven.Deployable;
import org.jreleaser.sdk.commons.AbstractMavenDeployer;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 1.18.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class Nexus3MavenDeployer extends AbstractMavenDeployer<org.jreleaser.model.api.deploy.maven.Nexus3MavenDeployer,
    org.jreleaser.model.internal.deploy.maven.Nexus3MavenDeployer> {

    private org.jreleaser.model.internal.deploy.maven.Nexus3MavenDeployer deployer;

    public Nexus3MavenDeployer(JReleaserContext context) {
        super(context);
    }

    @Override
    public org.jreleaser.model.internal.deploy.maven.Nexus3MavenDeployer getDeployer() {
        return deployer;
    }

    @Override
    public void setDeployer(org.jreleaser.model.internal.deploy.maven.Nexus3MavenDeployer deployer) {
        this.deployer = deployer;
    }

    @Override
    public String getType() {
        return org.jreleaser.model.api.deploy.maven.Nexus3MavenDeployer.TYPE;
    }

    @Override
    public void deploy(String name) throws DeployException {
        Set<Deployable> deployables = collectDeployables(false);

        String baseUrl = deployer.getResolvedUrl(context, context.fullProps());
        String username = deployer.getUsername();
        String password = deployer.getPassword();

        Nexus3 nexus = new Nexus3(context.asImmutable(), baseUrl, username, password,
            deployer.getConnectTimeout(), deployer.getReadTimeout(), context.isDryrun());

        Map<String, Map<String, Set<Deployable>>> groupedArtifacts = new LinkedHashMap<>();
        for (Deployable deployable : deployables) {
            groupedArtifacts.computeIfAbsent(deployable.getGroupId(), k1 -> new LinkedHashMap<>())
                .computeIfAbsent(deployable.getArtifactId(), k -> new LinkedHashSet<>())
                .add(deployable);
        }

        for (Map.Entry<String, Map<String, Set<Deployable>>> group : groupedArtifacts.entrySet()) {
            String groupId = group.getKey();
            for (Map.Entry<String, Set<Deployable>> artifacts : group.getValue().entrySet()) {
                for (Deployable deployable : artifacts.getValue()) {
                    context.getLogger().info(" - {}", deployable.getFullDeployPath());
                }

                if (!context.isDryrun()) {
                    try {
                        nexus.deploy(groupId, artifacts.getKey(), artifacts.getValue());
                    } catch (Nexus3Exception e) {
                        context.getLogger().trace(e);
                        throw new DeployException(RB.$("ERROR_unexpected_deploy",
                            groupId + ":" + artifacts.getKey(), e.getMessage()), e);
                    }
                }
            }
        }
    }
}
