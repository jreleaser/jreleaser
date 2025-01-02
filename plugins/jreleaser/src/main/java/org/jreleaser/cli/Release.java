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
package org.jreleaser.cli;

import org.jreleaser.engine.context.ModelAutoConfigurer;
import org.jreleaser.model.UpdateSection;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.workflow.Workflows;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "release")
public class Release extends AbstractPlatformAwareModelCommand<Main> {
    @CommandLine.Option(names = {"--dry-run"})
    Boolean dryrun;

    @CommandLine.ArgGroup
    Composite composite;

    static class Composite {
        @CommandLine.ArgGroup(exclusive = false, order = 1,
            headingKey = "include.filter.header")
        Include include;

        @CommandLine.ArgGroup(exclusive = false, order = 2,
            headingKey = "exclude.filter.header")
        Exclude exclude;

        @CommandLine.ArgGroup(exclusive = false, order = 3,
            headingKey = "auto-config.header")
        AutoConfigGroup autoConfig;

        String[] includedDistributions() {
            return null != include ? include.includedDistributions : null;
        }

        String[] excludedDistributions() {
            return null != exclude ? exclude.excludedDistributions : null;
        }

        String[] includedDeployerTypes() {
            return null != include ? include.includedDeployerTypes : null;
        }

        String[] includedDeployerNames() {
            return null != include ? include.includedDeployerNames : null;
        }

        String[] excludedDeployerTypes() {
            return null != exclude ? exclude.excludedDeployerTypes : null;
        }

        String[] excludedDeployerNames() {
            return null != exclude ? exclude.excludedDeployerNames : null;
        }

        String[] includedUploaderTypes() {
            return null != include ? include.includedUploaderTypes : null;
        }

        String[] includedUploaderNames() {
            return null != include ? include.includedUploaderNames : null;
        }

        String[] excludedUploaderTypes() {
            return null != exclude ? exclude.excludedUploaderTypes : null;
        }

        String[] excludedUploaderNames() {
            return null != exclude ? exclude.excludedUploaderNames : null;
        }

        String[] includedCatalogers() {
            return null != include ? include.includedCatalogers : null;
        }

        String[] excludedCatalogers() {
            return null != exclude ? exclude.excludedCatalogers : null;
        }

        boolean isAutoConfig() {
            return null != autoConfig && autoConfig.autoConfig;
        }
    }

    static class Include {
        @CommandLine.Option(names = {"-d", "--distribution"},
            paramLabel = "<distribution>")
        String[] includedDistributions;

        @CommandLine.Option(names = {"-y", "--deployer"},
            paramLabel = "<deployer>")
        String[] includedDeployerTypes;

        @CommandLine.Option(names = {"-yn", "--deployer-name"},
            paramLabel = "<name>")
        String[] includedDeployerNames;

        @CommandLine.Option(names = {"-u", "--uploader"},
            paramLabel = "<uploader>")
        String[] includedUploaderTypes;

        @CommandLine.Option(names = {"-un", "--uploader-name"},
            paramLabel = "<name>")
        String[] includedUploaderNames;

        @CommandLine.Option(names = {"--cataloger"},
            paramLabel = "<cataloger>")
        String[] includedCatalogers;
    }

    static class Exclude {
        @CommandLine.Option(names = {"-xd", "--exclude-distribution"},
            paramLabel = "<distribution>")
        String[] excludedDistributions;

        @CommandLine.Option(names = {"-xy", "--exclude-deployer"},
            paramLabel = "<deployer>")
        String[] excludedDeployerTypes;

        @CommandLine.Option(names = {"-xyn", "--exclude-deployer-name"},
            paramLabel = "<name>")
        String[] excludedDeployerNames;

        @CommandLine.Option(names = {"-xu", "--exclude-uploader"},
            paramLabel = "<uploader>")
        String[] excludedUploaderTypes;

        @CommandLine.Option(names = {"-xun", "--exclude-uploader-name"},
            paramLabel = "<name>")
        String[] excludedUploaderNames;

        @CommandLine.Option(names = {"--exclude-cataloger"},
            paramLabel = "<cataloger>")
        String[] excludedCatalogers;
    }

    static class AutoConfigGroup {
        @CommandLine.Option(names = {"--auto-config"})
        boolean autoConfig;

