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
package org.jreleaser.sdk.gitlab;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.deploy.DeployException;
import org.jreleaser.model.spi.deploy.maven.Deployable;
import org.jreleaser.sdk.commons.AbstractMavenDeployer;
import org.jreleaser.sdk.gitlab.api.GlPackage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public class GitlabMavenDeployer extends AbstractMavenDeployer<org.jreleaser.model.api.deploy.maven.GitlabMavenDeployer,
    org.jreleaser.model.internal.deploy.maven.GitlabMavenDeployer> {
    private org.jreleaser.model.internal.deploy.maven.GitlabMavenDeployer deployer;

    public GitlabMavenDeployer(JReleaserContext context) {
        super(context);
    }

    @Override
    public org.jreleaser.model.internal.deploy.maven.GitlabMavenDeployer getDeployer() {
        return deployer;
    }

    @Override
    public void setDeployer(org.jreleaser.model.internal.deploy.maven.GitlabMavenDeployer deployer) {
        this.deployer = deployer;
    }

    @Override
    public String getType() {
        return org.jreleaser.model.api.deploy.maven.GitlabMavenDeployer.TYPE;
    }

    @Override
    public void deploy(String name) throws DeployException {
        deployPackages();
    }

    @Override
    protected void deleteExistingPackages(String baseUrl, String token, Set<Deployable> deployables) throws DeployException {
        Gitlab api = createApi(baseUrl, token);
        List<GlPackage> glPackages = new ArrayList<>();

        if (!context.isDryrun()) {
            try {
                glPackages.addAll(api.listPackages(Integer.parseInt(deployer.getProjectIdentifier()), "maven"));
            } catch (IOException e) {
                context.getLogger().trace(e);
                throw new DeployException(RB.$("ERROR_unexpected_error"), e);
            }

            // delete existing packages (if any)
            for (Deployable deployable : deployables) {
                if (deployable.getFilename().endsWith(".pom")) {
                    deletePackage(api, deployable, glPackages);
                }
            }
        }
    }

    private Gitlab createApi(String baseUrl, String token) throws DeployException {
        URL url = null;

        try {
            url = new URI(baseUrl).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            context.getLogger().trace(e);
            throw new DeployException(RB.$("ERROR_unexpected_error"), e);
        }

        StringBuilder theUrl = new StringBuilder(url.getProtocol())
            .append("://")
            .append(url.getHost());
        if (url.getPort() != -1) {
            theUrl.append(url.getPort());
        }

        try {
            return new Gitlab(context.asImmutable(),
                theUrl.toString(),
                token,
                deployer.getConnectTimeout(),
                deployer.getReadTimeout());
        } catch (Exception e) {
            context.getLogger().trace(e);
            throw new DeployException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private void deletePackage(Gitlab api, Deployable deployable, List<GlPackage> glPackages) {
        try {
            String name = deployable.getGroupId().replace(".", "/") + "/" + deployable.getArtifactId();
            Optional<GlPackage> glPackage = glPackages.stream()
                .filter(p -> p.getName().equals(name) && p.getVersion().equals(deployable.getVersion()))
                .findFirst();

            glPackage.ifPresent(p -> api.deletePackage(Integer.parseInt(deployer.getProjectIdentifier()), p.getId()));
        } catch (Exception e) {
            context.getLogger().debug(RB.$("ERROR_gitlab_delete_package", deployer.getUsername(),
                "maven",
                deployable.getGroupId() + "-" + deployable.getArtifactId(),
                deployable.getVersion()));
        }
    }
}
