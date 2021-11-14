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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Project implements ExtraProperties {
    private final List<String> authors = new ArrayList<>();
    private final List<String> tags = new ArrayList<>();
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final Java java = new Java();
    private final Snapshot snapshot = new Snapshot();
    private String name;
    private String version;
    private VersionPattern versionPattern;
    private String snapshotPattern;
    private String description;
    private String longDescription;
    private String website;
    private String license;
    private String licenseUrl;
    private String copyright;
    private String vendor;
    private String docsUrl;

    void setAll(Project project) {
        this.name = project.name;
        this.version = project.version;
        this.versionPattern = project.versionPattern;
        this.snapshotPattern = project.snapshotPattern;
        this.description = project.description;
        this.longDescription = project.longDescription;
        this.website = project.website;
        this.license = project.license;
        this.licenseUrl = project.licenseUrl;
        this.copyright = project.copyright;
        this.vendor = project.vendor;
        this.docsUrl = project.docsUrl;
        setJava(project.java);
        setSnapshot(project.snapshot);
        setAuthors(project.authors);
        setTags(project.tags);
        setExtraProperties(project.extraProperties);
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

    public VersionPattern getVersionPattern() {
        return versionPattern;
    }

    public void setVersionPattern(VersionPattern versionPattern) {
        this.versionPattern = versionPattern;
    }

    public String resolveVersionPattern() {
        return versionPattern != null ? versionPattern.name() : null;
    }

    @Deprecated
    public String getSnapshotPattern() {
        return snapshotPattern;
    }

    @Deprecated
    public void setSnapshotPattern(String snapshotPattern) {
        this.snapshotPattern = snapshotPattern;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Snapshot snapshot) {
        this.snapshot.setAll(snapshot);
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

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getDocsUrl() {
        return docsUrl;
    }

    public void setDocsUrl(String docsUrl) {
        this.docsUrl = docsUrl;
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

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors.clear();
        this.authors.addAll(authors);
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }

    public static class Snapshot {
        private String pattern;
        private String label;
        private Boolean fullChangelog;

        void setAll(Snapshot snapshot) {
            this.pattern = snapshot.pattern;
            this.label = snapshot.label;
            this.fullChangelog = snapshot.fullChangelog;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public boolean isFullChangelog() {
            return fullChangelog != null && fullChangelog;
        }

        public void setFullChangelog(Boolean fullChangelog) {
            this.fullChangelog = fullChangelog;
        }

        public boolean isFullChangelogSet() {
            return fullChangelog != null;
        }
    }
}
