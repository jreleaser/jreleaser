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
import org.jreleaser.gradle.plugin.dsl.assemble.NativeImageAssembler
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
class NativeImageAssemblerImpl extends AbstractJavaAssembler implements NativeImageAssembler {
    String name
    final Property<String> imageName
    final Property<String> imageNameTransform
    final Property<Archive.Format> archiveFormat
    final ListProperty<String> args
    final SetProperty<String> components
    final JavaImpl java
    final PlatformImpl platform
    final ArchiveOptionsImpl options
    final SwidTagImpl swid

    private final ArtifactImpl graal
    private final UpxImpl upx
    private final LinuxImpl linux
    private final WindowsImpl windows
    private final OsxImpl osx
    final NamedDomainObjectContainer<ArtifactImpl> graalJdks

    @Inject
    NativeImageAssemblerImpl(ObjectFactory objects) {
        super(objects)

        imageName = objects.property(String).convention(Providers.<String> notDefined())
        imageNameTransform = objects.property(String).convention(Providers.<String> notDefined())
        archiveFormat = objects.property(Archive.Format).convention(Archive.Format.ZIP)
        args = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        components = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        java = objects.newInstance(JavaImpl, objects)
        platform = objects.newInstance(PlatformImpl, objects)
        graal = objects.newInstance(ArtifactImpl, objects)
        graal.setName('graal')
        upx = objects.newInstance(UpxImpl, objects)
        linux = objects.newInstance(LinuxImpl, objects)
        windows = objects.newInstance(WindowsImpl, objects)
        osx = objects.newInstance(OsxImpl, objects)
        options = objects.newInstance(ArchiveOptionsImpl, objects)
        swid = objects.newInstance(SwidTagImpl, objects)

        graalJdks = objects.domainObjectContainer(ArtifactImpl, new NamedDomainObjectFactory<ArtifactImpl>() {
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
            args.present ||
            components.present ||
            java.isSet() ||
            graal.isSet() ||
            upx.isSet() ||
            linux.isSet() ||
            windows.isSet() ||
            osx.isSet() ||
            !graalJdks.isEmpty() ||
            options.isSet()
    }

    @Override
    void arg(String arg) {
        if (isNotBlank(arg)) {
            args.add(arg.trim())
        }
    }

    @Override
    void component(String component) {
        if (isNotBlank(component)) {
            components.add(component.trim())
        }
    }

    @Override
    void graal(Action<? super Artifact> action) {
        action.execute(graal)
    }

    @Override
    void upx(Action<? super Upx> action) {
        action.execute(upx)
    }

    @Override
    void linux(Action<? super Linux> action) {
        action.execute(linux)
    }

    @Override
    void windows(Action<? super Windows> action) {
        action.execute(windows)
    }

    @Override
    void osx(Action<? super Osx> action) {
        action.execute(osx)
    }

    @Override
    void graalJdk(Action<? super Artifact> action) {
        action.execute(graalJdks.maybeCreate("graalJdk-${graalJdks.size()}".toString()))
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
    void graal(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, graal)
    }

    @Override
    @CompileDynamic
    void upx(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upx) Closure<Void> action) {
        ConfigureUtil.configure(action, upx)
    }

    @Override
    @CompileDynamic
    void linux(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upx) Closure<Void> action) {
        ConfigureUtil.configure(action, linux)
    }

