/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import org.jreleaser.util.Env;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jreleaser.model.GitService.TAG_EARLY_ACCESS;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Project implements Domain, ExtraProperties {
    public static final String PROJECT_NAME = "PROJECT_NAME";
    public static final String PROJECT_VERSION = "PROJECT_VERSION";
    public static final String SNAPSHOT_PATTERN = "SNAPSHOT_PATTERN";
    public static final String DEFAULT_SNAPSHOT_PATTERN = ".*-SNAPSHOT";

    private final List<String> authors = new ArrayList<>();
    private final List<String> tags = new ArrayList<>();
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final Java java = new Java();
    private String name;
    private String version;
    private String description;
    private String longDescription;
    private String website;
    private String license;
    private String snapshotPattern;
    private Boolean snapshot;

    void setAll(Project project) {
        this.name = project.name;
        this.version = project.version;
        this.snapshotPattern = project.snapshotPattern;
        this.description = project.description;
        this.longDescription = project.longDescription;
        this.website = project.website;
        this.license = project.license;
        this.java.setAll(project.java);
        setAuthors(project.authors);
        setTags(project.tags);
        setExtraProperties(project.extraProperties);
    }

    @Override
    public String getPrefix() {
        return "project";
    }

    public String getEffectiveVersion() {
        if (isSnapshot()) {
            return TAG_EARLY_ACCESS;
        }

        return getResolvedVersion();
    }

    public boolean isSnapshot() {
        if (null == snapshot) {
            snapshot = getResolvedVersion().matches(getResolvedSnapshotPattern());
        }
        return snapshot;
    }

    public boolean isRelease() {
        return !isSnapshot();
    }

    public String getResolvedName() {
        return Env.resolve(PROJECT_NAME, name);
    }

    public String getResolvedVersion() {
        String resolvedVersion = Env.resolve(PROJECT_VERSION, version);
        // prevent NPE during validation
        return isNotBlank(resolvedVersion) ? resolvedVersion : "";
    }

    public String getResolvedSnapshotPattern() {
        snapshotPattern = Env.resolve(SNAPSHOT_PATTERN, snapshotPattern);
        if (isBlank(snapshotPattern)) {
            snapshotPattern = DEFAULT_SNAPSHOT_PATTERN;
        }
        return snapshotPattern;
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

    public String getSnapshotPattern() {
        return snapshotPattern;
    }

    public void setSnapshotPattern(String snapshotPattern) {
        this.snapshotPattern = snapshotPattern;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public Java getJava() {
        return java;
    }

    public void setJava(Java java) {
        this.java.setAll(java);
    }

    @Override
    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    @Override
    public void setExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.clear();
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public void addExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.putAll(extraProperties);
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors.clear();
        this.authors.addAll(authors);
    }

    public void addAuthors(List<String> authors) {
        this.authors.addAll(authors);
    }

    public void addAuthor(String author) {
        if (isNotBlank(author)) {
            this.authors.add(author.trim());
        }
    }

    public void removeAuthor(String author) {
        if (isNotBlank(author)) {
            this.authors.remove(author.trim());
        }
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }

    public void addTags(List<String> tags) {
        this.tags.addAll(tags);
    }

    public void addTag(String tag) {
        if (isNotBlank(tag)) {
            this.tags.add(tag.trim());
        }
    }

    public void removeTag(String tag) {
        if (isNotBlank(tag)) {
            this.tags.remove(tag.trim());
        }
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("version", version);
        map.put("snapshotPattern", snapshotPattern);
        map.put("snapshot", isSnapshot());
        map.put("description", description);
        map.put("longDescription", longDescription);
        map.put("website", website);
        map.put("license", license);
        map.put("authors", authors);
        map.put("tags", tags);
        map.put("extraProperties", getResolvedExtraProperties());
        if (java.isEnabled()) {
            map.put("java", java.asMap(full));
        }
        return map;
    }
}
