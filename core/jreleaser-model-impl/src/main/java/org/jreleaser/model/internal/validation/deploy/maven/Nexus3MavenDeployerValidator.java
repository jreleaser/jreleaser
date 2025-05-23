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
import org.jreleaser.model.internal.deploy.maven.Nexus3MavenDeployer;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.Validator.mergeErrors;
import static org.jreleaser.model.internal.validation.deploy.maven.MavenDeployersValidator.validateMavenDeployer;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.18.0
 */
public final class Nexus3MavenDeployerValidator {
    private Nexus3MavenDeployerValidator() {
        // noop
    }

    public static void validateNexus3MavenDeployer(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, Nexus3MavenDeployer> nexus3 = context.getModel().getDeploy().getMaven().getNexus3();
        if (!nexus3.isEmpty()) context.getLogger().debug("deploy.maven.nexus3");

        for (Map.Entry<String, Nexus3MavenDeployer> e : nexus3.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateDeploy() || mode.validateConfig()) {
                Errors incoming = new Errors();
                validateNexus3MavenDeployer(context, e.getValue(), incoming);
                mergeErrors(context, errors, incoming, e.getValue());
            }
        }
    }

    private static void validateNexus3MavenDeployer(JReleaserContext context, Nexus3MavenDeployer mavenDeployer, Errors errors) {
        validateMavenDeployer(context, mavenDeployer, errors);
        if (!mavenDeployer.isEnabled()) return;

        if (isBlank(context.getModel().getProject().getLanguages().getJava().getGroupId())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "project.java.groupId"));
        }

        mavenDeployer.setSign(false);
        mavenDeployer.setChecksums(false);
    }
}
