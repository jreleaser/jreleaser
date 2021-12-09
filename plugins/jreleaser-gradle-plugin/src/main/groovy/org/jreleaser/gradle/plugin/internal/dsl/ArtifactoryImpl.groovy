/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Artifactory
import org.jreleaser.gradle.plugin.dsl.ArtifactoryRepository
import org.jreleaser.model.HttpUploader
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@CompileStatic
class ArtifactoryImpl extends AbstractHttpUploader implements Artifactory {
    String name
    final Property<String> host
    final Property<String> username
    final Property<String> password
    final Property<HttpUploader.Authorization> authorization

    final NamedDomainObjectContainer<ArtifactoryRepositoryImpl> repositories

    @Inject
    ArtifactoryImpl(ObjectFactory objects) {
        super(objects)
        host = objects.property(String).convention(Providers.notDefined())
        username = objects.property(String).convention(Providers.notDefined())
        password = objects.property(String).convention(Providers.notDefined())
        authorization = objects.property(HttpUploader.Authorization).convention(Providers.notDefined())

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
        this.authorization.set(HttpUploader.Authorization.of(authorization))
    }

    @Override
    void repository(Action<? super ArtifactoryRepository> action) {
        action.execute(repositories.maybeCreate("repository-${repositories.size()}".toString()))
    }

    @Override
    void repository(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ArtifactoryRepository) Closure<Void> action) {
        ConfigureUtil.configure(action, repositories.maybeCreate("repository-${repositories.size()}".toString()))
    }

    org.jreleaser.model.Artifactory toModel() {
        org.jreleaser.model.Artifactory artifactory = new org.jreleaser.model.Artifactory()
        artifactory.name = name
        fillProperties(artifactory)
        if (host.present) artifactory.host = host.get()
        if (username.present) artifactory.username = username.get()
        if (password.present) artifactory.password = password.get()
        if (authorization.present) artifactory.authorization = authorization.get()
        for (ArtifactoryRepositoryImpl repository : repositories) {
            artifactory.addRepository(repository.toModel())
        }
        artifactory
    }
}
