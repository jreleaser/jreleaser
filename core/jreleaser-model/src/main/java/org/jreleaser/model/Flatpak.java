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

import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.model.Distribution.DistributionType.BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.TAR;
import static org.jreleaser.util.FileType.TAR_GZ;
import static org.jreleaser.util.FileType.TAR_XZ;
import static org.jreleaser.util.FileType.TGZ;
import static org.jreleaser.util.FileType.TXZ;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isFalse;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class Flatpak extends AbstractRepositoryPackager<Flatpak> {
    public static final String TYPE = "flatpak";
    public static final String SKIP_FLATPAK = "skipFlatpak";

    private static final Map<Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();

    static {
        Set<String> extensions = setOf(
            TAR_GZ.extension(),
            TAR_XZ.extension(),
            TGZ.extension(),
            TXZ.extension(),
            TAR.extension(),
            ZIP.extension());

        SUPPORTED.put(BINARY, extensions);
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(NATIVE_IMAGE, extensions);
    }

    private final FlatpakRepository repository = new FlatpakRepository();
    private final List<Screenshot> screenshots = new ArrayList<>();
    private final List<String> categories = new ArrayList<>();
    private final List<Icon> icons = new ArrayList<>();
    private final Set<String> sdkExtensions = new LinkedHashSet<>();
    private final Set<String> finishArgs = new LinkedHashSet<>();
    private String componentId;
    private String developerName;
    private Runtime runtime;
    private String runtimeVersion;

    public Flatpak() {
        super(TYPE);
    }

    @Override
    public void freeze() {
        super.freeze();
        screenshots.forEach(ModelObject::freeze);
        icons.forEach(ModelObject::freeze);
    }

    @Override
    public void merge(Flatpak source) {
        freezeCheck();
        super.merge(source);
        this.componentId = merge(this.componentId, source.componentId);
        this.developerName = merge(this.developerName, source.developerName);
        this.runtime = merge(this.runtime, source.runtime);
        this.runtimeVersion = merge(this.runtimeVersion, source.runtimeVersion);
        setSdkExtensions(merge(this.sdkExtensions, source.sdkExtensions));
        setFinishArgs(merge(this.finishArgs, source.finishArgs));
        setRepository(source.repository);
        setCategories(merge(this.categories, source.categories));
        setScreenshots(merge(this.screenshots, source.screenshots));
        setIcons(merge(this.icons, source.icons));
    }

    @Override
    public Set<Stereotype> getSupportedStereotypes() {
        return setOf(Stereotype.CLI, Stereotype.DESKTOP);
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        freezeCheck();
        this.componentId = componentId;
    }

    public List<String> getCategories() {
        return freezeWrap(categories);
    }

    public void setCategories(List<String> tags) {
        freezeCheck();
        this.categories.clear();
        this.categories.addAll(tags);
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        freezeCheck();
        this.developerName = developerName;
    }

    public Runtime getRuntime() {
        return runtime;
    }

    public void setRuntime(Runtime runtime) {
        freezeCheck();
        this.runtime = runtime;
    }

    public void setRuntime(String runtime) {
        setRuntime(Runtime.of(runtime));
    }

    public String getRuntimeVersion() {
        return runtimeVersion;
    }

    public void setRuntimeVersion(String runtimeVersion) {
        freezeCheck();
        this.runtimeVersion = runtimeVersion;
    }

    public Set<String> getSdkExtensions() {
        return freezeWrap(sdkExtensions);
    }

    public void setSdkExtensions(Set<String> sdkExtensions) {
        freezeCheck();
        this.sdkExtensions.clear();
        this.sdkExtensions.addAll(sdkExtensions);
    }

    public Set<String> getFinishArgs() {
        return freezeWrap(finishArgs);
    }

    public void setFinishArgs(Set<String> finishArgs) {
        freezeCheck();
        this.finishArgs.clear();
        this.finishArgs.addAll(finishArgs);
    }

    public List<Screenshot> getScreenshots() {
        return freezeWrap(screenshots);
    }

    public void setScreenshots(List<Screenshot> screenshots) {
        freezeCheck();
        this.screenshots.clear();
        this.screenshots.addAll(screenshots);
    }

    public void addScreenshot(Screenshot screenshot) {
        freezeCheck();
        if (null != screenshot) {
            this.screenshots.add(screenshot);
        }
    }

    public List<Icon> getIcons() {
        return freezeWrap(icons);
    }

    public void setIcons(List<Icon> icons) {
        freezeCheck();
        this.icons.clear();
        this.icons.addAll(icons);
    }

    public void addIcon(Icon icon) {
        freezeCheck();
        if (null != icon) {
            this.icons.add(icon);
        }
    }

    public FlatpakRepository getRepository() {
        return repository;
    }

    public void setRepository(FlatpakRepository repository) {
        this.repository.merge(repository);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> map) {
        super.asMap(full, map);
        map.put("componentId", componentId);
        map.put("categories", categories);
        map.put("developerName", developerName);
        map.put("runtime", runtime);
        map.put("runtimeVersion", runtimeVersion);
        map.put("sdkExtensions", sdkExtensions);
        map.put("finishArgs", finishArgs);
        Map<String, Map<String, Object>> sm = new LinkedHashMap<>();
        int i = 0;
        for (Screenshot screenshot : screenshots) {
            sm.put("screenshot " + (i++), screenshot.asMap(full));
        }
        map.put("screenshots", sm);
        sm = new LinkedHashMap<>();
        i = 0;
        for (Icon icon : icons) {
            sm.put("icon " + (i++), icon.asMap(full));
        }
        map.put("icons", sm);
        map.put("repository", repository.asMap(full));
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return repository;
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) ||
            (PlatformUtils.isLinux(platform) && PlatformUtils.isIntel64(platform));
    }

    @Override
    public boolean supportsDistribution(Distribution distribution) {
        return SUPPORTED.containsKey(distribution.getType());
    }

    @Override
    public Set<String> getSupportedExtensions(Distribution distribution) {
        return Collections.unmodifiableSet(SUPPORTED.getOrDefault(distribution.getType(), Collections.emptySet()));
    }

    @Override
    protected boolean isNotSkipped(Artifact artifact) {
        return isFalse(artifact.getExtraProperties().get(SKIP_FLATPAK));
    }

    public enum Runtime {
        FREEDESKTOP("org.freedesktop.Platform", "org.freedesktop.Sdk"),
        GNOME("org.gnome.Platform", "org.gnome.Sdk"),
        KDE("org.kde.Platform", "org.kde.Sdk"),
        ELEMENTARY("io.elementary.Platform", "io.elementary.Sdk");

        private final String runtime;
        private final String sdk;

        Runtime(String runtime, String sdk) {
            this.runtime = runtime;
            this.sdk = sdk;
        }

        public String runtime() {
            return runtime;
        }

        public String sdk() {
            return sdk;
        }

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static Runtime of(String str) {
            if (isBlank(str)) return null;
            return Runtime.valueOf(str.toUpperCase(Locale.ENGLISH).trim());
        }
    }

    public static class FlatpakRepository extends AbstractRepositoryTap<FlatpakRepository> {
        public FlatpakRepository() {
            super("flatpak", "flatpak");
        }
    }
}
