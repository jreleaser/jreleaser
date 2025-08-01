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
package org.jreleaser.gradle.plugin.dsl.assemble

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.common.Artifact

/**
 *
 * @author Andres Almiray
 * @since 0.10.0
 */
@CompileStatic
interface JpackageAssembler extends JavaAssembler {
    Property<String> getJlink()

    Property<Boolean> getAttachPlatform()

    Property<Boolean> getVerbose()

    void runtimeImage(Action<? super Artifact> action)

    void applicationPackage(Action<? super ApplicationPackage> action)

    void launcher(Action<? super Launcher> action)

    void linux(Action<? super Linux> action)

    void windows(Action<? super Windows> action)

    void osx(Action<? super Osx> action)

    @CompileStatic
    interface ApplicationPackage {
        ListProperty<String> getFileAssociations()

        Property<String> getAppName()

        Property<String> getAppVersion()

        Property<String> getVendor()

        Property<String> getCopyright()

        Property<String> getLicenseFile()
    }

    @CompileStatic
    interface Launcher {
        ListProperty<String> getArguments()

        ListProperty<String> getJavaOptions()

        ListProperty<String> getLaunchers()
    }

    @CompileStatic
    interface PlatformPackager {
        Property<String> getAppName()

        RegularFileProperty getIcon()

        void setIcon(String icon)

        ListProperty<String> getTypes()

        Property<String> getInstallDir()

        Property<String> getResourceDir()

        Property<Boolean> getLauncherAsService()

        void jdk(Action<? super Artifact> action)
    }

    @CompileStatic
    interface Linux extends PlatformPackager {
        ListProperty<String> getPackageDeps()

        Property<String> getPackageName()

        Property<String> getMaintainer()

        Property<String> getMenuGroup()

        Property<String> getLicense()

        Property<String> getAppRelease()

        Property<String> getAppCategory()

        Property<Boolean> getShortcut()
    }

    @CompileStatic
    interface Windows extends PlatformPackager {
        Property<Boolean> getConsole()

        Property<Boolean> getDirChooser()

        Property<Boolean> getMenu()

        Property<Boolean> getPerUserInstall()

        Property<Boolean> getShortcut()

        Property<Boolean> getShortcutPrompt()

        Property<String> getMenuGroup()

        Property<String> getUpgradeUuid()

        Property<String> getHelpUrl()

        Property<String> getUpdateUrl()
    }

    @CompileStatic
    interface Osx extends PlatformPackager {
        Property<String> getPackageIdentifier()

        Property<String> getPackageName()

        Property<String> getPackageSigningPrefix()

        Property<String> getSigningKeychain()

        Property<String> getSigningKeyUsername()

        Property<Boolean> getSign()
    }
}