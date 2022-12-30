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
package org.jreleaser.model.internal.assemble;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.FileSet;
import org.jreleaser.model.internal.common.Glob;
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

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.jreleaser.model.api.assemble.JpackageAssembler.TYPE;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.CollectionUtils.setOf;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public final class
JpackageAssembler extends AbstractJavaAssembler<JpackageAssembler, org.jreleaser.model.api.assemble.JpackageAssembler> {
    private final Set<Artifact> runtimeImages = new LinkedHashSet<>();

    private final ApplicationPackage applicationPackage = new ApplicationPackage();
    private final Launcher launcher = new Launcher();
    private final Linux linux = new Linux();
    private final Windows windows = new Windows();
    private final Osx osx = new Osx();

    private String jlink;
    private Boolean attachPlatform;
    private Boolean verbose;

    private final org.jreleaser.model.api.assemble.JpackageAssembler immutable = new org.jreleaser.model.api.assemble.JpackageAssembler() {
        private Set<? extends org.jreleaser.model.api.common.Artifact> runtimeImages;
        private Set<? extends org.jreleaser.model.api.assemble.JpackageAssembler.PlatformPackager> platformPackagers;
        private List<? extends org.jreleaser.model.api.common.Glob> jars;
        private List<? extends org.jreleaser.model.api.common.Glob> files;
        private List<? extends org.jreleaser.model.api.common.FileSet> fileSets;
        private Set<? extends org.jreleaser.model.api.common.Artifact> outputs;

        @Override
        public String getJlink() {
            return jlink;
        }

        @Override
        public boolean isAttachPlatform() {
            return JpackageAssembler.this.isAttachPlatform();
        }

        @Override
        public boolean isVerbose() {
            return JpackageAssembler.this.isVerbose();
        }

        @Override
        public Set<? extends org.jreleaser.model.api.common.Artifact> getRuntimeImages() {
            if (null == runtimeImages) {
                runtimeImages = JpackageAssembler.this.runtimeImages.stream()
                    .map(Artifact::asImmutable)
                    .collect(toSet());
            }
            return runtimeImages;
        }

        @Override
        public ApplicationPackage getApplicationPackage() {
            return applicationPackage.asImmutable();
        }

        @Override
        public Launcher getLauncher() {
            return launcher.asImmutable();
        }

        @Override
        public Linux getLinux() {
            return linux.asImmutable();
        }

        @Override
        public Windows getWindows() {
            return windows.asImmutable();
        }

        @Override
        public Osx getOsx() {
            return osx.asImmutable();
        }

        @Override
        public Set<? extends PlatformPackager> getPlatformPackagers() {
            if (null == platformPackagers) {
                platformPackagers = unmodifiableSet(setOf(
                    osx.asImmutable(),
                    linux.asImmutable(),
                    windows.asImmutable()
                ));
            }
            return platformPackagers;
        }

        @Override
        public String getExecutable() {
            return executable;
        }

        @Override
        public String getTemplateDirectory() {
            return templateDirectory;
        }

        @Override
        public org.jreleaser.model.api.common.Java getJava() {
            return java.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.common.Artifact getMainJar() {
            return mainJar.asImmutable();
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Glob> getJars() {
            if (null == jars) {
                jars = JpackageAssembler.this.jars.stream()
                    .map(Glob::asImmutable)
                    .collect(toList());
            }
            return jars;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Glob> getFiles() {
            if (null == files) {
                files = JpackageAssembler.this.files.stream()
                    .map(Glob::asImmutable)
                    .collect(toList());
            }
            return files;
        }

        @Override
        public org.jreleaser.model.api.platform.Platform getPlatform() {
            return platform.asImmutable();
        }

        @Override
        public Distribution.DistributionType getDistributionType() {
            return JpackageAssembler.this.getDistributionType();
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public Stereotype getStereotype() {
            return JpackageAssembler.this.getStereotype();
        }

        @Override
        public boolean isExported() {
            return JpackageAssembler.this.isExported();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.FileSet> getFileSets() {
            if (null == fileSets) {
                fileSets = JpackageAssembler.this.fileSets.stream()
                    .map(FileSet::asImmutable)
                    .collect(toList());
            }
            return fileSets;
        }

        @Override
        public Set<? extends org.jreleaser.model.api.common.Artifact> getOutputs() {
            if (null == outputs) {
                outputs = JpackageAssembler.this.outputs.stream()
                    .map(Artifact::asImmutable)
                    .collect(toSet());
            }
            return outputs;
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return JpackageAssembler.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(JpackageAssembler.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return JpackageAssembler.this.getPrefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }
    };

    public JpackageAssembler() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.assemble.JpackageAssembler asImmutable() {
        return immutable;
    }

    @Override
    public Distribution.DistributionType getDistributionType() {
        return Distribution.DistributionType.NATIVE_PACKAGE;
    }

    @Override
    public void merge(JpackageAssembler source) {
        super.merge(source);
        this.jlink = merge(this.jlink, source.jlink);
        this.attachPlatform = merge(this.attachPlatform, source.attachPlatform);
        this.verbose = merge(this.verbose, source.verbose);
        setRuntimeImages(source.runtimeImages);
        setApplicationPackage(source.applicationPackage);
        setLauncher(source.launcher);
        setLinux(source.linux);
        setWindows(source.windows);
        setOsx(source.osx);
    }

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
        return Artifact.sortArtifacts(runtimeImages);
    }

    public void setRuntimeImages(Set<Artifact> runtimeImages) {
        this.runtimeImages.clear();
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

        String getResolvedAppName(JReleaserContext context, JpackageAssembler jpackage);

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

    public static final class ApplicationPackage extends AbstractModelObject<ApplicationPackage> implements Domain {
        private final List<String> fileAssociations = new ArrayList<>();

        private String appName;
        private String appVersion;
        private String vendor;
        private String copyright;
        private String licenseFile;

        private final org.jreleaser.model.api.assemble.JpackageAssembler.ApplicationPackage immutable = new org.jreleaser.model.api.assemble.JpackageAssembler.ApplicationPackage() {
            @Override
            public String getAppName() {
                return appName;
            }

            @Override
            public String getAppVersion() {
                return appVersion;
            }

            @Override
            public String getVendor() {
                return vendor;
            }

            @Override
            public String getCopyright() {
                return copyright;
            }

            @Override
            public List<String> getFileAssociations() {
                return unmodifiableList(fileAssociations);
            }

            @Override
            public String getLicenseFile() {
                return licenseFile;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(ApplicationPackage.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.assemble.JpackageAssembler.ApplicationPackage asImmutable() {
            return immutable;
        }

        @Override
        public void merge(ApplicationPackage source) {
            this.appName = this.merge(this.appName, source.appName);
            this.appVersion = this.merge(this.appVersion, source.appVersion);
            this.vendor = this.merge(this.vendor, source.vendor);
            this.copyright = this.merge(this.copyright, source.copyright);
            this.licenseFile = this.merge(this.licenseFile, source.licenseFile);
            setFileAssociations(merge(this.fileAssociations, source.fileAssociations));
        }

        public String getResolvedAppVersion(JReleaserContext context, JpackageAssembler jpackage) {
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

    public static final class Launcher extends AbstractModelObject<Launcher> implements Domain {
        private final List<String> arguments = new ArrayList<>();
        private final List<String> javaOptions = new ArrayList<>();
        private final List<String> launchers = new ArrayList<>();

        private final org.jreleaser.model.api.assemble.JpackageAssembler.Launcher immutable = new org.jreleaser.model.api.assemble.JpackageAssembler.Launcher() {
            @Override
            public List<String> getLaunchers() {
                return unmodifiableList(launchers);
            }

            @Override
            public List<String> getArguments() {
                return unmodifiableList(arguments);
            }

            @Override
            public List<String> getJavaOptions() {
                return unmodifiableList(javaOptions);
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Launcher.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.assemble.JpackageAssembler.Launcher asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Launcher source) {
            setArguments(merge(this.arguments, source.arguments));
            setJavaOptions(merge(this.javaOptions, source.javaOptions));
            setLaunchers(merge(this.launchers, source.launchers));
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

    public abstract static class AbstractPlatformPackager<S extends AbstractPlatformPackager<S>> extends AbstractModelObject<S> implements PlatformPackager {
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
        public void merge(S source) {
            this.icon = this.merge(this.icon, source.icon);
            this.appName = this.merge(this.appName, source.appName);
            this.enabled = this.merge(this.enabled, source.enabled);
            this.installDir = this.merge(this.installDir, source.installDir);
            this.resourceDir = this.merge(this.resourceDir, source.resourceDir);
            setJdk(source.jdk);
            setTypes(merge(this.types, source.types));
        }

        @Override
        public String getResolvedAppName(JReleaserContext context, JpackageAssembler jpackage) {
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
            this.jdk.merge(jdk);
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

    public static final class Linux extends AbstractPlatformPackager<Linux> {
        private final List<String> packageDeps = new ArrayList<>();
        private String packageName;
        private String maintainer;
        private String menuGroup;
        private String license;
        private String appRelease;
        private String appCategory;
        private Boolean shortcut;

        private final org.jreleaser.model.api.assemble.JpackageAssembler.Linux immutable = new org.jreleaser.model.api.assemble.JpackageAssembler.Linux() {
            @Override
            public List<String> getPackageDeps() {
                return unmodifiableList(packageDeps);
            }

            @Override
            public String getPackageName() {
                return packageName;
            }

            @Override
            public String getMaintainer() {
                return maintainer;
            }

            @Override
            public String getMenuGroup() {
                return menuGroup;
            }

            @Override
            public String getLicense() {
                return license;
            }

            @Override
            public String getAppRelease() {
                return appRelease;
            }

            @Override
            public String getAppCategory() {
                return appCategory;
            }

            @Override
            public boolean isShortcut() {
                return Linux.this.isShortcut();
            }

            @Override
            public String getAppName() {
                return appName;
            }

            @Override
            public String getIcon() {
                return icon;
            }

            @Override
            public String getPlatform() {
                return platform;
            }

            @Override
            public boolean isEnabled() {
                return Linux.this.isEnabled();
            }

            @Override
            public org.jreleaser.model.api.common.Artifact getJdk() {
                return jdk.asImmutable();
            }

            @Override
            public List<String> getTypes() {
                return unmodifiableList(types);
            }

            @Override
            public String getInstallDir() {
                return installDir;
            }

            @Override
            public String getResourceDir() {
                return resourceDir;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Linux.this.asMap(full));
            }
        };

        public Linux() {
            super("linux", Arrays.asList("deb", "rpm"));
        }

        public org.jreleaser.model.api.assemble.JpackageAssembler.Linux asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Linux source) {
            super.merge(source);
            this.packageName = this.merge(this.packageName, source.packageName);
            this.maintainer = this.merge(this.maintainer, source.maintainer);
            this.menuGroup = this.merge(this.menuGroup, source.menuGroup);
            this.license = this.merge(this.license, source.license);
            this.appRelease = this.merge(this.appRelease, source.appRelease);
            this.appCategory = this.merge(this.appCategory, source.appCategory);
            this.shortcut = this.merge(this.shortcut, source.shortcut);
            setPackageDeps(merge(this.packageDeps, source.packageDeps));
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

        public void setShortcut(Boolean shortcut) {
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

    public static final class Windows extends AbstractPlatformPackager<Windows> {
        private Boolean console;
        private Boolean dirChooser;
        private Boolean menu;
        private Boolean perUserInstall;
        private Boolean shortcut;
        private String menuGroup;
        private String upgradeUuid;

        private final org.jreleaser.model.api.assemble.JpackageAssembler.Windows immutable = new org.jreleaser.model.api.assemble.JpackageAssembler.Windows() {
            @Override
            public boolean isConsole() {
                return Windows.this.isConsole();
            }

            @Override
            public boolean isDirChooser() {
                return Windows.this.isDirChooser();
            }

            @Override
            public boolean isMenu() {
                return Windows.this.isMenu();
            }

            @Override
            public boolean isPerUserInstall() {
                return Windows.this.isPerUserInstall();
            }

            @Override
            public boolean isShortcut() {
                return Windows.this.isShortcut();
            }

            @Override
            public String getMenuGroup() {
                return menuGroup;
            }

            @Override
            public String getUpgradeUuid() {
                return upgradeUuid;
            }

            @Override
            public String getAppName() {
                return appName;
            }

            @Override
            public String getIcon() {
                return icon;
            }

            @Override
            public String getPlatform() {
                return platform;
            }

            @Override
            public boolean isEnabled() {
                return Windows.this.isEnabled();
            }

            @Override
            public org.jreleaser.model.api.common.Artifact getJdk() {
                return jdk.asImmutable();
            }

            @Override
            public List<String> getTypes() {
                return unmodifiableList(types);
            }

            @Override
            public String getInstallDir() {
                return installDir;
            }

            @Override
            public String getResourceDir() {
                return resourceDir;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Windows.this.asMap(full));
            }
        };

        public Windows() {
            super("windows", Arrays.asList("msi", "exe"));
        }

        public org.jreleaser.model.api.assemble.JpackageAssembler.Windows asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Windows source) {
            super.merge(source);
            this.console = this.merge(this.console, source.console);
            this.dirChooser = this.merge(this.dirChooser, source.dirChooser);
            this.menu = this.merge(this.menu, source.menu);
            this.perUserInstall = this.merge(this.perUserInstall, source.perUserInstall);
            this.shortcut = this.merge(this.shortcut, source.shortcut);
            this.menuGroup = this.merge(this.menuGroup, source.menuGroup);
            this.upgradeUuid = this.merge(this.upgradeUuid, source.upgradeUuid);
        }

        public boolean isConsole() {
            return console != null && console;
        }

        public void setConsole(Boolean console) {
            this.console = console;
        }

        public boolean isDirChooser() {
            return dirChooser != null && dirChooser;
        }

        public void setDirChooser(Boolean dirChooser) {
            this.dirChooser = dirChooser;
        }

        public boolean isMenu() {
            return menu != null && menu;
        }

        public void setMenu(Boolean menu) {
            this.menu = menu;
        }

        public boolean isPerUserInstall() {
            return perUserInstall != null && perUserInstall;
        }

        public void setPerUserInstall(Boolean perUserInstall) {
            this.perUserInstall = perUserInstall;
        }

        public boolean isShortcut() {
            return shortcut != null && shortcut;
        }

        public void setShortcut(Boolean shortcut) {
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

    public static final class Osx extends AbstractPlatformPackager<Osx> {
        private String packageIdentifier;
        private String packageName;
        private String packageSigningPrefix;
        private String signingKeychain;
        private String signingKeyUsername;
        private Boolean sign;

        private final org.jreleaser.model.api.assemble.JpackageAssembler.Osx immutable = new org.jreleaser.model.api.assemble.JpackageAssembler.Osx() {
            @Override
            public String getPackageIdentifier() {
                return packageIdentifier;
            }

            @Override
            public String getPackageName() {
                return packageName;
            }

            @Override
            public String getPackageSigningPrefix() {
                return packageSigningPrefix;
            }

            @Override
            public String getSigningKeychain() {
                return signingKeychain;
            }

            @Override
            public String getSigningKeyUsername() {
                return signingKeyUsername;
            }

            @Override
            public boolean isSign() {
                return Osx.this.isSign();
            }

            @Override
            public String getAppName() {
                return appName;
            }

            @Override
            public String getIcon() {
                return icon;
            }

            @Override
            public String getPlatform() {
                return platform;
            }

            @Override
            public boolean isEnabled() {
                return Osx.this.isEnabled();
            }

            @Override
            public org.jreleaser.model.api.common.Artifact getJdk() {
                return jdk.asImmutable();
            }

            @Override
            public List<String> getTypes() {
                return unmodifiableList(types);
            }

            @Override
            public String getInstallDir() {
                return installDir;
            }

            @Override
            public String getResourceDir() {
                return resourceDir;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Osx.this.asMap(full));
            }
        };

        public Osx() {
            super("osx", Arrays.asList("dmg", "pkg"));
        }

        public org.jreleaser.model.api.assemble.JpackageAssembler.Osx asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Osx source) {
            super.merge(source);
            this.packageIdentifier = this.merge(this.packageIdentifier, source.packageIdentifier);
            this.packageName = this.merge(this.packageName, source.packageName);
            this.packageSigningPrefix = this.merge(this.packageSigningPrefix, source.packageSigningPrefix);
            this.signingKeychain = this.merge(this.signingKeychain, source.signingKeychain);
            this.signingKeyUsername = this.merge(this.signingKeyUsername, source.signingKeyUsername);
            this.sign = this.merge(this.sign, source.sign);
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
