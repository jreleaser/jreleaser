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
package org.jreleaser.gradle.plugin.dsl.download

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.jreleaser.gradle.plugin.dsl.common.Activatable

/**
 *
 * @author Andres Almiray
 * @since 1.1.0
 */
@CompileStatic
interface Download extends Activatable {
    NamedDomainObjectContainer<FtpDownloader> getFtp()

    NamedDomainObjectContainer<HttpDownloader> getHttp()

    NamedDomainObjectContainer<ScpDownloader> getScp()

    NamedDomainObjectContainer<SftpDownloader> getSftp()

    void ftp(Action<? super NamedDomainObjectContainer<FtpDownloader>> action)

    void http(Action<? super NamedDomainObjectContainer<HttpDownloader>> action)

    void scp(Action<? super NamedDomainObjectContainer<ScpDownloader>> action)

    void sftp(Action<? super NamedDomainObjectContainer<SftpDownloader>> action)

    void ftp(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void http(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void scp(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void sftp(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)
}