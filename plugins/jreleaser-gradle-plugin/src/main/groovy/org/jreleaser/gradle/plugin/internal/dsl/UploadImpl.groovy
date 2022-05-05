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
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.Artifactory
import org.jreleaser.gradle.plugin.dsl.FtpUploader
import org.jreleaser.gradle.plugin.dsl.HttpUploader
import org.jreleaser.gradle.plugin.dsl.S3
import org.jreleaser.gradle.plugin.dsl.ScpUploader
import org.jreleaser.gradle.plugin.dsl.SftpUploader
import org.jreleaser.gradle.plugin.dsl.AzureArtifacts
import org.jreleaser.gradle.plugin.dsl.Upload
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@CompileStatic
class UploadImpl implements Upload {
    final Property<Boolean> enabled
    final NamedDomainObjectContainer<Artifactory> artifactory
    final NamedDomainObjectContainer<FtpUploader> ftp
    final NamedDomainObjectContainer<HttpUploader> http
    final NamedDomainObjectContainer<S3> s3
    final NamedDomainObjectContainer<ScpUploader> scp
    final NamedDomainObjectContainer<SftpUploader> sftp
    final NamedDomainObjectContainer<AzureArtifacts> azureArtifacts

    @Inject
    UploadImpl(ObjectFactory objects) {
        enabled = objects.property(Boolean).convention(true)

        artifactory = objects.domainObjectContainer(Artifactory, new NamedDomainObjectFactory<Artifactory>() {
            @Override
            Artifactory create(String name) {
                ArtifactoryImpl a = objects.newInstance(ArtifactoryImpl, objects)
                a.name = name
                return a
            }
        })

        ftp = objects.domainObjectContainer(FtpUploader, new NamedDomainObjectFactory<FtpUploader>() {
            @Override
            FtpUploader create(String name) {
                FtpUploaderImpl h = objects.newInstance(FtpUploaderImpl, objects)
                h.name = name
                return h
            }
        })

        http = objects.domainObjectContainer(HttpUploader, new NamedDomainObjectFactory<HttpUploader>() {
            @Override
            HttpUploader create(String name) {
                HttpUploaderImpl h = objects.newInstance(HttpUploaderImpl, objects)
                h.name = name
                return h
            }
        })

        s3 = objects.domainObjectContainer(S3, new NamedDomainObjectFactory<S3>() {
            @Override
            S3 create(String name) {
                S3Impl s = objects.newInstance(S3Impl, objects)
                s.name = name
                return s
            }
        })

        scp = objects.domainObjectContainer(ScpUploader, new NamedDomainObjectFactory<ScpUploader>() {
            @Override
            ScpUploader create(String name) {
                ScpUploaderImpl h = objects.newInstance(ScpUploaderImpl, objects)
                h.name = name
                return h
            }
        })

        sftp = objects.domainObjectContainer(SftpUploader, new NamedDomainObjectFactory<SftpUploader>() {
            @Override
            SftpUploader create(String name) {
                SftpUploaderImpl h = objects.newInstance(SftpUploaderImpl, objects)
                h.name = name
                return h
            }
        })

        azureArtifacts = objects.domainObjectContainer(AzureArtifacts, new NamedDomainObjectFactory<AzureArtifacts>() {
            @Override
            AzureArtifacts create(String name) {
                AzureArtifactsImpl z = objects.newInstance(AzureArtifactsImpl, objects)
                z.name = name
                return z
            }
        })
    }

    @Override
    void artifactory(Action<? super NamedDomainObjectContainer<Artifactory>> action) {
        action.execute(artifactory)
    }

    @Override
    void ftp(Action<? super NamedDomainObjectContainer<FtpUploader>> action) {
        action.execute(ftp)
    }

    @Override
    void http(Action<? super NamedDomainObjectContainer<HttpUploader>> action) {
        action.execute(http)
    }

    @Override
    void s3(Action<? super NamedDomainObjectContainer<S3>> action) {
        action.execute(s3)
    }

    @Override
    void scp(Action<? super NamedDomainObjectContainer<ScpUploader>> action) {
        action.execute(scp)
    }

    @Override
    void sftp(Action<? super NamedDomainObjectContainer<SftpUploader>> action) {
        action.execute(sftp)
    }

    @Override
    void azureArtifacts(Action<? super NamedDomainObjectContainer<AzureArtifacts>> action) {
        action.execute(azureArtifacts)
    }

    @Override
    void artifactory(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, artifactory)
    }

    @Override
    void ftp(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, ftp)
    }

    @Override
    void http(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, http)
    }

    @Override
    void s3(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, s3)
    }

    @Override
    void scp(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, scp)
    }

    @Override
    void sftp(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, sftp)
    }

    @Override
    void azureArtifacts(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, azureArtifacts)
    }

    @CompileDynamic
    org.jreleaser.model.Upload toModel() {
        org.jreleaser.model.Upload upload = new org.jreleaser.model.Upload()

        artifactory.each { upload.addArtifactory(((ArtifactoryImpl) it).toModel()) }
        ftp.each { upload.addFtp(((FtpUploaderImpl) it).toModel()) }
        http.each { upload.addHttp(((HttpUploaderImpl) it).toModel()) }
        s3.each { upload.addS3(((S3Impl) it).toModel()) }
        scp.each { upload.addScp(((ScpUploaderImpl) it).toModel()) }
        sftp.each { upload.addSftp(((SftpUploaderImpl) it).toModel()) }
        azureArtifacts.each { upload.addAzureArtifacts(((AzureArtifactsImpl) it).toModel()) }

        upload
    }
}
