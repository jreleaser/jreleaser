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
package org.jreleaser.sdk.github;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.deploy.DeployException;
import org.jreleaser.model.spi.deploy.maven.Deployable;
import org.jreleaser.sdk.commons.AbstractMavenDeployer;
import org.jreleaser.sdk.github.api.GhPackageVersion;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public class GithubMavenDeployer extends AbstractMavenDeployer<org.jreleaser.model.api.deploy.maven.GithubMavenDeployer,
    org.jreleaser.model.internal.deploy.maven.GithubMavenDeployer> {
    private org.jreleaser.model.internal.deploy.maven.GithubMavenDeployer deployer;

    public GithubMavenDeployer(JReleaserContext context) {
        super(context);
    }

    @Override
    public org.jreleaser.model.internal.deploy.maven.GithubMavenDeployer getDeployer() {
        return deployer;
    }

    @Override
    public void setDeployer(org.jreleaser.model.internal.deploy.maven.GithubMavenDeployer deployer) {
        this.deployer = deployer;
    }

    @Override
    public String getType() {
        return org.jreleaser.model.api.deploy.maven.GithubMavenDeployer.TYPE;
    }

    @Override
    public void deploy(String name) throws DeployException {
        deployPackages();
    }

    @Override
    protected void deleteExistingPackages(String baseUrl, String token, Set<Deployable> deployables) throws DeployException {
        for (Deployable deployable : deployables) {
            if (deployable.getFilename().endsWith(".pom")) {
                deletePackage(token, deployable);
            }
        }
    }

    private void deletePackage(String token, Deployable deployable) {
        String packageType = "maven";
        String packageName = deployable.getGroupId() + "." + deployable.getArtifactId();
        String packageVersion = deployable.getVersion();

        try {
            Github api = new Github(context.asImmutable(),
                context.getModel().getRelease().getGithub().getApiEndpoint(),
                token,
                deployer.getConnectTimeout(),
                deployer.getReadTimeout());

            List<GhPackageVersion> ghPackageVersions = api.listPackageVersions(packageType, packageName);
            if (ghPackageVersions.isEmpty()) return;

            if (ghPackageVersions.size() == 1) {
                api.deletePackage(packageType, packageName);
            } else {
                Optional<GhPackageVersion> ghPackageVersion = ghPackageVersions.stream()
                    .filter(pv -> pv.getName().equals(packageVersion))
                    .findFirst();

                if (ghPackageVersion.isPresent()) {
                    api.deletePackageVersion(packageType, packageType, packageVersion);
                }
            }
        } catch (Exception e) {
            context.getLogger().debug(RB.$("ERROR_github_delete_package", deployer.getUsername(),
                packageType,
                packageName,
                packageVersion));
        }
    }
}
