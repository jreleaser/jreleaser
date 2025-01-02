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
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.assemble.JpackageAssembler
import org.jreleaser.gradle.plugin.dsl.common.Artifact
import org.jreleaser.gradle.plugin.internal.dsl.catalog.swid.SwidTagImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.ArtifactImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.JavaImpl
import org.jreleaser.gradle.plugin.internal.dsl.platform.PlatformImpl
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.10.0
 */
@CompileStatic
class JpackageAssemblerImpl extends AbstractJavaAssembler implements JpackageAssembler {
    String name
    final Property<String> jlink
    final Property<Boolean> attachPlatform
    final Property<Boolean> verbose
    final JavaImpl java
    final PlatformImpl platform
    final SwidTagImpl swid

    private final ApplicationPackageImpl applicationPackage
    private final LauncherImpl launcher
    private final LinuxImpl linux
    private final WindowsImpl windows
    private final OsxImpl osx
    final NamedDomainObjectContainer<ArtifactImpl> runtimeImages

    @Inject
    JpackageAssemblerImpl(ObjectFactory objects) {
        super(objects)

        jlink = objects.property(String).convention(Providers.<String> notDefined())
        attachPlatform = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        verbose = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        java = objects.newInstance(JavaImpl, objects)
        platform = objects.newInstance(PlatformImpl, objects)
        swid = objects.newInstance(SwidTagImpl, objects)
        applicationPackage = objects.newInstance(ApplicationPackageImpl, objects)
        launcher = objects.newInstance(LauncherImpl, objects)
        linux = objects.newInstance(LinuxImpl, objects)
        windows = objects.newInstance(WindowsImpl, objects)
        osx = objects.newInstance(OsxImpl, objects)

        runtimeImages = objects.domainObjectContainer(ArtifactImpl, new NamedDomainObjectFactory<ArtifactImpl>() {
            @Override
            ArtifactImpl create(String name) {
                ArtifactImpl artifact = objects.newInstance(ArtifactImpl, objects)
                artifact.name = name
                artifact
            }
        })
    }

    @Internal
    boolean isSet() {
        super.isSet() ||
            jlink.present ||
            attachPlatform.present ||
            verbose.present ||
            java.isSet() ||
            platform.isSet() ||
            applicationPackage.isSet() ||
            launcher.isSet() ||
            linux.isSet() ||
            windows.isSet() ||
            osx.isSet() ||
            !runtimeImages.isEmpty()
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void runtimeImage(Action<? super Artifact> action) {
        action.execute(runtimeImages.maybeCreate("runtimeImage-${runtimeImages.size()}".toString()))
    }

    @Override
    void applicationPackage(Action<? super ApplicationPackage> action) {
        action.execute(applicationPackage)
    }

    @Override
    void launcher(Action<? super Launcher> action) {
        action.execute(launcher)
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
    @CompileDynamic
    void runtimeImage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, runtimeImages.maybeCreate("runtimeImage-${runtimeImages.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void applicationPackage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ApplicationPackage) Closure<Void> action) {
        ConfigureUtil.configure(action, applicationPackage)
    }

    @Override
    @CompileDynamic
    void launcher(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Launcher) Closure<Void> action) {
        ConfigureUtil.configure(action, launcher)
    }

    @Override
    @CompileDynamic
    void linux(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Linux) Closure<Void> action) {
        ConfigureUtil.configure(action, linux)
    }

