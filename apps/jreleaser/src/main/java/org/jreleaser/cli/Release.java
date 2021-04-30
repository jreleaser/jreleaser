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
package org.jreleaser.cli;

import org.jreleaser.engine.context.ContextCreator;
import org.jreleaser.model.Active;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.GitService;
import org.jreleaser.model.Github;
import org.jreleaser.model.Gitlab;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.model.releaser.spi.Repository;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.workflow.Workflows;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "release",
    mixinStandardHelpOptions = true,
    description = "Create or update a release..")
public class Release extends AbstractModelCommand {
    @CommandLine.Option(names = {"-y", "--dryrun"},
        description = "Skips remote operations.")
    boolean dryrun;

    @CommandLine.Option(names = {"--auto-config"},
        description = "Model auto configuration..")
    boolean autoConfig;

    @CommandLine.Option(names = {"--project-name"},
        description = "The projects name.")
    String projectName;

    @CommandLine.Option(names = {"--project-version"},
        description = "The projects version.")
    String projectVersion;

    @CommandLine.Option(names = {"--tag-name"},
        description = "The release tag.")
    String tagName;

    @CommandLine.Option(names = {"--release-name"},
        description = "The release name.")
    String releaseName;

    @CommandLine.Option(names = {"--milestone-name"},
        description = "The milestone name.")
    String milestoneName;

    @CommandLine.Option(names = {"--prerelease"},
        description = "If the release is a prerelease.")
    boolean prerelease;

    @CommandLine.Option(names = {"--draft"},
        description = "If the release is a draft.")
    boolean draft;

    @CommandLine.Option(names = {"--overwrite"},
        description = "Overwrite an existing release.")
    boolean overwrite;

    @CommandLine.Option(names = {"--update"},
        description = "Update an existing release.")
    boolean update;

    @CommandLine.Option(names = {"--skip-tag"},
        description = "Skip tagging the release.")
    boolean skipTag;

    @CommandLine.Option(names = {"--changelog"},
        description = "Path to changelog file.")
    String changelog;

    @CommandLine.Option(names = {"--changelog-formatted"},
        description = "Format generated changelog.")
    boolean changelogFormatted;

    @CommandLine.Option(names = {"--username"},
        description = "Git username.")
    String username;

    @CommandLine.Option(names = {"--commit-author-name"},
        description = "Commit author name.")
    String commitAuthorName;

    @CommandLine.Option(names = {"--commit-author-email"},
        description = "Commit author e-mail.")
    String commitAuthorEmail;

    @CommandLine.Option(names = {"--signing-enabled"},
        description = "Sign files.")
    boolean signing;

    @CommandLine.Option(names = {"--signing-armored"},
        description = "Generate ascii armored signatures.")
    boolean armored;

    @CommandLine.Option(names = {"--file"},
        description = "Input file(s) to be uploaded.")
    String[] files;

    protected void execute() {
        if (!autoConfig) {
            super.execute();
            return;
        }

        basedir();
        initLogger();
        logger.info("JReleaser {}", JReleaserVersion.getPlainVersion());
        logger.info("Auto configure is ON");
        logger.increaseIndent();
        logger.info("- basedir set to {}", actualBasedir.toAbsolutePath());
        dumpAutoConfig();
        logger.decreaseIndent();
        doExecute(createAutoConfiguredContext());
    }

    private void basedir() {
        actualBasedir = null != basedir ? basedir : Paths.get(".").normalize();
        if (!Files.exists(actualBasedir)) {
            throw halt("Missing required option: '--basedir=<basedir>'");
        }
    }

    private JReleaserContext createAutoConfiguredContext() {
        return ContextCreator.create(
            logger,
            JReleaserContext.Mode.FULL,
            autoConfiguredModel(),
            actualBasedir,
            getOutputDirectory(),
            dryrun());
    }

    private void dumpAutoConfig() {
        if (isNotBlank(projectName)) logger.info("- project.name: {}", projectName);
        if (isNotBlank(projectVersion)) logger.info("- project.version: {}", projectVersion);
        if (isNotBlank(username)) logger.info("- release.username: {}", username);
        if (isNotBlank(tagName)) logger.info("- release.tagName: {}", tagName);
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
        if (files != null && files.length > 0) {
            for (String file : files) {
                logger.info("- file: {}", actualBasedir.relativize(actualBasedir.resolve(file)));
            }
        }
    }

    private JReleaserModel autoConfiguredModel() {
        JReleaserModel model = new JReleaserModel();
        model.getProject().setName(projectName);
        model.getProject().setVersion(projectVersion);

        try {
            Repository repository = GitSdk.of(actualBasedir).getRemote();
            GitService service = null;
            switch (repository.getKind()) {
                case GITHUB:
                    service = new Github();
                    ((Github) service).setPrerelease(prerelease);
                    ((Github) service).setDraft(draft);
                    break;
                case GITLAB:
                    service = new Gitlab();
                    break;
                default:
                    throw halt("Auto configuration does not support " + repository.getHttpUrl());
            }

            service.setUsername(username);
            service.setTagName(tagName);
            service.setReleaseName(releaseName);
            service.getMilestone().setName(milestoneName);
            service.setOverwrite(overwrite);
            service.setUpdate(update);
            service.setSkipTag(skipTag);
            if (isNotBlank(changelog)) service.getChangelog().setExternal(changelog);
            if (isNotBlank(commitAuthorName)) service.getCommitAuthor().setName(commitAuthorName);
            if (isNotBlank(commitAuthorEmail)) service.getCommitAuthor().setEmail(commitAuthorEmail);
            if (changelogFormatted) service.getChangelog().setFormatted(Active.ALWAYS);
        } catch (IOException e) {
            throw halt(e.getMessage());
        }

        if (signing) {
            model.getSigning().setActive(Active.ALWAYS);
            model.getSigning().setArmored(armored);
        }

        if (files != null && files.length > 0) {
            for (String file : files) {
                model.getFiles().addArtifact(Artifact.of(Paths.get(file)));
            }
        }

        return model;
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        Workflows.release(context).execute();
    }

    @Override
    protected boolean dryrun() {
        return dryrun;
    }

    private HaltExecutionException halt(String message) throws HaltExecutionException {
        spec.commandLine().getErr()
            .println(spec.commandLine().getColorScheme().errorText(message));
        spec.commandLine().usage(parent.out);
        throw new HaltExecutionException();
    }
}
