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
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.mustache.TemplateContext;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.KEY_IDENTIFIER;
import static org.jreleaser.model.Constants.KEY_PROJECT_IDENTIFIER;
import static org.jreleaser.model.JReleaserOutput.nag;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class GitlabReleaser extends BaseReleaser<org.jreleaser.model.api.release.GitlabReleaser, GitlabReleaser> {
    private static final long serialVersionUID = 8626675568115866407L;

    private final Map<String, String> uploadLinks = new LinkedHashMap<>();
    private String projectIdentifier;

    @JsonIgnore
    private final org.jreleaser.model.api.release.GitlabReleaser immutable = new org.jreleaser.model.api.release.GitlabReleaser() {
        private static final long serialVersionUID = 6362243270119328338L;

        @Override
        public boolean isPrerelease() {
            return GitlabReleaser.this.isPrerelease();
        }

        @Override
        public String getIdentifier() {
            return getProjectIdentifier();
        }

        @Override
        public String getProjectIdentifier() {
            return projectIdentifier;
        }

        @Override
        public Map<String, String> getUploadLinks() {
            return unmodifiableMap(uploadLinks);
        }

        @Override
        public String getServiceName() {
            return GitlabReleaser.this.getServiceName();
        }

        @Override
        public boolean isReleaseSupported() {
            return GitlabReleaser.this.isReleaseSupported();
        }

        @Override
        public String getCanonicalRepoName() {
            return GitlabReleaser.this.getCanonicalRepoName();
        }

        @Override
        public String getReverseRepoHost() {
            return GitlabReleaser.this.getReverseRepoHost();
        }

        @Override
        public boolean isMatch() {
            return GitlabReleaser.this.isMatch();
        }

        @Override
        public String getHost() {
            return GitlabReleaser.this.getHost();
        }

        @Override
        public String getName() {
            return GitlabReleaser.this.getName();
        }

        @Override
        public String getRepoUrl() {
            return GitlabReleaser.this.getRepoUrl();
        }

        @Override
        public String getRepoCloneUrl() {
            return GitlabReleaser.this.getRepoCloneUrl();
        }

        @Override
        public String getCommitUrl() {
            return GitlabReleaser.this.getCommitUrl();
        }

        @Override
        public String getSrcUrl() {
            return GitlabReleaser.this.getSrcUrl();
        }

        @Override
        public String getDownloadUrl() {
            return GitlabReleaser.this.getDownloadUrl();
        }

        @Override
        public String getReleaseNotesUrl() {
            return GitlabReleaser.this.getReleaseNotesUrl();
        }

        @Override
        public String getLatestReleaseUrl() {
            return GitlabReleaser.this.getLatestReleaseUrl();
        }

        @Override
        public String getIssueTrackerUrl() {
            return GitlabReleaser.this.getIssueTrackerUrl();
        }

        @Override
        public String getUsername() {
            return GitlabReleaser.this.getUsername();
        }

        @Override
        public String getToken() {
            return GitlabReleaser.this.getToken();
        }

        @Override
        public String getTagName() {
            return GitlabReleaser.this.getTagName();
        }

        @Override
        public String getPreviousTagName() {
            return GitlabReleaser.this.getPreviousTagName();
        }

        @Override
        public String getReleaseName() {
            return GitlabReleaser.this.getReleaseName();
        }

        @Override
        public String getBranch() {
            return GitlabReleaser.this.getBranch();
        }

        @Override
        public String getBranchPush() {
            return GitlabReleaser.this.getBranchPush();
        }

        @Override
        public Prerelease getPrerelease() {
            return GitlabReleaser.this.getPrerelease().asImmutable();
        }

        @Override
        public boolean isSign() {
            return GitlabReleaser.this.isSign();
        }

        @Override
        public org.jreleaser.model.api.release.Changelog getChangelog() {
            return GitlabReleaser.this.getChangelog().asImmutable();
        }

        @Override
        public Milestone getMilestone() {
            return GitlabReleaser.this.getMilestone().asImmutable();
        }

        @Override
        public Issues getIssues() {
            return GitlabReleaser.this.getIssues().asImmutable();
        }

        @Override
        public boolean isSkipTag() {
            return GitlabReleaser.this.isSkipTag();
        }

        @Override
        public boolean isSkipRelease() {
            return GitlabReleaser.this.isSkipRelease();
        }

        @Override
        public boolean isOverwrite() {
            return GitlabReleaser.this.isOverwrite();
        }

        @Override
        public Update getUpdate() {
            return GitlabReleaser.this.getUpdate().asImmutable();
        }

        @Override
        public String getApiEndpoint() {
            return GitlabReleaser.this.getApiEndpoint();
        }

        @Override
        public boolean isArtifacts() {
            return GitlabReleaser.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return GitlabReleaser.this.isFiles();
        }

        @Override
        public boolean isChecksums() {
            return GitlabReleaser.this.isChecksums();
        }

        @Override
        public boolean isCatalogs() {
            return GitlabReleaser.this.isCatalogs();
        }

        @Override
        public boolean isSignatures() {
            return GitlabReleaser.this.isSignatures();
        }

        @Override
        public Active getUploadAssets() {
            return GitlabReleaser.this.getUploadAssets();
        }

        @Override
        public org.jreleaser.model.api.common.CommitAuthor getCommitAuthor() {
            return GitlabReleaser.this.getCommitAuthor().asImmutable();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(GitlabReleaser.this.asMap(full));
        }

        @Override
        public boolean isEnabled() {
            return GitlabReleaser.this.isEnabled();
        }

        @Override
        public String getOwner() {
            return GitlabReleaser.this.getOwner();
        }

        @Override
        public Integer getConnectTimeout() {
            return GitlabReleaser.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return GitlabReleaser.this.getReadTimeout();
        }
    };

    public GitlabReleaser() {
        super(org.jreleaser.model.api.release.GitlabReleaser.TYPE, true);
        setHost("gitlab.com");
        setRepoUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}");
        setRepoCloneUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}.git");
        setCommitUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/-/commits");
        setSrcUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/-/blob/{{repoBranch}}");
        setDownloadUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/-/releases/{{tagName}}/downloads/{{artifactFile}}");
        setReleaseNotesUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/-/releases/{{tagName}}");
        setLatestReleaseUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/-/releases/permalink/latest");
        setIssueTrackerUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/-/issues");
    }

    @Override
    public org.jreleaser.model.api.release.GitlabReleaser asImmutable() {
        return immutable;
    }

    @Override
    public void merge(GitlabReleaser source) {
        super.merge(source);
        this.projectIdentifier = merge(this.projectIdentifier, source.projectIdentifier);
        setUploadLinks(merge(this.uploadLinks, source.uploadLinks));
    }

    @Override
    public String getReverseRepoHost() {
        return "com.gitlab";
    }

    @Deprecated
    @JsonPropertyDescription("gitlab.identifier is deprecated since 1.2.0 and will be removed in 2.0.0. Use gitlab.projectIdentifier instead")
    public String getIdentifier() {
        return getProjectIdentifier();
    }

    @Deprecated
    public void setIdentifier(String identifier) {
        nag("gitlab.identifier is deprecated since 1.2.0 and will be removed in 2.0.0. Use gitlab.projectIdentifier instead");
        setProjectIdentifier(identifier);
    }

    public String getProjectIdentifier() {
        return projectIdentifier;
    }

    public void setProjectIdentifier(String projectIdentifier) {
        this.projectIdentifier = projectIdentifier;
    }

    public Map<String, String> getUploadLinks() {
        return uploadLinks;
    }

    public void setUploadLinks(Map<String, String> uploadLinks) {
        this.uploadLinks.clear();
        this.uploadLinks.putAll(uploadLinks);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = super.asMap(full);
        map.put("projectIdentifier", projectIdentifier);
        map.put("uploadLinks", uploadLinks);
        return map;
    }

    @Override
    public TemplateContext props(JReleaserModel model) {
        TemplateContext props = super.props(model);
        props.set(KEY_IDENTIFIER, projectIdentifier);
        props.set(KEY_PROJECT_IDENTIFIER, projectIdentifier);

        return props;
    }
}
