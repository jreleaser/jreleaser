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
package org.jreleaser.model.api.assemble;

import org.jreleaser.model.api.common.Artifact;
import org.jreleaser.model.api.common.Domain;

import java.util.List;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public interface JpackageAssembler extends Assembler, JavaAssembler {
    String TYPE = "jpackage";

    String getJlink();

    boolean isAttachPlatform();

    boolean isVerbose();

    Set<? extends Artifact> getRuntimeImages();

    ApplicationPackage getApplicationPackage();

    Launcher getLauncher();

    Linux getLinux();

    Windows getWindows();

    Osx getOsx();

    Set<? extends PlatformPackager> getPlatformPackagers();

    interface ApplicationPackage extends Domain {
        String getAppName();

        String getAppVersion();

        String getVendor();

        String getCopyright();

        List<String> getFileAssociations();

        String getLicenseFile();
    }

    interface PlatformPackager extends Domain {
        String getAppName();

        String getIcon();

        String getPlatform();

        boolean isEnabled();

        Artifact getJdk();

        List<String> getTypes();

        String getInstallDir();

        String getResourceDir();
    }

    interface Launcher extends Domain {
        List<String> getLaunchers();

        List<String> getArguments();

        List<String> getJavaOptions();
    }

    interface Linux extends PlatformPackager {
        List<String> getPackageDeps();

        String getPackageName();

        String getMaintainer();

        String getMenuGroup();

        String getLicense();

        String getAppRelease();

        String getAppCategory();

        boolean isShortcut();
    }

    interface Osx extends PlatformPackager {
        String getPackageIdentifier();

        String getPackageName();

        String getPackageSigningPrefix();

        String getSigningKeychain();

        String getSigningKeyUsername();

        boolean isSign();
    }

    interface Windows extends PlatformPackager {
        boolean isConsole();

        boolean isDirChooser();

        boolean isMenu();

        boolean isPerUserInstall();

        boolean isShortcut();

        String getMenuGroup();

        String getUpgradeUuid();
    }
}
