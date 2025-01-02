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
package org.jreleaser.gradle.plugin.tasks

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.jreleaser.engine.context.ModelAutoConfigurer
import org.jreleaser.gradle.plugin.internal.JReleaserLoggerService
import org.jreleaser.model.UpdateSection
import org.jreleaser.model.internal.JReleaserContext
import org.jreleaser.util.Env
import org.jreleaser.util.PlatformUtils
import org.jreleaser.workflow.Workflows

import javax.inject.Inject

import static java.util.stream.Collectors.toList
import static org.jreleaser.util.StringUtils.isBlank
import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@CompileStatic
abstract class JReleaseAutoConfigReleaseTask extends DefaultTask {
    static final String NAME = 'jreleaserAutoConfigRelease'

    @InputDirectory
    final DirectoryProperty outputDirectory
    @Input
    @Optional
    final Property<Boolean> dryrun
    @Input
    @Optional
    final Property<Boolean> gitRootSearch
    @Input
    @Optional
    final Property<Boolean> strict
    @Input
    @Optional
    final Property<String> projectName
    @Input
    @Optional
    final Property<String> projectVersion
    @Input
    @Optional
    final Property<String> projectVersionPattern
    @Input
    @Optional
    final Property<String> projectSnapshotPattern
    @Input
    @Optional
    final Property<String> projectSnapshotLabel
    @Input
    @Optional
    final Property<Boolean> projectSnapshotFullChangelog
    @Input
    @Optional
    final Property<String> projectCopyright
    @Input
    @Optional
    final Property<String> projectInceptionYear
    @Input
    @Optional
    final Property<String> projectStereotype
    @Input
    @Optional
    final Property<String> projectDescription
    @Input
    @Optional
    final ListProperty<String> authors
    @Input
    @Optional
    final Property<String> tagName
    @Input
    @Optional
    final Property<String> releaseName
    @Input
    @Optional
    final Property<String> branch
    @Input
    @Optional
    final Property<String> milestoneName
    @Input
    @Optional
    final Property<String> changeLog
    @Input
    @Optional
    final Property<String> username
    @Input
    @Optional
    final Property<String> commitAuthorName
    @Input
    @Optional
    final Property<String> commitAuthorEmail
    @Input
    @Optional
    final Property<Boolean> prerelease
    @Input
    @Optional
    final Property<String> prereleasePattern
    @Input
    @Optional
    final Property<Boolean> draft
    @Input
    @Optional
    final Property<Boolean> overwrite
    @Input
    @Optional
    final Property<Boolean> update
    @Input
    @Optional
    final SetProperty<UpdateSection> updateSections
    @Input
    @Optional
    final Property<Boolean> skipTag
    @Input
    @Optional
    final Property<Boolean> skipRelease
    @Input
    @Optional
    final Property<Boolean> skipChecksums
    @Input
    @Optional
    final Property<Boolean> changelogFormatted
    @Input
    @Optional
    final Property<Boolean> signing
    @Input
    @Optional
    final Property<Boolean> armored
    @Input
    @Optional
    final ListProperty<String> files
    @Input
    @Optional
    final ListProperty<String> globs
    @Input
    final Property<Boolean> selectCurrentPlatform
    @Input
    @Optional
    final ListProperty<String> selectPlatforms
    @Input
    @Optional
    final ListProperty<String> rejectPlatforms
    @Internal
    final Property<JReleaserLoggerService> jlogger

    @Option(option = 'project-name', description = 'The project name (OPTIONAL).')
    void setProjectName(String projectName) {
        this.projectName.set(projectName)
    }

    @Option(option = 'project-version', description = 'The project version (OPTIONAL).')
    void setProjectVersion(String projectVersion) {
        this.projectVersion.set(projectVersion)
    }

    @Option(option = 'project-version-pattern', description = 'The project version pattern (OPTIONAL).')
    void setProjectVersionPattern(String projectVersionPattern) {
        this.projectVersionPattern.set(projectVersionPattern)
    }

    @Option(option = 'project-snapshot-pattern', description = 'The project snapshot pattern (OPTIONAL).')
    void setProjectSnapshotPattern(String projectSnapshotPattern) {
        this.projectSnapshotPattern.set(projectSnapshotPattern)
    }

    @Option(option = 'project-snapshot-label', description = 'The project snapshot label (OPTIONAL).')
    void setProjectSnapshotLabel(String projectSnapshotLabel) {
        this.projectSnapshotLabel.set(projectSnapshotLabel)
    }

