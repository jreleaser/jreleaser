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
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.catalog.sbom.SbomCatalogerProcessor;
import org.jreleaser.model.spi.catalog.sbom.SbomCatalogerProcessorFactory;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class ProjectSbomCatalogers {
    private ProjectSbomCatalogers() {
        // noop
    }

    public static <A extends org.jreleaser.model.api.catalog.sbom.SbomCataloger, C extends org.jreleaser.model.internal.catalog.sbom.SbomCataloger<A>> SbomCatalogerProcessor<A, C> findSbomCataloger(JReleaserContext context, C cataloger) {
        Map<String, SbomCatalogerProcessor<?, ?>> catalogers = StreamSupport.stream(ServiceLoader.load(SbomCatalogerProcessorFactory.class,
                ProjectSbomCatalogers.class.getClassLoader()).spliterator(), false)
            .collect(Collectors.toMap(SbomCatalogerProcessorFactory::getName, factory -> factory.getSbomCataloger(context)));

        if (catalogers.containsKey(cataloger.getType())) {
            SbomCatalogerProcessor sbomCataloger = catalogers.get(cataloger.getType());
            sbomCataloger.setCataloger(cataloger);
            return sbomCataloger;
        }

        throw new JReleaserException(RB.$("ERROR_unsupported_cataloger", cataloger.getType()));
    }
}
