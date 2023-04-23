/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
package org.jreleaser.gradle.plugin.internal.dsl.upload

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.upload.ArtifactoryUploader
import org.jreleaser.gradle.plugin.dsl.upload.FtpUploader
import org.jreleaser.gradle.plugin.dsl.upload.GiteaUploader
import org.jreleaser.gradle.plugin.dsl.upload.GitlabUploader
import org.jreleaser.gradle.plugin.dsl.upload.HttpUploader
import org.jreleaser.gradle.plugin.dsl.upload.S3Uploader
import org.jreleaser.gradle.plugin.dsl.upload.ScpUploader
import org.jreleaser.gradle.plugin.dsl.upload.SftpUploader
import org.jreleaser.gradle.plugin.dsl.upload.Upload
import org.jreleaser.gradle.plugin.internal.dsl.download.ScpUploaderImpl
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@CompileStatic
class UploadImpl implements Upload {
    final Property<Active> active
    final NamedDomainObjectContainer<ArtifactoryUploader> artifactory
    final NamedDomainObjectContainer<FtpUploader> ftp
    final NamedDomainObjectContainer<GiteaUploader> gitea
    final NamedDomainObjectContainer<GitlabUploader> gitlab
    final NamedDomainObjectContainer<HttpUploader> http
    final NamedDomainObjectContainer<S3Uploader> s3
    final NamedDomainObjectContainer<ScpUploader> scp
    final NamedDomainObjectContainer<SftpUploader> sftp

    @Inject
    UploadImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())

        artifactory = objects.domainObjectContainer(ArtifactoryUploader, new NamedDomainObjectFactory<ArtifactoryUploader>() {
            @Override
            ArtifactoryUploader create(String name) {
                ArtifactoryUploaderImpl a = objects.newInstance(ArtifactoryUploaderImpl, objects)
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

        gitea = objects.domainObjectContainer(GiteaUploader, new NamedDomainObjectFactory<GiteaUploader>() {
            @Override
            GiteaUploader create(String name) {
                GiteaUploaderImpl h = objects.newInstance(GiteaUploaderImpl, objects)
                h.name = name
                return h
            }
        })

        gitlab = objects.domainObjectContainer(GitlabUploader, new NamedDomainObjectFactory<GitlabUploader>() {
            @Override
            GitlabUploader create(String name) {
                GitlabUploaderImpl h = objects.newInstance(GitlabUploaderImpl, objects)
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

        s3 = objects.domainObjectContainer(S3Uploader, new NamedDomainObjectFactory<S3Uploader>() {
            @Override
            S3Uploader create(String name) {
                S3UploaderImpl s = objects.newInstance(S3UploaderImpl, objects)
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
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void artifactory(Action<? super NamedDomainObjectContainer<ArtifactoryUploader>> action) {
        action.execute(artifactory)
    }

    @Override
    void ftp(Action<? super NamedDomainObjectContainer<FtpUploader>> action) {
        action.execute(ftp)
    }

    @Override
    void gitea(Action<? super NamedDomainObjectContainer<GiteaUploader>> action) {
        action.execute(gitea)
    }

    @Override
    void gitlab(Action<? super NamedDomainObjectContainer<GitlabUploader>> action) {
        action.execute(gitlab)
    }

    @Override
    void http(Action<? super NamedDomainObjectContainer<HttpUploader>> action) {
        action.execute(http)
    }

    @Override
    void s3(Action<? super NamedDomainObjectContainer<S3Uploader>> action) {
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
    @CompileDynamic
    void artifactory(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, artifactory)
    }

    @Override
    @CompileDynamic
    void ftp(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, ftp)
    }

    @Override
    @CompileDynamic
    void gitea(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, gitea)
    }

    @Override
    @CompileDynamic
    void gitlab(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, gitlab)
    }

    @Override
    @CompileDynamic
    void http(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, http)
    }

    @Override
    @CompileDynamic
    void s3(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, s3)
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
    org.jreleaser.model.internal.upload.Upload toModel() {
        org.jreleaser.model.internal.upload.Upload upload = new org.jreleaser.model.internal.upload.Upload()
        if (active.present) upload.active = active.get()

        artifactory.each { upload.addArtifactory(((ArtifactoryUploaderImpl) it).toModel()) }
        ftp.each { upload.addFtp(((FtpUploaderImpl) it).toModel()) }
        gitea.each { upload.addGitea(((GiteaUploaderImpl) it).toModel()) }
        gitlab.each { upload.addGitlab(((GitlabUploaderImpl) it).toModel()) }
        http.each { upload.addHttp(((HttpUploaderImpl) it).toModel()) }
        s3.each { upload.addS3(((S3UploaderImpl) it).toModel()) }
        scp.each { upload.addScp(((ScpUploaderImpl) it).toModel()) }
        sftp.each { upload.addSftp(((SftpUploaderImpl) it).toModel()) }

        upload
    }
}
