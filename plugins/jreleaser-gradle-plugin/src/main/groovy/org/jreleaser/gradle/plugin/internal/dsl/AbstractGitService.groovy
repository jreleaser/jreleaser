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
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Changelog
import org.jreleaser.gradle.plugin.dsl.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.GitService
import org.jreleaser.model.Active
import org.jreleaser.model.UpdateSection
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class AbstractGitService implements GitService {
    final Property<Boolean> enabled
    final Property<String> host
    final Property<String> repoOwner
    final Property<String> name
    final Property<String> repoUrl
    final Property<String> repoCloneUrl
    final Property<String> commitUrl
    final Property<String> srcUrl
    final Property<String> downloadUrl
    final Property<String> releaseNotesUrl
    final Property<String> latestReleaseUrl
    final Property<String> issueTrackerUrl
    final Property<String> username
    final Property<String> token
    final Property<String> tagName
    final Property<String> previousTagName
    final Property<String> releaseName
    final Property<String> branch
    final Property<Boolean> sign
    final Property<Boolean> skipTag
    final Property<Boolean> skipRelease
    final Property<String> apiEndpoint
    final Property<Integer> connectTimeout
    final Property<Integer> readTimeout
    final Property<Boolean> artifacts
    final Property<Boolean> files
    final Property<Boolean> checksums
    final Property<Boolean> signatures
    final Property<Boolean> overwrite
    final Property<Active> uploadAssets

    final UpdateImpl update

    @Inject
    AbstractGitService(ObjectFactory objects) {
        enabled = objects.property(Boolean).convention(Providers.notDefined())
        host = objects.property(String).convention(Providers.notDefined())
        repoOwner = objects.property(String).convention(Providers.notDefined())
        name = objects.property(String).convention(Providers.notDefined())
        repoUrl = objects.property(String).convention(Providers.notDefined())
        repoCloneUrl = objects.property(String).convention(Providers.notDefined())
        commitUrl = objects.property(String).convention(Providers.notDefined())
        srcUrl = objects.property(String).convention(Providers.notDefined())
        downloadUrl = objects.property(String).convention(Providers.notDefined())
        releaseNotesUrl = objects.property(String).convention(Providers.notDefined())
        latestReleaseUrl = objects.property(String).convention(Providers.notDefined())
        issueTrackerUrl = objects.property(String).convention(Providers.notDefined())

        username = objects.property(String).convention(Providers.notDefined())
        token = objects.property(String).convention(Providers.notDefined())
        tagName = objects.property(String).convention(Providers.notDefined())
        previousTagName = objects.property(String).convention(Providers.notDefined())
        releaseName = objects.property(String).convention(Providers.notDefined())
        branch = objects.property(String).convention(Providers.notDefined())
        sign = objects.property(Boolean).convention(Providers.notDefined())
        skipTag = objects.property(Boolean).convention(Providers.notDefined())
        skipRelease = objects.property(Boolean).convention(Providers.notDefined())
        apiEndpoint = objects.property(String).convention(Providers.notDefined())
        connectTimeout = objects.property(Integer).convention(Providers.notDefined())
        readTimeout = objects.property(Integer).convention(Providers.notDefined())
        artifacts = objects.property(Boolean).convention(Providers.notDefined())
        files = objects.property(Boolean).convention(Providers.notDefined())
        checksums = objects.property(Boolean).convention(Providers.notDefined())
        signatures = objects.property(Boolean).convention(Providers.notDefined())
        overwrite = objects.property(Boolean).convention(Providers.notDefined())
        uploadAssets = objects.property(Active).convention(Providers.notDefined())

        update = objects.newInstance(UpdateImpl, objects)
    }

    @Internal
    boolean isSet() {
        enabled.present ||
            host.present ||
            repoOwner.present ||
            name.present ||
            repoUrl.present ||
            repoCloneUrl.present ||
            commitUrl.present ||
            srcUrl.present ||
            downloadUrl.present ||
            releaseNotesUrl.present ||
            latestReleaseUrl.present ||
            issueTrackerUrl.present ||
            username.present ||
            token.present ||
            tagName.present ||
            previousTagName.present ||
            releaseName.present ||
            branch.present ||
            sign.present ||
            skipTag.present ||
            skipRelease.present ||
            apiEndpoint.present ||
            connectTimeout.present ||
            readTimeout.present ||
            artifacts.present ||
            files.present ||
            checksums.present ||
            signatures.present ||
            overwrite.present ||
            update.isSet() ||
            uploadAssets.present
    }

    @Override
    void setUploadAssets(String str) {
        if (isNotBlank(str)) {
            uploadAssets.set(Active.of(str.trim()))
        }
    }

    @Override
    void changelog(Action<? super Changelog> action) {
        action.execute(changelog)
    }

    @Override
    void milestone(Action<? super Milestone> action) {
        action.execute(milestone)
    }

    @Override
    void commitAuthor(Action<? super CommitAuthor> action) {
        action.execute(commitAuthor)
    }

    @Override
    void update(Action<? super Update> action) {
        action.execute(update)
    }

    @Override
    void changelog(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Changelog) Closure<Void> action) {
        ConfigureUtil.configure(action, changelog)
    }

    @Override
    void milestone(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Milestone) Closure<Void> action) {
        ConfigureUtil.configure(action, milestone)
    }

    @Override
    void commitAuthor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommitAuthor) Closure<Void> action) {
        ConfigureUtil.configure(action, commitAuthor)
    }

    @Override
    void update(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Update) Closure<Void> action) {
        ConfigureUtil.configure(action, update)
    }

    protected void toModel(org.jreleaser.model.GitService service) {
        if (enabled.present) service.enabled = enabled.get()
        if (host.present) service.host = host.get()
        if (repoOwner.present) service.owner = repoOwner.get()
        if (name.present) service.name = name.get()
        if (repoUrl.present) service.repoUrl = repoUrl.get()
        if (repoCloneUrl.present) service.repoCloneUrl = repoCloneUrl.get()
        if (commitUrl.present) service.commitUrl = commitUrl.get()
        if (srcUrl.present) service.srcUrl = srcUrl.get()
        if (downloadUrl.present) service.downloadUrl = downloadUrl.get()
        if (releaseNotesUrl.present) service.releaseNotesUrl = releaseNotesUrl.get()
        if (latestReleaseUrl.present) service.latestReleaseUrl = latestReleaseUrl.get()
        if (issueTrackerUrl.present) service.issueTrackerUrl = issueTrackerUrl.get()
        if (username.present) service.username = username.get()
        if (token.present) service.token = token.get()
        if (tagName.present) service.tagName = tagName.get()
        if (previousTagName.present) service.previousTagName = previousTagName.get()
        if (releaseName.present) service.releaseName = releaseName.get()
        if (branch.present) service.branch = branch.get()
        if (apiEndpoint.present) service.apiEndpoint = apiEndpoint.get()
        if (connectTimeout.present) service.connectTimeout = connectTimeout.get()
        if (readTimeout.present) service.readTimeout = readTimeout.get()
        if (artifacts.present) service.artifacts = artifacts.get()
        if (files.present) service.files = files.get()
        if (checksums.present) service.checksums = checksums.get()
        if (signatures.present) service.signatures = signatures.get()
        if (uploadAssets.present) service.uploadAssets = uploadAssets.get()
        if (sign.present) service.sign = sign.get()
        if (skipTag.present) service.skipTag = skipTag.get()
        if (skipRelease.present) service.skipRelease = skipRelease.get()
        if (overwrite.present) service.overwrite = overwrite.get()
        if (update.isSet()) service.update = update.toModel()
    }

    @CompileStatic
    static class UpdateImpl implements Update {
        final Property<Boolean> enabled
        final SetProperty<UpdateSection> sections

        @Inject
        UpdateImpl(ObjectFactory objects) {
            enabled = objects.property(Boolean).convention(Providers.notDefined())
            sections = objects.setProperty(UpdateSection).convention(Providers.notDefined())
        }

        @Internal
        boolean isSet() {
            enabled.present ||
                sections.present
        }

        @Override
        void section(String str) {
            if (isNotBlank(str)) {
                this.sections.add(UpdateSection.of(str.trim()))
            }
        }

        org.jreleaser.model.GitService.Update toModel() {
            org.jreleaser.model.GitService.Update update = new org.jreleaser.model.GitService.Update()
            if (enabled.present) update.enabled = enabled.get()
            update.sections = (Set<UpdateSection>) sections.getOrElse([] as Set<UpdateSection>)
            update
        }
    }

    @CompileStatic
    static class PrereleaseImpl implements Prerelease {
        final Property<String> pattern
        final Property<Boolean> enabled

        @Inject
        PrereleaseImpl(ObjectFactory objects) {
            pattern = objects.property(String).convention(Providers.notDefined())
            enabled = objects.property(Boolean).convention(Providers.notDefined())
        }

        @Internal
        boolean isSet() {
            pattern.present ||
                enabled.present
        }

        org.jreleaser.model.GitService.Prerelease toModel() {
            org.jreleaser.model.GitService.Prerelease prerelease = new org.jreleaser.model.GitService.Prerelease()
            if (pattern.present) prerelease.pattern = pattern.get()
            if (enabled.present) prerelease.enabled = enabled.get()
            prerelease
        }
    }

    @CompileStatic
    static class MilestoneImpl implements Milestone {
        final Property<Boolean> close
        final Property<String> name

        @Inject
        MilestoneImpl(ObjectFactory objects) {
            close = objects.property(Boolean).convention(Providers.notDefined())
            name = objects.property(String).convention(Providers.notDefined())
        }

        @Internal
        boolean isSet() {
            close.present ||
                name.present
        }

        org.jreleaser.model.GitService.Milestone toModel() {
            org.jreleaser.model.GitService.Milestone milestone = new org.jreleaser.model.GitService.Milestone()
            if (close.present) milestone.close = close.get()
            if (name.present) milestone.name = name.get()
            milestone
        }
    }
}
