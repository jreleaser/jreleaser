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
package org.jreleaser.gradle.plugin.tasks

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.jreleaser.engine.context.ModelAutoConfigurer
import org.jreleaser.gradle.plugin.internal.JReleaserLoggerAdapter
import org.jreleaser.model.JReleaserContext
import org.jreleaser.workflow.Workflows

import javax.inject.Inject
import java.nio.file.Files
import java.nio.file.Path

/**
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@CompileStatic
abstract class JReleaseAutoConfigReleaseTask extends DefaultTask {
    @Input
    final DirectoryProperty outputDirectory
    @Input
    @Optional
    final Property<Boolean> dryrun
    @Input
    @Optional
    final Property<String> projectName
    @Input
    @Optional
    final Property<String> projectVersion
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
    final Property<Boolean> draft
    @Input
    @Optional
    final Property<Boolean> overwrite
    @Input
    @Optional
    final Property<Boolean> update
    @Input
    @Optional
    final Property<Boolean> skipTag
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

    @Option(option = 'project-name', description = 'The project name (OPTIONAL).')
    void setProjectName(String projectName) {
        this.projectName.set(projectName)
    }

    @Option(option = 'project-version', description = 'The project version (OPTIONAL).')
    void setProjectVersion(String projectVersion) {
        this.projectVersion.set(projectVersion)
    }

    @Option(option = 'tag-name', description = 'The release tga (OPTIONAL).')
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

    @Option(option = 'dryrun', description = 'Skip remote operations.')
    void setDryrun(boolean dryrun) {
        this.dryrun.set(dryrun)
    }

    @Option(option = 'prerelease', description = 'If the release is a prerelease (OPTIONAL).')
    void setPrerelease(boolean prerelease) {
        this.prerelease.set(prerelease)
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

    @Inject
    JReleaseAutoConfigReleaseTask(ObjectFactory objects) {
        dryrun = objects.property(Boolean).convention(false)
        outputDirectory = objects.directoryProperty()

        projectName = objects.property(String).convention(project.name)
        projectVersion = objects.property(String).convention(String.valueOf(project.version))
        tagName = objects.property(String).convention(Providers.notDefined())
        releaseName = objects.property(String).convention(Providers.notDefined())
        branch = objects.property(String).convention(Providers.notDefined())
        milestoneName = objects.property(String).convention(Providers.notDefined())
        changeLog = objects.property(String).convention(Providers.notDefined())
        username = objects.property(String).convention(Providers.notDefined())
        commitAuthorName = objects.property(String).convention(Providers.notDefined())
        commitAuthorEmail = objects.property(String).convention(Providers.notDefined())
        prerelease = objects.property(Boolean).convention(false)
        draft = objects.property(Boolean).convention(false)
        overwrite = objects.property(Boolean).convention(false)
        update = objects.property(Boolean).convention(false)
        skipTag = objects.property(Boolean).convention(false)
        changelogFormatted = objects.property(Boolean).convention(false)
        signing = objects.property(Boolean).convention(false)
        armored = objects.property(Boolean).convention(false)
        files = objects.listProperty(String).convention([])
    }

    @TaskAction
    void performAction() {
        Path outputDirectoryPath = outputDirectory.get().asFile.toPath()
        Files.createDirectories(outputDirectoryPath)
        PrintWriter tracer = new PrintWriter(new FileOutputStream(outputDirectoryPath
            .resolve('trace.log').toFile()))

        JReleaserContext context = ModelAutoConfigurer.builder()
            .logger(new JReleaserLoggerAdapter(project, tracer))
            .basedir(project.projectDir.toPath())
            .outputDirectory(outputDirectoryPath)
            .dryrun(dryrun.get())
            .projectName(projectName.get())
            .projectVersion(projectVersion.get())
            .tagName(tagName.orNull)
            .releaseName(releaseName.orNull)
            .branch(branch.orNull)
            .milestoneName(milestoneName.orNull)
            .prerelease(prerelease.get())
            .draft(draft.get())
            .overwrite(overwrite.get())
            .update(update.get())
            .skipTag(skipTag.get())
            .changelog(changeLog.orNull)
            .changelogFormatted(changelogFormatted.get())
            .username(username.orNull)
            .commitAuthorName(commitAuthorName.orNull)
            .commitAuthorEmail(commitAuthorEmail.orNull)
            .signing(signing.get())
            .armored(armored.get())
            .files((List<String>) files.getOrElse([] as List<String>))
            .autoConfigure()

        Workflows.release(context).execute()
    }
}
