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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.catalog.sbom.CyclonedxSbomCataloger
import org.jreleaser.gradle.plugin.dsl.catalog.sbom.Sbom
import org.jreleaser.gradle.plugin.dsl.catalog.sbom.SyftSbomCataloger
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.5.0
 */
@CompileStatic
class SbomImpl implements Sbom {
    final Property<Active> active
    final CyclonedxSbomCatalogerImpl cyclonedx
    final SyftSbomCatalogerImpl syft

    @Inject
    SbomImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        cyclonedx = objects.newInstance(CyclonedxSbomCatalogerImpl, objects)
        syft = objects.newInstance(SyftSbomCatalogerImpl, objects)
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void cyclonedx(Action<? super CyclonedxSbomCataloger> action) {
        action.execute(cyclonedx)
    }

    @Override
    void syft(Action<? super SyftSbomCataloger> action) {
        action.execute(syft)
    }

    @Override
    @CompileDynamic
    void cyclonedx(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CyclonedxSbomCataloger) Closure<Void> action) {
        ConfigureUtil.configure(action, cyclonedx)
    }

    @Override
    @CompileDynamic
    void syft(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SyftSbomCataloger) Closure<Void> action) {
        ConfigureUtil.configure(action, syft)
    }

    @CompileDynamic
    org.jreleaser.model.internal.catalog.sbom.Sbom toModel() {
        org.jreleaser.model.internal.catalog.sbom.Sbom sbom = new org.jreleaser.model.internal.catalog.sbom.Sbom()
        if (active.present) sbom.active = active.get()

        sbom.cyclonedx = cyclonedx.toModel()
        sbom.syft = syft.toModel()

        sbom
    }
}
