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
import org.jreleaser.util.FileType;
import org.jreleaser.util.PlatformUtils;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.getClassNameForLowerCaseHyphenSeparatedName;
import static org.jreleaser.util.StringUtils.getNaturalName;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Brew extends AbstractRepositoryTool {
    public static final String NAME = "brew";
    public static final String SKIP_BREW = "skipBrew";

    private final List<Dependency> dependencies = new ArrayList<>();
    private final List<String> livecheck = new ArrayList<>();
    private final HomebrewTap tap = new HomebrewTap();
    private final Cask cask = new Cask();

    private String formulaName;
    private String cachedFormulaName;
    private Boolean multiPlatform;

    public Brew() {
        super(NAME);
    }

    void setAll(Brew brew) {
        super.setAll(brew);
        this.formulaName = brew.formulaName;
        this.multiPlatform = brew.multiPlatform;
        setTap(brew.tap);
        setDependenciesAsList(brew.dependencies);
        setLivecheck(brew.livecheck);
        setCask(brew.cask);
    }

    public String getResolvedFormulaName(JReleaserContext context) {
        if (isBlank(cachedFormulaName)) {
            if (formulaName.contains("{{")) {
                cachedFormulaName = applyTemplate(formulaName, context.props());
            } else {
                cachedFormulaName = formulaName;
            }
            cachedFormulaName = getClassNameForLowerCaseHyphenSeparatedName(cachedFormulaName);
        }
        return cachedFormulaName;
    }

    public String getResolvedFormulaName(Map<String, Object> props) {
        if (isBlank(cachedFormulaName)) {
            if (formulaName.contains("{{")) {
                cachedFormulaName = applyTemplate(formulaName, props);
            } else {
                cachedFormulaName = formulaName;
            }
            cachedFormulaName = getClassNameForLowerCaseHyphenSeparatedName(cachedFormulaName);
        } else if (cachedFormulaName.contains("{{")) {
            cachedFormulaName = applyTemplate(cachedFormulaName, props);
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

    public boolean isMultiPlatform() {
        return multiPlatform != null && multiPlatform;
    }

    public void setMultiPlatform(Boolean multiPlatform) {
        this.multiPlatform = multiPlatform;
    }

    public boolean isMultiPlatformSet() {
        return multiPlatform != null;
    }

    public HomebrewTap getTap() {
        return tap;
    }

    public void setTap(HomebrewTap tap) {
        this.tap.setAll(tap);
    }

    public Cask getCask() {
        return cask;
    }

    public void setCask(Cask cask) {
        this.cask.setAll(cask);
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

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("formulaName", formulaName);
        props.put("multiPlatform", isMultiPlatform());
        props.put("tap", tap.asMap(full));
        props.put("dependencies", dependencies);
        props.put("livecheck", livecheck);
        props.put("cask", cask.asMap(full));
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return tap;
    }

    @Override
    public boolean supportsPlatform(String platform) {
        if (isMultiPlatform()) {
            return (isBlank(platform) || PlatformUtils.isMac(platform) || PlatformUtils.isLinux(platform)) &&
                !PlatformUtils.isAlpineLinux(platform);
        }
        return isBlank(platform) || PlatformUtils.isMac(platform);
    }

    @Override
    public Set<String> getSupportedExtensions() {
        Set<String> set = new LinkedHashSet<>();
        set.add(FileType.DMG.extension());
        set.add(FileType.PKG.extension());
        set.add(FileType.ZIP.extension());
        set.add(FileType.JAR.extension());
        return set;
    }

    public static class Dependency {
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
            if (o == null || getClass() != o.getClass()) return false;
            Dependency that = (Dependency) o;
            return key.equals(that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }

    public static class HomebrewTap extends AbstractRepositoryTap {
        public HomebrewTap() {
            super("homebrew", "homebrew-tap");
        }
    }

    public static class Cask implements Domain {
        private final List<CaskItem> uninstall = new ArrayList<>();
        private final List<CaskItem> zap = new ArrayList<>();
        protected Boolean enabled;
        private String name;
        private String displayName;
        private String pkgName;
        private String appName;
        private String appcast;

        @JsonIgnore
        private String cachedCaskName;
        @JsonIgnore
        private String cachedDisplayName;
        @JsonIgnore
        private String cachedAppName;
        @JsonIgnore
        private String cachedPkgName;

        void setAll(Cask cask) {
            this.enabled = cask.enabled;
            this.name = cask.name;
            this.displayName = cask.displayName;
            this.pkgName = cask.pkgName;
            this.appName = cask.appName;
            this.appcast = cask.appcast;
            setUninstallItems(cask.uninstall);
            setZapItems(cask.zap);
        }

        public void enable() {
            this.enabled = true;
        }

        public void disable() {
            this.enabled = false;
        }

        public boolean isEnabled() {
            return enabled != null && enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabledSet() {
            return enabled != null;
        }

        public String getResolvedAppcast(Map<String, Object> props) {
            if (isNotBlank(appcast)) {
                return applyTemplate(new StringReader(appcast), props);
            }
            return appcast;
        }

        public String getResolvedCaskName(JReleaserContext context) {
            if (isBlank(cachedCaskName)) {
                if (name.contains("{{")) {
                    cachedCaskName = applyTemplate(new StringReader(name), context.getModel().props());
                } else {
                    cachedCaskName = name;
                }
                cachedCaskName = cachedCaskName.toLowerCase();
            }
            return cachedCaskName;
        }

        public String getResolvedCaskName(Map<String, Object> props) {
            if (isBlank(cachedCaskName)) {
                if (name.contains("{{")) {
                    cachedCaskName = applyTemplate(new StringReader(name), props);
                } else {
                    cachedCaskName = name;
                }
                cachedCaskName = getClassNameForLowerCaseHyphenSeparatedName(cachedCaskName);
            } else if (cachedCaskName.contains("{{")) {
                cachedCaskName = applyTemplate(new StringReader(cachedCaskName), props);
                cachedCaskName = getClassNameForLowerCaseHyphenSeparatedName(cachedCaskName);
            }
            return cachedCaskName;
        }

        public String getResolvedDisplayName(JReleaserContext context) {
            if (isBlank(cachedDisplayName)) {
                if (displayName.contains("{{")) {
                    cachedDisplayName = applyTemplate(new StringReader(displayName), context.getModel().props());
                } else {
                    cachedDisplayName = displayName;
                }
                cachedDisplayName = getClassNameForLowerCaseHyphenSeparatedName(cachedDisplayName);
            }
            return cachedDisplayName;
        }

        public String getResolvedDisplayName(Map<String, Object> props) {
            if (isBlank(cachedDisplayName)) {
                if (displayName.contains("{{")) {
                    cachedDisplayName = applyTemplate(new StringReader(displayName), props);
                } else {
                    cachedDisplayName = displayName;
                }
                cachedDisplayName = getNaturalName(getClassNameForLowerCaseHyphenSeparatedName(cachedDisplayName));
            } else if (cachedDisplayName.contains("{{")) {
                cachedDisplayName = applyTemplate(new StringReader(cachedDisplayName), props);
                cachedDisplayName = getNaturalName(getClassNameForLowerCaseHyphenSeparatedName(cachedDisplayName));
            }
            return cachedDisplayName;
        }

        public String getResolvedAppName(JReleaserContext context) {
            if (isBlank(cachedAppName)) {
                if (appName.contains("{{")) {
                    cachedAppName = applyTemplate(new StringReader(appName), context.getModel().props());
                } else {
                    cachedAppName = appName;
                }
            }
            return cachedAppName;
        }

        public String getResolvedAppName(Map<String, Object> props) {
            if (isBlank(cachedAppName)) {
                if (appName.contains("{{")) {
                    cachedAppName = applyTemplate(new StringReader(appName), props);
                } else {
                    cachedAppName = appName;
                }
            } else if (cachedAppName.contains("{{")) {
                cachedAppName = applyTemplate(new StringReader(cachedAppName), props);
            }
            return cachedAppName;
        }

        public String getResolvedPkgName(JReleaserContext context) {
            if (isBlank(cachedPkgName)) {
                if (pkgName.contains("{{")) {
                    cachedPkgName = applyTemplate(new StringReader(pkgName), context.getModel().props());
                } else {
                    cachedPkgName = pkgName;
                }
            }
            return cachedPkgName;
        }

        public String getResolvedPkgName(Map<String, Object> props) {
            if (isBlank(cachedPkgName)) {
                if (pkgName.contains("{{")) {
                    cachedPkgName = applyTemplate(new StringReader(pkgName), props);
                } else {
                    cachedPkgName = pkgName;
                }
            } else if (cachedPkgName.contains("{{")) {
                cachedPkgName = applyTemplate(new StringReader(cachedPkgName), props);
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
                    .collect(Collectors.toList()));
            }
            if (!zap.isEmpty()) {
                map.put("zap", zap.stream()
                    .map(CaskItem::asMap)
                    .collect(Collectors.toList()));
            }
            return map;
        }
    }

    public static class CaskItem implements Domain {
        private final List<String> items = new ArrayList<>();
        private String name;

        public CaskItem(String name, List<String> items) {
            this.name = name;
            this.items.addAll(items);
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

        public void addItems(List<String> item) {
            this.items.addAll(item);
        }

        public void addItem(String item) {
            if (isNotBlank(item)) {
                this.items.add(item.trim());
            }
        }

        public void removeItem(String item) {
            if (isNotBlank(item)) {
                this.items.remove(item.trim());
            }
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
    }
}
