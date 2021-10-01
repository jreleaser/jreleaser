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
package org.jreleaser.cli;

import org.jreleaser.engine.context.ModelAutoConfigurer;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.UpdateSection;
import org.jreleaser.workflow.Workflows;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "release",
    mixinStandardHelpOptions = true,
    description = "Create or update a release.")
public class Release extends AbstractPlatformAwareModelCommand {
    @CommandLine.Option(names = {"-y", "--dryrun"},
        description = "Skip remote operations.")
    boolean dryrun;

    @CommandLine.Option(names = {"--auto-config"},
        description = "Model auto configuration.")
    boolean autoConfig;

    @CommandLine.Option(names = {"--project-name"},
        description = "The project name.")
    String projectName;

    @CommandLine.Option(names = {"--project-version"},
        description = "The project version.")
    String projectVersion;

    @CommandLine.Option(names = {"--project-version-pattern"},
        description = "The project version pattern.")
    String projectVersionPattern;

    @CommandLine.Option(names = {"--project-snapshot-pattern"},
        description = "The project snapshot pattern.")
    String projectSnapshotPattern;

    @CommandLine.Option(names = {"--project-snapshot-label"},
        description = "The project snapshot label.")
    String projectSnapshotLabel;

    @CommandLine.Option(names = {"--project-snapshot-full-changelog"},
        description = "Calculate full changelog since last non-snapshot release.")
    boolean projectSnapshotFullChangelog;

    @CommandLine.Option(names = {"--tag-name"},
        description = "The release tag.")
    String tagName;

    @CommandLine.Option(names = {"--previous-tag-name"},
        description = "The previous release tag.")
    String previousTagName;

    @CommandLine.Option(names = {"--release-name"},
        description = "The release name.")
    String releaseName;

    @CommandLine.Option(names = {"--milestone-name"},
        description = "The milestone name.")
    String milestoneName;

    @CommandLine.Option(names = {"--prerelease"},
        description = "If the release is a prerelease.")
    boolean prerelease;

    @CommandLine.Option(names = {"--prerelease-pattern"},
        description = "The prerelease pattern.")
    String prereleasePattern;

    @CommandLine.Option(names = {"--draft"},
        description = "If the release is a draft.")
    boolean draft;

    @CommandLine.Option(names = {"--overwrite"},
        description = "Overwrite an existing release.")
    boolean overwrite;

    @CommandLine.Option(names = {"--update"},
        description = "Update an existing release.")
    boolean update;

    @CommandLine.Option(names = {"--update-section"},
        paramLabel = "<section>",
        description = "Release section to be updated. Repeatable.")
    String[] updateSections;

    @CommandLine.Option(names = {"--skip-tag"},
        description = "Skip tagging the release.")
    boolean skipTag;

    @CommandLine.Option(names = {"--skip-release"},
        description = "Skip creating a release.")
    boolean skipRelease;

    @CommandLine.Option(names = {"--branch"},
        description = "The release branch.")
    String branch;

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
        paramLabel = "<file>",
        description = "Input file to be uploaded. Repeatable.")
    String[] files;

    @CommandLine.Option(names = {"--glob"},
        paramLabel = "<file>",
        description = "Input file to be uploaded (as glob). Repeatable.")
    String[] globs;

    protected void execute() {
        if (!autoConfig) {
            super.execute();
            return;
        }

        basedir();
        initLogger();

        JReleaserContext context = ModelAutoConfigurer.builder()
            .logger(logger)
            .basedir(actualBasedir)
            .outputDirectory(getOutputDirectory())
            .dryrun(dryrun())
            .gitRootSearch(gitRootSearch)
            .projectName(projectName)
            .projectVersion(projectVersion)
            .projectVersionPattern(projectVersionPattern)
            .projectSnapshotPattern(projectSnapshotPattern)
            .projectSnapshotLabel(projectSnapshotLabel)
            .projectSnapshotFullChangelog(projectSnapshotFullChangelog)
            .tagName(tagName)
            .previousTagName(previousTagName)
            .releaseName(releaseName)
            .milestoneName(milestoneName)
            .branch(branch)
            .prerelease(prerelease)
            .prereleasePattern(prereleasePattern)
            .draft(draft)
            .overwrite(overwrite)
            .update(update)
            .updateSections(collectUpdateSections())
            .skipTag(skipTag)
            .skipRelease(skipRelease)
            .changelog(changelog)
            .changelogFormatted(changelogFormatted)
            .username(username)
            .commitAuthorName(commitAuthorName)
            .commitAuthorEmail(commitAuthorEmail)
            .signing(signing)
            .armored(armored)
            .files(collectFiles())
            .globs(collectGlobs())
            .selectedPlatforms(collectSelectedPlatforms())
            .autoConfigure();

        doExecute(context);
    }

    private List<String> collectFiles() {
        List<String> list = new ArrayList<>();
        if (files != null && files.length > 0) {
            Collections.addAll(list, files);
        }
        return list;
    }

    private List<String> collectGlobs() {
        List<String> list = new ArrayList<>();
        if (globs != null && globs.length > 0) {
            Collections.addAll(list, globs);
        }
        return list;
    }

    private Set<UpdateSection> collectUpdateSections() {
        Set<UpdateSection> set = new LinkedHashSet<>();
        if (updateSections != null && updateSections.length > 0) {
            for (String updateSection : updateSections) {
                set.add(UpdateSection.of(updateSection.trim()));
            }
        }
        return set;
    }

    private void basedir() {
        actualBasedir = null != basedir ? basedir : Paths.get(".").normalize();
        if (!Files.exists(actualBasedir)) {
            throw halt("Missing required option: '--basedir=<basedir>'");
        }
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
