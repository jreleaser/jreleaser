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
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.assemble.JavaAssembler
import org.jreleaser.gradle.plugin.dsl.common.Artifact
import org.jreleaser.gradle.plugin.dsl.common.Glob
import org.jreleaser.gradle.plugin.dsl.common.Java
import org.jreleaser.gradle.plugin.internal.dsl.common.ArtifactImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.GlobImpl
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.8.0
 */
@CompileStatic
abstract class AbstractJavaAssembler extends AbstractAssembler implements JavaAssembler {
    final Property<String> executable

    private final ArtifactImpl mainJar
    private final NamedDomainObjectContainer<GlobImpl> jars

    @Inject
    AbstractJavaAssembler(ObjectFactory objects) {
        super(objects)
        executable = objects.property(String).convention(Providers.<String> notDefined())
        mainJar = objects.newInstance(ArtifactImpl, objects)
        mainJar.setName('mainJar')

        jars = objects.domainObjectContainer(GlobImpl, new NamedDomainObjectFactory<GlobImpl>() {
            @Override
            GlobImpl create(String name) {
                GlobImpl glob = objects.newInstance(GlobImpl, objects)
                glob.name = name
                glob
            }
        })
    }

    @Internal
    boolean isSet() {
        super.isSet() ||
            executable.present ||
            mainJar.isSet() ||
            !jars.isEmpty()
    }

    @Override
    void java(Action<? super Java> action) {
        action.execute(java)
    }

    @Override
    void mainJar(Action<? super Artifact> action) {
        action.execute(mainJar)
    }

    @Override
    void jars(Action<? super Glob> action) {
        action.execute(jars.maybeCreate("jars-${jars.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void java(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Java) Closure<Void> action) {
        ConfigureUtil.configure(action, java)
    }

    @Override
    @CompileDynamic
    void mainJar(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, mainJar)
    }

    @Override
    @CompileDynamic
    void jars(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Glob) Closure<Void> action) {
        ConfigureUtil.configure(action, jars.maybeCreate("jars-${jars.size()}".toString()))
    }

    protected <A extends org.jreleaser.model.internal.assemble.JavaAssembler> void fillProperties(A assembler) {
        super.fillProperties(assembler)
        if (mainJar.isSet()) assembler.mainJar = mainJar.toModel()
        for (GlobImpl glob : jars) {
            assembler.addJar(glob.toModel())
        }
        if (executable.present) assembler.executable = executable.get()
    }
}