    @Option(option = 'project-snapshot-full-changelog', description = 'Calculate full changelog since last non-snapshot release (OPTIONAL).')
    void setProjectSnapshotFullChangelog(boolean projectSnapshotFullChangelog) {
        this.projectSnapshotFullChangelog.set(projectSnapshotFullChangelog)
    }

    @Option(option = 'project-copyright', description = 'The project copyright (OPTIONAL).')
    void setProjectCopyright(String projectCopyright) {
        this.projectCopyright.set(projectCopyright)
    }

    @Option(option = 'project-description', description = 'The project description (OPTIONAL).')
    void setProjectDescription(String projectDescription) {
        this.projectDescription.set(projectDescription)
    }

    @Option(option = 'project-inception-year', description = 'The project inception year (OPTIONAL).')
    void setProjectInceptionYear(String projectInceptionYear) {
        this.projectInceptionYear.set(projectInceptionYear)
    }

    @Option(option = 'project-stereotype', description = 'The project stereotype (OPTIONAL).')
    void setProjectStereotype(String projectStereotype) {
        this.projectStereotype.set(projectStereotype)
    }

    @Option(option = 'author', description = 'The project authors (OPTIONAL).')
    void setAuthor(List<String> authors) {
        this.authors.addAll(authors)
    }

    @Option(option = 'tag-name', description = 'The release tag (OPTIONAL).')
    void setTagName(String tagName) {
        this.tagName.set(tagName)
    }

    @Option(option = 'release-name', description = 'The release name (OPTIONAL).')
    void setReleaseName(String releaseName) {
        this.releaseName.set(releaseName)
    }

    @Option(option = 'branch', description = 'The release branch (OPTIONAL).')
    void setBranch(String branch) {
        this.branch.set(branch)
    }

    @Option(option = 'milestone-name', description = 'The milestone name (OPTIONAL).')
    void setMilestoneName(String milestoneName) {
        this.milestoneName.set(milestoneName)
    }

    @Option(option = 'changeLog', description = 'Path to changelog file (OPTIONAL).')
    void setChangeLog(String changeLog) {
        this.changeLog.set(changeLog)
    }

    @Option(option = 'username', description = 'Git username (OPTIONAL).')
    void setUsername(String username) {
        this.username.set(username)
    }

    @Option(option = 'commit-author-name', description = 'Commit author name (OPTIONAL).')
    void setCommitAuthorName(String commitAuthorName) {
        this.commitAuthorName.set(commitAuthorName)
    }

    @Option(option = 'commit-author-email', description = 'Commit author email (OPTIONAL).')
    void setCommitAuthorEmail(String commitAuthorEmail) {
        this.commitAuthorEmail.set(commitAuthorEmail)
    }

    @Option(option = 'dryrun', description = 'Skip remote operations (OPTIONAL).')
    void setDryrun(boolean dryrun) {
        this.dryrun.set(dryrun)
    }

    @Option(option = 'git-root-search', description = 'Searches for the Git root (OPTIONAL).')
    void setGitRootSearch(boolean gitRootSearch) {
        this.gitRootSearch.set(gitRootSearch)
    }

    @Option(option = 'strict', description = 'Enable strict mode (OPTIONAL).')
    void setStrict(boolean strict) {
        this.strict.set(strict)
    }

    @Option(option = 'prerelease', description = 'If the release is a prerelease (OPTIONAL).')
    void setPrerelease(boolean prerelease) {
        this.prerelease.set(prerelease)
    }

    @Option(option = 'prerelease-pattern', description = 'The prerelease pattern (OPTIONAL).')
    void prereleasePattern(String prereleasePattern) {
        this.prereleasePattern.set(prereleasePattern)
    }

    @Option(option = 'draft', description = 'If the release is a draft (OPTIONAL).')
    void setDraft(boolean draft) {
        this.draft.set(draft)
    }

    @Option(option = 'overwrite', description = 'Overwrite an existing release (OPTIONAL).')
    void setOverwrite(boolean overwrite) {
        this.overwrite.set(overwrite)
    }

    @Option(option = 'update', description = 'Update an existing release (OPTIONAL).')
    void setUpdate(boolean update) {
        this.update.set(update)
    }

    @Option(option = 'skip-tag', description = 'Skip tagging the release (OPTIONAL).')
    void setSkipTag(boolean skipTag) {
        this.skipTag.set(skipTag)
    }

