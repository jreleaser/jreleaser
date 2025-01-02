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
package org.jreleaser.gradle.plugin.internal.dsl.upload

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.upload.ArtifactoryUploader
import org.jreleaser.model.Active
import org.jreleaser.model.Http
import org.jreleaser.util.FileType
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@CompileStatic
class ArtifactoryUploaderImpl extends AbstractWebUploader implements ArtifactoryUploader {
    String name
    final Property<String> host
    final Property<String> username
    final Property<String> password
    final Property<Http.Authorization> authorization

    final NamedDomainObjectContainer<ArtifactoryRepositoryImpl> repositories

    @Inject
    ArtifactoryUploaderImpl(ObjectFactory objects) {
        super(objects)
        host = objects.property(String).convention(Providers.<String> notDefined())
        username = objects.property(String).convention(Providers.<String> notDefined())
        password = objects.property(String).convention(Providers.<String> notDefined())
        authorization = objects.property(Http.Authorization).convention(Providers.<Http.Authorization> notDefined())

        repositories = objects.domainObjectContainer(ArtifactoryRepositoryImpl, new NamedDomainObjectFactory<ArtifactoryRepositoryImpl>() {
            @Override
            ArtifactoryRepositoryImpl create(String name) {
                ArtifactoryRepositoryImpl repository = objects.newInstance(ArtifactoryRepositoryImpl, objects)
                repository.name = name
                repository
            }
        })
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            host.present ||
            username.present ||
            password.present ||
            authorization.present
    }

    @Override
    void setAuthorization(String authorization) {
        this.authorization.set(Http.Authorization.of(authorization))
    }

    @Override
    void repository(Action<? super ArtifactoryRepository> action) {
        action.execute(repositories.maybeCreate("repository-${repositories.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void repository(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ArtifactoryRepository) Closure<Void> action) {
        ConfigureUtil.configure(action, repositories.maybeCreate("repository-${repositories.size()}".toString()))
    }

    org.jreleaser.model.internal.upload.ArtifactoryUploader toModel() {
        org.jreleaser.model.internal.upload.ArtifactoryUploader uploader = new org.jreleaser.model.internal.upload.ArtifactoryUploader()
        uploader.name = name
        fillProperties(uploader)
        if (host.present) uploader.host = host.get()
        if (username.present) uploader.username = username.get()
        if (password.present) uploader.password = password.get()
        if (authorization.present) uploader.authorization = authorization.get()
        for (ArtifactoryRepositoryImpl repository : repositories) {
            uploader.addRepository(repository.toModel())
        }
        uploader
    }

    @CompileStatic
    static class ArtifactoryRepositoryImpl implements ArtifactoryRepository {
        String name
        final Property<Active> active
        final Property<String> path
        final SetProperty<FileType> fileTypes

        @Inject
        ArtifactoryRepositoryImpl(ObjectFactory objects) {
            active = objects.property(Active).convention(Providers.<Active> notDefined())
            path = objects.property(String).convention(Providers.<String> notDefined())
            fileTypes = objects.setProperty(FileType).convention(Providers.<Set<FileType>> notDefined())
        }

        @Override
        void setActive(String str) {
            if (isNotBlank(str)) {
                active.set(Active.of(str.trim()))
            }
        }

        @Override
        void setFileType(String str) {
            if (isNotBlank(str)) {
                fileTypes.add(FileType.of(str.trim()))
            }
        }

        org.jreleaser.model.internal.upload.ArtifactoryUploader.ArtifactoryRepository toModel() {
            org.jreleaser.model.internal.upload.ArtifactoryUploader.ArtifactoryRepository repository = new org.jreleaser.model.internal.upload.ArtifactoryUploader.ArtifactoryRepository()
            if (active.present) repository.active = active.get()
            if (path.present) repository.path = path.get()
            repository.fileTypes = (Set<FileType>) fileTypes.getOrElse([] as Set<FileType>)
            repository
        }
    }
}
