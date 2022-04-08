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
    private final Update update = new Update();
    protected Boolean enabled;
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
    private Boolean sign;
    private Boolean skipTag;
    private Boolean skipRelease;
    private Boolean overwrite;
    private String apiEndpoint;
    private int connectTimeout;
    private int readTimeout;
    private Boolean artifacts;
    private Boolean files;
    private Boolean checksums;
    private Boolean signatures;
    private Active uploadAssets;

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
        this.tagName = tagName;
    }

    public String getPreviousTagName() {
        return previousTagName;
    }

    public void setPreviousTagName(String previousTagName) {
        this.previousTagName = previousTagName;
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
        return sign != null && sign;
    }

    public void setSign(Boolean sign) {
        this.sign = sign;
    }

    public boolean isSignSet() {
        return sign != null;
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

    public boolean isSkipRelease() {
        return skipRelease != null && skipRelease;
    }

    public void setSkipRelease(Boolean skipRelease) {
        this.skipRelease = skipRelease;
    }

    public boolean isSkipReleaseSet() {
        return skipRelease != null;
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

    public Update getUpdate() {
        return update;
    }

    public void setUpdate(Update update) {
        this.update.setAll(update);
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

    public Active getUploadAssets() {
        return uploadAssets;
    }

    public void setUploadAssets(Active uploadAssets) {
        this.uploadAssets = uploadAssets;
    }

    public String resolveUploadAssets() {
        return uploadAssets != null ? uploadAssets.name() : null;
    }

    public static class Update {
        private final Set<UpdateSection> sections = new LinkedHashSet<>();
        private Boolean enabled;

        void setAll(Update update) {
            this.enabled = update.enabled;
            setSections(update.sections);
        }

        public boolean isEnabled() {
            return enabled != null && enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabledSet() {
            return enabled != null;
        }

        public Set<UpdateSection> getSections() {
            return sections;
        }

        public void setSections(Set<UpdateSection> sections) {
            this.sections.clear();
            this.sections.addAll(sections);
        }
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

    public static class Milestone {
        private Boolean close;
        private String name;

        void setAll(Milestone changelog) {
            this.close = changelog.close;
            this.name = changelog.name;
        }

        public Boolean isClose() {
            return close == null || close;
        }

        public void setClose(Boolean close) {
            this.close = close;
        }

        public boolean isCloseSet() {
            return close != null;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
