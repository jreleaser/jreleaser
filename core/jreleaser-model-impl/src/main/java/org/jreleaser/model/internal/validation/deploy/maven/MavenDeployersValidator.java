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
package org.jreleaser.model.internal.validation.deploy.maven;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.deploy.maven.Maven;
import org.jreleaser.model.internal.deploy.maven.MavenDeployer;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.util.Locale;

import static org.jreleaser.model.internal.validation.deploy.maven.ArtifactoryMavenDeployerValidator.validateArtifactoryMavenDeployer;
import static org.jreleaser.model.internal.validation.deploy.maven.GiteaMavenDeployerValidator.validateGiteaMavenDeployer;
import static org.jreleaser.model.internal.validation.deploy.maven.GithubMavenDeployerValidator.validateGithubMavenDeployer;
import static org.jreleaser.model.internal.validation.deploy.maven.GitlabMavenDeployerValidator.validateGitlabMavenDeployer;
import static org.jreleaser.model.internal.validation.deploy.maven.Nexus2MavenDeployerValidator.validateNexus2MavenDeployer;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public abstract class MavenDeployersValidator extends Validator {
    public static void validateMavenDeployers(JReleaserContext context, Mode mode, Errors errors) {
        Maven maven = context.getModel().getDeploy().getMaven();
        context.getLogger().debug("deploy.maven");

        validateArtifactoryMavenDeployer(context, mode, errors);
        validateGiteaMavenDeployer(context, mode, errors);
        validateGithubMavenDeployer(context, mode, errors);
        validateGitlabMavenDeployer(context, mode, errors);
        validateNexus2MavenDeployer(context, mode, errors);

        if (mode.validateDeploy() || mode.validateConfig()) {
            boolean activeSet = maven.isActiveSet();
            maven.resolveEnabled(context.getModel().getProject());

            if (maven.isEnabled()) {
                boolean enabled = !maven.getActiveArtifactories().isEmpty() ||
                    !maven.getActiveGiteas().isEmpty() ||
                    !maven.getActiveGithubs().isEmpty() ||
                    !maven.getActiveGitlabs().isEmpty() ||
                    !maven.getActiveNexus2s().isEmpty();

                if (!activeSet && !enabled) {
                    context.getLogger().debug(RB.$("validation.disabled"));
                    maven.disable();
                }
            }
        }
    }

    static void validateMavenDeployer(JReleaserContext context, Mode mode, MavenDeployer mavenDeployer, Errors errors) {
        context.getLogger().debug("deploy.maven.{}.{}", mavenDeployer.getType(), mavenDeployer.getName());

        if (!mavenDeployer.isActiveSet()) {
            mavenDeployer.setActive(Active.NEVER);
        }
        if (!mavenDeployer.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        String baseEnvKey = mavenDeployer.getType().toLowerCase(Locale.ENGLISH);
        String deployerPrefix = "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName();

        mavenDeployer.setUrl(
            checkProperty(context,
                baseEnvKey + "_" + Env.toVar(mavenDeployer.getName()) + "_URL",
                "maven.deploy." + mavenDeployer.getType() + "." + mavenDeployer.getName()+ ".url",
                mavenDeployer.getUrl(),
                errors));

        if (isNotBlank(mavenDeployer.getUrl()) && mavenDeployer.getUrl().endsWith("/")) {
            mavenDeployer.setUrl(mavenDeployer.getUrl().substring(0, mavenDeployer.getUrl().length() - 1));
        }

        switch (mavenDeployer.resolveAuthorization()) {
            case BEARER:
                mavenDeployer.setPassword(
                    checkProperty(context,
                        listOf(
                            baseEnvKey + "_" + Env.toVar(mavenDeployer.getName()) + "_PASSWORD",
                            baseEnvKey + "_" + Env.toVar(mavenDeployer.getName()) + "_TOKEN",
                            baseEnvKey + "_PASSWORD",
                            baseEnvKey + "_TOKEN"),
                        "maven.deploy." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".password",
                        mavenDeployer.getPassword(),
                        errors,
                        context.isDryrun()));
                break;
            case BASIC:
                mavenDeployer.setUsername(
                    checkProperty(context,
                        listOf(
                            baseEnvKey + "_" + Env.toVar(mavenDeployer.getName()) + "_USERNAME",
                            baseEnvKey + "_USERNAME"),
                        "maven.deploy." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".username",
                        mavenDeployer.getUsername(),
                        errors,
                        context.isDryrun()));

                mavenDeployer.setPassword(
                    checkProperty(context,
                        listOf(
                            baseEnvKey + "_" + Env.toVar(mavenDeployer.getName()) + "_PASSWORD",
                            baseEnvKey + "_" + Env.toVar(mavenDeployer.getName()) + "_TOKEN",
                            baseEnvKey + "_PASSWORD",
                            baseEnvKey + "_TOKEN"),
                        "maven.deploy." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".password",
                        mavenDeployer.getPassword(),
                        errors,
                        context.isDryrun()));
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

        if (mavenDeployer.isApplyMavenCentralRules() && !mavenDeployer.isVerifyPomSet()) {
            mavenDeployer.setVerifyPom(true);
        }

        if (mavenDeployer.isSign() && !context.getModel().getSigning().isEnabled()) {
            if (!context.isDryrun() /*&& !mode.validateConfig()*/) {
                errors.configuration(RB.$("validation_maven_deployer_signing", deployerPrefix));
            }
        }
    }
}