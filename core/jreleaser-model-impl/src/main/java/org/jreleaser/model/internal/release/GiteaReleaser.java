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

import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class GiteaReleaser extends BaseReleaser<org.jreleaser.model.api.release.GiteaReleaser, GiteaReleaser> {
    private static final long serialVersionUID = -1447563457831439973L;

    private Boolean draft;

    @JsonIgnore
    private final org.jreleaser.model.api.release.GiteaReleaser immutable = new org.jreleaser.model.api.release.GiteaReleaser() {
        private static final long serialVersionUID = -2135534604107948779L;

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
            return GiteaReleaser.this.getHost();
        }

        @Override
        public String getName() {
            return GiteaReleaser.this.getName();
        }

        @Override
        public String getRepoUrl() {
            return GiteaReleaser.this.getRepoUrl();
        }

        @Override
        public String getRepoCloneUrl() {
            return GiteaReleaser.this.getRepoCloneUrl();
        }

        @Override
        public String getCommitUrl() {
            return GiteaReleaser.this.getCommitUrl();
        }

        @Override
        public String getSrcUrl() {
            return GiteaReleaser.this.getSrcUrl();
        }

        @Override
        public String getDownloadUrl() {
            return GiteaReleaser.this.getDownloadUrl();
        }

        @Override
        public String getReleaseNotesUrl() {
            return GiteaReleaser.this.getReleaseNotesUrl();
        }

        @Override
        public String getLatestReleaseUrl() {
            return GiteaReleaser.this.getLatestReleaseUrl();
        }

        @Override
        public String getIssueTrackerUrl() {
            return GiteaReleaser.this.getIssueTrackerUrl();
        }

        @Override
        public String getUsername() {
            return GiteaReleaser.this.getUsername();
        }

        @Override
        public String getToken() {
            return GiteaReleaser.this.getToken();
        }

        @Override
        public String getTagName() {
            return GiteaReleaser.this.getTagName();
        }

        @Override
        public String getPreviousTagName() {
            return GiteaReleaser.this.getPreviousTagName();
        }

        @Override
        public String getReleaseName() {
            return GiteaReleaser.this.getReleaseName();
        }

        @Override
        public String getBranch() {
            return GiteaReleaser.this.getBranch();
        }

        @Override
        public String getBranchPush() {
            return GiteaReleaser.this.getBranchPush();
        }

        @Override
        public Prerelease getPrerelease() {
            return GiteaReleaser.this.getPrerelease().asImmutable();
        }

        @Override
        public boolean isSign() {
            return GiteaReleaser.this.isSign();
        }

        @Override
        public org.jreleaser.model.api.release.Changelog getChangelog() {
            return GiteaReleaser.this.getChangelog().asImmutable();
        }

        @Override
        public Milestone getMilestone() {
            return GiteaReleaser.this.getMilestone().asImmutable();
        }

        @Override
        public Issues getIssues() {
            return GiteaReleaser.this.getIssues().asImmutable();
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
            return GiteaReleaser.this.getUpdate().asImmutable();
        }

        @Override
        public String getApiEndpoint() {
            return GiteaReleaser.this.getApiEndpoint();
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
        public boolean isCatalogs() {
            return GiteaReleaser.this.isCatalogs();
        }

        @Override
        public boolean isSignatures() {
            return GiteaReleaser.this.isSignatures();
        }

        @Override
        public Active getUploadAssets() {
            return GiteaReleaser.this.getUploadAssets();
        }

        @Override
        public org.jreleaser.model.api.common.CommitAuthor getCommitAuthor() {
            return GiteaReleaser.this.getCommitAuthor().asImmutable();
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
            return GiteaReleaser.this.getOwner();
        }

        @Override
        public Integer getConnectTimeout() {
            return GiteaReleaser.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return GiteaReleaser.this.getReadTimeout();
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

    @Override
    public org.jreleaser.model.api.release.GiteaReleaser asImmutable() {
        return immutable;
    }

    @Override
    public void merge(GiteaReleaser source) {
        super.merge(source);
        this.draft = merge(this.draft, source.draft);
    }

    public boolean isDraft() {
        return null != draft && draft;
    }

    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    public boolean isDraftSet() {
        return null != draft;
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
