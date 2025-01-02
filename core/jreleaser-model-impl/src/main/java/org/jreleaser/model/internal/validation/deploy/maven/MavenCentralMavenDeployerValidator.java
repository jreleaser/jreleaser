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
import org.jreleaser.model.internal.deploy.maven.MavenCentralMavenDeployer;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.util.Map;
import java.util.Properties;

import static org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer.DEPLOYMENT_ID;
import static org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer.Stage.FULL;
import static org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer.Stage.PUBLISH;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.deploy.maven.MavenDeployersValidator.validateMavenDeployer;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.12.0
 */
public final class MavenCentralMavenDeployerValidator {
    private MavenCentralMavenDeployerValidator() {
        // noop
    }

    public static void validateMavenCentralMavenDeployer(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, MavenCentralMavenDeployer> mavenCentral = context.getModel().getDeploy().getMaven().getMavenCentral();
        if (!mavenCentral.isEmpty()) context.getLogger().debug("deploy.maven.mavenCentral");

        for (Map.Entry<String, MavenCentralMavenDeployer> e : mavenCentral.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateDeploy() || mode.validateConfig()) {
                validateMavenCentralMavenDeployer(context, e.getValue(), errors);
            }
        }
    }

    private static void validateMavenCentralMavenDeployer(JReleaserContext context, MavenCentralMavenDeployer mavenDeployer, Errors errors) {
        if (!mavenDeployer.isApplyMavenCentralRulesSet()) {
            mavenDeployer.setApplyMavenCentralRules(true);
        }

        if (isBlank(mavenDeployer.getVerifyUrl()) &&
            mavenDeployer.isApplyMavenCentralRules()) {
            mavenDeployer.setVerifyUrl("https://repo1.maven.org/maven2/{{path}}/{{filename}}");
        }

        validateMavenDeployer(context, mavenDeployer, errors);
        if (!mavenDeployer.isEnabled()) return;

        Properties vars = context.getModel().getEnvironment().getVars();
        mavenDeployer.setStage(Env.resolve(mavenDeployer.keysFor("STAGE"), vars));
        mavenDeployer.setDeploymentId(Env.resolve(mavenDeployer.keysFor(DEPLOYMENT_ID), vars));

        if (null == mavenDeployer.getStage()) {
            mavenDeployer.setStage(FULL);
        }

        if (mavenDeployer.getRetryDelay() <= 0) {
            mavenDeployer.setRetryDelay(10);
        }
        if (mavenDeployer.getMaxRetries() <= 0) {
            mavenDeployer.setMaxRetries(60);
        }

        if (mavenDeployer.getStage() == PUBLISH) {
            mavenDeployer.setDeploymentId(
                checkProperty(context,
                    mavenDeployer.keysFor("deployment.id"),
                    "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".deploymentId",
                    mavenDeployer.getDeploymentId(),
                    errors));
        }

        Errors tmp = new Errors();
        mavenDeployer.setNamespace(
            checkProperty(context,
                mavenDeployer.keysFor("namespace"),
                "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".namespace",
                mavenDeployer.getNamespace(),
                tmp));

        if (isBlank(mavenDeployer.getNamespace())) {
            mavenDeployer.setNamespace(context.getModel().getProject().getLanguages().getJava().getGroupId());
        }

        if (isBlank(mavenDeployer.getNamespace())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "project.java.groupId"));
            errors.addAll(tmp);
        }
    }
}
