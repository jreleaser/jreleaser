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
import org.gradle.api.Action
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.deploy.Deployer
import org.jreleaser.model.Http

/**
 *
 * @author Andres Almiray
 * @since 1.3.0
 */
@CompileStatic
interface MavenDeployer extends Deployer {
    Property<Integer> getConnectTimeout()

    Property<Integer> getReadTimeout()

    Property<String> getUrl()

    Property<String> getUsername()

    Property<String> getPassword()

    Property<Http.Authorization> getAuthorization()

    void setAuthorization(String authorization)

    void stagingRepository(String str)

    Property<Boolean> getSign()

    Property<Boolean> getChecksums()

    Property<Boolean> getSourceJar()

    Property<Boolean> getJavadocJar()

    Property<Boolean> getVerifyPom()

    Property<Boolean> getApplyMavenCentralRules()

    ListProperty<String> getStagingRepositories()

    Property<Boolean> getSnapshotSupported()

    void artifactOverride(Action<? super ArtifactOverride> action)

    void artifactOverride(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ArtifactOverride) Closure<Void> action)

    interface ArtifactOverride {
        Property<String> getGroupId()

        Property<String> getArtifactId()

        Property<Boolean> getJar()

        Property<Boolean> getSourceJar()

        Property<Boolean> getJavadocJar()

        Property<Boolean> getVerifyPom()
    }
}