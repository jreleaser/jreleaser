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

import static java.util.Collections.unmodifiableSet;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public class Jpackage extends AbstractJavaAssembler<Jpackage> {
    public static final String TYPE = "jpackage";

    private final Set<Artifact> runtimeImages = new LinkedHashSet<>();

    private final ApplicationPackage applicationPackage = new ApplicationPackage();
    private final Launcher launcher = new Launcher();
    private final Linux linux = new Linux();
    private final Windows windows = new Windows();
    private final Osx osx = new Osx();

    private String jlink;
    private Boolean attachPlatform;
    private Boolean verbose;

    public Jpackage() {
        super(TYPE);
    }

    @Override
    public Distribution.DistributionType getDistributionType() {
        return Distribution.DistributionType.NATIVE_PACKAGE;
    }

    @Override
    public void freeze() {
        super.freeze();
        runtimeImages.forEach(Artifact::freeze);
        applicationPackage.freeze();
        launcher.freeze();
        linux.freeze();
        windows.freeze();
        osx.freeze();
    }

    @Override
    public void merge(Jpackage jpackage) {
        freezeCheck();
        super.merge(jpackage);
        this.jlink = merge(this.jlink, jpackage.jlink);
        this.attachPlatform = merge(this.attachPlatform, jpackage.attachPlatform);
        this.verbose = merge(this.verbose, jpackage.verbose);
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
        freezeCheck();
        this.jlink = jlink;
    }

    public boolean isAttachPlatformSet() {
        return attachPlatform != null;
    }

    public boolean isAttachPlatform() {
        return attachPlatform != null && attachPlatform;
    }

    public void setAttachPlatform(Boolean attachPlatform) {
        freezeCheck();
        this.attachPlatform = attachPlatform;
    }

    public boolean isVerboseSet() {
        return verbose != null;
    }

    public boolean isVerbose() {
        return verbose != null && verbose;
    }

    public void setVerbose(Boolean verbose) {
        freezeCheck();
        this.verbose = verbose;
    }

    public Set<Artifact> getRuntimeImages() {
        return freezeWrap(Artifact.sortArtifacts(runtimeImages));
    }

    public void setRuntimeImages(Set<Artifact> runtimeImages) {
        freezeCheck();
        this.runtimeImages.clear();
        this.runtimeImages.addAll(runtimeImages);
    }

    public void addRuntimeImage(Artifact jdk) {
        freezeCheck();
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
        this.applicationPackage.merge(applicationPackage);
    }

    public Launcher getLauncher() {
        return launcher;
    }

    public void setLauncher(Launcher launcher) {
        this.launcher.merge(launcher);
    }

    public Linux getLinux() {
        return linux;
    }

    public void setLinux(Linux linux) {
        this.linux.merge(linux);
    }

    public Windows getWindows() {
        return windows;
    }

    public void setWindows(Windows windows) {
        this.windows.merge(windows);
    }

    public Osx getOsx() {
        return osx;
    }

    public void setOsx(Osx osx) {
        this.osx.merge(osx);
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
        return unmodifiableSet(setOf(osx, linux, windows));
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

    public static class ApplicationPackage extends AbstractModelObject<ApplicationPackage> implements Domain {
        private final List<String> fileAssociations = new ArrayList<>();

        private String appName;
        private String appVersion;
        private String vendor;
        private String copyright;
        private String licenseFile;

        @Override
        public void merge(ApplicationPackage applicationPackage) {
            freezeCheck();
            this.appName = this.merge(this.appName, applicationPackage.appName);
            this.appVersion = this.merge(this.appVersion, applicationPackage.appVersion);
            this.vendor = this.merge(this.vendor, applicationPackage.vendor);
            this.copyright = this.merge(this.copyright, applicationPackage.copyright);
            this.licenseFile = this.merge(this.licenseFile, applicationPackage.licenseFile);
            setFileAssociations(merge(this.fileAssociations, applicationPackage.fileAssociations));
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
            freezeCheck();
            this.appName = appName;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            freezeCheck();
            this.appVersion = appVersion;
        }

        public String getVendor() {
            return vendor;
        }

        public void setVendor(String vendor) {
            freezeCheck();
            this.vendor = vendor;
        }

        public String getCopyright() {
            return copyright;
        }

        public void setCopyright(String copyright) {
            freezeCheck();
            this.copyright = copyright;
        }

        public List<String> getFileAssociations() {
            return freezeWrap(fileAssociations);
        }

        public void setFileAssociations(List<String> fileAssociations) {
            freezeCheck();
            this.fileAssociations.clear();
            this.fileAssociations.addAll(fileAssociations);
        }

        public String getLicenseFile() {
            return licenseFile;
        }

        public void setLicenseFile(String licenseFile) {
            freezeCheck();
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

    public static class Launcher extends AbstractModelObject<Launcher> implements Domain {
        private final List<String> arguments = new ArrayList<>();
        private final List<String> javaOptions = new ArrayList<>();
        private final List<String> launchers = new ArrayList<>();

        @Override
        public void merge(Launcher launcher) {
            freezeCheck();
            setArguments(merge(this.arguments, launcher.arguments));
            setJavaOptions(merge(this.javaOptions, launcher.javaOptions));
            setLaunchers(merge(this.launchers, launcher.launchers));
        }

        public boolean isSet() {
            return !arguments.isEmpty() ||
                !javaOptions.isEmpty() ||
                !launchers.isEmpty();
        }

        public List<String> getLaunchers() {
            return freezeWrap(launchers);
        }

        public void setLaunchers(List<String> launchers) {
            freezeCheck();
            this.launchers.clear();
            this.launchers.addAll(launchers);
        }

        public void addLaunchers(List<String> launchers) {
            freezeCheck();
            this.launchers.addAll(launchers);
        }

        public List<String> getArguments() {
            return freezeWrap(arguments);
        }

        public void setArguments(List<String> arguments) {
            freezeCheck();
            this.arguments.clear();
            this.arguments.addAll(arguments);
        }

        public void addArguments(List<String> arguments) {
            freezeCheck();
            this.arguments.addAll(arguments);
        }

        public List<String> getJavaOptions() {
            return freezeWrap(javaOptions);
        }

        public void setJavaOptions(List<String> javaOptions) {
            freezeCheck();
            this.javaOptions.clear();
            this.javaOptions.addAll(javaOptions);
        }

        public void addJavaOptions(List<String> javaOptions) {
            freezeCheck();
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

    private static abstract class AbstractPlatformPackager<S extends AbstractPlatformPackager<S>> extends AbstractModelObject<S> implements PlatformPackager {
        protected final Artifact jdk = new Artifact();
        protected final List<String> types = new ArrayList<>();
        protected final List<String> validTypes = new ArrayList<>();
        protected final String platform;

        @JsonIgnore
        protected boolean enabled;
        protected String appName;
        protected String icon;
        protected String installDir;
        protected String resourceDir;

        protected AbstractPlatformPackager(String platform, List<String> validTypes) {
            this.platform = platform;
            this.validTypes.addAll(validTypes);
        }

        @Override
        public void freeze() {
            super.freeze();
            jdk.freeze();
        }

        @Override
        public void merge(S packager) {
            freezeCheck();
            this.icon = this.merge(this.icon, packager.icon);
            this.appName = this.merge(this.appName, packager.appName);
            this.enabled = this.merge(this.enabled, packager.enabled);
            this.installDir = this.merge(this.installDir, packager.installDir);
            this.resourceDir = this.merge(this.resourceDir, packager.resourceDir);
            setJdk(packager.jdk);
            setTypes(merge(this.types, packager.types));
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
            freezeCheck();
            this.appName = appName;
        }

        @Override
        public String getIcon() {
            return icon;
        }

        @Override
        public void setIcon(String icon) {
            freezeCheck();
            this.icon = icon;
        }

        @Override
        public List<String> getValidTypes() {
            return freezeWrap(validTypes);
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
            freezeCheck();
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
            this.jdk.merge(jdk);
        }

        @Override
        public List<String> getTypes() {
            return freezeWrap(types);
        }

        @Override
        public void setTypes(List<String> types) {
            freezeCheck();
            this.types.clear();
            this.types.addAll(types);
        }

        @Override
        public String getInstallDir() {
            return installDir;
        }

        @Override
        public void setInstallDir(String installDir) {
            freezeCheck();
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

    public static class Linux extends AbstractPlatformPackager<Linux> {
        private final List<String> packageDeps = new ArrayList<>();
        private String packageName;
        private String maintainer;
        private String menuGroup;
        private String license;
        private String appRelease;
        private String appCategory;
        private Boolean shortcut;

        public Linux() {
            super("linux", Arrays.asList("deb", "rpm"));
        }

        @Override
        public void merge(Linux packager) {
            freezeCheck();
            super.merge(packager);
            this.packageName = this.merge(this.packageName, packager.packageName);
            this.maintainer = this.merge(this.maintainer, packager.maintainer);
            this.menuGroup = this.merge(this.menuGroup, packager.menuGroup);
            this.license = this.merge(this.license, packager.license);
            this.appRelease = this.merge(this.appRelease, packager.appRelease);
            this.appCategory = this.merge(this.appCategory, packager.appCategory);
            this.shortcut = this.merge(this.shortcut, packager.shortcut);
            setPackageDeps(merge(this.packageDeps, packager.packageDeps));
        }

        public List<String> getPackageDeps() {
            return freezeWrap(packageDeps);
        }

        public void setPackageDeps(List<String> packageDeps) {
            freezeCheck();
            this.packageDeps.clear();
            this.packageDeps.addAll(packageDeps);
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            freezeCheck();
            this.packageName = packageName;
        }

        public String getMaintainer() {
            return maintainer;
        }

        public void setMaintainer(String maintainer) {
            freezeCheck();
            this.maintainer = maintainer;
        }

        public String getMenuGroup() {
            return menuGroup;
        }

        public void setMenuGroup(String menuGroup) {
            freezeCheck();
            this.menuGroup = menuGroup;
        }

        public String getLicense() {
            return license;
        }

        public void setLicense(String license) {
            freezeCheck();
            this.license = license;
        }

        public String getAppRelease() {
            return appRelease;
        }

        public void setAppRelease(String appRelease) {
            freezeCheck();
            this.appRelease = appRelease;
        }

        public String getAppCategory() {
            return appCategory;
        }

        public void setAppCategory(String appCategory) {
            freezeCheck();
            this.appCategory = appCategory;
        }

        public boolean isShortcut() {
            return shortcut != null && shortcut;
        }

        public void setShortcut(Boolean shortcut) {
            freezeCheck();
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

    public static class Windows extends AbstractPlatformPackager<Windows> {
        private Boolean console;
        private Boolean dirChooser;
        private Boolean menu;
        private Boolean perUserInstall;
        private Boolean shortcut;
        private String menuGroup;
        private String upgradeUuid;

        public Windows() {
            super("windows", Arrays.asList("msi", "exe"));
        }

        @Override
        public void merge(Windows packager) {
            freezeCheck();
            super.merge(packager);
            this.console = this.merge(this.console, packager.console);
            this.dirChooser = this.merge(this.dirChooser, packager.dirChooser);
            this.menu = this.merge(this.menu, packager.menu);
            this.perUserInstall = this.merge(this.perUserInstall, packager.perUserInstall);
            this.shortcut = this.merge(this.shortcut, packager.shortcut);
            this.menuGroup = this.merge(this.menuGroup, packager.menuGroup);
            this.upgradeUuid = this.merge(this.upgradeUuid, packager.upgradeUuid);
        }

        public boolean isConsole() {
            return console != null && console;
        }

        public void setConsole(Boolean console) {
            freezeCheck();
            this.console = console;
        }

        public boolean isDirChooser() {
            return dirChooser != null && dirChooser;
        }

        public void setDirChooser(Boolean dirChooser) {
            freezeCheck();
            this.dirChooser = dirChooser;
        }

        public boolean isMenu() {
            return menu != null && menu;
        }

        public void setMenu(Boolean menu) {
            freezeCheck();
            this.menu = menu;
        }

        public boolean isPerUserInstall() {
            return perUserInstall != null && perUserInstall;
        }

        public void setPerUserInstall(Boolean perUserInstall) {
            freezeCheck();
            this.perUserInstall = perUserInstall;
        }

        public boolean isShortcut() {
            return shortcut != null && shortcut;
        }

        public void setShortcut(Boolean shortcut) {
            freezeCheck();
            this.shortcut = shortcut;
        }

        public String getMenuGroup() {
            return menuGroup;
        }

        public void setMenuGroup(String menuGroup) {
            freezeCheck();
            this.menuGroup = menuGroup;
        }

        public String getUpgradeUuid() {
            return upgradeUuid;
        }

        public void setUpgradeUuid(String upgradeUuid) {
            freezeCheck();
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

    public static class Osx extends AbstractPlatformPackager<Osx> {
        private String packageIdentifier;
        private String packageName;
        private String packageSigningPrefix;
        private String signingKeychain;
        private String signingKeyUsername;
        private Boolean sign;

        public Osx() {
            super("osx", Arrays.asList("dmg", "pkg"));
        }

        @Override
        public void merge(Osx packager) {
            freezeCheck();
            super.merge(packager);
            this.packageIdentifier = this.merge(this.packageIdentifier, packager.packageIdentifier);
            this.packageName = this.merge(this.packageName, packager.packageName);
            this.packageSigningPrefix = this.merge(this.packageSigningPrefix, packager.packageSigningPrefix);
            this.signingKeychain = this.merge(this.signingKeychain, packager.signingKeychain);
            this.signingKeyUsername = this.merge(this.signingKeyUsername, packager.signingKeyUsername);
            this.sign = this.merge(this.sign, packager.sign);
        }

        public String getPackageIdentifier() {
            return packageIdentifier;
        }

        public void setPackageIdentifier(String packageIdentifier) {
            freezeCheck();
            this.packageIdentifier = packageIdentifier;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            freezeCheck();
            this.packageName = packageName;
        }

        public String getPackageSigningPrefix() {
            return packageSigningPrefix;
        }

        public void setPackageSigningPrefix(String packageSigningPrefix) {
            freezeCheck();
            this.packageSigningPrefix = packageSigningPrefix;
        }

        public String getSigningKeychain() {
            return signingKeychain;
        }

        public void setSigningKeychain(String signingKeychain) {
            freezeCheck();
            this.signingKeychain = signingKeychain;
        }

        public String getSigningKeyUsername() {
            return signingKeyUsername;
        }

        public void setSigningKeyUsername(String signingKeyUsername) {
            freezeCheck();
            this.signingKeyUsername = signingKeyUsername;
        }

        public boolean isSign() {
            return sign != null && sign;
        }

        public void setSign(Boolean sign) {
            freezeCheck();
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
