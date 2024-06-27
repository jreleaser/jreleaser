/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 The JReleaser authors.
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
package org.jreleaser.gradle.plugin.dsl.servers

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

/**
 * @author Andres Almiray
 * @author Jan Wloka
 * @since 1.20.0
 */
interface Servers {
    NamedDomainObjectContainer<GenericServer> getGeneric()

    NamedDomainObjectContainer<FtpServer> getFtp()

    NamedDomainObjectContainer<HttpServer> getHttp()

    NamedDomainObjectContainer<SshServer> getSsh()

    NamedDomainObjectContainer<GithubServer> getGithub()

    NamedDomainObjectContainer<GitlabServer> getGitlab()

    NamedDomainObjectContainer<GiteaServer> getGitea()

    NamedDomainObjectContainer<ForgejoServer> getForgejo()

    void generic(Action<? super NamedDomainObjectContainer<GenericServer>> action)

    void ftp(Action<? super NamedDomainObjectContainer<FtpServer>> action)

    void http(Action<? super NamedDomainObjectContainer<HttpServer>> action)

    void ssh(Action<? super NamedDomainObjectContainer<SshServer>> action)

    void github(Action<? super NamedDomainObjectContainer<GithubServer>> action)

    void gitlab(Action<? super NamedDomainObjectContainer<GitlabServer>> action)

    void gitea(Action<? super NamedDomainObjectContainer<GiteaServer>> action)

    void forgejo(Action<? super NamedDomainObjectContainer<ForgejoServer>> action)

    void generic(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer<GenericServer>) Closure<Void> action)

    void ftp(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer<FtpServer>) Closure<Void> action)

    void http(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer<HttpServer>) Closure<Void> action)

    void ssh(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer<SshServer>) Closure<Void> action)

    void github(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer<GithubServer>) Closure<Void> action)

    void gitlab(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer<GitlabServer>) Closure<Void> action)

    void gitea(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer<GiteaServer>) Closure<Void> action)

    void forgejo(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer<ForgejoServer>) Closure<Void> action)
}
