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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.util.Constants;
import org.jreleaser.util.Env;
import org.jreleaser.util.MustacheUtils;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.SemVer;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.getClassNameForLowerCaseHyphenSeparatedName;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class GitService<S extends GitService<S>> extends AbstractModelObject<S> implements Releaser, CommitAuthorAware, OwnerAware, TimeoutAware {
    public static final String KEY_SKIP_RELEASE = "skipRelease";
    public static final String KEY_SKIP_RELEASE_SIGNATURES = "skipReleaseSignatures";

    public static final String TAG_NAME = "TAG_NAME";
    public static final String PREVIOUS_TAG_NAME = "PREVIOUS_TAG_NAME";
    public static final String RELEASE_NAME = "RELEASE_NAME";
    public static final String OVERWRITE = "OVERWRITE";
    public static final String UPDATE = "UPDATE";
    public static final String PRERELEASE = "PRERELEASE";
    public static final String DRAFT = "DRAFT";
    public static final String SKIP_TAG = "SKIP_TAG";
    public static final String SKIP_RELEASE = "SKIP_RELEASE";
    public static final String BRANCH = "BRANCH";
    public static final String PRERELEASE_PATTERN = "PRERELEASE_PATTERN";

    @JsonIgnore
    protected final String serviceName;
    protected final Changelog changelog = new Changelog();
    protected final Milestone milestone = new Milestone();
    protected final CommitAuthor commitAuthor = new CommitAuthor();
    protected final Update update = new Update();
    protected final Prerelease prerelease = new Prerelease();
    @JsonIgnore
    protected final boolean releaseSupported;

    @JsonIgnore
    protected boolean match = true;
    protected Boolean enabled;
    protected String host;
    protected String owner;
    protected String name;
    protected String repoUrl;
    protected String repoCloneUrl;
    protected String commitUrl;
    protected String srcUrl;
    protected String downloadUrl;
    protected String releaseNotesUrl;
    protected String latestReleaseUrl;
    protected String issueTrackerUrl;
    protected String username;
    protected String token;
    protected String tagName;
    protected String previousTagName;
    protected String releaseName;
    protected String branch;
    protected Boolean sign;
    protected Boolean skipTag;
    protected Boolean skipRelease;
    protected Boolean overwrite;
    protected String apiEndpoint;
    protected int connectTimeout;
    protected int readTimeout;
    protected Boolean artifacts;
    protected Boolean files;
    protected Boolean checksums;
    protected Boolean signatures;
    protected Active uploadAssets;
    protected Boolean uploadAssetsEnabled;

    @JsonIgnore
    protected String cachedTagName;
    @JsonIgnore
    protected String cachedReleaseName;

    protected GitService(String serviceName, boolean releaseSupported) {
        this.serviceName = serviceName;
        this.releaseSupported = releaseSupported;
    }

    @Override
    public boolean isReleaseSupported() {
        return releaseSupported;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public void freeze() {
        super.freeze();
        changelog.freeze();
        milestone.freeze();
        commitAuthor.freeze();
        update.freeze();
        prerelease.freeze();
    }

    @Override
    public void merge(S service) {
        freezeCheck();
        this.match = service.match;
        this.enabled = merge(this.enabled, service.enabled);
        this.host = merge(this.host, service.host);
        this.owner = merge(this.owner, service.owner);
        this.name = merge(this.name, service.name);
        this.repoUrl = merge(this.repoUrl, service.repoUrl);
        this.repoCloneUrl = merge(this.repoCloneUrl, service.repoCloneUrl);
        this.commitUrl = merge(this.commitUrl, service.commitUrl);
        this.srcUrl = merge(this.srcUrl, service.srcUrl);
        this.downloadUrl = merge(this.downloadUrl, service.downloadUrl);
        this.releaseNotesUrl = merge(this.releaseNotesUrl, service.releaseNotesUrl);
        this.latestReleaseUrl = merge(this.latestReleaseUrl, service.latestReleaseUrl);
        this.issueTrackerUrl = merge(this.issueTrackerUrl, service.issueTrackerUrl);
        this.username = merge(this.username, service.username);
        this.token = merge(this.token, service.token);
        this.tagName = merge(this.tagName, service.tagName);
        this.previousTagName = merge(this.previousTagName, service.previousTagName);
        this.releaseName = merge(this.releaseName, service.releaseName);
        this.branch = merge(this.branch, service.branch);
        this.sign = merge(this.sign, service.sign);
        this.skipTag = merge(this.skipTag, service.skipTag);
        this.skipRelease = merge(this.skipRelease, service.skipRelease);
        this.overwrite = merge(this.overwrite, service.overwrite);
        this.apiEndpoint = merge(this.apiEndpoint, service.apiEndpoint);
        this.connectTimeout = merge(this.connectTimeout, service.connectTimeout);
        this.readTimeout = merge(this.readTimeout, service.readTimeout);
        this.artifacts = merge(this.artifacts, service.artifacts);
        this.files = merge(this.files, service.files);
        this.checksums = merge(this.checksums, service.checksums);
        this.signatures = merge(this.signatures, service.signatures);
        this.uploadAssets = merge(this.uploadAssets, service.uploadAssets);
        this.uploadAssetsEnabled = merge(this.uploadAssetsEnabled, service.uploadAssetsEnabled);
        setCommitAuthor(service.commitAuthor);
        setUpdate(service.update);
        setPrerelease(service.prerelease);
        setChangelog(service.changelog);
        setMilestone(service.milestone);
    }

    public String getCanonicalRepoName() {
        if (isNotBlank(owner)) {
            return owner + "/" + name;
        }
        return name;
    }

    public abstract String getReverseRepoHost();

    public String getConfiguredTagName() {
        return Env.env(TAG_NAME, tagName);
    }

    public String getConfiguredPreviousTagName() {
        return Env.env(PREVIOUS_TAG_NAME, previousTagName);
    }

    public String getResolvedTagName(JReleaserModel model) {
        if (isBlank(cachedTagName)) {
            cachedTagName = getConfiguredTagName();
        }

        if (isBlank(cachedTagName)) {
            cachedTagName = resolveTemplate(tagName, props(model));
        } else if (cachedTagName.contains("{{")) {
            cachedTagName = resolveTemplate(cachedTagName, props(model));
        }

        return cachedTagName;
    }

    public String getEffectiveTagName(JReleaserModel model) {
        if (model.getProject().isSnapshot()) {
            return model.getProject().getSnapshot().getResolvedLabel(model);
        }
        return cachedTagName;
    }

    public String getConfiguredReleaseName() {
        return Env.env(RELEASE_NAME, cachedReleaseName);
    }

    public String getResolvedReleaseName(JReleaserModel model) {
        if (isBlank(cachedReleaseName)) {
            cachedReleaseName = getConfiguredReleaseName();
        }

        if (isBlank(cachedReleaseName)) {
            cachedReleaseName = resolveTemplate(releaseName, props(model));
        } else if (cachedReleaseName.contains("{{")) {
            cachedReleaseName = resolveTemplate(cachedReleaseName, props(model));
        }

        return cachedReleaseName;
    }

    public String getEffectiveReleaseName() {
        return cachedReleaseName;
    }

    public String getResolvedRepoUrl(JReleaserModel model) {
        if (!releaseSupported) return "";
        return resolveTemplate(repoUrl, props(model));
    }

    public String getResolvedRepoCloneUrl(JReleaserModel model) {
        if (!releaseSupported) return "";
        return resolveTemplate(repoCloneUrl, props(model));
    }

    public String getResolvedRepoUrl(JReleaserModel model, String repoOwner, String repoName) {
        if (!releaseSupported) return "";
        Map<String, Object> props = props(model);
        props.put(Constants.KEY_REPO_OWNER, repoOwner);
        props.put(Constants.KEY_REPO_NAME, repoName);
        return resolveTemplate(repoUrl, props);
    }

    public String getResolvedRepoCloneUrl(JReleaserModel model, String repoOwner, String repoName) {
        if (!releaseSupported) return "";
        Map<String, Object> props = props(model);
        props.put(Constants.KEY_REPO_OWNER, repoOwner);
        props.put(Constants.KEY_REPO_NAME, repoName);
        return resolveTemplate(repoCloneUrl, props);
    }

    public String getResolvedCommitUrl(JReleaserModel model) {
        if (!releaseSupported) return "";
        return resolveTemplate(commitUrl, props(model));
    }

    public String getResolvedSrcUrl(JReleaserModel model) {
        if (!releaseSupported) return "";
        return resolveTemplate(srcUrl, props(model));
    }

    public String getResolvedDownloadUrl(JReleaserModel model) {
        if (!releaseSupported) return "";
        return resolveTemplate(downloadUrl, props(model));
    }

    public String getResolvedReleaseNotesUrl(JReleaserModel model) {
        if (!releaseSupported) return "";
        return resolveTemplate(releaseNotesUrl, props(model));
    }

    public String getResolvedLatestReleaseUrl(JReleaserModel model) {
        if (!releaseSupported) return "";
        return resolveTemplate(latestReleaseUrl, props(model));
    }

    public String getResolvedIssueTrackerUrl(JReleaserModel model) {
        if (!releaseSupported) return "";
        return resolveTemplate(issueTrackerUrl, props(model));
    }

    public boolean resolveUploadAssetsEnabled(Project project) {
        if (null == uploadAssets) {
            uploadAssets = Active.ALWAYS;
        }
        uploadAssetsEnabled = uploadAssets.check(project, this);
        return uploadAssetsEnabled;
    }

    public boolean isMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        freezeCheck();
        this.match = match;
    }

    @Override
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        freezeCheck();
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabledSet() {
        return enabled != null;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        freezeCheck();
        this.host = host;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public void setOwner(String owner) {
        freezeCheck();
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        freezeCheck();
        this.name = name;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        freezeCheck();
        this.repoUrl = repoUrl;
    }

    public String getRepoCloneUrl() {
        return repoCloneUrl;
    }

    public void setRepoCloneUrl(String repoCloneUrl) {
        freezeCheck();
        this.repoCloneUrl = repoCloneUrl;
    }

    public String getCommitUrl() {
        return commitUrl;
    }

    public void setCommitUrl(String commitUrl) {
        freezeCheck();
        this.commitUrl = commitUrl;
    }

    public String getSrcUrl() {
        return srcUrl;
    }

    public void setSrcUrl(String srcUrl) {
        freezeCheck();
        this.srcUrl = srcUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        freezeCheck();
        this.downloadUrl = downloadUrl;
    }

    public String getReleaseNotesUrl() {
        return releaseNotesUrl;
    }

    public void setReleaseNotesUrl(String releaseNotesUrl) {
        freezeCheck();
        this.releaseNotesUrl = releaseNotesUrl;
    }

    public String getLatestReleaseUrl() {
        return latestReleaseUrl;
    }

    public void setLatestReleaseUrl(String latestReleaseUrl) {
        freezeCheck();
        this.latestReleaseUrl = latestReleaseUrl;
    }

    public String getIssueTrackerUrl() {
        return issueTrackerUrl;
    }

    public void setIssueTrackerUrl(String issueTrackerUrl) {
        freezeCheck();
        this.issueTrackerUrl = issueTrackerUrl;
    }

    public String getResolvedToken() {
        return Env.env(Env.toVar(getServiceName()) + "_TOKEN", token);
    }

    public String getResolvedUsername() {
        return Env.env(Env.toVar(getServiceName()) + "_USERNAME", username);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        freezeCheck();
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        freezeCheck();
        this.token = token;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        freezeCheck();
        this.tagName = tagName;
    }

    public String getPreviousTagName() {
        return previousTagName;
    }

    public void setPreviousTagName(String previousTagName) {
        freezeCheck();
        this.previousTagName = previousTagName;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        freezeCheck();
        this.releaseName = releaseName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        freezeCheck();
        this.branch = branch;
    }

    @Override
    public CommitAuthor getCommitAuthor() {
        return commitAuthor;
    }

    @Override
    public void setCommitAuthor(CommitAuthor commitAuthor) {
        this.commitAuthor.merge(commitAuthor);
    }

    public Prerelease getPrerelease() {
        return prerelease;
    }

    public void setPrerelease(Prerelease prerelease) {
        this.prerelease.merge(prerelease);
    }

    public boolean isSign() {
        return sign != null && sign;
    }

    public void setSign(Boolean sign) {
        freezeCheck();
        this.sign = sign;
    }

    public Changelog getChangelog() {
        return changelog;
    }

    public void setChangelog(Changelog changelog) {
        this.changelog.merge(changelog);
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public void setMilestone(Milestone milestone) {
        this.milestone.merge(milestone);
    }

    public boolean isSkipTag() {
        return skipTag != null && skipTag;
    }

    public void setSkipTag(Boolean skipTag) {
        freezeCheck();
        this.skipTag = skipTag;
    }

    public boolean isSkipTagSet() {
        return skipTag != null;
    }

    public boolean isSkipRelease() {
        return skipRelease != null && skipRelease;
    }

    public void setSkipRelease(Boolean skipRelease) {
        freezeCheck();
        this.skipRelease = skipRelease;
    }

    public boolean isSkipReleaseSet() {
        return skipRelease != null;
    }

    public boolean isOverwrite() {
        return overwrite != null && overwrite;
    }

    public void setOverwrite(Boolean overwrite) {
        freezeCheck();
        this.overwrite = overwrite;
    }

    public boolean isOverwriteSet() {
        return overwrite != null;
    }

    public Update getUpdate() {
        return update;
    }

    public void setUpdate(Update update) {
        this.update.merge(update);
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        freezeCheck();
        this.apiEndpoint = apiEndpoint;
    }

    @Override
    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public void setConnectTimeout(Integer connectTimeout) {
        freezeCheck();
        this.connectTimeout = connectTimeout;
    }

    @Override
    public Integer getReadTimeout() {
        return readTimeout;
    }

    @Override
    public void setReadTimeout(Integer readTimeout) {
        freezeCheck();
        this.readTimeout = readTimeout;
    }

    public boolean isArtifactsSet() {
        return artifacts != null;
    }

    public Boolean isArtifacts() {
        return artifacts == null || artifacts;
    }

    public void setArtifacts(Boolean artifacts) {
        freezeCheck();
        this.artifacts = artifacts;
    }

    public Boolean isFiles() {
        return files == null || files;
    }

    public boolean isFilesSet() {
        return files != null;
    }

    public void setFiles(Boolean files) {
        freezeCheck();
        this.files = files;
    }

    public boolean isChecksumsSet() {
        return checksums != null;
    }

    public Boolean isChecksums() {
        return checksums == null || checksums;
    }

    public void setChecksums(Boolean checksums) {
        freezeCheck();
        this.checksums = checksums;
    }

    public boolean isSignaturesSet() {
        return signatures != null;
    }

    public Boolean isSignatures() {
        return signatures == null || signatures;
    }

    public void setSignatures(Boolean signatures) {
        freezeCheck();
        this.signatures = signatures;
    }

    public Active getUploadAssets() {
        return uploadAssets;
    }

    public void setUploadAssets(Active uploadAssets) {
        freezeCheck();
        this.uploadAssets = uploadAssets;
    }

    public void setUploadAssets(String str) {
        setUploadAssets(Active.of(str));
    }

    public boolean isUploadAssetsSet() {
        return uploadAssets != null;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("host", host);
        props.put("owner", owner);
        props.put("name", name);
        props.put("username", username);
        props.put("token", isNotBlank(getResolvedToken()) ? Constants.HIDE : Constants.UNSET);
        if (releaseSupported) {
            props.put("uploadAssets", uploadAssets);
            props.put("artifacts", isArtifacts());
            props.put("files", isFiles());
            props.put("checksums", isChecksums());
            props.put("signatures", isSignatures());
            props.put("repoUrl", repoUrl);
            props.put("repoCloneUrl", repoCloneUrl);
            props.put("commitUrl", commitUrl);
            props.put("srcUrl", srcUrl);
            props.put("downloadUrl", downloadUrl);
            props.put("releaseNotesUrl", releaseNotesUrl);
            props.put("latestReleaseUrl", latestReleaseUrl);
            props.put("issueTrackerUrl", issueTrackerUrl);
        }
        props.put("tagName", tagName);
        if (releaseSupported) {
            props.put("releaseName", releaseName);
        }
        props.put("branch", branch);
        props.put("commitAuthor", commitAuthor.asMap(full));
        props.put("sign", isSign());
        props.put("skipTag", isSkipTag());
        props.put("skipRelease", isSkipRelease());
        props.put("overwrite", isOverwrite());
        if (releaseSupported) {
            props.put("update", update.asMap(full));
            props.put("apiEndpoint", apiEndpoint);
            props.put("connectTimeout", connectTimeout);
            props.put("readTimeout", readTimeout);
        }
        props.put("changelog", changelog.asMap(full));
        if (releaseSupported) {
            props.put("milestone", milestone.asMap(full));
        }
        props.put("prerelease", prerelease.asMap(full));
        return props;
    }

    public Map<String, Object> props(JReleaserModel model) {
        // duplicate from JReleaserModel to avoid endless recursion
        Map<String, Object> props = new LinkedHashMap<>();
        Project project = model.getProject();
        props.putAll(model.getEnvironment().getProperties());
        props.putAll(model.getEnvironment().getSourcedProperties());
        props.put(Constants.KEY_PROJECT_NAME, project.getName());
        props.put(Constants.KEY_PROJECT_NAME_CAPITALIZED, getClassNameForLowerCaseHyphenSeparatedName(project.getName()));
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

        props.put(Constants.KEY_REPO_HOST, host);
        props.put(Constants.KEY_REPO_OWNER, owner);
        props.put(Constants.KEY_REPO_NAME, name);
        props.put(Constants.KEY_REPO_BRANCH, branch);
        props.put(Constants.KEY_REVERSE_REPO_HOST, getReverseRepoHost());
        props.put(Constants.KEY_CANONICAL_REPO_NAME, getCanonicalRepoName());
        props.put(Constants.KEY_TAG_NAME, project.isSnapshot() ? project.getSnapshot().getResolvedLabel(model) : cachedTagName);
        props.put(Constants.KEY_RELEASE_NAME, cachedReleaseName);
        props.put(Constants.KEY_MILESTONE_NAME, milestone.getEffectiveName());

        applyTemplates(props, project.getResolvedExtraProperties());
        props.put(Constants.KEY_ZONED_DATE_TIME_NOW, model.getNow());
        MustacheUtils.applyFunctions(props);

        return props;
    }

    public void fillProps(Map<String, Object> props, JReleaserModel model) {
        props.put(Constants.KEY_REPO_HOST, host);
        props.put(Constants.KEY_REPO_OWNER, owner);
        props.put(Constants.KEY_REPO_NAME, name);
        props.put(Constants.KEY_REPO_BRANCH, branch);
        props.put(Constants.KEY_REVERSE_REPO_HOST, getReverseRepoHost());
        props.put(Constants.KEY_CANONICAL_REPO_NAME, getCanonicalRepoName());
        props.put(Constants.KEY_TAG_NAME, getEffectiveTagName(model));
        props.put(Constants.KEY_RELEASE_NAME, getEffectiveReleaseName());
        props.put(Constants.KEY_MILESTONE_NAME, milestone.getEffectiveName());
        props.put(Constants.KEY_REPO_URL, getResolvedRepoUrl(model));
        props.put(Constants.KEY_REPO_CLONE_URL, getResolvedRepoCloneUrl(model));
        props.put(Constants.KEY_COMMIT_URL, getResolvedCommitUrl(model));
        props.put(Constants.KEY_SRC_URL, getResolvedSrcUrl(model));
        props.put(Constants.KEY_RELEASE_NOTES_URL, getResolvedReleaseNotesUrl(model));
        props.put(Constants.KEY_LATEST_RELEASE_URL, getResolvedLatestReleaseUrl(model));
        props.put(Constants.KEY_ISSUE_TRACKER_URL, getResolvedIssueTrackerUrl(model));
    }

    public static class Update extends AbstractModelObject<Update> implements Domain {
        private final Set<UpdateSection> sections = new LinkedHashSet<>();
        private Boolean enabled;

        @Override
        public void merge(Update update) {
            freezeCheck();
            this.enabled = merge(this.enabled, update.enabled);
            setSections(merge(this.sections, update.sections));
        }

        public boolean isEnabled() {
            return enabled != null && enabled;
        }

        public void setEnabled(Boolean enabled) {
            freezeCheck();
            this.enabled = enabled;
        }

        public boolean isEnabledSet() {
            return enabled != null;
        }

        public Set<UpdateSection> getSections() {
            return freezeWrap(sections);
        }

        public void setSections(Set<UpdateSection> sections) {
            freezeCheck();
            this.sections.clear();
            this.sections.addAll(sections);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("enabled", isEnabled());
            map.put("sections", sections);
            return map;
        }
    }

    public static class Prerelease extends AbstractModelObject<Prerelease> implements Domain {
        private Boolean enabled;
        private String pattern;

        @Override
        public void merge(Prerelease prerelease) {
            freezeCheck();
            this.enabled = merge(this.enabled, prerelease.enabled);
            this.pattern = merge(this.pattern, prerelease.pattern);
        }

        public void disable() {
            enabled = false;
        }

        public boolean isEnabled() {
            return enabled != null && enabled;
        }

        public void setEnabled(Boolean enabled) {
            freezeCheck();
            this.enabled = enabled;
        }

        public boolean isPrerelease(String version) {
            if (null == enabled) {
                String configuredPattern = getConfiguredPattern();
                if (isNotBlank(configuredPattern)) {
                    enabled = version.matches(configuredPattern);
                } else {
                    enabled = false;
                }
            }

            return enabled;
        }

        public String getConfiguredPattern() {
            return Env.env(PRERELEASE_PATTERN, pattern);
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            freezeCheck();
            this.pattern = pattern;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("enabled", isEnabled());
            map.put("pattern", getConfiguredPattern());
            return map;
        }
    }

    public static class Milestone extends AbstractModelObject<Milestone> implements Domain {
        public static final String MILESTONE_NAME = "MILESTONE_NAME";

        private Boolean close;
        private String name;

        @JsonIgnore
        private String cachedName;

        @Override
        public void merge(Milestone milestone) {
            freezeCheck();
            this.close = merge(this.close, milestone.close);
            this.name = merge(this.name, milestone.name);
        }

        public String getConfiguredName() {
            return Env.env(MILESTONE_NAME, cachedName);
        }

        public String getResolvedName(Map<String, Object> props) {
            if (isBlank(cachedName)) {
                cachedName = getConfiguredName();
            }

            if (isBlank(cachedName)) {
                cachedName = resolveTemplate(name, props);
            } else if (cachedName.contains("{{")) {
                cachedName = resolveTemplate(cachedName, props);
            }

            return cachedName;
        }

        public String getEffectiveName() {
            return cachedName;
        }

        public Boolean isClose() {
            return close == null || close;
        }

        public void setClose(Boolean close) {
            freezeCheck();
            this.close = close;
        }

        public boolean isCloseSet() {
            return close != null;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            freezeCheck();
            this.name = name;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", name);
            map.put("close", isClose());
            return map;
        }
    }
}
