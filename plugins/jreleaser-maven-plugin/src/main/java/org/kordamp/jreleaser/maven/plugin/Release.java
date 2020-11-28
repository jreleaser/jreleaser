/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Andres Almiray.
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
package org.kordamp.jreleaser.maven.plugin;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.kordamp.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Release extends AbstractDomain {
    private RepoType repoType = RepoType.GITHUB;
    private String repoHost;
    private String repoOwner;
    private String repoName;
    private String downloadUrlFormat;
    private String releaseNotesUrlFormat;
    private String latestReleaseUrlFormat;
    private String issueTrackerUrlFormat;
    private String authorization;
    private String tagName;
    private String targetCommitish = "main";
    private String releaseName;
    private String body = "";
    private boolean draft;
    private boolean prerelease;
    private boolean overwrite;
    private boolean allowUploadToExisting;
    private String apiEndpoint;

    void setAll(Release release) {
        this.repoType = release.repoType;
        this.repoHost = release.repoHost;
        this.repoOwner = release.repoOwner;
        this.repoName = release.repoName;
        this.downloadUrlFormat = release.downloadUrlFormat;
        this.releaseNotesUrlFormat = release.releaseNotesUrlFormat;
        this.latestReleaseUrlFormat = release.latestReleaseUrlFormat;
        this.issueTrackerUrlFormat = release.issueTrackerUrlFormat;
        this.authorization = release.authorization;
        this.tagName = release.tagName;
        this.targetCommitish = release.targetCommitish;
        this.releaseName = release.releaseName;
        this.body = release.body;
        this.draft = release.draft;
        this.prerelease = release.prerelease;
        this.overwrite = release.overwrite;
        this.allowUploadToExisting = release.allowUploadToExisting;
        this.apiEndpoint = release.apiEndpoint;
    }

    public RepoType getRepoType() {
        return repoType;
    }

    public void setRepoType(RepoType repoType) {
        this.repoType = repoType;
    }

    public void setRepoType(String repoType) {
        if (isNotBlank(repoType)) {
            this.repoType = RepoType.valueOf(repoType.toUpperCase());
        }
    }

    public String getRepoHost() {
        return repoHost;
    }

    public void setRepoHost(String repoHost) {
        this.repoHost = repoHost;
    }

    public String getRepoOwner() {
        return repoOwner;
    }

    public void setRepoOwner(String repoOwner) {
        this.repoOwner = repoOwner;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
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

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTargetCommitish() {
        return targetCommitish;
    }

    public void setTargetCommitish(String targetCommitish) {
        this.targetCommitish = targetCommitish;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    public boolean isPrerelease() {
        return prerelease;
    }

    public void setPrerelease(boolean prerelease) {
        this.prerelease = prerelease;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean isAllowUploadToExisting() {
        return allowUploadToExisting;
    }

    public void setAllowUploadToExisting(boolean allowUploadToExisting) {
        this.allowUploadToExisting = allowUploadToExisting;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("repoHost", repoHost);
        map.put("repoType", repoType);
        map.put("repoOwner", repoOwner);
        map.put("repoName", repoName);
        map.put("downloadUrlFormat", downloadUrlFormat);
        map.put("releaseNotesUrlFormat", releaseNotesUrlFormat);
        map.put("latestReleaseUrlFormat", latestReleaseUrlFormat);
        map.put("issueTrackerUrlFormat", issueTrackerUrlFormat);
        map.put("authorization", authorization);
        map.put("tagName", tagName);
        map.put("targetCommitish", targetCommitish);
        map.put("releaseName", releaseName);
        map.put("body ", isNotBlank(body));
        map.put("draft", draft);
        map.put("prerelease", prerelease);
        map.put("overwrite", overwrite);
        map.put("allowUploadToExisting", allowUploadToExisting);
        map.put("apiEndpoint", apiEndpoint);
        return map;
    }

    public enum RepoType {
        GITHUB("github.com",
            "https://{{repoHost}}/{{repoOwner}}/{{repoName}}/releases/download/v{{projectVersion}}/{{artifactFileName}}",
            "https://{{repoHost}}/{{repoOwner}}/{{repoName}}/releases/tag/v{{projectVersion}}",
            "https://{{repoHost}}/{{repoOwner}}/{{repoName}}/releases/latest",
            "https://{{repoHost}}/{{repoOwner}}/{{repoName}}/issues");
        /*
        GITLAB("gitlab.com",
            "https://{{repoHost}}/{{repoOwner}}/{{repoName}}/-/archive/v{{projectVersion}}/{{artifactFileName}}",
            "https://{{repoHost}}/{{repoOwner}}/{{repoName}}/-/releases/v{{projectVersion}}",
            "https://{{repoHost}}/{{repoOwner}}/{{repoName}}/-/releases/v{{projectVersion}}",
            "https://{{repoHost}}/{{repoOwner}}/{{repoName}}/-/issues");
         */

        private final String repoHost;
        private final String downloadUrlFormat;
        private final String releaseNotesUrlFormat;
        private final String latestReleaseUrlFormat;
        private final String issueTrackerUrlFormat;

        RepoType(String repoHost,
                 String downloadUrlFormat,
                 String releaseNotesUrlFormat,
                 String latestReleaseUrlFormat,
                 String issueTrackerUrlFormat) {
            this.repoHost = repoHost;
            this.downloadUrlFormat = downloadUrlFormat;
            this.releaseNotesUrlFormat = releaseNotesUrlFormat;
            this.latestReleaseUrlFormat = latestReleaseUrlFormat;
            this.issueTrackerUrlFormat = issueTrackerUrlFormat;
        }

        String repoHost() {
            return this.repoHost;
        }

        String downloadUrlFormat() {
            return this.downloadUrlFormat;
        }

        String releaseNotesUrlFormat() {
            return this.releaseNotesUrlFormat;
        }

        String latestReleaseUrlFormat() {
            return this.latestReleaseUrlFormat;
        }

        String issueTrackerUrlFormat() {
            return this.issueTrackerUrlFormat;
        }
    }
}
