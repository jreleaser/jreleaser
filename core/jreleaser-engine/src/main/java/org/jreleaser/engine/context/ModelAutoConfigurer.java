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
package org.jreleaser.engine.context;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.Codeberg;
import org.jreleaser.model.GitService;
import org.jreleaser.model.Github;
import org.jreleaser.model.Gitlab;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.model.UpdateSection;
import org.jreleaser.model.releaser.spi.Repository;
import org.jreleaser.model.util.Artifacts;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.util.JReleaserException;
import org.jreleaser.util.JReleaserLogger;

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
public class ModelAutoConfigurer {
    private static final String GLOB_PREFIX = "glob:";
    private static final String REGEX_PREFIX = "regex:";

    private final List<String> files = new ArrayList<>();
    private final List<String> globs = new ArrayList<>();
    private final List<String> selectedPlatforms = new ArrayList<>();
    private final Set<UpdateSection> updateSections = new LinkedHashSet<>();
    private JReleaserLogger logger;
    private Path basedir;
    private Path outputDirectory;
    private boolean dryrun;
    private boolean gitRootSearch;
    private String projectName;
    private String projectVersion;
    private String projectVersionPattern;
    private String projectSnapshotPattern;
    private String projectSnapshotLabel;
    private boolean projectSnapshotFullChangelog;
    private String tagName;
    private String previousTagName;
    private String releaseName;
    private String milestoneName;
    private String branch;
    private boolean prerelease;
    private String prereleasePattern;
    private boolean draft;
    private boolean overwrite;
    private boolean update;
    private boolean skipTag;
    private boolean skipRelease;
    private String changelog;
    private boolean changelogFormatted;
    private String username;
    private String commitAuthorName;
    private String commitAuthorEmail;
    private boolean signing;
    private boolean armored;

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

    public ModelAutoConfigurer dryrun(boolean dryrun) {
        this.dryrun = dryrun;
        return this;
    }

    public ModelAutoConfigurer gitRootSearch(boolean gitRootSearch) {
        this.gitRootSearch = gitRootSearch;
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

    public ModelAutoConfigurer prerelease(boolean prerelease) {
        this.prerelease = prerelease;
        return this;
    }

    public ModelAutoConfigurer prereleasePattern(String prereleasePattern) {
        this.prereleasePattern = prereleasePattern;
        return this;
    }

    public ModelAutoConfigurer draft(boolean draft) {
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
            JReleaserContext.Mode.FULL,
            autoConfiguredModel(basedir),
            basedir,
            outputDirectory,
            dryrun,
            gitRootSearch,
            selectedPlatforms);
    }

    private void dumpAutoConfig() {
        if (isNotBlank(projectName)) logger.info("- project.name: {}", projectName);
        if (isNotBlank(projectVersion)) logger.info("- project.version: {}", projectVersion);
        if (isNotBlank(projectVersionPattern)) logger.info("- project.version.pattern: {}", projectVersionPattern);
        if (isNotBlank(projectSnapshotPattern)) logger.info("- project.snapshot.pattern: {}", projectSnapshotPattern);
        if (isNotBlank(projectSnapshotLabel)) logger.info("- project.snapshot.label: {}", projectSnapshotLabel);
        if (projectSnapshotFullChangelog) logger.info("- project.snapshot.full.changelog: true");
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
        if (prerelease) logger.info("- release.prerelease: true");
        if (isNotBlank(prereleasePattern)) logger.info("- release.prerelease.pattern: {}", prereleasePattern);
        if (draft) logger.info("- release.draft: true");
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
    }

    private JReleaserModel autoConfiguredModel(Path basedir) {
        JReleaserModel model = new JReleaserModel();
        model.getProject().setName(projectName);
        model.getProject().setVersion(projectVersion);
        model.getProject().setVersionPattern(projectVersionPattern);
        model.getProject().getSnapshot().setPattern(projectSnapshotPattern);
        model.getProject().getSnapshot().setLabel(projectSnapshotLabel);
        model.getProject().getSnapshot().setFullChangelog(projectSnapshotFullChangelog);

        try {
            Repository repository = GitSdk.of(basedir, gitRootSearch).getRemote();
            GitService service = null;
            switch (repository.getKind()) {
                case GITHUB:
                    service = new Github();
                    model.getRelease().setGithub((Github) service);
                    if (prerelease) ((Github) service).getPrerelease().setEnabled(true);
                    ((Github) service).getPrerelease().setPattern(prereleasePattern);
                    ((Github) service).setDraft(draft);
                    break;
                case GITLAB:
                    service = new Gitlab();
                    model.getRelease().setGitlab((Gitlab) service);
                    break;
                case CODEBERG:
                    service = new Codeberg();
                    model.getRelease().setCodeberg((Codeberg) service);
                    if (prerelease) ((Codeberg) service).getPrerelease().setEnabled(true);
                    ((Codeberg) service).getPrerelease().setPattern(prereleasePattern);
                    ((Codeberg) service).setDraft(draft);
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
            service.setUpdate(update);
            if (!updateSections.isEmpty()) {
                if (!service.isUpdate()) {
                    throw new JReleaserException(RB.$("ERROR_context_configurer_update_not_set"));
                }
                service.setUpdateSections(updateSections);
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
