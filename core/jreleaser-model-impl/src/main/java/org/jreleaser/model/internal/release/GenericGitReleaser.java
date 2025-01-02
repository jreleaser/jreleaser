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
public final class GenericGitReleaser extends BaseReleaser<org.jreleaser.model.api.release.GenericGitReleaser, GenericGitReleaser> {
    private static final long serialVersionUID = -2400306764813478894L;

    @JsonIgnore
    private final org.jreleaser.model.api.release.GenericGitReleaser immutable = new org.jreleaser.model.api.release.GenericGitReleaser() {
        private static final long serialVersionUID = -2894120261199811013L;

        @Override
        public boolean isPrerelease() {
            return GenericGitReleaser.this.isPrerelease();
        }

        @Override
        public String getServiceName() {
            return GenericGitReleaser.this.getServiceName();
        }

        @Override
        public boolean isReleaseSupported() {
            return GenericGitReleaser.this.isReleaseSupported();
        }

        @Override
        public String getCanonicalRepoName() {
            return GenericGitReleaser.this.getCanonicalRepoName();
        }

        @Override
        public String getReverseRepoHost() {
            return GenericGitReleaser.this.getReverseRepoHost();
        }

        @Override
        public boolean isMatch() {
            return GenericGitReleaser.this.isMatch();
        }

        @Override
        public String getHost() {
            return GenericGitReleaser.this.getHost();
        }

        @Override
        public String getName() {
            return GenericGitReleaser.this.getName();
        }

        @Override
        public String getRepoUrl() {
            return GenericGitReleaser.this.getRepoUrl();
        }

        @Override
        public String getRepoCloneUrl() {
            return GenericGitReleaser.this.getRepoCloneUrl();
        }

        @Override
        public String getCommitUrl() {
            return GenericGitReleaser.this.getCommitUrl();
        }

        @Override
        public String getSrcUrl() {
            return GenericGitReleaser.this.getSrcUrl();
        }

        @Override
        public String getDownloadUrl() {
            return GenericGitReleaser.this.getDownloadUrl();
        }

        @Override
        public String getReleaseNotesUrl() {
            return GenericGitReleaser.this.getReleaseNotesUrl();
        }

        @Override
        public String getLatestReleaseUrl() {
            return GenericGitReleaser.this.getLatestReleaseUrl();
        }

        @Override
        public String getIssueTrackerUrl() {
            return GenericGitReleaser.this.getIssueTrackerUrl();
        }

        @Override
        public String getUsername() {
            return GenericGitReleaser.this.getUsername();
        }

        @Override
        public String getToken() {
            return GenericGitReleaser.this.getToken();
        }

        @Override
        public String getTagName() {
            return GenericGitReleaser.this.getTagName();
        }

        @Override
        public String getPreviousTagName() {
            return GenericGitReleaser.this.getPreviousTagName();
        }

        @Override
        public String getReleaseName() {
            return GenericGitReleaser.this.getReleaseName();
        }

        @Override
        public String getBranch() {
            return GenericGitReleaser.this.getBranch();
        }

        @Override
        public String getBranchPush() {
            return GenericGitReleaser.this.getBranchPush();
        }

        @Override
        public Prerelease getPrerelease() {
            return GenericGitReleaser.this.getPrerelease().asImmutable();
        }

        @Override
        public boolean isSign() {
            return GenericGitReleaser.this.isSign();
        }

        @Override
        public org.jreleaser.model.api.release.Changelog getChangelog() {
            return GenericGitReleaser.this.getChangelog().asImmutable();
        }

        @Override
        public Milestone getMilestone() {
            return GenericGitReleaser.this.getMilestone().asImmutable();
        }

        @Override
        public Issues getIssues() {
            return GenericGitReleaser.this.getIssues().asImmutable();
        }

        @Override
        public boolean isSkipTag() {
            return GenericGitReleaser.this.isSkipTag();
        }

        @Override
        public boolean isSkipRelease() {
            return GenericGitReleaser.this.isSkipRelease();
        }

        @Override
        public boolean isOverwrite() {
            return GenericGitReleaser.this.isOverwrite();
        }

        @Override
        public Update getUpdate() {
            return GenericGitReleaser.this.getUpdate().asImmutable();
        }

        @Override
        public String getApiEndpoint() {
            return GenericGitReleaser.this.getApiEndpoint();
        }

        @Override
        public boolean isArtifacts() {
            return GenericGitReleaser.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return GenericGitReleaser.this.isFiles();
        }

        @Override
        public boolean isChecksums() {
            return GenericGitReleaser.this.isChecksums();
        }

        @Override
        public boolean isCatalogs() {
            return GenericGitReleaser.this.isCatalogs();
        }

        @Override
        public boolean isSignatures() {
            return GenericGitReleaser.this.isSignatures();
        }

        @Override
        public Active getUploadAssets() {
            return GenericGitReleaser.this.getUploadAssets();
        }

        @Override
        public org.jreleaser.model.api.common.CommitAuthor getCommitAuthor() {
            return GenericGitReleaser.this.getCommitAuthor().asImmutable();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(GenericGitReleaser.this.asMap(full));
        }

        @Override
        public boolean isEnabled() {
            return GenericGitReleaser.this.isEnabled();
        }

        @Override
        public String getOwner() {
            return GenericGitReleaser.this.getOwner();
        }

        @Override
        public Integer getConnectTimeout() {
            return GenericGitReleaser.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return GenericGitReleaser.this.getReadTimeout();
        }
    };

    public GenericGitReleaser() {
        super(org.jreleaser.model.api.release.GenericGitReleaser.TYPE, false);
    }

    @Override
    public org.jreleaser.model.api.release.GenericGitReleaser asImmutable() {
        return immutable;
    }

    @Override
    public String getReverseRepoHost() {
        return "";
    }
}
