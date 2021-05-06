/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class GitService implements Releaser {
    private final CommitAuthor commitAuthor = new CommitAuthor();
    private final Changelog changelog = new Changelog();
    private final Milestone milestone = new Milestone();
    protected Boolean enabled;
    private String host;
    private String owner;
    private String name;
    private String repoUrlFormat;
    private String repoCloneUrlFormat;
    private String commitUrlFormat;
    private String downloadUrlFormat;
    private String releaseNotesUrlFormat;
    private String latestReleaseUrlFormat;
    private String issueTrackerUrlFormat;
    private String username;
    private String token;
    private String tagName;
    private String releaseName;
    private boolean sign;
    private Boolean skipTag;
    private Boolean overwrite;
    private Boolean update;
    private String apiEndpoint;
    private int connectTimeout;
    private int readTimeout;

    void setAll(GitService service) {
        this.enabled = service.enabled;
        this.host = service.host;
        this.owner = service.owner;
        this.name = service.name;
        this.repoUrlFormat = service.repoUrlFormat;
        this.repoCloneUrlFormat = service.repoCloneUrlFormat;
        this.commitUrlFormat = service.commitUrlFormat;
        this.downloadUrlFormat = service.downloadUrlFormat;
        this.releaseNotesUrlFormat = service.releaseNotesUrlFormat;
        this.latestReleaseUrlFormat = service.latestReleaseUrlFormat;
        this.issueTrackerUrlFormat = service.issueTrackerUrlFormat;
        this.username = service.username;
        this.token = service.token;
        this.tagName = service.tagName;
        this.releaseName = service.releaseName;
        this.sign = service.sign;
        this.skipTag = service.skipTag;
        this.overwrite = service.overwrite;
        this.update = service.update;
        this.apiEndpoint = service.apiEndpoint;
        this.connectTimeout = service.connectTimeout;
        this.readTimeout = service.readTimeout;
        setCommitAuthor(service.commitAuthor);
        setChangelog(service.changelog);
        setMilestone(service.milestone);
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

    public String getRepoUrlFormat() {
        return repoUrlFormat;
    }

    public void setRepoUrlFormat(String repoUrlFormat) {
        this.repoUrlFormat = repoUrlFormat;
    }

    public String getRepoCloneUrlFormat() {
        return repoCloneUrlFormat;
    }

    public void setRepoCloneUrlFormat(String repoCloneUrlFormat) {
        this.repoCloneUrlFormat = repoCloneUrlFormat;
    }

    public String getCommitUrlFormat() {
        return commitUrlFormat;
    }

    public void setCommitUrlFormat(String commitUrlFormat) {
        this.commitUrlFormat = commitUrlFormat;
    }

    public String getDownloadUrlFormat() {
        return downloadUrlFormat;
    }

    public void setDownloadUrlFormat(String downloadUrlFormat) {
        this.downloadUrlFormat = downloadUrlFormat;
    }

    public String getReleaseNotesUrlFormat() {
        return releaseNotesUrlFormat;
    }

    public void setReleaseNotesUrlFormat(String releaseNotesUrlFormat) {
        this.releaseNotesUrlFormat = releaseNotesUrlFormat;
    }

    public String getLatestReleaseUrlFormat() {
        return latestReleaseUrlFormat;
    }

    public void setLatestReleaseUrlFormat(String latestReleaseUrlFormat) {
        this.latestReleaseUrlFormat = latestReleaseUrlFormat;
    }

    public String getIssueTrackerUrlFormat() {
        return issueTrackerUrlFormat;
    }

    public void setIssueTrackerUrlFormat(String issueTrackerUrlFormat) {
        this.issueTrackerUrlFormat = issueTrackerUrlFormat;
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
}
