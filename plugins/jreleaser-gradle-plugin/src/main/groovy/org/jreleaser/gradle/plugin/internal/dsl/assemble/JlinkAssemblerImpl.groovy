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
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.assemble.JlinkAssembler
import org.jreleaser.gradle.plugin.dsl.catalog.swid.SwidTag
import org.jreleaser.gradle.plugin.dsl.common.ArchiveOptions
import org.jreleaser.gradle.plugin.dsl.common.Artifact
import org.jreleaser.gradle.plugin.internal.dsl.catalog.swid.SwidTagImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.ArchiveOptionsImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.ArtifactImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.JavaImpl
import org.jreleaser.gradle.plugin.internal.dsl.platform.PlatformImpl
import org.jreleaser.model.Active
import org.jreleaser.model.Archive
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class JlinkAssemblerImpl extends AbstractJavaAssembler implements JlinkAssembler {
    String name
    final Property<String> imageName
    final Property<String> imageNameTransform
    final Property<Archive.Format> archiveFormat
    final Property<Boolean> copyJars
    final ListProperty<String> args
    final SetProperty<String> moduleNames
    final SetProperty<String> additionalModuleNames
    final JavaImpl java
    final PlatformImpl platform
    final ArchiveOptionsImpl options
    final SwidTagImpl swid

    private final JdepsImpl jdeps
    private final JavaArchiveImpl javaArchive
    private final ArtifactImpl jdk
    final NamedDomainObjectContainer<ArtifactImpl> targetJdks

    @Inject
    JlinkAssemblerImpl(ObjectFactory objects) {
        super(objects)

        imageName = objects.property(String).convention(Providers.<String> notDefined())
        imageNameTransform = objects.property(String).convention(Providers.<String> notDefined())
        archiveFormat = objects.property(Archive.Format).convention(Archive.Format.ZIP)
        copyJars = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        args = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        moduleNames = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        additionalModuleNames = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        java = objects.newInstance(JavaImpl, objects)
        platform = objects.newInstance(PlatformImpl, objects)
        jdeps = objects.newInstance(JdepsImpl, objects)
        javaArchive = objects.newInstance(JavaArchiveImpl, objects)
        jdk = objects.newInstance(ArtifactImpl, objects)
        jdk.setName('jdk')
        options = objects.newInstance(ArchiveOptionsImpl, objects)
        swid = objects.newInstance(SwidTagImpl, objects)

        targetJdks = objects.domainObjectContainer(ArtifactImpl, new NamedDomainObjectFactory<ArtifactImpl>() {
            @Override
            ArtifactImpl create(String name) {
                ArtifactImpl artifact = objects.newInstance(ArtifactImpl, objects)
                artifact.name = name
                artifact
            }
        })
    }

    @Override
    void setArchiveFormat(String str) {
        if (isNotBlank(str)) {
            this.archiveFormat.set(Archive.Format.of(str.trim()))
        }
    }

    @Internal
    boolean isSet() {
        super.isSet() ||
            imageName.present ||
            imageNameTransform.present ||
            copyJars.present ||
            args.present ||
            java.isSet() ||
            jdeps.isSet() ||
            javaArchive.isSet() ||
            jdk.isSet() ||
            moduleNames.present ||
            additionalModuleNames.present ||
            !targetJdks.isEmpty() ||
            platform.isSet() ||
            options.isSet()
    }

    @Override
    void arg(String arg) {
        if (isNotBlank(arg)) {
            args.add(arg.trim())
        }
    }

    @Override
    void jdeps(Action<? super Jdeps> action) {
        action.execute(jdeps)
    }

    @Override
    void jdk(Action<? super Artifact> action) {
        action.execute(jdk)
    }

    @Override
    void targetJdk(Action<? super Artifact> action) {
        action.execute(targetJdks.maybeCreate("targetJdk-${targetJdks.size()}".toString()))
    }

    @Override
    void options(Action<? super ArchiveOptions> action) {
        action.execute(options)
    }

    @Override
    void javaArchive(Action<? super JavaArchive> action) {
        action.execute(javaArchive)
    }

    @Override
    void swid(Action<? super SwidTag> action) {
        action.execute(swid)
    }

    @Override
    @CompileDynamic
    void jdeps(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Jdeps) Closure<Void> action) {
        ConfigureUtil.configure(action, jdeps)
    }

    @Override
    @CompileDynamic
    void jdk(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, jdk)
    }

    @Override
    @CompileDynamic
    void targetJdk(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, targetJdks.maybeCreate("targetJdk-${targetJdks.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void options(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ArchiveOptions) Closure<Void> action) {
        ConfigureUtil.configure(action, options)
    }

    @Override
    @CompileDynamic
    void javaArchive(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = JavaArchive) Closure<Void> action) {
        ConfigureUtil.configure(action, javaArchive)
    }

    @Override
    @CompileDynamic
    void swid(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SwidTag) Closure<Void> action) {
        ConfigureUtil.configure(action, swid)
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    org.jreleaser.model.internal.assemble.JlinkAssembler toModel() {
        org.jreleaser.model.internal.assemble.JlinkAssembler assembler = new org.jreleaser.model.internal.assemble.JlinkAssembler()
        assembler.name = name
        fillProperties(assembler)
        assembler.args = (List<String>) args.getOrElse([])
        if (jdeps.isSet()) assembler.jdeps = jdeps.toModel()
        if (javaArchive.isSet()) assembler.javaArchive = javaArchive.toModel()
        if (jdk.isSet()) assembler.jdk = jdk.toModel()
        assembler.archiveFormat = archiveFormat.get()
        assembler.java = java.toModel()
        assembler.platform = platform.toModel()
        if (imageName.present) assembler.imageName = imageName.get()
        if (imageNameTransform.present) assembler.imageNameTransform = imageNameTransform.get()
        if (copyJars.present) assembler.copyJars = copyJars.get()
        assembler.moduleNames = (Set<String>) moduleNames.getOrElse([] as Set)
        assembler.additionalModuleNames = (Set<String>) additionalModuleNames.getOrElse([] as Set)
        for (ArtifactImpl artifact : targetJdks) {
            assembler.addTargetJdk(artifact.toModel())
        }
        if (options.isSet()) assembler.options = options.toModel()
        assembler.swid = swid.toModel()
        assembler
    }

    @CompileStatic
    static class JdepsImpl implements Jdeps {
        final Property<Boolean> enabled
        final Property<String> multiRelease
        final Property<Boolean> ignoreMissingDeps
        final Property<Boolean> useWildcardInPath
        final SetProperty<String> targets

        @Inject
        JdepsImpl(ObjectFactory objects) {
            enabled = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            multiRelease = objects.property(String).convention(Providers.<String> notDefined())
            ignoreMissingDeps = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            useWildcardInPath = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            targets = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        }

        @Internal
        boolean isSet() {
            multiRelease.present ||
                ignoreMissingDeps.present ||
                useWildcardInPath.present ||
                targets.present
        }

        org.jreleaser.model.internal.assemble.JlinkAssembler.Jdeps toModel() {
            org.jreleaser.model.internal.assemble.JlinkAssembler.Jdeps jdeps = new org.jreleaser.model.internal.assemble.JlinkAssembler.Jdeps()
            if (enabled.present) {
                jdeps.enabled = enabled.get()
            } else {
                jdeps.enabled = true
            }
            if (multiRelease.present) jdeps.multiRelease = multiRelease.get()
            if (ignoreMissingDeps.present) jdeps.ignoreMissingDeps = ignoreMissingDeps.get()
            if (useWildcardInPath.present) jdeps.useWildcardInPath = useWildcardInPath.get()
            jdeps.targets = (Set<String>) targets.getOrElse([] as Set)
            jdeps
        }
    }


    @CompileStatic
    static class JavaArchiveImpl implements JavaArchive {
        final Property<String> path
        final Property<String> mainJarName
        final Property<String> libDirectoryName

        @Inject
        JavaArchiveImpl(ObjectFactory objects) {
            path = objects.property(String).convention(Providers.<String> notDefined())
            mainJarName = objects.property(String).convention(Providers.<String> notDefined())
            libDirectoryName = objects.property(String).convention(Providers.<String> notDefined())
        }

        @Internal
        boolean isSet() {
            path.present ||
                mainJarName.present ||
                libDirectoryName.present
        }

        org.jreleaser.model.internal.assemble.JlinkAssembler.JavaArchive toModel() {
            org.jreleaser.model.internal.assemble.JlinkAssembler.JavaArchive javaArchive = new org.jreleaser.model.internal.assemble.JlinkAssembler.JavaArchive()
            if (path.present) javaArchive.path = path.get()
            if (mainJarName.present) javaArchive.mainJarName = mainJarName.get()
            if (libDirectoryName.present) javaArchive.libDirectoryName = libDirectoryName.get()
            javaArchive
        }
    }
}
