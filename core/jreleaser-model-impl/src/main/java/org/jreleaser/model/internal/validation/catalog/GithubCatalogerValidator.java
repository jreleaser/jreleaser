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
package org.jreleaser.model.internal.validation.catalog;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.catalog.GithubCataloger;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;

/**
 * @author Andres Almiray
 * @since 1.13.0
 */
public final class GithubCatalogerValidator {
    private GithubCatalogerValidator() {
        // noop
    }

    public static void validateGithubCataloger(JReleaserContext context, Mode mode, Errors errors) {
        GithubCataloger slsa = context.getModel().getCatalog().getGithub();
        context.getLogger().debug("catalog.github");

        resolveActivatable(context, slsa, "catalog.github", "NEVER");
        if (!slsa.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        slsa.setAttestationName(
            checkProperty(context,
                "catalog.github.attestation.name",
                "github.attestationName",
                slsa.getAttestationName(),
                "{{projectName}}-{{projectEffectiveVersion}}"));
    }
}
