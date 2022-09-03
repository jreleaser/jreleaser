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
package org.jreleaser.model.internal.release;

import org.jreleaser.model.Active;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class GiteaReleaser extends BaseReleaser<GiteaReleaser> {
    private Boolean draft;

    private final org.jreleaser.model.api.release.GiteaReleaser immutable = new org.jreleaser.model.api.release.GiteaReleaser() {
        @Override
        public boolean isPrerelease() {
            return GiteaReleaser.this.isPrerelease();
        }

        @Override
        public boolean isDraft() {
            return GiteaReleaser.this.isDraft();
        }

        @Override
        public String getServiceName() {
            return GiteaReleaser.this.getServiceName();
        }

        @Override
        public boolean isReleaseSupported() {
            return GiteaReleaser.this.isReleaseSupported();
        }

        @Override
        public String getCanonicalRepoName() {
            return GiteaReleaser.this.getCanonicalRepoName();
        }

        @Override
        public String getReverseRepoHost() {
            return GiteaReleaser.this.getReverseRepoHost();
        }

        @Override
        public boolean isMatch() {
            return GiteaReleaser.this.isMatch();
        }

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getRepoUrl() {
            return repoUrl;
        }

        @Override
        public String getRepoCloneUrl() {
            return repoCloneUrl;
        }

        @Override
        public String getCommitUrl() {
            return commitUrl;
        }

        @Override
        public String getSrcUrl() {
            return srcUrl;
        }

        @Override
        public String getDownloadUrl() {
            return downloadUrl;
        }

        @Override
        public String getReleaseNotesUrl() {
            return releaseNotesUrl;
        }

        @Override
        public String getLatestReleaseUrl() {
            return latestReleaseUrl;
        }

        @Override
        public String getIssueTrackerUrl() {
            return issueTrackerUrl;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getToken() {
            return token;
        }

        @Override
        public String getTagName() {
            return tagName;
        }

        @Override
        public String getPreviousTagName() {
            return previousTagName;
        }

        @Override
        public String getReleaseName() {
            return releaseName;
        }

        @Override
        public String getBranch() {
            return branch;
        }

        @Override
        public Prerelease getPrerelease() {
            return prerelease.asImmutable();
        }

        @Override
        public boolean isSign() {
            return GiteaReleaser.this.isSign();
        }

        @Override
        public org.jreleaser.model.api.release.Changelog getChangelog() {
            return changelog.asImmutable();
        }

        @Override
        public Milestone getMilestone() {
            return milestone.asImmutable();
        }

        @Override
        public Issues getIssues() {
            return issues.asImmutable();
        }

        @Override
        public boolean isSkipTag() {
            return GiteaReleaser.this.isSkipTag();
        }

        @Override
        public boolean isSkipRelease() {
            return GiteaReleaser.this.isSkipRelease();
        }

        @Override
        public boolean isOverwrite() {
            return GiteaReleaser.this.isOverwrite();
        }

        @Override
        public Update getUpdate() {
            return update.asImmutable();
        }

        @Override
        public String getApiEndpoint() {
            return apiEndpoint;
        }

        @Override
        public boolean isArtifacts() {
            return GiteaReleaser.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return GiteaReleaser.this.isFiles();
        }

        @Override
        public boolean isChecksums() {
            return GiteaReleaser.this.isChecksums();
        }

        @Override
        public boolean isSignatures() {
            return GiteaReleaser.this.isSignatures();
        }

        @Override
        public Active getUploadAssets() {
            return uploadAssets;
        }

        @Override
        public org.jreleaser.model.api.common.CommitAuthor getCommitAuthor() {
            return commitAuthor.asImmutable();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(GiteaReleaser.this.asMap(full));
        }

        @Override
        public boolean isEnabled() {
            return GiteaReleaser.this.isEnabled();
        }

        @Override
        public String getOwner() {
            return owner;
        }

        @Override
        public Integer getConnectTimeout() {
            return connectTimeout;
        }

        @Override
        public Integer getReadTimeout() {
            return readTimeout;
        }
    };

    public GiteaReleaser() {
        this(org.jreleaser.model.api.release.GiteaReleaser.TYPE);
    }

    GiteaReleaser(String name) {
        super(name, true);
        setRepoUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}");
        setRepoCloneUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}.git");
        setCommitUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/commits");
        setSrcUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/src/{{repoBranch}}");
        setDownloadUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/releases/download/{{tagName}}/{{artifactFile}}");
        setReleaseNotesUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/releases/tag/{{tagName}}");
        setLatestReleaseUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/releases/latest");
        setIssueTrackerUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/issues");
    }

    public org.jreleaser.model.api.release.GiteaReleaser asImmutable() {
        return immutable;
    }

    @Override
    public void merge(GiteaReleaser source) {
        super.merge(source);
        this.draft = merge(this.draft, source.draft);
    }

    public boolean isDraft() {
        return draft != null && draft;
    }

    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    public boolean isDraftSet() {
        return draft != null;
    }

    @Override
    public String getReverseRepoHost() {
        return null;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = super.asMap(full);
        map.put("draft", isDraft());
        return map;
    }
}
