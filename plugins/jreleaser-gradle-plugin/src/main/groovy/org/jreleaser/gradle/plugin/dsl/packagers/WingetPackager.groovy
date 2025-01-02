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
package org.jreleaser.gradle.plugin.dsl.packagers

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

/**
 *
 * @author Andres Almiray
 * @since 1.5.0
 */
@CompileStatic
interface WingetPackager extends RepositoryPackager {
    Property<String> getDefaultLocale()

    Property<String> getAuthor()

    Property<String> getMoniker()

    ListProperty<String> getTags()

    Property<String> getMinimumOsVersion()

    Property<String> getProductCode()

    Tap getRepository()

    Package getWingetPackage()

    Publisher getPublisher()

    Installer getInstaller()

    void repository(Action<? super Tap> action)

    void wingetPackage(Action<? super Package> action)

    void publisher(Action<? super Publisher> action)

    void installer(Action<? super Installer> action)

    void repository(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Tap) Closure<Void> action)

    void wingetPackage(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Package) Closure<Void> action)

    void publisher(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Publisher) Closure<Void> action)

    void installer(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Installer) Closure<Void> action)

    void tag(String tag)

    interface Package {
        Property<String> getIdentifier()

        Property<String> getVersion()

        Property<String> getName()

        Property<String> getUrl()
    }

    interface Publisher {
        Property<String> getName()

        Property<String> getUrl()

        Property<String> getSupportUrl()
    }

    interface Installer {
        Property<org.jreleaser.model.api.packagers.WingetPackager.Installer.Type> getType()

        Property<org.jreleaser.model.api.packagers.WingetPackager.Installer.Scope> getScope()

        SetProperty<org.jreleaser.model.api.packagers.WingetPackager.Installer.Mode> getModes()

        Property<org.jreleaser.model.api.packagers.WingetPackager.Installer.UpgradeBehavior> getUpgradeBehavior()

        Property<String> getCommand()

        void setType(String str)

        void setScope(String str)

        void mode(String str)

        void setUpgradeBehavior(String str)

        void dependencies(Action<? super Dependencies> action)

        void dependencies(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Dependencies) Closure<Void> action)
    }

    interface Dependencies {
        SetProperty<String> getWindowsFeatures()

        SetProperty<String> getWindowsLibraries()

        SetProperty<String> getExternalDependencies()

        void windowsFeature(String str)

        void windowsLibrary(String str)

        void externalDependency(String str)

        void packageDependency(Action<? super PackageDependency> action)

        void packageDependency(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = PackageDependency) Closure<Void> action)
    }

    interface PackageDependency {
        Property<String> getPackageIdentifier()

        Property<String> getMinimumVersion()
    }
}