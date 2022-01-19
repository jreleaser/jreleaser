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

import org.apache.maven.plugins.annotations.Parameter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Brew extends AbstractRepositoryPackager {
    @Parameter(property = "dependencies")
    private final List<Dependency> dependencies = new ArrayList<>();
    private final Tap tap = new Tap();
    private final List<String> livecheck = new ArrayList<>();
    private final Cask cask = new Cask();

    private String formulaName;
    private Boolean multiPlatform;

    void setAll(Brew brew) {
        super.setAll(brew);
        this.formulaName = brew.formulaName;
        this.multiPlatform = brew.multiPlatform;
        setTap(brew.tap);
        setDependencies(brew.dependencies);
        setLivecheck(brew.livecheck);
        setCask(brew.cask);
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

    public Tap getTap() {
        return tap;
    }

    public void setTap(Tap tap) {
        this.tap.setAll(tap);
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies.clear();
        this.dependencies.addAll(dependencies);
    }

    public Cask getCask() {
        return cask;
    }

    public void setCask(Cask cask) {
        this.cask.setAll(cask);
    }

    public List<String> getLivecheck() {
        return livecheck;
    }

    public void setLivecheck(List<String> livecheck) {
        this.livecheck.clear();
        this.livecheck.addAll(livecheck);
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            !dependencies.isEmpty() ||
            tap.isSet() ||
            !livecheck.isEmpty() ||
            multiPlatform != null ||
            cask.isSet();
    }

    public static class Cask {
        private final Map<String, List<String>> uninstall = new LinkedHashMap<>();
        private final Map<String, List<String>> zap = new LinkedHashMap<>();

        private String name;
        private String displayName;
        private String pkgName;
        private String appName;
        private String appcast;
        private Boolean enabled;

        void setAll(Cask cask) {
            this.name = cask.name;
            this.displayName = cask.displayName;
            this.pkgName = cask.pkgName;
            this.appName = cask.appName;
            this.appcast = cask.appcast;
            this.enabled = cask.enabled;
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

        public String getAppcast() {
            return appcast;
        }

        public void setAppcast(String appcast) {
            this.appcast = appcast;
        }

        public boolean isEnabled() {
            return enabled != null && enabled;
        }

        public boolean isEnabledSet() {
            return enabled != null;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
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
                isNotBlank(appcast) ||
                !uninstall.isEmpty() ||
                !zap.isEmpty();
        }
    }

    public static class Dependency {
        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
