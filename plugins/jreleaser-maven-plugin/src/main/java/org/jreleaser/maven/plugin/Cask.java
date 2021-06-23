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
package org.jreleaser.maven.plugin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
public class Cask {
    private final Map<String, List<String>> uninstall = new LinkedHashMap<>();
    private final Map<String, List<String>> zap = new LinkedHashMap<>();

    private String name;
    private String displayName;
    private String pkgName;
    private String appName;

    void setAll(Cask cask) {
        this.name = cask.name;
        this.displayName = cask.displayName;
        this.pkgName = cask.pkgName;
        this.appName = cask.appName;
        setUninstall(cask.uninstall);
        setZap(cask.zap);
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

    public Map<String, List<String>> getUninstall() {
        return uninstall;
    }

    public void setUninstall(Map<String, List<String>> uninstall) {
        this.uninstall.clear();
        this.uninstall.putAll(uninstall);
    }

    public Map<String, List<String>> getZap() {
        return zap;
    }

    public void setZap(Map<String, List<String>> zap) {
        this.zap.clear();
        this.zap.putAll(zap);
    }

    public boolean isSet() {
        return isNotBlank(name) ||
            isNotBlank(displayName) ||
            isNotBlank(pkgName) ||
            isNotBlank(appName) ||
            !uninstall.isEmpty() ||
            !zap.isEmpty();
    }
}
