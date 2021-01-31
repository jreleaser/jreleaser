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
package org.kordamp.jreleaser.maven.plugin;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class GitService extends AbstractDomain {
    private String repoHost;
    private String repoOwner;
    private String repoName;
    private String downloadUrlFormat;
    private String releaseNotesUrlFormat;
    private String latestReleaseUrlFormat;
    private String issueTrackerUrlFormat;
    private String authorization;
    private String tagName;
    private String releaseName;
    private Changelog changelog = new Changelog();
    private boolean overwrite;
    private boolean allowUploadToExisting;
    private String apiEndpoint;

    void setAll(GitService service) {
        this.repoHost = service.repoHost;
        this.repoOwner = service.repoOwner;
        this.repoName = service.repoName;
        this.downloadUrlFormat = service.downloadUrlFormat;
        this.releaseNotesUrlFormat = service.releaseNotesUrlFormat;
        this.latestReleaseUrlFormat = service.latestReleaseUrlFormat;
        this.issueTrackerUrlFormat = service.issueTrackerUrlFormat;
        this.authorization = service.authorization;
        this.tagName = service.tagName;
        this.releaseName = service.releaseName;
        this.overwrite = service.overwrite;
        this.allowUploadToExisting = service.allowUploadToExisting;
        this.apiEndpoint = service.apiEndpoint;
        this.changelog.setAll(service.changelog);
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

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public Changelog getChangelog() {
        return changelog;
    }

    public void setChangelog(Changelog changelog) {
        this.changelog = changelog;
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
        map.put("repoOwner", repoOwner);
        map.put("repoName", repoName);
        map.put("downloadUrlFormat", downloadUrlFormat);
        map.put("releaseNotesUrlFormat", releaseNotesUrlFormat);
        map.put("latestReleaseUrlFormat", latestReleaseUrlFormat);
        map.put("issueTrackerUrlFormat", issueTrackerUrlFormat);
        map.put("authorization", authorization);
        map.put("tagName", tagName);
        map.put("releaseName", releaseName);
        map.put("overwrite", overwrite);
        map.put("allowUploadToExisting", allowUploadToExisting);
        map.put("apiEndpoint", apiEndpoint);
        map.put("changelog", changelog.asMap());
        return map;
    }
}
