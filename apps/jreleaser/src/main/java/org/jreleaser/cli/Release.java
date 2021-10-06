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
@CommandLine.Command(name = "release")
public class Release extends AbstractPlatformAwareModelCommand {
    @CommandLine.Option(names = {"-y", "--dryrun"})
    boolean dryrun;

    @CommandLine.ArgGroup(exclusive = false, multiplicity = "0..1",
        headingKey = "auto-config.header")
    AutoConfigGroup autoConfigGroup;

    static class AutoConfigGroup {
        @CommandLine.Option(names = {"--auto-config"})
        boolean autoConfig;

        @CommandLine.Option(names = {"--project-name"})
        String projectName;

        @CommandLine.Option(names = {"--project-version"})
        String projectVersion;

        @CommandLine.Option(names = {"--project-version-pattern"})
        String projectVersionPattern;

        @CommandLine.Option(names = {"--project-snapshot-pattern"})
        String projectSnapshotPattern;

        @CommandLine.Option(names = {"--project-snapshot-label"})
        String projectSnapshotLabel;

        @CommandLine.Option(names = {"--project-snapshot-full-changelog"})
        boolean projectSnapshotFullChangelog;

        @CommandLine.Option(names = {"--tag-name"})
        String tagName;

        @CommandLine.Option(names = {"--previous-tag-name"})
        String previousTagName;

        @CommandLine.Option(names = {"--release-name"})
        String releaseName;

        @CommandLine.Option(names = {"--milestone-name"})
        String milestoneName;

        @CommandLine.Option(names = {"--prerelease"})
        boolean prerelease;

        @CommandLine.Option(names = {"--prerelease-pattern"})
        String prereleasePattern;

        @CommandLine.Option(names = {"--draft"})
        boolean draft;

        @CommandLine.Option(names = {"--overwrite"})
        boolean overwrite;

        @CommandLine.Option(names = {"--update"})
        boolean update;

        @CommandLine.Option(names = {"--update-section"},
            paramLabel = "<section>")
        String[] updateSections;

        @CommandLine.Option(names = {"--skip-tag"})
        boolean skipTag;

        @CommandLine.Option(names = {"--skip-release"})
        boolean skipRelease;

        @CommandLine.Option(names = {"--branch"})
        String branch;

        @CommandLine.Option(names = {"--changelog"})
        String changelog;

        @CommandLine.Option(names = {"--changelog-formatted"})
        boolean changelogFormatted;

        @CommandLine.Option(names = {"--username"})
        String username;

        @CommandLine.Option(names = {"--commit-author-name"})
        String commitAuthorName;

        @CommandLine.Option(names = {"--commit-author-email"})
        String commitAuthorEmail;

        @CommandLine.Option(names = {"--signing-enabled"})
        boolean signing;

        @CommandLine.Option(names = {"--signing-armored"})
        boolean armored;

        @CommandLine.Option(names = {"--file"},
            paramLabel = "<file>")
        String[] files;

        @CommandLine.Option(names = {"--glob"},
            paramLabel = "<glob>")
        String[] globs;
    }

    protected void execute() {
        if (null == autoConfigGroup || !autoConfigGroup.autoConfig) {
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
            .projectName(autoConfigGroup.projectName)
            .projectVersion(autoConfigGroup.projectVersion)
            .projectVersionPattern(autoConfigGroup.projectVersionPattern)
            .projectSnapshotPattern(autoConfigGroup.projectSnapshotPattern)
            .projectSnapshotLabel(autoConfigGroup.projectSnapshotLabel)
            .projectSnapshotFullChangelog(autoConfigGroup.projectSnapshotFullChangelog)
            .tagName(autoConfigGroup.tagName)
            .previousTagName(autoConfigGroup.previousTagName)
            .releaseName(autoConfigGroup.releaseName)
            .milestoneName(autoConfigGroup.milestoneName)
            .branch(autoConfigGroup.branch)
            .prerelease(autoConfigGroup.prerelease)
            .prereleasePattern(autoConfigGroup.prereleasePattern)
            .draft(autoConfigGroup.draft)
            .overwrite(autoConfigGroup.overwrite)
            .update(autoConfigGroup.update)
            .updateSections(collectUpdateSections())
            .skipTag(autoConfigGroup.skipTag)
            .skipRelease(autoConfigGroup.skipRelease)
            .changelog(autoConfigGroup.changelog)
            .changelogFormatted(autoConfigGroup.changelogFormatted)
            .username(autoConfigGroup.username)
            .commitAuthorName(autoConfigGroup.commitAuthorName)
            .commitAuthorEmail(autoConfigGroup.commitAuthorEmail)
            .signing(autoConfigGroup.signing)
            .armored(autoConfigGroup.armored)
            .files(collectFiles())
            .globs(collectGlobs())
            .selectedPlatforms(collectSelectedPlatforms())
            .autoConfigure();

        doExecute(context);
    }

    private List<String> collectFiles() {
        List<String> list = new ArrayList<>();
        if (autoConfigGroup.files != null && autoConfigGroup.files.length > 0) {
            Collections.addAll(list, autoConfigGroup.files);
        }
        return list;
    }

    private List<String> collectGlobs() {
        List<String> list = new ArrayList<>();
        if (autoConfigGroup.globs != null && autoConfigGroup.globs.length > 0) {
            Collections.addAll(list, autoConfigGroup.globs);
        }
        return list;
    }

    private Set<UpdateSection> collectUpdateSections() {
        Set<UpdateSection> set = new LinkedHashSet<>();
        if (autoConfigGroup.updateSections != null && autoConfigGroup.updateSections.length > 0) {
            for (String updateSection : autoConfigGroup.updateSections) {
                set.add(UpdateSection.of(updateSection.trim()));
            }
        }
        return set;
    }

    private void basedir() {
        actualBasedir = null != basedir ? basedir : Paths.get(".").normalize();
        if (!Files.exists(actualBasedir)) {
            throw halt(String.format(
                bundle.getString("ERROR_missing_required_option"),
                "--basedir=<basedir>"));
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
