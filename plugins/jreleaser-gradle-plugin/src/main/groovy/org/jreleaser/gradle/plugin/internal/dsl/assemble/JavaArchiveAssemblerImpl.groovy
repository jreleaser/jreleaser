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
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.assemble.JavaArchiveAssembler
import org.jreleaser.gradle.plugin.dsl.catalog.swid.SwidTag
import org.jreleaser.gradle.plugin.dsl.common.ArchiveOptions
import org.jreleaser.gradle.plugin.dsl.common.Artifact
import org.jreleaser.gradle.plugin.dsl.common.EnvironmentVariables
import org.jreleaser.gradle.plugin.dsl.common.Executable
import org.jreleaser.gradle.plugin.dsl.common.Glob
import org.jreleaser.gradle.plugin.dsl.common.JvmOptions
import org.jreleaser.gradle.plugin.internal.dsl.catalog.swid.SwidTagImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.ArchiveOptionsImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.ArtifactImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.EnvironmentVariablesImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.ExecutableImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.GlobImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.JvmOptionsImpl
import org.jreleaser.gradle.plugin.internal.dsl.platform.PlatformImpl
import org.jreleaser.model.Archive
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.4.0
 */
@CompileStatic
class JavaArchiveAssemblerImpl extends AbstractAssembler implements JavaArchiveAssembler {
    String name
    final Property<String> archiveName
    final SetProperty<Archive.Format> formats
    final JavaImpl java
    final ExecutableImpl executable
    final ArtifactImpl mainJar
    final PlatformImpl platform
    final ArchiveOptionsImpl options
    final SwidTagImpl swid

    private final NamedDomainObjectContainer<GlobImpl> jars

    @Inject
    JavaArchiveAssemblerImpl(ObjectFactory objects) {
        super(objects)
        archiveName = objects.property(String).convention(Providers.<String> notDefined())
        formats = objects.setProperty(Archive.Format).convention(Providers.<Set<Archive.Format>> notDefined())
        executable = objects.newInstance(ExecutableImpl, objects)
        java = objects.newInstance(JavaImpl, objects)
        mainJar = objects.newInstance(ArtifactImpl, objects)
        mainJar.setName('mainJar')
        platform = objects.newInstance(PlatformImpl, objects)
        options = objects.newInstance(ArchiveOptionsImpl, objects)
        swid = objects.newInstance(SwidTagImpl, objects)

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
            archiveName.present ||
            formats.present ||
            executable.isSet() ||
            java.isSet() ||
            mainJar.isSet() ||
            !jars.isEmpty() ||
            options.isSet()
    }

    @Override
    void format(String format) {
        if (isNotBlank(format)) {
            formats.add(Archive.Format.of(format))
        }
    }

    @Override
    void java(Action<? super org.jreleaser.gradle.plugin.dsl.assemble.JavaArchiveAssembler.Java> action) {
        action.execute(java)
    }

    @Override
    void mainJar(Action<? super Artifact> action) {
        action.execute(mainJar)
    }

    @Override
    void executable(Action<? super Executable> action) {
        action.execute(executable)
    }

    @Override
    void jars(Action<? super Glob> action) {
        action.execute(jars.maybeCreate("jars-${jars.size()}".toString()))
    }

    @Override
    void options(Action<? super ArchiveOptions> action) {
        action.execute(options)
    }

    @Override
    void swid(Action<? super SwidTag> action) {
        action.execute(swid)
    }

    @Override
    @CompileDynamic
    void java(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = JavaArchiveAssembler.Java) Closure<Void> action) {
        ConfigureUtil.configure(action, java)
    }

    @Override
    @CompileDynamic
    void mainJar(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, mainJar)
    }

    @Override
    @CompileDynamic
    void executable(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Executable) Closure<Void> action) {
        ConfigureUtil.configure(action, executable)
    }

    @Override
    @CompileDynamic
    void jars(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Glob) Closure<Void> action) {
        ConfigureUtil.configure(action, jars.maybeCreate("jars-${jars.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void options(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ArchiveOptions) Closure<Void> action) {
        ConfigureUtil.configure(action, options)
    }

    @Override
    @CompileDynamic
    void swid(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SwidTag) Closure<Void> action) {
        ConfigureUtil.configure(action, swid)
    }

    org.jreleaser.model.internal.assemble.JavaArchiveAssembler toModel() {
        org.jreleaser.model.internal.assemble.JavaArchiveAssembler assembler = new org.jreleaser.model.internal.assemble.JavaArchiveAssembler()
        assembler.name = name
        fillProperties(assembler)
        if (archiveName.present) assembler.archiveName = archiveName.get()
        assembler.formats = (Set<Archive.Format>) formats.getOrElse([] as Set<Archive.Format>)
        if (executable.isSet()) assembler.executable = executable.toModel()
        if (mainJar.isSet()) assembler.mainJar = mainJar.toModel()
        assembler.java = java.toModel()
        for (GlobImpl glob : jars) {
            assembler.addJar(glob.toModel())
        }
        if (options.isSet()) assembler.options = options.toModel()
        assembler.swid = swid.toModel()
        assembler
    }

    @CompileStatic
    static class JavaImpl implements JavaArchiveAssembler.Java {
        final Property<String> mainModule
        final Property<String> mainClass
        final SetProperty<String> options
        final JvmOptionsImpl jvmOptions
        final EnvironmentVariablesImpl environmentVariables

        @Inject
        JavaImpl(ObjectFactory objects) {
            mainModule = objects.property(String).convention(Providers.<String> notDefined())
            mainClass = objects.property(String).convention(Providers.<String> notDefined())
            options = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
            jvmOptions = objects.newInstance(JvmOptionsImpl, objects)
            environmentVariables = objects.newInstance(EnvironmentVariablesImpl, objects)
        }

        @Internal
        boolean isSet() {
            mainModule.present ||
                mainClass.present ||
                options.present ||
                jvmOptions.isSet() ||
                environmentVariables.isSet()
        }

        @Override
        void jvmOptions(Action<? super JvmOptions> action) {
            action.execute(jvmOptions)
        }

        @Override
        void environmentVariables(Action<? super EnvironmentVariables> action) {
            action.execute(environmentVariables)
        }

        @Override
        @CompileDynamic
        void jvmOptions(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = JvmOptions) Closure<Void> action) {
            ConfigureUtil.configure(action, jvmOptions)
        }

        @Override
        @CompileDynamic
        void environmentVariables(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = EnvironmentVariables) Closure<Void> action) {
            ConfigureUtil.configure(action, environmentVariables)
        }

        org.jreleaser.model.internal.assemble.JavaArchiveAssembler.Java toModel() {
            org.jreleaser.model.internal.assemble.JavaArchiveAssembler.Java java = new org.jreleaser.model.internal.assemble.JavaArchiveAssembler.Java()
            if (mainModule.present) java.mainModule = mainModule.get()
            if (mainClass.present) java.mainClass = mainClass.get()
            options.getOrElse([] as Set<String>).forEach { option -> jvmOptions.universal(option) }
            java.jvmOptions = jvmOptions.toModel()
            java.environmentVariables = environmentVariables.toModel()
            java
        }
    }
}
