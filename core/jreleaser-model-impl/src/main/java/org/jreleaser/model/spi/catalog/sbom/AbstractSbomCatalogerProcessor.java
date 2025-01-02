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
import org.jreleaser.model.internal.common.Artifact;

import java.util.Set;

import static org.jreleaser.model.spi.catalog.sbom.SbomCatalogerProcessorHelper.collectArtifacts;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public abstract class AbstractSbomCatalogerProcessor<A extends org.jreleaser.model.api.catalog.sbom.SbomCataloger,
    C extends org.jreleaser.model.internal.catalog.sbom.SbomCataloger<A>> implements SbomCatalogerProcessor<A, C> {
    protected final JReleaserContext context;

    protected AbstractSbomCatalogerProcessor(JReleaserContext context) {
        this.context = context;
    }

    protected Set<Artifact> collectArtifactsSelf(JReleaserContext context) {
        return collectArtifacts(context, getCataloger());
    }
}
