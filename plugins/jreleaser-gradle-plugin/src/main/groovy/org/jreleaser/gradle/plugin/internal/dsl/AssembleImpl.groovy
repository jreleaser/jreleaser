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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.Assemble
import org.jreleaser.model.Jlink
import org.jreleaser.model.NativeImage

import javax.inject.Inject
import java.util.stream.Collectors

/**
 *
 * @author Andres Almiray
 * @since 0.2.0
 */
@CompileStatic
class AssembleImpl implements Assemble {
    final Property<Boolean> enabled
    final NamedDomainObjectContainer<JlinkImpl> jlink
    final NamedDomainObjectContainer<NativeImageImpl> nativeImage

    @Inject
    AssembleImpl(ObjectFactory objects) {
        enabled = objects.property(Boolean).convention(true)

        jlink = objects.domainObjectContainer(JlinkImpl, new NamedDomainObjectFactory<JlinkImpl>() {
            @Override
            JlinkImpl create(String name) {
                JlinkImpl jlink = objects.newInstance(JlinkImpl, objects)
                jlink.name = name
                return jlink
            }
        })

        nativeImage = objects.domainObjectContainer(NativeImageImpl, new NamedDomainObjectFactory<NativeImageImpl>() {
            @Override
            NativeImageImpl create(String name) {
                NativeImageImpl nativeImage = objects.newInstance(NativeImageImpl, objects)
                nativeImage.name = name
                return nativeImage
            }
        })
    }

    @CompileDynamic
    org.jreleaser.model.Assemble toModel() {
        org.jreleaser.model.Assemble assemble = new org.jreleaser.model.Assemble()

        assemble.jlink = (jlink.toList().stream()
            .collect(Collectors.toMap(
                { JlinkImpl a -> a.name },
                { JlinkImpl a -> a.toModel() })) as Map<String, Jlink>)

        assemble.nativeImage = (nativeImage.toList().stream()
            .collect(Collectors.toMap(
                { NativeImageImpl a -> a.name },
                { NativeImageImpl a -> a.toModel() })) as Map<String, NativeImage>)

        assemble
    }
}
