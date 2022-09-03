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
package org.jreleaser.model.internal.packagers;

import org.jreleaser.model.Active;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Icon;
import org.jreleaser.model.internal.common.Screenshot;
import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Distribution.DistributionType.BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE;
import static org.jreleaser.model.api.packagers.AppImagePackager.SKIP_APPIMAGE;
import static org.jreleaser.model.api.packagers.AppImagePackager.TYPE;
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
public final class AppImagePackager extends AbstractRepositoryPackager<AppImagePackager> {
    private static final Map<org.jreleaser.model.Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();

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

    private final AppImageRepository repository = new AppImageRepository();
    private final List<Screenshot> screenshots = new ArrayList<>();
    private final List<Icon> icons = new ArrayList<>();
    private final List<String> categories = new ArrayList<>();
    private final Set<String> skipReleases = new LinkedHashSet<>();
    private String componentId;
    private String developerName;
    private Boolean requiresTerminal;

    private final org.jreleaser.model.api.packagers.AppImagePackager immutable = new org.jreleaser.model.api.packagers.AppImagePackager() {
        private List<? extends org.jreleaser.model.api.common.Screenshot> screenshots;
        private List<? extends org.jreleaser.model.api.common.Icon> icons;

        @Override
        public String getComponentId() {
            return componentId;
        }

        @Override
        public List<String> getCategories() {
            return unmodifiableList(categories);
        }

        @Override
        public String getDeveloperName() {
            return developerName;
        }

        @Override
        public boolean isRequiresTerminal() {
            return AppImagePackager.this.isRequiresTerminal();
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Screenshot> getScreenshots() {
            if (null == screenshots) {
                screenshots = AppImagePackager.this.screenshots.stream()
                    .map(Screenshot::asImmutable)
                    .collect(toList());
            }
            return screenshots;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Icon> getIcons() {
            if (null == icons) {
                icons = AppImagePackager.this.icons.stream()
                    .map(Icon::asImmutable)
                    .collect(toList());
            }
            return icons;
        }

        @Override
        public Set<String> getSkipReleases() {
            return unmodifiableSet(skipReleases);
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
            return commitAuthor.asImmutable();
        }

        @Override
        public String getTemplateDirectory() {
            return templateDirectory;
        }

        @Override
        public List<String> getSkipTemplates() {
            return unmodifiableList(skipTemplates);
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getDownloadUrl() {
            return downloadUrl;
        }

        @Override
        public boolean supportsPlatform(String platform) {
            return AppImagePackager.this.supportsPlatform(platform);
        }

        @Override
        public boolean supportsDistribution(Distribution.DistributionType distributionType) {
            return AppImagePackager.this.supportsDistribution(distributionType);
        }

        @Override
        public Set<String> getSupportedFileExtensions(Distribution.DistributionType distributionType) {
            return AppImagePackager.this.getSupportedFileExtensions(distributionType);
        }

        @Override
        public Set<Stereotype> getSupportedStereotypes() {
            return AppImagePackager.this.getSupportedStereotypes();
        }

        @Override
        public boolean isSnapshotSupported() {
            return AppImagePackager.this.isSnapshotSupported();
        }

        @Override
        public boolean isContinueOnError() {
            return AppImagePackager.this.isContinueOnError();
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return AppImagePackager.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(AppImagePackager.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return AppImagePackager.this.getPrefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }
    };

    public AppImagePackager() {
        super(TYPE);
    }

    public org.jreleaser.model.api.packagers.AppImagePackager asImmutable() {
        return immutable;
    }

    @Override
    public void merge(AppImagePackager source) {
        super.merge(source);
        this.componentId = merge(this.componentId, source.componentId);
        this.developerName = merge(this.developerName, source.developerName);
        this.requiresTerminal = merge(this.requiresTerminal, source.requiresTerminal);
        setRepository(source.repository);
        setCategories(merge(this.categories, source.categories));
        setScreenshots(merge(this.screenshots, source.screenshots));
        setIcons(merge(this.icons, source.icons));
        setSkipReleases(merge(this.skipReleases, source.skipReleases));
    }

    @Override
    public Set<Stereotype> getSupportedStereotypes() {
        return setOf(Stereotype.CLI, Stereotype.DESKTOP);
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> tags) {
        this.categories.clear();
        this.categories.addAll(tags);
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

    public boolean isRequiresTerminal() {
        return requiresTerminal != null && requiresTerminal;
    }

    public void setRequiresTerminal(Boolean requiresTerminal) {
        this.requiresTerminal = requiresTerminal;
    }

    public boolean isRequiresTerminalSet() {
        return requiresTerminal != null;
    }

    public List<Screenshot> getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(List<Screenshot> screenshots) {
        this.screenshots.clear();
        this.screenshots.addAll(screenshots);
    }

    public void addScreenshot(Screenshot screenshot) {
        if (null != screenshot) {
            this.screenshots.add(screenshot);
        }
    }

    public List<Icon> getIcons() {
        return icons;
    }

    public void setIcons(List<Icon> icons) {
        this.icons.clear();
        this.icons.addAll(icons);
    }

    public void addIcon(Icon icon) {
        if (null != icon) {
            this.icons.add(icon);
        }
    }

    public Set<String> getSkipReleases() {
        return skipReleases;
    }

    public void setSkipReleases(Set<String> tags) {
        this.skipReleases.clear();
        this.skipReleases.addAll(tags);
    }

    public AppImageRepository getRepository() {
        return repository;
    }

    public void setRepository(AppImageRepository repository) {
        this.repository.merge(repository);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> map) {
        super.asMap(full, map);
        map.put("componentId", componentId);
        map.put("categories", categories);
        map.put("developerName", developerName);
        map.put("requiresTerminal", isRequiresTerminal());
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
        map.put("skipReleases", skipReleases);
        map.put("repository", repository.asMap(full));
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return getPackagerRepository();
    }

    public PackagerRepository getPackagerRepository() {
        return repository;
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) ||
            PlatformUtils.isLinux(platform) && PlatformUtils.isIntel64(platform) && !PlatformUtils.isAlpineLinux(platform);
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
        return isFalse(artifact.getExtraProperties().get(SKIP_APPIMAGE));
    }

    public static final class AppImageRepository extends PackagerRepository {
        public AppImageRepository() {
            super("appimage", "appimage");
        }
    }
}