    @Override
    @CompileDynamic
    void windows(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upx) Closure<Void> action) {
        ConfigureUtil.configure(action, windows)
    }

    @Override
    @CompileDynamic
    void osx(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upx) Closure<Void> action) {
        ConfigureUtil.configure(action, osx)
    }

    @Override
    @CompileDynamic
    void graalJdk(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, graalJdks.maybeCreate("graalJdk-${graalJdks.size()}".toString()))
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

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    org.jreleaser.model.internal.assemble.NativeImageAssembler toModel() {
        org.jreleaser.model.internal.assemble.NativeImageAssembler assembler = new org.jreleaser.model.internal.assemble.NativeImageAssembler()
        assembler.name = name
        fillProperties(assembler)
        assembler.java = java.toModel()
        assembler.platform = platform.toModel()
        if (imageName.present) assembler.imageName = imageName.get()
        if (imageNameTransform.present) assembler.imageNameTransform = imageNameTransform.get()
        assembler.archiveFormat = archiveFormat.get()
        assembler.args = (List<String>) args.getOrElse([])
        assembler.components = (Set<String>) components.getOrElse([] as Set)
        if (graal.isSet()) assembler.graal = graal.toModel()
        if (upx.isSet()) assembler.upx = upx.toModel()
        if (linux.isSet()) assembler.linux = linux.toModel()
        if (windows.isSet()) assembler.windows = windows.toModel()
        if (osx.isSet()) assembler.osx = osx.toModel()
        for (ArtifactImpl artifact : graalJdks) {
            assembler.addGraalJdk(artifact.toModel())
        }
        if (options.isSet()) assembler.options = options.toModel()
        assembler.swid = swid.toModel()
        assembler
    }

    @CompileStatic
    static class UpxImpl implements Upx {
        final Property<Active> active
        final Property<String> version
        final ListProperty<String> args

        @Inject
        UpxImpl(ObjectFactory objects) {
            active = objects.property(Active).convention(Providers.<Active> notDefined())
            version = objects.property(String).convention(Providers.<String> notDefined())
            args = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        }

        @Override
        void setActive(String str) {
            if (isNotBlank(str)) {
                active.set(Active.of(str.trim()))
            }
        }

        @Override
        void arg(String arg) {
            if (isNotBlank(arg)) {
                args.add(arg.trim())
            }
        }

        @Internal
        boolean isSet() {
            active.present ||
                version.present ||
                args.present
        }

        org.jreleaser.model.internal.assemble.NativeImageAssembler.Upx toModel() {
            org.jreleaser.model.internal.assemble.NativeImageAssembler.Upx upx = new org.jreleaser.model.internal.assemble.NativeImageAssembler.Upx()
            if (active.present) upx.active = active.get()
            if (version.present) upx.version = version.get()
            upx.args = (List<String>) args.getOrElse([])
            upx
        }
    }

    @CompileStatic
    static class LinuxImpl implements Linux {
        final ListProperty<String> args

        @Inject
        LinuxImpl(ObjectFactory objects) {
            args = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        }

        @Override
        void arg(String arg) {
            if (isNotBlank(arg)) {
                args.add(arg.trim())
            }
        }

        @Internal
        boolean isSet() {
            args.present
        }

        org.jreleaser.model.internal.assemble.NativeImageAssembler.Linux toModel() {
            org.jreleaser.model.internal.assemble.NativeImageAssembler.Linux linux = new org.jreleaser.model.internal.assemble.NativeImageAssembler.Linux()
            linux.args = (List<String>) args.getOrElse([])
            linux
        }
    }

    @CompileStatic
    static class WindowsImpl implements Windows {
        final ListProperty<String> args

        @Inject
        WindowsImpl(ObjectFactory objects) {
            args = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        }

        @Override
        void arg(String arg) {
            if (isNotBlank(arg)) {
                args.add(arg.trim())
            }
        }

        @Internal
        boolean isSet() {
            args.present
        }

        org.jreleaser.model.internal.assemble.NativeImageAssembler.Windows toModel() {
            org.jreleaser.model.internal.assemble.NativeImageAssembler.Windows windows = new org.jreleaser.model.internal.assemble.NativeImageAssembler.Windows()
            windows.args = (List<String>) args.getOrElse([])
            windows
        }
    }

    @CompileStatic
    static class OsxImpl implements Osx {
        final ListProperty<String> args

        @Inject
        OsxImpl(ObjectFactory objects) {
            args = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        }

        @Override
        void arg(String arg) {
            if (isNotBlank(arg)) {
                args.add(arg.trim())
            }
        }

        @Internal
        boolean isSet() {
            args.present
        }

        org.jreleaser.model.internal.assemble.NativeImageAssembler.Osx toModel() {
            org.jreleaser.model.internal.assemble.NativeImageAssembler.Osx osx = new org.jreleaser.model.internal.assemble.NativeImageAssembler.Osx()
            osx.args = (List<String>) args.getOrElse([])
            osx
        }
    }
}
