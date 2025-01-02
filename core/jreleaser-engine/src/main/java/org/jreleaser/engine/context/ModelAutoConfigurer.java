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
package org.jreleaser.engine.context;

import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.Active;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.model.UpdateSection;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.release.CodebergReleaser;
import org.jreleaser.model.internal.release.GithubReleaser;
import org.jreleaser.model.internal.release.GitlabReleaser;
import org.jreleaser.model.internal.util.Artifacts;
import org.jreleaser.model.spi.release.Repository;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.util.Env;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public final class ModelAutoConfigurer {
    private static final String GLOB_PREFIX = "glob:";
    private static final String REGEX_PREFIX = "regex:";

    private final List<String> authors = new ArrayList<>();
    private final List<String> files = new ArrayList<>();
    private final List<String> globs = new ArrayList<>();
    private final List<String> selectedPlatforms = new ArrayList<>();
    private final List<String> rejectedPlatforms = new ArrayList<>();
    private final Set<UpdateSection> updateSections = new LinkedHashSet<>();
    private JReleaserLogger logger;
    private Path basedir;
    private Path outputDirectory;
    private Boolean dryrun;
    private Boolean gitRootSearch;
    private Boolean strict;
    private String projectName;
    private String projectVersion;
    private String projectVersionPattern;
    private String projectSnapshotPattern;
    private String projectSnapshotLabel;
    private boolean projectSnapshotFullChangelog;
    private String projectCopyright;
    private String projectDescription;
    private String projectInceptionYear;
    private String projectStereotype;
    private String tagName;
    private String previousTagName;
    private String releaseName;
    private String milestoneName;
    private String branch;
    private Boolean prerelease;
    private String prereleasePattern;
    private Boolean draft;
    private boolean overwrite;
    private boolean update;
    private boolean skipTag;
    private boolean skipRelease;
    private boolean skipChecksums;
    private String changelog;
    private boolean changelogFormatted;
    private String username;
    private String commitAuthorName;
    private String commitAuthorEmail;
    private boolean signing;
    private boolean armored;

    private ModelAutoConfigurer() {
        // noop
    }

    public ModelAutoConfigurer logger(JReleaserLogger logger) {
        this.logger = logger;
        return this;
    }

    public ModelAutoConfigurer basedir(Path basedir) {
        this.basedir = basedir;
        return this;
    }

    public ModelAutoConfigurer outputDirectory(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public ModelAutoConfigurer dryrun(Boolean dryrun) {
        this.dryrun = dryrun;
        return this;
    }

    public ModelAutoConfigurer gitRootSearch(Boolean gitRootSearch) {
        this.gitRootSearch = gitRootSearch;
        return this;
    }

    public ModelAutoConfigurer strict(Boolean strict) {
        this.strict = strict;
        return this;
    }

    public ModelAutoConfigurer projectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public ModelAutoConfigurer projectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
        return this;
    }

    public ModelAutoConfigurer projectVersionPattern(String projectVersionPattern) {
        this.projectVersionPattern = projectVersionPattern;
        return this;
    }

    public ModelAutoConfigurer projectSnapshotPattern(String projectSnapshotPattern) {
        this.projectSnapshotPattern = projectSnapshotPattern;
        return this;
    }

    public ModelAutoConfigurer projectSnapshotLabel(String projectSnapshotLabel) {
        this.projectSnapshotLabel = projectSnapshotLabel;
        return this;
    }

    public ModelAutoConfigurer projectSnapshotFullChangelog(boolean projectSnapshotFullChangelog) {
        this.projectSnapshotFullChangelog = projectSnapshotFullChangelog;
        return this;
    }

    public ModelAutoConfigurer projectCopyright(String projectCopyright) {
        this.projectCopyright = projectCopyright;
        return this;
    }

    public ModelAutoConfigurer projectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
        return this;
    }

    public ModelAutoConfigurer projectInceptionYear(String projectInceptionYear) {
        this.projectInceptionYear = projectInceptionYear;
        return this;
    }

    public ModelAutoConfigurer projectStereotype(String projectStereotype) {
        this.projectStereotype = projectStereotype;
        return this;
    }

    public ModelAutoConfigurer tagName(String tagName) {
        this.tagName = tagName;
        return this;
    }

    public ModelAutoConfigurer previousTagName(String previousTagName) {
        this.previousTagName = previousTagName;
        return this;
    }

    public ModelAutoConfigurer branch(String branch) {
        this.branch = branch;
        return this;
    }

    public ModelAutoConfigurer releaseName(String releaseName) {
        this.releaseName = releaseName;
        return this;
    }

    public ModelAutoConfigurer milestoneName(String milestoneName) {
        this.milestoneName = milestoneName;
        return this;
    }

    public ModelAutoConfigurer prerelease(Boolean prerelease) {
        this.prerelease = prerelease;
        return this;
    }

    public ModelAutoConfigurer prereleasePattern(String prereleasePattern) {
        this.prereleasePattern = prereleasePattern;
        return this;
    }

    public ModelAutoConfigurer draft(Boolean draft) {
        this.draft = draft;
        return this;
    }

    public ModelAutoConfigurer overwrite(boolean overwrite) {
        this.overwrite = overwrite;
        return this;
    }

    public ModelAutoConfigurer update(boolean update) {
        this.update = update;
        return this;
    }

    public ModelAutoConfigurer updateSections(Set<UpdateSection> updateSections) {
        this.updateSections.clear();
        if (null != updateSections && !updateSections.isEmpty()) {
            updateSections.forEach(this::updateSection);
        }
        return this;
    }

    public ModelAutoConfigurer updateSection(UpdateSection updateSection) {
        if (null != updateSection) {
            this.updateSections.add(updateSection);
        }
        return this;
    }

    public ModelAutoConfigurer skipTag(boolean skipTag) {
        this.skipTag = skipTag;
        return this;
    }

    public ModelAutoConfigurer skipRelease(boolean skipRelease) {
        this.skipRelease = skipRelease;
        return this;
    }

    public ModelAutoConfigurer skipChecksums(boolean skipChecksums) {
        this.skipChecksums = skipChecksums;
        return this;
    }

    public ModelAutoConfigurer changelog(String changelog) {
        this.changelog = changelog;
        return this;
    }

    public ModelAutoConfigurer changelogFormatted(boolean changelogFormatted) {
        this.changelogFormatted = changelogFormatted;
        return this;
    }

    public ModelAutoConfigurer username(String username) {
        this.username = username;
        return this;
    }

    public ModelAutoConfigurer commitAuthorName(String commitAuthorName) {
        this.commitAuthorName = commitAuthorName;
        return this;
    }

    public ModelAutoConfigurer commitAuthorEmail(String commitAuthorEmail) {
        this.commitAuthorEmail = commitAuthorEmail;
        return this;
    }

    public ModelAutoConfigurer signing(boolean signing) {
        this.signing = signing;
        return this;
    }

    public ModelAutoConfigurer armored(boolean armored) {
        this.armored = armored;
        return this;
    }

    public ModelAutoConfigurer authors(List<String> authors) {
        this.authors.clear();
        if (null != authors && !authors.isEmpty()) {
            authors.forEach(this::file);
        }
        return this;
    }

    public ModelAutoConfigurer author(String author) {
        if (isNotBlank(author)) {
            this.authors.add(author.trim());
        }
        return this;
    }

    public ModelAutoConfigurer files(List<String> files) {
        this.files.clear();
        if (null != files && !files.isEmpty()) {
            files.forEach(this::file);
        }
        return this;
    }

    public ModelAutoConfigurer file(String file) {
        if (isNotBlank(file)) {
            this.files.add(file.trim());
        }
        return this;
    }

    public ModelAutoConfigurer globs(List<String> globs) {
        this.globs.clear();
        if (null != globs && !globs.isEmpty()) {
            globs.forEach(this::glob);
        }
        return this;
    }

    public ModelAutoConfigurer glob(String glob) {
        if (isNotBlank(glob)) {
            if (glob.startsWith(GLOB_PREFIX) || glob.startsWith(REGEX_PREFIX)) {
                this.globs.add(glob.trim());
            } else {
                this.globs.add(GLOB_PREFIX + glob.trim());
            }
        }
        return this;
    }

    public ModelAutoConfigurer selectedPlatforms(List<String> platforms) {
        this.selectedPlatforms.clear();
        if (null != platforms && !platforms.isEmpty()) {
            platforms.forEach(this::selectedPlatform);
        }
        return this;
    }

    public ModelAutoConfigurer selectedPlatform(String platform) {
        if (isNotBlank(platform)) {
            this.selectedPlatforms.add(platform.trim());
        }
        return this;
    }

    public ModelAutoConfigurer rejectedPlatforms(List<String> platforms) {
        this.rejectedPlatforms.clear();
        if (null != platforms && !platforms.isEmpty()) {
            platforms.forEach(this::rejectedPlatform);
        }
        return this;
    }

    public ModelAutoConfigurer rejectedPlatform(String platform) {
        if (isNotBlank(platform)) {
            this.rejectedPlatforms.add(platform.trim());
        }
        return this;
    }

    public JReleaserContext autoConfigure() {
        requireNonNull(logger, "Argument 'logger' ust not be null");
        requireNonNull(basedir, "Argument 'basedir' ust not be null");
        requireNonNull(outputDirectory, "Argument 'outputDirectory' ust not be null");

        logger.info("JReleaser {}", JReleaserVersion.getPlainVersion());
        JReleaserVersion.banner(logger.getTracer());
        logger.info(RB.$("context.configurer.auto-config.on"));
        logger.increaseIndent();
        logger.info(RB.$("context.configurer.basedir.set"), basedir.toAbsolutePath());
        dumpAutoConfig();
        logger.decreaseIndent();
        return createAutoConfiguredContext();
    }

    private JReleaserContext createAutoConfiguredContext() {
        return ContextCreator.create(
            logger,
            JReleaserContext.Configurer.CLI,
            Mode.FULL,
            JReleaserCommand.RELEASE,
            autoConfiguredModel(basedir),
            basedir,
            outputDirectory,
            resolveBoolean(org.jreleaser.model.api.JReleaserContext.DRY_RUN, dryrun),
            resolveBoolean(org.jreleaser.model.api.JReleaserContext.GIT_ROOT_SEARCH, gitRootSearch),
            resolveBoolean(org.jreleaser.model.api.JReleaserContext.STRICT, strict),
            selectedPlatforms,
            rejectedPlatforms);
    }

    private boolean resolveBoolean(String key, Boolean value) {
        if (null != value) return value;
        String resolvedValue = Env.resolve(key, "");
        return isNotBlank(resolvedValue) && Boolean.parseBoolean(resolvedValue);
    }

    private void dumpAutoConfig() {
        if (isNotBlank(projectName)) logger.info("- project.name: {}", projectName);
        if (isNotBlank(projectVersion)) logger.info("- project.version: {}", projectVersion);
        if (isNotBlank(projectVersionPattern)) logger.info("- project.version.pattern: {}", projectVersionPattern);
        if (isNotBlank(projectSnapshotPattern)) logger.info("- project.snapshot.pattern: {}", projectSnapshotPattern);
        if (isNotBlank(projectSnapshotLabel)) logger.info("- project.snapshot.label: {}", projectSnapshotLabel);
        if (projectSnapshotFullChangelog) logger.info("- project.snapshot.full.changelog: true");
        if (isNotBlank(projectDescription)) logger.info("- project.description: {}", projectDescription);
        if (isNotBlank(projectCopyright)) logger.info("- project.copyright: {}", projectCopyright);
        if (isNotBlank(projectInceptionYear)) logger.info("- project.inceptionYear: {}", projectInceptionYear);
        if (isNotBlank(projectStereotype)) logger.info("- project.stereotype: {}", projectStereotype);
        if (!authors.isEmpty()) {
            for (String author : authors) {
                logger.info("- author: {}", author);
            }
        }
        if (isNotBlank(username)) logger.info("- release.username: {}", username);
        if (isNotBlank(tagName)) logger.info("- release.tagName: {}", tagName);
        if (isNotBlank(previousTagName)) logger.info("- release.previousTagName: {}", previousTagName);
        if (isNotBlank(branch)) logger.info("- release.branch: {}", branch);
        if (isNotBlank(releaseName)) logger.info("- release.releaseName: {}", releaseName);
        if (isNotBlank(milestoneName)) logger.info("- release.milestone.name: {}", milestoneName);
        if (overwrite) logger.info("- release.overwrite: true");
        if (update) logger.info("- release.update: true");
        if (!updateSections.isEmpty()) logger.info("- release.updateSections: " + updateSections);
        if (skipTag) logger.info("- release.skipTag: true");
        if (skipRelease) logger.info("- release.skipRelease: true");
        if (skipChecksums) logger.info("- checksums.disabled: true");
        if (null != prerelease && prerelease) logger.info("- release.prerelease: true");
        if (isNotBlank(prereleasePattern)) logger.info("- release.prerelease.pattern: {}", prereleasePattern);
        if (null != draft && draft) logger.info("- release.draft: true");
        if (isNotBlank(changelog)) logger.info(" - release.changelog: {}", changelog);
        if (changelogFormatted) logger.info("- release.changelog.formatted: true");
        if (isNotBlank(commitAuthorName)) logger.info("- release.commitAuthor.name: {}", commitAuthorName);
        if (isNotBlank(commitAuthorEmail)) logger.info("- release.commitAuthor.email: {}", commitAuthorEmail);
        if (signing) logger.info("- signing.enabled: true");
        if (armored) logger.info("- signing.armored: true");
        if (!files.isEmpty()) {
            for (String file : files) {
                logger.info("- file: {}", basedir.relativize(basedir.resolve(file)));
            }
        }
        if (!globs.isEmpty()) {
            for (String glob : globs) {
                logger.info("- glob: {}", glob);
            }
        }
        if (!selectedPlatforms.isEmpty()) {
            for (String platform : selectedPlatforms) {
                logger.info("- platform: {}", platform);
            }
        }
        if (!rejectedPlatforms.isEmpty()) {
            for (String platform : rejectedPlatforms) {
                logger.info("- !platform: {}", platform);
            }
        }
    }

    private JReleaserModel autoConfiguredModel(Path basedir) {
        JReleaserModel model = new JReleaserModel();
        model.getProject().setName(projectName);
        model.getProject().setDescription(projectDescription);
        model.getProject().setCopyright(projectCopyright);
        model.getProject().setStereotype(projectStereotype);
        model.getProject().setInceptionYear(projectInceptionYear);
        model.getProject().setAuthors(authors);
        model.getProject().setVersion(projectVersion);
        model.getProject().setVersionPattern(projectVersionPattern);
        model.getProject().getSnapshot().setPattern(projectSnapshotPattern);
        model.getProject().getSnapshot().setLabel(projectSnapshotLabel);
        model.getProject().getSnapshot().setFullChangelog(projectSnapshotFullChangelog);

        try {
            boolean grs = resolveBoolean(org.jreleaser.model.api.JReleaserContext.GIT_ROOT_SEARCH, gitRootSearch);
            Repository repository = GitSdk.of(basedir, grs).getRemote();
            BaseReleaser<?, ?> service = null;
            switch (repository.getKind()) {
                case GITHUB:
                    service = new GithubReleaser();
                    model.getRelease().setGithub((GithubReleaser) service);
                    service.getPrerelease().setEnabled(prerelease);
                    service.getPrerelease().setPattern(prereleasePattern);
                    ((GithubReleaser) service).setDraft(draft);
                    break;
                case GITLAB:
                    service = new GitlabReleaser();
                    model.getRelease().setGitlab((GitlabReleaser) service);
                    break;
                case CODEBERG:
                    service = new CodebergReleaser();
                    model.getRelease().setCodeberg((CodebergReleaser) service);
                    service.getPrerelease().setEnabled(prerelease);
                    service.getPrerelease().setPattern(prereleasePattern);
                    ((CodebergReleaser) service).setDraft(draft);
                    break;
                default:
                    throw new JReleaserException(RB.$("ERROR_context_configurer_unsupported_url", repository.getHttpUrl()));
            }

            service.setUsername(username);
            service.setTagName(tagName);
            service.setPreviousTagName(previousTagName);
            service.setReleaseName(releaseName);
            service.getMilestone().setName(milestoneName);
            service.setOverwrite(overwrite);
            service.getUpdate().setEnabled(update);
            if (!updateSections.isEmpty()) {
                if (!update) {
                    throw new JReleaserException(RB.$("ERROR_context_configurer_update_not_set"));
                }
                service.getUpdate().setSections(updateSections);
            }
            service.setSkipTag(skipTag);
            service.setSkipRelease(skipRelease);
            if (isNotBlank(branch)) service.setBranch(branch);
            if (isNotBlank(changelog)) service.getChangelog().setExternal(changelog);
            if (isNotBlank(commitAuthorName)) service.getCommitAuthor().setName(commitAuthorName);
            if (isNotBlank(commitAuthorEmail)) service.getCommitAuthor().setEmail(commitAuthorEmail);
            if (changelogFormatted) service.getChangelog().setFormatted(Active.ALWAYS);
        } catch (IOException e) {
            throw new JReleaserException(e.getMessage());
        }

        if (skipChecksums) {
            model.getChecksum().setArtifacts(false);
            model.getChecksum().setFiles(false);
            model.getSigning().setChecksums(false);
        }

        if (signing) {
            model.getSigning().setActive(Active.ALWAYS);
            model.getSigning().setArmored(armored);
        }

        if (!files.isEmpty()) {
            for (String file : files) {
                model.getFiles().addArtifact(Artifact.of(basedir.resolve(file)));
            }
        }

        if (!globs.isEmpty()) {
            model.getFiles().addArtifacts(Artifacts.resolveFiles(logger, basedir, globs));
        }

        return model;
    }

    public static ModelAutoConfigurer builder() {
        return new ModelAutoConfigurer();
    }
}
