/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Github extends GitService {
    public static final String NAME = "github";

    private Boolean draft;
    private Boolean prerelease;

    public Github() {
        super(NAME, true);
        setHost("github.com");
        setRepoUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}");
        setRepoCloneUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}.git");
        setCommitUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/commits");
        setDownloadUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/releases/download/{{tagName}}/{{artifactFileName}}");
        setReleaseNotesUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/releases/tag/{{tagName}}");
        setLatestReleaseUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/releases/latest");
        setIssueTrackerUrl("https://{{repoHost}}/{{repoOwner}}/{{repoName}}/issues");
    }

    void setAll(Github service) {
        super.setAll(service);
        this.draft = service.draft;
        this.prerelease = service.prerelease;
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

    public boolean isPrerelease() {
        return prerelease != null && prerelease;
    }

    public void setPrerelease(Boolean prerelease) {
        this.prerelease = prerelease;
    }

    public boolean isPrereleaseSet() {
        return prerelease != null;
    }

    @Override
    public String getReverseRepoHost() {
        return "com.github";
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = super.asMap(full);
        map.put("draft", isDraft());
        map.put("prerelease", isPrerelease());
        return map;
    }
}
