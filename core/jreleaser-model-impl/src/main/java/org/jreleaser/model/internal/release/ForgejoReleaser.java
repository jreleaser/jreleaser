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
 * @since 1.18.0
 */
public class ForgejoReleaser extends BaseReleaser<org.jreleaser.model.api.release.ForgejoReleaser, ForgejoReleaser> {
    private static final long serialVersionUID = 7907765447614974335L;

    private Boolean draft;

    @JsonIgnore
    private final org.jreleaser.model.api.release.ForgejoReleaser immutable = new org.jreleaser.model.api.release.ForgejoReleaser() {
        private static final long serialVersionUID = 6619063750320880824L;

        @Override
        public boolean isPrerelease() {
            return ForgejoReleaser.this.isPrerelease();
        }

        @Override
        public boolean isDraft() {
            return ForgejoReleaser.this.isDraft();
        }

        @Override
        public String getServiceName() {
            return ForgejoReleaser.this.getServiceName();
        }

        @Override
        public boolean isReleaseSupported() {
            return ForgejoReleaser.this.isReleaseSupported();
        }

        @Override
        public String getCanonicalRepoName() {
            return ForgejoReleaser.this.getCanonicalRepoName();
        }

        @Override
        public String getReverseRepoHost() {
            return ForgejoReleaser.this.getReverseRepoHost();
        }

        @Override
        public boolean isMatch() {
            return ForgejoReleaser.this.isMatch();
        }

        @Override
        public String getHost() {
            return ForgejoReleaser.this.getHost();
        }

        @Override
        public String getName() {
            return ForgejoReleaser.this.getName();
        }

        @Override
        public String getRepoUrl() {
            return ForgejoReleaser.this.getRepoUrl();
        }

        @Override
        public String getRepoCloneUrl() {
            return ForgejoReleaser.this.getRepoCloneUrl();
        }

        @Override
        public String getCommitUrl() {
            return ForgejoReleaser.this.getCommitUrl();
        }

        @Override
        public String getSrcUrl() {
            return ForgejoReleaser.this.getSrcUrl();
        }

        @Override
        public String getDownloadUrl() {
            return ForgejoReleaser.this.getDownloadUrl();
        }

        @Override
        public String getReleaseNotesUrl() {
            return ForgejoReleaser.this.getReleaseNotesUrl();
        }

        @Override
        public String getLatestReleaseUrl() {
            return ForgejoReleaser.this.getLatestReleaseUrl();
        }

        @Override
        public String getIssueTrackerUrl() {
            return ForgejoReleaser.this.getIssueTrackerUrl();
        }

        @Override
        public String getUsername() {
            return ForgejoReleaser.this.getUsername();
        }

        @Override
        public String getToken() {
            return ForgejoReleaser.this.getToken();
        }

        @Override
        public String getTagName() {
            return ForgejoReleaser.this.getTagName();
        }

        @Override
        public String getPreviousTagName() {
            return ForgejoReleaser.this.getPreviousTagName();
        }

        @Override
        public String getReleaseName() {
            return ForgejoReleaser.this.getReleaseName();
        }

        @Override
        public String getBranch() {
            return ForgejoReleaser.this.getBranch();
        }

        @Override
        public String getBranchPush() {
            return ForgejoReleaser.this.getBranchPush();
        }

        @Override
        public Prerelease getPrerelease() {
            return ForgejoReleaser.this.getPrerelease().asImmutable();
        }

        @Override
        public boolean isSign() {
            return ForgejoReleaser.this.isSign();
        }

        @Override
        public org.jreleaser.model.api.release.Changelog getChangelog() {
            return ForgejoReleaser.this.getChangelog().asImmutable();
        }

        @Override
        public Milestone getMilestone() {
            return ForgejoReleaser.this.getMilestone().asImmutable();
        }

        @Override
        public Issues getIssues() {
            return ForgejoReleaser.this.getIssues().asImmutable();
        }

        @Override
        public boolean isSkipTag() {
            return ForgejoReleaser.this.isSkipTag();
        }

        @Override
        public boolean isSkipRelease() {
            return ForgejoReleaser.this.isSkipRelease();
        }

        @Override
        public boolean isOverwrite() {
            return ForgejoReleaser.this.isOverwrite();
        }

        @Override
        public Update getUpdate() {
            return ForgejoReleaser.this.getUpdate().asImmutable();
        }

        @Override
        public String getApiEndpoint() {
            return ForgejoReleaser.this.getApiEndpoint();
        }

        @Override
        public boolean isArtifacts() {
            return ForgejoReleaser.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return ForgejoReleaser.this.isFiles();
        }

        @Override
        public boolean isChecksums() {
            return ForgejoReleaser.this.isChecksums();
        }

        @Override
        public boolean isCatalogs() {
            return ForgejoReleaser.this.isCatalogs();
        }

        @Override
        public boolean isSignatures() {
            return ForgejoReleaser.this.isSignatures();
        }

        @Override
        public Active getUploadAssets() {
            return ForgejoReleaser.this.getUploadAssets();
        }

        @Override
        public org.jreleaser.model.api.common.CommitAuthor getCommitAuthor() {
            return ForgejoReleaser.this.getCommitAuthor().asImmutable();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(ForgejoReleaser.this.asMap(full));
        }

        @Override
        public boolean isEnabled() {
            return ForgejoReleaser.this.isEnabled();
        }

        @Override
        public String getOwner() {
            return ForgejoReleaser.this.getOwner();
        }

        @Override
        public Integer getConnectTimeout() {
            return ForgejoReleaser.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return ForgejoReleaser.this.getReadTimeout();
        }
    };

    public ForgejoReleaser() {
        this(org.jreleaser.model.api.release.ForgejoReleaser.TYPE);
    }

    ForgejoReleaser(String name) {
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
    public org.jreleaser.model.api.release.ForgejoReleaser asImmutable() {
        return immutable;
    }

    @Override
    public void merge(ForgejoReleaser source) {
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
