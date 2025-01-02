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
package org.jreleaser.gradle.plugin.internal.dsl.catalog.sbom

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.catalog.sbom.CyclonedxSbomCataloger

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.6.0
 */
@CompileStatic
class CyclonedxSbomCatalogerImpl extends AbstractSbomCataloger implements CyclonedxSbomCataloger {
    final Property<String> version
    final SetProperty<org.jreleaser.model.api.catalog.sbom.CyclonedxSbomCataloger.Format> formats
    final PackImpl pack

    @Inject
    CyclonedxSbomCatalogerImpl(ObjectFactory objects) {
        super(objects)
        version = objects.property(String).convention(Providers.<String> notDefined())
        formats = objects.setProperty(org.jreleaser.model.api.catalog.sbom.CyclonedxSbomCataloger.Format)
            .convention(Providers.<Set<org.jreleaser.model.api.catalog.sbom.CyclonedxSbomCataloger.Format>> notDefined())
        pack = objects.newInstance(PackImpl, objects)
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            version.present ||
            formats.present ||
            pack.isSet()
    }

    @Override
    void format(String str) {
        if (isNotBlank(str)) {
            this.formats.add(org.jreleaser.model.api.catalog.sbom.CyclonedxSbomCataloger.Format.of(str.trim()))
        }
    }

    org.jreleaser.model.internal.catalog.sbom.CyclonedxSbomCataloger toModel() {
        org.jreleaser.model.internal.catalog.sbom.CyclonedxSbomCataloger cataloger = new org.jreleaser.model.internal.catalog.sbom.CyclonedxSbomCataloger()
        fillProperties(cataloger)
        if (version.present) cataloger.version = version.get()
        cataloger.pack = pack.toModel()
        cataloger.formats = (Set<org.jreleaser.model.api.catalog.sbom.CyclonedxSbomCataloger.Format>) formats.getOrElse([] as Set<org.jreleaser.model.api.catalog.sbom.CyclonedxSbomCataloger.Format>)
        cataloger
    }
}
