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
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.GitService

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
    final Property<String> commitUrlFormat
    final Property<String> downloadUrlFormat
    final Property<String> releaseNotesUrlFormat
    final Property<String> latestReleaseUrlFormat
    final Property<String> issueTrackerUrlFormat
    final Property<String> username
    final Property<String> password
    final Property<String> tagName
    final Property<String> releaseName
    final Property<String> commitAuthorName
    final Property<String> commitAuthorEmail
    final Property<Boolean> sign
    final Property<String> signingKey
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
        commitUrlFormat = objects.property(String).convention(Providers.notDefined())
        downloadUrlFormat = objects.property(String).convention(Providers.notDefined())
        releaseNotesUrlFormat = objects.property(String).convention(Providers.notDefined())
        latestReleaseUrlFormat = objects.property(String).convention(Providers.notDefined())
        issueTrackerUrlFormat = objects.property(String).convention(Providers.notDefined())

        username = objects.property(String).convention(Providers.notDefined())
        password = objects.property(String).convention(Providers.notDefined())
        tagName = objects.property(String).convention(Providers.notDefined())
        releaseName = objects.property(String).convention(Providers.notDefined())
        commitAuthorName = objects.property(String).convention(Providers.notDefined())
        commitAuthorEmail = objects.property(String).convention(Providers.notDefined())
        sign = objects.property(Boolean).convention(Providers.notDefined())
        signingKey = objects.property(String).convention(Providers.notDefined())
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
            commitUrlFormat.present ||
            downloadUrlFormat.present ||
            releaseNotesUrlFormat.present ||
            latestReleaseUrlFormat.present ||
            issueTrackerUrlFormat.present ||
            username.present ||
            password.present ||
            tagName.present ||
            releaseName.present ||
            commitAuthorName.present ||
            commitAuthorEmail.present ||
            sign.present ||
            signingKey.present ||
            apiEndpoint.present ||
            overwrite.present ||
            allowUploadToExisting.present
    }

    protected void toModel(org.jreleaser.model.GitService service) {
        if (enabled.present) service.enabled = enabled.get()
        if (host.present) service.host = host.get()
        if (owner.present) service.owner = owner.get()
        if (name.present) service.name = name.get()
        if (repoUrlFormat.present) service.repoUrlFormat = repoUrlFormat.get()
        if (commitUrlFormat.present) service.commitUrlFormat = commitUrlFormat.get()
        if (downloadUrlFormat.present) service.downloadUrlFormat = downloadUrlFormat.get()
        if (releaseNotesUrlFormat.present) service.releaseNotesUrlFormat = releaseNotesUrlFormat.get()
        if (latestReleaseUrlFormat.present) service.latestReleaseUrlFormat = latestReleaseUrlFormat.get()
        if (issueTrackerUrlFormat.present) service.issueTrackerUrlFormat = issueTrackerUrlFormat.get()
        if (username.present) service.username = username.get()
        if (password.present) service.password = password.get()
        if (tagName.present) service.tagName = tagName.get()
        if (releaseName.present) service.releaseName = releaseName.get()
        if (commitAuthorName.present) service.commitAuthorName = commitAuthorName.get()
        if (commitAuthorEmail.present) service.commitAuthorEmail = commitAuthorEmail.get()
        if (signingKey.present) service.signingKey = signingKey.get()
        if (apiEndpoint.present) service.apiEndpoint = apiEndpoint.get()
        service.sign = sign.getOrElse(false)
        service.overwrite = overwrite.getOrElse(false)
        service.allowUploadToExisting = allowUploadToExisting.getOrElse(false)
    }
}
