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

import org.jreleaser.model.releaser.spi.Commit;
import org.jreleaser.util.Constants;
import org.jreleaser.util.MustacheUtils;
import org.jreleaser.util.OsDetector;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.Version;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.jreleaser.util.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.getClassNameForLowerCaseHyphenSeparatedName;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserModel implements Domain {
    private final Environment environment = new Environment();
    private final Project project = new Project();
    private final Release release = new Release();
    private final Packagers packagers = new Packagers();
    private final Announce announce = new Announce();
    private final Assemble assemble = new Assemble();
    private final Upload upload = new Upload();
    private final Checksum checksum = new Checksum();
    private final Signing signing = new Signing();
    private final Files files = new Files();
    private final Map<String, Distribution> distributions = new LinkedHashMap<>();

    private final String timestamp;
    private Commit commit;

    public JReleaserModel() {
        this.timestamp = ZonedDateTime.now().format(new DateTimeFormatterBuilder()
            .append(ISO_LOCAL_DATE_TIME)
            .optionalStart()
            .appendOffset("+HH:MM", "Z")
            .optionalEnd()
            .toFormatter());
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Commit getCommit() {
        return commit;
    }

    public void setCommit(Commit commit) {
        this.commit = commit;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment.setAll(environment);
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project.setAll(project);
    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release.setAll(release);
    }

    public Packagers getPackagers() {
        return packagers;
    }

    public void setPackagers(Packagers packagers) {
        this.packagers.setAll(packagers);
    }

    public Announce getAnnounce() {
        return announce;
    }

    public void setAnnounce(Announce announce) {
        this.announce.setAll(announce);
    }

    public Assemble getAssemble() {
        return assemble;
    }

    public void setAssemble(Assemble assemble) {
        this.assemble.setAll(assemble);
    }

    public Upload getUpload() {
        return upload;
    }

    public void setUpload(Upload upload) {
        this.upload.setAll(upload);
    }

    public Checksum getChecksum() {
        return checksum;
    }

    public void setChecksum(Checksum checksum) {
        this.checksum.setAll(checksum);
    }

    public Signing getSigning() {
        return signing;
    }

    public void setSigning(Signing signing) {
        this.signing.setAll(signing);
    }

    public Files getFiles() {
        return files;
    }

    public void setFiles(Files files) {
        this.files.setAll(files);
    }

    public List<Distribution> getActiveDistributions() {
        return distributions.values().stream()
            .filter(Distribution::isEnabled)
            .collect(Collectors.toList());
    }

    public Map<String, Distribution> getDistributions() {
        return distributions;
    }

    public void setDistributions(Map<String, Distribution> distributions) {
        this.distributions.clear();
        this.distributions.putAll(distributions);
    }

    public void addDistributions(Map<String, Distribution> distributions) {
        this.distributions.putAll(distributions);
    }

    public void addDistribution(Distribution distribution) {
        this.distributions.put(distribution.getName(), distribution);
    }

    public Distribution findDistribution(String name) {
        if (isBlank(name)) {
            throw new JReleaserException("Distribution name must not be blank");
        }

        if (distributions.containsKey(name)) {
            return distributions.get(name);
        }

        throw new JReleaserException("Distribution '" + name + "' not found");
    }

    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (environment.isSet()) map.put("environment", environment.asMap(full));
        map.put("project", project.asMap(full));
        map.put("release", release.asMap(full));
        map.put("checksum", checksum.asMap(full));
        if (full || signing.isEnabled()) map.put("signing", signing.asMap(full));
        if (full || announce.isEnabled()) map.put("announce", announce.asMap(full));
        if (!files.isEmpty()) map.put("files", files.asMap(full));
        if (full || packagers.hasEnabledPackagers()) map.put("packagers", packagers.asMap(full));
        if (full || assemble.isEnabled()) map.put("assemble", assemble.asMap(full));
        if (full || upload.isEnabled()) map.put("upload", upload.asMap(full));

        List<Map<String, Object>> distributions = this.distributions.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(Collectors.toList());
        if (!distributions.isEmpty()) map.put("distributions", distributions);
        return map;
    }

    public Map<String, Object> props() {
        Map<String, Object> props = new LinkedHashMap<>();
        fillProjectProperties(props, project);
        fillReleaseProperties(props, release);

        String osName = PlatformUtils.getOsDetector().get(OsDetector.DETECTED_NAME);
        String osArch = PlatformUtils.getOsDetector().get(OsDetector.DETECTED_ARCH);
        props.put(Constants.KEY_OS_NAME, osName);
        props.put(Constants.KEY_OS_ARCH, osArch);
        props.put(Constants.KEY_OS_PLATFORM, osName + "-" + osArch);
        props.put(Constants.KEY_OS_VERSION, PlatformUtils.getOsDetector().get(OsDetector.DETECTED_VERSION));

        applyTemplates(props, project.getResolvedExtraProperties());

        return props;
    }

    private void fillProjectProperties(Map<String, Object> props, Project project) {
        props.putAll(environment.getProperties());
        props.put(Constants.KEY_TIMESTAMP, timestamp);
        props.put(Constants.KEY_COMMIT_SHORT_HASH, commit.getShortHash());
        props.put(Constants.KEY_COMMIT_FULL_HASH, commit.getFullHash());
        props.put(Constants.KEY_PROJECT_NAME, project.getName());
        props.put(Constants.KEY_PROJECT_NAME_CAPITALIZED, getClassNameForLowerCaseHyphenSeparatedName(project.getName()));
        props.put(Constants.KEY_PROJECT_VERSION, project.getVersion());
        props.put(Constants.KEY_PROJECT_EFFECTIVE_VERSION, project.getEffectiveVersion());
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
        props.put(Constants.KEY_PROJECT_AUTHORS_BY_SPACE, String.join(" ", project.getAuthors()));
        props.put(Constants.KEY_PROJECT_AUTHORS_BY_COMMA, String.join(",", project.getAuthors()));
        props.put(Constants.KEY_PROJECT_TAGS_BY_SPACE, String.join(" ", project.getTags()));
        props.put(Constants.KEY_PROJECT_TAGS_BY_COMMA, String.join(",", project.getTags()));

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

        props.putAll(project.getResolvedExtraProperties());
    }

    private void fillReleaseProperties(Map<String, Object> props, Release release) {
        GitService service = release.getGitService();
        props.put(Constants.KEY_REPO_HOST, service.getHost());
        props.put(Constants.KEY_REPO_OWNER, service.getOwner());
        props.put(Constants.KEY_REPO_NAME, service.getName());
        props.put(Constants.KEY_REPO_BRANCH, service.getBranch());
        props.put(Constants.KEY_TAG_NAME, service.getEffectiveTagName(this));
        props.put(Constants.KEY_RELEASE_NAME, service.getEffectiveReleaseName());
        props.put(Constants.KEY_MILESTONE_NAME, service.getMilestone().getEffectiveName());
        props.put(Constants.KEY_REVERSE_REPO_HOST, service.getReverseRepoHost());
        props.put(Constants.KEY_CANONICAL_REPO_NAME, service.getCanonicalRepoName());
        props.put(Constants.KEY_REPO_URL, service.getResolvedRepoUrl(this));
        props.put(Constants.KEY_REPO_CLONE_URL, service.getResolvedRepoCloneUrl(this));
        props.put(Constants.KEY_COMMIT_URL, service.getResolvedCommitUrl(this));
        props.put(Constants.KEY_RELEASE_NOTES_URL, service.getResolvedReleaseNotesUrl(this));
        props.put(Constants.KEY_LATEST_RELEASE_URL, service.getResolvedLatestReleaseUrl(this));
        props.put(Constants.KEY_ISSUE_TRACKER_URL, service.getResolvedIssueTrackerUrl(this));
    }
}
