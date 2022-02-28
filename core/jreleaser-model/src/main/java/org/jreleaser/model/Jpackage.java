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
package org.jreleaser.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.util.CollectionUtils;
import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.jreleaser.util.JReleaserOutput.nag;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public class Jpackage extends AbstractJavaAssembler {
    public static final String TYPE = "jpackage";

    private final Set<Artifact> runtimeImages = new LinkedHashSet<>();

    private final ApplicationPackage applicationPackage = new ApplicationPackage();
    private final Launcher launcher = new Launcher();
    private final Linux linux = new Linux();
    private final Windows windows = new Windows();
    private final Osx osx = new Osx();

    private String jlink;
    private Boolean attachPlatform;

    public Jpackage() {
        super(TYPE);
    }

    @Override
    public Distribution.DistributionType getDistributionType() {
        return Distribution.DistributionType.NATIVE_PACKAGE;
    }

    void setAll(Jpackage jpackage) {
        super.setAll(jpackage);
        this.jlink = jpackage.jlink;
        this.attachPlatform = jpackage.attachPlatform;
        setRuntimeImages(jpackage.runtimeImages);
        setApplicationPackage(jpackage.applicationPackage);
        setLauncher(jpackage.launcher);
        setLinux(jpackage.linux);
        setWindows(jpackage.windows);
        setOsx(jpackage.osx);
    }

    public String getJlink() {
        return jlink;
    }

    public void setJlink(String jlink) {
        this.jlink = jlink;
    }

    public void setModuleName(String moduleName) {
        nag("jlink.moduleName has been deprecated since 1.0.0-M3 and will be removed in the future. Use jlink.java.mainModule instead");
        this.java.setMainModule(moduleName);
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

    public Set<Artifact> getRuntimeImages() {
        return Artifact.sortArtifacts(runtimeImages);
    }

    public void setRuntimeImages(Set<Artifact> runtimeImages) {
        this.runtimeImages.clear();
        this.runtimeImages.addAll(runtimeImages);
    }

    public void addRuntimeImages(Set<Artifact> runtimeImages) {
        this.runtimeImages.addAll(runtimeImages);
    }

    public void addRuntimeImage(Artifact jdk) {
        if (null != jdk) {
            this.runtimeImages.add(jdk);
        }
    }

    public Optional<Artifact> findRuntimeImageByPlatform(String platform) {
        return runtimeImages.stream()
            .filter(ri -> ri.getPlatform().equals(platform))
            .findFirst();
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

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("jlink", jlink);
        props.put("attachPlatform", isAttachPlatform());
        Map<String, Map<String, Object>> mapped = new LinkedHashMap<>();
        int i = 0;
        for (Artifact runtimeImage : getRuntimeImages()) {
            mapped.put("runtimeImage " + (i++), runtimeImage.asMap(full));
        }
        props.put("runtimeImages", mapped);
        if (launcher.isSet()) props.put("launcher", launcher.asMap(full));
        props.put("applicationPackage", applicationPackage.asMap(full));
        props.putAll(linux.asMap(full));
        props.putAll(osx.asMap(full));
        props.putAll(windows.asMap(full));
    }

    public PlatformPackager getResolvedPlatformPackager() {
        String currentPlatform = PlatformUtils.getCurrentFull();
        if (PlatformUtils.isMac(currentPlatform)) {
            return getOsx();
        } else if (PlatformUtils.isWindows(currentPlatform)) {
            return getWindows();
        }
        return getLinux();
    }

    public Set<PlatformPackager> getPlatformPackagers() {
        return CollectionUtils.newSet(osx, linux, windows);
    }

    public interface PlatformPackager extends Domain {
        String getAppName();

        void setAppName(String appName);

        String getResolvedAppName(JReleaserContext context, Jpackage jpackage);

        String getIcon();

        void setIcon(String icon);

        List<String> getValidTypes();

        String getPlatform();

        boolean isEnabled();

        void enable();

        void disable();

        Artifact getJdk();

        void setJdk(Artifact jdk);

        List<String> getTypes();

        void setTypes(List<String> types);

        String getInstallDir();

        void setInstallDir(String installDir);

        String getResourceDir();

        void setResourceDir(String resourceDir);
    }

    public static class ApplicationPackage implements Domain {
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

        public String getResolvedAppVersion(JReleaserContext context, Jpackage jpackage) {
            Map<String, Object> props = context.getModel().props();
            props.putAll(jpackage.props());
            return resolveTemplate(appVersion, props);
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

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("appName", appName);
            props.put("appVersion", appVersion);
            props.put("copyright", copyright);
            props.put("vendor", vendor);
            props.put("licenseFile", licenseFile);
            props.put("fileAssociations", fileAssociations);
            return props;
        }
    }

    public static class Launcher implements Domain {
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

        public void addLaunchers(List<String> launchers) {
            this.launchers.addAll(launchers);
        }

        public List<String> getArguments() {
            return arguments;
        }

        public void setArguments(List<String> arguments) {
            this.arguments.clear();
            this.arguments.addAll(arguments);
        }

        public void addArguments(List<String> arguments) {
            this.arguments.addAll(arguments);
        }

        public List<String> getJavaOptions() {
            return javaOptions;
        }

        public void setJavaOptions(List<String> javaOptions) {
            this.javaOptions.clear();
            this.javaOptions.addAll(javaOptions);
        }

        public void addJavaOptions(List<String> javaOptions) {
            this.javaOptions.addAll(javaOptions);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("arguments", arguments);
            props.put("javaOptions", javaOptions);
            props.put("launchers", launchers);
            return props;
        }
    }

    private static abstract class AbstractPlatformPackager implements PlatformPackager {
        private final Artifact jdk = new Artifact();
        private final List<String> types = new ArrayList<>();
        private final List<String> validTypes = new ArrayList<>();
        private final String platform;

        @JsonIgnore
        private boolean enabled;
        private String appName;
        private String icon;
        private String installDir;
        private String resourceDir;

        protected AbstractPlatformPackager(String platform, List<String> validTypes) {
            this.platform = platform;
            this.validTypes.addAll(validTypes);
        }

        void setAll(AbstractPlatformPackager packager) {
            this.icon = packager.icon;
            this.appName = packager.appName;
            this.enabled = packager.enabled;
            this.installDir = packager.installDir;
            this.resourceDir = packager.resourceDir;
            setJdk(packager.jdk);
            setTypes(packager.types);
        }

        @Override
        public String getResolvedAppName(JReleaserContext context, Jpackage jpackage) {
            Map<String, Object> props = context.getModel().props();
            props.putAll(jpackage.props());
            return resolveTemplate(appName, props);
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
        public List<String> getValidTypes() {
            return validTypes;
        }

        @Override
        public String getPlatform() {
            return platform;
        }

        @Override
        public String getResourceDir() {
            return resourceDir;
        }

        @Override
        public void setResourceDir(String resourceDir) {
            this.resourceDir = resourceDir;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void enable() {
            this.enabled = true;
        }

        @Override
        public void disable() {
            this.enabled = false;
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
        public Map<String, Object> asMap(boolean full) {
            if (!full && !isEnabled()) return Collections.emptyMap();

            Map<String, Object> props = new LinkedHashMap<>();
            props.put("enabled", isEnabled());
            props.put("appName", appName);
            props.put("icon", icon);
            props.put("resourceDir", resourceDir);
            props.put("types", types);
            props.put("jdk", jdk.asMap(full));
            props.put("installDir", installDir);
            asMap(full, props);

            Map<String, Object> map = new LinkedHashMap<>();
            map.put(platform, props);
            return map;
        }

        protected abstract void asMap(boolean full, Map<String, Object> props);
    }

    public static class Linux extends AbstractPlatformPackager {
        private final List<String> packageDeps = new ArrayList<>();
        private String packageName;
        private String maintainer;
        private String menuGroup;
        private String license;
        private String appRelease;
        private String appCategory;
        private boolean shortcut;

        public Linux() {
            super("linux", Arrays.asList("deb", "rpm"));
        }

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
            return shortcut;
        }

        public void setShortcut(boolean shortcut) {
            this.shortcut = shortcut;
        }

        @Override
        protected void asMap(boolean full, Map<String, Object> props) {
            props.put("packageName", packageName);
            props.put("maintainer", maintainer);
            props.put("menuGroup", menuGroup);
            props.put("license", license);
            props.put("appRelease", appRelease);
            props.put("appCategory", appCategory);
            props.put("shortcut", shortcut);
            props.put("packageDeps", packageDeps);
        }
    }

    public static class Windows extends AbstractPlatformPackager {
        private boolean console;
        private boolean dirChooser;
        private boolean menu;
        private boolean perUserInstall;
        private boolean shortcut;
        private String menuGroup;
        private String upgradeUuid;

        public Windows() {
            super("windows", Arrays.asList("msi", "exe"));
        }

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
            return console;
        }

        public void setConsole(boolean console) {
            this.console = console;
        }

        public boolean isDirChooser() {
            return dirChooser;
        }

        public void setDirChooser(boolean dirChooser) {
            this.dirChooser = dirChooser;
        }

        public boolean isMenu() {
            return menu;
        }

        public void setMenu(boolean menu) {
            this.menu = menu;
        }

        public boolean isPerUserInstall() {
            return perUserInstall;
        }

        public void setPerUserInstall(boolean perUserInstall) {
            this.perUserInstall = perUserInstall;
        }

        public boolean isShortcut() {
            return shortcut;
        }

        public void setShortcut(boolean shortcut) {
            this.shortcut = shortcut;
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

        @Override
        protected void asMap(boolean full, Map<String, Object> props) {
            props.put("console", console);
            props.put("dirChooser", dirChooser);
            props.put("menu", menu);
            props.put("perUserInstall", perUserInstall);
            props.put("shortcut", shortcut);
            props.put("menuGroup", menuGroup);
            props.put("upgradeUuid", upgradeUuid);
        }
    }

    public static class Osx extends AbstractPlatformPackager {
        private String packageIdentifier;
        private String packageName;
        private String packageSigningPrefix;
        private String signingKeychain;
        private String signingKeyUsername;
        private boolean sign;

        public Osx() {
            super("osx", Arrays.asList("dmg", "pkg"));
        }

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
            return sign;
        }

        public void setSign(boolean sign) {
            this.sign = sign;
        }

        @Override
        protected void asMap(boolean full, Map<String, Object> props) {
            props.put("packageIdentifier", packageIdentifier);
            props.put("packageName", packageName);
            props.put("packageSigningPrefix", packageSigningPrefix);
            props.put("signingKeychain", signingKeychain);
            props.put("signingKeyUsername", signingKeyUsername);
            props.put("sign", sign);
        }
    }
}
