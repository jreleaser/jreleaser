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
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.assemble.ArchiveAssembler
import org.jreleaser.gradle.plugin.dsl.assemble.Assemble
import org.jreleaser.gradle.plugin.dsl.assemble.DebAssembler
import org.jreleaser.gradle.plugin.dsl.assemble.JavaArchiveAssembler
import org.jreleaser.gradle.plugin.dsl.assemble.JlinkAssembler
import org.jreleaser.gradle.plugin.dsl.assemble.JpackageAssembler
import org.jreleaser.gradle.plugin.dsl.assemble.NativeImageAssembler
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
class AssembleImpl implements Assemble {
    final Property<Active> active
    final NamedDomainObjectContainer<ArchiveAssembler> archive
    final NamedDomainObjectContainer<DebAssembler> deb
    final NamedDomainObjectContainer<JavaArchiveAssembler> javaArchive
    final NamedDomainObjectContainer<JlinkAssembler> jlink
    final NamedDomainObjectContainer<JpackageAssembler> jpackage
    final NamedDomainObjectContainer<NativeImageAssembler> nativeImage

    @Inject
    AssembleImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())

        archive = objects.domainObjectContainer(ArchiveAssembler, new NamedDomainObjectFactory<ArchiveAssembler>() {
            @Override
            ArchiveAssembler create(String name) {
                ArchiveAssemblerImpl archive = objects.newInstance(ArchiveAssemblerImpl, objects)
                archive.name = name
                archive
            }
        })

        deb = objects.domainObjectContainer(DebAssembler, new NamedDomainObjectFactory<DebAssembler>() {
            @Override
            DebAssembler create(String name) {
                DebAssemblerImpl deb = objects.newInstance(DebAssemblerImpl, objects)
                deb.name = name
                deb
            }
        })

        javaArchive = objects.domainObjectContainer(JavaArchiveAssembler, new NamedDomainObjectFactory<JavaArchiveAssembler>() {
            @Override
            JavaArchiveAssembler create(String name) {
                JavaArchiveAssemblerImpl archive = objects.newInstance(JavaArchiveAssemblerImpl, objects)
                archive.name = name
                archive
            }
        })

        jlink = objects.domainObjectContainer(JlinkAssembler, new NamedDomainObjectFactory<JlinkAssembler>() {
            @Override
            JlinkAssembler create(String name) {
                JlinkAssemblerImpl jlink = objects.newInstance(JlinkAssemblerImpl, objects)
                jlink.name = name
                jlink
            }
        })

        jpackage = objects.domainObjectContainer(JpackageAssembler, new NamedDomainObjectFactory<JpackageAssembler>() {
            @Override
            JpackageAssembler create(String name) {
                JpackageAssemblerImpl jpackage = objects.newInstance(JpackageAssemblerImpl, objects)
                jpackage.name = name
                jpackage
            }
        })

        nativeImage = objects.domainObjectContainer(NativeImageAssembler, new NamedDomainObjectFactory<NativeImageAssembler>() {
            @Override
            NativeImageAssembler create(String name) {
                NativeImageAssemblerImpl nativeImage = objects.newInstance(NativeImageAssemblerImpl, objects)
                nativeImage.name = name
                nativeImage
            }
        })
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void archive(Action<? super NamedDomainObjectContainer<ArchiveAssembler>> action) {
        action.execute(archive)
    }

    @Override
    void deb(Action<? super NamedDomainObjectContainer<DebAssembler>> action) {
        action.execute(deb)
    }

    @Override
    void javaArchive(Action<? super NamedDomainObjectContainer<JavaArchiveAssembler>> action) {
        action.execute(javaArchive)
    }

    @Override
    void jlink(Action<? super NamedDomainObjectContainer<JlinkAssembler>> action) {
        action.execute(jlink)
    }

    @Override
    void jpackage(Action<? super NamedDomainObjectContainer<JpackageAssembler>> action) {
        action.execute(jpackage)
    }

    @Override
    void nativeImage(Action<? super NamedDomainObjectContainer<NativeImageAssembler>> action) {
        action.execute(nativeImage)
    }

    @Override
    @CompileDynamic
    void archive(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, archive)
    }

    @Override
    @CompileDynamic
    void deb(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, deb)
    }

    @Override
    @CompileDynamic
    void javaArchive(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, javaArchive)
    }

    @Override
    @CompileDynamic
    void jlink(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, jlink)
    }

    @Override
    @CompileDynamic
    void jpackage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, jpackage)
    }

    @Override
    @CompileDynamic
    void nativeImage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, nativeImage)
    }

    @CompileDynamic
    org.jreleaser.model.internal.assemble.Assemble toModel() {
        org.jreleaser.model.internal.assemble.Assemble assemble = new org.jreleaser.model.internal.assemble.Assemble()
        if (active.present) assemble.active = active.get()

        archive.each { assemble.addArchive(((ArchiveAssemblerImpl) it).toModel()) }
        deb.each { assemble.addDeb(((DebAssemblerImpl) it).toModel()) }
        javaArchive.each { assemble.addJavaArchive(((JavaArchiveAssemblerImpl) it).toModel()) }
        jlink.each { assemble.addJlink(((JlinkAssemblerImpl) it).toModel()) }
        jpackage.each { assemble.addJpackage(((JpackageAssemblerImpl) it).toModel()) }
        nativeImage.each { assemble.addNativeImage(((NativeImageAssemblerImpl) it).toModel()) }

        assemble
    }
}
