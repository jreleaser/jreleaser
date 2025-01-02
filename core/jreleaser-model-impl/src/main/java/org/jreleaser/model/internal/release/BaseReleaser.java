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
package org.jreleaser.model.internal.release;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.Constants;
import org.jreleaser.model.UpdateSection;
import org.jreleaser.model.api.common.Apply;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.CommitAuthor;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.EnabledAware;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.mustache.MustacheUtils;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.util.Env;
import org.jreleaser.util.PlatformUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.getCapitalizedName;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class BaseReleaser<A extends org.jreleaser.model.api.release.Releaser, S extends BaseReleaser<A, S>> extends AbstractModelObject<S> implements Releaser<A> {
    private static final long serialVersionUID = 1051555604419029072L;

    @JsonIgnore
    private final String serviceName;
    private final Changelog changelog = new Changelog();
    private final Milestone milestone = new Milestone();
    private final Issues issues = new Issues();
    private final CommitAuthor commitAuthor = new CommitAuthor();
    private final Update update = new Update();
    private final Prerelease prerelease = new Prerelease();
    @JsonIgnore
    private final boolean releaseSupported;
    @JsonIgnore
    private boolean match = true;
    private Boolean enabled;
    private String host;
    private String owner;
    private String name;
    private String repoUrl;
    private String repoCloneUrl;
    private String commitUrl;
    private String srcUrl;
    private String downloadUrl;
    private String releaseNotesUrl;
    private String latestReleaseUrl;
    private String issueTrackerUrl;
    private String username;
    private String token;
    private String tagName;
    private String previousTagName;
    private String releaseName;
    private String branch;
    private String branchPush;
    protected Boolean sign;
    protected Boolean skipTag;
    protected Boolean skipRelease;
    protected Boolean overwrite;
    private String apiEndpoint;
    private int connectTimeout;
    private int readTimeout;
    protected Boolean artifacts;
    protected Boolean files;
    protected Boolean checksums;
    protected Boolean signatures;
    protected Boolean catalogs;
    private Active uploadAssets;
    protected Boolean uploadAssetsEnabled;
    @JsonIgnore
    private String cachedBranchPush;
    @JsonIgnore
    private String cachedTagName;
    @JsonIgnore
    private String cachedPreviousTagName;
    @JsonIgnore
    private String cachedReleaseName;

    protected BaseReleaser(String serviceName, boolean releaseSupported) {
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
    public void merge(S source) {
        this.match = source.isMatch();
        this.enabled = merge(this.enabled, source.isEnabled());
        this.host = merge(this.host, source.getHost());
        this.owner = merge(this.owner, source.getOwner());
        this.name = merge(this.name, source.getName());
        this.repoUrl = merge(this.repoUrl, source.getRepoUrl());
        this.repoCloneUrl = merge(this.repoCloneUrl, source.getRepoCloneUrl());
        this.commitUrl = merge(this.commitUrl, source.getCommitUrl());
        this.srcUrl = merge(this.srcUrl, source.getSrcUrl());
        this.downloadUrl = merge(this.downloadUrl, source.getDownloadUrl());
        this.releaseNotesUrl = merge(this.releaseNotesUrl, source.getReleaseNotesUrl());
        this.latestReleaseUrl = merge(this.latestReleaseUrl, source.getLatestReleaseUrl());
        this.issueTrackerUrl = merge(this.issueTrackerUrl, source.getIssueTrackerUrl());
        this.username = merge(this.username, source.getUsername());
        this.token = merge(this.token, source.getToken());
        this.tagName = merge(this.tagName, source.getTagName());
        this.previousTagName = merge(this.previousTagName, source.getPreviousTagName());
        this.releaseName = merge(this.releaseName, source.getReleaseName());
        this.branch = merge(this.branch, source.getBranch());
        this.branchPush = merge(this.branchPush, source.getBranchPush());
        this.sign = merge(this.sign, source.sign);
        this.skipTag = merge(this.skipTag, source.skipTag);
        this.skipRelease = merge(this.skipRelease, source.skipRelease);
        this.overwrite = merge(this.overwrite, source.overwrite);
        this.apiEndpoint = merge(this.apiEndpoint, source.getApiEndpoint());
        this.connectTimeout = merge(this.getConnectTimeout(), source.getConnectTimeout());
        this.readTimeout = merge(this.getReadTimeout(), source.getReadTimeout());
        this.artifacts = merge(this.artifacts, source.artifacts);
        this.files = merge(this.files, source.files);
        this.checksums = merge(this.checksums, source.checksums);
        this.signatures = merge(this.signatures, source.signatures);
        this.catalogs = merge(this.catalogs, source.catalogs);
        this.uploadAssets = merge(this.uploadAssets, source.getUploadAssets());
        this.uploadAssetsEnabled = merge(this.uploadAssetsEnabled, source.uploadAssetsEnabled);
        setCommitAuthor(source.getCommitAuthor());
        setUpdate(source.getUpdate());
        setPrerelease(source.getPrerelease());
        setChangelog(source.getChangelog());
        setMilestone(source.getMilestone());
        setIssues(source.getIssues());
    }

    protected Boolean getUploadAssetsEnabled() {
        return uploadAssetsEnabled;
    }

    public abstract String getReverseRepoHost();

    @Override
    public boolean isPrerelease() {
        return getPrerelease().isEnabled();
    }

    public String getCanonicalRepoName() {
        if (isNotBlank(owner)) {
            return owner + "/" + name;
        }
        return name;
    }

    public String getResolvedBranchPush(JReleaserModel model) {
        if (isBlank(cachedBranchPush)) {
            cachedBranchPush = resolveTemplate(branchPush, props(model));
        }
        if (isNotBlank(cachedBranchPush) && cachedBranchPush.contains("{{")) {
            cachedBranchPush = resolveTemplate(cachedBranchPush, props(model));
        }

        return cachedBranchPush;
    }

    public String getResolvedPreviousTagName(JReleaserModel model) {
        if (isBlank(cachedPreviousTagName) && isNotBlank(previousTagName)) {
            cachedPreviousTagName = resolveTemplate(previousTagName, props(model));
        }
        if (isNotBlank(cachedPreviousTagName) && cachedPreviousTagName.contains("{{")) {
            cachedPreviousTagName = resolveTemplate(cachedPreviousTagName, props(model));
        }

        return cachedPreviousTagName;
    }

    public String getResolvedTagName(JReleaserModel model) {
        if (isBlank(cachedTagName)) {
            cachedTagName = resolveTemplate(tagName, props(model));
        }
        if (isNotBlank(cachedTagName) && cachedTagName.contains("{{")) {
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

    public String getResolvedReleaseName(JReleaserModel model) {
        if (isBlank(cachedReleaseName)) {
            cachedReleaseName = resolveTemplate(releaseName, props(model));
        }
        if (isNotBlank(cachedReleaseName) && cachedReleaseName.contains("{{")) {
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
        TemplateContext props = props(model);
        props.set(Constants.KEY_REPO_OWNER, repoOwner);
        props.set(Constants.KEY_REPO_NAME, repoName);
        return resolveTemplate(repoUrl, props);
    }

    public String getResolvedRepoCloneUrl(JReleaserModel model, String repoOwner, String repoName) {
        if (!releaseSupported) return "";
        TemplateContext props = props(model);
        props.set(Constants.KEY_REPO_OWNER, repoOwner);
        props.set(Constants.KEY_REPO_NAME, repoName);
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

    public String getResolvedIssueTrackerUrl(JReleaserModel model, boolean appendSlash) {
        if (!releaseSupported) return "";
        String issueTracker = resolveTemplate(issueTrackerUrl, props(model));
        if (appendSlash && isNotBlank(issueTracker) && !issueTracker.endsWith("/")) {
            issueTracker += "/";
        }
        return issueTracker;
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
        this.match = match;
    }

    @Override
    public boolean isEnabled() {
        return null != enabled && enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabledSet() {
        return null != enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getRepoCloneUrl() {
        return repoCloneUrl;
    }

    public void setRepoCloneUrl(String repoCloneUrl) {
        this.repoCloneUrl = repoCloneUrl;
    }

    public String getCommitUrl() {
        return commitUrl;
    }

    public void setCommitUrl(String commitUrl) {
        this.commitUrl = commitUrl;
    }

    public String getSrcUrl() {
        return srcUrl;
    }

    public void setSrcUrl(String srcUrl) {
        this.srcUrl = srcUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getReleaseNotesUrl() {
        return releaseNotesUrl;
    }

    public void setReleaseNotesUrl(String releaseNotesUrl) {
        this.releaseNotesUrl = releaseNotesUrl;
    }

    public String getLatestReleaseUrl() {
        return latestReleaseUrl;
    }

    public void setLatestReleaseUrl(String latestReleaseUrl) {
        this.latestReleaseUrl = latestReleaseUrl;
    }

    public String getIssueTrackerUrl() {
        return issueTrackerUrl;
    }

    public void setIssueTrackerUrl(String issueTrackerUrl) {
        this.issueTrackerUrl = issueTrackerUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.cachedTagName = null;
        this.tagName = tagName;
    }

    public String getPreviousTagName() {
        return previousTagName;
    }

    public void setPreviousTagName(String previousTagName) {
        this.cachedPreviousTagName = null;
        this.previousTagName = previousTagName;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.cachedReleaseName = null;
        this.releaseName = releaseName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getBranchPush() {
        return branchPush;
    }

    public void setBranchPush(String branchPush) {
        this.branchPush = branchPush;
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
        return null != sign && sign;
    }

    public void setSign(Boolean sign) {
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

    public Issues getIssues() {
        return issues;
    }

    public void setIssues(Issues issues) {
        this.issues.merge(issues);
    }

    public boolean isSkipTag() {
        return null != skipTag && skipTag;
    }

    public void setSkipTag(Boolean skipTag) {
        this.skipTag = skipTag;
    }

    public boolean isSkipTagSet() {
        return null != skipTag;
    }

    public boolean isSkipRelease() {
        return null != skipRelease && skipRelease;
    }

    public void setSkipRelease(Boolean skipRelease) {
        this.skipRelease = skipRelease;
    }

    public boolean isSkipReleaseSet() {
        return null != skipRelease;
    }

    public boolean isOverwrite() {
        return null != overwrite && overwrite;
    }

    public void setOverwrite(Boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean isOverwriteSet() {
        return null != overwrite;
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
        this.apiEndpoint = apiEndpoint;
    }

    @Override
    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public Integer getReadTimeout() {
        return readTimeout;
    }

    @Override
    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public boolean isArtifactsSet() {
        return null != artifacts;
    }

    public boolean isArtifacts() {
        return null == artifacts || artifacts;
    }

    public void setArtifacts(Boolean artifacts) {
        this.artifacts = artifacts;
    }

    public boolean isFiles() {
        return null == files || files;
    }

    public void setFiles(Boolean files) {
        this.files = files;
    }

    public boolean isFilesSet() {
        return null != files;
    }

    public boolean isChecksumsSet() {
        return null != checksums;
    }

    public boolean isChecksums() {
        return null == checksums || checksums;
    }

    public void setChecksums(Boolean checksums) {
        this.checksums = checksums;
    }

    public boolean isCatalogsSet() {
        return null != catalogs;
    }

    public boolean isCatalogs() {
        return null == catalogs || catalogs;
    }

    public void setCatalogs(Boolean catalogs) {
        this.catalogs = catalogs;
    }

    public boolean isSignaturesSet() {
        return null != signatures;
    }

    public boolean isSignatures() {
        return null == signatures || signatures;
    }

    public void setSignatures(Boolean signatures) {
        this.signatures = signatures;
    }

    public Active getUploadAssets() {
        return uploadAssets;
    }

    public void setUploadAssets(Active uploadAssets) {
        this.uploadAssets = uploadAssets;
    }

    public void setUploadAssets(String str) {
        setUploadAssets(Active.of(str));
    }

    public boolean isUploadAssetsSet() {
        return null != uploadAssets;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("host", host);
        map.put("owner", owner);
        map.put("name", name);
        map.put("username", username);
        map.put("token", isNotBlank(token) ? Constants.HIDE : Constants.UNSET);
        if (releaseSupported) {
            map.put("uploadAssets", uploadAssets);
            map.put("artifacts", isArtifacts());
            map.put("files", isFiles());
            map.put("checksums", isChecksums());
            map.put("catalogs", isCatalogs());
            map.put("signatures", isSignatures());
            map.put("repoUrl", repoUrl);
            map.put("repoCloneUrl", repoCloneUrl);
            map.put("commitUrl", commitUrl);
            map.put("srcUrl", srcUrl);
            map.put("downloadUrl", downloadUrl);
            map.put("releaseNotesUrl", releaseNotesUrl);
            map.put("latestReleaseUrl", latestReleaseUrl);
            map.put("issueTrackerUrl", issueTrackerUrl);
        }
        map.put("tagName", tagName);
        map.put("previousTagName", previousTagName);
        if (releaseSupported) {
            map.put("releaseName", releaseName);
        }
        map.put("branch", branch);
        map.put("branchPush", branchPush);
        map.put("commitAuthor", commitAuthor.asMap(full));
        map.put("sign", isSign());
        map.put("skipTag", isSkipTag());
        map.put("skipRelease", isSkipRelease());
        map.put("overwrite", isOverwrite());
        if (releaseSupported) {
            map.put("update", update.asMap(full));
            map.put("apiEndpoint", apiEndpoint);
            map.put("connectTimeout", connectTimeout);
            map.put("readTimeout", readTimeout);
        }
        map.put("changelog", changelog.asMap(full));
        if (releaseSupported) {
            map.put("milestone", milestone.asMap(full));
            map.put("issues", issues.asMap(full));
        }
        map.put("prerelease", prerelease.asMap(full));
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
        props.set(Constants.KEY_PROJECT_VERSION, project.getVersion());
        props.set(Constants.KEY_PROJECT_STEREOTYPE, project.getStereotype());
        props.set(Constants.KEY_PROJECT_EFFECTIVE_VERSION, project.getEffectiveVersion());
        props.set(Constants.KEY_PROJECT_SNAPSHOT, String.valueOf(project.isSnapshot()));
        if (isNotBlank(project.getDescription())) {
            props.set(Constants.KEY_PROJECT_DESCRIPTION, MustacheUtils.passThrough(project.getDescription()));
        }
        if (isNotBlank(project.getLongDescription())) {
            props.set(Constants.KEY_PROJECT_LONG_DESCRIPTION, MustacheUtils.passThrough(project.getLongDescription()));
        }
        if (isNotBlank(project.getLicense())) {
            props.set(Constants.KEY_PROJECT_LICENSE, project.getLicense());
        }
        if (null != project.getInceptionYear()) {
            props.set(Constants.KEY_PROJECT_INCEPTION_YEAR, project.getInceptionYear());
        }
        if (isNotBlank(project.getCopyright())) {
            props.set(Constants.KEY_PROJECT_COPYRIGHT, project.getCopyright());
        }
        if (isNotBlank(project.getVendor())) {
            props.set(Constants.KEY_PROJECT_VENDOR, project.getVendor());
        }
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

        props.set(Constants.KEY_REPO_HOST, host);
        props.set(Constants.KEY_REPO_OWNER, owner);
        props.set(Constants.KEY_REPO_NAME, name);
        props.set(Constants.KEY_REPO_BRANCH, branch);
        props.set(Constants.KEY_REPO_BRANCH_PUSH, cachedBranchPush);
        props.set(Constants.KEY_REVERSE_REPO_HOST, getReverseRepoHost());
        props.set(Constants.KEY_CANONICAL_REPO_NAME, getCanonicalRepoName());
        props.set(Constants.KEY_TAG_NAME, project.isSnapshot() ? project.getSnapshot().getResolvedLabel(model) : cachedTagName);
        props.set(Constants.KEY_PREVIOUS_TAG_NAME, cachedPreviousTagName);
        props.set(Constants.KEY_RELEASE_NAME, cachedReleaseName);
        props.set(Constants.KEY_MILESTONE_NAME, milestone.getEffectiveName());

        applyTemplates(props, project.resolvedExtraProperties());
        props.set(Constants.KEY_ZONED_DATE_TIME_NOW, model.getNow());

        return props;
    }

    public void fillProps(TemplateContext props, JReleaserModel model) {
        props.set(Constants.KEY_REPO_HOST, host);
        props.set(Constants.KEY_REPO_OWNER, owner);
        props.set(Constants.KEY_REPO_NAME, name);
        props.set(Constants.KEY_REPO_BRANCH, branch);
        props.set(Constants.KEY_REPO_BRANCH_PUSH, getResolvedBranchPush(model));
        props.set(Constants.KEY_REVERSE_REPO_HOST, getReverseRepoHost());
        props.set(Constants.KEY_CANONICAL_REPO_NAME, getCanonicalRepoName());
        props.set(Constants.KEY_TAG_NAME, getEffectiveTagName(model));
        props.set(Constants.KEY_PREVIOUS_TAG_NAME, getResolvedPreviousTagName(model));
        props.set(Constants.KEY_RELEASE_NAME, getEffectiveReleaseName());
        props.set(Constants.KEY_MILESTONE_NAME, milestone.getEffectiveName());
        props.set(Constants.KEY_REPO_URL, getResolvedRepoUrl(model));
        props.set(Constants.KEY_REPO_CLONE_URL, getResolvedRepoCloneUrl(model));
        props.set(Constants.KEY_COMMIT_URL, getResolvedCommitUrl(model));
        props.set(Constants.KEY_SRC_URL, getResolvedSrcUrl(model));
        props.set(Constants.KEY_RELEASE_NOTES_URL, getResolvedReleaseNotesUrl(model));
        props.set(Constants.KEY_LATEST_RELEASE_URL, getResolvedLatestReleaseUrl(model));
        props.set(Constants.KEY_ISSUE_TRACKER_URL, getResolvedIssueTrackerUrl(model, false));
    }

    public static final class Update extends AbstractModelObject<Update> implements Domain, EnabledAware {
        private static final long serialVersionUID = -3809529510256990035L;

        private final Set<UpdateSection> sections = new LinkedHashSet<>();
        private Boolean enabled;

        @JsonIgnore
        private final org.jreleaser.model.api.release.Releaser.Update immutable = new org.jreleaser.model.api.release.Releaser.Update() {
            private static final long serialVersionUID = -7253526159752557224L;

            @Override
            public Set<UpdateSection> getSections() {
                return unmodifiableSet(sections);
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Update.this.asMap(full));
            }

            @Override
            public boolean isEnabled() {
                return Update.this.isEnabled();
            }
        };

        public org.jreleaser.model.api.release.Releaser.Update asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Update source) {
            this.enabled = merge(this.enabled, source.enabled);
            setSections(merge(this.sections, source.sections));
        }

        @Override
        public boolean isEnabled() {
            return null != enabled && enabled;
        }

        @Override
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean isEnabledSet() {
            return null != enabled;
        }

        public Set<UpdateSection> getSections() {
            return sections;
        }

        public void setSections(Set<UpdateSection> sections) {
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

    public static final class Prerelease extends AbstractModelObject<Prerelease> implements Domain, EnabledAware {
        private static final long serialVersionUID = -7358833182369685133L;

        private Boolean enabled;
        private String pattern;

        @JsonIgnore
        private final org.jreleaser.model.api.release.Releaser.Prerelease immutable = new org.jreleaser.model.api.release.Releaser.Prerelease() {
            private static final long serialVersionUID = -862797136353472181L;

            @Override
            public boolean isPrerelease(String version) {
                return Prerelease.this.isPrerelease(version);
            }

            @Override
            public String getPattern() {
                return pattern;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Prerelease.this.asMap(full));
            }

            @Override
            public boolean isEnabled() {
                return Prerelease.this.isEnabled();
            }
        };

        public org.jreleaser.model.api.release.Releaser.Prerelease asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Prerelease source) {
            this.enabled = merge(this.enabled, source.enabled);
            this.pattern = merge(this.pattern, source.pattern);
        }

        public void disable() {
            enabled = false;
        }

        @Override
        public boolean isEnabled() {
            return null != enabled && enabled;
        }

        @Override
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean isEnabledSet() {
            return null != enabled;
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
            return Env.env(org.jreleaser.model.api.release.Releaser.PRERELEASE_PATTERN, pattern);
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
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

    public static final class Milestone extends AbstractModelObject<Milestone> implements Domain {
        private static final long serialVersionUID = 5385291379888571159L;

        private Boolean close;
        private String name;

        @JsonIgnore
        private final org.jreleaser.model.api.release.Releaser.Milestone immutable = new org.jreleaser.model.api.release.Releaser.Milestone() {
            private static final long serialVersionUID = 2957887352276816797L;

            @Override
            public boolean isClose() {
                return Milestone.this.isClose();
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Milestone.this.asMap(full));
            }
        };

        @JsonIgnore
        private String cachedName;

        public org.jreleaser.model.api.release.Releaser.Milestone asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Milestone source) {
            this.close = merge(this.close, source.close);
            this.name = merge(this.name, source.name);
        }

        public String getConfiguredName() {
            return Env.env(org.jreleaser.model.api.release.Releaser.MILESTONE_NAME, cachedName);
        }

        public String getResolvedName(TemplateContext props) {
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

        public boolean isClose() {
            return null == close || close;
        }

        public void setClose(Boolean close) {
            this.close = close;
        }

        public boolean isCloseSet() {
            return null != close;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
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

    public static final class Issues extends AbstractModelObject<Issues> implements Domain, EnabledAware {
        private static final long serialVersionUID = -8544658826532962076L;

        private final Label label = new Label();
        private Apply applyMilestone;
        private String comment;
        private Boolean enabled;

        @JsonIgnore
        private final org.jreleaser.model.api.release.Releaser.Issues immutable = new org.jreleaser.model.api.release.Releaser.Issues() {
            private static final long serialVersionUID = 7857893617001154950L;

            @Override
            public String getComment() {
                return comment;
            }

            @Override
            public Apply getApplyMilestone() {
                return applyMilestone;
            }

            @Override
            public Label getLabel() {
                return label.asImmutable();
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Issues.this.asMap(full));
            }

            @Override
            public boolean isEnabled() {
                return Issues.this.isEnabled();
            }
        };

        public org.jreleaser.model.api.release.Releaser.Issues asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Issues source) {
            this.comment = merge(this.comment, source.comment);
            this.enabled = merge(this.enabled, source.enabled);
            this.applyMilestone = merge(this.applyMilestone, source.applyMilestone);
            setLabel(source.label);
        }

        @Override
        public boolean isEnabled() {
            return null != enabled && enabled;
        }

        @Override
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean isEnabledSet() {
            return null != enabled;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public Label getLabel() {
            return label;
        }

        public void setLabel(Label label) {
            this.label.merge(label);
        }

        public Apply getApplyMilestone() {
            return applyMilestone;
        }

        public void setApplyMilestone(Apply applyMilestone) {
            this.applyMilestone = applyMilestone;
        }

        public void setApplyMilestone(String str) {
            setApplyMilestone(Apply.of(str));
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("enabled", isEnabled());
            map.put("comment", comment);
            map.put("label", label.asMap(full));
            map.put("applyMilestone", applyMilestone);
            return map;
        }

        public static final class Label extends AbstractModelObject<Label> implements Domain {
            private static final long serialVersionUID = 6951467063217168128L;

            private String name;
            private String color;
            private String description;

            @JsonIgnore
            private final org.jreleaser.model.api.release.Releaser.Issues.Label immutable = new org.jreleaser.model.api.release.Releaser.Issues.Label() {
                private static final long serialVersionUID = 8642359487826561699L;

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public String getColor() {
                    return color;
                }

                @Override
                public String getDescription() {
                    return description;
                }

                @Override
                public Map<String, Object> asMap(boolean full) {
                    return unmodifiableMap(Label.this.asMap(full));
                }
            };

            public org.jreleaser.model.api.release.Releaser.Issues.Label asImmutable() {
                return immutable;
            }

            @Override
            public void merge(Label source) {
                this.name = merge(this.name, source.name);
                this.color = merge(this.color, source.color);
                this.description = merge(this.description, source.description);
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getColor() {
                return color;
            }

            public void setColor(String color) {
                this.color = color;
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("name", name);
                map.put("color", color);
                map.put("description", description);
                return map;
            }
        }
    }
}
