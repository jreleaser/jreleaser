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
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.jreleaser.gradle.plugin.dsl.Artifact
import org.jreleaser.gradle.plugin.dsl.Glob
import org.jreleaser.gradle.plugin.dsl.Java
import org.jreleaser.gradle.plugin.dsl.NativeImage
import org.jreleaser.model.Active

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class NativeImageImpl extends AbstractAssembler implements NativeImage {
    String name
    final ListProperty<String> args
    final JavaImpl java

    private final ArtifactImpl graal
    private final ArtifactImpl mainJar
    private final NamedDomainObjectContainer<GlobImpl> jars

    @Inject
    NativeImageImpl(ObjectFactory objects) {
        super(objects)

        args = objects.listProperty(String).convention(Providers.notDefined())
        java = objects.newInstance(JavaImpl, objects)
        graal = objects.newInstance(ArtifactImpl, objects)
        mainJar = objects.newInstance(ArtifactImpl, objects)
        jars = objects.domainObjectContainer(GlobImpl, new NamedDomainObjectFactory<GlobImpl>() {
            @Override
            GlobImpl create(String name) {
                GlobImpl glob = objects.newInstance(GlobImpl, objects)
                glob.name = name
                glob
            }
        })
    }

    void setName(String name) {
        this.name = name
    }

    @Override
    void addArg(String arg) {
        if (isNotBlank(arg)) {
            args.add(arg.trim())
        }
    }

    @Override
    void graal(Action<? super Artifact> action) {
        action.execute(graal)
    }

    @Override
    void mainJar(Action<? super Artifact> action) {
        action.execute(mainJar)
    }

    @Override
    void java(Action<? super Java> action) {
        action.execute(java)
    }

    @Override
    void jars(Action<? super Glob> action) {
        GlobImpl glob = jars.maybeCreate("jars-${jars.size()}".toString())
        action.execute(glob)
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    org.jreleaser.model.NativeImage toModel() {
        org.jreleaser.model.NativeImage nativeImage = new org.jreleaser.model.NativeImage()
        nativeImage.name = name
        fillProperties(nativeImage)
        nativeImage.java = java.toModel()
        nativeImage.args = (List<String>) args.getOrElse([])
        nativeImage.graal = graal.toModel()
        nativeImage.mainJar = mainJar.toModel()
        for (GlobImpl glob : jars) {
            nativeImage.addJar(glob.toModel())
        }
        nativeImage
    }
}
