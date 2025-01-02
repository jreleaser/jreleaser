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
package org.jreleaser.model.spi.catalog.sbom;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.catalog.sbom.SbomCataloger;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.util.Artifacts;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.JAR;
import static org.jreleaser.util.FileType.TAR;
import static org.jreleaser.util.FileType.TAR_BZ2;
import static org.jreleaser.util.FileType.TAR_GZ;
import static org.jreleaser.util.FileType.TAR_XZ;
import static org.jreleaser.util.FileType.TBZ2;
import static org.jreleaser.util.FileType.TGZ;
import static org.jreleaser.util.FileType.TXZ;
import static org.jreleaser.util.FileType.ZIP;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public abstract class SbomCatalogerProcessorHelper {
    private static final Set<String> SUPPORTED_EXTENSIONS = setOf(
        ZIP.extension(),
        JAR.extension(),
        TAR.extension(),
        TAR_BZ2.extension(),
        TAR_GZ.extension(),
        TAR_XZ.extension(),
        TBZ2.extension(),
        TGZ.extension(),
        TXZ.extension());

    private SbomCatalogerProcessorHelper() {
        // noop
    }

    public static Set<Artifact> resolveArtifacts(JReleaserContext context) {
        Set<Artifact> artifacts = new LinkedHashSet<>();

        List<? extends SbomCataloger<?>> catalogers = context.getModel().getCatalog().getSbom().findAllActiveSbomCatalogers();
        for (SbomCataloger<?> cataloger : catalogers) {
            artifacts.addAll(resolveArtifacts(context, cataloger));
        }

        return artifacts;
    }

    public static Set<Artifact> resolveArtifacts(JReleaserContext context, SbomCataloger<?> cataloger) {
        Set<Artifact> candidates = collectArtifacts(context, cataloger);
        return cataloger.resolveArtifacts(context, candidates);
    }

    public static Set<Artifact> collectArtifacts(JReleaserContext context) {
        Set<Artifact> artifacts = new LinkedHashSet<>();

        List<? extends SbomCataloger<?>> catalogers = context.getModel().getCatalog().getSbom().findAllActiveSbomCatalogers();
        for (SbomCataloger<?> cataloger : catalogers) {
            artifacts.addAll(collectArtifacts(context, cataloger));
        }

        return artifacts;
    }

    public static Set<Artifact> collectArtifacts(JReleaserContext context, SbomCataloger<?> cataloger) {
        Set<Artifact> artifacts = new LinkedHashSet<>();

        if (cataloger.isFiles()) {
            for (Artifact artifact : Artifacts.resolveFiles(context)) {
                if (!isArtifactSupported(artifact) ||
                    cataloger.isSkipped(artifact) ||
                    !artifact.isActiveAndSelected() ||
                    artifact.isOptional(context) && !artifact.resolvedPathExists()) {
                    continue;
                }
                artifacts.add(artifact);
            }
        }

        for (Distribution distribution : context.getModel().getActiveDistributions()) {
            if (cataloger.isSkipped(distribution)) continue;

            for (Artifact artifact : distribution.getArtifacts()) {
                if (!artifact.isActiveAndSelected() || cataloger.isSkipped(artifact)) continue;
                // resolve
                artifact.getEffectivePath(context, distribution);
                if (!isArtifactSupported(artifact) ||
                    artifact.isOptional(context) && !artifact.resolvedPathExists()) {
                    continue;
                }
                artifacts.add(artifact);
            }
        }

        return artifacts;
    }

    private static boolean isArtifactSupported(Artifact artifact) {
        String filename = artifact.getEffectivePath().getFileName().toString();
        for (String extension : SUPPORTED_EXTENSIONS) {
            if (filename.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}
