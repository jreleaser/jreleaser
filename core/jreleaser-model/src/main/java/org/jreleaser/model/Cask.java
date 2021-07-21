/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.getClassNameForLowerCaseHyphenSeparatedName;
import static org.jreleaser.util.StringUtils.getNaturalName;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
public class Cask implements Domain {
    private final List<CaskItem> uninstall = new ArrayList<>();
    private final List<CaskItem> zap = new ArrayList<>();
    protected boolean enabled;
    private String name;
    private String displayName;
    private String pkgName;
    private String appName;
    private String appcast;

    private String cachedCaskName;
    private String cachedDisplayName;
    private String cachedAppName;
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
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
