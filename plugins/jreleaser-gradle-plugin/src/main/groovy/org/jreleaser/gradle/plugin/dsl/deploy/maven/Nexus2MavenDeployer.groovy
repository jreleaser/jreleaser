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
package org.jreleaser.gradle.plugin.dsl.deploy.maven

import groovy.transform.CompileStatic
import org.gradle.api.provider.Property

/**
 *
 * @author Andres Almiray
 * @since 1.3.0
 */
@CompileStatic
interface Nexus2MavenDeployer extends MavenDeployer {
    Property<String> getSnapshotUrl()

    Property<String> getVerifyUrl()

    Property<Boolean> getCloseRepository()

    Property<Boolean> getReleaseRepository()

    Property<Integer> getTransitionDelay()

    Property<Integer> getTransitionMaxRetries()

    Property<String> getStagingProfileId()

    Property<String> getStagingRepositoryId()

    Property<org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.Stage> getStartStage()

    Property<org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.Stage> getEndStage()

    void setStartStage(String stage)

    void setEndStage(String stage)
}