    @Override
    @CompileDynamic
    void windows(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Windows) Closure<Void> action) {
        ConfigureUtil.configure(action, windows)
    }

    @Override
    @CompileDynamic
    void osx(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Osx) Closure<Void> action) {
        ConfigureUtil.configure(action, osx)
    }

    org.jreleaser.model.internal.assemble.JpackageAssembler toModel() {
        org.jreleaser.model.internal.assemble.JpackageAssembler assembler = new org.jreleaser.model.internal.assemble.JpackageAssembler()
        assembler.name = name
        fillProperties(assembler)
        if (java.isSet()) assembler.java = java.toModel()
        if (platform.isSet()) assembler.platform = platform.toModel()
        if (applicationPackage.isSet()) assembler.applicationPackage = applicationPackage.toModel()
        if (launcher.isSet()) assembler.launcher = launcher.toModel()
        if (linux.isSet()) assembler.linux = linux.toModel()
        if (windows.isSet()) assembler.windows = windows.toModel()
        if (osx.isSet()) assembler.osx = osx.toModel()
        if (jlink.present) assembler.jlink = jlink.get()
        if (attachPlatform.present) assembler.attachPlatform = attachPlatform.get()
        if (verbose.present) assembler.verbose = verbose.get()
        for (ArtifactImpl artifact : runtimeImages) {
            assembler.addRuntimeImage(artifact.toModel())
        }
        assembler.swid = swid.toModel()

        assembler
    }

    @CompileStatic
    static class ApplicationPackageImpl implements ApplicationPackage {
        final ListProperty<String> fileAssociations
        final Property<String> appName
        final Property<String> appVersion
        final Property<String> vendor
        final Property<String> copyright
        final Property<String> licenseFile

        @Inject
        ApplicationPackageImpl(ObjectFactory objects) {
            appName = objects.property(String).convention(Providers.<String> notDefined())
            appVersion = objects.property(String).convention(Providers.<String> notDefined())
            vendor = objects.property(String).convention(Providers.<String> notDefined())
            copyright = objects.property(String).convention(Providers.<String> notDefined())
            licenseFile = objects.property(String).convention(Providers.<String> notDefined())
            fileAssociations = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        }

        @Internal
        boolean isSet() {
            appName.present ||
                appVersion.present ||
                vendor.present ||
                copyright.present ||
                licenseFile.present ||
                fileAssociations.present
        }

        org.jreleaser.model.internal.assemble.JpackageAssembler.ApplicationPackage toModel() {
            org.jreleaser.model.internal.assemble.JpackageAssembler.ApplicationPackage a = new org.jreleaser.model.internal.assemble.JpackageAssembler.ApplicationPackage()
            a.appName = appName.orNull
            a.appVersion = appVersion.orNull
            a.vendor = vendor.orNull
            a.copyright = copyright.orNull
            a.licenseFile = licenseFile.orNull
            a.fileAssociations = (List<String>) fileAssociations.getOrElse([] as List<String>)
            a
        }
    }


    @CompileStatic
    static class LauncherImpl implements Launcher {
        final ListProperty<String> arguments
        final ListProperty<String> javaOptions
        final ListProperty<String> launchers

        @Inject
        LauncherImpl(ObjectFactory objects) {
            arguments = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
            javaOptions = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
            launchers = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        }

        @Internal
        boolean isSet() {
            arguments.present ||
                javaOptions.present ||
                launchers.present
        }

        org.jreleaser.model.internal.assemble.JpackageAssembler.Launcher toModel() {
            org.jreleaser.model.internal.assemble.JpackageAssembler.Launcher a = new org.jreleaser.model.internal.assemble.JpackageAssembler.Launcher()
            a.arguments = (List<String>) arguments.getOrElse([] as List<String>)
            a.javaOptions = (List<String>) javaOptions.getOrElse([] as List<String>)
            a.launchers = (List<String>) launchers.getOrElse([] as List<String>)
            a
        }
    }

    @CompileStatic
    private abstract static class AbstractPlatformPackager implements PlatformPackager {
        final Property<String> appName
        final RegularFileProperty icon
        final ListProperty<String> types
        final Property<String> installDir
        final Property<String> resourceDir

        @Inject
        AbstractPlatformPackager(ObjectFactory objects) {
            appName = objects.property(String).convention(Providers.<String> notDefined())
            icon = objects.fileProperty().convention(Providers.notDefined())
            types = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
            installDir = objects.property(String).convention(Providers.<String> notDefined())
            resourceDir = objects.property(String).convention(Providers.<String> notDefined())
        }

        @Override
        void setIcon(String icon) {
            this.icon.set(new File(icon))
        }

        @Internal
        boolean isSet() {
            appName.present ||
                icon.present ||
                types.present ||
                jdk.isSet() ||
                installDir.present ||
                resourceDir.present
        }

        protected abstract ArtifactImpl getJdk()

        @Override
        void jdk(Action<? super Artifact> action) {
            action.execute(jdk)
        }

        @Override
        @CompileDynamic
        void jdk(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
            ConfigureUtil.configure(action, jdk)
        }

        void fillProperties(org.jreleaser.model.internal.assemble.JpackageAssembler.PlatformPackager p) {
            p.appName = appName.orNull
            p.icon = icon.orNull
            p.types = (List<String>) types.getOrElse([] as List<String>)
            p.installDir = installDir.orNull
            p.resourceDir = resourceDir.orNull
            if (jdk.isSet()) p.jdk = jdk.toModel()
        }
    }

    @CompileStatic
    static class LinuxImpl extends AbstractPlatformPackager implements Linux {
        final ListProperty<String> packageDeps
        final Property<String> packageName
        final Property<String> maintainer
        final Property<String> menuGroup
        final Property<String> license
        final Property<String> appRelease
        final Property<String> appCategory
        final Property<Boolean> shortcut
        private final ArtifactImpl jdk

        @Inject
        LinuxImpl(ObjectFactory objects) {
            super(objects)
            packageDeps = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
            packageName = objects.property(String).convention(Providers.<String> notDefined())
            maintainer = objects.property(String).convention(Providers.<String> notDefined())
            menuGroup = objects.property(String).convention(Providers.<String> notDefined())
            license = objects.property(String).convention(Providers.<String> notDefined())
            appRelease = objects.property(String).convention(Providers.<String> notDefined())
            appCategory = objects.property(String).convention(Providers.<String> notDefined())
            shortcut = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            jdk = objects.newInstance(ArtifactImpl, objects)
            jdk.setName('jdk')
        }

        @Internal
        boolean isSet() {
            super.isSet() ||
                packageDeps.present ||
                packageName.present ||
                maintainer.present ||
                menuGroup.present ||
                license.present ||
                appRelease.present ||
                appCategory.present ||
                shortcut.present
        }

        @Override
        protected ArtifactImpl getJdk() {
            return jdk
        }

        org.jreleaser.model.internal.assemble.JpackageAssembler.Linux toModel() {
            org.jreleaser.model.internal.assemble.JpackageAssembler.Linux a = new org.jreleaser.model.internal.assemble.JpackageAssembler.Linux()
            fillProperties(a)
            a.packageName = packageName.orNull
            a.maintainer = maintainer.orNull
            a.menuGroup = menuGroup.orNull
            a.license = license.orNull
            a.appRelease = appRelease.orNull
            a.appCategory = appCategory.orNull
            a.shortcut = shortcut.orNull
            a.packageDeps = (List<String>) packageDeps.getOrElse([] as List<String>)
            a
        }
    }

    @CompileStatic
    static class WindowsImpl extends AbstractPlatformPackager implements Windows {
        final Property<Boolean> console
        final Property<Boolean> dirChooser
        final Property<Boolean> menu
        final Property<Boolean> perUserInstall
        final Property<Boolean> shortcut
        final Property<String> menuGroup
        final Property<String> upgradeUuid
        private final ArtifactImpl jdk

        @Inject
        WindowsImpl(ObjectFactory objects) {
            super(objects)
            console = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            dirChooser = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            menu = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            perUserInstall = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            shortcut = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            menuGroup = objects.property(String).convention(Providers.<String> notDefined())
            upgradeUuid = objects.property(String).convention(Providers.<String> notDefined())
            jdk = objects.newInstance(ArtifactImpl, objects)
            jdk.setName('jdk')
        }

        @Internal
        boolean isSet() {
            super.isSet() ||
                console.present ||
                dirChooser.present ||
                menu.present ||
                perUserInstall.present ||
                shortcut.present ||
                menuGroup.present ||
                upgradeUuid.present
        }

        @Override
        protected ArtifactImpl getJdk() {
            return jdk
        }

        org.jreleaser.model.internal.assemble.JpackageAssembler.Windows toModel() {
            org.jreleaser.model.internal.assemble.JpackageAssembler.Windows a = new org.jreleaser.model.internal.assemble.JpackageAssembler.Windows()
            fillProperties(a)
            a.console = console.orNull
            a.dirChooser = dirChooser.orNull
            a.menu = menu.orNull
            a.perUserInstall = perUserInstall.orNull
            a.shortcut = shortcut.orNull
            a.menuGroup = menuGroup.orNull
            a.upgradeUuid = upgradeUuid.orNull
            if (jdk.isSet()) a.jdk = jdk.toModel()
            a
        }
    }

    @CompileStatic
    static class OsxImpl extends AbstractPlatformPackager implements Osx {
        final Property<String> packageIdentifier
        final Property<String> packageName
        final Property<String> packageSigningPrefix
        final Property<String> signingKeychain
        final Property<String> signingKeyUsername
        final Property<Boolean> sign
        private final ArtifactImpl jdk

        @Inject
        OsxImpl(ObjectFactory objects) {
            super(objects)
            packageIdentifier = objects.property(String).convention(Providers.<String> notDefined())
            packageName = objects.property(String).convention(Providers.<String> notDefined())
            packageSigningPrefix = objects.property(String).convention(Providers.<String> notDefined())
            signingKeychain = objects.property(String).convention(Providers.<String> notDefined())
            signingKeyUsername = objects.property(String).convention(Providers.<String> notDefined())
            sign = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            jdk = objects.newInstance(ArtifactImpl, objects)
            jdk.setName('jdk')
        }

        @Internal
        boolean isSet() {
            super.isSet() ||
                packageIdentifier.present ||
                packageName.present ||
                packageSigningPrefix.present ||
                signingKeychain.present ||
                signingKeyUsername.present ||
                sign.present
        }

        @Override
        protected ArtifactImpl getJdk() {
            return jdk
        }

        org.jreleaser.model.internal.assemble.JpackageAssembler.Osx toModel() {
            org.jreleaser.model.internal.assemble.JpackageAssembler.Osx a = new org.jreleaser.model.internal.assemble.JpackageAssembler.Osx()
            fillProperties(a)
            a.packageIdentifier = packageIdentifier.orNull
            a.packageName = packageName.orNull
            a.packageSigningPrefix = packageSigningPrefix.orNull
            a.signingKeychain = signingKeychain.orNull
            a.signingKeyUsername = signingKeyUsername.orNull
            a.sign = sign.orNull
            a
        }
    }
}
