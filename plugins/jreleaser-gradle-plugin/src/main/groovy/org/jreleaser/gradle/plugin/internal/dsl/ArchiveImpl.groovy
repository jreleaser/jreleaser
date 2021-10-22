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
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Archive
import org.jreleaser.gradle.plugin.dsl.FileSet
import org.jreleaser.model.Distribution.DistributionType
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ArchiveImpl extends AbstractAssembler implements Archive {
    String name
    final Property<String> archiveName
    final Property<DistributionType> distributionType
    final Property<Boolean> attachPlatform
    final SetProperty<org.jreleaser.model.Archive.Format> formats
    final NamedDomainObjectContainer<FileSetImpl> fileSets

    @Inject
    ArchiveImpl(ObjectFactory objects) {
        super(objects)
        archiveName = objects.property(String).convention(Providers.notDefined())
        distributionType = objects.property(DistributionType).convention(DistributionType.JAVA_BINARY)
        attachPlatform = objects.property(Boolean).convention(Providers.notDefined())
        formats = objects.setProperty(org.jreleaser.model.Archive.Format).convention(Providers.notDefined())

        fileSets = objects.domainObjectContainer(FileSetImpl, new NamedDomainObjectFactory<FileSetImpl>() {
            @Override
            FileSetImpl create(String name) {
                FileSetImpl fs = objects.newInstance(FileSetImpl, objects)
                fs.name = name
                fs
            }
        })
    }

    @Internal
    boolean isSet() {
        super.isSet() ||
            archiveName.present ||
            distributionType.present ||
            attachPlatform.present ||
            formats.present ||
            !fileSets.isEmpty()
    }

    @Override
    void setDistributionType(String distributionType) {
        this.distributionType.set(DistributionType.of(distributionType))
    }

    @Override
    void format(String format) {
        if (isNotBlank(format)) {
            formats.add(org.jreleaser.model.Archive.Format.of(format))
        }
    }

    @Override
    void fileSet(Action<? super FileSet> action) {
        action.execute(fileSets.maybeCreate("fileSet-${fileSets.size()}".toString()))
    }

    @Override
    void fileSet(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = FileSet) Closure<Void> action) {
        ConfigureUtil.configure(action, fileSets.maybeCreate("fileSet-${fileSets.size()}".toString()))
    }

    org.jreleaser.model.Archive toModel() {
        org.jreleaser.model.Archive archive = new org.jreleaser.model.Archive()
        archive.name = name
        fillProperties(archive)
        if (archiveName.present) archive.archiveName = archiveName.get()
        if (attachPlatform.present) archive.attachPlatform = attachPlatform.get()
        archive.distributionType = distributionType.get()
        for (FileSetImpl fileSet : fileSets) {
            archive.addFileSet(fileSet.toModel())
        }
        archive.formats = (Set<org.jreleaser.model.Archive.Format>) formats.getOrElse([] as Set<org.jreleaser.model.Archive.Format>)
        archive
    }
}
