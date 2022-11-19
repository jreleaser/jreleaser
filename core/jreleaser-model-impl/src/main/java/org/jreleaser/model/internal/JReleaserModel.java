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
package org.jreleaser.model.internal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.mustachejava.TemplateFunction;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Constants;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.model.internal.announce.Announce;
import org.jreleaser.model.internal.assemble.Assemble;
import org.jreleaser.model.internal.checksum.Checksum;
import org.jreleaser.model.internal.deploy.Deploy;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.download.Download;
import org.jreleaser.model.internal.environment.Environment;
import org.jreleaser.model.internal.extensions.Extension;
import org.jreleaser.model.internal.files.Files;
import org.jreleaser.model.internal.hooks.Hooks;
import org.jreleaser.model.internal.packagers.Packagers;
import org.jreleaser.model.internal.platform.Platform;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.release.Release;
import org.jreleaser.model.internal.signing.Signing;
import org.jreleaser.model.internal.upload.Upload;
import org.jreleaser.mustache.MustacheUtils;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.version.SemanticVersion;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.getCapitalizedName;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class JReleaserModel {
    private final Environment environment = new Environment();
    private final Hooks hooks = new Hooks();
    private final Project project = new Project();
    private final Platform platform = new Platform();
    private final Release release = new Release();
    private final Packagers packagers = new Packagers();
    private final Announce announce = new Announce();
    private final Download download = new Download();
    private final Assemble assemble = new Assemble();
    private final Deploy deploy = new Deploy();
    private final Upload upload = new Upload();
    private final Checksum checksum = new Checksum();
    private final Signing signing = new Signing();
    private final Files files = new Files();
    private final Map<String, Distribution> distributions = new LinkedHashMap<>();
    private final Map<String, Extension> extensions = new LinkedHashMap<>();

    @JsonIgnore
    private final ZonedDateTime now;
    @JsonIgnore
    private final String timestamp;
    @JsonIgnore
    private org.jreleaser.model.api.JReleaserModel.Commit commit;

    private final org.jreleaser.model.api.JReleaserModel immutable = new org.jreleaser.model.api.JReleaserModel() {
        private Map<String, ? extends org.jreleaser.model.api.distributions.Distribution> distributions;
        private Map<String, ? extends org.jreleaser.model.api.extensions.Extension> extensions;

        @Override
        public ZonedDateTime getNow() {
            return now;
        }

        @Override
        public String getTimestamp() {
            return timestamp;
        }

        @Override
        public Commit getCommit() {
            return commit;
        }

        @Override
        public org.jreleaser.model.api.environment.Environment getEnvironment() {
            return environment.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.hooks.Hooks getHooks() {
            return hooks.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.platform.Platform getPlatform() {
            return platform.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.project.Project getProject() {
            return project.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.release.Release getRelease() {
            return release.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.Packagers getPackagers() {
            return packagers.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.announce.Announce getAnnounce() {
            return announce.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.assemble.Assemble getAssemble() {
            return assemble.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.download.Download getDownload() {
            return download.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.deploy.Deploy getDeploy() {
            return deploy.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.upload.Upload getUpload() {
            return upload.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.checksum.Checksum getChecksum() {
            return checksum.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.signing.Signing getSigning() {
            return signing.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.files.Files getFiles() {
            return files.asImmutable();
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.distributions.Distribution> getDistributions() {
            if (null == distributions) {
                distributions = JReleaserModel.this.distributions.values().stream()
                    .map(Distribution::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.distributions.Distribution::getName, identity()));
            }
            return distributions;
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.extensions.Extension> getExtensions() {
            if (null == extensions) {
                extensions = JReleaserModel.this.extensions.values().stream()
                    .map(Extension::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.extensions.Extension::getName, identity()));
            }
            return extensions;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(JReleaserModel.this.asMap(full));
        }
    };

    public JReleaserModel() {
        this.now = ZonedDateTime.now();
        this.timestamp = now.format(new DateTimeFormatterBuilder()
            .append(ISO_LOCAL_DATE_TIME)
            .optionalStart()
            .appendOffset("+HH:MM", "Z")
            .optionalEnd()
            .toFormatter());
    }

    public org.jreleaser.model.api.JReleaserModel asImmutable() {
        return immutable;
    }

    public ZonedDateTime getNow() {
        return now;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public org.jreleaser.model.api.JReleaserModel.Commit getCommit() {
        return commit;
    }

    public void setCommit(org.jreleaser.model.api.JReleaserModel.Commit commit) {
        this.commit = commit;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment.merge(environment);
    }

    public Hooks getHooks() {
        return hooks;
    }

    public void setHooks(Hooks hooks) {
        this.hooks.merge(hooks);
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform.merge(platform);
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project.merge(project);
    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release.merge(release);
    }

    public Packagers getPackagers() {
        return packagers;
    }

    public void setPackagers(Packagers packagers) {
        this.packagers.merge(packagers);
    }

    public Announce getAnnounce() {
        return announce;
    }

    public void setAnnounce(Announce announce) {
        this.announce.merge(announce);
    }

    public Assemble getAssemble() {
        return assemble;
    }

    public void setAssemble(Assemble assemble) {
        this.assemble.merge(assemble);
    }

    public Download getDownload() {
        return download;
    }

    public void setDownload(Download download) {
        this.download.merge(download);
    }

    public Deploy getDeploy() {
        return deploy;
    }

    public void setDeploy(Deploy deploy) {
        this.deploy.merge(deploy);
    }

    public Upload getUpload() {
        return upload;
    }

    public void setUpload(Upload upload) {
        this.upload.merge(upload);
    }

    public Checksum getChecksum() {
        return checksum;
    }

    public void setChecksum(Checksum checksum) {
        this.checksum.merge(checksum);
    }

    public Signing getSigning() {
        return signing;
    }

    public void setSigning(Signing signing) {
        this.signing.merge(signing);
    }

    public Files getFiles() {
        return files;
    }

    public void setFiles(Files files) {
        this.files.merge(files);
    }

    public List<Distribution> getActiveDistributions() {
        return distributions.values().stream()
            .filter(Distribution::isEnabled)
            .collect(toList());
    }

    public Map<String, Distribution> getDistributions() {
        return distributions;
    }

    public void setDistributions(Map<String, Distribution> distributions) {
        this.distributions.clear();
        this.distributions.putAll(distributions);
    }

    public void addDistribution(Distribution distribution) {
        this.distributions.put(distribution.getName(), distribution);
    }

    public Distribution findDistribution(String name) {
        if (isBlank(name)) {
            throw new JReleaserException(RB.$("ERROR_distribution_name_is_blank"));
        }

        if (distributions.containsKey(name)) {
            return distributions.get(name);
        }

        throw new JReleaserException(RB.$("ERROR_distribution_not_found", name));
    }

    public List<Extension> getActiveExtensions() {
        return extensions.values().stream()
            .filter(Extension::isEnabled)
            .collect(toList());
    }

    public Map<String, Extension> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, Extension> extensions) {
        this.extensions.clear();
        this.extensions.putAll(extensions);
    }

    public void addExtension(Extension extension) {
        this.extensions.put(extension.getName(), extension);
    }

    public ZonedDateTime resolveArchiveTimestamp() {
        if (null != commit) return commit.getTimestamp();
        return now;
    }

    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();

        List<Map<String, Object>> extensions = this.extensions.values()
            .stream()
            .filter(e -> full || e.isEnabled())
            .map(e -> e.asMap(full))
            .collect(toList());
        if (!extensions.isEmpty()) map.put("extensions", extensions);

        if (full || environment.isSet()) map.put("environment", environment.asMap(full));
        if (full || hooks.isSet()) map.put("hooks", hooks.asMap(full));
        map.put("project", project.asMap(full));
        if (full || platform.isSet()) map.put("platform", platform.asMap(full));
        map.put("release", release.asMap(full));
        map.put("checksum", checksum.asMap(full));
        if (full || signing.isEnabled()) map.put("signing", signing.asMap(full));
        if (full || announce.isEnabled()) map.put("announce", announce.asMap(full));
        if (!files.isEmpty()) map.put("files", files.asMap(full));
        if (full || packagers.hasEnabledPackagers()) map.put("packagers", packagers.asMap(full));
        if (full || download.isEnabled()) map.put("download", download.asMap(full));
        if (full || assemble.isEnabled()) map.put("assemble", assemble.asMap(full));
        if (full || deploy.isEnabled()) map.put("deploy", deploy.asMap(full));
        if (full || upload.isEnabled()) map.put("upload", upload.asMap(full));

        List<Map<String, Object>> distributions = this.distributions.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(toList());
        if (!distributions.isEmpty()) map.put("distributions", distributions);

        return map;
    }

    public Map<String, Object> props() {
        Map<String, Object> props = new LinkedHashMap<>();

        String jreleaserCreationStamp = String.format("Generated with JReleaser %s at %s",
            JReleaserVersion.getPlainVersion(), timestamp);
        props.put("jreleaserCreationStamp", jreleaserCreationStamp);

        fillProjectProperties(props, project);
        fillReleaserProperties(props, release);

        String osName = PlatformUtils.getDetectedOs();
        String osArch = PlatformUtils.getDetectedArch();
        props.put(Constants.KEY_OS_NAME, osName);
        props.put(Constants.KEY_OS_ARCH, osArch);
        props.put(Constants.KEY_OS_VERSION, PlatformUtils.getDetectedVersion());
        props.put(Constants.KEY_OS_PLATFORM, PlatformUtils.getCurrentFull());
        props.put(Constants.KEY_OS_PLATFORM_REPLACED, getPlatform().applyReplacements(PlatformUtils.getCurrentFull()));

        applyTemplates(props, project.getResolvedExtraProperties());
        props.put(Constants.KEY_ZONED_DATE_TIME_NOW, now);
        props.put(ReleaserDownloadUrl.NAME, new ReleaserDownloadUrl());

        return props;
    }

    private void fillProjectProperties(Map<String, Object> props, Project project) {
        props.putAll(environment.getProperties());
        props.putAll(environment.getSourcedProperties());
        props.put(Constants.KEY_TIMESTAMP, timestamp);
        if (commit != null) {
            props.put(Constants.KEY_COMMIT_SHORT_HASH, commit.getShortHash());
            props.put(Constants.KEY_COMMIT_FULL_HASH, commit.getFullHash());
        }
        props.put(Constants.KEY_PROJECT_NAME, project.getName());
        props.put(Constants.KEY_PROJECT_NAME_CAPITALIZED, getCapitalizedName(project.getName()));
        props.put(Constants.KEY_PROJECT_VERSION, project.getVersion());
        props.put(Constants.KEY_PROJECT_STEREOTYPE, project.getStereotype());
        props.put(Constants.KEY_PROJECT_EFFECTIVE_VERSION, project.getEffectiveVersion());
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
            SemanticVersion jv = SemanticVersion.of(project.getJava().getVersion());
            props.put(Constants.KEY_PROJECT_JAVA_VERSION_MAJOR, jv.getMajor());
            if (jv.hasMinor()) props.put(Constants.KEY_PROJECT_JAVA_VERSION_MINOR, jv.getMinor());
            if (jv.hasPatch()) props.put(Constants.KEY_PROJECT_JAVA_VERSION_PATCH, jv.getPatch());
            if (jv.hasTag()) props.put(Constants.KEY_PROJECT_JAVA_VERSION_TAG, jv.getTag());
            if (jv.hasBuild()) props.put(Constants.KEY_PROJECT_JAVA_VERSION_BUILD, jv.getBuild());
        }

        project.parseVersion();
        props.putAll(project.getResolvedExtraProperties());
    }

    private void fillReleaserProperties(Map<String, Object> props, Release release) {
        BaseReleaser service = release.getReleaser();
        props.put(Constants.KEY_REPO_HOST, service.getHost());
        props.put(Constants.KEY_REPO_OWNER, service.getOwner());
        props.put(Constants.KEY_REPO_NAME, service.getName());
        props.put(Constants.KEY_REPO_BRANCH, service.getBranch());
        props.put(Constants.KEY_REVERSE_REPO_HOST, service.getReverseRepoHost());
        props.put(Constants.KEY_CANONICAL_REPO_NAME, service.getCanonicalRepoName());
        props.put(Constants.KEY_TAG_NAME, service.getEffectiveTagName(this));
        props.put(Constants.KEY_RELEASE_NAME, service.getEffectiveReleaseName());
        props.put(Constants.KEY_MILESTONE_NAME, service.getMilestone().getEffectiveName());
        props.put(Constants.KEY_REPO_URL, service.getResolvedRepoUrl(this));
        props.put(Constants.KEY_REPO_CLONE_URL, service.getResolvedRepoCloneUrl(this));
        props.put(Constants.KEY_COMMIT_URL, service.getResolvedCommitUrl(this));
        props.put(Constants.KEY_SRC_URL, service.getResolvedSrcUrl(this));
        props.put(Constants.KEY_RELEASE_NOTES_URL, service.getResolvedReleaseNotesUrl(this));
        props.put(Constants.KEY_LATEST_RELEASE_URL, service.getResolvedLatestReleaseUrl(this));
        props.put(Constants.KEY_ISSUE_TRACKER_URL, service.getResolvedIssueTrackerUrl(this));
    }

    private final class ReleaserDownloadUrl implements TemplateFunction {
        private static final String NAME = "f_release_download_url";

        @Override
        public String apply(String input) {
            String format = "md";
            String artifactFile = "";
            String[] parts = input.split(":");
            if (parts.length == 1) {
                artifactFile = parts[0];
            } else if (parts.length == 2) {
                format = parts[0];
                artifactFile = parts[1];
            } else {
                throw new JReleaserException(RB.$("ERROR_invalid_function_input", input, NAME));
            }

            switch (format.toLowerCase(Locale.ENGLISH)) {
                case "md":
                    return ("[{{artifactFile}}](" + getRelease().getReleaser().getDownloadUrl() + ")")
                        .replace("{{artifactFile}}", artifactFile);
                case "adoc":
                    return ("link:" + getRelease().getReleaser().getDownloadUrl() + "[{{artifactFile}}]")
                        .replace("{{artifactFile}}", artifactFile);
                case "html":
                    return ("<a href=\"" + getRelease().getReleaser().getDownloadUrl() + "\">{{artifactFile}}</a>")
                        .replace("{{artifactFile}}", artifactFile);
            }

            throw new JReleaserException(RB.$("ERROR_invalid_function_input", input, NAME));
        }
    }
}
