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
package org.jreleaser.model.internal.validation.deploy.maven;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.deploy.maven.Maven;
import org.jreleaser.model.internal.deploy.maven.MavenDeployer;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.util.DefaultVersions;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.model.internal.validation.deploy.maven.ArtifactoryMavenDeployerValidator.validateArtifactoryMavenDeployer;
import static org.jreleaser.model.internal.validation.deploy.maven.AzureMavenDeployerValidator.validateAzureMavenDeployer;
import static org.jreleaser.model.internal.validation.deploy.maven.GiteaMavenDeployerValidator.validateGiteaMavenDeployer;
import static org.jreleaser.model.internal.validation.deploy.maven.GithubMavenDeployerValidator.validateGithubMavenDeployer;
import static org.jreleaser.model.internal.validation.deploy.maven.GitlabMavenDeployerValidator.validateGitlabMavenDeployer;
import static org.jreleaser.model.internal.validation.deploy.maven.MavenCentralMavenDeployerValidator.validateMavenCentralMavenDeployer;
import static org.jreleaser.model.internal.validation.deploy.maven.Nexus2MavenDeployerValidator.validateNexus2MavenDeployer;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class MavenDeployersValidator {
    private MavenDeployersValidator() {
        // noop
    }

    public static void validateMavenDeployers(JReleaserContext context, Mode mode, Errors errors) {
        Maven maven = context.getModel().getDeploy().getMaven();
        context.getLogger().debug("deploy.maven");

        validatePomchecker(context);
        validateArtifactoryMavenDeployer(context, mode, errors);
        validateAzureMavenDeployer(context, mode, errors);
        validateGiteaMavenDeployer(context, mode, errors);
        validateGithubMavenDeployer(context, mode, errors);
        validateGitlabMavenDeployer(context, mode, errors);
        validateNexus2MavenDeployer(context, mode, errors);
        validateMavenCentralMavenDeployer(context, mode, errors);

        if (mode.validateDeploy() || mode.validateConfig()) {
            boolean activeSet = maven.isActiveSet();
            resolveActivatable(context, maven, "deploy.maven", "ALWAYS");
            maven.resolveEnabledWithSnapshot(context.getModel().getProject());

            if (maven.isEnabled()) {
                boolean enabled = !maven.getActiveArtifactories().isEmpty() ||
                    !maven.getActiveAzures().isEmpty() ||
                    !maven.getActiveGiteas().isEmpty() ||
                    !maven.getActiveGithubs().isEmpty() ||
                    !maven.getActiveGitlabs().isEmpty() ||
                    !maven.getActiveNexus2s().isEmpty() ||
                    !maven.getActiveMavenCentrals().isEmpty();

                if (!activeSet && !enabled) {
                    context.getLogger().debug(RB.$("validation.disabled"));
                    maven.disable();
                }
            }
        }
    }

    private static void validatePomchecker(JReleaserContext context) {
        Maven.Pomchecker pomchecker = context.getModel().getDeploy().getMaven().getPomchecker();

        if (isBlank(pomchecker.getVersion())) {
            pomchecker.setVersion(DefaultVersions.getInstance().getPomcheckerVersion());
        }

        if (!pomchecker.isStrictSet()) {
            pomchecker.setStrict(true);
        }

        if (!pomchecker.isStrict() && !pomchecker.isFailOnWarningSet()) {
            pomchecker.setFailOnWarning(false);
        }

        if (!pomchecker.isFailOnWarningSet()) {
            pomchecker.setFailOnWarning(true);
        }

        if (!pomchecker.isFailOnErrorSet()) {
            pomchecker.setFailOnError(true);
        }
    }

    static void validateMavenDeployer(JReleaserContext context, MavenDeployer<?> mavenDeployer, Errors errors) {
        context.getLogger().debug("deploy.maven.{}.{}", mavenDeployer.getType(), mavenDeployer.getName());

        resolveActivatable(context, mavenDeployer,
            listOf("deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName(),
                "deploy.maven." + mavenDeployer.getType()),
            "NEVER");
        if (!mavenDeployer.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().warn(RB.$("deployer.disabled", mavenDeployer.getType(), mavenDeployer.getName()));
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        String deployerPrefix = "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName();

        mavenDeployer.setUrl(
            checkProperty(context,
                mavenDeployer.keysFor("url"),
                "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".url",
                mavenDeployer.getUrl(),
                errors));

        if (isNotBlank(mavenDeployer.getUrl()) && mavenDeployer.getUrl().endsWith("/")) {
            mavenDeployer.setUrl(mavenDeployer.getUrl().substring(0, mavenDeployer.getUrl().length() - 1));
        }

        String defaultUsername = null;
        String defaultPassword = null;
        BaseReleaser<?, ?> service = context.getModel().getRelease().getReleaser();
        if (null != service && mavenDeployer.getType().equalsIgnoreCase(service.getServiceName())) {
            defaultUsername = service.getUsername();
            defaultPassword = service.getToken();
        }
        String setUsername = mavenDeployer.getUsername();
        String setPassword = mavenDeployer.getPassword();
        if (isBlank(setUsername)) {
            setUsername = defaultUsername;
        }
        if (isBlank(setPassword)) {
            setPassword = defaultPassword;
        }

        switch (mavenDeployer.resolveAuthorization()) {
            case BEARER:
                mavenDeployer.setPassword(
                    checkProperty(context,
                        listOf(
                            "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".password",
                            "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".token",
                            "deploy.maven." + mavenDeployer.getType() + ".password",
                            "deploy.maven." + mavenDeployer.getType() + ".token",
                            mavenDeployer.getType() + "." + mavenDeployer.getName() + ".password",
                            mavenDeployer.getType() + "." + mavenDeployer.getName() + ".token",
                            mavenDeployer.getType() + ".password",
                            mavenDeployer.getType() + ".token"),
                        "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".password",
                        setPassword,
                        errors));
                break;
            case BASIC:
                mavenDeployer.setUsername(
                    checkProperty(context,
                        mavenDeployer.keysFor("username"),
                        "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".username",
                        setUsername,
                        errors));

                mavenDeployer.setPassword(
                    checkProperty(context,
                        listOf(
                            "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".password",
                            "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".token",
                            "deploy.maven." + mavenDeployer.getType() + ".password",
                            "deploy.maven." + mavenDeployer.getType() + ".token",
                            mavenDeployer.getType() + "." + mavenDeployer.getName() + ".password",
                            mavenDeployer.getType() + "." + mavenDeployer.getName() + ".token",
                            mavenDeployer.getType() + ".password",
                            mavenDeployer.getType() + ".token"),
                        "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".password",
                        setPassword,
                        errors));
                break;
            case NONE:
                errors.configuration(RB.$("validation_value_cannot_be", deployerPrefix + ".authorization", "NONE"));
                context.getLogger().debug(RB.$("validation.disabled.error"));
                mavenDeployer.disable();
                break;
        }

        validateTimeout(mavenDeployer);

        if (mavenDeployer.getStagingRepositories().isEmpty()) {
            errors.configuration(RB.$("validation_must_not_be_empty", deployerPrefix + ".stagingDirectories"));
        }

        if (mavenDeployer.isApplyMavenCentralRules() && !mavenDeployer.isSignSet()) {
            mavenDeployer.setSign(true);
        }

        if (mavenDeployer.isApplyMavenCentralRules() && !mavenDeployer.isChecksumsSet()) {
            mavenDeployer.setChecksums(true);
        }

        if (mavenDeployer.isApplyMavenCentralRules() && !mavenDeployer.isVerifyPomSet()) {
            mavenDeployer.setVerifyPom(true);
        }

        if (mavenDeployer.isSign() && !context.getModel().getSigning().isEnabled() && !context.isDryrun() /*&& !mode.validateConfig()*/) {
            errors.configuration(RB.$("validation_maven_deployer_signing", deployerPrefix));
        }

        int index = 0;
        for (MavenDeployer.ArtifactOverride artifactOverride : mavenDeployer.getArtifactOverrides()) {
            if (isBlank(artifactOverride.getGroupId())) {
                artifactOverride.setGroupId(context.getModel().getProject().getLanguages().getJava().getGroupId());
            }

            if (isBlank(artifactOverride.getGroupId())) {
                errors.configuration(RB.$("validation_must_not_be_null", deployerPrefix + ".artifactOverrides[" + index + "].groupId"));
            }

            if (isBlank(artifactOverride.getArtifactId())) {
                errors.configuration(RB.$("validation_must_not_be_null", deployerPrefix + ".artifactOverrides[" + index + "].artifactId"));
            }

            index++;
        }
    }
}