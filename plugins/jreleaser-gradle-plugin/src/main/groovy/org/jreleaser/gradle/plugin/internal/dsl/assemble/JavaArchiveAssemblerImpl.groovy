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
package org.jreleaser.gradle.plugin.internal.dsl.assemble

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.assemble.JavaArchiveAssembler
import org.jreleaser.gradle.plugin.dsl.common.Artifact
import org.jreleaser.gradle.plugin.dsl.common.Executable
import org.jreleaser.gradle.plugin.dsl.common.Glob
import org.jreleaser.gradle.plugin.internal.dsl.common.ArtifactImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.ExecutableImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.GlobImpl
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
    final DirectoryProperty templateDirectory
    final JavaImpl java
    final ExecutableImpl executable
    final ArtifactImpl mainJar
    final PlatformImpl platform

    private final NamedDomainObjectContainer<GlobImpl> jars
    private final NamedDomainObjectContainer<GlobImpl> files

    @Inject
    JavaArchiveAssemblerImpl(ObjectFactory objects) {
        super(objects)
        archiveName = objects.property(String).convention(Providers.<String> notDefined())
        formats = objects.setProperty(Archive.Format).convention(Providers.<Set<Archive.Format>> notDefined())
        templateDirectory = objects.directoryProperty().convention(Providers.notDefined())
        executable = objects.newInstance(ExecutableImpl, objects)
        java = objects.newInstance(JavaImpl, objects)
        mainJar = objects.newInstance(ArtifactImpl, objects)
        mainJar.setName('mainJar')
        platform = objects.newInstance(PlatformImpl, objects)

        jars = objects.domainObjectContainer(GlobImpl, new NamedDomainObjectFactory<GlobImpl>() {
            @Override
            GlobImpl create(String name) {
                GlobImpl glob = objects.newInstance(GlobImpl, objects)
                glob.name = name
                glob
            }
        })

        files = objects.domainObjectContainer(GlobImpl, new NamedDomainObjectFactory<GlobImpl>() {
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
            templateDirectory.present ||
            !jars.isEmpty() ||
            !files.isEmpty()
    }

    @Override
    void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory.set(new File(templateDirectory))
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
    void files(Action<? super Glob> action) {
        action.execute(files.maybeCreate("files-${files.size()}".toString()))
    }

    @Override
    void java(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = org.jreleaser.gradle.plugin.dsl.assemble.JavaArchiveAssembler.Java) Closure<Void> action) {
        ConfigureUtil.configure(action, java)
    }

    @Override
    void mainJar(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, mainJar)
    }

    @Override
    void executable(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Executable) Closure<Void> action) {
        ConfigureUtil.configure(action, executable)
    }

    @Override
    void jars(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Glob) Closure<Void> action) {
        ConfigureUtil.configure(action, jars.maybeCreate("jars-${jars.size()}".toString()))
    }

    @Override
    void files(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Glob) Closure<Void> action) {
        ConfigureUtil.configure(action, files.maybeCreate("files-${files.size()}".toString()))
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
        for (GlobImpl glob : files) {
            assembler.addFile(glob.toModel())
        }
        if (templateDirectory.present) {
            assembler.templateDirectory = templateDirectory.get().asFile.toPath().toString()
        }
        assembler
    }

    @CompileStatic
    static class JavaImpl implements JavaArchiveAssembler.Java {
        final Property<String> mainModule
        final Property<String> mainClass
        final ListProperty<String> options

        @Inject
        JavaImpl(ObjectFactory objects) {
            mainModule = objects.property(String).convention(Providers.<String> notDefined())
            mainClass = objects.property(String).convention(Providers.<String> notDefined())
            options = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        }

        @Internal
        boolean isSet() {
            mainModule.present ||
                mainClass.present ||
                options.present
        }

        org.jreleaser.model.internal.assemble.JavaArchiveAssembler.Java toModel() {
            org.jreleaser.model.internal.assemble.JavaArchiveAssembler.Java java = new org.jreleaser.model.internal.assemble.JavaArchiveAssembler.Java()
            if (mainModule.present) java.mainModule = mainModule.get()
            if (mainClass.present) java.mainClass = mainClass.get()
            java.options = (List<String>) options.getOrElse([] as List<String>)
            java
        }
    }
}
