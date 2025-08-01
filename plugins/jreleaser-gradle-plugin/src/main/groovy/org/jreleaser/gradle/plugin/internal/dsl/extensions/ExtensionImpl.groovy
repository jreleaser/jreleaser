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
package org.jreleaser.gradle.plugin.internal.dsl.extensions


import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.extensions.Extension
import org.jreleaser.gradle.plugin.dsl.tools.Jbang
import org.jreleaser.gradle.plugin.internal.dsl.tools.JbangImpl

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 1.3.0
 */
@CompileStatic
class ExtensionImpl implements Extension {
    String name
    final Property<Boolean> enabled
    final Property<String> gav
    final DirectoryProperty directory
    final NamedDomainObjectContainer<ProviderImpl> providers
    final JbangImpl jbang

    @Inject
    ExtensionImpl(ObjectFactory objects) {
        enabled = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        gav = objects.property(String).convention(Providers.<String> notDefined())
        directory = objects.directoryProperty().convention(Providers.notDefined())
        jbang = objects.newInstance(JbangImpl, objects)

        providers = objects.domainObjectContainer(ProviderImpl, new NamedDomainObjectFactory<ProviderImpl>() {
            @Override
            ProviderImpl create(String name) {
                ProviderImpl provider = objects.newInstance(ProviderImpl, objects)
                provider.name = name
                return provider
            }
        })
    }

    @Override
    void provider(Action<? super Provider> action) {
        action.execute(providers.maybeCreate("provider-${providers.size()}".toString()))
    }

    @Override
    void jbang(Action<? super Jbang> action) {
        action.execute(jbang)
    }

    org.jreleaser.model.internal.extensions.Extension toModel() {
        org.jreleaser.model.internal.extensions.Extension extension = new org.jreleaser.model.internal.extensions.Extension()
        extension.name = name
        if (enabled.present) extension.enabled = enabled.get()
        if (gav.present) extension.gav = gav.get()
        extension.jbang = jbang.toModel()
        if (directory.present) {
            extension.directory = directory.get().asFile.toPath().toAbsolutePath().toString()
        }
        for (ProviderImpl provider : providers) {
            extension.addProvider(provider.toModel())
        }
        extension
    }

    static class ProviderImpl implements Provider {
        String name
        final Property<String> type
        final MapProperty<String, Object> properties

        @Inject
        ProviderImpl(ObjectFactory objects) {
            type = objects.property(String).convention(Providers.<String> notDefined())
            properties = objects.mapProperty(String, Object).convention(Providers.notDefined())
        }

        org.jreleaser.model.internal.extensions.Extension.Provider toModel() {
            org.jreleaser.model.internal.extensions.Extension.Provider provider = new org.jreleaser.model.internal.extensions.Extension.Provider()
            if (type.present) provider.type = type.get()
            if (properties.present) provider.properties.putAll(properties.get())
            provider
        }
    }
}
