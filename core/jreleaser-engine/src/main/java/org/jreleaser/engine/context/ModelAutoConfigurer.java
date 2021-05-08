/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import org.jreleaser.model.Active;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.Codeberg;
import org.jreleaser.model.GitService;
import org.jreleaser.model.Github;
import org.jreleaser.model.Gitlab;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.model.releaser.spi.Repository;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.util.JReleaserLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class ModelAutoConfigurer {
    private final List<String> files = new ArrayList<>();
    private JReleaserLogger logger;
    private Path basedir;
    private Path outputDirectory;
    private boolean dryrun;
    private String projectName;
    private String projectVersion;
    private String tagName;
    private String releaseName;
    private String milestoneName;
    private String branch;
    private boolean prerelease;
    private boolean draft;
    private boolean overwrite;
    private boolean update;
    private boolean skipTag;
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

    public ModelAutoConfigurer projectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public ModelAutoConfigurer projectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
        return this;
    }

    public ModelAutoConfigurer tagName(String tagName) {
        this.tagName = tagName;
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

    public ModelAutoConfigurer skipTag(boolean skipTag) {
        this.skipTag = skipTag;
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
        this.files.addAll(files);
        return this;
    }

    public ModelAutoConfigurer file(String file) {
        if (isNotBlank(file)) {
            this.files.add(file.trim());
        }
        return this;
    }

    public JReleaserContext autoConfigure() {
        requireNonNull(logger, "Argument 'logger' ust not be null");
        requireNonNull(basedir, "Argument 'basedir' ust not be null");
        requireNonNull(outputDirectory, "Argument 'outputDirectory' ust not be null");

        logger.info("JReleaser {}", JReleaserVersion.getPlainVersion());
        logger.info("Auto configure is ON");
        logger.increaseIndent();
        logger.info("- basedir set to {}", basedir.toAbsolutePath());
        dumpAutoConfig();
        logger.decreaseIndent();
        return createAutoConfiguredContext();
    }

    private JReleaserContext createAutoConfiguredContext() {
        return ContextCreator.create(
            logger,
            JReleaserContext.Mode.FULL,
            autoConfiguredModel(basedir),
            basedir,
            outputDirectory,
            dryrun);
    }

    private void dumpAutoConfig() {
        if (isNotBlank(projectName)) logger.info("- project.name: {}", projectName);
        if (isNotBlank(projectVersion)) logger.info("- project.version: {}", projectVersion);
        if (isNotBlank(username)) logger.info("- release.username: {}", username);
        if (isNotBlank(tagName)) logger.info("- release.tagName: {}", tagName);
        if (isNotBlank(branch)) logger.info("- release.branch: {}", branch);
        if (isNotBlank(releaseName)) logger.info("- release.releaseName: {}", releaseName);
        if (isNotBlank(milestoneName)) logger.info("- release.milestone.name: {}", milestoneName);
        if (overwrite) logger.info("- release.overwrite: true");
        if (update) logger.info("- release.update: true");
        if (skipTag) logger.info("- release.skipTag: true");
        if (prerelease) logger.info("- release.prerelease: true");
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
    }

    private JReleaserModel autoConfiguredModel(Path basedir) {
        JReleaserModel model = new JReleaserModel();
        model.getProject().setName(projectName);
        model.getProject().setVersion(projectVersion);

        try {
            Repository repository = GitSdk.of(basedir).getRemote();
            GitService service = null;
            switch (repository.getKind()) {
                case GITHUB:
                    service = new Github();
                    model.getRelease().setGithub((Github) service);
                    ((Github) service).setPrerelease(prerelease);
                    ((Github) service).setDraft(draft);
                    break;
                case GITLAB:
                    service = new Gitlab();
                    model.getRelease().setGitlab((Gitlab) service);
                    break;
                case CODEBERG:
                    service = new Codeberg();
                    model.getRelease().setCodeberg((Codeberg) service);
                    ((Codeberg) service).setPrerelease(prerelease);
                    ((Codeberg) service).setDraft(draft);
                    break;
                default:
                    throw new JReleaserException("Auto configuration does not support " + repository.getHttpUrl());
            }

            service.setUsername(username);
            service.setTagName(tagName);
            service.setReleaseName(releaseName);
            service.getMilestone().setName(milestoneName);
            service.setOverwrite(overwrite);
            service.setUpdate(update);
            service.setSkipTag(skipTag);
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

        return model;
    }

    public static ModelAutoConfigurer builder() {
        return new ModelAutoConfigurer();
    }
}