        @CommandLine.Option(names = {"--project-name"}, paramLabel = "<name>")
        String projectName;

        @CommandLine.Option(names = {"--project-version"}, paramLabel = "<version>")
        String projectVersion;

        @CommandLine.Option(names = {"--project-version-pattern"}, paramLabel = "<pattern>")
        String projectVersionPattern;

        @CommandLine.Option(names = {"--project-snapshot-pattern"}, paramLabel = "<pattern>")
        String projectSnapshotPattern;

        @CommandLine.Option(names = {"--project-snapshot-label"}, paramLabel = "<label>")
        String projectSnapshotLabel;

        @CommandLine.Option(names = {"--project-snapshot-full-changelog"})
        boolean projectSnapshotFullChangelog;

        @CommandLine.Option(names = {"--project-copyright"}, paramLabel = "<copyright>")
        String projectCopyright;

        @CommandLine.Option(names = {"--project-description"}, paramLabel = "<description>")
        String projectDescription;

        @CommandLine.Option(names = {"--project-inception-year"}, paramLabel = "<year>")
        String projectInceptionYear;

        @CommandLine.Option(names = {"--project-stereotype"}, paramLabel = "<stereotype>")
        String projectStereotype;

        @CommandLine.Option(names = {"--author"},
                paramLabel = "<author>")
        String[] authors;

        @CommandLine.Option(names = {"--tag-name"}, paramLabel = "<tag>")
        String tagName;

        @CommandLine.Option(names = {"--previous-tag-name"}, paramLabel = "<tag>")
        String previousTagName;

        @CommandLine.Option(names = {"--release-name"}, paramLabel = "<name>")
        String releaseName;

        @CommandLine.Option(names = {"--milestone-name"}, paramLabel = "<name>")
        String milestoneName;

        @CommandLine.Option(names = {"--prerelease"})
        Boolean prerelease;

        @CommandLine.Option(names = {"--prerelease-pattern"}, paramLabel = "<pattern>")
        String prereleasePattern;

        @CommandLine.Option(names = {"--draft"})
        Boolean draft;

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

        @CommandLine.Option(names = {"--skip-checksums"})
        boolean skipChecksums;

        @CommandLine.Option(names = {"--branch"})
        String branch;

        @CommandLine.Option(names = {"--changelog"})
        String changelog;

        @CommandLine.Option(names = {"--changelog-formatted"})
        boolean changelogFormatted;

        @CommandLine.Option(names = {"--username"})
        String username;

        @CommandLine.Option(names = {"--commit-author-name"}, paramLabel = "<name>")
        String commitAuthorName;

        @CommandLine.Option(names = {"--commit-author-email"}, paramLabel = "<email>")
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

    @Override
    protected void collectCandidateDeprecatedArgs(Set<AbstractCommand.DeprecatedArg> args) {
        super.collectCandidateDeprecatedArgs(args);
        args.add(new DeprecatedArg("-d", "--distribution", "1.5.0"));
        args.add(new DeprecatedArg("-xd", "--exclude-distribution", "1.5.0"));
        args.add(new DeprecatedArg("-y", "--deployer", "1.5.0"));
        args.add(new DeprecatedArg("-yn", "--deployer-name", "1.5.0"));
        args.add(new DeprecatedArg("-xy", "--exclude-deployer", "1.5.0"));
        args.add(new DeprecatedArg("-xyn", "--exclude-deployer-name", "1.5.0"));
        args.add(new DeprecatedArg("-u", "--uploader", "1.5.0"));
        args.add(new DeprecatedArg("-un", "--uploader-name", "1.5.0"));
        args.add(new DeprecatedArg("-xu", "--exclude-uploader", "1.5.0"));
        args.add(new DeprecatedArg("-xun", "--exclude-uploader-name", "1.5.0"));
    }

