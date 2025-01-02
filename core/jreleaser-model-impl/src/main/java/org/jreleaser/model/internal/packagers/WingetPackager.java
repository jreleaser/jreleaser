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
package org.jreleaser.model.internal.packagers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jreleaser.model.Active;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.api.common.Domain;
import org.jreleaser.model.api.packagers.WingetPackager.Installer.Mode;
import org.jreleaser.model.api.packagers.WingetPackager.Installer.Scope;
import org.jreleaser.model.api.packagers.WingetPackager.Installer.Type;
import org.jreleaser.model.api.packagers.WingetPackager.Installer.UpgradeBehavior;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.jreleaser.model.Distribution.DistributionType.BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_PACKAGE;
import static org.jreleaser.model.api.packagers.WingetPackager.SKIP_WINGET;
import static org.jreleaser.model.api.packagers.WingetPackager.TYPE;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.EXE;
import static org.jreleaser.util.FileType.MSI;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isFalse;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class WingetPackager extends AbstractRepositoryPackager<org.jreleaser.model.api.packagers.WingetPackager, WingetPackager> {
    private static final long serialVersionUID = -9015011090998365168L;

    private static final Map<Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();

    static {
        SUPPORTED.put(NATIVE_IMAGE, setOf(ZIP.extension()));
        SUPPORTED.put(BINARY, setOf(ZIP.extension()));
        SUPPORTED.put(JAVA_BINARY, setOf(ZIP.extension()));
        SUPPORTED.put(JLINK, setOf(ZIP.extension()));
        SUPPORTED.put(NATIVE_PACKAGE, setOf(MSI.extension(), EXE.extension()));
    }

    private final WingetRepository repository = new WingetRepository();
    private final List<String> tags = new ArrayList<>();
    @JsonProperty("package")
    private final Package pack = new Package();
    private final Publisher publisher = new Publisher();
    private final Installer installer = new Installer();

    private String defaultLocale;
    private String author;
    private String moniker;
    private String minimumOsVersion;
    private String productCode;

    @JsonIgnore
    private final org.jreleaser.model.api.packagers.WingetPackager immutable = new org.jreleaser.model.api.packagers.WingetPackager() {
        private static final long serialVersionUID = 4117704008560830372L;

        @Override
        public Package getPackage() {
            return WingetPackager.this.getPackage().asImmutable();
        }

        @Override
        public Publisher getPublisher() {
            return WingetPackager.this.getPublisher().asImmutable();
        }

        @Override
        public Installer getInstaller() {
            return WingetPackager.this.getInstaller().asImmutable();
        }

        @Override
        public String getDefaultLocale() {
            return WingetPackager.this.getDefaultLocale();
        }

        @Override
        public String getAuthor() {
            return WingetPackager.this.getAuthor();
        }

        @Override
        public String getMoniker() {
            return WingetPackager.this.getMoniker();
        }

        @Override
        public List<String> getTags() {
            return unmodifiableList(WingetPackager.this.getTags());
        }

        @Override
        public String getMinimumOsVersion() {
            return WingetPackager.this.getMinimumOsVersion();
        }

        @Override
        public String getProductCode() {
            return WingetPackager.this.getProductCode();
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getRepository() {
            return repository.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getPackagerRepository() {
            return getRepository();
        }

        @Override
        public org.jreleaser.model.api.common.CommitAuthor getCommitAuthor() {
            return WingetPackager.this.getCommitAuthor().asImmutable();
        }

        @Override
        public String getTemplateDirectory() {
            return WingetPackager.this.getTemplateDirectory();
        }

        @Override
        public List<String> getSkipTemplates() {
            return unmodifiableList(WingetPackager.this.getSkipTemplates());
        }

        @Override
        public String getType() {
            return WingetPackager.this.getType();
        }

        @Override
        public String getDownloadUrl() {
            return WingetPackager.this.getDownloadUrl();
        }

        @Override
        public boolean supportsPlatform(String platform) {
            return WingetPackager.this.supportsPlatform(platform);
        }

        @Override
        public boolean supportsDistribution(Distribution.DistributionType distributionType) {
            return WingetPackager.this.supportsDistribution(distributionType);
        }

        @Override
        public Set<String> getSupportedFileExtensions(Distribution.DistributionType distributionType) {
            return WingetPackager.this.getSupportedFileExtensions(distributionType);
        }

        @Override
        public Set<Stereotype> getSupportedStereotypes() {
            return WingetPackager.this.getSupportedStereotypes();
        }

        @Override
        public boolean isSnapshotSupported() {
            return WingetPackager.this.isSnapshotSupported();
        }

        @Override
        public boolean isContinueOnError() {
            return WingetPackager.this.isContinueOnError();
        }

        @Override
        public Active getActive() {
            return WingetPackager.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return WingetPackager.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(WingetPackager.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return WingetPackager.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(WingetPackager.this.getExtraProperties());
        }
    };

    public WingetPackager() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.packagers.WingetPackager asImmutable() {
        return immutable;
    }

    @Override
    public void merge(WingetPackager source) {
        super.merge(source);
        this.defaultLocale = merge(this.defaultLocale, source.defaultLocale);
        this.author = merge(this.author, source.author);
        this.moniker = merge(this.moniker, source.moniker);
        this.minimumOsVersion = merge(this.minimumOsVersion, source.minimumOsVersion);
        this.productCode = merge(this.productCode, source.productCode);
        setPackage(source.pack);
        setPublisher(source.publisher);
        setInstaller(source.installer);
        setRepository(source.repository);
        setTags(merge(this.tags, source.tags));
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMoniker() {
        return moniker;
    }

    public void setMoniker(String moniker) {
        this.moniker = moniker;
    }

    public String getMinimumOsVersion() {
        return minimumOsVersion;
    }

    public void setMinimumOsVersion(String minimumOsVersion) {
        this.minimumOsVersion = minimumOsVersion;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }

    @JsonProperty("package")
    public Package getPackage() {
        return pack;
    }

    public void setPackage(Package pack) {
        this.pack.merge(pack);
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher.merge(publisher);
    }

    public Installer getInstaller() {
        return installer;
    }

    public void setInstaller(Installer installer) {
        this.installer.merge(installer);
    }

    public WingetRepository getRepository() {
        return repository;
    }

    public void setRepository(WingetRepository repository) {
        this.repository.merge(repository);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("defaultLocale", defaultLocale);
        props.put("author", author);
        props.put("moniker", moniker);
        props.put("minimumOsVersion", minimumOsVersion);
        props.put("productCode", productCode);
        props.put("package", pack.asMap(full));
        props.put("publisher", publisher.asMap(full));
        props.put("installer", installer.asMap(full));
        props.put("repository", repository.asMap(full));
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return getPackagerRepository();
    }

    public PackagerRepository getPackagerRepository() {
        return getRepository();
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) || PlatformUtils.isWindows(platform);
    }

    @Override
    public boolean supportsDistribution(Distribution.DistributionType distributionType) {
        return SUPPORTED.containsKey(distributionType);
    }

    @Override
    public Set<String> getSupportedFileExtensions(Distribution.DistributionType distributionType) {
        return unmodifiableSet(SUPPORTED.getOrDefault(distributionType, emptySet()));
    }

    @Override
    protected boolean isNotSkipped(Artifact artifact) {
        return isFalse(artifact.getExtraProperties().get(SKIP_WINGET));
    }

    public static final class WingetRepository extends PackagerRepository {
        private static final long serialVersionUID = -3859984358170896375L;

        public WingetRepository() {
            super("winget", "winget");
        }
    }

    public static final class Package extends AbstractModelObject<Package> implements Domain {
        private static final long serialVersionUID = -8531840172639009180L;

        private String identifier;
        private String name;
        private String version;
        private String url;

        @JsonIgnore
        private final org.jreleaser.model.api.packagers.WingetPackager.Package immutable = new org.jreleaser.model.api.packagers.WingetPackager.Package() {
            private static final long serialVersionUID = -681067573112565263L;

            @Override
            public String getIdentifier() {
                return Package.this.getIdentifier();
            }

            @Override
            public String getName() {
                return Package.this.getName();
            }

            @Override
            public String getUrl() {
                return Package.this.getUrl();
            }

            @Override
            public String getVersion() {
                return Package.this.getVersion();
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Package.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.packagers.WingetPackager.Package asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Package source) {
            this.identifier = this.merge(this.identifier, source.identifier);
            this.name = this.merge(this.name, source.name);
            this.version = this.merge(this.version, source.version);
            this.url = this.merge(this.url, source.url);
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("identifier", identifier);
            props.put("name", name);
            props.put("version", version);
            props.put("url", url);
            return props;
        }
    }

    public static final class Publisher extends AbstractModelObject<Publisher> implements Domain {
        private static final long serialVersionUID = -3812598331451051428L;

        private String name;
        private String url;
        private String supportUrl;

        @JsonIgnore
        private final org.jreleaser.model.api.packagers.WingetPackager.Publisher immutable = new org.jreleaser.model.api.packagers.WingetPackager.Publisher() {
            private static final long serialVersionUID = -5458105963902729331L;

            @Override
            public String getName() {
                return Publisher.this.getName();
            }

            @Override
            public String getUrl() {
                return Publisher.this.getUrl();
            }

            @Override
            public String getSupportUrl() {
                return Publisher.this.getSupportUrl();
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Publisher.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.packagers.WingetPackager.Publisher asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Publisher source) {
            this.name = this.merge(this.name, source.name);
            this.url = this.merge(this.url, source.url);
            this.supportUrl = this.merge(this.supportUrl, source.supportUrl);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getSupportUrl() {
            return supportUrl;
        }

        public void setSupportUrl(String supportUrl) {
            this.supportUrl = supportUrl;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("name", name);
            props.put("url", url);
            props.put("supportUrl", supportUrl);
            return props;
        }
    }

    public static final class Installer extends AbstractModelObject<Installer> implements Domain {
        private static final long serialVersionUID = 4609121090512808243L;

        private final Set<Mode> modes = new LinkedHashSet<>();
        private final Dependencies dependencies = new Dependencies();
        private Type type = Type.WIX;
        private Scope scope = Scope.MACHINE;
        private UpgradeBehavior upgradeBehavior = UpgradeBehavior.INSTALL;
        private String command;

        @JsonIgnore
        private final org.jreleaser.model.api.packagers.WingetPackager.Installer immutable = new org.jreleaser.model.api.packagers.WingetPackager.Installer() {
            private static final long serialVersionUID = 8677697915447641476L;

            @Override
            public Type getType() {
                return Installer.this.getType();
            }

            @Override
            public Scope getScope() {
                return Installer.this.getScope();
            }

            @Override
            public Set<Mode> getModes() {
                return unmodifiableSet(Installer.this.getModes());
            }

            @Override
            public UpgradeBehavior getUpgradeBehavior() {
                return Installer.this.getUpgradeBehavior();
            }

            @Override
            public String getCommand() {
                return Installer.this.getCommand();
            }

            @Override
            public org.jreleaser.model.api.packagers.WingetPackager.Dependencies getDependencies() {
                return Installer.this.getDependencies().asImmutable();
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Installer.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.packagers.WingetPackager.Installer asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Installer source) {
            this.type = this.merge(this.type, source.type);
            this.scope = this.merge(this.scope, source.scope);
            this.upgradeBehavior = this.merge(this.upgradeBehavior, source.upgradeBehavior);
            this.command = this.merge(this.command, source.command);
            setModes(merge(this.modes, source.modes));
            setDependencies(source.dependencies);
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public void setType(String str) {
            setType(Type.of(str));
        }

        public Scope getScope() {
            return scope;
        }

        public void setScope(Scope scope) {
            this.scope = scope;
        }

        public void setScope(String str) {
            setScope(Scope.of(str));
        }

        public UpgradeBehavior getUpgradeBehavior() {
            return upgradeBehavior;
        }

        public void setUpgradeBehavior(UpgradeBehavior upgradeBehavior) {
            this.upgradeBehavior = upgradeBehavior;
        }

        public void setUpgradeBehavior(String str) {
            setUpgradeBehavior(UpgradeBehavior.of(str));
        }

        public Set<Mode> getModes() {
            return modes;
        }

        public void setModes(Set<Mode> modes) {
            this.modes.clear();
            this.modes.addAll(modes);
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public Dependencies getDependencies() {
            return dependencies;
        }

        public void setDependencies(Dependencies dependencies) {
            this.dependencies.merge(dependencies);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("type", type);
            props.put("scope", scope);
            props.put("upgradeBehavior", upgradeBehavior);
            props.put("modes", modes);
            props.put("command", command);
            props.put("dependencies", dependencies.asMap(full));
            return props;
        }
    }

    public static final class Dependencies extends AbstractModelObject<Dependencies> implements Domain {
        private static final long serialVersionUID = 8335992485241726722L;

        private final Set<String> windowsFeatures = new LinkedHashSet<>();
        private final Set<String> windowsLibraries = new LinkedHashSet<>();
        private final Set<String> externalDependencies = new LinkedHashSet<>();
        private final Set<PackageDependency> packageDependencies = new LinkedHashSet<>();

        @JsonIgnore
        private final org.jreleaser.model.api.packagers.WingetPackager.Dependencies immutable = new org.jreleaser.model.api.packagers.WingetPackager.Dependencies() {
            private static final long serialVersionUID = -2782564782601367602L;

            @Override
            public Set<String> getWindowsFeatures() {
                return unmodifiableSet(Dependencies.this.getWindowsFeatures());
            }

            @Override
            public Set<String> getWindowsLibraries() {
                return unmodifiableSet(Dependencies.this.getWindowsLibraries());
            }

            @Override
            public Set<String> getExternalDependencies() {
                return unmodifiableSet(Dependencies.this.getExternalDependencies());
            }

            @Override
            public Set<org.jreleaser.model.api.packagers.WingetPackager.PackageDependency> getPackageDependencies() {
                return unmodifiableSet(asImmutable().getPackageDependencies());
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Dependencies.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.packagers.WingetPackager.Dependencies asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Dependencies source) {
            setWindowsFeatures(merge(this.windowsFeatures, source.windowsFeatures));
            setWindowsLibraries(merge(this.windowsLibraries, source.windowsLibraries));
            setExternalDependencies(merge(this.externalDependencies, source.externalDependencies));
            setPackageDependencies(mergeModel(this.packageDependencies, source.packageDependencies));
        }

        public boolean hasDependencies() {
            return !windowsFeatures.isEmpty() ||
                !windowsLibraries.isEmpty() ||
                !externalDependencies.isEmpty() ||
                !packageDependencies.isEmpty();
        }

        public boolean hasWindowsFeatures() {
            return !windowsFeatures.isEmpty();
        }

        public boolean hasWindowsLibraries() {
            return !windowsLibraries.isEmpty();
        }

        public boolean hasExternalDependencies() {
            return !externalDependencies.isEmpty();
        }

        public boolean hasPackageDependencies() {
            return !packageDependencies.isEmpty();
        }

        public Set<String> getWindowsFeatures() {
            return windowsFeatures;
        }

        public void setWindowsFeatures(Set<String> windowsFeatures) {
            this.windowsFeatures.clear();
            this.windowsFeatures.addAll(windowsFeatures);
        }

        public Set<String> getWindowsLibraries() {
            return windowsLibraries;
        }

        public void setWindowsLibraries(Set<String> windowsLibraries) {
            this.windowsLibraries.clear();
            this.windowsLibraries.addAll(windowsLibraries);
        }

        public Set<String> getExternalDependencies() {
            return externalDependencies;
        }

        public void setExternalDependencies(Set<String> externalDependencies) {
            this.externalDependencies.clear();
            this.externalDependencies.addAll(externalDependencies);
        }

        public Set<PackageDependency> getPackageDependencies() {
            return packageDependencies;
        }

        public void setPackageDependencies(Set<PackageDependency> packageDependencies) {
            this.packageDependencies.clear();
            this.packageDependencies.addAll(packageDependencies);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("windowsFeatures", windowsFeatures);
            props.put("windowsLibraries", windowsLibraries);
            props.put("externalDependencies", externalDependencies);

            Map<String, Map<String, Object>> mappedDependencies = new LinkedHashMap<>();
            int i = 0;
            for (PackageDependency dependency : packageDependencies) {
                mappedDependencies.put("packageDependency " + (i++), dependency.asMap(full));
            }
            props.put("packageDependencies", mappedDependencies);

            return props;
        }

        public void addPackageDependency(PackageDependency packageDependency) {
            setPackageDependencies(mergeModel(this.packageDependencies, singleton(packageDependency)));
        }
    }

    public static final class PackageDependency extends AbstractModelObject<PackageDependency> implements Domain, Comparable<PackageDependency> {
        private static final long serialVersionUID = -4450142474645845838L;

        private String packageIdentifier;
        private String minimumVersion;

        @JsonIgnore
        private final org.jreleaser.model.api.packagers.WingetPackager.PackageDependency immutable = new org.jreleaser.model.api.packagers.WingetPackager.PackageDependency() {
            private static final long serialVersionUID = -8028336005839082886L;

            @Override
            public String getPackageIdentifier() {
                return packageIdentifier;
            }

            @Override
            public String getMinimumVersion() {
                return minimumVersion;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return PackageDependency.this.asMap(full);
            }
        };

        public org.jreleaser.model.api.packagers.WingetPackager.PackageDependency asImmutable() {
            return immutable;
        }

        @Override
        public void merge(PackageDependency source) {
            this.packageIdentifier = this.merge(this.packageIdentifier, source.packageIdentifier);
            this.minimumVersion = this.merge(this.minimumVersion, source.minimumVersion);
        }

        public String getPackageIdentifier() {
            return packageIdentifier;
        }

        public void setPackageIdentifier(String packageIdentifier) {
            this.packageIdentifier = packageIdentifier;
        }

        public String getMinimumVersion() {
            return minimumVersion;
        }

        public void setMinimumVersion(String minimumVersion) {
            this.minimumVersion = minimumVersion;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("packageIdentifier", packageIdentifier);
            props.put("minimumVersion", minimumVersion);
            return props;
        }

        @Override
        public int compareTo(PackageDependency o) {
            return Comparator.comparing(PackageDependency::getPackageIdentifier)
                .compare(this, o);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PackageDependency that = (PackageDependency) o;
            return packageIdentifier.equals(that.packageIdentifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(packageIdentifier);
        }
    }
}
