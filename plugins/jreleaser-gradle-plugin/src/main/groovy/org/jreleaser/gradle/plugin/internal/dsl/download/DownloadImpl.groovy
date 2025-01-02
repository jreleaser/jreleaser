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
package org.jreleaser.gradle.plugin.internal.dsl.download

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.download.Download
import org.jreleaser.gradle.plugin.dsl.download.FtpDownloader
import org.jreleaser.gradle.plugin.dsl.download.HttpDownloader
import org.jreleaser.gradle.plugin.dsl.download.ScpDownloader
import org.jreleaser.gradle.plugin.dsl.download.SftpDownloader
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.1.0
 */
@CompileStatic
class DownloadImpl implements Download {
    final Property<Active> active
    final NamedDomainObjectContainer<FtpDownloader> ftp
    final NamedDomainObjectContainer<HttpDownloader> http
    final NamedDomainObjectContainer<ScpDownloader> scp
    final NamedDomainObjectContainer<SftpDownloader> sftp

    @Inject
    DownloadImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())

        ftp = objects.domainObjectContainer(FtpDownloader, new NamedDomainObjectFactory<FtpDownloader>() {
            @Override
            FtpDownloader create(String name) {
                FtpDownloaderImpl h = objects.newInstance(FtpDownloaderImpl, objects)
                h.name = name
                return h
            }
        })

        http = objects.domainObjectContainer(HttpDownloader, new NamedDomainObjectFactory<HttpDownloader>() {
            @Override
            HttpDownloader create(String name) {
                HttpDownloaderImpl h = objects.newInstance(HttpDownloaderImpl, objects)
                h.name = name
                return h
            }
        })

        scp = objects.domainObjectContainer(ScpDownloader, new NamedDomainObjectFactory<ScpDownloader>() {
            @Override
            ScpDownloader create(String name) {
                ScpDownloaderImpl h = objects.newInstance(ScpDownloaderImpl, objects)
                h.name = name
                return h
            }
        })

        sftp = objects.domainObjectContainer(SftpDownloader, new NamedDomainObjectFactory<SftpDownloader>() {
            @Override
            SftpDownloader create(String name) {
                SftpDownloaderImpl h = objects.newInstance(SftpDownloaderImpl, objects)
                h.name = name
                return h
            }
        })
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void ftp(Action<? super NamedDomainObjectContainer<FtpDownloader>> action) {
        action.execute(ftp)
    }

    @Override
    void http(Action<? super NamedDomainObjectContainer<HttpDownloader>> action) {
        action.execute(http)
    }

    @Override
    void scp(Action<? super NamedDomainObjectContainer<ScpDownloader>> action) {
        action.execute(scp)
    }

    @Override
    void sftp(Action<? super NamedDomainObjectContainer<SftpDownloader>> action) {
        action.execute(sftp)
    }

    @Override
    @CompileDynamic
    void ftp(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, ftp)
    }

    @Override
    @CompileDynamic
    void http(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, http)
    }

    @Override
    @CompileDynamic
    void scp(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, scp)
    }

    @Override
    @CompileDynamic
    void sftp(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, sftp)
    }

    @CompileDynamic
    org.jreleaser.model.internal.download.Download toModel() {
        org.jreleaser.model.internal.download.Download download = new org.jreleaser.model.internal.download.Download()
        if (active.present) download.active = active.get()

        ftp.each { download.addFtp(((FtpDownloaderImpl) it).toModel()) }
        http.each { download.addHttp(((HttpDownloaderImpl) it).toModel()) }
        scp.each { download.addScp(((ScpDownloaderImpl) it).toModel()) }
        sftp.each { download.addSftp(((SftpDownloaderImpl) it).toModel()) }

        download
    }
}
