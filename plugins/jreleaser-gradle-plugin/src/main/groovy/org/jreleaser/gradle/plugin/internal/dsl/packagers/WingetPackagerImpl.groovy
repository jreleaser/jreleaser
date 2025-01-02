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
package org.jreleaser.gradle.plugin.internal.dsl.packagers

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
import org.jreleaser.gradle.plugin.dsl.common.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.packagers.Tap
import org.jreleaser.gradle.plugin.dsl.packagers.WingetPackager
import org.jreleaser.gradle.plugin.internal.dsl.common.CommitAuthorImpl
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.5.0
 */
@CompileStatic
class WingetPackagerImpl extends AbstractRepositoryPackager implements WingetPackager {
    final CommitAuthorImpl commitAuthor
    final TapImpl repository
    final Property<String> defaultLocale
    final Property<String> author
    final Property<String> moniker
    final Property<String> minimumOsVersion
    final Property<String> productCode
    final ListProperty<String> tags

    final PackageImpl wingetPackage
    final PublisherImpl publisher
    final InstallerImpl installer

    @Inject
    WingetPackagerImpl(ObjectFactory objects) {
        super(objects)
        repository = objects.newInstance(TapImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)
        defaultLocale = objects.property(String).convention(Providers.<String> notDefined())
        author = objects.property(String).convention(Providers.<String> notDefined())
        moniker = objects.property(String).convention(Providers.<String> notDefined())
        minimumOsVersion = objects.property(String).convention(Providers.<String> notDefined())
        productCode = objects.property(String).convention(Providers.<String> notDefined())
        tags = objects.listProperty(String).convention(Providers.<List<String>> notDefined())

        wingetPackage = objects.newInstance(PackageImpl, objects)
        publisher = objects.newInstance(PublisherImpl, objects)
        installer = objects.newInstance(InstallerImpl, objects)
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            commitAuthor.isSet() ||
            repository.isSet() ||
            defaultLocale.present ||
            author.present ||
            moniker.present ||
            minimumOsVersion.present ||
            productCode.present ||
            tags.present ||
            wingetPackage.isSet() ||
            publisher.isSet() ||
            installer.isSet()
    }

    @Override
    void tag(String tag) {
        if (isNotBlank(tag)) {
            tags.add(tag.trim())
        }
    }

    @Override
    void repository(Action<? super Tap> action) {
        action.execute(repository)
    }

    @Override
    void commitAuthor(Action<? super CommitAuthor> action) {
        action.execute(commitAuthor)
    }

    @Override
    void wingetPackage(Action<? super Package> action) {
        action.execute(wingetPackage)
    }

    @Override
    void publisher(Action<? super Publisher> action) {
        action.execute(publisher)
    }

    @Override
    void installer(Action<? super Installer> action) {
        action.execute(installer)
    }

