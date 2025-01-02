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
package org.jreleaser.engine.catalog.sbom;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.catalog.CatalogProcessingException;
import org.jreleaser.model.spi.catalog.sbom.SbomCatalogerProcessor;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public class ProjectSbomCataloger {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.catalog.sbom.SbomCataloger<?> cataloger;

    private ProjectSbomCataloger(JReleaserContext context,
                                 org.jreleaser.model.internal.catalog.sbom.SbomCataloger<?> cataloger) {
        this.context = context;
        this.cataloger = cataloger;
    }

    public org.jreleaser.model.internal.catalog.sbom.SbomCataloger<?> getSbomCataloger() {
        return cataloger;
    }

    public SbomCatalogerProcessor.Result catalog() throws CatalogProcessingException {
        if (!cataloger.isEnabled()) {
            context.getLogger().debug(RB.$("catalogers.skip.catalog"), cataloger.getType());
            return SbomCatalogerProcessor.Result.SKIPPED;
        }

        return ProjectSbomCatalogers.findSbomCataloger(context, cataloger)
            .catalog();
    }

    public static ProjectSbomCatalogerBuilder builder() {
        return new ProjectSbomCatalogerBuilder();
    }

    public static class ProjectSbomCatalogerBuilder {
        private JReleaserContext context;
        private org.jreleaser.model.internal.catalog.sbom.SbomCataloger<?> cataloger;

        public ProjectSbomCatalogerBuilder context(JReleaserContext context) {
            this.context = requireNonNull(context, "'context' must not be null");
            return this;
        }

        public ProjectSbomCatalogerBuilder cataloger(org.jreleaser.model.internal.catalog.sbom.SbomCataloger<?> cataloger) {
            this.cataloger = requireNonNull(cataloger, "'cataloger' must not be null");
            return this;
        }

        public ProjectSbomCataloger build() {
            requireNonNull(context, "'context' must not be null");
            requireNonNull(cataloger, "'cataloger' must not be null");
            return new ProjectSbomCataloger(context, cataloger);
        }
    }
}
