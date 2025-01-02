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
package org.jreleaser.gradle.plugin.internal.dsl.packagers

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.common.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.packagers.DockerPackager
import org.jreleaser.gradle.plugin.dsl.packagers.DockerSpec
import org.jreleaser.gradle.plugin.internal.dsl.common.CommitAuthorImpl
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class DockerPackagerImpl extends AbstractDockerConfiguration implements DockerPackager {
    final NamedDomainObjectContainer<DockerSpec> specs
    final Property<Boolean> continueOnError
    final Property<String> downloadUrl
    final DockerRepositoryImpl repository
    final CommitAuthorImpl commitAuthor

    @Inject
    DockerPackagerImpl(ObjectFactory objects) {
        super(objects)
        continueOnError = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        downloadUrl = objects.property(String).convention(Providers.<String> notDefined())
        repository = objects.newInstance(DockerRepositoryImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)

        specs = objects.domainObjectContainer(DockerSpec, new NamedDomainObjectFactory<DockerSpec>() {
            @Override
            DockerSpec create(String name) {
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
    void specs(Action<? super NamedDomainObjectContainer<DockerSpec>> action) {
        action.execute(specs)
    }

    @Override
    void commitAuthor(Action<? super CommitAuthor> action) {
        action.execute(commitAuthor)
    }

    @Override
    @CompileDynamic
    void repository(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DockerRepository) Closure<Void> action) {
        ConfigureUtil.configure(action, repository)
    }

    @Override
    @CompileDynamic
    void specs(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, specs)
    }

    @Override
    @CompileDynamic
    void commitAuthor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommitAuthor) Closure<Void> action) {
        ConfigureUtil.configure(action, commitAuthor)
    }

    @CompileDynamic
    org.jreleaser.model.internal.packagers.DockerPackager toModel() {
        org.jreleaser.model.internal.packagers.DockerPackager packager = new org.jreleaser.model.internal.packagers.DockerPackager()
        toModel(packager)
        if (continueOnError.present) packager.continueOnError = continueOnError.get()
        if (downloadUrl.present) packager.downloadUrl = downloadUrl.get()
        if (repository.isSet()) packager.repository = repository.toModel()
        if (commitAuthor.isSet()) packager.commitAuthor = commitAuthor.toModel()

        specs.each { packager.addSpec(((DockerSpecImpl) it).toModel()) }

        packager
    }

    @CompileStatic
    static class DockerRepositoryImpl implements DockerRepository {
        final Property<Active> active
        final Property<String> repoOwner
        final Property<String> name
        final Property<String> tagName
        final Property<String> branch
        final Property<String> branchPush
        final Property<String> username
        final Property<String> token
        final Property<String> commitMessage
        final Property<Boolean> versionedSubfolders
        final MapProperty<String, Object> extraProperties

        @Inject
        DockerRepositoryImpl(ObjectFactory objects) {
            active = objects.property(Active).convention(Providers.<Active> notDefined())
            repoOwner = objects.property(String).convention(Providers.<String> notDefined())
            name = objects.property(String).convention(Providers.<String> notDefined())
            tagName = objects.property(String).convention(Providers.<String> notDefined())
            branch = objects.property(String).convention(Providers.<String> notDefined())
            branchPush = objects.property(String).convention(Providers.<String> notDefined())
            username = objects.property(String).convention(Providers.<String> notDefined())
            token = objects.property(String).convention(Providers.<String> notDefined())
            commitMessage = objects.property(String).convention(Providers.<String> notDefined())
            versionedSubfolders = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
        }

        @Override
        void setActive(String str) {
            if (isNotBlank(str)) {
                active.set(Active.of(str.trim()))
            }
        }

        @Internal
        boolean isSet() {
            active.present ||
                repoOwner.present ||
                name.present ||
                tagName.present ||
                branch.present ||
                branchPush.present ||
                username.present ||
                versionedSubfolders.present ||
                token.present ||
                commitMessage.present ||
                extraProperties.present
        }

        org.jreleaser.model.internal.packagers.DockerPackager.DockerRepository toModel() {
            org.jreleaser.model.internal.packagers.DockerPackager.DockerRepository tap = new org.jreleaser.model.internal.packagers.DockerPackager.DockerRepository()
            if (active.present) tap.active = active.get()
            if (repoOwner.present) tap.owner = repoOwner.get()
            if (name.present) tap.name = name.get()
            if (tagName.present) tap.tagName = tagName.get()
            if (branch.present) tap.branch = branch.get()
            if (branchPush.present) tap.branchPush = branchPush.get()
            if (username.present) tap.username = username.get()
            if (token.present) tap.token = token.get()
            if (commitMessage.present) tap.commitMessage = commitMessage.get()
            if (versionedSubfolders.present) tap.versionedSubfolders = versionedSubfolders.get()
            if (extraProperties.present) tap.extraProperties.putAll(extraProperties.get())
            tap
        }
    }
}
