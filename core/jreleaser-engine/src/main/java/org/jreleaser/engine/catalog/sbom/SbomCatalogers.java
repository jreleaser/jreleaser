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
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.hooks.ExecutionEvent;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.catalog.sbom.Sbom;
import org.jreleaser.model.internal.catalog.sbom.SbomCataloger;
import org.jreleaser.model.spi.catalog.CatalogProcessingException;
import org.jreleaser.model.spi.catalog.sbom.SbomCatalogerProcessor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static org.jreleaser.engine.catalog.CatalogerSupport.fireCatalogEvent;
import static org.jreleaser.model.internal.JReleaserSupport.supportedSbomCatalogers;
import static org.jreleaser.model.spi.catalog.sbom.SbomCatalogerProcessor.Result.SKIPPED;
import static org.jreleaser.model.spi.catalog.sbom.SbomCatalogerProcessor.Result.UPTODATE;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class SbomCatalogers {
    private SbomCatalogers() {
        // noop
    }

    public static void catalog(JReleaserContext context) {
        context.getLogger().increaseIndent();
        context.getLogger().setPrefix("sbom");

        Sbom sbom = context.getModel().getCatalog().getSbom();
        if (!sbom.isEnabled()) {
            context.getLogger().info(RB.$("catalogers.not.enabled"));
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
            return;
        }

        try {
            doCatalog(context, sbom);
        } finally {
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
        }
    }

    private static void doCatalog(JReleaserContext context, Sbom sbom) {
        Set<SbomCatalogerOutcome> outcomes = new LinkedHashSet<>();

        if (!context.getIncludedCatalogers().isEmpty()) {
            for (String catalogerType : context.getIncludedCatalogers()) {
                // check if the catalogerType is valid
                if (!supportedSbomCatalogers().contains(catalogerType)) {
                    context.getLogger().warn(RB.$("ERROR_unsupported_cataloger", catalogerType));
                    continue;
                }

                SbomCataloger<?> cataloger = sbom.findSbomCataloger(catalogerType);
                context.getLogger().info(RB.$("catalogers.catalog.all.artifacts.with"), catalogerType);
                outcomes.add(new SbomCatalogerOutcome(catalogerType, cataloger, catalog(context, cataloger)));
            }
        } else {
            context.getLogger().info(RB.$("catalogers.catalog.all.artifacts"));
            for (SbomCataloger<?> cataloger : sbom.findAllActiveSbomCatalogers()) {
                String catalogerType = cataloger.getType();

                if (context.getExcludedCatalogers().contains(catalogerType)) {
                    context.getLogger().info(RB.$("catalogers.cataloger.excluded"), catalogerType);
                    continue;
                }

                outcomes.add(new SbomCatalogerOutcome(catalogerType, cataloger, catalog(context, cataloger)));
            }
        }

        Map<SbomCatalogerProcessor.Result, List<SbomCatalogerOutcome>> groupedBy = outcomes.stream()
            .collect(groupingBy(SbomCatalogerOutcome::getResult));

        boolean allSkipped = outcomes.size() == groupedBy.computeIfAbsent(SKIPPED, result -> emptyList()).size();
        boolean allUptodate = outcomes.size() == groupedBy.computeIfAbsent(UPTODATE, result -> emptyList()).size();

        if (allSkipped) {
            context.getLogger().info(RB.$("catalogers.not.triggered"));
        } else if (allUptodate) {
            context.getLogger().info(RB.$("catalog.sbom.not.changed"));
        }
    }

    private static SbomCatalogerProcessor.Result catalog(JReleaserContext context, SbomCataloger<?> cataloger) {
        try {
            context.getLogger().increaseIndent();
            context.getLogger().setPrefix(cataloger.getType());

            fireCatalogEvent(ExecutionEvent.before(JReleaserCommand.CATALOG.toStep()), context, cataloger);

            ProjectSbomCataloger projectCataloger = createProjectCataloger(context, cataloger);
            SbomCatalogerProcessor.Result result = projectCataloger.catalog();
            fireCatalogEvent(ExecutionEvent.success(JReleaserCommand.CATALOG.toStep()), context, cataloger);
            return result;
        } catch (CatalogProcessingException e) {
            fireCatalogEvent(ExecutionEvent.failure(JReleaserCommand.CATALOG.toStep(), e), context, cataloger);
            throw new JReleaserException(RB.$("ERROR_unexpected_error"), e);
        } finally {
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
        }
    }

    private static ProjectSbomCataloger createProjectCataloger(JReleaserContext context,
                                                               SbomCataloger<?> cataloger) {
        return ProjectSbomCataloger.builder()
            .context(context)
            .cataloger(cataloger)
            .build();
    }

    private static class SbomCatalogerOutcome {
        private final String type;
        private final SbomCataloger<?> cataloger;
        private final SbomCatalogerProcessor.Result result;

        private SbomCatalogerOutcome(String type, SbomCataloger<?> cataloger, SbomCatalogerProcessor.Result result) {
            this.type = type;
            this.cataloger = cataloger;
            this.result = result;
        }

        public String getType() {
            return type;
        }

        public SbomCataloger<?> getCataloger() {
            return cataloger;
        }

        public SbomCatalogerProcessor.Result getResult() {
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SbomCatalogerOutcome that = (SbomCatalogerOutcome) o;
            return type.equals(that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type);
        }
    }
}
