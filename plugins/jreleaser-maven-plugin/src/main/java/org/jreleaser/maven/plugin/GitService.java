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

import org.jreleaser.model.UpdateSection;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class GitService implements Releaser {
    private final CommitAuthor commitAuthor = new CommitAuthor();
    private final Changelog changelog = new Changelog();
    private final Milestone milestone = new Milestone();
    private final Set<UpdateSection> updateSections = new LinkedHashSet<>();
    protected Boolean enabled;
    private String host;
    private String owner;
    private String name;
    private String repoUrl;
    private String repoCloneUrl;
    private String commitUrl;
    private String downloadUrl;
    private String releaseNotesUrl;
    private String latestReleaseUrl;
    private String issueTrackerUrl;
    private String username;
    private String token;
    private String tagName;
    private String releaseName;
    private String branch;
    private boolean sign;
    private Boolean skipTag;
    private Boolean overwrite;
    private Boolean update;
    private String apiEndpoint;
    private int connectTimeout;
    private int readTimeout;
    private Boolean artifacts;
    private Boolean files;
    private Boolean checksums;
    private Boolean signatures;

    void setAll(GitService service) {
        this.enabled = service.enabled;
        this.host = service.host;
        this.owner = service.owner;
        this.name = service.name;
        this.repoUrl = service.repoUrl;
        this.repoCloneUrl = service.repoCloneUrl;
        this.commitUrl = service.commitUrl;
        this.downloadUrl = service.downloadUrl;
        this.releaseNotesUrl = service.releaseNotesUrl;
        this.latestReleaseUrl = service.latestReleaseUrl;
        this.issueTrackerUrl = service.issueTrackerUrl;
        this.username = service.username;
        this.token = service.token;
        this.tagName = service.tagName;
        this.releaseName = service.releaseName;
        this.branch = service.branch;
        this.sign = service.sign;
        this.skipTag = service.skipTag;
        this.overwrite = service.overwrite;
        this.update = service.update;
        this.apiEndpoint = service.apiEndpoint;
        this.connectTimeout = service.connectTimeout;
        this.readTimeout = service.readTimeout;
        this.artifacts = service.artifacts;
        this.files = service.files;
        this.checksums = service.checksums;
        this.signatures = service.signatures;
        setCommitAuthor(service.commitAuthor);
        setChangelog(service.changelog);
        setMilestone(service.milestone);
        setUpdateSections(service.updateSections);
    }

    @Override
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
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
        this.host = host;
    }

    public String getOwner() {
        return owner;
    }

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

    @Deprecated
    public String getRepoUrlFormat() {
        System.out.println("getRepoUrlFormat() has been deprecated since 0.5.0 wan will be removed in the future. Use getRepoUrl() instead");
        return repoUrl;
    }

    @Deprecated
    public void setRepoUrlFormat(String repoUrl) {
        System.out.println("setRepoUrlFormat() has been deprecated since 0.5.0 wan will be removed in the future. Use setRepoUrl() instead");
        this.repoUrl = repoUrl;
    }

    @Deprecated
    public String getRepoCloneUrlFormat() {
        System.out.println("getRepoCloneUrlFormat() has been deprecated since 0.5.0 wan will be removed in the future. Use getRepoCloneUrl() instead");
        return repoCloneUrl;
    }

    @Deprecated
    public void setRepoCloneUrlFormat(String repoCloneUrl) {
        System.out.println("setRepoCloneUrlFormat() has been deprecated since 0.5.0 wan will be removed in the future. Use setRepoCloneUrl() instead");
        this.repoCloneUrl = repoCloneUrl;
    }

    @Deprecated
    public String getCommitUrlFormat() {
        System.out.println("getCommitUrlFormat() has been deprecated since 0.5.0 wan will be removed in the future. Use getCommitUrl() instead");
        return commitUrl;
    }

    @Deprecated
    public void setCommitUrlFormat(String commitUrl) {
        System.out.println("setCommitUrlFormat() has been deprecated since 0.5.0 wan will be removed in the future. Use setCommitUrl() instead");
        this.commitUrl = commitUrl;
    }

    @Deprecated
    public String getDownloadUrlFormat() {
        System.out.println("getDownloadUrlFormat() has been deprecated since 0.5.0 wan will be removed in the future. Use getDownloadUrl() instead");
        return downloadUrl;
    }

    @Deprecated
    public void setDownloadUrlFormat(String downloadUrl) {
        System.out.println("setDownloadUrlFormat() has been deprecated since 0.5.0 wan will be removed in the future. Use setDownloadUrl() instead");
        this.downloadUrl = downloadUrl;
    }

    @Deprecated
    public String getReleaseNotesUrlFormat() {
        System.out.println("getReleaseNotesUrlFormat() has been deprecated since 0.5.0 wan will be removed in the future. Use getReleaseNotesUrl() instead");
        return releaseNotesUrl;
    }

    @Deprecated
    public void setReleaseNotesUrlFormat(String releaseNotesUrl) {
        System.out.println("setReleaseNotesUrlFormat() has been deprecated since 0.5.0 wan will be removed in the future. Use setReleaseNotesUrl() instead");
        this.releaseNotesUrl = releaseNotesUrl;
    }

    @Deprecated
    public String getLatestReleaseUrlFormat() {
        System.out.println("getLatestReleaseUrlFormat() has been deprecated since 0.5.0 wan will be removed in the future. Use getLatestReleaseUrl() instead");
        return latestReleaseUrl;
    }

    @Deprecated
    public void setLatestReleaseUrlFormat(String latestReleaseUrl) {
        System.out.println("setLatestReleaseUrlFormat() has been deprecated since 0.5.0 wan will be removed in the future. Use setLatestReleaseUrl() instead");
        this.latestReleaseUrl = latestReleaseUrl;
    }

    @Deprecated
    public String getIssueTrackerUrlFormat() {
        System.out.println("getIssueTrackerUrlFormat() has been deprecated since 0.5.0 wan will be removed in the future. Use getIssueTrackerUrl() instead");
        return issueTrackerUrl;
    }

    @Deprecated
    public void setIssueTrackerUrlFormat(String issueTrackerUrl) {
        System.out.println("setIssueTrackerUrlFormat() has been deprecated since 0.5.0 wan will be removed in the future. Use setIssueTrackerUrl() instead");
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
        this.tagName = tagName;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public CommitAuthor getCommitAuthor() {
        return commitAuthor;
    }

    public void setCommitAuthor(CommitAuthor commitAuthor) {
        this.commitAuthor.setAll(commitAuthor);
    }

    public boolean isSign() {
        return sign;
    }

    public void setSign(boolean sign) {
        this.sign = sign;
    }

    public Changelog getChangelog() {
        return changelog;
    }

    public void setChangelog(Changelog changelog) {
        this.changelog.setAll(changelog);
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public void setMilestone(Milestone milestone) {
        this.milestone.setAll(milestone);
    }

    public boolean isSkipTag() {
        return skipTag != null && skipTag;
    }

    public void setSkipTag(Boolean skipTag) {
        this.skipTag = skipTag;
    }

    public boolean isSkipTagSet() {
        return skipTag != null;
    }

    public boolean isOverwrite() {
        return overwrite != null && overwrite;
    }

    public void setOverwrite(Boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean isOverwriteSet() {
        return overwrite != null;
    }

    public boolean isUpdate() {
        return update != null && update;
    }

    public void setUpdate(Boolean update) {
        this.update = update;
    }

    public Set<UpdateSection> getUpdateSections() {
        return updateSections;
    }

    public void setUpdateSections(Set<UpdateSection> updateSections) {
        this.updateSections.clear();
        this.updateSections.addAll(updateSections);
    }

    public boolean isUpdateSet() {
        return update != null;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    @Override
    public int getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public int getReadTimeout() {
        return readTimeout;
    }

    @Override
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public boolean isArtifactsSet() {
        return artifacts != null;
    }

    public Boolean isArtifacts() {
        return artifacts == null || artifacts;
    }

    public void setArtifacts(Boolean artifacts) {
        this.artifacts = artifacts;
    }

    public Boolean isFiles() {
        return files == null || files;
    }

    public boolean isFilesSet() {
        return files != null;
    }

    public void setFiles(Boolean files) {
        this.files = files;
    }

    public boolean isChecksumsSet() {
        return checksums != null;
    }

    public Boolean isChecksums() {
        return checksums == null || checksums;
    }

    public void setChecksums(Boolean checksums) {
        this.checksums = checksums;
    }

    public boolean isSignaturesSet() {
        return signatures != null;
    }

    public Boolean isSignatures() {
        return signatures == null || signatures;
    }

    public void setSignatures(Boolean signatures) {
        this.signatures = signatures;
    }

    public static class Prerelease {
        private Boolean enabled;
        private String pattern;

        void setAll(Prerelease prerelease) {
            this.enabled = prerelease.enabled;
            this.pattern = prerelease.pattern;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
    }
}
