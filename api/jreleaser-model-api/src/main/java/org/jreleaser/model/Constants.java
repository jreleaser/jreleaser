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
package org.jreleaser.model;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface Constants {
    String UNSET = "**unset**";
    String HIDE = "************";

    String MAGIC_SET = "__MAGIC_SET__";
    String SKIP_OPENJDK = "skipOpenjdk";
    String SKIP_LICENSE_FILE = "skipLicenseFile";
    String DEFAULT_GIT_REMOTE = "DEFAULT_GIT_REMOTE";
    String JRELEASER_USER_HOME = "JRELEASER_USER_HOME";
    String XDG_CONFIG_HOME = "XDG_CONFIG_HOME";
    String XDG_CACHE_HOME = "XDG_CACHE_HOME";
    String OPTIONAL = "optional";

    // General
    String KEY_ZONED_DATE_TIME_NOW = "__ZonedDateTime_now__";
    String KEY_TIMESTAMP = "timestamp";
    String KEY_PLATFORM = "platform";
    String KEY_PLATFORM_REPLACED = "platformReplaced";
    String KEY_COMMIT_SHORT_HASH = "commitShortHash";
    String KEY_COMMIT_FULL_HASH = "commitFullHash";

    String KEY_VERSION_MAJOR = "versionMajor";
    String KEY_VERSION_MINOR = "versionMinor";
    String KEY_VERSION_PATCH = "versionPatch";
    String KEY_VERSION_TAG = "versionTag";
    String KEY_VERSION_NUMBER = "versionNumber";
    String KEY_VERSION_PRERELEASE = "versionPrerelease";
    String KEY_VERSION_BUILD = "versionBuild";
    String KEY_VERSION_OPTIONAL = "versionOptional";
    String KEY_VERSION_YEAR = "versionYear";
    String KEY_VERSION_MONTH = "versionMonth";
    String KEY_VERSION_DAY = "versionDay";
    String KEY_VERSION_WEEK = "versionWeek";
    String KEY_VERSION_MICRO = "versionMicro";
    String KEY_VERSION_MODIFIER = "versionModifier";
    String KEY_VERSION_WITH_UNDERSCORES = "versionWithUnderscores";
    String KEY_VERSION_WITH_DASHES = "versionWithDashes";
    String KEY_VERSION_NUMBER_WITH_UNDERSCORES = "versionNumberWithUnderscores";
    String KEY_VERSION_NUMBER_WITH_DASHES = "versionNumberWithDashes";
    String KEY_EFFECTIVE_VERSION_WITH_UNDERSCORES = "effectiveVersionWithUnderscores";
    String KEY_EFFECTIVE_VERSION_WITH_DASHES = "effectiveVersionWithDashes";

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
    String KEY_PROJECT_STEREOTYPE = "projectStereotype";
    String KEY_PROJECT_COPYRIGHT = "projectCopyright";
    String KEY_PROJECT_VENDOR = "projectVendor";
    String KEY_PROJECT_LICENSE = "projectLicense";
    String KEY_PROJECT_INCEPTION_YEAR = "projectInceptionYear";
    String KEY_PROJECT_LICENSE_URL = "projectLicenseUrl";
    String KEY_PROJECT_AUTHORS = "projectAuthors";
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
    String KEY_OS_PLATFORM_REPLACED = "osPlatformReplaced";
    String KEY_OS_VERSION = "osVersion";

    // Release
    String KEY_REPO_HOST = "repoHost";
    String KEY_REPO_OWNER = "repoOwner";
    String KEY_REPO_NAME = "repoName";
    String KEY_IDENTIFIER = "identifier";
    String KEY_PROJECT_IDENTIFIER = "projectIdentifier";
    String KEY_REPO_BRANCH = "repoBranch";
    String KEY_REPO_BRANCH_PUSH = "repoBranchPush";
    String KEY_TAG_NAME = "tagName";
    String KEY_PREVIOUS_TAG_NAME = "previousTagName";
    String KEY_RELEASE_NAME = "releaseName";
    String KEY_MILESTONE_NAME = "milestoneName";
    String KEY_CANONICAL_REPO_NAME = "repoCanonicalName";
    String KEY_REPO_URL = "repoUrl";
    String KEY_REPO_CLONE_URL = "repoCloneUrl";
    String KEY_COMMIT_URL = "commitsUrl";
    String KEY_SRC_URL = "srcUrl";
    String KEY_RELEASE_NOTES_URL = "releaseNotesUrl";
    String KEY_LATEST_RELEASE_URL = "latestReleaseUrl";
    String KEY_ISSUE_TRACKER_URL = "issueTrackerUrl";
    String KEY_REVERSE_REPO_HOST = "reverseRepoHost";
    String KEY_REVERSE_DOMAIN = "reverseDomain";
    String KEY_CHANGELOG = "changelog";
    String KEY_CHANGELOG_CHANGES = "changelogChanges";
    String KEY_CHANGELOG_CONTRIBUTORS = "changelogContributors";
    String KEY_CHANGELOG_CONTENT = "changelogContent";
    String KEY_CHANGELOG_TITLE = "changelogTitle";
    String KEY_CATEGORIZE_SCOPES = "categorizeScopes";

    // Distribution
    String KEY_DISTRIBUTION_NAME = "distributionName";
    String KEY_DISTRIBUTION_EXECUTABLE = "distributionExecutable";
    String KEY_DISTRIBUTION_EXECUTABLE_NAME = "distributionExecutableName";
    String KEY_DISTRIBUTION_EXECUTABLE_UNIX = "distributionExecutableUnix";
    String KEY_DISTRIBUTION_EXECUTABLE_WINDOWS = "distributionExecutableWindows";
    String KEY_DISTRIBUTION_EXECUTABLE_EXTENSION = "distributionExecutableExtension";
    String KEY_DISTRIBUTION_EXECUTABLE_EXTENSION_UNIX = "distributionExecutableExtensionUnix";
    String KEY_DISTRIBUTION_EXECUTABLE_EXTENSION_WINDOWS = "distributionExecutableExtensionWindows";
    String KEY_DISTRIBUTION_TAGS_BY_SPACE = "distributionTagsBySpace";
    String KEY_DISTRIBUTION_TAGS_BY_COMMA = "distributionTagsByComma";
    String KEY_DISTRIBUTION_STEREOTYPE = "distributionStereotype";
    String KEY_DISTRIBUTION_URL = "distributionUrl";
    String KEY_DISTRIBUTION_SIZE = "distributionSize";
    String KEY_DISTRIBUTION_SHA_256 = "distributionSha256";
    String KEY_DISTRIBUTION_ARTIFACT = "distributionArtifact";
    String KEY_DISTRIBUTION_ARTIFACT_PLATFORM = "distributionArtifactPlatform";
    String KEY_DISTRIBUTION_ARTIFACT_PLATFORM_REPLACED = "distributionArtifactPlatformReplaced";
    String KEY_DISTRIBUTION_ARTIFACT_NAME = "distributionArtifactName";
    String KEY_DISTRIBUTION_ARTIFACT_VERSION = "distributionArtifactVersion";
    String KEY_DISTRIBUTION_ARTIFACT_OS = "distributionArtifactOs";
    String KEY_DISTRIBUTION_ARTIFACT_ARCH = "distributionArtifactArch";
    String KEY_DISTRIBUTION_ARTIFACT_SIZE = "distributionArtifactSize";
    String KEY_DISTRIBUTION_ARTIFACT_FILE = "distributionArtifactFile";
    String KEY_DISTRIBUTION_ARTIFACT_FILE_NAME = "distributionArtifactFileName";
    String KEY_DISTRIBUTION_ARTIFACT_ROOT_ENTRY_NAME = "distributionArtifactRootEntryName";
    String KEY_DISTRIBUTION_ARTIFACT_FILE_EXTENSION = "distributionArtifactFileExtension";
    String KEY_DISTRIBUTION_ARTIFACT_FILE_FORMAT = "distributionArtifactFileFormat";
    String KEY_DISTRIBUTION_ARTIFACT_ARCHIVE_FORMAT = "distributionArtifactArchiveFormat";
    String KEY_DISTRIBUTION_JAVA_VERSION = "distributionJavaVersion";
    String KEY_DISTRIBUTION_JAVA_VERSION_MAJOR = "distributionJavaVersionMajor";
    String KEY_DISTRIBUTION_JAVA_VERSION_MINOR = "distributionJavaVersionMinor";
    String KEY_DISTRIBUTION_JAVA_VERSION_PATCH = "distributionJavaVersionPatch";
    String KEY_DISTRIBUTION_JAVA_VERSION_TAG = "distributionJavaVersionTag";
    String KEY_DISTRIBUTION_JAVA_VERSION_BUILD = "distributionJavaVersionBuild";
    String KEY_DISTRIBUTION_JAVA_GROUP_ID = "distributionJavaGroupId";
    String KEY_DISTRIBUTION_JAVA_ARTIFACT_ID = "distributionJavaArtifactId";
    String KEY_DISTRIBUTION_JAVA_MAIN_JAR = "distributionJavaMainJar";
    String KEY_DISTRIBUTION_JAVA_MAIN_CLASS = "distributionJavaMainClass";
    String KEY_DISTRIBUTION_JAVA_MAIN_MODULE = "distributionJavaMainModule";
    @Deprecated
    String KEY_DISTRIBUTION_JAVA_OPTIONS = "distributionJavaOptions";
    String KEY_DISTRIBUTION_JAVA_JVM_OPTIONS_UNIVERSAL = "distributionJavaJvmOptionsUniversal";
    String KEY_DISTRIBUTION_JAVA_JVM_OPTIONS_UNIX = "distributionJavaJvmOptionsUnix";
    String KEY_DISTRIBUTION_JAVA_JVM_OPTIONS_LINUX = "distributionJavaJvmOptionsLinux";
    String KEY_DISTRIBUTION_JAVA_JVM_OPTIONS_OSX = "distributionJavaJvmOptionsOsx";
    String KEY_DISTRIBUTION_JAVA_JVM_OPTIONS_WINDOWS = "distributionJavaJvmOptionsWindows";
    String KEY_DISTRIBUTION_JAVA_ENVIRONMENT_VARIABLES_UNIVERSAL = "distributionJavaEnvironmentVariablesUniversal";
    String KEY_DISTRIBUTION_JAVA_ENVIRONMENT_VARIABLES_UNIX = "distributionJavaEnvironmentVariablesUnix";
    String KEY_DISTRIBUTION_JAVA_ENVIRONMENT_VARIABLES_LINUX = "distributionJavaEnvironmentVariablesLinux";
    String KEY_DISTRIBUTION_JAVA_ENVIRONMENT_VARIABLES_OSX = "distributionJavaEnvironmentVariablesOsx";
    String KEY_DISTRIBUTION_JAVA_ENVIRONMENT_VARIABLES_WINDOWS = "distributionJavaEnvironmentVariablesWindows";

    // Artifact
    String KEY_ARTIFACT_PLATFORM = "artifactPlatform";
    String KEY_ARTIFACT_PLATFORM_REPLACED = "artifactPlatformReplaced";
    String KEY_ARTIFACT_FILE = "artifactFile";
    String KEY_ARTIFACT_FILE_NAME = "artifactFileName";
    String KEY_ARTIFACT_ROOT_ENTRY_NAME = "artifactRootEntryName";
    String KEY_ARTIFACT_FILE_EXTENSION = "artifactFileExtension";
    String KEY_ARTIFACT_FILE_FORMAT = "artifactFileFormat";
    String KEY_ARTIFACT_NAME = "artifactName";
    String KEY_ARTIFACT_VERSION = "artifactVersion";
    String KEY_ARTIFACT_OS = "artifactOs";
    String KEY_ARTIFACT_ARCH = "artifactArch";
    String KEY_ARTIFACT_SIZE = "artifactSize";
    String KEY_ARTIFACT_ARCHIVE_FORMAT = "artifactArchiveFormat";
    String KEY_ARCHIVE_FORMAT = "archiveFormat";

    // AppImage
    @Deprecated
    String KEY_APPIMAGE_REPO_OWNER = "appImageRepoOwner";
    @Deprecated
    String KEY_APPIMAGE_REPO_NAME = "appImageRepoName";
    String KEY_APPIMAGE_REPOSITORY_OWNER = "appImageRepositoryOwner";
    String KEY_APPIMAGE_REPOSITORY_NAME = "appImageRepositoryName";
    String KEY_APPIMAGE_COMPONENT_ID = "appImageComponentId";
    String KEY_APPIMAGE_CATEGORIES = "appImageCategories";
    String KEY_APPIMAGE_CATEGORIES_BY_COMMA = "appImageCategoriesByComma";
    String KEY_APPIMAGE_DEVELOPER_NAME = "appImageDeveloperName";
    String KEY_APPIMAGE_REQUIRES_TERMINAL = "appImageRequiresTerminal";
    String KEY_APPIMAGE_RELEASES = "appImageReleases";
    String KEY_APPIMAGE_URLS = "appImageUrls";
    String KEY_APPIMAGE_SCREENSHOTS = "appImageScreenshots";
    String KEY_APPIMAGE_ICONS = "appImageIcons";
    String KEY_APPIMAGE_DISTRIBUTION_ARTIFACT_FILE = "appImageDistributionArtifactFile";
    String KEY_APPIMAGE_DISTRIBUTION_URL = "appImageDistributionUrl";
    String KEY_APPIMAGE_DISTRIBUTION_ARTIFACT_FILE_NAME = "appImageDistributionArtifactFileName";

    // Asdf
    @Deprecated
    String KEY_ASDF_PLUGIN_REPO_URL = "asdfPluginRepoUrl";
    String KEY_ASDF_PLUGIN_REPOSITORY_URL = "asdfPluginRepositoryUrl";
    String KEY_ASDF_PLUGIN_TOOL_CHECK = "asdfPluginToolCheck";
    String KEY_ASDF_DISTRIBUTION_ARTIFACT_FILE = "asdfDistributionArtifactFile";
    String KEY_ASDF_DISTRIBUTION_URL = "asdfDistributionUrl";
    String KEY_ASDF_DISTRIBUTION_ARTIFACT_FILE_NAME = "asdfDistributionArtifactFileName";
    String KEY_ASDF_DISTRIBUTION_ARTIFACT_ROOT_ENTRY_NAME = "asdfDistributionArtifactRootEntryName";

    // Brew
    String KEY_BREW_FORMULA_NAME = "brewFormulaName";
    String KEY_BREW_DEPENDENCIES = "brewDependencies";
    @Deprecated
    String KEY_HOMEBREW_TAP_REPO_OWNER = "brewTapRepoOwner";
    @Deprecated
    String KEY_HOMEBREW_TAP_REPO_NAME = "brewTapRepoName";
    @Deprecated
    String KEY_HOMEBREW_TAP_NAME = "brewTapName";
    @Deprecated
    String KEY_HOMEBREW_TAP_REPO_URL = "brewTapRepoUrl";
    @Deprecated
    String KEY_HOMEBREW_TAP_REPO_CLONE_URL = "brewTapRepoCloneUrl";
    String KEY_HOMEBREW_REPOSITORY_OWNER = "brewRepositoryOwner";
    String KEY_HOMEBREW_REPOSITORY_NAME = "brewRepositoryName";
    String KEY_HOMEBREW_REPOSITORY_ALIAS = "brewRepositoryAlias";
    String KEY_HOMEBREW_REPOSITORY_URL = "brewRepositoryUrl";
    String KEY_HOMEBREW_REPOSITORY_CLONE_URL = "brewRepositoryCloneUrl";
    String KEY_BREW_HAS_LIVECHECK = "brewHasLivecheck";
    String KEY_BREW_LIVECHECK = "brewLivecheck";
    String KEY_BREW_DOWNLOAD_STRATEGY = "brewDownloadStrategy";
    String KEY_BREW_REQUIRE_RELATIVE = "brewRequireRelative";
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
    String KEY_BREW_CASK_HAS_APPCAST = "brewCaskHasAppcast";
    String KEY_BREW_CASK_APPCAST = "brewCaskAppcast";
    String KEY_BREW_CASK_HAS_BINARY = "brewCaskHasBinary";
    String KEY_BREW_MULTIPLATFORM = "brewMultiPlatform";

    // Docker
    String KEY_DOCKER_SPEC_NAME = "dockerSpecName";
    String KEY_DOCKER_BASE_IMAGE = "dockerBaseImage";
    String KEY_DOCKER_LABELS = "dockerLabels";
    String KEY_DOCKER_PRE_COMMANDS = "dockerPreCommands";
    String KEY_DOCKER_POST_COMMANDS = "dockerPostCommands";

    // Scoop
    String KEY_SCOOP_PACKAGE_NAME = "scoopPackageName";
    String KEY_SCOOP_CHECKVER_URL = "scoopCheckverUrl";
    String KEY_SCOOP_AUTOUPDATE_URL = "scoopAutoupdateUrl";
    String KEY_SCOOP_AUTOUPDATE_EXTRACT_DIR = "scoopAutoupdateExtractDir";
    @Deprecated
    String KEY_SCOOP_BUCKET_REPO_URL = "scoopBucketRepoUrl";
    @Deprecated
    String KEY_SCOOP_BUCKET_REPO_CLONE_URL = "scoopBucketRepoCloneUrl";
    String KEY_SCOOP_REPOSITORY_URL = "scoopRepositoryUrl";
    String KEY_SCOOP_REPOSITORY_CLONE_URL = "scoopRepositoryCloneUrl";

    // Sdkman
    String KEY_SDKMAN_CANDIDATE = "sdkmanCandidate";
    String KEY_SDKMAN_RELEASE_NOTES_URL = "sdkmanReleaseNotesUrl";

    // Chocolatey
    String KEY_CHOCOLATEY_PACKAGE_NAME = "chocolateyPackageName";
    String KEY_CHOCOLATEY_PACKAGE_VERSION = "chocolateyPackageVersion";
    String KEY_CHOCOLATEY_USERNAME = "chocolateyUsername";
    String KEY_CHOCOLATEY_TITLE = "chocolateyTitle";
    String KEY_CHOCOLATEY_ICON_URL = "chocolateyIconUrl";
    String KEY_CHOCOLATEY_SOURCE = "chocolateySource";
    String KEY_CHOCOLATEY_PACKAGE_SOURCE_URL = "chocolateyPackageSourceUrl";
    @Deprecated
    String KEY_CHOCOLATEY_BUCKET_REPO_URL = "chocolateyBucketRepoUrl";
    @Deprecated
    String KEY_CHOCOLATEY_BUCKET_REPO_CLONE_URL = "chocolateyBucketRepoCloneUrl";
    String KEY_CHOCOLATEY_REPOSITORY_URL = "chocolateyRepositoryUrl";
    String KEY_CHOCOLATEY_REPOSITORY_CLONE_URL = "chocolateyRepositoryCloneUrl";

    // Jbang
    String KEY_JBANG_ALIAS_NAME = "jbangAliasName";
    String KEY_JBANG_SCRIPT_NAME = "jbangScriptName";
    String KEY_JBANG_DISTRIBUTION_GA = "jbangDistributionGA";
    @Deprecated
    String KEY_JBANG_CATALOG_REPO_URL = "jbangCatalogRepoUrl";
    @Deprecated
    String KEY_JBANG_CATALOG_REPO_CLONE_URL = "jbangCatalogRepoCloneUrl";
    String KEY_JBANG_REPOSITORY_URL = "jbangRepositoryUrl";
    String KEY_JBANG_REPOSITORY_CLONE_URL = "jbangRepositoryCloneUrl";

    // Jib
    String KEY_JIB_SPEC_NAME = "jibSpecName";
    String KEY_JIB_BASE_IMAGE = "jibBaseImage";
    String KEY_JIB_CREATION_TIME = "jibCreationTime";
    String KEY_JIB_USER = "jibUser";
    String KEY_JIB_FORMAT = "jibFormat";
    String KEY_JIB_WORKING_DIRECTORY = "jibWorkingDirectory";
    String KEY_JIB_HAS_VOLUMES = "jibHasVolumes";
    String KEY_JIB_VOLUMES = "jibVolumes";
    String KEY_JIB_HAS_EXPOSED_PORTS = "jibHasExposedPorts";
    String KEY_JIB_EXPOSED_PORTS = "jibExposedPorts";
    String KEY_JIB_HAS_ENVIRONMENT = "jibHasEnvironment";
    String KEY_JIB_ENVIRONMENT = "jibEnvironment";
    String KEY_JIB_LABELS = "jibLabels";

    // Macports
    String KEY_MACPORTS_APP_NAME = "macportsAppName";
    String KEY_MACPORTS_PACKAGE_NAME = "macportsPackageName";
    String KEY_MACPORTS_REVISION = "macportsRevision";
    String KEY_MACPORTS_CATEGORIES = "macportsCategories";
    String KEY_MACPORTS_MAINTAINERS = "macportsMaintainers";
    String KEY_MACPORTS_DISTRIBUTION_URL = "macportsDistributionUrl";
    String KEY_MACPORTS_DISTNAME = "macportsDistname";
    String KEY_MACPORTS_JAVA_VERSION = "macportsJavaVersion";
    @Deprecated
    String KEY_MACPORTS_REPOSITORY_REPO_URL = "macportsRepositoryRepoUrl";
    @Deprecated
    String KEY_MACPORTS_REPOSITORY_REPO_CLONE_URL = "macportsRepositoryRepoCloneUrl";
    String KEY_MACPORTS_REPOSITORY_URL = "macportsRepositoryUrl";
    String KEY_MACPORTS_REPOSITORY_CLONE_URL = "macportsRepositoryCloneUrl";

    // Snap
    String KEY_SNAP_PACKAGE_NAME = "snapPackageName";
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
    String KEY_SNAP_HAS_ARCHITECTURES = "snapHasArchitectures";
    String KEY_SNAP_ARCHITECTURES = "snapArchitectures";
    @Deprecated
    String KEY_SNAP_REPO_URL = "snapRepoUrl";
    @Deprecated
    String KEY_SNAP_REPO_CLONE_URL = "snapRepoCloneUrl";
    String KEY_SNAP_REPOSITORY_URL = "snapRepositoryUrl";
    String KEY_SNAP_REPOSITORY_CLONE_URL = "snapRepositoryCloneUrl";

    // Spec
    String KEY_SPEC_PACKAGE_NAME = "specPackageName";
    String KEY_SPEC_RELEASE = "specRelease";
    String KEY_SPEC_REQUIRES = "specRequires";
    String KEY_SPEC_DIRECTORIES = "specDirectories";
    String KEY_SPEC_BINARIES = "specBinaries";
    String KEY_SPEC_FILES = "specFiles";
    String KEY_SPEC_REPOSITORY_URL = "specRepositoryUrl";
    String KEY_SPEC_REPOSITORY_CLONE_URL = "specRepositoryCloneUrl";

    // Flatpak
    @Deprecated
    String KEY_FLATPAK_REPO_OWNER = "flatpakRepoOwner";
    @Deprecated
    String KEY_FLATPAK_REPO_NAME = "flatpakRepoName";
    String KEY_FLATPAK_REPOSITORY_OWNER = "flatpakRepositoryOwner";
    String KEY_FLATPAK_REPOSITORY_NAME = "flatpakRepositoryName";
    String KEY_FLATPAK_COMPONENT_ID = "flatpakComponentId";
    String KEY_FLATPAK_CATEGORIES = "flatpakCategories";
    String KEY_FLATPAK_CATEGORIES_BY_COMMA = "flatpakCategoriesByComma";
    String KEY_FLATPAK_CATEGORIES_BY_SEMICOLON = "flatpakCategoriesBySemicolon";
    String KEY_FLATPAK_DEVELOPER_NAME = "flatpakDeveloperName";
    String KEY_FLATPAK_RELEASES = "flatpakReleases";
    String KEY_FLATPAK_URLS = "flatpakUrls";
    String KEY_FLATPAK_SCREENSHOTS = "flatpakScreenshots";
    String KEY_FLATPACK_ICONS = "flatpakIcons";
    String KEY_FLATPAK_DIRECTORIES = "flatpakDirectories";
    String KEY_FLATPAK_BINARIES = "flatpakBinaries";
    String KEY_FLATPAK_FILES = "flatpakFiles";
    String KEY_FLATPAK_HAS_SDK_EXTENSIONS = "flatpakHasSdkExtensions";
    String KEY_FLATPAK_SDK_EXTENSIONS = "flatpakSdkExtensions";
    String KEY_FLATPAK_HAS_SDK_FINISH_ARGS = "flatpakHasFinishArgs";
    String KEY_FLATPAK_SDK_FINISH_ARGS = "flatpakFinishArgs";
    String KEY_FLATPAK_RUNTIME = "flatpakRuntime";
    String KEY_FLATPAK_RUNTIME_VERSION = "flatpakRuntimeVersion";
    String KEY_FLATPAK_SDK = "flatpakSdk";
    String KEY_FLATPAK_INCLUDE_OPENJDK = "flatpakIncludeOpenJdk";

    // Winget
    String KEY_WINGET_PACKAGE_IDENTIFIER = "wingetPackageIdentifier";
    String KEY_WINGET_PACKAGE_NAME = "wingetPackageName";
    String KEY_WINGET_PACKAGE_VERSION = "wingetPackageVersion";
    String KEY_WINGET_PACKAGE_URL = "wingetPackageUrl";
    String KEY_WINGET_PACKAGE_LOCALE = "wingetPackageLocale";
    String KEY_WINGET_PUBLISHER_NAME = "wingetPackagePublisher";
    String KEY_WINGET_PUBLISHER_URL = "wingetPublisherUrl";
    String KEY_WINGET_PUBLISHER_SUPPORT_URL = "wingetPublisherSupportUrl";
    String KEY_WINGET_DEFAULT_LOCALE = "wingetDefaultLocale";
    String KEY_WINGET_AUTHOR = "wingetAuthor";
    String KEY_WINGET_MONIKER = "wingetMoniker";
    String KEY_WINGET_MINIMUM_OS_VERSION = "wingetMinimumOsVersion";
    String KEY_WINGET_PRODUCT_CODE = "wingetProductCode";
    String KEY_WINGET_HAS_TAGS = "wingetHasTags";
    String KEY_WINGET_TAGS = "wingetTags";
    String KEY_WINGET_MANIFEST_TYPE = "wingetManifestType";
    String KEY_WINGET_INSTALLER_TYPE = "wingetInstallerType";
    String KEY_WINGET_SCOPE = "wingetScope";
    String KEY_WINGET_INSTALL_MODES = "wingetInstallModes";
    String KEY_WINGET_UPGRADE_BEHAVIOR = "wingetUpgradeBehavior";
    String KEY_WINGET_RELEASE_DATE = "wingetReleaseDate";
    String KEY_WINGET_INSTALLER_ARCHITECTURE = "wingetInstallerArchitecture";
    String KEY_WINGET_INSTALLERS = "wingetInstallers";
    String KEY_WINGET_HAS_DEPENDENCIES = "wingetHasDependencies";
    String KEY_WINGET_HAS_WINDOWS_FEATURES = "wingetHasWindowsFeatures";
    String KEY_WINGET_WINDOWS_FEATURES = "wingetWindowsFeatures";
    String KEY_WINGET_HAS_WINDOWS_LIBRARIES = "wingetHasWindowsLibraries";
    String KEY_WINGET_WINDOWS_LIBRARIES = "wingetWindowsLibraries";
    String KEY_WINGET_HAS_EXTERNAL_DEPENDENCIES = "wingetHasExternalDependencies";
    String KEY_WINGET_EXTERNAL_DEPENDENCIES = "wingetExternalDependencies";
    String KEY_WINGET_HAS_PACKAGE_DEPENDENCIES = "wingetHasPackageDependencies";
    String KEY_WINGET_PACKAGE_DEPENDENCIES = "wingetPackageDependencies";

    String KEY_DEB_INSTALLATION_PATH = "debInstallationPath";
    String KEY_DEB_ARCHITECTURE = "debControlArchitecture";
    String KEY_DEB_CONTROL_PACKAGE = "debControlPackage";
    String KEY_DEB_CONTROL_VERSION = "debControlVersion";
    String KEY_DEB_CONTROL_REVISION = "debControlRevision";
    String KEY_DEB_CONTROL_MAINTAINER = "debControlMaintainer";
    String KEY_DEB_CONTROL_SECTION = "debControlSection";
    String KEY_DEB_CONTROL_PRIORITY = "debControlPriority";
    String KEY_DEB_CONTROL_PROVIDES = "debControlProvides";
    String KEY_DEB_CONTROL_ARCHITECTURE = "debControlArchitecture";
    String KEY_DEB_CONTROL_ESSENTIAL = "debControlEssential";
    String KEY_DEB_CONTROL_DESCRIPTION = "debControlDescription";
    String KEY_DEB_CONTROL_HOMEPAGE = "debControlHomepage";
    String KEY_DEB_CONTROL_BUILT_USING = "debControlBuiltUsing";
    String KEY_DEB_CONTROL_INSTALLED_SIZE = "debControlInstalledSize";
    String KEY_DEB_CONTROL_HAS_DEPENDS = "debControlHasDepends";
    String KEY_DEB_CONTROL_DEPENDS = "debControlDepends";
    String KEY_DEB_CONTROL_HAS_PRE_DEPENDS = "debControlHasPreDepends";
    String KEY_DEB_CONTROL_PRE_DEPENDS = "debControlPreDepends";
    String KEY_DEB_CONTROL_HAS_RECOMMENDS = "debControlHasPreDepends";
    String KEY_DEB_CONTROL_RECOMMENDS = "debControlPreDepends";
    String KEY_DEB_CONTROL_HAS_SUGGESTS = "debControlHasPreDepends";
    String KEY_DEB_CONTROL_SUGGESTS = "debControlPreDepends";
    String KEY_DEB_CONTROL_HAS_ENHANCES = "debControlHasPreDepends";
    String KEY_DEB_CONTROL_ENHANCES = "debControlPreDepends";
    String KEY_DEB_CONTROL_HAS_BREAKS = "debControlHasPreDepends";
    String KEY_DEB_CONTROL_BREAKS = "debControlPreDepends";
    String KEY_DEB_CONTROL_HAS_CONFLICTS = "debControlHasPreDepends";
    String KEY_DEB_CONTROL_CONFLICTS = "debControlPreDepends";

    // Linkedin
    String KEY_LINKEDIN_OWNER = "linkedinOwner";
    String KEY_LINKEDIN_SUBJECT = "linkedinSubject";

    // Gofish
    String KEY_GOFISH_PACKAGES = "gofishPackages";

    // upload
    String KEY_UPLOADER_NAME = "uploaderName";

    // Download
    String KEY_DOWNLOADER_NAME = "downloaderName";

    // Directories
    String KEY_BASEDIR = "basedir";
    String KEY_COMMAND = "command";
    String KEY_BASE_OUTPUT_DIRECTORY = "baseOutputDirectory";
    String KEY_OUTPUT_DIRECTORY = "outputDirectory";
    String KEY_CHECKSUMS_DIRECTORY = "checksumDirectory";
    String KEY_CATALOGS_DIRECTORY = "catalogsDirectory";
    String KEY_SIGNATURES_DIRECTORY = "signaturesDirectory";
    String KEY_ASSEMBLE_DIRECTORY = "assembleDirectory";
    String KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY = "distributionAssembleDirectory";
    String KEY_ARTIFACTS_DIRECTORY = "artifactsDirectory";
    String KEY_DEPLOY_DIRECTORY = "deployDirectory";
    String KEY_DOWNLOAD_DIRECTORY = "downloadDirectory";
    String KEY_PREPARE_DIRECTORY = "prepareDirectory";
    String KEY_PACKAGE_DIRECTORY = "packageDirectory";
    String KEY_DISTRIBUTION_PREPARE_DIRECTORY = "distributionPrepareDirectory";
    String KEY_DISTRIBUTION_PACKAGE_DIRECTORY = "distributionPackageDirectory";

    String KEY_GRAALVM_NAGIVE_IMAGE = "graalVMNativeImage";

    String SKIP_SBOM = "skipSbom";
    String SKIP_SBOM_CYCLONEDX = "skipSbomCyclonedx";
    String SKIP_SBOM_SYFT = "skipSbomSyft";
    String SKIP_CASK_DISPLAY_NAME_TRANSFORM = "skipCaskDisplayNameTransform";

    // Container Images
    String DOCKER_IO = "docker.io";
    String LABEL_OCI_IMAGE_TITLE = "org.opencontainers.image.title";
    String LABEL_OCI_IMAGE_DESCRIPTION = "org.opencontainers.image.description";
    String LABEL_OCI_IMAGE_REVISION = "org.opencontainers.image.revision";
    String LABEL_OCI_IMAGE_VERSION = "org.opencontainers.image.version";
    String LABEL_OCI_IMAGE_LICENSES = "org.opencontainers.image.licenses";
    String LABEL_OCI_IMAGE_URL = "org.opencontainers.image.url";
}
