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
package org.jreleaser.sdk.gitea;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.deploy.DeployException;
import org.jreleaser.model.spi.deploy.maven.Deployable;
import org.jreleaser.sdk.commons.AbstractMavenDeployer;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public class GiteaMavenDeployer extends AbstractMavenDeployer<org.jreleaser.model.api.deploy.maven.GiteaMavenDeployer,
    org.jreleaser.model.internal.deploy.maven.GiteaMavenDeployer> {
    private org.jreleaser.model.internal.deploy.maven.GiteaMavenDeployer deployer;

    public GiteaMavenDeployer(JReleaserContext context) {
        super(context);
    }

    @Override
    public org.jreleaser.model.internal.deploy.maven.GiteaMavenDeployer getDeployer() {
        return deployer;
    }

    @Override
    public void setDeployer(org.jreleaser.model.internal.deploy.maven.GiteaMavenDeployer deployer) {
        this.deployer = deployer;
    }

    @Override
    public String getType() {
        return org.jreleaser.model.api.deploy.maven.GiteaMavenDeployer.TYPE;
    }

    @Override
    public void deploy(String name) throws DeployException {
        deployPackages();
    }

    @Override
    protected void deleteExistingPackages(String baseUrl, String token, Set<Deployable> deployables) throws DeployException {
        for (Deployable deployable : deployables) {
            if (deployable.getFilename().endsWith(".pom")) {
                deletePackage(baseUrl, token, deployable);
            }
        }
    }

    private void deletePackage(String baseUrl, String token, Deployable deployable) throws DeployException {
        URL url = null;

        try {
            url = new URI(baseUrl).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            context.getLogger().trace(e);
            throw new DeployException(RB.$("ERROR_unexpected_deploy", deployable.getGav(), e.getMessage()), e);
        }

        StringBuilder theUrl = new StringBuilder(url.getProtocol())
            .append("://")
            .append(url.getHost());
        if (url.getPort() != -1) {
            theUrl.append(url.getPort());
        }

        try {
            Gitea api = new Gitea(context.asImmutable(),
                theUrl.toString(),
                token,
                deployer.getConnectTimeout(),
                deployer.getReadTimeout());

            api.deletePackage(deployer.getUsername(),
                "maven",
                deployable.getGroupId() + "-" + deployable.getArtifactId(),
                deployable.getVersion());
        } catch (Exception e) {
            context.getLogger().debug(RB.$("ERROR_gitea_delete_package", deployer.getUsername(),
                "maven",
                deployable.getGroupId() + "-" + deployable.getArtifactId(),
                deployable.getVersion()));
        }
    }
}