    @Override
    @CompileDynamic
    void repository(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Tap) Closure<Void> action) {
        ConfigureUtil.configure(action, repository)
    }

    @Override
    @CompileDynamic
    void commitAuthor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommitAuthor) Closure<Void> action) {
        ConfigureUtil.configure(action, commitAuthor)
    }

    @Override
    @CompileDynamic
    void wingetPackage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Package) Closure<Void> action) {
        ConfigureUtil.configure(action, wingetPackage)
    }

    @Override
    @CompileDynamic
    void publisher(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Publisher) Closure<Void> action) {
        ConfigureUtil.configure(action, publisher)
    }

    @Override
    @CompileDynamic
    void installer(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Installer) Closure<Void> action) {
        ConfigureUtil.configure(action, installer)
    }

    org.jreleaser.model.internal.packagers.WingetPackager toModel() {
        org.jreleaser.model.internal.packagers.WingetPackager packager = new org.jreleaser.model.internal.packagers.WingetPackager()
        fillPackagerProperties(packager)
        fillTemplatePackagerProperties(packager)
        if (repository.isSet()) packager.repository = repository.toWingetRepository()
        if (commitAuthor.isSet()) packager.commitAuthor = commitAuthor.toModel()
        if (defaultLocale.present) packager.defaultLocale = defaultLocale.get()
        if (author.present) packager.author = author.get()
        if (moniker.present) packager.moniker = moniker.get()
        if (minimumOsVersion.present) packager.minimumOsVersion = minimumOsVersion.get()
        if (productCode.present) packager.productCode = productCode.get()
        packager.tags = (List<String>) tags.getOrElse([])
        if (wingetPackage.isSet()) packager.setPackage(wingetPackage.toModel())
        if (publisher.isSet()) packager.publisher = publisher.toModel()
        if (installer.isSet()) packager.installer = installer.toModel()
        packager
    }

    @CompileStatic
    static class PackageImpl implements Package {
        final Property<String> identifier
        final Property<String> name
        final Property<String> version
        final Property<String> url

        @Inject
        PackageImpl(ObjectFactory objects) {
            identifier = objects.property(String).convention(Providers.<String> notDefined())
            name = objects.property(String).convention(Providers.<String> notDefined())
            version = objects.property(String).convention(Providers.<String> notDefined())
            url = objects.property(String).convention(Providers.<String> notDefined())
        }

        @Internal
        boolean isSet() {
            identifier.present ||
                name.present ||
                version.present ||
                url.present
        }

        org.jreleaser.model.internal.packagers.WingetPackager.Package toModel() {
            org.jreleaser.model.internal.packagers.WingetPackager.Package p = new org.jreleaser.model.internal.packagers.WingetPackager.Package()
            if (identifier.present) p.identifier = identifier.get()
            if (name.present) p.name = name.get()
            if (version.present) p.version = version.get()
            if (url.present) p.url = url.get()
            p
        }
    }

    @CompileStatic
    static class PublisherImpl implements Publisher {
        final Property<String> name
        final Property<String> url
        final Property<String> supportUrl

        @Inject
        PublisherImpl(ObjectFactory objects) {
            name = objects.property(String).convention(Providers.<String> notDefined())
            url = objects.property(String).convention(Providers.<String> notDefined())
            supportUrl = objects.property(String).convention(Providers.<String> notDefined())
        }

        @Internal
        boolean isSet() {
            name.present ||
                url.present ||
                supportUrl.present
        }

        org.jreleaser.model.internal.packagers.WingetPackager.Publisher toModel() {
            org.jreleaser.model.internal.packagers.WingetPackager.Publisher p = new org.jreleaser.model.internal.packagers.WingetPackager.Publisher()
            if (name.present) p.name = name.get()
            if (url.present) p.url = url.get()
            if (supportUrl.present) p.supportUrl = supportUrl.get()
            p
        }
    }

    @CompileStatic
    static class InstallerImpl implements Installer {
        final Property<org.jreleaser.model.api.packagers.WingetPackager.Installer.Type> type
        final Property<org.jreleaser.model.api.packagers.WingetPackager.Installer.Scope> scope
        final SetProperty<org.jreleaser.model.api.packagers.WingetPackager.Installer.Mode> modes
        final Property<org.jreleaser.model.api.packagers.WingetPackager.Installer.UpgradeBehavior> upgradeBehavior
        final Property<String> command
        final DependenciesImpl dependencies

        @Inject
        InstallerImpl(ObjectFactory objects) {
            type = objects.property(org.jreleaser.model.api.packagers.WingetPackager.Installer.Type).convention(Providers.<org.jreleaser.model.api.packagers.WingetPackager.Installer.Type> notDefined())
            scope = objects.property(org.jreleaser.model.api.packagers.WingetPackager.Installer.Scope).convention(Providers.<org.jreleaser.model.api.packagers.WingetPackager.Installer.Scope> notDefined())
            upgradeBehavior = objects.property(org.jreleaser.model.api.packagers.WingetPackager.Installer.UpgradeBehavior).convention(Providers.<org.jreleaser.model.api.packagers.WingetPackager.Installer.UpgradeBehavior> notDefined())
            command = objects.property(String).convention(Providers.<String> notDefined())
            modes = objects.setProperty(org.jreleaser.model.api.packagers.WingetPackager.Installer.Mode).convention(Providers.<Set<org.jreleaser.model.api.packagers.WingetPackager.Installer.Mode>> notDefined())
            dependencies = objects.newInstance(DependenciesImpl, objects)
        }

        @Override
        void setType(String str) {
            if (isNotBlank(str)) {
                type.set(org.jreleaser.model.api.packagers.WingetPackager.Installer.Type.of(str.trim()))
            }
        }

        @Override
        void setScope(String str) {
            if (isNotBlank(str)) {
                scope.set(org.jreleaser.model.api.packagers.WingetPackager.Installer.Scope.of(str.trim()))
            }
        }

        @Override
        void setUpgradeBehavior(String str) {
            if (isNotBlank(str)) {
                upgradeBehavior.set(org.jreleaser.model.api.packagers.WingetPackager.Installer.UpgradeBehavior.of(str.trim()))
            }
        }

        @Override
        void dependencies(Action<? super Dependencies> action) {
            action.execute(dependencies)
        }

        @Override
        @CompileDynamic
        void dependencies(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Dependencies) Closure<Void> action) {
            ConfigureUtil.configure(action, dependencies)
        }

        @Override
        void mode(String str) {
            if (isNotBlank(str)) {
                modes.add(org.jreleaser.model.api.packagers.WingetPackager.Installer.Mode.of(str.trim()))
            }
        }

        @Internal
        boolean isSet() {
            type.present ||
                scope.present ||
                modes.present ||
                upgradeBehavior.present ||
                command.present
        }

        org.jreleaser.model.internal.packagers.WingetPackager.Installer toModel() {
            org.jreleaser.model.internal.packagers.WingetPackager.Installer p = new org.jreleaser.model.internal.packagers.WingetPackager.Installer()
            if (type.present) p.type = type.get()
            if (scope.present) p.scope = scope.get()
            if (upgradeBehavior.present) p.upgradeBehavior = upgradeBehavior.get()
            if (command.present) p.command = command.get()
            p.modes = (Set<org.jreleaser.model.api.packagers.WingetPackager.Installer.Mode>) modes.getOrElse([] as Set<org.jreleaser.model.api.packagers.WingetPackager.Installer.Mode>)
            p.dependencies = dependencies.toModel()
            p
        }
    }

    @CompileStatic
    static class DependenciesImpl implements Dependencies {
        final SetProperty<String> windowsFeatures
        final SetProperty<String> windowsLibraries
        final SetProperty<String> externalDependencies

        private final NamedDomainObjectContainer<PackageDependencyImpl> packageDependencies

        @Inject
        DependenciesImpl(ObjectFactory objects) {
            windowsFeatures = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
            windowsLibraries = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
            externalDependencies = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())

            packageDependencies = objects.domainObjectContainer(PackageDependencyImpl, new NamedDomainObjectFactory<PackageDependencyImpl>() {
                @Override
                PackageDependencyImpl create(String name) {
                    objects.newInstance(PackageDependencyImpl, objects)
                }
            })
        }

        @Internal
        boolean isSet() {
            windowsFeatures.present ||
                windowsLibraries.present ||
                externalDependencies.present ||
                packageDependencies.empty
        }

        @Override
        void windowsFeature(String str) {
            if (isNotBlank(str)) {
                windowsFeatures.add(str.trim())
            }
        }

        @Override
        void windowsLibrary(String str) {
            if (isNotBlank(str)) {
                windowsLibraries.add(str.trim())
            }
        }

        @Override
        void externalDependency(String str) {
            if (isNotBlank(str)) {
                externalDependencies.add(str.trim())
            }
        }

        @Override
        void packageDependency(Action<? super PackageDependency> action) {
            action.execute(packageDependencies.maybeCreate("packageDependency-${packageDependencies.size()}".toString()))
        }

        @Override
        @CompileDynamic
        void packageDependency(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PackageDependency) Closure<Void> action) {
            ConfigureUtil.configure(action, packageDependencies.maybeCreate("packageDependency-${packageDependencies.size()}".toString()))
        }

        org.jreleaser.model.internal.packagers.WingetPackager.Dependencies toModel() {
            org.jreleaser.model.internal.packagers.WingetPackager.Dependencies p = new org.jreleaser.model.internal.packagers.WingetPackager.Dependencies()
            p.windowsFeatures = (Set<String>) windowsFeatures.getOrElse([] as Set<String>)
            p.windowsLibraries = (Set<String>) windowsLibraries.getOrElse([] as Set<String>)
            p.externalDependencies = (Set<String>) externalDependencies.getOrElse([] as Set<String>)
            for (PackageDependencyImpl dependency : packageDependencies) {
                p.addPackageDependency(dependency.toModel())
            }
            p
        }
    }

    @CompileStatic
    static class PackageDependencyImpl implements PackageDependency {
        final Property<String> packageIdentifier
        final Property<String> minimumVersion

        @Inject
        PackageDependencyImpl(ObjectFactory objects) {
            packageIdentifier = objects.property(String).convention(Providers.<String> notDefined())
            minimumVersion = objects.property(String).convention(Providers.<String> notDefined())
        }

        @Internal
        boolean isSet() {
            packageIdentifier.present ||
                minimumVersion.present
        }

        org.jreleaser.model.internal.packagers.WingetPackager.PackageDependency toModel() {
            org.jreleaser.model.internal.packagers.WingetPackager.PackageDependency p = new org.jreleaser.model.internal.packagers.WingetPackager.PackageDependency()
            if (packageIdentifier.present) p.packageIdentifier = packageIdentifier.get()
            if (minimumVersion.present) p.minimumVersion = minimumVersion.get()
            p
        }
    }
}
