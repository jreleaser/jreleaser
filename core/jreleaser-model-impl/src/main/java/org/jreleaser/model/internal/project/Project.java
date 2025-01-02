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
package org.jreleaser.model.internal.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.github.mustachejava.TemplateFunction;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.Constants;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.ExtraProperties;
import org.jreleaser.model.internal.common.Icon;
import org.jreleaser.model.internal.common.Java;
import org.jreleaser.model.internal.common.Screenshot;
import org.jreleaser.mustache.MustacheUtils;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.util.Env;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.version.CalVer;
import org.jreleaser.version.ChronVer;
import org.jreleaser.version.CustomVersion;
import org.jreleaser.version.JavaModuleVersion;
import org.jreleaser.version.JavaRuntimeVersion;
import org.jreleaser.version.SemanticVersion;
import org.jreleaser.version.Version;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.JReleaserOutput.nag;
import static org.jreleaser.model.api.project.Project.DEFAULT_SNAPSHOT_PATTERN;
import static org.jreleaser.model.api.project.Project.PROJECT_NAME;
import static org.jreleaser.model.api.project.Project.PROJECT_SNAPSHOT_LABEL;
import static org.jreleaser.model.api.project.Project.PROJECT_SNAPSHOT_PATTERN;
import static org.jreleaser.model.api.project.Project.PROJECT_VERSION;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.getCapitalizedName;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class Project extends AbstractModelObject<Project> implements Domain, ExtraProperties, Active.Releaseable {
    private static final long serialVersionUID = 7080958109557667812L;

    private final List<String> authors = new ArrayList<>();
    private final List<String> tags = new ArrayList<>();
    private final List<String> maintainers = new ArrayList<>();
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final Links links = new Links();
    private final Languages languages = new Languages();
    private final Snapshot snapshot = new Snapshot();
    private final List<Screenshot> screenshots = new ArrayList<>();
    private final List<Icon> icons = new ArrayList<>();
    private String name;
    private String version;
    private VersionPattern versionPattern = new VersionPattern();
    private String description;
    private String longDescription;
    private String license;
    private String inceptionYear;
    private String copyright;
    private String vendor;
    private Stereotype stereotype = Stereotype.NONE;

    @JsonIgnore
    private final org.jreleaser.model.api.project.Project immutable = new org.jreleaser.model.api.project.Project() {
        private static final long serialVersionUID = -2581431031545327120L;

        private List<? extends org.jreleaser.model.api.common.Screenshot> screenshots;
        private List<? extends org.jreleaser.model.api.common.Icon> icons;

        @Override
        public boolean isSnapshot() {
            return Project.this.isSnapshot();
        }

        @Override
        public boolean isRelease() {
            return Project.this.isRelease();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getVersion() {
            return version;
        }

        @Override
        public String getVersionPattern() {
            return versionPattern.toString();
        }

        @Override
        public Snapshot getSnapshot() {
            return snapshot.asImmutable();
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getLongDescription() {
            return longDescription;
        }

        @Override
        public String getWebsite() {
            return Project.this.getWebsite();
        }

        @Override
        public String getLicense() {
            return license;
        }

        @Override
        public String getLicenseUrl() {
            return Project.this.getLicenseUrl();
        }

        @Override
        public String getInceptionYear() {
            return inceptionYear;
        }

        @Override
        public String getCopyright() {
            return copyright;
        }

        @Override
        public String getVendor() {
            return vendor;
        }

        @Override
        public String getDocsUrl() {
            return Project.this.getDocsUrl();
        }

        @Override
        public Stereotype getStereotype() {
            return stereotype;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Screenshot> getScreenshots() {
            if (null == screenshots) {
                screenshots = Project.this.screenshots.stream()
                    .map(Screenshot::asImmutable)
                    .collect(toList());
            }
            return screenshots;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Icon> getIcons() {
            if (null == icons) {
                icons = Project.this.icons.stream()
                    .map(Icon::asImmutable)
                    .collect(toList());
            }
            return icons;
        }

        @Deprecated
        @Override
        public org.jreleaser.model.api.common.Java getJava() {
            return languages.getJava().asImmutable();
        }

        @Override
        public org.jreleaser.model.api.project.Languages getLanguages() {
            return languages.asImmutable();
        }

        @Override
        public List<String> getAuthors() {
            return unmodifiableList(authors);
        }

        @Override
        public List<String> getTags() {
            return unmodifiableList(tags);
        }

        @Override
        public List<String> getMaintainers() {
            return unmodifiableList(maintainers);
        }

        @Override
        public Links getLinks() {
            return links.asImmutable();
        }

        @Override
        public Version<?> version() {
            return Project.this.version();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Project.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return Project.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }
    };

    public org.jreleaser.model.api.project.Project asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Project source) {
        this.name = merge(this.name, source.name);
        this.version = merge(this.version, source.version);
        this.versionPattern = merge(this.versionPattern, source.versionPattern);
        this.description = merge(this.description, source.description);
        this.longDescription = merge(this.longDescription, source.longDescription);
        this.license = merge(this.license, source.license);
        this.inceptionYear = merge(this.inceptionYear, source.inceptionYear);
        this.copyright = merge(this.copyright, source.copyright);
        this.vendor = merge(this.vendor, source.vendor);
        this.stereotype = merge(this.stereotype, source.stereotype);
        setLanguages(source.languages);
        setSnapshot(source.snapshot);
        setAuthors(merge(this.authors, source.authors));
        setTags(merge(this.tags, source.tags));
        setMaintainers(merge(this.maintainers, source.maintainers));
        setExtraProperties(merge(this.extraProperties, source.extraProperties));
        setLinks(source.links);
        setScreenshots(merge(this.screenshots, source.screenshots));
        setIcons(merge(this.icons, source.icons));
    }

    @Override
    public String prefix() {
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

    @Override
    public boolean isRelease() {
        return !isSnapshot();
    }

    public String getResolvedName() {
        return Env.env(PROJECT_NAME, name);
    }

    public String getResolvedVersion() {
        String resolvedVersion = Env.env(PROJECT_VERSION, version);
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

    public String getVersionPattern() {
        return null != versionPattern ? versionPattern.toString() : "";
    }

    public void setVersionPattern(VersionPattern versionPattern) {
        this.versionPattern.merge(versionPattern);
    }

    public void setVersionPattern(String str) {
        setVersionPattern(VersionPattern.of(str));
    }

    public VersionPattern versionPattern() {
        return versionPattern;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Snapshot snapshot) {
        this.snapshot.merge(snapshot);
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

    @Deprecated
    @JsonPropertyDescription("project.website is deprecated since 1.2.0 and will be removed in 2.0.0. Use project.links.homepage instead")
    public String getWebsite() {
        return links.getHomepage();
    }

    @Deprecated
    public void setWebsite(String website) {
        nag("project.website is deprecated since 1.2.0 and will be removed in 2.0.0. Use project.links.homepage instead");
        links.setHomepage(website);
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    @Deprecated
    @JsonPropertyDescription("project.licenseUrl is deprecated since 1.2.0 and will be removed in 2.0.0. Use project.links.license instead")
    public String getLicenseUrl() {
        return links.getLicense();
    }

    @Deprecated
    public void setLicenseUrl(String licenseUrl) {
        nag("project.licenseUrl is deprecated since 1.2.0 and will be removed in 2.0.0. Use project.links.license instead");
        links.setLicense(licenseUrl);
    }

    public String getInceptionYear() {
        return inceptionYear;
    }

    public void setInceptionYear(String inceptionYear) {
        this.inceptionYear = inceptionYear;
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

    @Deprecated
    @JsonPropertyDescription("project.docsUrl is deprecated since 1.2.0 and will be removed in 2.0.0. Use project.links.documentation instead")
    public String getDocsUrl() {
        return links.getDocumentation();
    }

    @Deprecated
    public void setDocsUrl(String docsUrl) {
        nag("project.docsUrl is deprecated since 1.2.0 and will be removed in 2.0.0. Use project.links.documentation instead");
        links.setDocumentation(docsUrl);
    }

    public Stereotype getStereotype() {
        return stereotype;
    }

    public void setStereotype(Stereotype stereotype) {
        this.stereotype = stereotype;
    }

    public void setStereotype(String str) {
        setStereotype(Stereotype.of(str));
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

    @Deprecated
    @JsonPropertyDescription("project.java is deprecated since 1.16.0 and will be removed in 2.0.0. Use project.languages.java instead")
    public Java getJava() {
        return languages.getJava();
    }

    @Deprecated
    public void setJava(Java java) {
        nag("project.java is deprecated since 1.16.0 and will be removed in 2.0.0. Use project.languages.java instead");
        this.languages.getJava().merge(java);
    }

    public Languages getLanguages() {
        return languages;
    }

    public void setLanguages(Languages languages) {
        this.languages.merge(languages);
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }

    public List<String> getMaintainers() {
        return maintainers;
    }

    public void setMaintainers(List<String> maintainers) {
        this.maintainers.clear();
        this.maintainers.addAll(maintainers);
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links.merge(links);
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
        map.put("license", license);
        map.put("inceptionYear", inceptionYear);
        map.put("copyright", copyright);
        map.put("vendor", vendor);
        map.put("authors", authors);
        map.put("tags", tags);
        map.put("maintainers", maintainers);
        map.put("stereotype", stereotype);
        map.put("links", links.asMap(full));
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
        map.put("extraProperties", getExtraProperties());
        if (languages.isEnabled() || full) {
            map.put("languages", languages.asMap(full));
        }
        return map;
    }

    public void parseVersion() {
        String v = getResolvedVersion();
        if (isBlank(v)) return;

        switch (versionPattern().getType()) {
            case SEMVER:
                try {
                    SemanticVersion parsedVersion = SemanticVersion.of(v);
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
                    throw new JReleaserException(RB.$("ERROR_version_invalid", v, "semver"), e);
                }
                break;
            case JAVA_RUNTIME:
                try {
                    JavaRuntimeVersion parsedVersion = JavaRuntimeVersion.of(v);
                    addExtraProperty(Constants.KEY_VERSION_NUMBER, parsedVersion.getVersion());
                    if (parsedVersion.hasPrerelease()) {
                        addExtraProperty(Constants.KEY_VERSION_PRERELEASE, parsedVersion.getPrerelease());
                    }
                    if (parsedVersion.hasBuild()) {
                        addExtraProperty(Constants.KEY_VERSION_BUILD, parsedVersion.getBuild());
                    }
                    if (parsedVersion.hasOptional()) {
                        addExtraProperty(Constants.KEY_VERSION_OPTIONAL, parsedVersion.getOptional());
                    }
                } catch (IllegalArgumentException e) {
                    throw new JReleaserException(RB.$("ERROR_version_invalid", v, "Java runtime"), e);
                }
                break;
            case JAVA_MODULE:
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
                    throw new JReleaserException(RB.$("ERROR_version_invalid", v, "Java module"), e);
                }
                break;
            case CALVER:
                try {
                    CalVer parsedVersion = CalVer.of(versionPattern().getFormat(), v);
                    addExtraProperty(Constants.KEY_VERSION_NUMBER, v);
                    addExtraProperty(Constants.KEY_VERSION_YEAR, parsedVersion.getYear());
                    if (parsedVersion.hasMonth()) {
                        addExtraProperty(Constants.KEY_VERSION_MONTH, parsedVersion.getMonth());
                    }
                    if (parsedVersion.hasDay()) {
                        addExtraProperty(Constants.KEY_VERSION_DAY, parsedVersion.getDay());
                    }
                    if (parsedVersion.hasWeek()) {
                        addExtraProperty(Constants.KEY_VERSION_WEEK, parsedVersion.getWeek());
                    }
                    if (parsedVersion.hasMinor()) {
                        addExtraProperty(Constants.KEY_VERSION_MINOR, parsedVersion.getMinor());
                    }
                    if (parsedVersion.hasMicro()) {
                        addExtraProperty(Constants.KEY_VERSION_MICRO, parsedVersion.getMicro());
                    }
                    if (parsedVersion.hasModifier()) {
                        addExtraProperty(Constants.KEY_VERSION_MODIFIER, parsedVersion.getModifier());
                    }
                } catch (IllegalArgumentException e) {
                    throw new JReleaserException(RB.$("ERROR_version_invalid", v, "calver"), e);
                }
                break;
            case CHRONVER:
                try {
                    ChronVer parsedVersion = ChronVer.of(v);
                    addExtraProperty(Constants.KEY_VERSION_NUMBER, v);
                    addExtraProperty(Constants.KEY_VERSION_YEAR, parsedVersion.getYear());
                    addExtraProperty(Constants.KEY_VERSION_MONTH, parsedVersion.getMonth());
                    addExtraProperty(Constants.KEY_VERSION_DAY, parsedVersion.getDay());
                    if (parsedVersion.hasChangeset()) {
                        addExtraProperty(Constants.KEY_VERSION_MODIFIER, parsedVersion.getChangeset().toString());
                    }
                } catch (IllegalArgumentException e) {
                    throw new JReleaserException(RB.$("ERROR_version_invalid", v, "chronver"), e);
                }
                break;
            default:
                addExtraProperty(Constants.KEY_VERSION_NUMBER, v);
                // noop
        }

        String vn = (String) getExtraProperties().get(Constants.KEY_VERSION_NUMBER);
        String ev = getEffectiveVersion();
        addExtraProperty(Constants.KEY_VERSION_WITH_UNDERSCORES, underscore(v));
        addExtraProperty(Constants.KEY_VERSION_WITH_DASHES, dash(v));
        addExtraProperty(Constants.KEY_VERSION_NUMBER_WITH_UNDERSCORES, underscore(vn));
        addExtraProperty(Constants.KEY_VERSION_NUMBER_WITH_DASHES, dash(vn));
        if (isNotBlank(ev)) {
            addExtraProperty(Constants.KEY_EFFECTIVE_VERSION_WITH_UNDERSCORES, underscore(ev));
            addExtraProperty(Constants.KEY_EFFECTIVE_VERSION_WITH_DASHES, dash(ev));
        }
    }

    private String underscore(String input) {
        return input.replace(".", "_")
            .replace("-", "_")
            .replace("+", "_");
    }

    private String dash(String input) {
        return input.replace(".", "-")
            .replace("_", "-")
            .replace("+", "-");
    }

    public Version<?> version() {
        String v = getResolvedVersion();
        switch (versionPattern().getType()) {
            case SEMVER:
                return SemanticVersion.of(v);
            case JAVA_RUNTIME:
                return JavaRuntimeVersion.of(v);
            case JAVA_MODULE:
                return JavaModuleVersion.of(v);
            case CALVER:
                return CalVer.of(versionPattern().getFormat(), v);
            case CHRONVER:
                return ChronVer.of(v);
            case CUSTOM:
            default:
                return CustomVersion.of(v);
        }
    }

    public static class Snapshot extends AbstractModelObject<Snapshot> implements Domain {
        private static final long serialVersionUID = -4157019875957411109L;

        private Boolean enabled;
        private String pattern;
        private String label;
        private Boolean fullChangelog;
        @JsonIgnore
        private String cachedLabel;

        @JsonIgnore
        private final org.jreleaser.model.api.project.Project.Snapshot immutable = new org.jreleaser.model.api.project.Project.Snapshot() {
            private static final long serialVersionUID = 2581314557970795502L;

            @Override
            public String getPattern() {
                return pattern;
            }

            @Override
            public String getLabel() {
                return label;
            }

            @Override
            public boolean isFullChangelog() {
                return Snapshot.this.isFullChangelog();
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Snapshot.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.project.Project.Snapshot asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Snapshot source) {
            this.enabled = this.merge(this.enabled, source.enabled);
            this.pattern = this.merge(this.pattern, source.pattern);
            this.label = this.merge(this.label, source.label);
            this.fullChangelog = this.merge(this.fullChangelog, source.fullChangelog);
        }

        public boolean isSnapshot(String version) {
            if (null == enabled) {
                enabled = version.matches(getResolvedPattern());
            }
            return enabled;
        }

        public String getConfiguredPattern() {
            return Env.env(PROJECT_SNAPSHOT_PATTERN, pattern);
        }

        public String getResolvedPattern() {
            pattern = getConfiguredPattern();
            if (isBlank(pattern)) {
                pattern = DEFAULT_SNAPSHOT_PATTERN;
            }
            return pattern;
        }

        public String getConfiguredLabel() {
            return Env.env(PROJECT_SNAPSHOT_LABEL, label);
        }

        public String getResolvedLabel(JReleaserModel model) {
            if (isBlank(cachedLabel)) {
                cachedLabel = getConfiguredLabel();
            }

            if (isBlank(cachedLabel)) {
                cachedLabel = resolveTemplate(label, props(model));
            } else if (cachedLabel.contains("{{")) {
                cachedLabel = resolveTemplate(cachedLabel, props(model));
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

        public Boolean getFullChangelog() {
            return fullChangelog;
        }

        public boolean isFullChangelog() {
            return null != fullChangelog && fullChangelog;
        }

        public void setFullChangelog(Boolean fullChangelog) {
            this.fullChangelog = fullChangelog;
        }

        public boolean isFullChangelogSet() {
            return null != fullChangelog;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("enabled", enabled);
            map.put("pattern", getConfiguredPattern());
            map.put("label", getConfiguredLabel());
            map.put("fullChangelog", isFullChangelog());
            return map;
        }

        public TemplateContext props(JReleaserModel model) {
            // duplicate from JReleaserModel to avoid endless recursion
            TemplateContext props = new TemplateContext();
            Project project = model.getProject();
            props.setAll(model.getEnvironment().getProperties());
            props.setAll(model.getEnvironment().getSourcedProperties());
            props.set(Constants.KEY_PROJECT_NAME, project.getName());
            props.set(Constants.KEY_PROJECT_NAME_CAPITALIZED, getCapitalizedName(project.getName()));
            props.set(Constants.KEY_PROJECT_STEREOTYPE, project.getStereotype());
            props.set(Constants.KEY_PROJECT_VERSION, project.getVersion());
            props.set(Constants.KEY_PROJECT_SNAPSHOT, String.valueOf(project.isSnapshot()));
            if (isNotBlank(project.getDescription())) {
                props.set(Constants.KEY_PROJECT_DESCRIPTION, MustacheUtils.passThrough(project.getDescription()));
            }
            if (isNotBlank(project.getLongDescription())) {
                props.set(Constants.KEY_PROJECT_LONG_DESCRIPTION, MustacheUtils.passThrough(project.getLongDescription()));
            }
            props.set(Constants.KEY_PROJECT_LICENSE, project.getLicense());
            props.set(Constants.KEY_PROJECT_INCEPTION_YEAR, project.getInceptionYear());
            props.set(Constants.KEY_PROJECT_COPYRIGHT, project.getCopyright());
            props.set(Constants.KEY_PROJECT_VENDOR, project.getVendor());
            project.getLinks().fillProps(props);

            project.getLanguages().fillProperties(props);

            project.parseVersion();
            props.setAll(project.resolvedExtraProperties());

            String osName = PlatformUtils.getDetectedOs();
            String osArch = PlatformUtils.getDetectedArch();
            props.set(Constants.KEY_OS_NAME, osName);
            props.set(Constants.KEY_OS_ARCH, osArch);
            props.set(Constants.KEY_OS_VERSION, PlatformUtils.getDetectedVersion());
            props.set(Constants.KEY_OS_PLATFORM, PlatformUtils.getCurrentFull());
            props.set(Constants.KEY_OS_PLATFORM_REPLACED, model.getPlatform().applyReplacements(PlatformUtils.getCurrentFull()));

            applyTemplates(props, project.resolvedExtraProperties());
            props.set(Constants.KEY_ZONED_DATE_TIME_NOW, model.getNow());

            return props;
        }
    }

    public static class Links extends AbstractModelObject<Links> implements Domain {
        private static final String PROJECT_LINK = "projectLink";
        private static final long serialVersionUID = 1574571238759859477L;

        private String homepage;
        private String documentation;
        private String license;
        private String bugTracker;
        private String faq;
        private String help;
        private String donation;
        private String translate;
        private String contact;
        private String vcsBrowser;
        private String contribute;

        @JsonIgnore
        private final org.jreleaser.model.api.project.Project.Links immutable = new org.jreleaser.model.api.project.Project.Links() {
            private static final long serialVersionUID = 3891594676066031996L;

            @Override
            public String getHomepage() {
                return homepage;
            }

            @Override
            public String getDocumentation() {
                return documentation;
            }

            @Override
            public String getLicense() {
                return license;
            }

            @Override
            public String getBugTracker() {
                return bugTracker;
            }

            @Override
            public String getFaq() {
                return faq;
            }

            @Override
            public String getHelp() {
                return help;
            }

            @Override
            public String getDonation() {
                return donation;
            }

            @Override
            public String getTranslate() {
                return translate;
            }

            @Override
            public String getContact() {
                return contact;
            }

            @Override
            public String getVcsBrowser() {
                return vcsBrowser;
            }

            @Override
            public String getContribute() {
                return contribute;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Links.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.project.Project.Links asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Links source) {
            this.homepage = merge(this.homepage, source.homepage);
            this.documentation = merge(this.documentation, source.documentation);
            this.license = merge(this.license, source.license);
            this.bugTracker = merge(this.bugTracker, source.bugTracker);
            this.faq = merge(this.faq, source.faq);
            this.help = merge(this.help, source.help);
            this.donation = merge(this.donation, source.donation);
            this.translate = merge(this.translate, source.translate);
            this.contact = merge(this.contact, source.contact);
            this.vcsBrowser = merge(this.vcsBrowser, source.vcsBrowser);
            this.contribute = merge(this.contribute, source.contribute);
        }

        public String getHomepage() {
            return homepage;
        }

        public void setHomepage(String homepage) {
            this.homepage = homepage;
        }

        public String getDocumentation() {
            return documentation;
        }

        public void setDocumentation(String documentation) {
            this.documentation = documentation;
        }

        public String getLicense() {
            return license;
        }

        public void setLicense(String license) {
            this.license = license;
        }

        public String getBugTracker() {
            return bugTracker;
        }

        public void setBugTracker(String bugTracker) {
            this.bugTracker = bugTracker;
        }

        public String getFaq() {
            return faq;
        }

        public void setFaq(String faq) {
            this.faq = faq;
        }

        public String getHelp() {
            return help;
        }

        public void setHelp(String help) {
            this.help = help;
        }

        public String getDonation() {
            return donation;
        }

        public void setDonation(String donation) {
            this.donation = donation;
        }

        public String getTranslate() {
            return translate;
        }

        public void setTranslate(String translate) {
            this.translate = translate;
        }

        public String getContact() {
            return contact;
        }

        public void setContact(String contact) {
            this.contact = contact;
        }

        public String getVcsBrowser() {
            return vcsBrowser;
        }

        public void setVcsBrowser(String vcsBrowser) {
            this.vcsBrowser = vcsBrowser;
        }

        public String getContribute() {
            return contribute;
        }

        public void setContribute(String contribute) {
            this.contribute = contribute;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            if (isNotBlank(homepage)) map.put("homepage", homepage);
            if (isNotBlank(documentation)) map.put("documentation", documentation);
            if (isNotBlank(license)) map.put("license", license);
            if (isNotBlank(bugTracker)) map.put("bugTracker", bugTracker);
            if (isNotBlank(vcsBrowser)) map.put("vcsBrowser", vcsBrowser);
            if (isNotBlank(faq)) map.put("faq", faq);
            if (isNotBlank(help)) map.put("help", help);
            if (isNotBlank(donation)) map.put("donation", donation);
            if (isNotBlank(translate)) map.put("translate", translate);
            if (isNotBlank(contact)) map.put("contact", contact);
            if (isNotBlank(contribute)) map.put("contribute", contribute);
            return map;
        }

        public void fillProps(TemplateContext props) {
            props.set(PROJECT_LINK + "Homepage", homepage);
            props.set(PROJECT_LINK + "Documentation", documentation);
            props.set(PROJECT_LINK + "License", license);
            props.set(PROJECT_LINK + "BugTracker", bugTracker);
            props.set(PROJECT_LINK + "VcsBrowser", vcsBrowser);
            props.set(PROJECT_LINK + "Faq", faq);
            props.set(PROJECT_LINK + "Help", help);
            props.set(PROJECT_LINK + "Donation", donation);
            props.set(PROJECT_LINK + "Translate", translate);
            props.set(PROJECT_LINK + "Contact", contact);
            props.set(PROJECT_LINK + "Contribute", contribute);
            // TODO: Remove these in 2.0.0
            props.set(Constants.KEY_PROJECT_WEBSITE, homepage);
            props.set(Constants.KEY_PROJECT_DOCS_URL, documentation);
            props.set(Constants.KEY_PROJECT_LICENSE_URL, license);
        }

        public Collection<LinkTemplate> asLinkTemplates(boolean flatpak) {
            List<LinkTemplate> links = new ArrayList<>();
            if (isNotBlank(homepage)) links.add(new LinkTemplate("homepage", homepage));
            if (!flatpak && isNotBlank(documentation)) links.add(new LinkTemplate("documentation", documentation));
            if (!flatpak && isNotBlank(license)) links.add(new LinkTemplate("license", license));
            if (isNotBlank(bugTracker)) links.add(new LinkTemplate("bugtracker", bugTracker));
            if (isNotBlank(vcsBrowser)) links.add(new LinkTemplate("vcs-browser", vcsBrowser));
            if (isNotBlank(faq)) links.add(new LinkTemplate("faq", faq));
            if (isNotBlank(help)) links.add(new LinkTemplate("help", help));
            if (isNotBlank(donation)) links.add(new LinkTemplate("donation", donation));
            if (isNotBlank(translate)) links.add(new LinkTemplate("translate", translate));
            if (isNotBlank(contact)) links.add(new LinkTemplate("contact", contact));
            if (isNotBlank(contribute)) links.add(new LinkTemplate("contribute", contribute));
            return links;
        }

        public static final class LinkTemplate {
            private final String type;
            private final String url;

            public LinkTemplate(String type, String url) {
                this.type = type;
                this.url = url;
            }

            public String getType() {
                return type;
            }

            public TemplateFunction getUrl() {
                return s -> url;
            }
        }
    }

    public static class VersionPattern extends AbstractModelObject<VersionPattern> implements Serializable {
        private static final long serialVersionUID = -8292733451111227968L;

        private org.jreleaser.model.VersionPattern.Type type;
        private String format;

        @JsonIgnore
        private final org.jreleaser.model.api.project.Project.VersionPattern immutable = new org.jreleaser.model.api.project.Project.VersionPattern() {
            private static final long serialVersionUID = 1073045324421554619L;

            @Override
            public org.jreleaser.model.VersionPattern.Type getType() {
                return type;
            }

            @Override
            public String getFormat() {
                return format;
            }

            @Override
            public String toString() {
                return VersionPattern.this.toString();
            }
        };

        public VersionPattern() {
            this.type = org.jreleaser.model.VersionPattern.Type.SEMVER;
        }

        public org.jreleaser.model.api.project.Project.VersionPattern asImmutable() {
            return immutable;
        }

        public org.jreleaser.model.VersionPattern.Type getType() {
            return type;
        }

        public void setType(org.jreleaser.model.VersionPattern.Type type) {
            this.type = type;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        @Override
        public String toString() {
            String s = type.toString();
            switch (type) {
                case CALVER:
                case CUSTOM:
                    if (isNotBlank(format)) {
                        s += ":" + format;
                    }
                    break;
                default:
                    // noop
                    break;
            }
            return s;
        }

        @Override
        public void merge(VersionPattern source) {
            if (null != source) {
                this.type = merge(this.type, source.type);
                this.format = merge(this.format, source.format);
            }
        }

        public static VersionPattern of(String str) {
            if (isBlank(str)) return null;

            String[] parts = str.trim().split(":");

            VersionPattern vp = new VersionPattern();
            switch (parts.length) {
                case 1:
                    vp.setType(org.jreleaser.model.VersionPattern.Type.of(parts[0]));
                    break;
                case 2:
                    vp.setType(org.jreleaser.model.VersionPattern.Type.of(parts[0]));
                    vp.setFormat(parts[1].trim());
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            return vp;
        }
    }
}
