/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property

/**
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@CompileStatic
interface Upload {
    Property<Boolean> getEnabled()

    NamedDomainObjectContainer<Artifactory> getArtifactory()

    NamedDomainObjectContainer<FtpUploader> getFtp()

    NamedDomainObjectContainer<HttpUploader> getHttp()

    NamedDomainObjectContainer<S3> getS3()

    NamedDomainObjectContainer<ScpUploader> getScp()

    NamedDomainObjectContainer<SftpUploader> getSftp()

    NamedDomainObjectContainer<AzureArtifacts> getAzureArtifacts()

    void artifactory(Action<? super NamedDomainObjectContainer<Artifactory>> action)

    void ftp(Action<? super NamedDomainObjectContainer<FtpUploader>> action)

    void http(Action<? super NamedDomainObjectContainer<HttpUploader>> action)

    void s3(Action<? super NamedDomainObjectContainer<S3>> action)

    void scp(Action<? super NamedDomainObjectContainer<ScpUploader>> action)

    void sftp(Action<? super NamedDomainObjectContainer<SftpUploader>> action)

    void azureArtifacts(Action<? super NamedDomainObjectContainer<AzureArtifacts>> action)

    void artifactory(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void ftp(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void http(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void s3(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void scp(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void sftp(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void azureArtifacts(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)
}