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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.Docker
import org.jreleaser.gradle.plugin.dsl.Tap
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class DockerImpl extends AbstractDockerConfiguration implements Docker {
    final NamedDomainObjectContainer<DockerSpecImpl> specs
    final Property<Boolean> continueOnError
    final TapImpl repository
    final CommitAuthorImpl commitAuthor

    @Inject
    DockerImpl(ObjectFactory objects) {
        super(objects)
        continueOnError = objects.property(Boolean).convention(Providers.notDefined())
        repository = objects.newInstance(TapImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)

        specs = objects.domainObjectContainer(DockerSpecImpl, new NamedDomainObjectFactory<DockerSpecImpl>() {
            @Override
            DockerSpecImpl create(String name) {
                DockerSpecImpl spec = objects.newInstance(DockerSpecImpl, objects)
                spec.name = name
                return spec
            }
        })
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            continueOnError.present||
            !specs.isEmpty() ||
            repository.isSet() ||
            commitAuthor.isSet()
    }

    @Override
    void repository(Action<? super Tap> action) {
        action.execute(repository)
    }

    @Override
    void commitAuthor(Action<? super CommitAuthor> action) {
        action.execute(commitAuthor)
    }

    @Override
    void repository(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Tap) Closure<Void> action) {
        ConfigureUtil.configure(action, repository)
    }

    @Override
    void commitAuthor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommitAuthor) Closure<Void> action) {
        ConfigureUtil.configure(action, commitAuthor)
    }

    @CompileDynamic
    org.jreleaser.model.Docker toModel() {
        org.jreleaser.model.Docker tool = new org.jreleaser.model.Docker()
        toModel(tool)
        if (continueOnError.present) tool.continueOnError = continueOnError.get()
        if (repository.isSet()) tool.repository = repository.toDockerRepository()
        if (commitAuthor.isSet()) tool.commitAuthor = commitAuthor.toModel()

        specs.each { tool.addSpec(it.toModel()) }

        tool
    }
}
