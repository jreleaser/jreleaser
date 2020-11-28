/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Andres Almiray.
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
package org.kordamp.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.kordamp.jreleaser.gradle.plugin.dsl.Release

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ReleaseImpl implements Release {
    final Property<String> repoHost
    final Property<String> repoOwner
    final Property<String> repoName
    final Property<String> downloadUrlFormat
    final Property<String> releaseNotesUrlFormat
    final Property<String> latestReleaseUrlFormat
    final Property<String> issueTrackerUrlFormat
    final Property<org.kordamp.jreleaser.model.Release.RepoType> repoType
    final Property<String> authorization
    final Property<String> tagName
    final Property<String> targetCommitish
    final Property<String> releaseName
    final Property<String> body
    final Property<String> apiEndpoint
    final Property<Boolean> draft
    final Property<Boolean> prerelease
    final Property<Boolean> overwrite
    final Property<Boolean> allowUploadToExisting

    @Inject
    ReleaseImpl(ObjectFactory objects) {
        repoHost = objects.property(String).convention(Providers.notDefined())
        repoOwner = objects.property(String).convention(Providers.notDefined())
        repoName = objects.property(String).convention(Providers.notDefined())
        downloadUrlFormat = objects.property(String).convention(Providers.notDefined())
        releaseNotesUrlFormat = objects.property(String).convention(Providers.notDefined())
        latestReleaseUrlFormat = objects.property(String).convention(Providers.notDefined())
        issueTrackerUrlFormat = objects.property(String).convention(Providers.notDefined())
        repoType = objects.property(org.kordamp.jreleaser.model.Release.RepoType)
            .convention(org.kordamp.jreleaser.model.Release.RepoType.GITHUB)

        authorization = objects.property(String).convention(Providers.notDefined())
        tagName = objects.property(String).convention(Providers.notDefined())
        targetCommitish = objects.property(String).convention(Providers.notDefined())
        releaseName = objects.property(String).convention(Providers.notDefined())
        body = objects.property(String).convention(Providers.notDefined())
        apiEndpoint = objects.property(String).convention(Providers.notDefined())
        draft = objects.property(Boolean).convention(false)
        prerelease = objects.property(Boolean).convention(false)
        overwrite = objects.property(Boolean).convention(false)
        allowUploadToExisting = objects.property(Boolean).convention(false)
    }

    @Override
    void setRepoType(String repoType) {
        this.repoType.set(org.kordamp.jreleaser.model.Release.RepoType.valueOf(repoType.toUpperCase()))
    }

    org.kordamp.jreleaser.model.Release toModel() {
        org.kordamp.jreleaser.model.Release release = new org.kordamp.jreleaser.model.Release()
        release.repoHost = repoHost.orNull
        release.repoOwner = repoOwner.orNull
        release.repoName = repoName.orNull
        release.downloadUrlFormat = downloadUrlFormat.orNull
        release.releaseNotesUrlFormat = releaseNotesUrlFormat.orNull
        release.latestReleaseUrlFormat = latestReleaseUrlFormat.orNull
        release.issueTrackerUrlFormat = issueTrackerUrlFormat.orNull
        release.repoType = repoType.get()
        release.authorization = authorization.orNull
        release.tagName = tagName.orNull
        release.targetCommitish = targetCommitish.orNull
        release.releaseName = releaseName.orNull
        release.body = body.orNull
        release.draft = draft.get()
        release.prerelease = prerelease.get()
        release.overwrite = overwrite.get()
        release.allowUploadToExisting = allowUploadToExisting.get()
        release.apiEndpoint = apiEndpoint.orNull
        release
    }
}
