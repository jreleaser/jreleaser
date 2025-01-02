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
package org.jreleaser.model.internal.validation.deploy;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.deploy.Deploy;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.deploy.maven.MavenDeployersValidator.validateMavenDeployers;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class DeployValidator {
    private DeployValidator() {
        // noop
    }

    public static void validateDeploy(JReleaserContext context, Mode mode, Errors errors) {
        context.getLogger().debug("deploy");

        Deploy deploy = context.getModel().getDeploy();
        validateMavenDeployers(context, mode, errors);

        if (mode.validateDeploy() || mode.validateConfig()) {
            boolean activeSet = deploy.isActiveSet();
            resolveActivatable(context, deploy, "deploy", "ALWAYS");
            deploy.resolveEnabled(context.getModel().getProject());

            if (deploy.isEnabled()) {
                boolean enabled = deploy.getMaven().isEnabled();

                if (!activeSet && !enabled) {
                    context.getLogger().debug(RB.$("validation.disabled"));
                    deploy.disable();
                }
            }
        }
    }
}