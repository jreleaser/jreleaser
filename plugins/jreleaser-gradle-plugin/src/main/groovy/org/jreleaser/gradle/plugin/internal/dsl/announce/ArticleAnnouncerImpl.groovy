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
package org.jreleaser.gradle.plugin.internal.dsl.announce

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.announce.ArticleAnnouncer
import org.jreleaser.gradle.plugin.dsl.common.Artifact
import org.jreleaser.gradle.plugin.dsl.common.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.packagers.Tap
import org.jreleaser.gradle.plugin.internal.dsl.common.ArtifactImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.CommitAuthorImpl
import org.jreleaser.gradle.plugin.internal.dsl.packagers.TapImpl
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.6.0
 */
@CompileStatic
class ArticleAnnouncerImpl extends AbstractAnnouncer implements ArticleAnnouncer {
    private final NamedDomainObjectContainer<ArtifactImpl> files
    final DirectoryProperty templateDirectory
    final TapImpl repository
    final CommitAuthorImpl commitAuthor

    @Inject
    ArticleAnnouncerImpl(ObjectFactory objects) {
        super(objects)

        files = objects.domainObjectContainer(ArtifactImpl, new NamedDomainObjectFactory<ArtifactImpl>() {
            @Override
            ArtifactImpl create(String name) {
                ArtifactImpl artifact = objects.newInstance(ArtifactImpl, objects)
                artifact.name = name
                artifact
            }
        })

        templateDirectory = objects.directoryProperty().convention(Providers.notDefined())
        repository = objects.newInstance(TapImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)
    }

    @Override
    void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory.set(new File(templateDirectory))
    }

    @Override
    void file(Action<? super Artifact> action) {
        action.execute(files.maybeCreate("files-${files.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void file(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, files.maybeCreate("files-${files.size()}".toString()))
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
    @CompileDynamic
    void repository(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Tap) Closure<Void> action) {
        ConfigureUtil.configure(action, repository)
    }

    @Override
    @CompileDynamic
    void commitAuthor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommitAuthor) Closure<Void> action) {
        ConfigureUtil.configure(action, commitAuthor)
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            !files.isEmpty() ||
            templateDirectory.present ||
            repository.isSet() ||
            commitAuthor.isSet()
    }

    org.jreleaser.model.internal.announce.ArticleAnnouncer toModel() {
        org.jreleaser.model.internal.announce.ArticleAnnouncer announcer = new org.jreleaser.model.internal.announce.ArticleAnnouncer()
        fillProperties(announcer)

        for (ArtifactImpl file : files) {
            announcer.addFile(file.toModel())
        }

        if (templateDirectory.present) {
            announcer.templateDirectory = templateDirectory.get().asFile.toPath().toAbsolutePath().toString()
        }

        if (repository.isSet()) announcer.repository = repository.toRepository()
        if (commitAuthor.isSet()) announcer.commitAuthor = commitAuthor.toModel()

        announcer
    }
}
