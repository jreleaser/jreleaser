/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.Snap
import org.jreleaser.gradle.plugin.dsl.Tap
import org.jreleaser.model.Plug
import org.jreleaser.model.Slot
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class SnapImpl extends AbstractRepositoryTool implements Snap {
    final Property<String> packageName
    final Property<String> base
    final Property<String> grade
    final Property<String> confinement
    final RegularFileProperty exportedLogin
    final Property<Boolean> remoteBuild
    final TapImpl snap
    final CommitAuthorImpl commitAuthor
    final SetProperty<String> localPlugs
    final SetProperty<String> localSlots
    final NamedDomainObjectContainer<PlugImpl> plugs
    final NamedDomainObjectContainer<SlotImpl> slots

    @Inject
    SnapImpl(ObjectFactory objects) {
        super(objects)
        packageName = objects.property(String).convention(Providers.notDefined())
        base = objects.property(String).convention(Providers.notDefined())
        grade = objects.property(String).convention(Providers.notDefined())
        confinement = objects.property(String).convention(Providers.notDefined())
        exportedLogin = objects.fileProperty().convention(Providers.notDefined())
        remoteBuild = objects.property(Boolean).convention(Providers.notDefined())
        localPlugs = objects.setProperty(String).convention(Providers.notDefined())
        localSlots = objects.setProperty(String).convention(Providers.notDefined())
        snap = objects.newInstance(TapImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)

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
    void addLocalPlug(String plug) {
        if (isNotBlank(plug)) {
            localPlugs.add(plug.trim())
        }
    }

    @Override
    void addLocalSlot(String slot) {
        if (isNotBlank(slot)) {
            localSlots.add(slot.trim())
        }
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            packageName.present ||
            base.present ||
            grade.present ||
            confinement.present ||
            exportedLogin.present ||
            remoteBuild.present ||
            localPlugs.present ||
            localSlots.present ||
            plugs.size() ||
            slots.size() ||
            snap.isSet() ||
            commitAuthor.isSet()
    }

    @Override
    void snap(Action<? super Tap> action) {
        action.execute(snap)
    }

    @Override
    void commitAuthor(Action<? super CommitAuthor> action) {
        action.execute(commitAuthor)
    }

    @Override
    void snap(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Tap) Closure<Void> action) {
        ConfigureUtil.configure(action, snap)
    }

    @Override
    void commitAuthor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommitAuthor) Closure<Void> action) {
        ConfigureUtil.configure(action, commitAuthor)
    }

    org.jreleaser.model.Snap toModel() {
        org.jreleaser.model.Snap tool = new org.jreleaser.model.Snap()
        fillToolProperties(tool)
        fillTemplateToolProperties(tool)
        if (snap.isSet()) tool.snap = snap.toSnapTap()
        if (packageName.present) tool.packageName = packageName.get()
        if (base.present) tool.base = base.get()
        if (grade.present) tool.grade = grade.get()
        if (confinement.present) tool.confinement = confinement.get()
        if (snap.isSet()) tool.snap = snap.toSnapTap()
        if (commitAuthor.isSet()) tool.commitAuthor = commitAuthor.toModel()
        if (exportedLogin.present) {
            tool.exportedLogin = exportedLogin.get().asFile.absolutePath
        }
        tool.remoteBuild = remoteBuild.getOrElse(false)
        tool.localPlugs = (Set<String>) localPlugs.getOrElse([] as Set<String>)
        tool.localSlots = (Set<String>) localSlots.getOrElse([] as Set<String>)
        tool.plugs.addAll(plugs.collect([]) { PlugImpl plug ->
            plug.toModel()
        } as Set<Plug>)
        tool.slots.addAll(slots.collect([]) { SlotImpl slot ->
            slot.toModel()
        } as Set<Slot>)
        tool
    }
}
