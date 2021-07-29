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
package org.jreleaser.gradle.plugin.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.jreleaser.model.UpdateSection

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface GitService extends Releaser {
    Property<String> getHost()

    Property<String> getOwner()

    Property<String> getName()

    Property<String> getRepoUrl()

    Property<String> getRepoCloneUrl()

    Property<String> getCommitUrl()

    Property<String> getDownloadUrl()

    Property<String> getReleaseNotesUrl()

    Property<String> getLatestReleaseUrl()

    Property<String> getIssueTrackerUrl()

    @Deprecated
    Property<String> getRepoUrlFormat()

    @Deprecated
    Property<String> getRepoCloneUrlFormat()

    @Deprecated
    Property<String> getCommitUrlFormat()

    @Deprecated
    Property<String> getDownloadUrlFormat()

    @Deprecated
    Property<String> getReleaseNotesUrlFormat()

    @Deprecated
    Property<String> getLatestReleaseUrlFormat()

    @Deprecated
    Property<String> getIssueTrackerUrlFormat()

    Property<String> getUsername()

    Property<String> getToken()

    Property<String> getTagName()

    Property<String> getReleaseName()

    Property<String> getBranch()

    Property<Boolean> getSign()

    Property<Boolean> getSkipTag()

    Property<String> getApiEndpoint()

    Property<Integer> getConnectTimeout()

    Property<Integer> getReadTimeout()

    Property<Boolean> getArtifacts()

    Property<Boolean> getFiles()

    Property<Boolean> getChecksums()

    Property<Boolean> getSignatures()

    Property<Boolean> getOverwrite()

    Property<Boolean> getUpdate()

    Changelog getChangelog()

    Milestone getMilestone()

    CommitAuthor getCommitAuthor()

    SetProperty<UpdateSection> getUpdateSections()

    void updateSection(String str)

    void changelog(Action<? super Changelog> action)

    void milestone(Action<? super Milestone> action)

    void commitAuthor(Action<? super CommitAuthor> action)

    void changelog(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Changelog) Closure<Void> action)

    void milestone(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Milestone) Closure<Void> action)

    void commitAuthor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommitAuthor) Closure<Void> action)
}