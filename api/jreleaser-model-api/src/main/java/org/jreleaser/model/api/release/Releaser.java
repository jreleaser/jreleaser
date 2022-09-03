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
package org.jreleaser.model.api.release;

import org.jreleaser.model.Active;
import org.jreleaser.model.UpdateSection;
import org.jreleaser.model.api.common.CommitAuthorAware;
import org.jreleaser.model.api.common.Domain;
import org.jreleaser.model.api.common.EnabledAware;
import org.jreleaser.model.api.common.OwnerAware;
import org.jreleaser.model.api.common.TimeoutAware;

import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface Releaser extends Domain, EnabledAware, CommitAuthorAware, OwnerAware, TimeoutAware, Active.Prereleaseable {
    String KEY_SKIP_RELEASE = "skipRelease";
    String KEY_SKIP_RELEASE_SIGNATURES = "skipReleaseSignatures";
    String TAG_NAME = "TAG_NAME";
    String PREVIOUS_TAG_NAME = "PREVIOUS_TAG_NAME";
    String RELEASE_NAME = "RELEASE_NAME";
    String OVERWRITE = "OVERWRITE";
    String UPDATE = "UPDATE";
    String PRERELEASE = "PRERELEASE";
    String DRAFT = "DRAFT";
    String SKIP_TAG = "SKIP_TAG";
    String SKIP_RELEASE = "SKIP_RELEASE";
    String BRANCH = "BRANCH";
    String PRERELEASE_PATTERN = "PRERELEASE_PATTERN";

    String getServiceName();

    boolean isReleaseSupported();

    String getCanonicalRepoName();

    String getReverseRepoHost();

    boolean isMatch();

    String getHost();

    String getName();

    String getRepoUrl();

    String getRepoCloneUrl();

    String getCommitUrl();

    String getSrcUrl();

    String getDownloadUrl();

    String getReleaseNotesUrl();

    String getLatestReleaseUrl();

    String getIssueTrackerUrl();

    String getUsername();

    String getToken();

    String getTagName();

    String getPreviousTagName();

    String getReleaseName();

    String getBranch();

    Prerelease getPrerelease();

    boolean isSign();

    Changelog getChangelog();

    Milestone getMilestone();

    Issues getIssues();

    boolean isSkipTag();

    boolean isSkipRelease();

    boolean isOverwrite();

    Update getUpdate();

    String getApiEndpoint();

    boolean isArtifacts();

    boolean isFiles();

    boolean isChecksums();

    boolean isSignatures();

    Active getUploadAssets();

    interface Update extends Domain, EnabledAware {
        Set<UpdateSection> getSections();
    }

    interface Prerelease extends Domain, EnabledAware {
        boolean isPrerelease(String version);

        String getPattern();
    }

    interface Milestone extends Domain {
        String MILESTONE_NAME = "MILESTONE_NAME";

        boolean isClose();

        String getName();
    }

    interface Issues extends Domain, EnabledAware {
        String getComment();

        Label getLabel();

        interface Label extends Domain {
            String getName();

            String getColor();

            String getDescription();
        }
    }
}
