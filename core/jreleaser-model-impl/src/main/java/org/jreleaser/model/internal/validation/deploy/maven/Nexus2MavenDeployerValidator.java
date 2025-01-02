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
import org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.Stage;
import org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.StageOperation;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.deploy.maven.Nexus2MavenDeployer;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.deploy.maven.MavenDeployersValidator.validateMavenDeployer;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class Nexus2MavenDeployerValidator {
    private Nexus2MavenDeployerValidator() {
        // noop
    }

    public static void validateNexus2MavenDeployer(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, Nexus2MavenDeployer> nexus2 = context.getModel().getDeploy().getMaven().getNexus2();
        if (!nexus2.isEmpty()) context.getLogger().debug("deploy.maven.nexus2");

        for (Map.Entry<String, Nexus2MavenDeployer> e : nexus2.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateDeploy() || mode.validateConfig()) {
                validateNexus2MavenDeployer(context, e.getValue(), errors);
            }
        }
    }

    private static void validateNexus2MavenDeployer(JReleaserContext context, Nexus2MavenDeployer mavenDeployer, Errors errors) {
        if (isNotBlank(mavenDeployer.getUrl()) &&
            mavenDeployer.getUrl().contains("oss.sonatype.org") &&
            !mavenDeployer.isApplyMavenCentralRulesSet()) {
            mavenDeployer.setApplyMavenCentralRules(true);
        }

        if (isBlank(mavenDeployer.getVerifyUrl()) &&
            mavenDeployer.isApplyMavenCentralRules() &&
            mavenDeployer.getUrl().contains("oss.sonatype.org")) {
            mavenDeployer.setVerifyUrl("https://repo1.maven.org/maven2/{{path}}/{{filename}}");
        }

        if (mavenDeployer.getTransitionDelay() <= 0) {
            mavenDeployer.setTransitionDelay(10);
        }
        if (mavenDeployer.getTransitionMaxRetries() <= 0) {
            mavenDeployer.setTransitionMaxRetries(60);
        }

        validateMavenDeployer(context, mavenDeployer, errors);
        if (!mavenDeployer.isEnabled()) return;

        if (context.getModel().getProject().isSnapshot()) {
            mavenDeployer.setSnapshotUrl(
                checkProperty(context,
                    mavenDeployer.keysFor("snapshot.url"),
                    "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".snapshotUrl",
                    mavenDeployer.getSnapshotUrl(),
                    errors));
        }

        mavenDeployer.setStagingProfileId(
            checkProperty(context,
                mavenDeployer.keysFor("staging.profile.id"),
                "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".stagingProfileId",
                mavenDeployer.getStagingProfileId(), ""));

        mavenDeployer.setStagingRepositoryId(
            checkProperty(context,
                mavenDeployer.keysFor("staging.repository.id"),
                "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".stagingRepositoryId",
                mavenDeployer.getStagingRepositoryId(), ""));

        mavenDeployer.setStartStage(
            Stage.of(checkProperty(context,
                mavenDeployer.keysFor("start.stage"),
                "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".startStage",
                mavenDeployer.getStartStage(), null)));

        mavenDeployer.setEndStage(
            Stage.of(checkProperty(context,
                mavenDeployer.keysFor("end.stage"),
                "deploy.maven." + mavenDeployer.getType() + "." + mavenDeployer.getName() + ".endStage",
                mavenDeployer.getEndStage(), null)));

        try {
            StageOperation.of(mavenDeployer.getStartStage(), mavenDeployer.getEndStage());
        } catch (IllegalArgumentException e) {
            errors.configuration(e.getMessage());
        }

        if (isBlank(context.getModel().getProject().getLanguages().getJava().getGroupId())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "project.java.groupId"));
        }
    }
}
