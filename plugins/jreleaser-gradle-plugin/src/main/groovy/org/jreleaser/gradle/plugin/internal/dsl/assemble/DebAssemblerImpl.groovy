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
package org.jreleaser.gradle.plugin.internal.dsl.assemble

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.assemble.DebAssembler
import org.jreleaser.gradle.plugin.internal.dsl.platform.PlatformImpl
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.4.0
 */
@CompileStatic
class DebAssemblerImpl extends AbstractAssembler implements DebAssembler {
    String name
    final Property<String> executable
    final Property<String> installationPath
    final Property<String> architecture
    final Property<String> assemblerRef
    final ControlImpl control
    final PlatformImpl platform

    @Inject
    DebAssemblerImpl(ObjectFactory objects) {
        super(objects)
        executable = objects.property(String).convention(Providers.<String> notDefined())
        installationPath = objects.property(String).convention(Providers.<String> notDefined())
        architecture = objects.property(String).convention(Providers.<String> notDefined())
        assemblerRef = objects.property(String).convention(Providers.<String> notDefined())
        control = objects.newInstance(ControlImpl, objects)
        platform = objects.newInstance(PlatformImpl, objects)
    }

    @Internal
    boolean isSet() {
        super.isSet() ||
            executable.present ||
            installationPath.present ||
            architecture.present ||
            assemblerRef.present ||
            control.isSet()
    }

    @Override
    void control(Action<? super DebAssembler.Control> action) {
        action.execute(control)
    }

    @Override
    @CompileDynamic
    void control(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DebAssembler.Control) Closure<Void> action) {
        ConfigureUtil.configure(action, control)
    }

    org.jreleaser.model.internal.assemble.DebAssembler toModel() {
        org.jreleaser.model.internal.assemble.DebAssembler assembler = new org.jreleaser.model.internal.assemble.DebAssembler()
        assembler.name = name
        fillProperties(assembler)
        if (executable.present) assembler.executable = executable.get()
        if (installationPath.present) assembler.installationPath = installationPath.get()
        if (architecture.present) assembler.architecture = architecture.get()
        if (assemblerRef.present) assembler.assemblerRef = assemblerRef.get()
        if (control.isSet()) assembler.control = control.toModel()
        assembler.platform = platform.toModel()
        assembler
    }

    @CompileStatic
    static class ControlImpl implements DebAssembler.Control {
        final Property<String> packageName
        final Property<String> packageVersion
        final Property<Integer> packageRevision
        final Property<String> provides
        final Property<String> maintainer
        final Property<org.jreleaser.model.api.assemble.DebAssembler.Section> section
        final Property<org.jreleaser.model.api.assemble.DebAssembler.Priority> priority
        final Property<Boolean> essential
        final Property<String> description
        final Property<String> homepage
        final Property<String> builtUsing
        final SetProperty<String> depends
        final SetProperty<String> preDepends
        final SetProperty<String> recommends
        final SetProperty<String> suggests
        final SetProperty<String> enhances
        final SetProperty<String> breaks
        final SetProperty<String> conflicts

        @Inject
        ControlImpl(ObjectFactory objects) {
            packageName = objects.property(String).convention(Providers.<String> notDefined())
            packageVersion = objects.property(String).convention(Providers.<String> notDefined())
            packageRevision = objects.property(Integer).convention(Providers.<Integer> notDefined())
            provides = objects.property(String).convention(Providers.<String> notDefined())
            maintainer = objects.property(String).convention(Providers.<String> notDefined())
            section = objects.property(org.jreleaser.model.api.assemble.DebAssembler.Section).convention(Providers.<org.jreleaser.model.api.assemble.DebAssembler.Section> notDefined())
            priority = objects.property(org.jreleaser.model.api.assemble.DebAssembler.Priority).convention(Providers.<org.jreleaser.model.api.assemble.DebAssembler.Priority> notDefined())
            essential = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            description = objects.property(String).convention(Providers.<String> notDefined())
            homepage = objects.property(String).convention(Providers.<String> notDefined())
            builtUsing = objects.property(String).convention(Providers.<String> notDefined())
            depends = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
            preDepends = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
            recommends = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
            suggests = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
            enhances = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
            breaks = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
            conflicts = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        }

        @Internal
        boolean isSet() {
            packageName.present ||
                packageVersion.present ||
                packageRevision.present ||
                provides.present ||
                maintainer.present ||
                section.present ||
                priority.present ||
                essential.present ||
                description.present ||
                homepage.present ||
                builtUsing.present ||
                depends.present ||
                preDepends.present ||
                recommends.present ||
                suggests.present ||
                enhances.present ||
                breaks.present ||
                conflicts.present
        }

        @Override
        void setSection(String str) {
            if (isNotBlank(str)) {
                section.set(org.jreleaser.model.api.assemble.DebAssembler.Section.of(str.trim()))
            }
        }

        @Override
        void setPriority(String str) {
            if (isNotBlank(str)) {
                priority.set(org.jreleaser.model.api.assemble.DebAssembler.Priority.of(str.trim()))
            }
        }

        org.jreleaser.model.internal.assemble.DebAssembler.Control toModel() {
            org.jreleaser.model.internal.assemble.DebAssembler.Control control = new org.jreleaser.model.internal.assemble.DebAssembler.Control()

            if (packageName.present) control.packageName = packageName.get()
            if (packageVersion.present) control.packageVersion = packageVersion.get()
            if (packageRevision.present) control.packageRevision = packageRevision.get()
            if (provides.present) control.provides = provides.get()
            if (maintainer.present) control.maintainer = maintainer.get()
            if (section.present) control.section = section.get()
            if (priority.present) control.priority = priority.get()
            if (essential.present) control.essential = essential.get()
            if (description.present) control.description = description.get()
            if (homepage.present) control.homepage = homepage.get()
            if (builtUsing.present) control.builtUsing = builtUsing.get()

            control.depends = (Set<String>) depends.getOrElse([] as Set)
            control.preDepends = (Set<String>) preDepends.getOrElse([] as Set)
            control.recommends = (Set<String>) recommends.getOrElse([] as Set)
            control.suggests = (Set<String>) suggests.getOrElse([] as Set)
            control.enhances = (Set<String>) enhances.getOrElse([] as Set)
            control.breaks = (Set<String>) breaks.getOrElse([] as Set)
            control.conflicts = (Set<String>) conflicts.getOrElse([] as Set)

            control
        }
    }
}
