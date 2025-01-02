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
import org.jreleaser.model.internal.catalog.Catalog;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.catalog.GithubCatalogerValidator.validateGithubCataloger;
import static org.jreleaser.model.internal.validation.catalog.SlsaCatalogerValidator.validateSlsaCataloger;
import static org.jreleaser.model.internal.validation.catalog.sbom.SbomCatalogersValidator.validateSbomCatalogers;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class CatalogValidator {
    private CatalogValidator() {
        // noop
    }

    public static void validateCatalog(JReleaserContext context, Mode mode, Errors errors) {
        context.getLogger().debug("catalog");

        Catalog catalog = context.getModel().getCatalog();
        validateSbomCatalogers(context, mode, errors);
        validateGithubCataloger(context, mode, errors);
        validateSlsaCataloger(context, mode, errors);

        if (mode.validateConfig()) {
            boolean activeSet = catalog.isActiveSet();
            resolveActivatable(context, catalog, "catalog", "ALWAYS");
            catalog.resolveEnabled(context.getModel().getProject());

            if (catalog.isEnabled()) {
                boolean enabled = catalog.getSbom().isEnabled() ||
                    catalog.getGithub().isEnabled() ||
                    catalog.getSlsa().isEnabled();

                if (!activeSet && !enabled) {
                    context.getLogger().debug(RB.$("validation.disabled"));
                    catalog.disable();
                }
            }
        }
    }
}