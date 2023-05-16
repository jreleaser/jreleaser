/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
import org.jreleaser.model.api.common.CommitAuthor;
import org.jreleaser.model.api.release.Changelog;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Hasnae Rehioui
 * @since 1.7.0
 */
public class BitbucketcloudReleaser extends BaseReleaser<org.jreleaser.model.api.release.BitbucketcloudReleaser, BitbucketcloudReleaser> {
    private static final long serialVersionUID = 3160208925103963195L;

    @JsonIgnore
    private final org.jreleaser.model.api.release.BitbucketcloudReleaser immutable = new org.jreleaser.model.api.release.BitbucketcloudReleaser() {
        private static final long serialVersionUID = -7158952152230034613L;

        @Override
        public String getServiceName() {
            return BitbucketcloudReleaser.this.getServiceName();
        }

        @Override
        public boolean isReleaseSupported() {
            return BitbucketcloudReleaser.this.isReleaseSupported();
        }

        @Override
        public String getCanonicalRepoName() {
            return BitbucketcloudReleaser.this.getCanonicalRepoName();
        }

        @Override
        public String getReverseRepoHost() {
            return BitbucketcloudReleaser.this.getReverseRepoHost();
        }

        @Override
        public boolean isMatch() {
            return BitbucketcloudReleaser.this.isMatch();
        }

        @Override
        public String getHost() {
            return BitbucketcloudReleaser.this.getHost();
        }

        @Override
        public String getName() {
            return BitbucketcloudReleaser.this.getName();
        }

        @Override
        public String getRepoUrl() {
            return BitbucketcloudReleaser.this.getRepoUrl();
        }

        @Override
        public String getRepoCloneUrl() {
            return BitbucketcloudReleaser.this.getRepoCloneUrl();
        }

        @Override
        public String getCommitUrl() {
            return BitbucketcloudReleaser.this.getCommitUrl();
        }

        @Override
        public String getSrcUrl() {
            return BitbucketcloudReleaser.this.getSrcUrl();
        }

        @Override
        public String getDownloadUrl() {
            return BitbucketcloudReleaser.this.getDownloadUrl();
        }

        @Override
        public String getReleaseNotesUrl() {
            return BitbucketcloudReleaser.this.getReleaseNotesUrl();
        }

        @Override
        public String getLatestReleaseUrl() {
            return BitbucketcloudReleaser.this.getLatestReleaseUrl();
        }

        @Override
        public String getIssueTrackerUrl() {
            return BitbucketcloudReleaser.this.getIssueTrackerUrl();
        }

        @Override
        public String getUsername() {
            return BitbucketcloudReleaser.this.getUsername();
        }

        @Override
        public String getToken() {
            return BitbucketcloudReleaser.this.getToken();
        }

        @Override
        public String getTagName() {
            return BitbucketcloudReleaser.this.getTagName();
        }

        @Override
        public String getPreviousTagName() {
            return BitbucketcloudReleaser.this.getPreviousTagName();
        }

        @Override
        public String getReleaseName() {
            return BitbucketcloudReleaser.this.getReleaseName();
        }

        @Override
        public String getBranch() {
            return BitbucketcloudReleaser.this.getBranch();
        }

        @Override
        public String getBranchPush() {
            return BitbucketcloudReleaser.this.getBranchPush();
        }

        @Override
        public Prerelease getPrerelease() {
            return BitbucketcloudReleaser.this.getPrerelease().asImmutable();
        }

        @Override
        public boolean isSign() {
            return BitbucketcloudReleaser.this.isSign();
        }

        @Override
        public Changelog getChangelog() {
            return BitbucketcloudReleaser.this.getChangelog().asImmutable();
        }

        @Override
        public Milestone getMilestone() {
            return BitbucketcloudReleaser.this.getMilestone().asImmutable();
        }

        @Override
        public Issues getIssues() {
            return BitbucketcloudReleaser.this.getIssues().asImmutable();
        }

        @Override
        public boolean isSkipTag() {
            return BitbucketcloudReleaser.this.isSkipTag();
        }

        @Override
        public boolean isSkipRelease() {
            return BitbucketcloudReleaser.this.isSkipRelease();
        }

        @Override
        public boolean isOverwrite() {
            return BitbucketcloudReleaser.this.isOverwrite();
        }

        @Override
        public Update getUpdate() {
            return BitbucketcloudReleaser.this.getUpdate().asImmutable();
        }

        @Override
        public String getApiEndpoint() {
            return BitbucketcloudReleaser.this.getApiEndpoint();
        }

        @Override
        public boolean isArtifacts() {
            return BitbucketcloudReleaser.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return BitbucketcloudReleaser.this.isFiles();
        }

        @Override
        public boolean isChecksums() {
            return BitbucketcloudReleaser.this.isChecksums();
        }

        @Override
        public boolean isCatalogs() {
            return BitbucketcloudReleaser.this.isCatalogs();
        }

        @Override
        public boolean isSignatures() {
            return BitbucketcloudReleaser.this.isSignatures();
        }

        @Override
        public Active getUploadAssets() {
            return BitbucketcloudReleaser.this.getUploadAssets();
        }

        @Override
        public boolean isPrerelease() {
            return BitbucketcloudReleaser.this.isPrerelease();
        }

        @Override
        public CommitAuthor getCommitAuthor() {
            return BitbucketcloudReleaser.this.getCommitAuthor().asImmutable();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(BitbucketcloudReleaser.this.asMap(full));
        }

        @Override
        public boolean isEnabled() {
            return BitbucketcloudReleaser.this.isEnabled();
        }

        @Override
        public String getOwner() {
            return BitbucketcloudReleaser.this.getOwner();
        }

        @Override
        public Integer getConnectTimeout() {
            return BitbucketcloudReleaser.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return BitbucketcloudReleaser.this.getReadTimeout();
        }
    };

    public BitbucketcloudReleaser() {
        super(org.jreleaser.model.api.release.BitbucketcloudReleaser.TYPE, true);
        setHost("bitbucket.org");
        setRepoUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}");
        setRepoCloneUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}.git");
        setCommitUrl("https:://{{repoHost}}/{{repoOwner}}/{{repoName}}/commits");
        setSrcUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/src/{{repoBranch}}");
    }

    @Override
    public String getReverseRepoHost() {
        return "org.bitbucket";
    }

    @Override
    public org.jreleaser.model.api.release.BitbucketcloudReleaser asImmutable() {
        return immutable;
    }
}
