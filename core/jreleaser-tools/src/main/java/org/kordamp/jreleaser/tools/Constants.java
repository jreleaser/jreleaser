/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Andres Almiray.
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
package org.kordamp.jreleaser.tools;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface Constants {
    // General
    String KEY_JAVA_VERSION = "javaVersion";

    // Project
    String KEY_PROJECT_NAME = "projectName";
    String KEY_PROJECT_NAME_CAPITALIZED = "projectNameCapitalized";
    String KEY_PROJECT_VERSION = "projectVersion";
    String KEY_PROJECT_DESCRIPTION = "projectDescription";
    String KEY_PROJECT_LONG_DESCRIPTION = "projectLongDescription";
    String KEY_PROJECT_WEBSITE = "projectWebsite";
    String KEY_PROJECT_LICENSE = "projectLicense";
    String KEY_PROJECT_AUTHORS_BY_SPACE = "projectAuthorsBySpace";
    String KEY_PROJECT_AUTHORS_BY_COMMA = "projectAuthorsByComma";
    String KEY_PROJECT_TAGS_BY_SPACE = "projectTagsBySpace";
    String KEY_PROJECT_TAGS_BY_COMMA = "projectTagsByComma";

    // Release
    String KEY_REPO_HOST = "repoHost";
    String KEY_REPO_OWNER = "repoOwner";
    String KEY_REPO_NAME = "repoName";

    // Distribution
    String KEY_DISTRIBUTION_NAME = "distributionName";
    String KEY_DISTRIBUTION_EXECUTABLE = "distributionExecutable";
    String KEY_DISTRIBUTION_TAGS_BY_SPACE = "distributionTagsBySpace";
    String KEY_DISTRIBUTION_TAGS_BY_COMMA = "distributionTagsByComma";
    String KEY_DISTRIBUTION_RELEASE_NOTES = "distributionReleaseNotes";
    String KEY_DISTRIBUTION_ISSUE_TRACKER = "distributionIssueTracker";
    String KEY_DISTRIBUTION_LATEST_RELEASE = "distributionLatestRelease";
    String KEY_DISTRIBUTION_JAVA_VERSION = "distributionJavaVersion";
    String KEY_DISTRIBUTION_URL = "distributionUrl";
    String KEY_DISTRIBUTION_SHA_256 = "distributionSha256";
    String KEY_DISTRIBUTION_FILE_NAME = "distributionFileName";

    // Artifact
    String KEY_ARTIFACT_FILE_NAME = "artifactFileName";

    // Brew
    String KEY_BREW_DEPENDENCIES = "brewDependencies";

    // Scoop
    String KEY_SCOOP_CHECKVER_URL = "scoopCheckverUrl";
    String KEY_SCOOP_AUTOUPDATE_URL = "scoopAutoupdateUrl";

    // Snap
    String KEY_SNAP_BASE = "snapBase";
    String KEY_SNAP_GRADE = "snapGrade";
    String KEY_SNAP_CONFINEMENT = "snapConfinement";
    String KEY_SNAP_HAS_PLUGS = "snapHasPlugs";
    String KEY_SNAP_PLUGS = "snapPlugs";
    String KEY_SNAP_HAS_SLOTS = "snapHasSlots";
    String KEY_SNAP_SLOTS = "snapSlots";
    String KEY_SNAP_HAS_LOCAL_PLUGS = "snapHasLocalPlugs";
    String KEY_SNAP_LOCAL_PLUGS = "snapLocalPlugs";

    // ToolProcessor
    String KEY_CHECKSUM_DIRECTORY = "__CHECKSUM_DIRECTORY__";
    String KEY_OUTPUT_DIRECTORY = "__OUTPUT_DIRECTORY__";
    String KEY_PREPARE_DIRECTORY = "__PREPARE_DIRECTORY__";
    String KEY_PACKAGE_DIRECTORY = "__PACKAGE_DIRECTORY__";
}
