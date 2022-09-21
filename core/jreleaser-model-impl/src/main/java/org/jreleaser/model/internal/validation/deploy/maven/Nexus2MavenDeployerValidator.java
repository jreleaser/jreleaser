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

import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.deploy.maven.Nexus2MavenDeployer;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.deploy.maven.MavenDeployersValidator.validateMavenDeployer;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public abstract class Nexus2MavenDeployerValidator extends Validator {
    public static void validateNexus2MavenDeployer(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, Nexus2MavenDeployer> artifactory = context.getModel().getDeploy().getMaven().getNexus2();
        if (!artifactory.isEmpty()) context.getLogger().debug("deploy.maven.artifactory");

        for (Map.Entry<String, Nexus2MavenDeployer> e : artifactory.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig()) {
                validateNexus2MavenDeployer(context, mode, e.getValue(), errors);
            }
        }
    }

    private static void validateNexus2MavenDeployer(JReleaserContext context, Mode mode, Nexus2MavenDeployer mavenDeployer, Errors errors) {
        if (isNotBlank(mavenDeployer.getUrl()) &&
            mavenDeployer.getUrl().contains("oss.sonatype.org") &&
            !mavenDeployer.isApplyMavenCentralRulesSet()) {
            mavenDeployer.setApplyMavenCentralRules(true);
        }

        validateMavenDeployer(context, mode, mavenDeployer, errors);
    }
}
