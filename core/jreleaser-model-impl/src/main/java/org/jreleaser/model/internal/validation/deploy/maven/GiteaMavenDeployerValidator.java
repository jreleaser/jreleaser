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

import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.deploy.maven.GiteaMavenDeployer;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.deploy.maven.MavenDeployersValidator.validateMavenDeployer;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class GiteaMavenDeployerValidator {
    private GiteaMavenDeployerValidator() {
        // noop
    }

    public static void validateGiteaMavenDeployer(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, GiteaMavenDeployer> gitea = context.getModel().getDeploy().getMaven().getGitea();
        if (!gitea.isEmpty()) context.getLogger().debug("deploy.maven.gitea");

        for (Map.Entry<String, GiteaMavenDeployer> e : gitea.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateDeploy() || mode.validateConfig()) {
                validateGiteaMavenDeployer(context, e.getValue(), errors);
            }
        }
    }

    private static void validateGiteaMavenDeployer(JReleaserContext context, GiteaMavenDeployer mavenDeployer, Errors errors) {
        validateMavenDeployer(context, mavenDeployer, errors);
        if (!mavenDeployer.isEnabled()) return;

        BaseReleaser<?, ?> service = context.getModel().getRelease().getReleaser();

        mavenDeployer.setUsername(
            checkProperty(context,
                listOf(
                    "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".username",
                    "deploy.maven." + mavenDeployer.getType() + ".username",
                    mavenDeployer.getType() + "." + mavenDeployer.getName() + ".username",
                    mavenDeployer.getType() + ".username"),
                "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".username",
                mavenDeployer.getUsername(),
                service.getUsername()));

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
                mavenDeployer.getPassword(),
                service.getToken()));

        if (isBlank(mavenDeployer.getUsername())) {
            mavenDeployer.setUsername(context.getModel().getRelease().getReleaser().getUsername());
        }
    }
}
