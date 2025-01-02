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
 * @since 0.4.0
 */
public class CodebergReleaser extends BaseReleaser<org.jreleaser.model.api.release.CodebergReleaser, CodebergReleaser> {
    private static final long serialVersionUID = -3260112972191332390L;

    private Boolean draft;

    @JsonIgnore
    private final org.jreleaser.model.api.release.CodebergReleaser immutable = new org.jreleaser.model.api.release.CodebergReleaser() {
        private static final long serialVersionUID = -4853178830155860635L;

        @Override
        public boolean isPrerelease() {
            return CodebergReleaser.this.isPrerelease();
        }

        @Override
        public boolean isDraft() {
            return CodebergReleaser.this.isDraft();
        }

        @Override
        public String getServiceName() {
            return CodebergReleaser.this.getServiceName();
        }

        @Override
        public boolean isReleaseSupported() {
            return CodebergReleaser.this.isReleaseSupported();
        }

        @Override
        public String getCanonicalRepoName() {
            return CodebergReleaser.this.getCanonicalRepoName();
        }

        @Override
        public String getReverseRepoHost() {
            return CodebergReleaser.this.getReverseRepoHost();
        }

        @Override
        public boolean isMatch() {
            return CodebergReleaser.this.isMatch();
        }

        @Override
        public String getHost() {
            return CodebergReleaser.this.getHost();
        }

        @Override
        public String getName() {
            return CodebergReleaser.this.getName();
        }

        @Override
        public String getRepoUrl() {
            return CodebergReleaser.this.getRepoUrl();
        }

        @Override
        public String getRepoCloneUrl() {
            return CodebergReleaser.this.getRepoCloneUrl();
        }

        @Override
        public String getCommitUrl() {
            return CodebergReleaser.this.getCommitUrl();
        }

        @Override
        public String getSrcUrl() {
            return CodebergReleaser.this.getSrcUrl();
        }

        @Override
        public String getDownloadUrl() {
            return CodebergReleaser.this.getDownloadUrl();
        }

        @Override
        public String getReleaseNotesUrl() {
            return CodebergReleaser.this.getReleaseNotesUrl();
        }

        @Override
        public String getLatestReleaseUrl() {
            return CodebergReleaser.this.getLatestReleaseUrl();
        }

        @Override
        public String getIssueTrackerUrl() {
            return CodebergReleaser.this.getIssueTrackerUrl();
        }

        @Override
        public String getUsername() {
            return CodebergReleaser.this.getUsername();
        }

        @Override
        public String getToken() {
            return CodebergReleaser.this.getToken();
        }

        @Override
        public String getTagName() {
            return CodebergReleaser.this.getTagName();
        }

        @Override
        public String getPreviousTagName() {
            return CodebergReleaser.this.getPreviousTagName();
        }

        @Override
        public String getReleaseName() {
            return CodebergReleaser.this.getReleaseName();
        }

        @Override
        public String getBranch() {
            return CodebergReleaser.this.getBranch();
        }

        @Override
        public String getBranchPush() {
            return CodebergReleaser.this.getBranchPush();
        }

        @Override
        public Prerelease getPrerelease() {
            return CodebergReleaser.this.getPrerelease().asImmutable();
        }

        @Override
        public boolean isSign() {
            return CodebergReleaser.this.isSign();
        }

        @Override
        public org.jreleaser.model.api.release.Changelog getChangelog() {
            return CodebergReleaser.this.getChangelog().asImmutable();
        }

        @Override
        public Milestone getMilestone() {
            return CodebergReleaser.this.getMilestone().asImmutable();
        }

        @Override
        public Issues getIssues() {
            return CodebergReleaser.this.getIssues().asImmutable();
        }

        @Override
        public boolean isSkipTag() {
            return CodebergReleaser.this.isSkipTag();
        }

        @Override
        public boolean isSkipRelease() {
            return CodebergReleaser.this.isSkipRelease();
        }

        @Override
        public boolean isOverwrite() {
            return CodebergReleaser.this.isOverwrite();
        }

        @Override
        public Update getUpdate() {
            return CodebergReleaser.this.getUpdate().asImmutable();
        }

        @Override
        public String getApiEndpoint() {
            return CodebergReleaser.this.getApiEndpoint();
        }

        @Override
        public boolean isArtifacts() {
            return CodebergReleaser.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return CodebergReleaser.this.isFiles();
        }

        @Override
        public boolean isChecksums() {
            return CodebergReleaser.this.isChecksums();
        }

        @Override
        public boolean isCatalogs() {
            return CodebergReleaser.this.isCatalogs();
        }

        @Override
        public boolean isSignatures() {
            return CodebergReleaser.this.isSignatures();
        }

        @Override
        public Active getUploadAssets() {
            return CodebergReleaser.this.getUploadAssets();
        }

        @Override
        public org.jreleaser.model.api.common.CommitAuthor getCommitAuthor() {
            return CodebergReleaser.this.getCommitAuthor().asImmutable();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(CodebergReleaser.this.asMap(full));
        }

        @Override
        public boolean isEnabled() {
            return CodebergReleaser.this.isEnabled();
        }

        @Override
        public String getOwner() {
            return CodebergReleaser.this.getOwner();
        }

        @Override
        public Integer getConnectTimeout() {
            return CodebergReleaser.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return CodebergReleaser.this.getReadTimeout();
        }
    };

    public CodebergReleaser() {
        this(org.jreleaser.model.api.release.CodebergReleaser.TYPE);
    }

    CodebergReleaser(String name) {
        super(name, true);
        setHost("codeberg.org");
        setApiEndpoint("https://codeberg.org");
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
    public org.jreleaser.model.api.release.CodebergReleaser asImmutable() {
        return immutable;
    }

    @Override
    public void merge(CodebergReleaser source) {
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
