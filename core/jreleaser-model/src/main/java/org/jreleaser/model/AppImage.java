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
import java.util.List;
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
public class AppImage extends AbstractRepositoryPackager<AppImage> {
    public static final String TYPE = "appimage";
    public static final String SKIP_APPIMAGE = "skipAppImage";

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

    private final AppImageRepository repository = new AppImageRepository();
    private final List<Screenshot> screenshots = new ArrayList<>();
    private final List<String> categories = new ArrayList<>();
    private String componentId;
    private String developerName;
    private Boolean requiresTerminal;

    public AppImage() {
        super(TYPE);
    }

    @Override
    public void freeze() {
        super.freeze();
        screenshots.forEach(ModelObject::freeze);
    }

    @Override
    public void merge(AppImage source) {
        freezeCheck();
        super.merge(source);
        this.componentId = merge(this.componentId, source.componentId);
        this.developerName = merge(this.developerName, source.developerName);
        this.requiresTerminal = merge(this.requiresTerminal, source.requiresTerminal);
        setRepository(source.repository);
        setCategories(merge(this.categories, source.categories));
        setScreenshots(merge(this.screenshots, source.screenshots));
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

    public boolean isRequiresTerminal() {
        return requiresTerminal != null && requiresTerminal;
    }

    public void setRequiresTerminal(Boolean requiresTerminal) {
        freezeCheck();
        this.requiresTerminal = requiresTerminal;
    }

    public boolean isRequiresTerminalSet() {
        return requiresTerminal != null;
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

    public AppImageRepository getRepository() {
        return repository;
    }

    public void setRepository(AppImageRepository repository) {
        this.repository.merge(repository);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("componentId", componentId);
        props.put("categories", categories);
        props.put("developerName", developerName);
        props.put("requiresTerminal", isRequiresTerminal());
        Map<String, Map<String, Object>> sm = new LinkedHashMap<>();
        int i = 0;
        for (Screenshot screenshot : screenshots) {
            sm.put("screenshot " + (i++), screenshot.asMap(full));
        }
        props.put("screenshots", sm);
        props.put("repository", repository.asMap(full));
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
        return isFalse(artifact.getExtraProperties().get(SKIP_APPIMAGE));
    }

    public static class AppImageRepository extends AbstractRepositoryTap<AppImageRepository> {
        public AppImageRepository() {
            super("appimage", "appimage");
        }
    }
}
