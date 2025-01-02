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
import org.jreleaser.model.Active;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Constants.SKIP_CASK_DISPLAY_NAME_TRANSFORM;
import static org.jreleaser.model.Distribution.DistributionType.BINARY;
import static org.jreleaser.model.Distribution.DistributionType.FLAT_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_PACKAGE;
import static org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR;
import static org.jreleaser.model.JReleaserOutput.nag;
import static org.jreleaser.model.api.packagers.BrewPackager.SKIP_BREW;
import static org.jreleaser.model.api.packagers.BrewPackager.TYPE;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.DMG;
import static org.jreleaser.util.FileType.JAR;
import static org.jreleaser.util.FileType.PKG;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.getClassNameForLowerCaseHyphenSeparatedName;
import static org.jreleaser.util.StringUtils.getNaturalName;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isFalse;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class BrewPackager extends AbstractRepositoryPackager<org.jreleaser.model.api.packagers.BrewPackager, BrewPackager> {
    private static final Map<org.jreleaser.model.Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();
    private static final long serialVersionUID = -2068963278096348674L;

    static {
        Set<String> extensions = setOf(ZIP.extension());
        SUPPORTED.put(NATIVE_IMAGE, extensions);
        SUPPORTED.put(BINARY, extensions);
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(NATIVE_PACKAGE, setOf(ZIP.extension(), DMG.extension(), PKG.extension()));
        SUPPORTED.put(SINGLE_JAR, setOf(JAR.extension()));
        SUPPORTED.put(FLAT_BINARY, emptySet());
    }

    private final List<Dependency> dependencies = new ArrayList<>();
    private final List<String> livecheck = new ArrayList<>();
    private final Set<String> requireRelative = new LinkedHashSet<>();
    private final HomebrewRepository repository = new HomebrewRepository();
    private final Cask cask = new Cask();

    private String formulaName;
    private String cachedFormulaName;
    private String downloadStrategy;
    private Boolean multiPlatform;

    @JsonIgnore
    private final org.jreleaser.model.api.packagers.BrewPackager immutable = new org.jreleaser.model.api.packagers.BrewPackager() {
        private static final long serialVersionUID = -5776275819147353630L;

        @Override
        public String getFormulaName() {
            return formulaName;
        }

        @Override
        public String getDownloadStrategy() {
            return downloadStrategy;
        }

        @Override
        public boolean isMultiPlatform() {
            return BrewPackager.this.isMultiPlatform();
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getRepository() {
            return repository.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getTap() {
            return getRepository();
        }

        @Override
        public Cask getCask() {
            return cask.asImmutable();
        }

        @Override
        public List<String> getLivecheck() {
            return unmodifiableList(livecheck);
        }

        @Override
        public Set<String> getRequireRelative() {
            return unmodifiableSet(requireRelative);
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getPackagerRepository() {
            return getRepository();
        }

        @Override
        public org.jreleaser.model.api.common.CommitAuthor getCommitAuthor() {
            return BrewPackager.this.getCommitAuthor().asImmutable();
        }

        @Override
        public String getTemplateDirectory() {
            return BrewPackager.this.getTemplateDirectory();
        }

        @Override
        public List<String> getSkipTemplates() {
            return unmodifiableList(BrewPackager.this.getSkipTemplates());
        }

        @Override
        public String getType() {
            return BrewPackager.this.getType();
        }

        @Override
        public String getDownloadUrl() {
            return BrewPackager.this.getDownloadUrl();
        }

        @Override
        public boolean supportsPlatform(String platform) {
            return BrewPackager.this.supportsPlatform(platform);
        }

        @Override
        public boolean supportsDistribution(org.jreleaser.model.Distribution.DistributionType distributionType) {
            return BrewPackager.this.supportsDistribution(distributionType);
        }

        @Override
        public Set<String> getSupportedFileExtensions(org.jreleaser.model.Distribution.DistributionType distributionType) {
            return BrewPackager.this.getSupportedFileExtensions(distributionType);
        }

        @Override
        public Set<Stereotype> getSupportedStereotypes() {
            return BrewPackager.this.getSupportedStereotypes();
        }

        @Override
        public boolean isSnapshotSupported() {
            return BrewPackager.this.isSnapshotSupported();
        }

        @Override
        public boolean isContinueOnError() {
            return BrewPackager.this.isContinueOnError();
        }

        @Override
        public Active getActive() {
            return BrewPackager.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return BrewPackager.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(BrewPackager.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return BrewPackager.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(BrewPackager.this.getExtraProperties());
        }
    };

    public BrewPackager() {
        super(TYPE);
        this.cask.setOwner(this);
    }

    @Override
    public org.jreleaser.model.api.packagers.BrewPackager asImmutable() {
        return immutable;
    }

    @Override
    public void merge(BrewPackager source) {
        super.merge(source);
        this.formulaName = merge(this.formulaName, source.formulaName);
        this.multiPlatform = merge(this.multiPlatform, source.multiPlatform);
        this.downloadStrategy = merge(this.downloadStrategy, source.downloadStrategy);
        setRepository(source.repository);
        setDependenciesAsList(merge(this.dependencies, source.dependencies));
        setLivecheck(merge(this.livecheck, source.livecheck));
        setRequireRelative(merge(this.requireRelative, source.requireRelative));
        setCask(source.cask);
    }

    public String getResolvedFormulaName(JReleaserContext context) {
        if (isBlank(cachedFormulaName)) {
            cachedFormulaName = resolveTemplate(formulaName, context.fullProps());
            cachedFormulaName = getClassNameForLowerCaseHyphenSeparatedName(cachedFormulaName);
        }
        return cachedFormulaName;
    }

    public String getResolvedFormulaName(TemplateContext props) {
        if (isBlank(cachedFormulaName)) {
            cachedFormulaName = resolveTemplate(formulaName, props);
            cachedFormulaName = getClassNameForLowerCaseHyphenSeparatedName(cachedFormulaName);
        } else if (cachedFormulaName.contains("{{")) {
            cachedFormulaName = resolveTemplate(cachedFormulaName, props);
            cachedFormulaName = getClassNameForLowerCaseHyphenSeparatedName(cachedFormulaName);
        }
        return cachedFormulaName;
    }

    public String getFormulaName() {
        return formulaName;
    }

    public void setFormulaName(String formulaName) {
        this.formulaName = formulaName;
    }

    public String getDownloadStrategy() {
        return downloadStrategy;
    }

    public void setDownloadStrategy(String downloadStrategy) {
        this.downloadStrategy = downloadStrategy;
    }

    public boolean isMultiPlatform() {
        return null != multiPlatform && multiPlatform;
    }

    public void setMultiPlatform(Boolean multiPlatform) {
        this.multiPlatform = multiPlatform;
    }

    public boolean isMultiPlatformSet() {
        return null != multiPlatform;
    }

    public HomebrewRepository getRepository() {
        return repository;
    }

    public void setRepository(HomebrewRepository repository) {
        this.repository.merge(repository);
    }

    @Deprecated
    public HomebrewRepository getTap() {
        return getRepository();
    }

    @Deprecated
    public void setTap(HomebrewRepository repository) {
        nag("brew.tap is deprecated since 1.8.0 and will be removed in 2.0.0. Use brew.repository instead");
        setRepository(repository);
    }

    public Cask getCask() {
        return cask;
    }

    public void setCask(Cask cask) {
        this.cask.merge(cask);
        this.cask.setOwner(this);
    }

    public void setDependencies(Map<String, String> dependencies) {
        if (null == dependencies || dependencies.isEmpty()) {
            return;
        }
        this.dependencies.clear();
        dependencies.forEach(this::addDependency);
    }

    public List<Dependency> getDependenciesAsList() {
        return dependencies;
    }

    public void setDependenciesAsList(List<Dependency> dependencies) {
        if (null == dependencies || dependencies.isEmpty()) {
            return;
        }
        this.dependencies.clear();
        this.dependencies.addAll(dependencies);
    }

    public void addDependencies(Map<String, String> dependencies) {
        if (null == dependencies || dependencies.isEmpty()) {
            return;
        }
        dependencies.forEach(this::addDependency);
    }

    public void addDependency(String key, String value) {
        dependencies.add(new Dependency(key, value));
    }

    public void addDependency(String key) {
        dependencies.add(new Dependency(key));
    }

    public List<String> getLivecheck() {
        return livecheck;
    }

    public void setLivecheck(List<String> livecheck) {
        this.livecheck.clear();
        this.livecheck.addAll(livecheck);
    }

    public boolean hasLivecheck() {
        return !livecheck.isEmpty();
    }

    public Set<String> getRequireRelative() {
        return requireRelative;
    }

    public void setRequireRelative(Set<String> requireRelative) {
        this.requireRelative.clear();
        this.requireRelative.addAll(requireRelative);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("formulaName", formulaName);
        props.put("downloadStrategy", downloadStrategy);
        props.put("multiPlatform", isMultiPlatform());
        props.put("repository", repository.asMap(full));
        props.put("dependencies", dependencies);
        props.put("livecheck", livecheck);
        props.put("requireRelative", requireRelative);
        props.put("cask", cask.asMap(full));
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return getRepository();
    }

    public PackagerRepository getPackagerRepository() {
        return getRepository();
    }

    @Override
    public boolean supportsPlatform(String platform) {
        if (isMultiPlatform()) {
            return isBlank(platform) || PlatformUtils.isMac(platform) || PlatformUtils.isLinux(platform) &&
                !PlatformUtils.isAlpineLinux(platform);
        }
        return isBlank(platform) || PlatformUtils.isMac(platform);
    }

    @Override
    public boolean supportsDistribution(org.jreleaser.model.Distribution.DistributionType distributionType) {
        return SUPPORTED.containsKey(distributionType);
    }

    @Override
    public Set<String> getSupportedFileExtensions(org.jreleaser.model.Distribution.DistributionType distributionType) {
        return unmodifiableSet(SUPPORTED.getOrDefault(distributionType, emptySet()));
    }

    @Override
    public List<Artifact> resolveCandidateArtifacts(JReleaserContext context, Distribution distribution) {
        List<Artifact> candidateArtifacts = super.resolveCandidateArtifacts(context, distribution);

        if (cask.isEnabled()) {
            return candidateArtifacts.stream()
                .filter(artifact -> isBlank(artifact.getPlatform()) || PlatformUtils.isMac(artifact.getPlatform()))
                .collect(toList());
        }

        return candidateArtifacts;
    }

    @Override
    protected boolean isNotSkipped(Artifact artifact) {
        return isFalse(artifact.getExtraProperties().get(SKIP_BREW));
    }

    public static final class Dependency {
        private final String key;
        private final String value;

        private Dependency(String key) {
            this(key, null);
        }

        private Dependency(String key, String value) {
            this.key = key;
            this.value = isBlank(value) || "null".equalsIgnoreCase(value) ? null : value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            StringBuilder formatted = new StringBuilder();
            if (key.startsWith(":")) {
                formatted.append(key);
            } else {
                formatted.append("\"")
                    .append(key)
                    .append("\"");
            }
            if (isNotBlank(value)) {
                formatted.append(" => \"")
                    .append(value)
                    .append("\"");
            }
            return formatted.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (null == o || getClass() != o.getClass()) return false;
            Dependency that = (Dependency) o;
            return key.equals(that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }

    public static final class HomebrewRepository extends PackagerRepository {
        private static final long serialVersionUID = 5221738236173499392L;

        public HomebrewRepository() {
            super("homebrew", "homebrew-tap");
        }
    }

    public static final class Cask extends AbstractModelObject<Cask> implements Domain {
        private static final long serialVersionUID = -8511530978828845393L;

        private final List<CaskItem> uninstall = new ArrayList<>();
        private final List<CaskItem> zap = new ArrayList<>();
        private Boolean enabled;
        private String name;
        private String displayName;
        private String pkgName;
        private String appName;
        private String appcast;
        @JsonIgnore
        private BrewPackager owner;

        @JsonIgnore
        private final org.jreleaser.model.api.packagers.BrewPackager.Cask immutable = new org.jreleaser.model.api.packagers.BrewPackager.Cask() {
            private static final long serialVersionUID = 5862868849533321019L;

            private List<? extends org.jreleaser.model.api.packagers.BrewPackager.CaskItem> uninstall;
            private List<? extends org.jreleaser.model.api.packagers.BrewPackager.CaskItem> zap;

            @Override
            public boolean isEnabled() {
                return Cask.this.isEnabled();
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDisplayName() {
                return displayName;
            }

            @Override
            public String getPkgName() {
                return pkgName;
            }

            @Override
            public String getAppName() {
                return appName;
            }

            @Override
            public String getAppcast() {
                return appcast;
            }

            @Override
            public List<? extends org.jreleaser.model.api.packagers.BrewPackager.CaskItem> getUninstallItems() {
                if (null == uninstall) {
                    uninstall = Cask.this.uninstall.stream()
                        .map(CaskItem::asImmutable)
                        .collect(toList());
                }
                return uninstall;
            }

            @Override
            public List<? extends org.jreleaser.model.api.packagers.BrewPackager.CaskItem> getZapItems() {
                if (null == zap) {
                    zap = Cask.this.zap.stream()
                        .map(CaskItem::asImmutable)
                        .collect(toList());
                }
                return zap;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Cask.this.asMap(full));
            }
        };

        @JsonIgnore
        private String cachedCaskName;
        @JsonIgnore
        private String cachedDisplayName;
        @JsonIgnore
        private String cachedAppName;
        @JsonIgnore
        private String cachedPkgName;

        public org.jreleaser.model.api.packagers.BrewPackager.Cask asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Cask source) {
            this.enabled = this.merge(this.enabled, source.enabled);
            this.name = this.merge(this.name, source.name);
            this.displayName = this.merge(this.displayName, source.displayName);
            this.pkgName = this.merge(this.pkgName, source.pkgName);
            this.appName = this.merge(this.appName, source.appName);
            this.appcast = this.merge(this.appcast, source.appcast);
            setUninstallItems(merge(this.uninstall, source.uninstall));
            setZapItems(merge(this.zap, source.zap));
        }

        public void setOwner(BrewPackager owner) {
            this.owner = owner;
        }

        public void enable() {
            this.enabled = true;
        }

        public void disable() {
            this.enabled = false;
        }


        public boolean isEnabled() {
            return null != enabled && enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabledSet() {
            return null != enabled;
        }

        public String getResolvedAppcast(TemplateContext props) {
            if (isNotBlank(appcast)) {
                return resolveTemplate(appcast, props);
            }
            return appcast;
        }

        public String getResolvedCaskName(TemplateContext props) {
            if (isBlank(cachedCaskName)) {
                cachedCaskName = resolveTemplate(name, props);
            } else if (cachedCaskName.contains("{{")) {
                cachedCaskName = resolveTemplate(cachedCaskName, props);
            }
            return cachedCaskName;
        }

        public String getResolvedDisplayName(TemplateContext props) {
            if (isBlank(cachedDisplayName)) {
                cachedDisplayName = resolveTemplate(displayName, props);
                if (isFalse(owner.getExtraProperty(SKIP_CASK_DISPLAY_NAME_TRANSFORM))) {
                    cachedDisplayName = getNaturalName(getClassNameForLowerCaseHyphenSeparatedName(cachedDisplayName));
                }
            } else if (cachedDisplayName.contains("{{")) {
                cachedDisplayName = resolveTemplate(cachedDisplayName, props);
                if (isFalse(owner.getExtraProperty(SKIP_CASK_DISPLAY_NAME_TRANSFORM))) {
                    cachedDisplayName = getNaturalName(getClassNameForLowerCaseHyphenSeparatedName(cachedDisplayName));
                }
            }
            return cachedDisplayName;
        }

        public String getResolvedAppName(TemplateContext props) {
            if (isBlank(cachedAppName)) {
                cachedAppName = resolveTemplate(appName, props);
            } else if (cachedAppName.contains("{{")) {
                cachedAppName = resolveTemplate(cachedAppName, props);
            }
            return cachedAppName;
        }

        public String getResolvedPkgName(TemplateContext props) {
            if (isBlank(cachedPkgName)) {
                cachedPkgName = resolveTemplate(pkgName, props);
            } else if (cachedPkgName.contains("{{")) {
                cachedPkgName = resolveTemplate(cachedPkgName, props);
            }
            return cachedPkgName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getPkgName() {
            return pkgName;
        }

        public void setPkgName(String pkgName) {
            this.pkgName = pkgName;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getAppcast() {
            return appcast;
        }

        public void setAppcast(String appcast) {
            this.appcast = appcast;
        }

        public List<CaskItem> getUninstallItems() {
            return uninstall;
        }

        void setUninstallItems(List<CaskItem> uninstall) {
            this.uninstall.clear();
            this.uninstall.addAll(uninstall);
        }

        public void setUninstall(Map<String, List<String>> uninstall) {
            this.uninstall.clear();
            uninstall.forEach((name, items) -> this.uninstall.add(new CaskItem(name, items)));
        }

        public void addUninstall(CaskItem item) {
            if (null != item) {
                this.uninstall.add(item);
            }
        }

        public boolean getHasUninstall() {
            return !uninstall.isEmpty();
        }

        public List<CaskItem> getZapItems() {
            return zap;
        }

        void setZapItems(List<CaskItem> zap) {
            this.zap.clear();
            this.zap.addAll(zap);
        }

        public void setZap(Map<String, List<String>> zap) {
            this.zap.clear();
            zap.forEach((name, items) -> this.zap.add(new CaskItem(name, items)));
        }

        public void addZap(CaskItem item) {
            if (null != item) {
                this.zap.add(item);
            }
        }

        public boolean getHasZap() {
            return !zap.isEmpty();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("enabled", isEnabled());
            map.put("name", name);
            map.put("displayName", displayName);
            map.put("appName", appName);
            map.put("pkgName", pkgName);
            map.put("appcast", appcast);
            if (!uninstall.isEmpty()) {
                map.put("uninstall", uninstall.stream()
                    .map(CaskItem::asMap)
                    .collect(toList()));
            }
            if (!zap.isEmpty()) {
                map.put("zap", zap.stream()
                    .map(CaskItem::asMap)
                    .collect(toList()));
            }
            return map;
        }
    }

    public static final class CaskItem extends AbstractModelObject<CaskItem> implements Domain {
        private static final long serialVersionUID = -2577845359978097441L;

        private final List<String> items = new ArrayList<>();
        private String name;

        @JsonIgnore
        private final org.jreleaser.model.api.packagers.BrewPackager.CaskItem immutable = new org.jreleaser.model.api.packagers.BrewPackager.CaskItem() {
            private static final long serialVersionUID = -8230159341038906539L;

            @Override
            public String getName() {
                return name;
            }

            @Override
            public List<String> getItems() {
                return unmodifiableList(items);
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(CaskItem.this.asMap(full));
            }
        };

        public CaskItem(String name, List<String> items) {
            this.name = name;
            this.items.addAll(items);
        }

        public org.jreleaser.model.api.packagers.BrewPackager.CaskItem asImmutable() {
            return immutable;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getItems() {
            return items;
        }

        public void setItems(List<String> items) {
            this.items.clear();
            this.items.addAll(items);
        }

        public boolean getHasItems() {
            return !items.isEmpty();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return asMap();
        }

        public Map<String, Object> asMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(name, items);
            return map;
        }

        @Override
        public void merge(CaskItem source) {
            this.name = merge(this.name, source.name);
            setItems(merge(this.items, source.items));
        }
    }
}
