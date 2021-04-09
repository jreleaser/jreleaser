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
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Changelog
import org.jreleaser.gradle.plugin.dsl.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.GitService
import org.jreleaser.gradle.plugin.dsl.Milestone

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class AbstractGitService implements GitService {
    final Property<Boolean> enabled
    final Property<String> host
    final Property<String> owner
    final Property<String> name
    final Property<String> repoUrlFormat
    final Property<String> repoCloneUrlFormat
    final Property<String> commitUrlFormat
    final Property<String> downloadUrlFormat
    final Property<String> releaseNotesUrlFormat
    final Property<String> latestReleaseUrlFormat
    final Property<String> issueTrackerUrlFormat
    final Property<String> username
    final Property<String> token
    final Property<String> tagName
    final Property<String> releaseName
    final Property<Boolean> sign
    final Property<Boolean> skipTagging
    final Property<String> apiEndpoint
    final Property<Boolean> overwrite
    final Property<Boolean> allowUploadToExisting

    @Inject
    AbstractGitService(ObjectFactory objects) {
        enabled = objects.property(Boolean).convention(Providers.notDefined())
        host = objects.property(String).convention(Providers.notDefined())
        owner = objects.property(String).convention(Providers.notDefined())
        name = objects.property(String).convention(Providers.notDefined())
        repoUrlFormat = objects.property(String).convention(Providers.notDefined())
        repoCloneUrlFormat = objects.property(String).convention(Providers.notDefined())
        commitUrlFormat = objects.property(String).convention(Providers.notDefined())
        downloadUrlFormat = objects.property(String).convention(Providers.notDefined())
        releaseNotesUrlFormat = objects.property(String).convention(Providers.notDefined())
        latestReleaseUrlFormat = objects.property(String).convention(Providers.notDefined())
        issueTrackerUrlFormat = objects.property(String).convention(Providers.notDefined())

        username = objects.property(String).convention(Providers.notDefined())
        token = objects.property(String).convention(Providers.notDefined())
        tagName = objects.property(String).convention(Providers.notDefined())
        releaseName = objects.property(String).convention(Providers.notDefined())
        sign = objects.property(Boolean).convention(Providers.notDefined())
        skipTagging = objects.property(Boolean).convention(Providers.notDefined())
        apiEndpoint = objects.property(String).convention(Providers.notDefined())
        overwrite = objects.property(Boolean).convention(Providers.notDefined())
        allowUploadToExisting = objects.property(Boolean).convention(Providers.notDefined())
    }

    @Internal
    boolean isSet() {
        enabled.present ||
            host.present ||
            owner.present ||
            name.present ||
            repoUrlFormat.present ||
            repoCloneUrlFormat.present ||
            commitUrlFormat.present ||
            downloadUrlFormat.present ||
            releaseNotesUrlFormat.present ||
            latestReleaseUrlFormat.present ||
            issueTrackerUrlFormat.present ||
            username.present ||
            token.present ||
            tagName.present ||
            releaseName.present ||
            sign.present ||
            skipTagging.present ||
            apiEndpoint.present ||
            overwrite.present ||
            allowUploadToExisting.present
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

    protected void toModel(org.jreleaser.model.GitService service) {
        if (enabled.present) service.enabled = enabled.get()
        if (host.present) service.host = host.get()
        if (owner.present) service.owner = owner.get()
        if (name.present) service.name = name.get()
        if (repoUrlFormat.present) service.repoUrlFormat = repoUrlFormat.get()
        if (repoCloneUrlFormat.present) service.repoCloneUrlFormat = repoCloneUrlFormat.get()
        if (commitUrlFormat.present) service.commitUrlFormat = commitUrlFormat.get()
        if (downloadUrlFormat.present) service.downloadUrlFormat = downloadUrlFormat.get()
        if (releaseNotesUrlFormat.present) service.releaseNotesUrlFormat = releaseNotesUrlFormat.get()
        if (latestReleaseUrlFormat.present) service.latestReleaseUrlFormat = latestReleaseUrlFormat.get()
        if (issueTrackerUrlFormat.present) service.issueTrackerUrlFormat = issueTrackerUrlFormat.get()
        if (username.present) service.username = username.get()
        if (token.present) service.token = token.get()
        if (tagName.present) service.tagName = tagName.get()
        if (releaseName.present) service.releaseName = releaseName.get()
        if (apiEndpoint.present) service.apiEndpoint = apiEndpoint.get()
        service.sign = sign.getOrElse(false)
        service.skipTagging = skipTagging.getOrElse(false)
        service.overwrite = overwrite.getOrElse(false)
        service.allowUploadToExisting = allowUploadToExisting.getOrElse(false)
    }
}
