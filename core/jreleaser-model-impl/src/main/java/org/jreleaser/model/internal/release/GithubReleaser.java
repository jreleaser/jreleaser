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
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.EnabledAware;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class GithubReleaser extends BaseReleaser<org.jreleaser.model.api.release.GithubReleaser, GithubReleaser> {
    private static final long serialVersionUID = -7946819245345601709L;
    private final ReleaseNotes releaseNotes = new ReleaseNotes();

    private Boolean draft;
    private String discussionCategoryName;

    private final org.jreleaser.model.api.release.GithubReleaser immutable = new org.jreleaser.model.api.release.GithubReleaser() {
        private static final long serialVersionUID = -4917130438022026501L;

        @Override
        public boolean isPrerelease() {
            return GithubReleaser.this.isPrerelease();
        }

        @Override
        public boolean isDraft() {
            return GithubReleaser.this.isDraft();
        }

        @Override
        public String getDiscussionCategoryName() {
            return discussionCategoryName;
        }

        @Override
        public ReleaseNotes getReleaseNotes() {
            return releaseNotes.asImmutable();
        }

        @Override
        public String getServiceName() {
            return GithubReleaser.this.getServiceName();
        }

        @Override
        public boolean isReleaseSupported() {
            return GithubReleaser.this.isReleaseSupported();
        }

        @Override
        public String getCanonicalRepoName() {
            return GithubReleaser.this.getCanonicalRepoName();
        }

        @Override
        public String getReverseRepoHost() {
            return GithubReleaser.this.getReverseRepoHost();
        }

        @Override
        public boolean isMatch() {
            return GithubReleaser.this.isMatch();
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
            return GithubReleaser.this.isSign();
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
            return GithubReleaser.this.isSkipTag();
        }

        @Override
        public boolean isSkipRelease() {
            return GithubReleaser.this.isSkipRelease();
        }

        @Override
        public boolean isOverwrite() {
            return GithubReleaser.this.isOverwrite();
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
            return GithubReleaser.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return GithubReleaser.this.isFiles();
        }

        @Override
        public boolean isChecksums() {
            return GithubReleaser.this.isChecksums();
        }

        @Override
        public boolean isSignatures() {
            return GithubReleaser.this.isSignatures();
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
            return unmodifiableMap(GithubReleaser.this.asMap(full));
        }

        @Override
        public boolean isEnabled() {
            return GithubReleaser.this.isEnabled();
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

    public GithubReleaser() {
        super(org.jreleaser.model.api.release.GithubReleaser.TYPE, true);
        setHost("github.com");
        setApiEndpoint("https://api.github.com");
        setRepoUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}");
        setRepoCloneUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}.git");
        setCommitUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/commits");
        setSrcUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/blob/{{repoBranch}}");
        setDownloadUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/releases/download/{{tagName}}/{{artifactFile}}");
        setReleaseNotesUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/releases/tag/{{tagName}}");
        setLatestReleaseUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/releases/latest");
        setIssueTrackerUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/issues");
    }

    @Override
    public org.jreleaser.model.api.release.GithubReleaser asImmutable() {
        return immutable;
    }

    @Override
    public void merge(GithubReleaser source) {
        super.merge(source);
        this.draft = merge(this.draft, source.draft);
        this.discussionCategoryName = merge(this.discussionCategoryName, source.discussionCategoryName);
        setReleaseNotes(source.releaseNotes);
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

    public String getDiscussionCategoryName() {
        return discussionCategoryName;
    }

    public void setDiscussionCategoryName(String discussionCategoryName) {
        this.discussionCategoryName = discussionCategoryName;
    }

    public ReleaseNotes getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(ReleaseNotes releaseNotes) {
        this.releaseNotes.merge(releaseNotes);
    }

    @Override
    public String getReverseRepoHost() {
        return "com.github";
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = super.asMap(full);
        map.put("draft", isDraft());
        map.put("discussionCategoryName", discussionCategoryName);
        map.put("releaseNotes", releaseNotes.asMap(full));
        return map;
    }

    public static final class ReleaseNotes extends AbstractModelObject<ReleaseNotes> implements Domain, EnabledAware {
        private static final long serialVersionUID = -1029998017479730113L;

        private Boolean enabled;
        private String configurationFile;

        private final org.jreleaser.model.api.release.GithubReleaser.ReleaseNotes immutable = new org.jreleaser.model.api.release.GithubReleaser.ReleaseNotes() {
            private static final long serialVersionUID = -301461478911447433L;

            @Override
            public String getConfigurationFile() {
                return configurationFile;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(ReleaseNotes.this.asMap(full));
            }

            @Override
            public boolean isEnabled() {
                return ReleaseNotes.this.isEnabled();
            }
        };

        public org.jreleaser.model.api.release.GithubReleaser.ReleaseNotes asImmutable() {
            return immutable;
        }

        @Override
        public void merge(ReleaseNotes source) {
            this.enabled = merge(this.enabled, source.enabled);
            this.configurationFile = merge(this.configurationFile, source.configurationFile);
        }

        @Override
        public boolean isEnabled() {
            return enabled != null && enabled;
        }

        @Override
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean isEnabledSet() {
            return enabled != null;
        }

        public String getConfigurationFile() {
            return configurationFile;
        }

        public void setConfigurationFile(String configurationFile) {
            this.configurationFile = configurationFile;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("enabled", isEnabled());
            map.put("configurationFile", configurationFile);
            return map;
        }
    }
}
