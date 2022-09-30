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
 * @since 0.4.0
 */
public final class GenericGitReleaser extends BaseReleaser<org.jreleaser.model.api.release.GenericGitReleaser, GenericGitReleaser> {
    private final org.jreleaser.model.api.release.GenericGitReleaser immutable = new org.jreleaser.model.api.release.GenericGitReleaser() {
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
            return GenericGitReleaser.this.isSign();
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
            return update.asImmutable();
        }

        @Override
        public String getApiEndpoint() {
            return apiEndpoint;
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
        public boolean isSignatures() {
            return GenericGitReleaser.this.isSignatures();
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
            return unmodifiableMap(GenericGitReleaser.this.asMap(full));
        }

        @Override
        public boolean isEnabled() {
            return GenericGitReleaser.this.isEnabled();
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
