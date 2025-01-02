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

import org.jreleaser.model.internal.common.Icon;
import org.jreleaser.model.internal.common.Screenshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractAppdataPackager<A extends org.jreleaser.model.api.packagers.RepositoryPackager, S extends AbstractAppdataPackager<A, S>> extends AbstractRepositoryPackager<A, S> implements RepositoryPackager<A> {
    private static final long serialVersionUID = -7485882117643430732L;

    private final List<Screenshot> screenshots = new ArrayList<>();
    private final List<String> categories = new ArrayList<>();
    private final List<Icon> icons = new ArrayList<>();
    private final Set<String> skipReleases = new LinkedHashSet<>();
    private String componentId;
    private String developerName;

    protected AbstractAppdataPackager(String type) {
        super(type);
    }

    @Override
    public void merge(S source) {
        super.merge(source);
        this.componentId = merge(this.componentId, source.getComponentId());
        this.developerName = merge(this.developerName, source.getDeveloperName());
        setCategories(merge(this.categories, source.getCategories()));
        setScreenshots(merge(this.screenshots, source.getScreenshots()));
        setIcons(merge(this.icons, source.getIcons()));
        setSkipReleases(merge(this.skipReleases, source.getSkipReleases()));
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

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("componentId", componentId);
        props.put("categories", categories);
        props.put("developerName", developerName);
        Map<String, Map<String, Object>> sm = new LinkedHashMap<>();
        int i = 0;
        for (Screenshot screenshot : screenshots) {
            sm.put("screenshot " + (i++), screenshot.asMap(full));
        }
        props.put("screenshots", sm);
        sm = new LinkedHashMap<>();
        i = 0;
        for (Icon icon : icons) {
            sm.put("icon " + (i++), icon.asMap(full));
        }
        props.put("icons", sm);
        props.put("skipReleases", skipReleases);
    }
}
