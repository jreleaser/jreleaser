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

import com.github.mustachejava.TemplateFunction;
import org.jreleaser.bundle.RB;
import org.jreleaser.util.CalVer;
import org.jreleaser.util.ChronVer;
import org.jreleaser.util.Constants;
import org.jreleaser.util.CustomVersion;
import org.jreleaser.util.Env;
import org.jreleaser.util.JReleaserException;
import org.jreleaser.util.JavaModuleVersion;
import org.jreleaser.util.JavaRuntimeVersion;
import org.jreleaser.util.MustacheUtils;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.SemVer;
import org.jreleaser.util.Version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jreleaser.util.JReleaserOutput.nag;
import static org.jreleaser.util.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.getClassNameForLowerCaseHyphenSeparatedName;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Project extends AbstractModelObject<Project> implements Domain, ExtraProperties {
    public static final String PROJECT_NAME = "PROJECT_NAME";
    public static final String PROJECT_VERSION = "PROJECT_VERSION";
    public static final String PROJECT_VERSION_PATTERN = "PROJECT_VERSION_PATTERN";
    public static final String PROJECT_SNAPSHOT_PATTERN = "PROJECT_SNAPSHOT_PATTERN";
    public static final String PROJECT_SNAPSHOT_LABEL = "PROJECT_SNAPSHOT_LABEL";
    public static final String PROJECT_SNAPSHOT_FULL_CHANGELOG = "PROJECT_SNAPSHOT_FULL_CHANGELOG";
    public static final String DEFAULT_SNAPSHOT_PATTERN = ".*-SNAPSHOT";
    public static final String DEFAULT_SNAPSHOT_LABEL = "early-access";

    private final List<String> authors = new ArrayList<>();
    private final List<String> tags = new ArrayList<>();
    private final List<String> maintainers = new ArrayList<>();
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final Links links = new Links();
    private final Java java = new Java();
    private final Snapshot snapshot = new Snapshot();
    private final List<Screenshot> screenshots = new ArrayList<>();
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

    @Override
    public void freeze() {
        super.freeze();
        links.freeze();
        java.freeze();
        snapshot.freeze();
        versionPattern.freeze();
        screenshots.forEach(ModelObject::freeze);
    }

    @Override
    public void merge(Project project) {
        freezeCheck();
        this.name = merge(this.name, project.name);
        this.version = merge(this.version, project.version);
        this.versionPattern = merge(this.versionPattern, project.versionPattern);
        this.description = merge(this.description, project.description);
        this.longDescription = merge(this.longDescription, project.longDescription);
        this.license = merge(this.license, project.license);
        this.inceptionYear = merge(this.inceptionYear, project.inceptionYear);
        this.copyright = merge(this.copyright, project.copyright);
        this.vendor = merge(this.vendor, project.vendor);
        this.stereotype = merge(this.stereotype, project.stereotype);
        setJava(project.java);
        setSnapshot(project.snapshot);
        setAuthors(merge(this.authors, project.authors));
        setTags(merge(this.tags, project.tags));
        setMaintainers(merge(this.maintainers, project.maintainers));
        setExtraProperties(merge(this.extraProperties, project.extraProperties));
        setLinks(project.links);
        setScreenshots(merge(this.screenshots, project.screenshots));
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
        freezeCheck();
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        freezeCheck();
        this.version = version;
    }

    public String getVersionPattern() {
        return versionPattern != null ? versionPattern.toString() : "";
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
        freezeCheck();
        this.description = description;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        freezeCheck();
        this.longDescription = longDescription;
    }

    @Deprecated
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
        freezeCheck();
        this.license = license;
    }

    @Deprecated
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
        freezeCheck();
        this.inceptionYear = inceptionYear;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        freezeCheck();
        this.copyright = copyright;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        freezeCheck();
        this.vendor = vendor;
    }

    @Deprecated
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
        freezeCheck();
        this.stereotype = stereotype;
    }

    public void setStereotype(String str) {
        setStereotype(Stereotype.of(str));
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

    public Java getJava() {
        return java;
    }

    public void setJava(Java java) {
        this.java.merge(java);
    }

    @Override
    public Map<String, Object> getExtraProperties() {
        return freezeWrap(extraProperties);
    }

    @Override
    public void setExtraProperties(Map<String, Object> extraProperties) {
        freezeCheck();
        this.extraProperties.clear();
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public void addExtraProperties(Map<String, Object> extraProperties) {
        freezeCheck();
        this.extraProperties.putAll(extraProperties);
    }

    public List<String> getAuthors() {
        return freezeWrap(authors);
    }

    public void setAuthors(List<String> authors) {
        freezeCheck();
        this.authors.clear();
        this.authors.addAll(authors);
    }

    public List<String> getTags() {
        return freezeWrap(tags);
    }

    public void setTags(List<String> tags) {
        freezeCheck();
        this.tags.clear();
        this.tags.addAll(tags);
    }

    public List<String> getMaintainers() {
        return freezeWrap(maintainers);
    }

    public void setMaintainers(List<String> maintainers) {
        freezeCheck();
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
        map.put("extraProperties", getResolvedExtraProperties());
        if (java.isEnabled()) {
            map.put("java", java.asMap(full));
        }
        return map;
    }

    public void parseVersion() {
        boolean isFrozen = frozen;

        try {
            frozen = false;
            doParseVersion();
        } finally {
            frozen = isFrozen;
        }
    }

    private void doParseVersion() {
        String v = getResolvedVersion();
        if (isBlank(v)) return;

        switch (versionPattern().getType()) {
            case SEMVER: {
                try {
                    SemVer parsedVersion = SemVer.of(v);
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
            }
            break;
            case JAVA_RUNTIME: {
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
                    throw new JReleaserException(RB.$("ERROR_version_invalid", v, "Java module"), e);
                }
            }
            break;
            case CALVER: {
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
            }
            break;
            case CHRONVER: {
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
            }
            break;
            default:
                addExtraProperty(Constants.KEY_VERSION_NUMBER, v);
                // noop
        }

        String vn = (String) getExtraProperties().get(Constants.KEY_VERSION_NUMBER);
        String ev = getEffectiveVersion();
        addExtraProperty(Constants.KEY_VERSION_WITH_UNDERSCORES, new MustacheUtils.UnderscoreFunction().apply(v));
        addExtraProperty(Constants.KEY_VERSION_WITH_DASHES, new MustacheUtils.DashFunction().apply(v));
        addExtraProperty(Constants.KEY_VERSION_NUMBER_WITH_UNDERSCORES, new MustacheUtils.UnderscoreFunction().apply(vn));
        addExtraProperty(Constants.KEY_VERSION_NUMBER_WITH_DASHES, new MustacheUtils.DashFunction().apply(vn));
        if (isNotBlank(ev)) {
            addExtraProperty(Constants.KEY_EFFECTIVE_VERSION_WITH_UNDERSCORES, new MustacheUtils.UnderscoreFunction().apply(ev));
            addExtraProperty(Constants.KEY_EFFECTIVE_VERSION_WITH_DASHES, new MustacheUtils.DashFunction().apply(ev));
        }
    }

    public Version<?> version() {
        String v = getResolvedVersion();
        switch (versionPattern().getType()) {
            case SEMVER:
                return SemVer.of(v);
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
        private Boolean enabled;
        private String pattern;
        private String label;
        private Boolean fullChangelog;
        private String cachedLabel;

        @Override
        public void merge(Snapshot snapshot) {
            freezeCheck();
            this.enabled = this.merge(this.enabled, snapshot.enabled);
            this.pattern = this.merge(this.pattern, snapshot.pattern);
            this.label = this.merge(this.label, snapshot.label);
            this.fullChangelog = this.merge(this.fullChangelog, snapshot.fullChangelog);
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
            freezeCheck();
            this.pattern = pattern;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            freezeCheck();
            this.label = label;
        }

        public Boolean getFullChangelog() {
            return fullChangelog;
        }

        public boolean isFullChangelog() {
            return fullChangelog != null && fullChangelog;
        }

        public void setFullChangelog(Boolean fullChangelog) {
            freezeCheck();
            this.fullChangelog = fullChangelog;
        }

        public boolean isFullChangelogSet() {
            return fullChangelog != null;
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

        public Map<String, Object> props(JReleaserModel model) {
            // duplicate from JReleaserModel to avoid endless recursion
            Map<String, Object> props = new LinkedHashMap<>();
            Project project = model.getProject();
            props.putAll(model.getEnvironment().getProperties());
            props.putAll(model.getEnvironment().getSourcedProperties());
            props.put(Constants.KEY_PROJECT_NAME, project.getName());
            props.put(Constants.KEY_PROJECT_NAME_CAPITALIZED, getClassNameForLowerCaseHyphenSeparatedName(project.getName()));
            props.put(Constants.KEY_PROJECT_STEREOTYPE, project.getStereotype());
            props.put(Constants.KEY_PROJECT_VERSION, project.getVersion());
            props.put(Constants.KEY_PROJECT_SNAPSHOT, String.valueOf(project.isSnapshot()));
            if (isNotBlank(project.getDescription())) {
                props.put(Constants.KEY_PROJECT_DESCRIPTION, MustacheUtils.passThrough(project.getDescription()));
            }
            if (isNotBlank(project.getLongDescription())) {
                props.put(Constants.KEY_PROJECT_LONG_DESCRIPTION, MustacheUtils.passThrough(project.getLongDescription()));
            }
            if (isNotBlank(project.getLicense())) {
                props.put(Constants.KEY_PROJECT_LICENSE, project.getLicense());
            }
            if (null != project.getInceptionYear()) {
                props.put(Constants.KEY_PROJECT_INCEPTION_YEAR, project.getInceptionYear());
            }
            if (isNotBlank(project.getCopyright())) {
                props.put(Constants.KEY_PROJECT_COPYRIGHT, project.getCopyright());
            }
            if (isNotBlank(project.getVendor())) {
                props.put(Constants.KEY_PROJECT_VENDOR, project.getVendor());
            }
            project.getLinks().fillProps(props);

            if (project.getJava().isEnabled()) {
                props.putAll(project.getJava().getResolvedExtraProperties());
                props.put(Constants.KEY_PROJECT_JAVA_GROUP_ID, project.getJava().getGroupId());
                props.put(Constants.KEY_PROJECT_JAVA_ARTIFACT_ID, project.getJava().getArtifactId());
                props.put(Constants.KEY_PROJECT_JAVA_VERSION, project.getJava().getVersion());
                props.put(Constants.KEY_PROJECT_JAVA_MAIN_CLASS, project.getJava().getMainClass());
                SemVer jv = SemVer.of(project.getJava().getVersion());
                props.put(Constants.KEY_PROJECT_JAVA_VERSION_MAJOR, jv.getMajor());
                if (jv.hasMinor()) props.put(Constants.KEY_PROJECT_JAVA_VERSION_MINOR, jv.getMinor());
                if (jv.hasPatch()) props.put(Constants.KEY_PROJECT_JAVA_VERSION_PATCH, jv.getPatch());
                if (jv.hasTag()) props.put(Constants.KEY_PROJECT_JAVA_VERSION_TAG, jv.getTag());
                if (jv.hasBuild()) props.put(Constants.KEY_PROJECT_JAVA_VERSION_BUILD, jv.getBuild());
            }

            project.parseVersion();
            props.putAll(project.getResolvedExtraProperties());

            String osName = PlatformUtils.getDetectedOs();
            String osArch = PlatformUtils.getDetectedArch();
            props.put(Constants.KEY_OS_NAME, osName);
            props.put(Constants.KEY_OS_ARCH, osArch);
            props.put(Constants.KEY_OS_VERSION, PlatformUtils.getDetectedVersion());
            props.put(Constants.KEY_OS_PLATFORM, PlatformUtils.getCurrentFull());
            props.put(Constants.KEY_OS_PLATFORM_REPLACED, model.getPlatform().applyReplacements(PlatformUtils.getCurrentFull()));

            applyTemplates(props, project.getResolvedExtraProperties());
            props.put(Constants.KEY_ZONED_DATE_TIME_NOW, model.getNow());
            MustacheUtils.applyFunctions(props);

            return props;
        }
    }

    public static class Links extends AbstractModelObject<Project.Links> implements Domain {
        private static final String PROJECT_LINK = "projectLink";

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

        @Override
        public void merge(Project.Links source) {
            freezeCheck();
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
            freezeCheck();
            this.homepage = homepage;
        }

        public String getDocumentation() {
            return documentation;
        }

        public void setDocumentation(String documentation) {
            freezeCheck();
            this.documentation = documentation;
        }

        public String getLicense() {
            return license;
        }

        public void setLicense(String license) {
            freezeCheck();
            this.license = license;
        }

        public String getBugTracker() {
            return bugTracker;
        }

        public void setBugTracker(String bugTracker) {
            freezeCheck();
            this.bugTracker = bugTracker;
        }

        public String getFaq() {
            return faq;
        }

        public void setFaq(String faq) {
            freezeCheck();
            this.faq = faq;
        }

        public String getHelp() {
            return help;
        }

        public void setHelp(String help) {
            freezeCheck();
            this.help = help;
        }

        public String getDonation() {
            return donation;
        }

        public void setDonation(String donation) {
            freezeCheck();
            this.donation = donation;
        }

        public String getTranslate() {
            return translate;
        }

        public void setTranslate(String translate) {
            freezeCheck();
            this.translate = translate;
        }

        public String getContact() {
            return contact;
        }

        public void setContact(String contact) {
            freezeCheck();
            this.contact = contact;
        }

        public String getVcsBrowser() {
            return vcsBrowser;
        }

        public void setVcsBrowser(String vcsBrowser) {
            freezeCheck();
            this.vcsBrowser = vcsBrowser;
        }

        public String getContribute() {
            return contribute;
        }

        public void setContribute(String contribute) {
            freezeCheck();
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

        public void fillProps(Map<String, Object> props) {
            if (isNotBlank(homepage)) props.put(PROJECT_LINK + "Homepage", homepage);
            if (isNotBlank(documentation)) props.put(PROJECT_LINK + "Documentation", documentation);
            if (isNotBlank(license)) props.put(PROJECT_LINK + "License", license);
            if (isNotBlank(bugTracker)) props.put(PROJECT_LINK + "BugTracker", bugTracker);
            if (isNotBlank(vcsBrowser)) props.put(PROJECT_LINK + "VcsBrowser", vcsBrowser);
            if (isNotBlank(faq)) props.put(PROJECT_LINK + "Faq", faq);
            if (isNotBlank(help)) props.put(PROJECT_LINK + "Help", help);
            if (isNotBlank(donation)) props.put(PROJECT_LINK + "Donation", donation);
            if (isNotBlank(translate)) props.put(PROJECT_LINK + "translate", translate);
            if (isNotBlank(contact)) props.put(PROJECT_LINK + "contact", contact);
            if (isNotBlank(contribute)) props.put(PROJECT_LINK + "contribute", contribute);
            if (isNotBlank(homepage)) props.put(Constants.KEY_PROJECT_WEBSITE, homepage);
            if (isNotBlank(documentation)) props.put(Constants.KEY_PROJECT_DOCS_URL, documentation);
            if (isNotBlank(license)) props.put(Constants.KEY_PROJECT_LICENSE_URL, license);
        }

        public Collection<AppdataLink> asAppdataLinks() {
            List<AppdataLink> links = new ArrayList<>();
            if (isNotBlank(homepage)) links.add(new AppdataLink("homepage", homepage));
            if (isNotBlank(bugTracker)) links.add(new AppdataLink("bugtracker", bugTracker));
            if (isNotBlank(faq)) links.add(new AppdataLink("faq", faq));
            if (isNotBlank(help)) links.add(new AppdataLink("help", help));
            if (isNotBlank(donation)) links.add(new AppdataLink("donation", donation));
            if (isNotBlank(translate)) links.add(new AppdataLink("translate", translate));
            if (isNotBlank(contact)) links.add(new AppdataLink("contact", contact));
            if (isNotBlank(vcsBrowser)) links.add(new AppdataLink("vcs-browser", vcsBrowser));
            if (isNotBlank(contribute)) links.add(new AppdataLink("contribute", contribute));
            return links;
        }

        public static final class AppdataLink {
            private String type;
            private String url;

            public AppdataLink(String type, String url) {
                this.type = type;
                this.url = url;
            }

            public String getType() {
                return type;
            }

            public TemplateFunction getUrl() {
                return (s) -> url;
            }
        }
    }
}
