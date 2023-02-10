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
package org.jreleaser.gradle.plugin.internal.dsl.catalog.sbom

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.catalog.sbom.SbomCataloger
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
abstract class AbstractSbomCataloger implements SbomCataloger {
    final Property<Active> active
    final Property<Boolean> distributions
    final Property<Boolean> files
    final MapProperty<String, Object> extraProperties

    @Inject
    AbstractSbomCataloger(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        distributions = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        files = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Internal
    boolean isSet() {
        active.present ||
            extraProperties.present ||
            distributions.present ||
            files.present
    }

    @Override
    void pack(Action<? super Pack> action) {
        action.execute(pack)
    }

    @Override
    void pack(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Pack) Closure<Void> action) {
        ConfigureUtil.configure(action, pack)
    }

    protected <C extends org.jreleaser.model.internal.catalog.sbom.SbomCataloger> void fillProperties(C cataloger) {
        if (active.present) cataloger.active = active.get()
        if (extraProperties.present) cataloger.extraProperties.putAll(extraProperties.get())
        if (distributions.present) cataloger.distributions = distributions.get()
        if (files.present) cataloger.files = files.get()
    }

    @CompileStatic
    static class PackImpl implements Pack {
        final Property<Boolean> enabled
        final Property<String> name

        @Inject
        PackImpl(ObjectFactory objects) {
            enabled = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            name = objects.property(String).convention(Providers.<String> notDefined())
        }

        boolean isSet() {
            enabled.present ||
                name.present
        }

        org.jreleaser.model.internal.catalog.sbom.SbomCataloger.Pack toModel() {
            org.jreleaser.model.internal.catalog.sbom.SbomCataloger.Pack pack = new org.jreleaser.model.internal.catalog.sbom.SbomCataloger.Pack()
            if (enabled.present) pack.enabled = enabled.get()
            if (name.present) pack.name = name.get()
            pack
        }
    }
}
