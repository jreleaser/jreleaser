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
package org.jreleaser.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Github extends GitService<Github> {
    public static final String NAME = "github";

    private final ReleaseNotes releaseNotes = new ReleaseNotes();

    private Boolean draft;
    private String discussionCategoryName;

    public Github() {
        super(NAME, true);
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
    public void freeze() {
        super.freeze();
        releaseNotes.freeze();
    }

    @Override
    public void merge(Github service) {
        freezeCheck();
        super.merge(service);
        this.draft = merge(this.draft, service.draft);
        this.discussionCategoryName = merge(this.discussionCategoryName, service.discussionCategoryName);
        setReleaseNotes(service.releaseNotes);
    }

    public boolean isDraft() {
        return draft != null && draft;
    }

    public void setDraft(Boolean draft) {
        freezeCheck();
        this.draft = draft;
    }

    public boolean isDraftSet() {
        return draft != null;
    }

    public String getDiscussionCategoryName() {
        return discussionCategoryName;
    }

    public void setDiscussionCategoryName(String discussionCategoryName) {
        freezeCheck();
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

    public static class ReleaseNotes extends AbstractModelObject<ReleaseNotes> implements Domain, EnabledAware {
        private Boolean enabled;
        private String configurationFile;

        @Override
        public void merge(ReleaseNotes source) {
            freezeCheck();
            this.enabled = merge(this.enabled, source.enabled);
            this.configurationFile = merge(this.configurationFile, source.configurationFile);
        }

        @Override
        public boolean isEnabled() {
            return enabled != null && enabled;
        }

        @Override
        public void setEnabled(Boolean enabled) {
            freezeCheck();
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
            freezeCheck();
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
