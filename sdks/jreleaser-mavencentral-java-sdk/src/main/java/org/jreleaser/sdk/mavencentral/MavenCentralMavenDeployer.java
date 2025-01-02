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
package org.jreleaser.sdk.mavencentral;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer.Stage;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.deploy.DeployException;
import org.jreleaser.model.spi.deploy.maven.Deployable;
import org.jreleaser.sdk.commons.AbstractMavenDeployer;
import org.jreleaser.sdk.mavencentral.api.Deployment;
import org.jreleaser.util.CollectionUtils;
import org.jreleaser.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.System.lineSeparator;
import static org.jreleaser.sdk.mavencentral.api.State.VALIDATED;
import static org.jreleaser.util.StringUtils.capitalize;
import static org.jreleaser.util.StringUtils.getClassNameForLowerCaseHyphenSeparatedName;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.12.0
 */
public class MavenCentralMavenDeployer extends AbstractMavenDeployer<org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer,
    org.jreleaser.model.internal.deploy.maven.MavenCentralMavenDeployer> {

    private org.jreleaser.model.internal.deploy.maven.MavenCentralMavenDeployer deployer;

    public MavenCentralMavenDeployer(JReleaserContext context) {
        super(context);
    }

    @Override
    public org.jreleaser.model.internal.deploy.maven.MavenCentralMavenDeployer getDeployer() {
        return deployer;
    }

    @Override
    public void setDeployer(org.jreleaser.model.internal.deploy.maven.MavenCentralMavenDeployer deployer) {
        this.deployer = deployer;
    }

    @Override
    public String getType() {
        return org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer.TYPE;
    }

    @Override
    public void deploy(String name) throws DeployException {
        String deploymentId = deployer.getDeploymentId();

        if (isNotBlank(deploymentId)) {
            context.getLogger().debug(RB.$("set.to", "deploymentId", deploymentId));
        }

        Path bundleZip = null;

        if (deployer.getStage() != Stage.PUBLISH) {
            Set<Deployable> deployables = collectDeployables();

            if (deployables.isEmpty()) {
                context.getLogger().info(RB.$("artifacts.no.match"));
                return;
            }

            bundleZip = createDeploymentBundle();
        }

        MavenCentral mavenCentral = new MavenCentral(context.asImmutable(),
            deployer.getResolvedUrl(context.fullProps()),
            deployer.getUsername(), deployer.getPassword(),
            deployer.getConnectTimeout(), deployer.getReadTimeout(), context.isDryrun(),
            deployer.getRetryDelay(), deployer.getMaxRetries());

        context.getAdditionalProperties().put(prefix("namespace"), deployer.getNamespace());
        Deployment deployment = null;

        if (deployer.getStage() != Stage.PUBLISH) {
            Optional<String> did = uploadArtifacts(mavenCentral, bundleZip);
            if (did.isPresent()) {
                deploymentId = did.get();
                context.getAdditionalProperties().put(prefix("deploymentId"), deploymentId);
                deployment = getDeployment(deploymentId, mavenCentral);
            }
        } else {
            context.getAdditionalProperties().put(prefix("deploymentId"), deploymentId);
            deployment = getDeployment(deploymentId, mavenCentral);
            checkDeploymentIsValid(deploymentId, deployment);
        }

        if (deployer.getStage() != Stage.UPLOAD && null != deployment) {
            checkDeploymentIsValid(deploymentId, deployment);
            try {
                context.getLogger().info(RB.$("maven.central.publish.deployment", deploymentId));
                mavenCentral.publish(deploymentId);
            } catch (MavenCentralException e) {
                throw new DeployException(RB.$("ERROR_maven_central_publish_deployment", deploymentId), e);
            }
        }
    }

    private void checkDeploymentIsValid(String deploymentId, Deployment deployment) throws DeployException {
        if (deployment.getDeploymentState() != VALIDATED) {
            Set<String> messages = resolveErrorMessages(deployment);
            String title = RB.$("maven.central.wait.deployment.invalid.state", deploymentId, CollectionUtils.listOf(VALIDATED), deployment.getDeploymentState());
            if (!messages.isEmpty()) {
                throw new DeployException(title + lineSeparator() + String.join(lineSeparator(), messages));
            } else {
                throw new DeployException(title);
            }
        }
    }

    private Deployment getDeployment(String deploymentId, MavenCentral mavenCentral) throws DeployException {
        Deployment deployment = resolveDeployment(mavenCentral, deploymentId);
        if (!deployment.getErrors().isEmpty()) {
            Set<String> messages = resolveErrorMessages(deployment);
            String title = RB.$("maven.central.deployment.failure", deploymentId);
            throw new DeployException(title + lineSeparator() + String.join(lineSeparator(), messages));
        }

        return deployment;
    }

    private Set<String> resolveErrorMessages(Deployment deployment) {
        Set<String> messages = new LinkedHashSet<>();

        for (Map.Entry<String, List<String>> e : deployment.getErrors().entrySet()) {
            for (String error : e.getValue()) {
                messages.add(e.getKey() + " " + error);
            }
        }

        return messages;
    }

    private Deployment resolveDeployment(MavenCentral mavenCentral, String deploymentId) throws DeployException {
        try {
            Optional<Deployment> deployment = mavenCentral.status(deploymentId);
            if (deployment.isPresent()) {
                return deployment.get();
            }
            throw new DeployException(RB.$("ERROR_maven_central_find_deployment", deploymentId));
        } catch (MavenCentralException e) {
            throw new DeployException(RB.$("ERROR_maven_central_find_deployment", deploymentId), e);
        }
    }

    private Path createDeploymentBundle() throws DeployException {
        Path bundleZip = context.getDeployDirectory().resolve(deployer.getType())
            .resolve(deployer.getName())
            .resolve(deployer.getNamespace() +
                "-" + context.getModel().getProject().getResolvedName() +
                "-" + context.getModel().getProject().getResolvedVersion() +
                "-bundle.zip");

        try {
            Path bundleDir = Files.createTempDirectory("maven-central");
            for (String stagingRepository : deployer.getStagingRepositories()) {
                Path stagingRepositoryDir = context.getBasedir().resolve(stagingRepository).normalize();
                FileUtils.copyFilesRecursive(context.getLogger(), stagingRepositoryDir, bundleDir,
                    p -> p.getFileName().toString().contains("maven-metadata.xml"));
            }

            Files.createDirectories(bundleZip.getParent());
            FileUtils.zip(bundleDir, bundleZip);

            return bundleZip;
        } catch (IOException e) {
            throw new DeployException(RB.$("ERROR_maven_central_create_bundle", bundleZip.toAbsolutePath()), e);
        }
    }

    private Optional<String> uploadArtifacts(MavenCentral mavenCentral, Path bundleZip) throws DeployException {
        context.getLogger().info(" - {}", bundleZip.getFileName());

        boolean success = true;
        for (Deployable deployable : collectDeployableArtifacts()) {
            if (mavenCentral.artifactExists(deployable, context.getModel().getProject().isSnapshot() ? null : getDeployer().getVerifyUrl())) {
                success = false;
            }
        }

        if (!success) {
            throw new DeployException(RB.$("ERROR_nexus_deploy_artifacts"));
        }

        if (!context.isDryrun()) {
            try {
                context.getLogger().info(RB.$("maven.central.upload.bundle", bundleZip.getFileName()));
                return Optional.of(mavenCentral.upload(bundleZip.toAbsolutePath()));
            } catch (MavenCentralException e) {
                context.getLogger().trace(e);
                throw new DeployException(RB.$("ERROR_unexpected_deploy",
                    context.getBasedir().relativize(bundleZip), e.getMessage()), e);
            }
        }

        return Optional.empty();
    }

    private String prefix(String input) {
        return "deploy" +
            capitalize(getDeployer().getType()) +
            getClassNameForLowerCaseHyphenSeparatedName(getDeployer().getName()) +
            capitalize(input);
    }
}
