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

import org.jreleaser.util.Constants;
import org.jreleaser.util.Env;
import org.jreleaser.util.JavaModuleVersion;
import org.jreleaser.util.MustacheUtils;
import org.jreleaser.util.OsDetector;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.Version;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.getClassNameForLowerCaseHyphenSeparatedName;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Project implements Domain, ExtraProperties {
    public static final String PROJECT_NAME = "PROJECT_NAME";
    public static final String PROJECT_VERSION = "PROJECT_VERSION";
    public static final String PROJECT_VERSION_PATTERN = "PROJECT_VERSION_PATTERN";
    public static final String PROJECT_SNAPSHOT_PATTERN = "PROJECT_SNAPSHOT_PATTERN";
    public static final String PROJECT_SNAPSHOT_LABEL = "PROJECT_SNAPSHOT_LABEL";
    public static final String DEFAULT_SNAPSHOT_PATTERN = ".*-SNAPSHOT";
    public static final String DEFAULT_SNAPSHOT_LABEL = "early-access";

    private final List<String> authors = new ArrayList<>();
    private final List<String> tags = new ArrayList<>();
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final Java java = new Java();
    private final Snapshot snapshot = new Snapshot();
    private String name;
    private String version;
    private VersionPattern versionPattern;
    private String description;
    private String longDescription;
    private String website;
    private String license;
    private String copyright;
    private String vendor;
    private String docsUrl;

    void setAll(Project project) {
        this.name = project.name;
        this.version = project.version;
        this.versionPattern = project.versionPattern;
        this.description = project.description;
        this.longDescription = project.longDescription;
        this.website = project.website;
        this.license = project.license;
        this.copyright = project.copyright;
        this.vendor = project.vendor;
        this.docsUrl = project.docsUrl;
        setJava(project.java);
        setSnapshot(project.snapshot);
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
            return getSnapshot().getEffectiveLabel();
        }

        return getResolvedVersion();
    }

    public boolean isSnapshot() {
        return snapshot.isSnapshot(getResolvedVersion());
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

    public void setVersionPattern(String str) {
        this.versionPattern = VersionPattern.of(str);
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Snapshot snapshot) {
        this.snapshot.setAll(snapshot);
    }

    @Deprecated
    public String getSnapshotPattern() {
        return this.snapshot.getPattern();
    }

    @Deprecated
    public void setSnapshotPattern(String snapshotPattern) {
        System.out.println("project.snapshotPattern has been deprecated since 0.6.0 and will be removed in the future. Use project.snapshot.pattern instead");
        this.snapshot.setPattern(snapshotPattern);
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
        map.put("versionPattern", versionPattern);
        map.put("snapshot", snapshot.asMap(full));
        map.put("description", description);
        map.put("longDescription", longDescription);
        map.put("website", website);
        map.put("docsUrl", docsUrl);
        map.put("license", license);
        map.put("copyright", copyright);
        map.put("vendor", vendor);
        map.put("authors", authors);
        map.put("tags", tags);
        map.put("extraProperties", getResolvedExtraProperties());
        if (java.isEnabled()) {
            map.put("java", java.asMap(full));
        }
        return map;
    }

    public void parseVersion() {
        String v = getResolvedVersion();
        if (isBlank(v)) return;

        switch (getVersionPattern()) {
            case SEMVER: {
                try {
                    Version parsedVersion = Version.of(v);
                    StringBuilder vn = new StringBuilder().append(parsedVersion.getMajor());
                    addExtraProperty(Constants.KEY_VERSION_MAJOR, parsedVersion.getMajor());
                    if (parsedVersion.hasMinor()) {
                        vn.append(".").append(parsedVersion.getMinor());
                        addExtraProperty(Constants.KEY_VERSION_MINOR, parsedVersion.getMinor());
                    }
                    if (parsedVersion.hasPatch()) {
                        vn.append(".").append(parsedVersion.getPatch());
                        addExtraProperty(Constants.KEY_VERSION_PATCH, parsedVersion.getPatch());
                    }
                    addExtraProperty(Constants.KEY_VERSION_NUMBER, vn.toString());
                    if (parsedVersion.hasTag()) {
                        addExtraProperty(Constants.KEY_VERSION_TAG, parsedVersion.getTag());
                    }
                    if (parsedVersion.hasBuild()) {
                        addExtraProperty(Constants.KEY_VERSION_BUILD, parsedVersion.getBuild());
                    }
                } catch (IllegalArgumentException e) {
                    throw new JReleaserException("Version '" + v + "' does not follow the semver spec", e);
                }
            }
            break;
            case JAVA_MODULE: {
                try {
                    JavaModuleVersion parsedVersion = JavaModuleVersion.of(v);
                    addExtraProperty(Constants.KEY_VERSION_NUMBER, parsedVersion.getVersion());
                    if (parsedVersion.hasPrerelease()) {
                        addExtraProperty(Constants.KEY_VERSION_PRERELEASE, parsedVersion.getPrerelease());
                    }
                    if (parsedVersion.hasBuild()) {
                        addExtraProperty(Constants.KEY_VERSION_BUILD, parsedVersion.getBuild());
                    }
                } catch (IllegalArgumentException e) {
                    throw new JReleaserException("Version '" + v + "' does not follow the Java module spec", e);
                }
            }
            break;
            default:
                addExtraProperty(Constants.KEY_VERSION_NUMBER, v);
                // noop
        }

        String vn = (String) getExtraProperties().get(Constants.KEY_VERSION_NUMBER);
        String ev = getEffectiveVersion();
        addExtraProperty(Constants.KEY_VERSION_WITH_UNDERSCORES, v.replace(".", "_").replace("-", "_"));
        addExtraProperty(Constants.KEY_VERSION_WITH_DASHES, v.replace(".", "-").replace("_", "-"));
        addExtraProperty(Constants.KEY_VERSION_NUMBER_WITH_UNDERSCORES, vn.replace(".", "_").replace("-", "_"));
        addExtraProperty(Constants.KEY_VERSION_NUMBER_WITH_DASHES, vn.replace(".", "-").replace("_", "-"));
        if (isNotBlank(ev)) {
            addExtraProperty(Constants.KEY_EFFECTIVE_VERSION_WITH_UNDERSCORES, ev.replace(".", "_").replace("-", "_"));
            addExtraProperty(Constants.KEY_EFFECTIVE_VERSION_WITH_DASHES, ev.replace(".", "-").replace("_", "-"));
        }
    }

    public static class Snapshot implements Domain {
        private Boolean enabled;
        private String pattern;
        private String label;
        private String cachedLabel;

        void setAll(Snapshot snapshot) {
            this.enabled = snapshot.enabled;
            this.pattern = snapshot.pattern;
            this.label = snapshot.label;
        }

        public boolean isSnapshot(String version) {
            if (null == enabled) {
                enabled = version.matches(getResolvedPattern());
            }
            return enabled;
        }

        public String getConfiguredPattern() {
            return Env.resolve(PROJECT_SNAPSHOT_PATTERN, pattern);
        }

        public String getResolvedPattern() {
            pattern = getConfiguredPattern();
            if (isBlank(pattern)) {
                pattern = DEFAULT_SNAPSHOT_PATTERN;
            }
            return pattern;
        }

        public String getConfiguredLabel() {
            return Env.resolve(PROJECT_SNAPSHOT_LABEL, label);
        }

        public String getResolvedLabel(JReleaserModel model) {
            if (isBlank(cachedLabel)) {
                cachedLabel = getConfiguredLabel();
            }

            if (isBlank(cachedLabel)) {
                cachedLabel = applyTemplate(label, props(model));
            } else if (cachedLabel.contains("{{")) {
                cachedLabel = applyTemplate(cachedLabel, props(model));
            }

            return cachedLabel;
        }

        public String getEffectiveLabel() {
            return cachedLabel;
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

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("enabled", enabled);
            map.put("pattern", getConfiguredPattern());
            map.put("label", getConfiguredLabel());
            return map;
        }

        public Map<String, Object> props(JReleaserModel model) {
            // duplicate from JReleaserModel to avoid endless recursion
            Map<String, Object> props = new LinkedHashMap<>();
            Project project = model.getProject();
            props.putAll(model.getEnvironment().getProperties());
            props.put(Constants.KEY_PROJECT_NAME, project.getName());
            props.put(Constants.KEY_PROJECT_NAME_CAPITALIZED, getClassNameForLowerCaseHyphenSeparatedName(project.getName()));
            props.put(Constants.KEY_PROJECT_VERSION, project.getVersion());
            props.put(Constants.KEY_PROJECT_SNAPSHOT, String.valueOf(project.isSnapshot()));
            if (isNotBlank(project.getDescription())) {
                props.put(Constants.KEY_PROJECT_DESCRIPTION, MustacheUtils.passThrough(project.getDescription()));
            }
            if (isNotBlank(project.getLongDescription())) {
                props.put(Constants.KEY_PROJECT_LONG_DESCRIPTION, MustacheUtils.passThrough(project.getLongDescription()));
            }
            if (isNotBlank(project.getWebsite())) {
                props.put(Constants.KEY_PROJECT_WEBSITE, project.getWebsite());
            }
            if (isNotBlank(project.getLicense())) {
                props.put(Constants.KEY_PROJECT_LICENSE, project.getLicense());
            }
            if (isNotBlank(project.getDocsUrl())) {
                props.put(Constants.KEY_PROJECT_DOCS_URL, project.getDocsUrl());
            }
            if (isNotBlank(project.getCopyright())) {
                props.put(Constants.KEY_PROJECT_COPYRIGHT, project.getCopyright());
            }
            if (isNotBlank(project.getVendor())) {
                props.put(Constants.KEY_PROJECT_VENDOR, project.getVendor());
            }

            if (project.getJava().isEnabled()) {
                props.putAll(project.getJava().getResolvedExtraProperties());
                props.put(Constants.KEY_PROJECT_JAVA_GROUP_ID, project.getJava().getGroupId());
                props.put(Constants.KEY_PROJECT_JAVA_ARTIFACT_ID, project.getJava().getArtifactId());
                props.put(Constants.KEY_PROJECT_JAVA_VERSION, project.getJava().getVersion());
                props.put(Constants.KEY_PROJECT_JAVA_MAIN_CLASS, project.getJava().getMainClass());
                Version jv = Version.of(project.getJava().getVersion());
                props.put(Constants.KEY_PROJECT_JAVA_VERSION_MAJOR, jv.getMajor());
                if (jv.hasMinor()) props.put(Constants.KEY_PROJECT_JAVA_VERSION_MINOR, jv.getMinor());
                if (jv.hasPatch()) props.put(Constants.KEY_PROJECT_JAVA_VERSION_PATCH, jv.getPatch());
                if (jv.hasTag()) props.put(Constants.KEY_PROJECT_JAVA_VERSION_TAG, jv.getTag());
                if (jv.hasBuild()) props.put(Constants.KEY_PROJECT_JAVA_VERSION_BUILD, jv.getBuild());
            }

            project.parseVersion();
            props.putAll(project.getResolvedExtraProperties());

            String osName = PlatformUtils.getOsDetector().get(OsDetector.DETECTED_NAME);
            String osArch = PlatformUtils.getOsDetector().get(OsDetector.DETECTED_ARCH);
            props.put(Constants.KEY_OS_NAME, osName);
            props.put(Constants.KEY_OS_ARCH, osArch);
            props.put(Constants.KEY_OS_PLATFORM, osName + "-" + osArch);
            props.put(Constants.KEY_OS_VERSION, PlatformUtils.getOsDetector().get(OsDetector.DETECTED_VERSION));

            applyTemplates(props, project.getResolvedExtraProperties());

            return props;
        }
    }
}
