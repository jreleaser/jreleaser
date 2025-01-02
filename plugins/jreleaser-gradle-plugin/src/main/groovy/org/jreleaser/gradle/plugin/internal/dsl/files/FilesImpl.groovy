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
package org.jreleaser.gradle.plugin.internal.dsl.files

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.common.Artifact
import org.jreleaser.gradle.plugin.dsl.common.Glob
import org.jreleaser.gradle.plugin.dsl.files.Files
import org.jreleaser.gradle.plugin.internal.dsl.common.ArtifactImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.GlobImpl
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
class FilesImpl implements Files {
    final Property<Active> active
    private final NamedDomainObjectContainer<ArtifactImpl> artifacts
    private final NamedDomainObjectContainer<GlobImpl> globs

    @Inject
    FilesImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        artifacts = objects.domainObjectContainer(ArtifactImpl, new NamedDomainObjectFactory<ArtifactImpl>() {
            @Override
            ArtifactImpl create(String name) {
                ArtifactImpl artifact = objects.newInstance(ArtifactImpl, objects)
                artifact.name = name
                artifact
            }
        })

        globs = objects.domainObjectContainer(GlobImpl, new NamedDomainObjectFactory<GlobImpl>() {
            @Override
            GlobImpl create(String name) {
                GlobImpl glob = objects.newInstance(GlobImpl, objects)
                glob.name = name
                glob
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
    void artifact(Action<? super Artifact> action) {
        action.execute(artifacts.maybeCreate("artifact-${artifacts.size()}".toString()))
    }

    @Override
    void glob(Action<? super Glob> action) {
        action.execute(globs.maybeCreate("glob-${globs.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void artifact(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Artifact) Closure<Void> action) {
        ConfigureUtil.configure(action, artifacts.maybeCreate("artifact-${artifacts.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void glob(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Glob) Closure<Void> action) {
        ConfigureUtil.configure(action, globs.maybeCreate("glob-${globs.size()}".toString()))
    }

    org.jreleaser.model.internal.files.Files toModel() {
        org.jreleaser.model.internal.files.Files files = new org.jreleaser.model.internal.files.Files()
        if (active.present) files.active = active.get()
        for (ArtifactImpl artifact : artifacts) {
            files.addArtifact(artifact.toModel())
        }
        for (GlobImpl glob : globs) {
            files.addGlob(glob.toModel())
        }
        files
    }
}
