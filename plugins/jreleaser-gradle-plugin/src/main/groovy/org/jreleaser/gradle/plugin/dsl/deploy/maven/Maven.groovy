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
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.common.Activatable

/**
 *
 * @author Andres Almiray
 * @since 1.3.0
 */
@CompileStatic
interface Maven extends Activatable {
    NamedDomainObjectContainer<ArtifactoryMavenDeployer> getArtifactory()

    NamedDomainObjectContainer<AzureMavenDeployer> getAzure()

    NamedDomainObjectContainer<GiteaMavenDeployer> getGitea()

    NamedDomainObjectContainer<GithubMavenDeployer> getGithub()

    NamedDomainObjectContainer<GitlabMavenDeployer> getGitlab()

    NamedDomainObjectContainer<Nexus2MavenDeployer> getNexus2()

    NamedDomainObjectContainer<MavenCentralMavenDeployer> getMavenCentral()

    void artifactory(Action<? super NamedDomainObjectContainer<ArtifactoryMavenDeployer>> action)

    void azure(Action<? super NamedDomainObjectContainer<AzureMavenDeployer>> action)

    void gitea(Action<? super NamedDomainObjectContainer<GiteaMavenDeployer>> action)

    void github(Action<? super NamedDomainObjectContainer<GithubMavenDeployer>> action)

    void gitlab(Action<? super NamedDomainObjectContainer<GitlabMavenDeployer>> action)

    void nexus2(Action<? super NamedDomainObjectContainer<Nexus2MavenDeployer>> action)

    void mavenCentral(Action<? super NamedDomainObjectContainer<MavenCentralMavenDeployer>> action)

    void artifactory(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void azure(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void gitea(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void github(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void gitlab(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void nexus2(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void mavenCentral(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void pomchecker(Action<? super Pomchecker> action)

    void pomchecker(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Pomchecker) Closure<Void> action)

    @CompileStatic
    interface Pomchecker {
        Property<String> getVersion()

        Property<Boolean> getFailOnError()

        Property<Boolean> getFailOnWarning()

        Property<Boolean> getStrict()
    }
}