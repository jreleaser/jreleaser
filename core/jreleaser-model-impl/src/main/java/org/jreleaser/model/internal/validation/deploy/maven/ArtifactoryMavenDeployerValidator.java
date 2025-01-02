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
import org.jreleaser.model.internal.deploy.maven.ArtifactoryMavenDeployer;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.deploy.maven.MavenDeployersValidator.validateMavenDeployer;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class ArtifactoryMavenDeployerValidator {
    private ArtifactoryMavenDeployerValidator() {
        // noop
    }

    public static void validateArtifactoryMavenDeployer(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, ArtifactoryMavenDeployer> artifactory = context.getModel().getDeploy().getMaven().getArtifactory();
        if (!artifactory.isEmpty()) context.getLogger().debug("deploy.maven.artifactory");

        for (Map.Entry<String, ArtifactoryMavenDeployer> e : artifactory.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateDeploy() || mode.validateConfig()) {
                validateMavenDeployer(context, e.getValue(), errors);
            }
        }
    }
}
