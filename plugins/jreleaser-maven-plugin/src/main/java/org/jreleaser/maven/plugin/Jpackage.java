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
package org.jreleaser.maven.plugin;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public class Jpackage extends AbstractJavaAssembler {
    private final Set<Artifact> runtimeImages = new LinkedHashSet<>();
    private final ApplicationPackage applicationPackage = new ApplicationPackage();
    private final Launcher launcher = new Launcher();
    private final Linux linux = new Linux();
    private final Windows windows = new Windows();
    private final Osx osx = new Osx();

    private String jlink;
    private Boolean attachPlatform;
    private Boolean verbose;

    public String getJlink() {
        return jlink;
    }

    public void setJlink(String jlink) {
        this.jlink = jlink;
    }

    public boolean isAttachPlatformSet() {
        return attachPlatform != null;
    }

    public boolean isAttachPlatform() {
        return attachPlatform != null && attachPlatform;
    }

    public void setAttachPlatform(Boolean attachPlatform) {
        this.attachPlatform = attachPlatform;
    }

    public boolean isVerboseSet() {
        return verbose != null;
    }

    public boolean isVerbose() {
        return verbose != null && verbose;
    }

    public void setVerbose(Boolean verbose) {
        this.verbose = verbose;
    }

    public Set<Artifact> getRuntimeImages() {
        return runtimeImages;
    }

    public void setRuntimeImages(Set<Artifact> runtimeImages) {
        this.runtimeImages.clear();
        this.runtimeImages.addAll(runtimeImages);
    }

    public ApplicationPackage getApplicationPackage() {
        return applicationPackage;
    }

    public void setApplicationPackage(ApplicationPackage applicationPackage) {
        this.applicationPackage.setAll(applicationPackage);
    }

    public Launcher getLauncher() {
        return launcher;
    }

    public void setLauncher(Launcher launcher) {
        this.launcher.setAll(launcher);
    }

    public Linux getLinux() {
        return linux;
    }

    public void setLinux(Linux linux) {
        this.linux.setAll(linux);
    }

    public Windows getWindows() {
        return windows;
    }

    public void setWindows(Windows windows) {
        this.windows.setAll(windows);
    }

    public Osx getOsx() {
        return osx;
    }

    public void setOsx(Osx osx) {
        this.osx.setAll(osx);
    }

    public interface PlatformPackager {
        String getAppName();

        void setAppName(String appName);

        String getIcon();

        void setIcon(String icon);

        Artifact getJdk();

        void setJdk(Artifact jdk);

        List<String> getTypes();

        void setTypes(List<String> types);

        String getInstallDir();

        void setInstallDir(String installDir);

        String getResourceDir();

        void setResourceDir(String resourceDir);
    }

    public static class ApplicationPackage {
        private final List<String> fileAssociations = new ArrayList<>();

        private String appName;
        private String appVersion;
        private String vendor;
        private String copyright;
        private String licenseFile;

        void setAll(ApplicationPackage applicationPackage) {
            this.appName = applicationPackage.appName;
            this.appVersion = applicationPackage.appVersion;
            this.vendor = applicationPackage.vendor;
            this.copyright = applicationPackage.copyright;
            this.licenseFile = applicationPackage.licenseFile;
            setFileAssociations(applicationPackage.fileAssociations);
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }

        public String getVendor() {
            return vendor;
        }

        public void setVendor(String vendor) {
            this.vendor = vendor;
        }

        public String getCopyright() {
            return copyright;
        }

        public void setCopyright(String copyright) {
            this.copyright = copyright;
        }

        public List<String> getFileAssociations() {
            return fileAssociations;
        }

        public void setFileAssociations(List<String> fileAssociations) {
            this.fileAssociations.clear();
            this.fileAssociations.addAll(fileAssociations);
        }

        public String getLicenseFile() {
            return licenseFile;
        }

        public void setLicenseFile(String licenseFile) {
            this.licenseFile = licenseFile;
        }
    }

    public static class Launcher {
        private final List<String> arguments = new ArrayList<>();
        private final List<String> javaOptions = new ArrayList<>();
        private final List<String> launchers = new ArrayList<>();

        void setAll(Launcher launcher) {
            setArguments(launcher.arguments);
            setJavaOptions(launcher.javaOptions);
            setLaunchers(launcher.launchers);
        }

        public boolean isSet() {
            return !arguments.isEmpty() ||
                !javaOptions.isEmpty() ||
                !launchers.isEmpty();
        }

        public List<String> getLaunchers() {
            return launchers;
        }

        public void setLaunchers(List<String> launchers) {
            this.launchers.clear();
            this.launchers.addAll(launchers);
        }

        public List<String> getArguments() {
            return arguments;
        }

        public void setArguments(List<String> arguments) {
            this.arguments.clear();
            this.arguments.addAll(arguments);
        }

        public List<String> getJavaOptions() {
            return javaOptions;
        }

        public void setJavaOptions(List<String> javaOptions) {
            this.javaOptions.clear();
            this.javaOptions.addAll(javaOptions);
        }
    }

    private static abstract class AbstractPlatformPackager implements PlatformPackager {
        private final Artifact jdk = new Artifact();
        private final List<String> types = new ArrayList<>();

        private String appName;
        private String icon;
        private String installDir;
        private String resourceDir;

        void setAll(AbstractPlatformPackager packager) {
            this.appName = packager.appName;
            this.icon = packager.icon;
            this.installDir = packager.installDir;
            this.resourceDir = packager.resourceDir;
            setJdk(packager.jdk);
            setTypes(packager.types);
        }

        @Override
        public String getAppName() {
            return appName;
        }

        @Override
        public void setAppName(String appName) {
            this.appName = appName;
        }

        @Override
        public String getIcon() {
            return icon;
        }

        @Override
        public void setIcon(String icon) {
            this.icon = icon;
        }

        @Override
        public Artifact getJdk() {
            return jdk;
        }

        @Override
        public void setJdk(Artifact jdk) {
            this.jdk.setAll(jdk);
        }

        @Override
        public List<String> getTypes() {
            return types;
        }

        @Override
        public void setTypes(List<String> types) {
            this.types.clear();
            this.types.addAll(types);
        }

        @Override
        public String getInstallDir() {
            return installDir;
        }

        @Override
        public void setInstallDir(String installDir) {
            this.installDir = installDir;
        }

        @Override
        public String getResourceDir() {
            return resourceDir;
        }

        @Override
        public void setResourceDir(String resourceDir) {
            this.resourceDir = resourceDir;
        }
    }

    public static class Linux extends AbstractPlatformPackager {
        private final List<String> packageDeps = new ArrayList<>();
        private String packageName;
        private String maintainer;
        private String menuGroup;
        private String license;
        private String appRelease;
        private String appCategory;
        private Boolean shortcut;

        void setAll(Linux packager) {
            super.setAll(packager);
            this.packageName = packager.packageName;
            this.maintainer = packager.maintainer;
            this.menuGroup = packager.menuGroup;
            this.license = packager.license;
            this.appRelease = packager.appRelease;
            this.appCategory = packager.appCategory;
            this.shortcut = packager.shortcut;
            setPackageDeps(packager.packageDeps);
        }

        public List<String> getPackageDeps() {
            return packageDeps;
        }

        public void setPackageDeps(List<String> packageDeps) {
            this.packageDeps.clear();
            this.packageDeps.addAll(packageDeps);
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getMaintainer() {
            return maintainer;
        }

        public void setMaintainer(String maintainer) {
            this.maintainer = maintainer;
        }

        public String getMenuGroup() {
            return menuGroup;
        }

        public void setMenuGroup(String menuGroup) {
            this.menuGroup = menuGroup;
        }

        public String getLicense() {
            return license;
        }

        public void setLicense(String license) {
            this.license = license;
        }

        public String getAppRelease() {
            return appRelease;
        }

        public void setAppRelease(String appRelease) {
            this.appRelease = appRelease;
        }

        public String getAppCategory() {
            return appCategory;
        }

        public void setAppCategory(String appCategory) {
            this.appCategory = appCategory;
        }

        public boolean isShortcut() {
            return shortcut != null && shortcut;
        }

        public void setShortcut(boolean shortcut) {
            this.shortcut = shortcut;
        }

        public boolean isShortcutSet() {
            return shortcut != null;
        }
    }

    public static class Windows extends AbstractPlatformPackager {
        private Boolean console;
        private Boolean dirChooser;
        private Boolean menu;
        private Boolean perUserInstall;
        private Boolean shortcut;
        private String menuGroup;
        private String upgradeUuid;

        void setAll(Windows packager) {
            super.setAll(packager);
            this.console = packager.console;
            this.dirChooser = packager.dirChooser;
            this.menu = packager.menu;
            this.perUserInstall = packager.perUserInstall;
            this.shortcut = packager.shortcut;
            this.menuGroup = packager.menuGroup;
            this.upgradeUuid = packager.upgradeUuid;
        }

        public boolean isConsole() {
            return console != null && console;
        }

        public void setConsole(Boolean console) {
            this.console = console;
        }

        public boolean isConsoleSet() {
            return console != null;
        }

        public boolean isDirChooser() {
            return dirChooser != null && dirChooser;
        }

        public void setDirChooser(Boolean dirChooser) {
            this.dirChooser = dirChooser;
        }

        public boolean isDirChooserSet() {
            return dirChooser != null;
        }

        public boolean isMenu() {
            return menu != null && menu;
        }

        public void setMenu(Boolean menu) {
            this.menu = menu;
        }

        public boolean isMenuSet() {
            return menu != null;
        }

        public boolean isPerUserInstall() {
            return perUserInstall != null && perUserInstall;
        }

        public void setPerUserInstall(Boolean perUserInstall) {
            this.perUserInstall = perUserInstall;
        }

        public boolean isPerUserInstallSet() {
            return perUserInstall != null;
        }

        public boolean isShortcut() {
            return shortcut != null && shortcut;
        }

        public void setShortcut(Boolean shortcut) {
            this.shortcut = shortcut;
        }

        public boolean isShortcutSet() {
            return shortcut != null;
        }

        public String getMenuGroup() {
            return menuGroup;
        }

        public void setMenuGroup(String menuGroup) {
            this.menuGroup = menuGroup;
        }

        public String getUpgradeUuid() {
            return upgradeUuid;
        }

        public void setUpgradeUuid(String upgradeUuid) {
            this.upgradeUuid = upgradeUuid;
        }
    }

    public static class Osx extends AbstractPlatformPackager {
        private String packageIdentifier;
        private String packageName;
        private String packageSigningPrefix;
        private String signingKeychain;
        private String signingKeyUsername;
        private Boolean sign;

        void setAll(Osx packager) {
            super.setAll(packager);
            this.packageIdentifier = packager.packageIdentifier;
            this.packageName = packager.packageName;
            this.packageSigningPrefix = packager.packageSigningPrefix;
            this.signingKeychain = packager.signingKeychain;
            this.signingKeyUsername = packager.signingKeyUsername;
            this.sign = packager.sign;
        }

        public String getPackageIdentifier() {
            return packageIdentifier;
        }

        public void setPackageIdentifier(String packageIdentifier) {
            this.packageIdentifier = packageIdentifier;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getPackageSigningPrefix() {
            return packageSigningPrefix;
        }

        public void setPackageSigningPrefix(String packageSigningPrefix) {
            this.packageSigningPrefix = packageSigningPrefix;
        }

        public String getSigningKeychain() {
            return signingKeychain;
        }

        public void setSigningKeychain(String signingKeychain) {
            this.signingKeychain = signingKeychain;
        }

        public String getSigningKeyUsername() {
            return signingKeyUsername;
        }

        public void setSigningKeyUsername(String signingKeyUsername) {
            this.signingKeyUsername = signingKeyUsername;
        }

        public boolean isSign() {
            return sign != null && sign;
        }

        public void setSign(Boolean sign) {
            this.sign = sign;
        }

        public boolean isSignSet() {
            return sign != null;
        }
    }
}
