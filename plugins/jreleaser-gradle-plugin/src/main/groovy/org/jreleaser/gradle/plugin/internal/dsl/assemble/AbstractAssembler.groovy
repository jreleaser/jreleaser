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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.assemble.Assembler
import org.jreleaser.gradle.plugin.dsl.common.Artifact
import org.jreleaser.gradle.plugin.dsl.common.FileSet
import org.jreleaser.gradle.plugin.dsl.common.Glob
import org.jreleaser.gradle.plugin.dsl.platform.Platform
import org.jreleaser.gradle.plugin.internal.dsl.common.ArtifactImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.FileSetImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.GlobImpl
import org.jreleaser.model.Active
import org.jreleaser.model.Stereotype
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class AbstractAssembler implements Assembler {
    final Property<Boolean> exported
    final Property<Active> active
    final Property<Stereotype> stereotype
    final MapProperty<String, Object> extraProperties
    final DirectoryProperty templateDirectory
    final SetProperty<String> skipTemplates
    private final NamedDomainObjectContainer<FileSetImpl> fileSets
    private final NamedDomainObjectContainer<GlobImpl> files
    private final NamedDomainObjectContainer<ArtifactImpl> artifacts

    @Inject
    AbstractAssembler(ObjectFactory objects) {
        exported = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        stereotype = objects.property(Stereotype).convention(Providers.<Stereotype> notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
        templateDirectory = objects.directoryProperty().convention(Providers.notDefined())
        skipTemplates = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())

        artifacts = objects.domainObjectContainer(ArtifactImpl, new NamedDomainObjectFactory<ArtifactImpl>() {
            @Override
            ArtifactImpl create(String name) {
                ArtifactImpl artifact = objects.newInstance(ArtifactImpl, objects)
                artifact.name = name
                artifact
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

        fileSets = objects.domainObjectContainer(FileSetImpl, new NamedDomainObjectFactory<FileSetImpl>() {
            @Override
            FileSetImpl create(String name) {
                FileSetImpl fs = objects.newInstance(FileSetImpl, objects)
                fs.name = name
                fs
            }
        })
    }

    @Internal
    boolean isSet() {
        exported.present ||
            active.present ||
            extraProperties.present ||
            templateDirectory.present ||
            skipTemplates.present ||
            !artifacts.isEmpty() ||
            !fileSets.isEmpty() ||
            !files.isEmpty()
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void setStereotype(String str) {
        if (isNotBlank(str)) {
            stereotype.set(Stereotype.of(str.trim()))
        }
    }

    @Override
    void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory.set(new File(templateDirectory))
    }

    @Override
    void skipTemplate(String template) {
        if (isNotBlank(template)) {
            skipTemplates.add(template.trim())
        }
    }

    @Override
    void artifact(Action<? super Artifact> action) {
        action.execute(artifacts.maybeCreate("artifact-${artifacts.size()}".toString()))
    }

    @Override
    void files(Action<? super Glob> action) {
        action.execute(files.maybeCreate("files-${files.size()}".toString()))
    }

    @Override
    void fileSet(Action<? super FileSet> action) {
        action.execute(fileSets.maybeCreate("fileSet-${fileSets.size()}".toString()))
    }

    @Override
    void platform(Action<? super Platform> action) {
        action.execute(platform)
    }

    @Override
    @CompileDynamic
    void artifact(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, artifacts.maybeCreate("artifact-${artifacts.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void files(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Glob) Closure<Void> action) {
        ConfigureUtil.configure(action, files.maybeCreate("files-${files.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void fileSet(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = FileSet) Closure<Void> action) {
        ConfigureUtil.configure(action, fileSets.maybeCreate("fileSet-${fileSets.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void platform(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Platform) Closure<Void> action) {
        ConfigureUtil.configure(action, platform)
    }

    protected <A extends org.jreleaser.model.internal.assemble.Assembler> void fillProperties(A assembler) {
        assembler.exported = exported.getOrElse(true)
        if (active.present) assembler.active = active.get()
        if (stereotype.present) assembler.stereotype = stereotype.get()
        if (extraProperties.present) assembler.extraProperties.putAll(extraProperties.get())
        for (ArtifactImpl artifact : artifacts) {
            assembler.addArtifact(artifact.toModel())
        }
        for (GlobImpl glob : files) {
            assembler.addFile(glob.toModel())
        }
        for (FileSetImpl fileSet : fileSets) {
            assembler.addFileSet(fileSet.toModel())
        }
        if (templateDirectory.present) {
            assembler.templateDirectory = templateDirectory.get().asFile.toPath().toAbsolutePath().toString()
        }
        assembler.skipTemplates = (Set<String>) skipTemplates.getOrElse([] as Set<String>)
    }
}
