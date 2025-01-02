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
package org.jreleaser.model.internal.validation.catalog.sbom;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.catalog.sbom.Sbom;
import org.jreleaser.model.internal.catalog.sbom.SbomCataloger;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.catalog.sbom.CyclonedxSbomCatalogerValidator.validateCyclonedxSbomCataloger;
import static org.jreleaser.model.internal.validation.catalog.sbom.SyftSbomCatalogerValidator.validateSyftSbomCataloger;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class SbomCatalogersValidator {
    private SbomCatalogersValidator() {
        // noop
    }

    public static void validateSbomCatalogers(JReleaserContext context, Mode mode, Errors errors) {
        Sbom sbom = context.getModel().getCatalog().getSbom();
        context.getLogger().debug("catalog.sbom");

        validateCyclonedxSbomCataloger(context, sbom.getCyclonedx(), errors);
        validateSyftSbomCataloger(context, sbom.getSyft(), errors);

        if (mode.validateConfig()) {
            boolean activeSet = sbom.isActiveSet();
            resolveActivatable(context, sbom, "catalog.sbom", "ALWAYS");
            sbom.resolveEnabledWithSnapshot(context.getModel().getProject());

            if (sbom.isEnabled()) {
                boolean enabled = sbom.getCyclonedx().isEnabled() ||
                    sbom.getSyft().isEnabled();

                if (!activeSet && !enabled) {
                    context.getLogger().debug(RB.$("validation.disabled"));
                    sbom.disable();
                }
            }
        }
    }

    static void validateSbomCataloger(JReleaserContext context, SbomCataloger<?> sbomCataloger, Errors errors) {
        context.getLogger().debug("catalog.sbom.{}", sbomCataloger.getType());

        resolveActivatable(context, sbomCataloger,
            "catalog.sbom." + sbomCataloger.getType(),
            "NEVER");
        if (!sbomCataloger.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!sbomCataloger.isDistributionsSet()) {
            sbomCataloger.setDistributions(true);
        }

        if (!sbomCataloger.isFilesSet()) {
            sbomCataloger.setFiles(true);
        }

        if (!sbomCataloger.isDistributions() && !sbomCataloger.isFiles()) {
            sbomCataloger.disable();
        }

        if (isBlank(sbomCataloger.getPack().getName())) {
            sbomCataloger.getPack().setName("{{projectName}}-{{projectVersion}}-sboms");
        }
    }
}