    @Override
    protected JReleaserContext createContext() {
        JReleaserContext context = super.createContext();
        if (null != composite) {
            context.setIncludedDistributions(collectEntries(composite.includedDistributions()));
            context.setIncludedDeployerTypes(collectEntries(composite.includedDeployerTypes(), true));
            context.setIncludedDeployerNames(collectEntries(composite.includedDeployerNames()));
            context.setIncludedUploaderTypes(collectEntries(composite.includedUploaderTypes(), true));
            context.setIncludedUploaderNames(collectEntries(composite.includedUploaderNames()));
            context.setIncludedCatalogers(collectEntries(composite.includedCatalogers(), true));
            context.setExcludedDistributions(collectEntries(composite.excludedDistributions()));
            context.setExcludedDeployerTypes(collectEntries(composite.excludedDeployerTypes(), true));
            context.setExcludedDeployerNames(collectEntries(composite.excludedDeployerNames()));
            context.setExcludedUploaderTypes(collectEntries(composite.excludedUploaderTypes(), true));
            context.setExcludedUploaderNames(collectEntries(composite.excludedUploaderNames()));
            context.setExcludedCatalogers(collectEntries(composite.excludedCatalogers(), true));
        }
        return context;
    }

    @Override
    protected void execute() {
        if (null == composite || !composite.isAutoConfig()) {
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
            .gitRootSearch(gitRootSearch())
            .strict(strict())
            .projectName(composite.autoConfig.projectName)
            .projectVersion(composite.autoConfig.projectVersion)
            .projectVersionPattern(composite.autoConfig.projectVersionPattern)
            .projectSnapshotPattern(composite.autoConfig.projectSnapshotPattern)
            .projectSnapshotLabel(composite.autoConfig.projectSnapshotLabel)
            .projectSnapshotFullChangelog(composite.autoConfig.projectSnapshotFullChangelog)
            .projectCopyright(composite.autoConfig.projectCopyright)
            .projectDescription(composite.autoConfig.projectDescription)
            .projectInceptionYear(composite.autoConfig.projectInceptionYear)
            .projectStereotype(composite.autoConfig.projectStereotype)
            .authors(collectEntries(composite.autoConfig.authors))
            .tagName(composite.autoConfig.tagName)
            .previousTagName(composite.autoConfig.previousTagName)
            .releaseName(composite.autoConfig.releaseName)
            .milestoneName(composite.autoConfig.milestoneName)
            .branch(composite.autoConfig.branch)
            .prerelease(composite.autoConfig.prerelease)
            .prereleasePattern(composite.autoConfig.prereleasePattern)
            .draft(composite.autoConfig.draft)
            .overwrite(composite.autoConfig.overwrite)
            .update(composite.autoConfig.update)
            .updateSections(collectUpdateSections())
            .skipTag(composite.autoConfig.skipTag)
            .skipRelease(composite.autoConfig.skipRelease)
            .skipChecksums(composite.autoConfig.skipChecksums)
            .changelog(composite.autoConfig.changelog)
            .changelogFormatted(composite.autoConfig.changelogFormatted)
            .username(composite.autoConfig.username)
            .commitAuthorName(composite.autoConfig.commitAuthorName)
            .commitAuthorEmail(composite.autoConfig.commitAuthorEmail)
            .signing(composite.autoConfig.signing)
            .armored(composite.autoConfig.armored)
            .files(collectEntries(composite.autoConfig.files))
            .globs(collectEntries(composite.autoConfig.globs))
            .selectedPlatforms(collectSelectedPlatforms())
            .rejectedPlatforms(collectRejectedPlatforms())
            .autoConfigure();

        doExecute(context);
    }

    private Set<UpdateSection> collectUpdateSections() {
        Set<UpdateSection> set = new LinkedHashSet<>();
        if (null != composite.autoConfig.updateSections && composite.autoConfig.updateSections.length > 0) {
            for (String updateSection : composite.autoConfig.updateSections) {
                set.add(UpdateSection.of(updateSection.trim()));
            }
        }
        return set;
    }

    private void basedir() {
        actualBasedir = null != basedir ? basedir : Paths.get(".").normalize();
        if (!Files.exists(actualBasedir)) {
            throw halt($("ERROR_missing_required_option", "--basedir=<basedir>"));
        }
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        Workflows.release(context).execute();
    }

    @Override
    protected Boolean dryrun() {
        return dryrun;
    }

    private HaltExecutionException halt(String message) throws HaltExecutionException {
        spec.commandLine().getErr()
            .println(spec.commandLine().getColorScheme().errorText(message));
        spec.commandLine().usage(parent().getOut());
        throw new HaltExecutionException();
    }

    @Override
    protected JReleaserCommand getCommand() {
        return JReleaserCommand.RELEASE;
    }
}
