/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.Archive
import org.jreleaser.gradle.plugin.dsl.Assemble
import org.jreleaser.gradle.plugin.dsl.Jlink
import org.jreleaser.gradle.plugin.dsl.Jpackage
import org.jreleaser.gradle.plugin.dsl.NativeImage
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
class AssembleImpl implements Assemble {
    final Property<Boolean> enabled
    final NamedDomainObjectContainer<Archive> archive
    final NamedDomainObjectContainer<Jlink> jlink
    final NamedDomainObjectContainer<Jpackage> jpackage
    final NamedDomainObjectContainer<NativeImage> nativeImage

    @Inject
    AssembleImpl(ObjectFactory objects) {
        enabled = objects.property(Boolean).convention(true)

        archive = objects.domainObjectContainer(Archive, new NamedDomainObjectFactory<Archive>() {
            @Override
            Archive create(String name) {
                ArchiveImpl archive = objects.newInstance(ArchiveImpl, objects)
                archive.name = name
                archive
            }
        })

        jlink = objects.domainObjectContainer(Jlink, new NamedDomainObjectFactory<Jlink>() {
            @Override
            Jlink create(String name) {
                JlinkImpl jlink = objects.newInstance(JlinkImpl, objects)
                jlink.name = name
                jlink
            }
        })

        jpackage = objects.domainObjectContainer(Jpackage, new NamedDomainObjectFactory<Jpackage>() {
            @Override
            Jpackage create(String name) {
                JpackageImpl jpackage = objects.newInstance(JpackageImpl, objects)
                jpackage.name = name
                jpackage
            }
        })

        nativeImage = objects.domainObjectContainer(NativeImage, new NamedDomainObjectFactory<NativeImage>() {
            @Override
            NativeImage create(String name) {
                NativeImageImpl nativeImage = objects.newInstance(NativeImageImpl, objects)
                nativeImage.name = name
                nativeImage
            }
        })
    }

    @Override
    void archive(Action<? super NamedDomainObjectContainer<Archive>> action) {
        action.execute(archive)
    }

    @Override
    void jlink(Action<? super NamedDomainObjectContainer<Jlink>> action) {
        action.execute(jlink)
    }

    @Override
    void jpackage(Action<? super NamedDomainObjectContainer<Jpackage>> action) {
        action.execute(jpackage)
    }

    @Override
    void nativeImage(Action<? super NamedDomainObjectContainer<NativeImage>> action) {
        action.execute(nativeImage)
    }

    @Override
    void archive(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, archive)
    }

    @Override
    void jlink(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, jlink)
    }

    @Override
    void jpackage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, jpackage)
    }

    @Override
    void nativeImage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, nativeImage)
    }

    @CompileDynamic
    org.jreleaser.model.Assemble toModel() {
        org.jreleaser.model.Assemble assemble = new org.jreleaser.model.Assemble()

        archive.each { assemble.addArchive(((ArchiveImpl) it).toModel()) }
        jlink.each { assemble.addJlink(((JlinkImpl) it).toModel()) }
        jpackage.each { assemble.addJpackage(((JpackageImpl) it).toModel()) }
        nativeImage.each { assemble.addNativeImage(((NativeImageImpl) it).toModel()) }

        assemble
    }
}
