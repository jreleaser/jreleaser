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
import org.jreleaser.gradle.plugin.dsl.common.Matrix
import org.jreleaser.gradle.plugin.internal.dsl.catalog.swid.SwidTagImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.ArchiveOptionsImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.ArtifactImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.JavaImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.MatrixImpl
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
    final ListProperty<String> args
    final SetProperty<String> components
    final JavaImpl java
    final PlatformImpl platform
    final ArchiveOptionsImpl options
    final SwidTagImpl swid

    private final ArtifactImpl graal
    private final ArchivingImpl archiving
    private final UpxImpl upx
    private final LinuxX86Impl linuxX86
    private final WindowsX86Impl windowsX86
    private final MacosX86Impl macosX86
    private final LinuxArmImpl linuxArm
    private final MacosArmImpl macosArm
    final NamedDomainObjectContainer<ArtifactImpl> graalJdks
    final Property<Boolean> applyDefaultMatrix
    final ArtifactImpl graalJdkPattern
    final MatrixImpl matrix

    @Inject
    NativeImageAssemblerImpl(ObjectFactory objects) {
        super(objects)

        imageName = objects.property(String).convention(Providers.<String> notDefined())
        imageNameTransform = objects.property(String).convention(Providers.<String> notDefined())
        args = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        components = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        java = objects.newInstance(JavaImpl, objects)
        platform = objects.newInstance(PlatformImpl, objects)
        graal = objects.newInstance(ArtifactImpl, objects)
        graal.setName('graal')
        archiving = objects.newInstance(ArchivingImpl, objects)
        upx = objects.newInstance(UpxImpl, objects)
        linuxX86 = objects.newInstance(LinuxX86Impl, objects)
        windowsX86 = objects.newInstance(WindowsX86Impl, objects)
        macosX86 = objects.newInstance(MacosX86Impl, objects)
        linuxArm = objects.newInstance(LinuxArmImpl, objects)
        macosArm = objects.newInstance(MacosArmImpl, objects)
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

        applyDefaultMatrix = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        graalJdkPattern = objects.newInstance(ArtifactImpl, objects)
        matrix = objects.newInstance(MatrixImpl, objects)
    }

    @Override
    void setArchiveFormat(String str) {
        archiving.setFormat(str)
    }

    @Override
    Property<Archive.Format> getArchiveFormat() {
        return archiving.format
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
            archiving.isSet() ||
            linuxX86.isSet() ||
            windowsX86.isSet() ||
            macosX86.isSet() ||
            linuxArm.isSet() ||
            macosArm.isSet() ||
            !graalJdks.isEmpty() ||
            applyDefaultMatrix.present ||
            graalJdkPattern.isSet() ||
            matrix.isSet() ||
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
    void archiving(Action<? super Archiving> action) {
        action.execute(archiving)
    }

    @Override
    void upx(Action<? super Upx> action) {
        action.execute(upx)
    }

    @Override
    @Deprecated
    void linux(Action<? super LinuxX86> action) {
        action.execute(linuxX86)
    }

    @Override
    @Deprecated
    void windows(Action<? super WindowsX86> action) {
        action.execute(windowsX86)
    }

    @Override
    @Deprecated
    void osx(Action<? super MacosX86> action) {
        action.execute(macosX86)
    }

    @Override
    void linuxX86(Action<? super LinuxX86> action) {
        action.execute(linuxX86)
    }

    @Override
    void windowsX86(Action<? super WindowsX86> action) {
        action.execute(windowsX86)
    }

    @Override
    void macosX86(Action<? super MacosX86> action) {
        action.execute(macosX86)
    }

    @Override
    void linuxArm(Action<? super LinuxArm> action) {
        action.execute(linuxArm)
    }

    @Override
    void macosArm(Action<? super MacosArm> action) {
        action.execute(macosArm)
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
    void matrix(Action<? super Matrix> action) {
        action.execute(matrix)
    }

    @Override
    void graalJdkPattern(Action<? super Artifact> action) {
        action.execute(graalJdkPattern)
    }

    @Override
    @CompileDynamic
    void graal(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, graal)
    }

    @Override
    @CompileDynamic
    void archiving(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Archiving) Closure<Void> action) {
        ConfigureUtil.configure(action, archiving)
    }

    @Override
    @CompileDynamic
    void upx(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upx) Closure<Void> action) {
        ConfigureUtil.configure(action, upx)
    }

    @Override
    @CompileDynamic
    @Deprecated
    void linux(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upx) Closure<Void> action) {
        ConfigureUtil.configure(action, linuxX86)
    }

    @Override
    @CompileDynamic
    @Deprecated
    void windows(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upx) Closure<Void> action) {
        ConfigureUtil.configure(action, windowsX86)
    }

    @Override
    @CompileDynamic
    @Deprecated
    void osx(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upx) Closure<Void> action) {
        ConfigureUtil.configure(action, macosX86)
    }

    @Override
    @CompileDynamic
    void linuxX86(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upx) Closure<Void> action) {
        ConfigureUtil.configure(action, linuxX86)
    }

    @Override
    @CompileDynamic
    void windowsX86(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upx) Closure<Void> action) {
        ConfigureUtil.configure(action, windowsX86)
    }

    @Override
    @CompileDynamic
    void macosX86(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upx) Closure<Void> action) {
        ConfigureUtil.configure(action, macosX86)
    }

    @Override
    @CompileDynamic
    void linuxArm(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upx) Closure<Void> action) {
        ConfigureUtil.configure(action, linuxArm)
    }

    @Override
    @CompileDynamic
    void macosArm(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Upx) Closure<Void> action) {
        ConfigureUtil.configure(action, macosArm)
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
    @CompileDynamic
    void matrix(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Matrix) Closure<Void> action) {
        ConfigureUtil.configure(action, matrix)
    }

    @Override
    @CompileDynamic
    void graalJdkPattern(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, graalJdkPattern)
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
        assembler.args = (List<String>) args.getOrElse([])
        assembler.components = (Set<String>) components.getOrElse([] as Set)
        if (graal.isSet()) assembler.graal = graal.toModel()
        if (archiving.isSet()) assembler.archiving = archiving.toModel()
        if (upx.isSet()) assembler.upx = upx.toModel()
        if (linuxX86.isSet()) assembler.linuxX86 = linuxX86.toModel()
        if (windowsX86.isSet()) assembler.windowsX86 = windowsX86.toModel()
        if (macosX86.isSet()) assembler.macosX86 = macosX86.toModel()
        if (linuxArm.isSet()) assembler.linuxArm = linuxArm.toModel()
        if (macosArm.isSet()) assembler.macosArm = macosArm.toModel()
        for (ArtifactImpl artifact : graalJdks) {
            assembler.addGraalJdk(artifact.toModel())
        }
        if (applyDefaultMatrix.present) assembler.applyDefaultMatrix = applyDefaultMatrix.get()
        if (matrix.isSet()) assembler.setMatrix(matrix.toModel())
        if (graalJdkPattern.isSet()) assembler.setGraalJdkPattern(graalJdkPattern.toModel())
        if (options.isSet()) assembler.options = options.toModel()
        assembler.swid = swid.toModel()
        assembler
    }

    @CompileStatic
    static class ArchivingImpl implements Archiving {
        final Property<Boolean> enabled
        final Property<Archive.Format> format

        @Inject
        ArchivingImpl(ObjectFactory objects) {
            enabled = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            format = objects.property(Archive.Format).convention(Archive.Format.ZIP)
        }

        @Override
        void setFormat(String str) {
            if (isNotBlank(str)) {
                this.format.set(Archive.Format.of(str.trim()))
            }
        }

        @Internal
        boolean isSet() {
            enabled.present ||
                format.present
        }

        org.jreleaser.model.internal.assemble.NativeImageAssembler.Archiving toModel() {
            org.jreleaser.model.internal.assemble.NativeImageAssembler.Archiving archiving = new org.jreleaser.model.internal.assemble.NativeImageAssembler.Archiving()
            if (enabled.present) archiving.enabled = enabled.get()
            if (format.present) archiving.format = format.get()
            archiving
        }
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
    static class LinuxX86Impl implements LinuxX86 {
        final ListProperty<String> args

        @Inject
        LinuxX86Impl(ObjectFactory objects) {
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

        org.jreleaser.model.internal.assemble.NativeImageAssembler.LinuxX86 toModel() {
            org.jreleaser.model.internal.assemble.NativeImageAssembler.LinuxX86 linuxX86 = new org.jreleaser.model.internal.assemble.NativeImageAssembler.LinuxX86()
            linuxX86.args = (List<String>) args.getOrElse([])
            linuxX86
        }
    }

    @CompileStatic
    static class WindowsX86Impl implements WindowsX86 {
        final ListProperty<String> args

        @Inject
        WindowsX86Impl(ObjectFactory objects) {
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

        org.jreleaser.model.internal.assemble.NativeImageAssembler.WindowsX86 toModel() {
            org.jreleaser.model.internal.assemble.NativeImageAssembler.WindowsX86 windowsX86 = new org.jreleaser.model.internal.assemble.NativeImageAssembler.WindowsX86()
            windowsX86.args = (List<String>) args.getOrElse([])
            windowsX86
        }
    }

    @CompileStatic
    static class MacosX86Impl implements MacosX86 {
        final ListProperty<String> args

        @Inject
        MacosX86Impl(ObjectFactory objects) {
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

        org.jreleaser.model.internal.assemble.NativeImageAssembler.MacosX86 toModel() {
            org.jreleaser.model.internal.assemble.NativeImageAssembler.MacosX86 macosX86 = new org.jreleaser.model.internal.assemble.NativeImageAssembler.MacosX86()
            macosX86.args = (List<String>) args.getOrElse([])
            macosX86
        }
    }

    @CompileStatic
    static class LinuxArmImpl implements LinuxArm {
        final ListProperty<String> args

        @Inject
        LinuxArmImpl(ObjectFactory objects) {
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

        org.jreleaser.model.internal.assemble.NativeImageAssembler.LinuxArm toModel() {
            org.jreleaser.model.internal.assemble.NativeImageAssembler.LinuxArm linuxArm = new org.jreleaser.model.internal.assemble.NativeImageAssembler.LinuxArm()
            linuxArm.args = (List<String>) args.getOrElse([])
            linuxArm
        }
    }

    @CompileStatic
    static class MacosArmImpl implements MacosArm {
        final ListProperty<String> args

        @Inject
        MacosArmImpl(ObjectFactory objects) {
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

        org.jreleaser.model.internal.assemble.NativeImageAssembler.MacosArm toModel() {
            org.jreleaser.model.internal.assemble.NativeImageAssembler.MacosArm macosArm = new org.jreleaser.model.internal.assemble.NativeImageAssembler.MacosArm()
            macosArm.args = (List<String>) args.getOrElse([])
            macosArm
        }
    }
}
