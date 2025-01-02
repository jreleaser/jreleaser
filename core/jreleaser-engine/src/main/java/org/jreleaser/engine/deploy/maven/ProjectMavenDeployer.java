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
package org.jreleaser.engine.deploy.maven;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.deploy.DeployException;
import org.jreleaser.model.spi.deploy.maven.MavenDeployer;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public class ProjectMavenDeployer {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.deploy.maven.MavenDeployer<?> deployer;

    private ProjectMavenDeployer(JReleaserContext context,
                                 org.jreleaser.model.internal.deploy.maven.MavenDeployer<?> deployer) {
        this.context = context;
        this.deployer = deployer;
    }

    public org.jreleaser.model.internal.deploy.maven.MavenDeployer<?> getMavenDeployer() {
        return deployer;
    }

    public boolean deploy() throws DeployException {
        if (!deployer.isEnabled()) {
            context.getLogger().debug(RB.$("deployers.skip.deploy"), deployer.getName());
            return false;
        }

        MavenDeployer<?, ?> artifactMavenDeployer = ArtifactDeployers.findMavenDeployer(context, deployer);

        context.getLogger().info(RB.$("deployers.deploy.to"), deployer.getName());

        artifactMavenDeployer.deploy(deployer.getName());
        return true;
    }

    public static ProjectMavenDeployerBuilder builder() {
        return new ProjectMavenDeployerBuilder();
    }

    public static class ProjectMavenDeployerBuilder {
        private JReleaserContext context;
        private org.jreleaser.model.internal.deploy.maven.MavenDeployer<?> deployer;

        public ProjectMavenDeployerBuilder context(JReleaserContext context) {
            this.context = requireNonNull(context, "'context' must not be null");
            return this;
        }

        public ProjectMavenDeployerBuilder deployer(org.jreleaser.model.internal.deploy.maven.MavenDeployer<?> deployer) {
            this.deployer = requireNonNull(deployer, "'deployer' must not be null");
            return this;
        }

        public ProjectMavenDeployer build() {
            requireNonNull(context, "'context' must not be null");
            requireNonNull(deployer, "'deployer' must not be null");
            return new ProjectMavenDeployer(context, deployer);
        }
    }
}
