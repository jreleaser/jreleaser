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

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface GithubReleaser extends BaseReleaser {
    Property<Boolean> getDraft()

    Property<Boolean> getImmutableRelease()

    Property<org.jreleaser.model.api.release.GithubReleaser.MakeLatest> getMakeLatest()

    Property<String> getDiscussionCategoryName()

    Prerelease getPrerelease()

    ReleaseNotes getReleaseNotes()

    void prerelease(Action<? super Prerelease> action)

    void releaseNotes(Action<? super ReleaseNotes> action)

    void setMakeLatest(String makeLatest)

    @CompileStatic
    interface ReleaseNotes {
        Property<Boolean> getEnabled()

        Property<String> getConfigurationFile()
    }
}