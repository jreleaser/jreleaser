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
package org.jreleaser.gradle.plugin.dsl.release

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.jreleaser.gradle.plugin.dsl.common.CommitAuthor
import org.jreleaser.model.Active
import org.jreleaser.model.UpdateSection
import org.jreleaser.model.api.common.Apply

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface BaseReleaser extends Releaser {
    Property<String> getHost()

    Property<String> getRepoOwner()

    Property<String> getName()

    Property<String> getRepoUrl()

    Property<String> getRepoCloneUrl()

    Property<String> getCommitUrl()

    Property<String> getSrcUrl()

    Property<String> getDownloadUrl()

    Property<String> getReleaseNotesUrl()

    Property<String> getLatestReleaseUrl()

    Property<String> getIssueTrackerUrl()

    Property<String> getUsername()

    Property<String> getToken()

    Property<String> getTagName()

    Property<String> getPreviousTagName()

    Property<String> getReleaseName()

    Property<String> getBranch()

    Property<String> getBranchPush()

    Property<Boolean> getSign()

    Property<Boolean> getSkipTag()

    Property<Boolean> getSkipRelease()

    Property<String> getApiEndpoint()

    Property<Integer> getConnectTimeout()

    Property<Integer> getReadTimeout()

    Property<Boolean> getArtifacts()

    Property<Boolean> getFiles()

    Property<Boolean> getChecksums()

    Property<Boolean> getCatalogs()

    Property<Boolean> getSignatures()

    Property<Boolean> getOverwrite()

    Changelog getChangelog()

    Milestone getMilestone()

    CommitAuthor getCommitAuthor()

    Property<Active> getUploadAssets()

    void setUploadAssets(String str)

    void changelog(Action<? super Changelog> action)

    void milestone(Action<? super Milestone> action)

    void commitAuthor(Action<? super CommitAuthor> action)

    void update(Action<? super Update> action)

    void issues(Action<? super Issues> action)

    void changelog(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Changelog) Closure<Void> action)

    void milestone(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Milestone) Closure<Void> action)

    void commitAuthor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommitAuthor) Closure<Void> action)

    void update(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Update) Closure<Void> action)

    void issues(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Issues) Closure<Void> action)

    @CompileStatic
    interface Update {
        Property<Boolean> getEnabled()

        SetProperty<UpdateSection> getSections()

        void section(String str)
    }

    @CompileStatic
    interface Prerelease {
        Property<Boolean> getEnabled()

        Property<String> getPattern()
    }

    @CompileStatic
    interface Milestone {
        Property<Boolean> getClose()

        Property<String> getName()
    }

    @CompileStatic
    interface Issues {
        Property<Boolean> getEnabled()

        Property<String> getComment()

        Property<Apply> getApplyMilestone()

        void setApplyMilestone(String str)

        void label(Action<? super Label> action)

        void label(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Label) Closure<Void> action)

        @CompileStatic
        interface Label {
            Property<String> getName()

            Property<String> getColor()

            Property<String> getDescription()
        }
    }
}