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
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.Docker
import org.jreleaser.gradle.plugin.dsl.DockerSpec
import org.jreleaser.model.Active
import org.jreleaser.util.StringUtils
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
    final Property<String> downloadUrl
    final DockerRepositoryImpl repository
    final CommitAuthorImpl commitAuthor

    @Inject
    DockerImpl(ObjectFactory objects) {
        super(objects)
        continueOnError = objects.property(Boolean).convention(Providers.notDefined())
        downloadUrl = objects.property(String).convention(Providers.notDefined())
        repository = objects.newInstance(DockerRepositoryImpl, objects)
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
            continueOnError.present ||
            downloadUrl.present ||
            !specs.isEmpty() ||
            repository.isSet() ||
            commitAuthor.isSet()
    }

    @Override
    void repository(Action<? super DockerRepository> action) {
        action.execute(repository)
    }

    @Override
    void specs(Action<? super NamedDomainObjectContainer<? extends DockerSpec>> action) {
        action.execute(specs)
    }

    @Override
    void commitAuthor(Action<? super CommitAuthor> action) {
        action.execute(commitAuthor)
    }

    @Override
    void repository(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DockerRepository) Closure<Void> action) {
        ConfigureUtil.configure(action, repository)
    }

    @Override
    void specs(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, specs)
    }

    @Override
    void commitAuthor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommitAuthor) Closure<Void> action) {
        ConfigureUtil.configure(action, commitAuthor)
    }

    @CompileDynamic
    org.jreleaser.model.Docker toModel() {
        org.jreleaser.model.Docker packager = new org.jreleaser.model.Docker()
        toModel(packager)
        if (continueOnError.present) packager.continueOnError = continueOnError.get()
        if (downloadUrl.present) packager.downloadUrl = downloadUrl.get()
        if (repository.isSet()) packager.repository = repository.toModel()
        if (commitAuthor.isSet()) packager.commitAuthor = commitAuthor.toModel()

        specs.each { packager.addSpec(it.toModel()) }

        packager
    }

    @CompileStatic
    static class DockerRepositoryImpl implements DockerRepository {
        final Property<Active> active
        final Property<String> owner
        final Property<String> name
        final Property<String> tagName
        final Property<String> branch
        final Property<String> username
        final Property<String> token
        final Property<String> commitMessage
        final Property<Boolean> versionedSubfolders

        @Inject
        DockerRepositoryImpl(ObjectFactory objects) {
            active = objects.property(Active).convention(Providers.notDefined())
            owner = objects.property(String).convention(Providers.notDefined())
            name = objects.property(String).convention(Providers.notDefined())
            tagName = objects.property(String).convention(Providers.notDefined())
            branch = objects.property(String).convention(Providers.notDefined())
            username = objects.property(String).convention(Providers.notDefined())
            token = objects.property(String).convention(Providers.notDefined())
            commitMessage = objects.property(String).convention(Providers.notDefined())
            versionedSubfolders = objects.property(Boolean).convention(Providers.notDefined())
        }

        @Override
        void setActive(String str) {
            if (StringUtils.isNotBlank(str)) {
                active.set(Active.of(str.trim()))
            }
        }

        @Internal
        boolean isSet() {
            active.present ||
                owner.present ||
                name.present ||
                tagName.present ||
                branch.present ||
                username.present ||
                versionedSubfolders.present ||
                token.present ||
                commitMessage.present
        }

        org.jreleaser.model.Docker.DockerRepository toModel() {
            org.jreleaser.model.Docker.DockerRepository tap = new org.jreleaser.model.Docker.DockerRepository()
            if (active.present) tap.active = active.get()
            if (owner.present) tap.owner = owner.get()
            if (name.present) tap.name = name.get()
            if (tagName.present) tap.tagName = tagName.get()
            if (branch.present) tap.branch = branch.get()
            if (username.present) tap.name = username.get()
            if (token.present) tap.token = token.get()
            if (commitMessage.present) tap.commitMessage = commitMessage.get()
            if (versionedSubfolders.present) tap.versionedSubfolders = versionedSubfolders.get()
            tap
        }
    }
}