    @Option(option = 'skip-release', description = 'Skip creating a release (OPTIONAL).')
    void setSkipRelease(boolean skipRelease) {
        this.skipRelease.set(skipRelease)
    }

    @Option(option = 'skip-checksums', description = 'Skip creating checksums (OPTIONAL).')
    void setSkipChecksums(boolean skipChecksums) {
        this.skipChecksums.set(skipChecksums)
    }

    @Option(option = 'changelog-formatted', description = 'Format generated changelog (OPTIONAL).')
    void setChangelogformatted(boolean changelogFormatted) {
        this.changelogFormatted.set(changelogFormatted)
    }

    @Option(option = 'signing', description = 'Sign files (OPTIONAL).')
    void setSigning(boolean signing) {
        this.signing.set(signing)
    }

    @Option(option = 'armored', description = 'Generate ascii armored signatures (OPTIONAL).')
    void setArmored(boolean armored) {
        this.armored.set(armored)
    }

    @Option(option = 'file', description = 'Input file(s) to be uploaded (OPTIONAL).')
    void setFile(List<String> files) {
        this.files.addAll(files)
    }

    @Option(option = 'glob', description = 'Input file(s) to be uploaded (as globs) (OPTIONAL).')
    void setGlob(List<String> globs) {
        this.globs.addAll(globs)
    }

    @Option(option = 'update-section', description = 'Release section to be updated (OPTIONAL).')
    void setUpdateSection(List<String> updateSections) {
        if (updateSections) {
            for (String updateSection : updateSections) {
                if (isNotBlank(updateSection)) {
                    this.updateSections.add(UpdateSection.of(updateSection.trim()))
                }
            }
        }
    }

    @Option(option = 'select-current-platform', description = 'Activates paths matching the current platform (OPTIONAL).')
    void setSelectCurrentPlatform(boolean selectCurrentPlatform) {
        this.selectCurrentPlatform.set(selectCurrentPlatform)
    }

    @Option(option = 'select-platform', description = 'Activates paths matching the given platform (OPTIONAL).')
    void setSelectPlatform(List<String> selectPlatforms) {
        this.selectPlatforms.addAll(selectPlatforms)
    }

    @Option(option = 'reject-platform', description = 'Activates paths not matching the given platform (OPTIONAL).')
    void setRejectPlatform(List<String> rejectPlatforms) {
        this.rejectPlatforms.addAll(rejectPlatforms)
    }

    @Inject
    JReleaseAutoConfigReleaseTask(ObjectFactory objects) {
        dryrun = objects.property(Boolean)
        gitRootSearch = objects.property(Boolean)
        strict = objects.property(Boolean)
        outputDirectory = objects.directoryProperty()
        jlogger = objects.property(JReleaserLoggerService)

        projectName = objects.property(String).convention(project.name)
        projectVersion = objects.property(String).convention(String.valueOf(project.version))
        projectVersionPattern = objects.property(String).convention(Providers.<String> notDefined())
        projectSnapshotPattern = objects.property(String).convention(Providers.<String> notDefined())
        projectSnapshotLabel = objects.property(String).convention(Providers.<String> notDefined())
        projectSnapshotFullChangelog = objects.property(Boolean).convention(false)
        projectCopyright = objects.property(String).convention(Providers.<String> notDefined())
        projectDescription = objects.property(String).convention(Providers.<String> notDefined())
        projectInceptionYear = objects.property(String).convention(Providers.<String> notDefined())
        projectStereotype = objects.property(String).convention(Providers.<String> notDefined())
        authors = objects.listProperty(String).convention([])
        tagName = objects.property(String).convention(Providers.<String> notDefined())
        releaseName = objects.property(String).convention(Providers.<String> notDefined())
        branch = objects.property(String).convention(Providers.<String> notDefined())
        milestoneName = objects.property(String).convention(Providers.<String> notDefined())
        changeLog = objects.property(String).convention(Providers.<String> notDefined())
        username = objects.property(String).convention(Providers.<String> notDefined())
        commitAuthorName = objects.property(String).convention(Providers.<String> notDefined())
        commitAuthorEmail = objects.property(String).convention(Providers.<String> notDefined())
        prerelease = objects.property(Boolean)
        prereleasePattern = objects.property(String).convention(Providers.<String> notDefined())
        draft = objects.property(Boolean)
        overwrite = objects.property(Boolean).convention(false)
        update = objects.property(Boolean).convention(false)
        updateSections = objects.setProperty(UpdateSection).convention(Providers.<Set<UpdateSection>> notDefined())
        skipTag = objects.property(Boolean).convention(false)
        skipRelease = objects.property(Boolean).convention(false)
        skipChecksums = objects.property(Boolean).convention(false)
        changelogFormatted = objects.property(Boolean).convention(false)
        signing = objects.property(Boolean).convention(false)
        armored = objects.property(Boolean).convention(false)
        files = objects.listProperty(String).convention([])
        globs = objects.listProperty(String).convention([])
        selectCurrentPlatform = objects.property(Boolean).convention(false)
        selectPlatforms = objects.listProperty(String).convention([])
        rejectPlatforms = objects.listProperty(String).convention([])
    }

