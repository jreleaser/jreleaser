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
package org.jreleaser.util;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface Constants {
    String UNSET = "**unset**";
    String HIDE = "************";

    // General
    String KEY_TIMESTAMP = "timestamp";
    String KEY_COMMIT_SHORT_HASH = "commitShortHash";
    String KEY_COMMIT_FULL_HASH = "commitFullHash";

    String KEY_VERSION_MAJOR = "versionMajor";
    String KEY_VERSION_MINOR = "versionMinor";
    String KEY_VERSION_PATCH = "versionPatch";
    String KEY_VERSION_TAG = "versionTag";
    String KEY_VERSION_NUMBER = "versionNumber";
    String KEY_VERSION_PRERELEASE = "versionPrelease";
    String KEY_VERSION_BUILD = "versionBuild";

    // Project
    String KEY_PROJECT_NAME = "projectName";
    String KEY_PROJECT_NAME_CAPITALIZED = "projectNameCapitalized";
    String KEY_PROJECT_VERSION = "projectVersion";
    String KEY_PROJECT_EFFECTIVE_VERSION = "projectEffectiveVersion";
    String KEY_PROJECT_SNAPSHOT = "projectSnapshot";
    String KEY_PROJECT_DESCRIPTION = "projectDescription";
    String KEY_PROJECT_LONG_DESCRIPTION = "projectLongDescription";
    String KEY_PROJECT_WEBSITE = "projectWebsite";
    String KEY_PROJECT_DOCS_URL = "projectDocsUrl";
    String KEY_PROJECT_COPYRIGHT = "projectCopyright";
    String KEY_PROJECT_VENDOR = "projectVendor";
    String KEY_PROJECT_LICENSE = "projectLicense";
    String KEY_PROJECT_AUTHORS_BY_SPACE = "projectAuthorsBySpace";
    String KEY_PROJECT_AUTHORS_BY_COMMA = "projectAuthorsByComma";
    String KEY_PROJECT_TAGS_BY_SPACE = "projectTagsBySpace";
    String KEY_PROJECT_TAGS_BY_COMMA = "projectTagsByComma";
    String KEY_PROJECT_JAVA_VERSION = "projectJavaVersion";
    String KEY_PROJECT_JAVA_VERSION_MAJOR = "projectJavaVersionMajor";
    String KEY_PROJECT_JAVA_VERSION_MINOR = "projectJavaVersionMinor";
    String KEY_PROJECT_JAVA_VERSION_PATCH = "projectJavaVersionPatch";
    String KEY_PROJECT_JAVA_VERSION_TAG = "projectJavaVersionTag";
    String KEY_PROJECT_JAVA_VERSION_BUILD = "projectJavaVersionBuild";
    String KEY_PROJECT_JAVA_GROUP_ID = "projectJavaGroupId";
    String KEY_PROJECT_JAVA_ARTIFACT_ID = "projectJavaArtifactId";
    String KEY_PROJECT_JAVA_MAIN_CLASS = "projectJavaMainClass";

    // Platform
    String KEY_OS_NAME = "osName";
    String KEY_OS_ARCH = "osArch";
    String KEY_OS_PLATFORM = "osPlatform";
    String KEY_OS_VERSION = "osVersion";

    // Release
    String KEY_REPO_HOST = "repoHost";
    String KEY_REPO_OWNER = "repoOwner";
    String KEY_REPO_NAME = "repoName";
    String KEY_IDENTIFIER = "identifier";
    String KEY_REPO_BRANCH = "repoBranch";
    String KEY_TAG_NAME = "tagName";
    String KEY_RELEASE_NAME = "releaseName";
    String KEY_MILESTONE_NAME = "milestoneName";
    String KEY_CANONICAL_REPO_NAME = "repoCanonicalName";
    String KEY_REPO_URL = "repoUrl";
    String KEY_REPO_CLONE_URL = "repoCloneUrl";
    String KEY_COMMIT_URL = "commitsUrl";
    String KEY_RELEASE_NOTES_URL = "releaseNotesUrl";
    String KEY_LATEST_RELEASE_URL = "latestReleaseUrl";
    String KEY_ISSUE_TRACKER_URL = "issueTrackerUrl";
    String KEY_REVERSE_REPO_HOST = "reverseRepoHost";
    String KEY_REVERSE_DOMAIN = "reverseDomain";
    String KEY_CHANGELOG = "changelog";
    String KEY_CHANGELOG_CHANGES = "changelogChanges";
    String KEY_CHANGELOG_CONTRIBUTORS = "changelogContributors";

    // Distribution
    String KEY_DISTRIBUTION_NAME = "distributionName";
    String KEY_DISTRIBUTION_EXECUTABLE = "distributionExecutable";
    String KEY_DISTRIBUTION_TAGS_BY_SPACE = "distributionTagsBySpace";
    String KEY_DISTRIBUTION_TAGS_BY_COMMA = "distributionTagsByComma";
    String KEY_DISTRIBUTION_URL = "distributionUrl";
    String KEY_DISTRIBUTION_SHA_256 = "distributionSha256";
    String KEY_DISTRIBUTION_ARTIFACT_NAME = "distributionArtifactName";
    String KEY_DISTRIBUTION_ARTIFACT_FILE_NAME = "distributionArtifactFileName";
    String KEY_DISTRIBUTION_JAVA_VERSION = "distributionJavaVersion";
    String KEY_DISTRIBUTION_JAVA_VERSION_MAJOR = "distributionJavaVersionMajor";
    String KEY_DISTRIBUTION_JAVA_VERSION_MINOR = "distributionJavaVersionMinor";
    String KEY_DISTRIBUTION_JAVA_VERSION_PATCH = "distributionJavaVersionPatch";
    String KEY_DISTRIBUTION_JAVA_VERSION_TAG = "distributionJavaVersionTag";
    String KEY_DISTRIBUTION_JAVA_VERSION_BUILD = "distributionJavaVersionBuild";
    String KEY_DISTRIBUTION_JAVA_GROUP_ID = "distributionJavaGroupId";
    String KEY_DISTRIBUTION_JAVA_ARTIFACT_ID = "distributionJavaArtifactId";
    String KEY_DISTRIBUTION_JAVA_MAIN_CLASS = "distributionJavaMainClass";

    // Artifact
    String KEY_ARTIFACT_PLATFORM = "artifactPlatform";
    String KEY_ARTIFACT_FILE_NAME = "artifactFileName";
    String KEY_ARTIFACT_NAME = "artifactName";

    // Brew
    String KEY_BREW_FORMULA_NAME = "brewFormulaName";
    String KEY_BREW_DEPENDENCIES = "brewDependencies";
    String KEY_HOMEBREW_TAP_REPO_URL = "brewTapRepoUrl";
    String KEY_HOMEBREW_TAP_REPO_CLONE_URL = "brewTapRepoCloneUrl";
    String KEY_BREW_HAS_LIVECHECK = "brewHasLivecheck";
    String KEY_BREW_LIVECHECK = "brewLivecheck";
    String KEY_BREW_CASK_NAME = "brewCaskName";
    String KEY_BREW_CASK_DISPLAY_NAME = "brewCaskDisplayName";
    String KEY_BREW_CASK_HAS_PKG = "brewCaskHasPkg";
    String KEY_BREW_CASK_PKG = "brewCaskPkg";
    String KEY_BREW_CASK_HAS_APP = "brewCaskHasApp";
    String KEY_BREW_CASK_APP = "brewCaskApp";
    String KEY_BREW_CASK_HAS_UNINSTALL = "brewCaskHasUninstall";
    String KEY_BREW_CASK_UNINSTALL = "brewCaskUninstall";
    String KEY_BREW_CASK_HAS_ZAP = "brewCaskHasZap";
    String KEY_BREW_CASK_ZAP = "brewCaskZap";

    // Docker
    String KEY_DOCKER_SPEC_NAME = "dockerSpecName";
    String KEY_DOCKER_BASE_IMAGE = "dockerBaseImage";
    String KEY_DOCKER_LABELS = "dockerLabels";
    String KEY_DOCKER_PRE_COMMANDS = "dockerPreCommands";
    String KEY_DOCKER_POST_COMMANDS = "dockerPostCommands";

    // Scoop
    String KEY_SCOOP_CHECKVER_URL = "scoopCheckverUrl";
    String KEY_SCOOP_AUTOUPDATE_URL = "scoopAutoupdateUrl";
    String KEY_SCOOP_BUCKET_REPO_URL = "scoopBucketRepoUrl";
    String KEY_SCOOP_BUCKET_REPO_CLONE_URL = "scoopBucketRepoCloneUrl";

    // Chocolatey
    String KEY_CHOCOLATEY_USERNAME = "chocolateyUsername";
    String KEY_CHOCOLATEY_BUCKET_REPO_URL = "chocolateyBucketRepoUrl";
    String KEY_CHOCOLATEY_BUCKET_REPO_CLONE_URL = "chocolateyBucketRepoCloneUrl";

    // Jbang
    String KEY_JBANG_ALIAS_NAME = "jbangAliasName";
    String KEY_JBANG_SCRIPT_NAME = "jbangScriptName";
    String KEY_JBANG_DISTRIBUTION_GA = "jbangDistributionGA";
    String KEY_JBANG_CATALOG_REPO_URL = "jbangCatalogRepoUrl";
    String KEY_JBANG_CATALOG_REPO_CLONE_URL = "jbangCatalogRepoCloneUrl";

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
    String KEY_SNAP_HAS_LOCAL_SLOTS = "snapHasLocalSlots";
    String KEY_SNAP_LOCAL_SLOTS = "snapLocalSlots";
    String KEY_SNAP_REPO_URL = "snapRepoUrl";
    String KEY_SNAP_REPO_CLONE_URL = "snapRepoCloneUrl";

    // Directories
    String KEY_OUTPUT_DIRECTORY = "outputDirectory";
    String KEY_CHECKSUMS_DIRECTORY = "checksumDirectory";
    String KEY_SIGNATURES_DIRECTORY = "signaturesDirectory";
    String KEY_ASSEMBLE_DIRECTORY = "assembleDirectory";
    String KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY = "distributionAssembleDirectory";
    String KEY_ARTIFACTS_DIRECTORY = "artifactsDirectory";
    String KEY_PREPARE_DIRECTORY = "prepareDirectory";
    String KEY_PACKAGE_DIRECTORY = "packageDirectory";
    String KEY_DISTRIBUTION_PREPARE_DIRECTORY = "distributionPrepareDirectory";
    String KEY_DISTRIBUTION_PACKAGE_DIRECTORY = "distributionPackageDirectory";
}
