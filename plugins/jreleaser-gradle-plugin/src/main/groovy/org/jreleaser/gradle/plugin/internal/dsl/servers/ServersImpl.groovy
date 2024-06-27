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
package org.jreleaser.gradle.plugin.internal.dsl.servers

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.model.ObjectFactory
import org.jreleaser.gradle.plugin.dsl.servers.ForgejoServer
import org.jreleaser.gradle.plugin.dsl.servers.FtpServer
import org.jreleaser.gradle.plugin.dsl.servers.GenericServer
import org.jreleaser.gradle.plugin.dsl.servers.GiteaServer
import org.jreleaser.gradle.plugin.dsl.servers.GithubServer
import org.jreleaser.gradle.plugin.dsl.servers.GitlabServer
import org.jreleaser.gradle.plugin.dsl.servers.HttpServer
import org.jreleaser.gradle.plugin.dsl.servers.Servers
import org.jreleaser.gradle.plugin.dsl.servers.SshServer
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

/**
 * @author Andres Almiray
 * @author Jan Wloka
 * @since 1.20.0
 */
@CompileStatic
class ServersImpl implements Servers {
    final NamedDomainObjectContainer<GenericServer> generic
    final NamedDomainObjectContainer<FtpServer> ftp
    final NamedDomainObjectContainer<HttpServer> http
    final NamedDomainObjectContainer<SshServer> ssh
    final NamedDomainObjectContainer<GiteaServer> gitea
    final NamedDomainObjectContainer<GithubServer> github
    final NamedDomainObjectContainer<GitlabServer> gitlab
    final NamedDomainObjectContainer<ForgejoServer> forgejo

    @Inject
    ServersImpl(ObjectFactory objects) {
        generic = objects.domainObjectContainer(GenericServer, new NamedDomainObjectFactory<GenericServer>() {
            @Override
            GenericServer create(String name) {
                GenericServerImpl h = objects.newInstance(GenericServerImpl, objects)
                h.name = name
                return h
            }
        })

        ftp = objects.domainObjectContainer(FtpServer, new NamedDomainObjectFactory<FtpServer>() {
            @Override
            FtpServer create(String name) {
                FtpServerImpl h = objects.newInstance(FtpServerImpl, objects)
                h.name = name
                return h
            }
        })

        http = objects.domainObjectContainer(HttpServer, new NamedDomainObjectFactory<HttpServer>() {
            @Override
            HttpServer create(String name) {
                HttpServerImpl h = objects.newInstance(HttpServerImpl, objects)
                h.name = name
                return h
            }
        })

        ssh = objects.domainObjectContainer(SshServer, new NamedDomainObjectFactory<SshServer>() {
            @Override
            SshServer create(String name) {
                SshServerImpl h = objects.newInstance(SshServerImpl, objects)
                h.name = name
                return h
            }
        })

        gitea = objects.domainObjectContainer(GiteaServer, new NamedDomainObjectFactory<GiteaServer>() {
            @Override
            GiteaServer create(String name) {
                GiteaServerImpl h = objects.newInstance(GiteaServerImpl, objects)
                h.name = name
                return h
            }
        })

        github = objects.domainObjectContainer(GithubServer, new NamedDomainObjectFactory<GithubServer>() {
            @Override
            GithubServer create(String name) {
                GithubServerImpl h = objects.newInstance(GithubServerImpl, objects)
                h.name = name
                return h
            }
        })

        gitlab = objects.domainObjectContainer(GitlabServer, new NamedDomainObjectFactory<GitlabServer>() {
            @Override
            GitlabServer create(String name) {
                GitlabServerImpl h = objects.newInstance(GitlabServerImpl, objects)
                h.name = name
                return h
            }
        })

        forgejo = objects.domainObjectContainer(ForgejoServer, new NamedDomainObjectFactory<ForgejoServer>() {
            @Override
            ForgejoServer create(String name) {
                ForgejoServerImpl h = objects.newInstance(ForgejoServerImpl, objects)
                h.name = name
                return h
            }
        })
    }

    @Override
    void generic(Action<? super NamedDomainObjectContainer<GenericServer>> action) {
        action.execute(generic)
    }

    @Override
    void ftp(Action<? super NamedDomainObjectContainer<FtpServer>> action) {
        action.execute(ftp)
    }

    @Override
    void http(Action<? super NamedDomainObjectContainer<HttpServer>> action) {
        action.execute(http)
    }

    @Override
    void ssh(Action<? super NamedDomainObjectContainer<SshServer>> action) {
        action.execute(ssh)
    }

    @Override
    void gitea(Action<? super NamedDomainObjectContainer<GiteaServer>> action) {
        action.execute(gitea)
    }

    @Override
    void github(Action<? super NamedDomainObjectContainer<GithubServer>> action) {
        action.execute(github)
    }

    @Override
    void gitlab(Action<? super NamedDomainObjectContainer<GitlabServer>> action) {
        action.execute(gitlab)
    }

    @Override
    void forgejo(Action<? super NamedDomainObjectContainer<ForgejoServer>> action) {
        action.execute(forgejo)
    }

    @Override
    @CompileDynamic
    void generic(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer<GenericServer>) Closure<Void> action) {
        ConfigureUtil.configure(action, generic)
    }

    @Override
    @CompileDynamic
    void ftp(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer<FtpServer>) Closure<Void> action) {
        ConfigureUtil.configure(action, ftp)
    }

    @Override
    @CompileDynamic
    void http(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer<HttpServer>) Closure<Void> action) {
        ConfigureUtil.configure(action, http)
    }

    @Override
    @CompileDynamic
    void ssh(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer<SshServer>) Closure<Void> action) {
        ConfigureUtil.configure(action, ssh)
    }

    @Override
    @CompileDynamic
    void gitea(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer<GiteaServer>) Closure<Void> action) {
        ConfigureUtil.configure(action, gitea)
    }

    @Override
    @CompileDynamic
    void github(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer<GithubServer>) Closure<Void> action) {
        ConfigureUtil.configure(action, github)
    }

    @Override
    @CompileDynamic
    void gitlab(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer<GitlabServer>) Closure<Void> action) {
        ConfigureUtil.configure(action, gitlab)
    }

    @Override
    @CompileDynamic
    void forgejo(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer<ForgejoServer>) Closure<Void> action) {
        ConfigureUtil.configure(action, forgejo)
    }

    @CompileDynamic
    org.jreleaser.model.internal.servers.Servers toModel() {
        org.jreleaser.model.internal.servers.Servers servers = new org.jreleaser.model.internal.servers.Servers()

        generic.each { servers.addGeneric(((GenericServerImpl) it).toModel()) }
        ftp.each { servers.addFtp(((FtpServerImpl) it).toModel()) }
        http.each { servers.addHttp(((HttpServerImpl) it).toModel()) }
        ssh.each { servers.addSsh(((SshServerImpl) it).toModel()) }
        gitea.each { servers.addGitea(((GiteaServerImpl) it).toModel()) }
        github.each { servers.addGithub(((GithubServerImpl) it).toModel()) }
        gitlab.each { servers.addGitlab(((GitlabServerImpl) it).toModel()) }
        forgejo.each { servers.addForgejo(((ForgejoServerImpl) it).toModel()) }

        servers
    }
}
