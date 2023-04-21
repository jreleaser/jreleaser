/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
package org.jreleaser.gradle.plugin.internal.dsl.catalog

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.catalog.Catalog
import org.jreleaser.gradle.plugin.dsl.catalog.SlsaCataloger
import org.jreleaser.gradle.plugin.dsl.catalog.sbom.Sbom
import org.jreleaser.gradle.plugin.internal.dsl.catalog.sbom.SbomImpl
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.2.0
 */
@CompileStatic
class CatalogImpl implements Catalog {
    final Property<Active> active
    final SbomImpl sbom
    final SlsaCatalogerImpl slsa

    @Inject
    CatalogImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        sbom = objects.newInstance(SbomImpl, objects)
        slsa = objects.newInstance(SlsaCatalogerImpl, objects)
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void sbom(Action<? super Sbom> action) {
        action.execute(sbom)
    }

    @Override
    void slsa(Action<? super SlsaCataloger> action) {
        action.execute(slsa)
    }

    @Override
    @CompileDynamic
    void sbom(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Sbom) Closure<Void> action) {
        ConfigureUtil.configure(action, sbom)
    }

    @Override
    @CompileDynamic
    void slsa(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SlsaCataloger) Closure<Void> action) {
        ConfigureUtil.configure(action, slsa)
    }

    org.jreleaser.model.internal.catalog.Catalog toModel() {
        org.jreleaser.model.internal.catalog.Catalog catalog = new org.jreleaser.model.internal.catalog.Catalog()
        if (active.present) catalog.active = active.get()
        catalog.sbom = sbom.toModel()
        catalog.slsa = slsa.toModel()
        catalog
    }
}