    @TaskAction
    void performAction() {
        JReleaserContext context = ModelAutoConfigurer.builder()
            .logger(jlogger.get().logger)
            .basedir(project.projectDir.toPath())
            .outputDirectory(outputDirectory.get().asFile.toPath())
            .dryrun(dryrun.getOrElse(false))
            .gitRootSearch(gitRootSearch.getOrElse(false))
            .strict(strict.getOrElse(false))
            .projectName(projectName.get())
            .projectVersion(projectVersion.get())
            .projectVersionPattern(projectVersionPattern.orNull)
            .projectSnapshotPattern(projectSnapshotPattern.orNull)
            .projectSnapshotLabel(projectSnapshotLabel.orNull)
            .projectSnapshotFullChangelog(projectSnapshotFullChangelog.get())
            .projectCopyright(projectCopyright.orNull)
            .projectDescription(projectDescription.orNull)
            .projectInceptionYear(projectInceptionYear.orNull)
            .projectStereotype(projectStereotype.orNull)
            .authors((List<String>) authors.getOrElse([] as List<String>))
            .tagName(tagName.orNull)
            .releaseName(releaseName.orNull)
            .branch(branch.orNull)
            .milestoneName(milestoneName.orNull)
            .prerelease(prerelease.orNull)
            .prereleasePattern(prereleasePattern.orNull)
            .draft(draft.orNull)
            .overwrite(overwrite.get())
            .update(update.get())
            .updateSections((Set<UpdateSection>) updateSections.getOrElse([] as Set<UpdateSection>))
            .skipTag(skipTag.get())
            .skipRelease(skipRelease.get())
            .skipChecksums(skipChecksums.get())
            .changelog(changeLog.orNull)
            .changelogFormatted(changelogFormatted.get())
            .username(username.orNull)
            .commitAuthorName(commitAuthorName.orNull)
            .commitAuthorEmail(commitAuthorEmail.orNull)
            .signing(signing.get())
            .armored(armored.get())
            .files((List<String>) files.getOrElse([] as List<String>))
            .globs((List<String>) globs.getOrElse([] as List<String>))
            .selectedPlatforms(collectSelectedPlatforms())
            .rejectedPlatforms(collectRejectedPlatforms())
            .autoConfigure()

        Workflows.release(context).execute()
    }

    protected List<String> collectSelectedPlatforms() {
        boolean resolvedSelectCurrentPlatform = resolveBoolean(org.jreleaser.model.api.JReleaserContext.SELECT_CURRENT_PLATFORM, selectCurrentPlatform.getOrElse(false))
        if (resolvedSelectCurrentPlatform) return Collections.singletonList(PlatformUtils.getCurrentFull())
        return resolveCollection(org.jreleaser.model.api.JReleaserContext.SELECT_PLATFORMS, selectPlatforms.get() as List<String>)
    }

    protected List<String> collectRejectedPlatforms() {
        return resolveCollection(org.jreleaser.model.api.JReleaserContext.REJECT_PLATFORMS, rejectPlatforms.get() as List<String>)
    }

    protected boolean resolveBoolean(String key, Boolean value) {
        if (null != value) return value
        String resolvedValue = Env.resolve(key, '')
        return isNotBlank(resolvedValue) && Boolean.parseBoolean(resolvedValue)
    }

    protected List<String> resolveCollection(String key, List<String> values) {
        if (!values.isEmpty()) return values
        String resolvedValue = Env.resolve(key, '')
        if (isBlank(resolvedValue)) return Collections.emptyList()
        return Arrays.stream(resolvedValue.trim().split(','))
            .map({ s -> s.trim() })
            .filter({ s -> isNotBlank(s) })
            .collect(toList())
    }
}
