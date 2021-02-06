/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.jreleaser.model.Plug
import org.jreleaser.model.Slot

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class SnapImpl extends AbstractTool implements org.jreleaser.gradle.plugin.dsl.Snap {
    final Property<String> base
    final Property<String> grade
    final Property<String> confinement
    final RegularFileProperty exportedLogin
    final ListProperty<String> localPlugs
    final NamedDomainObjectContainer<PlugImpl> plugs
    final NamedDomainObjectContainer<SlotImpl> slots

    @Inject
    SnapImpl(ObjectFactory objects, Provider<Directory> distributionsDirProvider) {
        super(objects, distributionsDirProvider)
        base = objects.property(String).convention(Providers.notDefined())
        grade = objects.property(String).convention(Providers.notDefined())
        confinement = objects.property(String).convention(Providers.notDefined())
        exportedLogin = objects.fileProperty().convention(Providers.notDefined())
        localPlugs = objects.listProperty(String).convention(Providers.notDefined())

        plugs = objects.domainObjectContainer(PlugImpl, new NamedDomainObjectFactory<PlugImpl>() {
            @Override
            PlugImpl create(String name) {
                PlugImpl plug = objects.newInstance(PlugImpl, objects)
                plug.name = name
                return plug
            }
        })

        slots = objects.domainObjectContainer(SlotImpl, new NamedDomainObjectFactory<SlotImpl>() {
            @Override
            SlotImpl create(String name) {
                SlotImpl slot = objects.newInstance(SlotImpl, objects)
                slot.name = name
                return slot
            }
        })
    }

    @Override
    protected String toolName() { 'snap' }

    @Override
    void addLocalPlug(String plug) {
        if (isNotBlank(plug)) {
            localPlugs.add(plug.trim())
        }
    }

    @Override
    @Internal
    boolean isSet() {
        return super.isSet() ||
            base.present ||
            grade.present ||
            confinement.present ||
            exportedLogin.present ||
            localPlugs.present ||
            plugs.size() ||
            slots.size()
    }

    org.jreleaser.model.Snap toModel() {
        org.jreleaser.model.Snap tool = new org.jreleaser.model.Snap()
        fillToolProperties(tool)
        tool.base = base.orNull
        tool.grade = grade.orNull
        tool.confinement = confinement.orNull
        if (exportedLogin.present) {
            tool.exportedLogin = exportedLogin.get().asFile.absolutePath
        }
        tool.localPlugs = (List<String>) localPlugs.getOrElse([])
        tool.plugs.addAll(plugs.collect([]) { PlugImpl plug ->
            plug.toModel()
        } as Set<Plug>)
        tool.slots.addAll(slots.collect([]) { SlotImpl slot ->
            slot.toModel()
        } as Set<Slot>)
        tool
    }
}
